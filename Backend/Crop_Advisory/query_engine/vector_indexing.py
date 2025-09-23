# query_engine/vector_indexing.py

from pathlib import Path
import os
import sys
import pandas as pd
from concurrent.futures import ThreadPoolExecutor
from langchain_community.document_loaders import (
    TextLoader,
    CSVLoader,
    PyPDFLoader,
    Docx2txtLoader,
)
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain.schema import Document


def process_file(file: Path, splitter):
    """Helper to load and split one file into chunks."""
    ext = file.suffix.lower()
    try:
        if ext == ".txt":
            loader = TextLoader(str(file), encoding="utf-8")
            docs = loader.load()

        elif ext == ".csv":
            loader = CSVLoader(str(file), encoding="utf-8")
            docs = loader.load()

        elif ext in [".xls", ".xlsx"]:
            df = pd.read_excel(file)
            text = df.to_csv(index=False)
            docs = [Document(page_content=text, metadata={"source": str(file)})]

        elif ext == ".pdf":
            loader = PyPDFLoader(str(file))
            docs = loader.load()

        elif ext == ".docx":
            loader = Docx2txtLoader(str(file))
            docs = loader.load()

        else:
            print(f"⚠️ Skipping unsupported file: {file}")
            return []

        chunks = splitter.split_documents(docs)
        print(f"✅ {len(chunks)} chunks created from {file}")
        return chunks

    except Exception as e:
        print(f"❌ Error loading {file}: {e}")
        return []


def build_crop_vectorstore(
    data_dir: str = "data/text_files",
    index_dir: str = "faiss_index/agmarket",
    chunk_size: int = 400,            # 🔧 Larger chunks = fewer vectors = faster retrieval
    chunk_overlap: int = 50,
    embedding_model: str = "sentence-transformers/all-MiniLM-L6-v2",
    max_workers: int = 4,
    incremental: bool = True,
):
    """
    Build and save FAISS vectorstore from text, csv, excel, pdf, and docx files.

    Optimized for CPU deployment.
    """
    docs_path = Path(data_dir)
    if not docs_path.exists():
        raise FileNotFoundError(f"Data directory not found: {docs_path.resolve()}")

    splitter = RecursiveCharacterTextSplitter(
        chunk_size=chunk_size, chunk_overlap=chunk_overlap
    )

    files = list(docs_path.rglob("*.*"))
    if not files:
        raise ValueError("No files found in data directory.")

    print(f"📂 Found {len(files)} files.")

    # ✅ Force CPU usage
    print("🖥 Using device: cpu (forced)")
    embeddings = HuggingFaceEmbeddings(
        model_name=embedding_model,
        model_kwargs={"device": "cpu"}   # ✅ force CPU only
    )

    index_path = Path(index_dir)
    vectorstore = None

    # Load existing index if incremental
    if incremental and index_path.exists():
        try:
            vectorstore = FAISS.load_local(
                index_path, embeddings, allow_dangerous_deserialization=True
            )
            print(f"📦 Loaded existing FAISS index from {index_path.resolve()}")
        except Exception as e:
            print(f"⚠️ Could not load existing index: {e}")
            vectorstore = None

    # Get already indexed files
    existing_sources = set()
    if vectorstore:
        try:
            existing_sources = {
                doc.metadata["source"]
                for doc in vectorstore.docstore._dict.values()
            }
        except Exception:
            pass

    new_docs = []
    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        results = executor.map(lambda f: process_file(f, splitter), files)
        for r in results:
            for doc in r:
                if str(doc.metadata.get("source")) not in existing_sources:
                    new_docs.append(doc)

    if not new_docs and vectorstore:
        print("✅ No new documents to add. Using existing FAISS index.")
        return vectorstore

    if new_docs:
        print(f"➕ Adding {len(new_docs)} new document chunks to FAISS index...")
        if vectorstore:
            vectorstore.add_documents(new_docs)
        else:
            vectorstore = FAISS.from_documents(new_docs, embeddings)

    # save index
    index_path.mkdir(parents=True, exist_ok=True)
    vectorstore.save_local(index_path)
    print(f"💾 FAISS vectorstore saved at: {index_path.resolve()}")

    return vectorstore


if __name__ == "__main__":
    model = sys.argv[1] if len(sys.argv) > 1 else "sentence-transformers/all-MiniLM-L6-v2"
    print(f"🚀 Building FAISS vectorstore with model: {model}")
    build_crop_vectorstore(embedding_model=model)
