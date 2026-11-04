import os
import math
from PIL import Image, ImageDraw

def create_fluxion_logo(size=512):
    # Render at 4x for supersampled antialiasing
    scale = 4
    w = size * scale
    h = size * scale
    
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 1. Dark obsidian rounded squircle
    radius = int(w * 0.22)
    # Background squircle
    draw.rounded_rectangle(
        [int(w * 0.04), int(h * 0.04), int(w * 0.96), int(h * 0.96)],
        radius=radius,
        fill=(13, 13, 17, 255),
        outline=(45, 45, 52, 255),
        width=int(w * 0.02)
    )

    # 2. Sleek geometric Fluxion flow mark (Chevron / Lightning stream)
    # Coordinates normalized from 0 to 1
    # Points representing a modern, sharp geometric stream bolt:
    # A dynamic lightning-arrow flow that screams high speed & purity
    
    pts = [
        (0.56, 0.20),  # Top right apex
        (0.38, 0.48),  # Mid left inner
        (0.50, 0.48),  # Mid right jog
        (0.42, 0.80),  # Bottom arrow point
        (0.68, 0.46),  # Mid right outer
        (0.54, 0.46),  # Mid left jog
    ]
    
    scaled_pts = [(int(x * w), int(y * h)) for x, y in pts]
    
    # Draw ivory polygon with rounded line joints
    draw.polygon(scaled_pts, fill=(250, 250, 252, 255))
    draw.line(scaled_pts + [scaled_pts[0]], fill=(250, 250, 252, 255), width=int(w * 0.015), joint="round")

    # Downsample with Lanczos filter for crisp edge antialiasing
    result = img.resize((size, size), Image.Resampling.LANCZOS)
    return result

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    master_icon = create_fluxion_logo(512)
    master_icon.save(os.path.join(base_dir, "icon.png"), "PNG")
    print("Created icon.png (512x512)")

    # 1. Generate Windows .ico
    ico_sizes = [(16, 16), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)]
    master_icon.save(
        os.path.join(base_dir, "icon.ico"),
        format="ICO",
        sizes=ico_sizes
    )
    print("Created icon.ico")

    # 2. Generate Android mipmap PNGs
    android_res_dir = os.path.join(base_dir, "android-app", "app", "src", "main", "res")
    mipmap_targets = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    for folder, px in mipmap_targets.items():
        folder_path = os.path.join(android_res_dir, folder)
        os.makedirs(folder_path, exist_ok=True)
        
        resized = master_icon.resize((px, px), Image.Resampling.LANCZOS)
        resized.save(os.path.join(folder_path, "ic_launcher.png"), "PNG")
        resized.save(os.path.join(folder_path, "ic_launcher_round.png"), "PNG")
        
        # Also remove old webp if present so android uses our pngs
        webp_1 = os.path.join(folder_path, "ic_launcher.webp")
        webp_2 = os.path.join(folder_path, "ic_launcher_round.webp")
        if os.path.exists(webp_1): os.remove(webp_1)
        if os.path.exists(webp_2): os.remove(webp_2)
        print(f"Updated {folder} ({px}x{px})")

if __name__ == "__main__":
    main()
