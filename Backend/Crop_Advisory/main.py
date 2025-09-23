import sys
import traceback
from pathlib import Path

# Add project root to Python path
project_root = Path(__file__).parent.resolve()
if str(project_root) not in sys.path:
    sys.path.append(str(project_root))


from fastapi import FastAPI, WebSocket, Query, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from query_engine.rag_pipeline import load_crop_vectorstore, ask
from utils.tts import stream_tts
from utils.sessions import active_session, create_session, delete_session
import json

app = FastAPI()

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load agriculture vectorstore
vectorstore = load_crop_vectorstore()


@app.websocket("/ws/stream")
async def websocket_endpoint(websocket: WebSocket, sid=Query(None)):
    session_id = create_session()
    await websocket.accept()

    try:
        while True:
            raw_data = await websocket.receive_text()
            query = raw_data.strip()
            print(f"[Farmer]: {query}")

            # Get farmer profile (if stored in session)
            session_info = active_session[session_id]
            farmer_profile = session_info.get("user_data", "No farmer profile available")

            # Run query through RAG
            response = ask(session_id, query, vectorstore, farmer_profile)

            print(f"[AgriAdvisor]: {response}")

            # Send back text
            await websocket.send_text(json.dumps({"type": "text", "data": response}))

            # Send back TTS audio
            async for chunk in stream_tts(response):
                await websocket.send_bytes(chunk)

    except WebSocketDisconnect:
        if session_id in active_session:
            delete_session(session_id)
        print(f"WebSocket disconnected for session {session_id}")

    except Exception as e:
        print(f"Error in session {session_id}:", e)
        traceback.print_exc()
        if session_id in active_session:
            delete_session(session_id)
