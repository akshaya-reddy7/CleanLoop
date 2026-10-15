"""
CleanLoop MobileNetV2 Waste Classifier Training Script
======================================================
This script fine-tunes MobileNetV2 on the TrashNet dataset (or equivalent 6-class waste dataset):
  - Cardboard (403 images)
  - Glass (501 images)
  - Metal (410 images)
  - Paper (594 images)
  - Plastic (482 images)
  - Trash (137 images)
  Total: 2,527 images

Usage:
  python train.py --data_dir /path/to/dataset --epochs 25 --batch_size 32 --lr 0.0001
"""

import os
import argparse
import time
import copy

try:
    import torch
    import torch.nn as nn
    import torch.optim as optim
    from torchvision import datasets, models, transforms
    from torch.utils.data import DataLoader, random_split
except ImportError:
    print("PyTorch and torchvision are required to run this training script.")
    print("Install via: pip install torch torchvision")
    exit(1)


def parse_args():
    parser = argparse.ArgumentParser(description="Train MobileNetV2 on Waste Classification")
    parser.add_argument("--data_dir", type=str, default="./dataset", help="Path to dataset root folder")
    parser.add_argument("--epochs", type=int, default=25, help="Number of training epochs")
    parser.add_argument("--batch_size", type=int, default=32, help="Batch size")
    parser.add_argument("--lr", type=float, default=1e-4, help="Learning rate")
    parser.add_argument("--output_dir", type=str, default="./weights", help="Output directory for model weights")
    return parser.parse_args()


def train_model(data_dir: str, epochs: int, batch_size: int, lr: float, output_dir: str):
    device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")

    # Standard data transformations
    train_transforms = transforms.Compose([
        transforms.RandomResizedCrop(224),
        transforms.RandomHorizontalFlip(),
        transforms.RandomRotation(15),
        transforms.ColorJitter(brightness=0.2, contrast=0.2),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ])

    val_transforms = transforms.Compose([
        transforms.Resize(256),
        transforms.CenterCrop(224),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ])

    if not os.path.exists(data_dir):
        print(f"Error: Dataset directory '{data_dir}' not found.")
        print("Please structure your dataset into 6 sub-folders:")
        print("  dataset/cardboard, dataset/glass, dataset/metal, dataset/paper, dataset/plastic, dataset/trash")
        return

    full_dataset = datasets.ImageFolder(data_dir, transform=train_transforms)
    class_names = full_dataset.classes
    print(f"Loaded dataset with {len(full_dataset)} images across classes: {class_names}")

    # 80/20 train/val split
    val_size = int(0.2 * len(full_dataset))
    train_size = len(full_dataset) - val_size
    train_subset, val_subset = random_split(full_dataset, [train_size, val_size])

    train_loader = DataLoader(train_subset, batch_size=batch_size, shuffle=True, num_workers=2)
    val_loader = DataLoader(val_subset, batch_size=batch_size, shuffle=False, num_workers=2)

    # Initialize MobileNetV2 with pretrained ImageNet weights
    model = models.mobilenet_v2(weights=models.MobileNet_V2_Weights.DEFAULT)

    # Fine-tune classifier head
    in_features = model.classifier[1].in_features
    model.classifier = nn.Sequential(
        nn.Dropout(p=0.2),
        nn.Linear(in_features, len(class_names))
    )

    model = model.to(device)

    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=lr, weight_decay=1e-4)
    scheduler = optim.lr_scheduler.StepLR(optimizer, step_size=7, gamma=0.1)

    best_model_wts = copy.deepcopy(model.state_dict())
    best_acc = 0.0

    os.makedirs(output_dir, exist_ok=True)
    save_path = os.path.join(output_dir, "mobilenetv2_waste.pth")

    for epoch in range(epochs):
        print(f"\n--- Epoch {epoch + 1}/{epochs} ---")

        # Training phase
        model.train()
        running_loss = 0.0
        running_corrects = 0

        for inputs, labels in train_loader:
            inputs, labels = inputs.to(device), labels.to(device)
            optimizer.zero_grad()

            outputs = model(inputs)
            _, preds = torch.max(outputs, 1)
            loss = criterion(outputs, labels)

            loss.backward()
            optimizer.step()

            running_loss += loss.item() * inputs.size(0)
            running_corrects += torch.sum(preds == labels.data)

        scheduler.step()

        train_loss = running_loss / train_size
        train_acc = running_corrects.double() / train_size
        print(f"Train Loss: {train_loss:.4f} Acc: {train_acc:.4f}")

        # Validation phase
        model.eval()
        val_loss = 0.0
        val_corrects = 0

        with torch.no_grad():
            for inputs, labels in val_loader:
                inputs, labels = inputs.to(device), labels.to(device)
                outputs = model(inputs)
                _, preds = torch.max(outputs, 1)
                loss = criterion(outputs, labels)

                val_loss += loss.item() * inputs.size(0)
                val_corrects += torch.sum(preds == labels.data)

        val_loss = val_loss / val_size
        val_acc = val_corrects.double() / val_size
        print(f"Val Loss: {val_loss:.4f} Acc: {val_acc:.4f}")

        if val_acc > best_acc:
            best_acc = val_acc
            best_model_wts = copy.deepcopy(model.state_dict())
            torch.save(best_model_wts, save_path)
            print(f"Saved new best model with Validation Accuracy: {best_acc:.4f} to {save_path}")

    print(f"\nTraining complete. Best Validation Accuracy: {best_acc:.4f}")


if __name__ == "__main__":
    args = parse_args()
    train_model(args.data_dir, args.epochs, args.batch_size, args.lr, args.output_dir)
