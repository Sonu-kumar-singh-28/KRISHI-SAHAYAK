"""
Utilities for streaming Text-To-Speech (TTS).

Supports multiple backends:
- edge_tts (default)
- UNMUTE (Kyutai) via WebSocket streaming

Backend is selected via the TTS_BACKEND environment variable:
- TTS_BACKEND=edge  (default)
- TTS_BACKEND=unmute

For UNMUTE, set UNMUTE_WS_URL to the WebSocket endpoint, e.g. ws://localhost:7331/tts
"""

from __future__ import annotations

import os
import json
import base64
from typing import Optional, Tuple, Union, AsyncGenerator

import edge_tts
from dotenv import load_dotenv

load_dotenv()


async def stream_tts(text: str, voice: str = "en-IN-NeerjaNeural") -> AsyncGenerator[bytes, None]:
    """
    Stream TTS audio chunks for the given text.

    Args:
        text (str): Text to synthesize.
        voice (str): Voice name (used by edge_tts backend only).

    Yields:
        bytes: Raw audio bytes for playback/streaming.
    """
    backend = os.getenv("TTS_BACKEND", "edge").lower()
    if backend == "unmute":
        async for data in _stream_tts_unmute(text):
            yield data
    else:
        async for data in _stream_tts_edge(text, voice):
            yield data


async def _stream_tts_edge(text: str, voice: str) -> AsyncGenerator[bytes, None]:
    """
    Stream TTS using Microsoft edge_tts.
    """
    communicate = edge_tts.Communicate(text, voice=voice)
    async for chunk in communicate.stream():
        if chunk["type"] == "audio":
            yield chunk["data"]


async def _stream_tts_unmute(text: str) -> AsyncGenerator[bytes, None]:
    """
    Stream TTS using an UNMUTE (Kyutai) WebSocket server.

    Notes:
        - This function is designed to work with a variety of UNMUTE-style servers.
        - It supports two inbound message formats:
            1) Binary frames -> treated directly as audio bytes
            2) JSON text frames with base64-encoded audio under key "audio"
               and optional {"event": "done"} termination signal.
        - Set UNMUTE_WS_URL in .env to your server endpoint.
    """
    import asyncio
    import websockets

    url = os.getenv("UNMUTE_WS_URL", "ws://localhost:7331/tts")

    # Reason: Keep connection simple and robust. We send the text as a single message.
    # Advanced servers may support incremental text; adopt as needed.
    async with websockets.connect(url, max_size=None) as ws:  # type: ignore
        # Try sending as JSON first; fall back to raw text if needed.
        start_payload = json.dumps({"type": "synthesize", "text": text})
        try:
            await ws.send(start_payload)
        except Exception:
            await ws.send(text)

        while True:
            try:
                msg = await ws.recv()
            except asyncio.CancelledError:
                break
            except Exception:
                # Connection closed or error
                break

            audio, done = _extract_audio_from_unmute_message(msg)
            if audio:
                yield audio
            if done:
                break


def _extract_audio_from_unmute_message(msg: Union[str, bytes]) -> Tuple[Optional[bytes], bool]:
    """
    Extract audio bytes and completion flag from a UNMUTE WebSocket message.

    Args:
        msg (Union[str, bytes]): Incoming WebSocket message.

    Returns:
        Tuple[Optional[bytes], bool]: (audio_bytes_or_None, is_done)
    """
    # Binary frames are treated as direct audio bytes
    if isinstance(msg, (bytes, bytearray)):
        return bytes(msg), False

    # Text frames: try to parse JSON with base64 audio
    try:
        data = json.loads(msg)
        if isinstance(data, dict):
            if "audio" in data:
                try:
                    return base64.b64decode(data["audio"]), False
                except Exception:
                    return None, False
            if data.get("event") == "done":
                return None, True
    except json.JSONDecodeError:
        pass

    return None, False