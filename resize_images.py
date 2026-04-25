from PIL import Image
import os

TARGET = (400, 225)

for filename in os.listdir("."):
    lower = filename.lower()
    if lower.endswith(".jpg") or lower.endswith(".png"):
        fmt = "JPEG" if lower.endswith(".jpg") else "PNG"
        with Image.open(filename) as img:
            resized = img.resize(TARGET, Image.LANCZOS)
            save_kwargs = {"quality": 90} if fmt == "JPEG" else {}
            resized.save(filename, fmt, **save_kwargs)
            print(f"{filename} → {TARGET[0]}x{TARGET[1]}")
