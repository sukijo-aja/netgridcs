import os
from PIL import Image, ImageDraw

src_img_path = "/home/masjo/.gemini/antigravity/brain/f44e0c66-88a6-4cf5-9022-fc3bdcabde55/android_starter_icon_1785606785892.png"
res_dir = "app/src/main/res"

sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192
}

def create_round_image(img, size):
    # Create circular mask
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size, size), fill=255)
    
    # Apply mask
    result = img.copy()
    result.putalpha(mask)
    return result

# Load source image
img = Image.open(src_img_path).convert("RGBA")

for dpi, size in sizes.items():
    folder = os.path.join(res_dir, f"mipmap-{dpi}")
    if not os.path.exists(folder):
        continue
        
    # Standard icon (scaled)
    scaled_img = img.resize((size, size), Image.LANCZOS)
    
    # Save standard
    scaled_img.save(os.path.join(folder, "ic_launcher.webp"), "WEBP")
    
    # Save round
    round_img = create_round_image(scaled_img, size)
    round_img.save(os.path.join(folder, "ic_launcher_round.webp"), "WEBP")
    
    # Save foreground (using standard for now)
    scaled_img.save(os.path.join(folder, "ic_launcher_foreground.webp"), "WEBP")

print("All launcher icons updated!")
