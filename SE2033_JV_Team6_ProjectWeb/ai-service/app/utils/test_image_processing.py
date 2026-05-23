"""
Image Processing Utilities
"""
import cv2
import numpy as np

class ImageProcessor:
    """Utility class for image processing"""

    @staticmethod
    def load_image(image_path):
        """Load image from file"""
        image = cv2.imread(image_path)
        return image

    @staticmethod
    def preprocess_image(image):
        """Preprocess image for model input"""
        # Preprocessing logic
        return image

    @staticmethod
    def resize_image(image, size=(224, 224)):
        """Resize image to specified dimensions"""
        resized = cv2.resize(image, size)
        return resized

