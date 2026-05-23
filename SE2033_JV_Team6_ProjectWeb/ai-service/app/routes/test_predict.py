"""
Routes for AI prediction endpoints
"""
from fastapi import APIRouter

router = APIRouter(prefix="/api/predict", tags=["predict"])

@router.post("/")
async def predict(file: bytes):
    """Predict diagnosis from medical image"""
    # Prediction logic here
    pass

