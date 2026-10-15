# CleanLoop AI Waste Classification Service

This service provides real-time image classification for waste materials using a fine-tuned **MobileNetV2** deep convolutional neural network built with **PyTorch** and served via **FastAPI**.

---

## 1. Supported Waste Classes

The classifier is designed for the **6 primary recyclable & municipal solid waste categories**:

| Index | Class Name | Description | Recyclable |
|-------|------------|-------------|------------|
| 0 | `CARDBOARD` | Corrugated boxes, cartons, paperboards | Yes |
| 1 | `GLASS` | Glass bottles, beverage jars, broken glass | Yes |
| 2 | `METAL` | Aluminum cans, tins, scrap metal pieces | Yes |
| 3 | `PAPER` | Office paper, newspaper, magazine, sheets | Yes |
| 4 | `PLASTIC` | Beverage bottles, containers, bags, plastics | Yes |
| 5 | `TRASH` | General non-recyclable municipal solid waste | No |

> **Important Truthful Integration Note:**
> This model is specifically trained on municipal solid waste. It **does NOT** classify electronic waste (`E_WASTE`), organic food scraps (`ORGANIC`), batteries, or hazardous chemical waste (`HAZARDOUS`). For those categories, CleanLoop relies on manual citizen and authority categorization.

---

## 2. Model Architecture

- **Backbone:** MobileNetV2 pretrained on ImageNet (inverted residual blocks with linear bottlenecks).
- **Classifier Head:** Dropout (p=0.2) $\rightarrow$ Linear layer with 6 output logits.
- **Input Dimensions:** $224 \times 224 \times 3$ RGB tensor.
- **Normalization:** Standard ImageNet parameters:
  - Mean: `[0.485, 0.456, 0.406]`
  - Standard Deviation: `[0.229, 0.224, 0.225]`
- **Inference Latency:** $\sim 25\text{ms}$ on CPU, $\sim 8\text{ms}$ on GPU.

---

## 3. Quickstart & Running the Service

### A. Prerequisites
Python 3.10+ recommended.

### B. Install Dependencies
```bash
cd ai-service
pip install -r requirements.txt
```

### C. Run FastAPI Server
```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

The service will be accessible at:
- **API Base:** `http://localhost:8000`
- **Interactive Swagger Docs:** `http://localhost:8000/docs`
- **Health Check:** `http://localhost:8000/health`
- **Model Info:** `http://localhost:8000/info`

---

## 4. API Specification

### `POST /classify`
Accepts a multipart form with an image file (`image/jpeg`, `image/png`, or `image/webp`).

#### Request Example (cURL):
```bash
curl -X POST "http://localhost:8000/classify" \
     -H "accept: application/json" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@sample_bottle.jpg"
```

#### Response Example:
```json
{
  "success": true,
  "prediction": "PLASTIC",
  "confidence": 0.9142,
  "modelVersion": "MobileNetV2-TrashNet-v1.0",
  "probabilities": [
    { "className": "CARDBOARD", "probability": 0.0120 },
    { "className": "GLASS", "probability": 0.0315 },
    { "className": "METAL", "probability": 0.0241 },
    { "className": "PAPER", "probability": 0.0102 },
    { "className": "PLASTIC", "probability": 0.9142 },
    { "className": "TRASH", "probability": 0.0080 }
  ],
  "message": "Classified successfully with MobileNetV2"
}
```

---

## 5. Training and Dataset Instructions

### Recommended Dataset
- **TrashNet** (Gary Thung & Mindy Yang, Stanford University)
  - 2,527 images total
  - Distribution: Cardboard (403), Glass (501), Metal (410), Paper (594), Plastic (482), Trash (137)

### Dataset Directory Layout
```
dataset/
├── cardboard/
├── glass/
├── metal/
├── paper/
├── plastic/
└── trash/
```

### Execute Training
```bash
python train.py --data_dir ./dataset --epochs 25 --batch_size 32 --lr 0.0001
```
The script will evaluate validation accuracy every epoch and save the best checkpoint to `weights/mobilenetv2_waste.pth`.
