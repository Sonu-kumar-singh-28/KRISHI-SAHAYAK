# query_engine/rag_pipeline.py

from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_community.vectorstores import FAISS
from langchain_huggingface import HuggingFaceEmbeddings
from langchain.chains import create_history_aware_retriever, create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.messages import BaseMessage
import os
from dotenv import load_dotenv
from typing import Dict, List

load_dotenv()
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")

# ---- LLM ----
llm = ChatGoogleGenerativeAI(
    model="gemini-2.0-flash-lite",
    temperature=0.4,
    max_output_tokens=256,
    google_api_key=GOOGLE_API_KEY,
)

# ---- Session Memory ----
session_store: Dict[str, BaseChatMessageHistory] = {}

class SlidingWindowHistory(BaseChatMessageHistory):
    def __init__(self, window_size: int = 8):
        self.window_size = window_size
        self._messages: List[BaseMessage] = []
    @property
    def messages(self) -> List[BaseMessage]:
        return self._messages
    def add_message(self, message: BaseMessage) -> None:
        self._messages.append(message)
        if len(self._messages) > self.window_size:
            self._messages = self._messages[-self.window_size:]
    def clear(self) -> None:
        self._messages = []

def get_session_history(session_id: str) -> BaseChatMessageHistory:
    if session_id not in session_store:
        session_store[session_id] = SlidingWindowHistory(window_size=8)
    return session_store[session_id]

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

# ---- Conversational RAG ----
def create_crop_rag_chain(vectorstore):
    contextualize_q_prompt = ChatPromptTemplate.from_messages([
        ("system", "Rewrite the latest farmer question into a standalone query if needed."),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ])

    history_aware_retriever = create_history_aware_retriever(
        llm, vectorstore.as_retriever(), contextualize_q_prompt
    )

    qa_prompt = ChatPromptTemplate.from_messages([
        ("system", """
You are Khrishi Sahayak, a helpful AI assistant for farmers in India. Your purpose is to provide practical, cost-effective, and data-driven agricultural advice. All responses must be in simple, direct language.
Core Function: Your primary role is to assist with crop advisory, cost-effective farming techniques, and disease management.
Polite Redirection: If a user asks for anything not related to agriculture, politely decline and steer the conversation back to farming. For example, "I can't help with that, but I can assist you with your farming needs. How can I help you with your crops today?"
Cost-Effective Advice: Always prioritize solutions that are financially viable for the farmer. Focus on methods that save money and increase profit.
Crop Advisory Protocol:
If a user asks for a crop advisory, you must first ask for three key pieces of information: land size (in acres or bighas), investment budget, and location (state and district).
Once you have this information, use your knowledge base of Indian agriculture to provide a comprehensive guide. This guide must include:
Recommended Crop: The best crop for their region's climate and market.
Best Variety: A high-yield and disease-resistant variety of that crop.
Investment Details: A breakdown of the estimated costs for seeds, fertilizers, pesticides, and labor.
Sowing Time: The ideal time of year to sow for maximum yield.
Fertilizer and Irrigation: A schedule for fertilizing and a water-saving irrigation plan.
Estimated Profit: A realistic calculation of their potential profit.
Plant Disease Protocol: If a user describes a plant disease, identify it and provide a simple, cost-effective treatment plan using easily available and safe products.
Language and Tone: Speak in a helpful and respectful manner. Use simple vocabulary and avoid technical jargon. Your goal is to satisfy the farmer with clear and useful information.
Strictly No markdown should be used in your responses. Output should be in plain text.
Context from knowledge base:
{context}

Farmer Profile:
{farmer_profile}
"""),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ])

    question_answer_chain = create_stuff_documents_chain(llm, qa_prompt)
    rag_chain = create_retrieval_chain(history_aware_retriever, question_answer_chain)

    conversational_rag_chain = RunnableWithMessageHistory(
        rag_chain,
        get_session_history,
        input_messages_key="input",
        history_messages_key="chat_history",
        output_messages_key="answer",
    )
    return conversational_rag_chain

# ---- Entry point ----
_conversational_chain = None

def initialize_chain(vectorstore):
    global _conversational_chain
    if _conversational_chain is None:
        _conversational_chain = create_crop_rag_chain(vectorstore)
    return _conversational_chain

def ask(SID: str, query: str, vectorstore, farmer_profile: str = None):
    chain = initialize_chain(vectorstore)
    result = chain.invoke(
        {
            "input": query,
            "farmer_profile": farmer_profile or "No farmer profile provided."
        },
        config={"configurable": {"session_id": SID}}
    )
    return result.get("answer", "Sorry, I couldn't generate an answer.")
