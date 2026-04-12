from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import logging

app = FastAPI(title="Job Portal AI Recommendation Engine")
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load the model directly into memory upon boot
logger.info("Loading sentence-transformers/all-MiniLM-L6-v2 model...")
model = SentenceTransformer('all-MiniLM-L6-v2')
logger.info("Model loaded successfully.")

class RequestData(BaseModel):
    candidate_text: str
    job_texts: list[str]

@app.post("/recommend")
def recommend(data: RequestData):
    if not data.candidate_text or not data.job_texts:
        return {"scores": []}
        
    try:
        candidate_embedding = model.encode([data.candidate_text])
        job_embeddings = model.encode(data.job_texts)

        similarities = cosine_similarity(candidate_embedding, job_embeddings)[0]

        return {
            "scores": similarities.tolist()
        }
    except Exception as e:
        logger.error(f"Error calculating embeddings: {e}")
        return {"scores": []}

@app.get("/health")
def health():
    return {"status": "ok"}
