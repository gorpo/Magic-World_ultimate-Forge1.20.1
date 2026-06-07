#!/usr/bin/env python3
"""Prepare local MagicWorld resource/shader packs for Minecraft Forge 1.20.1.

The ZIP files under run/ are intentionally not tracked by Git because some of
these packs are larger than GitHub's normal 100 MB file limit. This script fixes
local pack metadata and writes a small manifest that can be committed.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import shutil
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RUN = ROOT / "run"
RESOURCEPACKS = RUN / "resourcepacks"
SHADERPACKS = RUN / "shaderpacks"
MANIFEST = ROOT / "docs" / "PACKS_MANIFEST.json"
RESOURCE_PACK_FORMAT_1_20_1 = 15
RESOURCE_PACK_ORDER = [
    "MagicWorldResource_1.20.1-256x.zip",
    "MagicWorldResource_1.20.1-models.zip",
    "MagicWorldResource_1.20.1-addon.zip",
    "MagicWorldResource_1.20.1-bonus.zip",
]


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def pack_description(path: Path) -> str:
    stem = path.stem.replace("MagicWorldResource_1.20.1-", "")
    return f"MagicWorld {stem} - Forge 1.20.1"


def read_pack_meta(path: Path) -> dict:
    with zipfile.ZipFile(path, "r") as archive:
        try:
            with archive.open("pack.mcmeta") as handle:
                return json.loads(handle.read().decode("utf-8-sig"))
        except KeyError:
            return {}


def copy_zipinfo(info: zipfile.ZipInfo) -> zipfile.ZipInfo:
    copied = zipfile.ZipInfo(info.filename, date_time=info.date_time)
    copied.compress_type = info.compress_type
    copied.comment = info.comment
    copied.extra = info.extra
    copied.internal_attr = info.internal_attr
    copied.external_attr = info.external_attr
    copied.create_system = info.create_system
    return copied


def write_streamed_entry(
    source: zipfile.ZipFile,
    target: zipfile.ZipFile,
    info: zipfile.ZipInfo,
) -> None:
    new_info = copy_zipinfo(info)
    with source.open(info, "r") as src, target.open(new_info, "w") as dst:
        shutil.copyfileobj(src, dst, length=1024 * 1024)


def fix_pack_meta(path: Path) -> bool:
    desired = {
        "pack": {
            "pack_format": RESOURCE_PACK_FORMAT_1_20_1,
            "description": pack_description(path),
        }
    }
    current = read_pack_meta(path)
    if current == desired:
        return False

    fd, tmp_name = tempfile.mkstemp(
        prefix=path.name + ".",
        suffix=".tmp",
        dir=str(path.parent),
    )
    os.close(fd)
    tmp_path = Path(tmp_name)

    try:
        with zipfile.ZipFile(path, "r") as zin, zipfile.ZipFile(
            tmp_path,
            "w",
            compression=zipfile.ZIP_DEFLATED,
            compresslevel=6,
            strict_timestamps=False,
        ) as zout:
            wrote_meta = False
            for info in zin.infolist():
                if info.filename == "pack.mcmeta":
                    meta_info = zipfile.ZipInfo("pack.mcmeta", date_time=info.date_time)
                    meta_info.compress_type = zipfile.ZIP_DEFLATED
                    meta_info.external_attr = info.external_attr
                    zout.writestr(
                        meta_info,
                        json.dumps(desired, indent=2, ensure_ascii=True).encode("utf-8"),
                    )
                    wrote_meta = True
                else:
                    write_streamed_entry(zin, zout, info)

            if not wrote_meta:
                zout.writestr(
                    "pack.mcmeta",
                    json.dumps(desired, indent=2, ensure_ascii=True).encode("utf-8"),
                )

        tmp_path.replace(path)
        return True
    except Exception:
        tmp_path.unlink(missing_ok=True)
        raise


def inspect_zip(path: Path, pack_type: str) -> dict:
    meta = {}
    roots: list[str] = []
    entries = 0
    with zipfile.ZipFile(path, "r") as archive:
        entries = len(archive.infolist())
        names = archive.namelist()
        roots = sorted({name.split("/", 1)[0] for name in names if "/" in name})
        if "pack.mcmeta" in names:
            meta = read_pack_meta(path)

    return {
        "name": path.name,
        "type": pack_type,
        "size_bytes": path.stat().st_size,
        "sha256": sha256(path),
        "entries": entries,
        "roots": roots,
        "pack_mcmeta": meta,
    }


def write_manifest() -> dict:
    resourcepacks = [
        inspect_zip(path, "resourcepack")
        for path in sorted(RESOURCEPACKS.glob("*.zip"))
    ] if RESOURCEPACKS.exists() else []
    shaderpacks = [
        inspect_zip(path, "shaderpack")
        for path in sorted(SHADERPACKS.glob("*.zip"))
    ] if SHADERPACKS.exists() else []

    manifest = {
        "minecraft": "1.20.1",
        "forge_resource_pack_format": RESOURCE_PACK_FORMAT_1_20_1,
        "shader_loader_note": "Forge vanilla does not load shaderpacks. Install Oculus/Iris-compatible loader plus Embeddium/Rubidium to render shaders.",
        "resourcepacks": resourcepacks,
        "shaderpacks": shaderpacks,
    }
    MANIFEST.parent.mkdir(parents=True, exist_ok=True)
    MANIFEST.write_text(json.dumps(manifest, indent=2, ensure_ascii=True) + "\n", encoding="utf-8")
    return manifest


def update_options_txt() -> bool:
    options = RUN / "options.txt"
    if not options.exists():
        return False

    existing = {path.name for path in RESOURCEPACKS.glob("*.zip")} if RESOURCEPACKS.exists() else set()
    enabled = ["vanilla"]
    enabled.extend(f"file/{name}" for name in RESOURCE_PACK_ORDER if name in existing)
    enabled.append("mod_resources")
    resource_line = "resourcePacks:" + json.dumps(enabled, separators=(",", ":"))

    lines = options.read_text(encoding="utf-8", errors="replace").splitlines()
    changed = False
    output: list[str] = []
    saw_resource = False
    saw_incompatible = False

    for line in lines:
        if line.startswith("resourcePacks:"):
            output.append(resource_line)
            saw_resource = True
            changed = changed or line != resource_line
        elif line.startswith("incompatibleResourcePacks:"):
            output.append("incompatibleResourcePacks:[]")
            saw_incompatible = True
            changed = changed or line != "incompatibleResourcePacks:[]"
        else:
            output.append(line)

    if not saw_resource:
        output.append(resource_line)
        changed = True
    if not saw_incompatible:
        output.append("incompatibleResourcePacks:[]")
        changed = True

    if changed:
        options.write_text("\n".join(output) + "\n", encoding="utf-8")
    return changed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--fix", action="store_true", help="rewrite resource pack ZIP metadata for Forge 1.20.1")
    parser.add_argument("--enable", action="store_true", help="enable local MagicWorld resource packs in run/options.txt")
    args = parser.parse_args()

    fixed: list[str] = []
    if args.fix and RESOURCEPACKS.exists():
        for path in sorted(RESOURCEPACKS.glob("*.zip")):
            if fix_pack_meta(path):
                fixed.append(path.name)

    options_changed = update_options_txt() if args.enable else False
    manifest = write_manifest()

    print(json.dumps({
        "fixed": fixed,
        "options_changed": options_changed,
        "manifest": str(MANIFEST.relative_to(ROOT)),
        "resourcepacks": len(manifest["resourcepacks"]),
        "shaderpacks": len(manifest["shaderpacks"]),
    }, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
