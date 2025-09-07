import cv2
import os
import numpy as np
import configs.microservice_settings as settings_for_microservice
from ultralytics import YOLO


def get_detector_params():
    detector_params = cv2.aruco.DetectorParameters()
    detector_params.adaptiveThreshWinSizeMin = (
        settings_for_microservice.ADAPTIVE_THRESH_WIN_SIZE_MIN
    )
    detector_params.adaptiveThreshWinSizeMax = (
        settings_for_microservice.ADAPTIVE_THRESH_WIN_SIZE_MAX
    )
    detector_params.adaptiveThreshWinSizeStep = (
        settings_for_microservice.ADAPTIVE_THRESH_WIN_SIZE_STEP
    )
    detector_params.minMarkerPerimeterRate = (
        settings_for_microservice.MIN_MARKER_PERIMETER_RATE
    )
    detector_params.maxMarkerPerimeterRate = (
        settings_for_microservice.MAX_MARKER_PERIMETER_RATE
    )
    detector_params.cornerRefinementMethod = (
        settings_for_microservice.CORNER_REFINEMENT_METHOD
    )
    return detector_params


def create_charuco_board():
    return cv2.aruco.CharucoBoard(
        size=(
            settings_for_microservice.CHARUCOBOARD_COLCOUNT,
            settings_for_microservice.CHARUCOBOARD_ROWCOUNT,
        ),
        squareLength=settings_for_microservice.SQUARE_LENGTH,
        markerLength=settings_for_microservice.MARKER_LENGTH,
        dictionary=settings_for_microservice.ARUCO_DICT,
    )


def detect_charuco_board(image, aruco_dict, charuco_board, detector_params):
    """Detect ChArUco board"""
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Detect markers with improved parameters
    corners, markers_ids, rejected = cv2.aruco.detectMarkers(
        gray, aruco_dict, parameters=detector_params
    )

    if (
            markers_ids is not None
            and len(markers_ids) >= settings_for_microservice.MIN_MARKERS_PER_IMAGE
    ):
        # Interpolate ChArUco corners
        retval, charuco_corners, charuco_ids = cv2.aruco.interpolateCornersCharuco(
            markerCorners=corners,
            markerIds=markers_ids,
            image=gray,
            board=charuco_board,
        )
        if (
                retval
                and charuco_corners is not None
                and charuco_ids is not None
                and len(charuco_ids) >= settings_for_microservice.MIN_CORNERS_PER_IMAGE
        ):
            return markers_ids, charuco_corners, charuco_ids

    return None, None, None


def load_model_and_validate_paths(test_dir=None):
    """Load YOLO model and validate all required paths."""
    best_model_path = f"{settings_for_microservice.TRAINING_OUTPUT_DIR}/{settings_for_microservice.MODEL_NAME}/weights/best.pt"

    if not os.path.exists(best_model_path):
        raise FileNotFoundError(f"Model not found at: {best_model_path}")

    if not os.path.exists(test_dir):
        raise FileNotFoundError(f"Test images directory not found: {test_dir}")

    print(f"Loading model from: {best_model_path}")
    model = YOLO(best_model_path)
    return model, best_model_path


def process_single_image_with_mask_extraction(
        model, img_path, confidence_threshold, iou_threshold
):
    """Process a single image and extract mask information."""
    image = cv2.imread(str(img_path))
    if image is None:
        print(f"Failed to load image: {img_path}")
        return None, None, None

    results = model.predict(
        task="segment",
        source=str(img_path),
        imgsz=settings_for_microservice.YOLO_INPUT_SIZE,
        rect=False,
        conf=confidence_threshold,
        iou=iou_threshold,
        save=False,
        verbose=False,
        max_det=1,
        agnostic_nms=True,
        retina_masks=True,
    )

    result = results[0]

    masks = None
    if result.masks is not None:
        masks = result.masks.data.cpu().numpy()

    return result, image, masks


def convert_numpy_types(obj):
    """Convert numpy types to Python native types for JSON serialization."""
    if isinstance(obj, np.ndarray):
        return obj.tolist()
    elif isinstance(obj, (np.float32, np.float64)):
        return float(obj)
    elif isinstance(obj, (np.int32, np.int64)):
        return int(obj)
    elif isinstance(obj, dict):
        return {key: convert_numpy_types(value) for key, value in obj.items()}
    elif isinstance(obj, list):
        return [convert_numpy_types(item) for item in obj]
    return obj