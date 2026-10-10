#!/usr/bin/env python3
"""Generate ClearLava's translucent textures from a local Minecraft client JAR."""

from __future__ import annotations

import argparse
import io
import zipfile
from pathlib import Path

from PIL import Image

TEXTURE_NAMES = ("lava_still", "lava_flow")
ALPHA = 140


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--client-jar", required=True, type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    args = parser.parse_args()

    if not args.client_jar.is_file():
        parser.error(f"client JAR does not exist: {args.client_jar}")

    texture_dir = args.output_dir / "assets" / "minecraft" / "textures" / "block"
    texture_dir.mkdir(parents=True, exist_ok=True)

    try:
        with zipfile.ZipFile(args.client_jar) as client_jar:
            for name in TEXTURE_NAMES:
                texture_path = f"assets/minecraft/textures/block/{name}.png"
                image_bytes = client_jar.read(texture_path)
                with Image.open(io.BytesIO(image_bytes)) as source:
                    image = source.convert("RGBA")
                red, green, blue, alpha = image.split()
                alpha = alpha.point(lambda _pixel: ALPHA)
                Image.merge("RGBA", (red, green, blue, alpha)).save(
                    texture_dir / f"{name}.png"
                )

                metadata_path = f"{texture_path}.mcmeta"
                try:
                    metadata = client_jar.read(metadata_path)
                except KeyError:
                    (texture_dir / f"{name}.png.mcmeta").unlink(missing_ok=True)
                else:
                    (texture_dir / f"{name}.png.mcmeta").write_bytes(metadata)
    except (KeyError, zipfile.BadZipFile) as error:
        raise SystemExit(f"Could not read Minecraft textures from {args.client_jar}: {error}")

    print(f"Generated translucent textures in {texture_dir}")


if __name__ == "__main__":
    main()
