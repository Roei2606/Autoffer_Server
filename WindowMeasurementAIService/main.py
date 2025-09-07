import json
import cv2
import os
from pathlib import Path
import numpy as np
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware

import microservice_scripts.microservice_utils as utils_for_microservice
import configs.microservice_settings as settings_for_microservice


class SimplifiedMeasurementSystem:
    """
    Simplified measurement system that outputs only width_mm and height_mm.
    """

    def __init__(self):
        self.board_cols = settings_for_microservice.CHARUCOBOARD_COLCOUNT
        self.board_rows = settings_for_microservice.CHARUCOBOARD_ROWCOUNT
        self.square_size_mm = settings_for_microservice.SQUARE_LENGTH * 1000  # mm
        self.charuco_board = utils_for_microservice.create_charuco_board()
        self.detector_params = utils_for_microservice.get_detector_params()

        # Load YOLO model
        self.detection_dataset_dir = settings_for_microservice.SUPER_TESTER
        self.yolo_model, self.model_path = (
            utils_for_microservice.load_model_and_validate_paths(
                self.detection_dataset_dir
            )
        )

    def calculate_pixel_to_mm_ratio(self, charuco_corners, charuco_ids):
        if charuco_corners is None or len(charuco_corners) < 2:
            return None

        pixel_distances = []
        id_to_corner = {corner_id[0]: charuco_corners[i][0] for i, corner_id in enumerate(charuco_ids)}
        detected_ids = [cid[0] for cid in charuco_ids]

        for i, id1 in enumerate(detected_ids):
            for j, id2 in enumerate(detected_ids):
                if i >= j:
                    continue
                row1, col1 = divmod(id1, self.board_cols - 1)
                row2, col2 = divmod(id2, self.board_cols - 1)
                if (abs(row1 - row2) == 1 and col1 == col2) or (abs(col1 - col2) == 1 and row1 == row2):
                    pixel_dist = np.linalg.norm(id_to_corner[id1] - id_to_corner[id2])
                    pixel_distances.append(pixel_dist)

        if not pixel_distances:
            return None
        return np.mean(pixel_distances) / self.square_size_mm

    def get_window_dimensions(self, mask, pixels_per_mm):
        mask_binary = (mask > 0.5).astype(np.uint8)
        contours, _ = cv2.findContours(mask_binary, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        if not contours:
            return None, None

        rect = cv2.minAreaRect(max(contours, key=cv2.contourArea))
        (_, _), (width, height), _ = rect
        width_mm = max(width, height) / pixels_per_mm
        height_mm = min(width, height) / pixels_per_mm
        return width_mm, height_mm

    def process_image(self, image_path):
        image = cv2.imread(str(image_path))
        if image is None:
            return {"error": f"Could not load image: {image_path}"}

        marker_ids, charuco_corners, charuco_ids = utils_for_microservice.detect_charuco_board(
            image, settings_for_microservice.ARUCO_DICT, self.charuco_board, self.detector_params
        )
        if marker_ids is None or charuco_corners is None or charuco_ids is None:
            return {"error": "Failed to detect ChArUco board"}

        pixels_per_mm = self.calculate_pixel_to_mm_ratio(charuco_corners, charuco_ids)
        if pixels_per_mm is None:
            return {"error": "Failed to calculate calibration"}

        result, _, masks = utils_for_microservice.process_single_image_with_mask_extraction(
            self.yolo_model, image_path, settings_for_microservice.CONFIDENCE_THRESHOLD,
            settings_for_microservice.IOU_THRESHOLD,
        )
        if result is None or masks is None or len(masks) == 0:
            return {"error": "No windows detected"}

        width_mm, height_mm = self.get_window_dimensions(masks[0], pixels_per_mm)
        if width_mm is None or height_mm is None:
            return {"error": "Failed to measure window dimensions"}

        return {"width_mm": int(round(width_mm)), "height_mm": int(round(height_mm))}


def measure_window_simple(image_path):
    system = SimplifiedMeasurementSystem()
    return system.process_image(image_path)


# ---------------- FastAPI ----------------
app = FastAPI(title="Simplified Window Measurement Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
async def health():
    return {"status": "healthy", "service": "simplified-window-measurement"}


@app.post("/measure")
async def measure(file: UploadFile = File(...)):
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")

    temp_path = Path("temp_upload.jpg")
    with open(temp_path, "wb") as f:
        f.write(await file.read())

    result = measure_window_simple(temp_path)
    os.remove(temp_path)
    return result


# ---------------- Standalone script ----------------
if __name__ == "__main__":
    output_dir = "measurement_results_simplified"
    images_paths = Path(settings_for_microservice.SUPER_TESTER).glob("*")
    results_file = Path(output_dir) / "simple_measurements.json"
    all_results = []

    os.makedirs(output_dir, exist_ok=True)

    for image_path in images_paths:
        try:
            result = measure_window_simple(image_path)
            all_results.append(result)
            print(result)
        except Exception as e:
            all_results.append({"error": str(e)})

    with open(results_file, "w") as f:
        json.dump(utils_for_microservice.convert_numpy_types(all_results), f, indent=2)

    print(f"Results saved to: {results_file}")
    print(f"Processed {len(all_results)} images")
