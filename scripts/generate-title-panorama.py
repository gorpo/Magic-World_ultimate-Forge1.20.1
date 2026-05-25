from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageOps


ROOT = Path(__file__).resolve().parents[1]
SOURCE_CANDIDATES = (
    "panorama_preview_faces.png",
    "testar_este_novo_panorama.png",
)
SOURCE_GLOB = "panorama_para_edi*.png"
FACE_SIZE = 2048
EDGE_CROP = max(18, FACE_SIZE // 56)
BOTTOM_CROP = max(8, FACE_SIZE // 128)

# CubeMapTexture loads _1, _3, _5, _4, _0, _2 into OpenGL cube faces:
# +X, -X, +Y, -Y, +Z, -Z. Therefore the vanilla file mapping is:
# panorama_0 = front/+Z, panorama_1 = right/+X,
# panorama_2 = back/-Z, panorama_3 = left/-X.
# The title screen model is flipped on X, so top/bottom land swapped
# compared with the raw OpenGL cube layer names.
MINECRAFT_FACE_INDEX = {
    "front": 0,
    "right": 1,
    "back": 2,
    "left": 3,
    "bottom": 5,
    "top": 4,
}


def find_source() -> Path:
    for candidate in SOURCE_CANDIDATES:
        path = ROOT / "screenshots" / candidate
        if path.exists():
            return path

    matches = sorted((ROOT / "screenshots").glob(SOURCE_GLOB))
    if not matches:
        raise FileNotFoundError(f"No source image found for {SOURCE_CANDIDATES} or {SOURCE_GLOB}")
    return matches[0]


def crop_face(source: Image.Image, left: float, top: float, right: float, bottom: float) -> Image.Image:
    width, height = source.size
    box = (
        int(width * left),
        int(height * top),
        int(width * right),
        int(height * bottom),
    )
    return source.crop(box).resize((FACE_SIZE, FACE_SIZE), Image.Resampling.LANCZOS)


def soften_edges(face: Image.Image, edge_width: int = 72) -> Image.Image:
    blurred = face.filter(ImageFilter.GaussianBlur(7.0))
    mask = Image.new("L", (FACE_SIZE, FACE_SIZE), 0)
    draw = ImageDraw.Draw(mask)

    for offset in range(edge_width):
        alpha = int(255 * (1.0 - offset / edge_width) ** 1.8)
        draw.rectangle((offset, offset, FACE_SIZE - 1 - offset, FACE_SIZE - 1 - offset), outline=alpha)

    mask = mask.filter(ImageFilter.GaussianBlur(8.0))
    return Image.composite(blurred, face, mask)


def make_sky(source: Image.Image) -> Image.Image:
    sky = crop_face(source, 0.18, 0.0, 0.82, 0.38)
    sky = sky.filter(ImageFilter.GaussianBlur(7.0))
    return ImageOps.autocontrast(sky, cutoff=1)


def make_grass_floor(source: Image.Image) -> Image.Image:
    source_width, source_height = source.size
    sample = source.crop(
        (
            int(source_width * 0.22),
            int(source_height * 0.66),
            int(source_width * 0.88),
            int(source_height * 0.98),
        )
    ).resize((FACE_SIZE, FACE_SIZE), Image.Resampling.BICUBIC)
    sample = sample.filter(ImageFilter.GaussianBlur(14.0))

    noise = Image.effect_noise((FACE_SIZE, FACE_SIZE), 76).convert("L")
    grass = ImageOps.colorize(noise, black="#183412", white="#6a8a2b")
    grass = grass.filter(ImageFilter.GaussianBlur(1.2))

    mixed = Image.blend(grass, sample, 0.38)

    shadow = Image.new("L", (FACE_SIZE, FACE_SIZE), 0)
    draw = ImageDraw.Draw(shadow)
    for y in range(FACE_SIZE):
        alpha = int(92 * (y / FACE_SIZE))
        draw.line((0, y, FACE_SIZE, y), fill=alpha)

    dark = Image.new("RGB", (FACE_SIZE, FACE_SIZE), "#081207")
    mixed = Image.composite(dark, mixed, shadow)
    return mixed.filter(ImageFilter.GaussianBlur(1.0))


def make_faces(source: Image.Image) -> dict[str, Image.Image]:
    width, height = source.size
    if width % 4 == 0 and height % 3 == 0 and width // 4 == height // 3:
        return polish_faces(make_faces_from_horizontal_cross(source))

    faces = {
        "front": crop_face(source, 0.20, 0.03, 0.76, 0.88),
        "right": crop_face(source, 0.52, 0.02, 1.0, 0.90),
        "back": crop_face(source, 0.34, 0.0, 0.92, 0.72).filter(ImageFilter.GaussianBlur(2.5)),
        "left": crop_face(source, 0.0, 0.02, 0.48, 0.90),
        "bottom": make_grass_floor(source),
        "top": make_sky(source),
    }

    for key in ("front", "right", "back", "left"):
        faces[key] = soften_edges(faces[key])

    return polish_faces(faces)


def make_faces_from_horizontal_cross(source: Image.Image) -> dict[str, Image.Image]:
    tile = source.size[0] // 4

    def cell(column: int, row: int) -> Image.Image:
        face = source.crop(
            (
                column * tile,
                row * tile,
                (column + 1) * tile,
                (row + 1) * tile,
            )
        )
        return face.resize((FACE_SIZE, FACE_SIZE), Image.Resampling.LANCZOS)

    side_strip = source.crop((0, tile, 4 * tile, 2 * tile)).resize((FACE_SIZE * 4, FACE_SIZE), Image.Resampling.LANCZOS)

    # Layout:
    #         top
    # left  front  right  back
    #       bottom
    faces = {
        "top": cell(1, 0),
        "left": make_side_face_from_strip(side_strip, "left"),
        "front": make_side_face_from_strip(side_strip, "front"),
        "right": make_side_face_from_strip(side_strip, "right"),
        "back": make_side_face_from_strip(side_strip, "back"),
        "bottom": cell(1, 2),
    }

    faces["back"] = fill_edge_band(faces["back"], right=EDGE_CROP, bottom=BOTTOM_CROP)
    faces["left"] = fill_edge_band(faces["left"], left=EDGE_CROP, bottom=BOTTOM_CROP)
    return faces


def polish_faces(faces: dict[str, Image.Image]) -> dict[str, Image.Image]:
    return {name: polish_face(face) for name, face in faces.items()}


def polish_face(face: Image.Image) -> Image.Image:
    polished = face.convert("RGB")
    polished = ImageEnhance.Color(polished).enhance(1.08)
    polished = ImageEnhance.Contrast(polished).enhance(1.06)
    polished = ImageEnhance.Sharpness(polished).enhance(1.18)
    return polished.filter(ImageFilter.UnsharpMask(radius=1.0, percent=65, threshold=3))


def fill_edge_band(face: Image.Image, left: int = 0, right: int = 0, bottom: int = 0) -> Image.Image:
    crop_box = (
        left,
        0,
        FACE_SIZE - right,
        FACE_SIZE - bottom,
    )
    return face.crop(crop_box).resize((FACE_SIZE, FACE_SIZE), Image.Resampling.LANCZOS)


def make_side_face_from_strip(strip: Image.Image, face: str) -> Image.Image:
    source = strip.convert("RGB")
    output = Image.new("RGB", (FACE_SIZE, FACE_SIZE))
    source_pixels = source.load()
    output_pixels = output.load()
    strip_width = source.width
    strip_height = source.height

    for py in range(FACE_SIZE):
        v = (py + 0.5) / FACE_SIZE
        y = 1.0 - 2.0 * v
        sy = max(0, min(strip_height - 1, int(v * strip_height)))

        for px in range(FACE_SIZE):
            u = (px + 0.5) / FACE_SIZE
            local = 2.0 * u - 1.0

            if face == "front":
                x = local
                z = 1.0
            elif face == "right":
                x = 1.0
                z = -local
            elif face == "back":
                x = -local
                z = -1.0
            else:
                x = -1.0
                z = local

            yaw = math.atan2(x, z)
            sx = int((((yaw / (math.pi / 2.0)) + 1.5) % 4.0) * FACE_SIZE)
            output_pixels[px, py] = source_pixels[max(0, min(strip_width - 1, sx)), sy]

    return output


def save_face(face: Image.Image, index: int) -> None:
    targets = [
        ROOT / "src/main/resources/assets/minecraft/textures/gui/title/background" / f"panorama_{index}.png",
        ROOT / "screenshots" / f"panorama{index + 1}.png",
    ]

    for target in targets:
        target.parent.mkdir(parents=True, exist_ok=True)
        face.save(target, optimize=True)


def save_previews(faces: dict[str, Image.Image]) -> None:
    preview = Image.new("RGB", (FACE_SIZE * 4, FACE_SIZE * 3), "#081018")
    positions = {
        "top": (FACE_SIZE, 0),
        "left": (0, FACE_SIZE),
        "front": (FACE_SIZE, FACE_SIZE),
        "right": (FACE_SIZE * 2, FACE_SIZE),
        "back": (FACE_SIZE * 3, FACE_SIZE),
        "bottom": (FACE_SIZE, FACE_SIZE * 2),
    }

    draw = ImageDraw.Draw(preview)
    for name, position in positions.items():
        preview.paste(faces[name], position)
        index = MINECRAFT_FACE_INDEX[name]
        draw.text((position[0] + 18, position[1] + 18), f"{name} -> panorama_{index}.png", fill=(230, 245, 255))

    preview.save(ROOT / "screenshots" / "panorama_preview_faces_ordered.png", optimize=True)


def sample_cube(faces: dict[str, Image.Image], x: float, y: float, z: float) -> tuple[int, int, int]:
    ax = abs(x)
    ay = abs(y)
    az = abs(z)

    if az >= ax and az >= ay:
        if z >= 0:
            face = "front"
            u = 0.5 + x / (2.0 * az)
        else:
            face = "back"
            u = 0.5 - x / (2.0 * az)
        v = 0.5 - y / (2.0 * az)
    elif ax >= ay:
        if x >= 0:
            face = "right"
            u = 0.5 - z / (2.0 * ax)
        else:
            face = "left"
            u = 0.5 + z / (2.0 * ax)
        v = 0.5 - y / (2.0 * ax)
    else:
        if y >= 0:
            face = "top"
            u = 0.5 + x / (2.0 * ay)
            v = 0.5 + z / (2.0 * ay)
        else:
            face = "bottom"
            u = 0.5 + x / (2.0 * ay)
            v = 0.5 - z / (2.0 * ay)

    px = max(0, min(FACE_SIZE - 1, int(u * (FACE_SIZE - 1))))
    py = max(0, min(FACE_SIZE - 1, int(v * (FACE_SIZE - 1))))
    return faces[face].getpixel((px, py))


def render_view(faces: dict[str, Image.Image], yaw_degrees: float, width: int = 480, height: int = 270) -> Image.Image:
    image = Image.new("RGB", (width, height))
    pixels = image.load()

    yaw = math.radians(yaw_degrees)
    sin_yaw = math.sin(yaw)
    cos_yaw = math.cos(yaw)
    fov = math.radians(85.0)
    scale = math.tan(fov / 2.0)
    aspect = width / height

    for py in range(height):
        ndc_y = (1.0 - 2.0 * ((py + 0.5) / height)) * scale
        for px in range(width):
            ndc_x = (2.0 * ((px + 0.5) / width) - 1.0) * scale * aspect
            x = ndc_x
            y = ndc_y
            z = 1.0
            rotated_x = x * cos_yaw + z * sin_yaw
            rotated_z = z * cos_yaw - x * sin_yaw
            pixels[px, py] = sample_cube(faces, rotated_x, y, rotated_z)

    return image


def save_3d_preview(faces: dict[str, Image.Image]) -> None:
    yaws = (0, 45, 90, 135, 180, 225, 270, 315)
    frames = []
    for yaw in yaws:
        frame = render_view(faces, yaw)
        draw = ImageDraw.Draw(frame)
        draw.rectangle((8, 8, 94, 30), fill=(0, 0, 0))
        draw.text((14, 13), f"yaw {yaw}", fill=(230, 245, 255))
        frames.append(frame)

    strip = Image.new("RGB", (frames[0].width * 4, frames[0].height * 2), "#081018")
    for index, frame in enumerate(frames):
        x = (index % 4) * frame.width
        y = (index // 4) * frame.height
        strip.paste(frame, (x, y))

    strip.save(ROOT / "screenshots" / "panorama_preview_3d_strip.png", optimize=True)
    frames[0].save(
        ROOT / "screenshots" / "panorama_preview_3d_spin.gif",
        save_all=True,
        append_images=frames[1:],
        duration=240,
        loop=0,
    )


def main() -> None:
    source_path = find_source()
    source = Image.open(source_path).convert("RGB")
    faces = make_faces(source)

    for logical_name, index in MINECRAFT_FACE_INDEX.items():
        save_face(faces[logical_name], index)

    save_previews(faces)
    save_3d_preview(faces)
    print(f"Generated ordered panorama faces from {source_path}")


if __name__ == "__main__":
    main()
