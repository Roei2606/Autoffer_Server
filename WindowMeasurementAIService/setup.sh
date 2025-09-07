#!/bin/bash

# macOS Setup Script for Window Measurement AI Service
echo "Setting up WindowMeasurementAIService..."

# Create Python virtual environment
python3 -m venv venv

# Activate virtual environment
source venv/bin/activate

# Upgrade pip
pip install --upgrade pip

# Install dependencies
pip install -r requirements.txt

echo "✅ Setup complete!"
echo ""
echo "To start the service:"
echo "1. cd WindowMeasurementAIService"
echo "2. source venv/bin/activate"
echo "3. python main.py"
