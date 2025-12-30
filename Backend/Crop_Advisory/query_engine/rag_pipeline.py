# query_engine/rag_pipeline.py

from langchain_groq import ChatGroq
from langchain_community.vectorstores import FAISS
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
import os
import time
from dotenv import load_dotenv
from typing import Dict, List

load_dotenv()
GROQ_API_KEY = os.getenv("GROQ_API_KEY")

# ---- LLM (Groq - Fast & Free) ----
llm = ChatGroq(
    model="llama-3.3-70b-versatile",  # Fast, free model on Groq
    temperature=0.4,
    max_tokens=256,
    api_key=GROQ_API_KEY,
)

# ---- Simple Session Memory (in-memory) ----
session_history: Dict[str, List[str]] = {}
MAX_HISTORY = 4  # Keep last 4 exchanges

def get_history(session_id: str) -> str:
    """Get formatted chat history for a session."""
    if session_id not in session_history:
        session_history[session_id] = []
    history = session_history[session_id]
    return "\n".join(history[-MAX_HISTORY:]) if history else "No previous conversation."

def add_to_history(session_id: str, user_msg: str, bot_msg: str):
    """Add exchange to session history."""
    if session_id not in session_history:
        session_history[session_id] = []
    session_history[session_id].append(f"Farmer: {user_msg}")
    session_history[session_id].append(f"Assistant: {bot_msg}")
    # Trim to max size
    if len(session_history[session_id]) > MAX_HISTORY * 2:
        session_history[session_id] = session_history[session_id][-MAX_HISTORY * 2:]

# ---- Vectorstore Loader ----
def load_crop_vectorstore():
    embeddings = HuggingFaceEmbeddings(model_name="BAAI/bge-small-en-v1.5")
    path = "faiss_index/agmarket"
    vectorstore = FAISS.load_local(
        folder_path=path,
        embeddings=embeddings,
        allow_dangerous_deserialization=True
    )
    print("✅ Crop advisory FAISS vectorstore loaded.")
    return vectorstore

# ---- Simple RAG Prompt (Single LLM call) ----
PROMPT_TEMPLATE = """You are Khrishi Sahayak, a helpful AI assistant for farmers in India.

Rules:
- Give practical, cost-effective agricultural advice
- Use simple language, avoid jargon
- If question is not about farming, politely redirect to agriculture topics
- No markdown formatting, use plain text only

Context from knowledge base:
{context}

Farmer Profile: {farmer_profile}

Recent Conversation:
{history}

Farmer's Question: {question}

Your helpful response:"""

prompt = ChatPromptTemplate.from_template(PROMPT_TEMPLATE)

# ---- Rate Limiting ----
_last_call_time = 0
MIN_CALL_INTERVAL = 0.5  # 500ms between API calls

def _rate_limit():
    """Simple rate limiter to prevent API overload."""
    global _last_call_time
    elapsed = time.time() - _last_call_time
    if elapsed < MIN_CALL_INTERVAL:
        time.sleep(MIN_CALL_INTERVAL - elapsed)
    _last_call_time = time.time()

# ---- Main Ask Function ----
def ask(SID: str, query: str, vectorstore, farmer_profile: str = None) -> str:
    """
    Simple RAG query - ONE LLM call only.
    """
    _rate_limit()
    
    try:
        # Retrieve relevant documents
        retriever = vectorstore.as_retriever(search_kwargs={"k": 3})
        docs = retriever.invoke(query)
        context = "\n\n".join([doc.page_content for doc in docs])
        
        # Get chat history
        history = get_history(SID)
        
        # Build chain: prompt -> llm -> parse output
        chain = prompt | llm | StrOutputParser()
        
        # Invoke with all inputs
        response = chain.invoke({
            "context": context,
            "farmer_profile": farmer_profile or "No farmer profile provided.",
            "history": history,
            "question": query
        })
        
        # Save to history
        add_to_history(SID, query, response)
        
        return response
    
    except Exception as e:
        error_msg = str(e)
        if "429" in error_msg or "rate" in error_msg.lower():
            return "Sorry, the service is temporarily busy. Please try again in a few minutes."
        print(f"❌ Error in ask(): {e}")
        return "Sorry, I encountered an error. Please try again."
