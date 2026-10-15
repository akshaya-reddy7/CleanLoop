import logging
from datetime import datetime
from typing import List, Dict, Any

from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from model import classifier, CLASSES, MODEL_VERSION, TORCH_AVAILABLE

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("cleanloop-ai-api")

app = FastAPI(
    title="CleanLoop AI Waste Classification Service",
    description="FastAPI service serving MobileNetV2 image classifier for 6 waste categories (Cardboard, Glass, Metal, Paper, Plastic, Trash).",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class ClassProbability(BaseModel):
    className: str
    probability: float


class PredictionResponse(BaseModel):
    success: bool
    prediction: str
    confidence: float
    modelVersion: str
    probabilities: List[ClassProbability]
    message: str


class InfoResponse(BaseModel):
    serviceName: str
    architecture: str
    supportedClasses: List[str]
    inputDimensions: str
    modelVersion: str
    torchLoaded: bool
    notes: str


@app.get("/health")
def health_check() -> Dict[str, Any]:
    return {
        "status": "UP",
        "service": "cleanloop-ai-service",
        "timestamp": datetime.utcnow().isoformat(),
        "torchAvailable": TORCH_AVAILABLE,
        "classesCount": len(CLASSES)
    }


@app.get("/info", response_model=InfoResponse)
def get_model_info():
    return InfoResponse(
        serviceName="CleanLoop AI Waste Classification Service",
        architecture="MobileNetV2 (Transfer Learning from ImageNet)",
        supportedClasses=CLASSES,
        inputDimensions="224x224 RGB",
        modelVersion=MODEL_VERSION,
        torchLoaded=TORCH_AVAILABLE,
        notes="Classifies images into 6 standard categories: Cardboard, Glass, Metal, Paper, Plastic, Trash. Does not recognize e-waste, hazardous or medical waste without specialized fine-tuning."
    )


@app.post("/classify", response_model=PredictionResponse)
async def classify_waste_image(file: UploadFile = File(...)):
    # Validate MIME type
    allowed_types = ["image/jpeg", "image/png", "image/webp", "image/jpg"]
    if file.content_type and file.content_type.lower() not in allowed_types:
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported image type: {file.content_type}. Only JPG, PNG, and WebP images are supported."
        )

    try:
        image_bytes = await file.read()
        if not image_bytes or len(image_bytes) == 0:
            raise HTTPException(status_code=400, detail="Empty image file received.")

        result = classifier.predict(image_bytes)
        return PredictionResponse(**result)
    except ValueError as ve:
        raise HTTPException(status_code=400, detail=str(ve))
    except Exception as e:
        logger.error("Error during image inference: %s", e)
        raise HTTPException(status_code=500, detail=f"Image inference error: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
