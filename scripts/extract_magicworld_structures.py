#!/usr/bin/env python3
from __future__ import annotations

import argparse
import gzip
import io
import math
import re
import struct
import zlib
from pathlib import Path
from typing import Callable, Iterable

import nbtlib


BOXES = {
    "house": {
        "world": Path("tmp/extracted/house/Hacienda House - 01"),
        "output": Path("src/main/resources/data/magicworld/structure/imported_house.nbt"),
        "box": (-248, -130, 5, 32, 110, 240),
        "format": "modern",
        "data_version": 2586,
    },
    "castle": {
        "world": Path("tmp/extracted/castle/The Witcher 1.0"),
        "output": Path("src/main/resources/data/magicworld/structure/imported_castle.nbt"),
        "box": (-704, -440, 64, 112, -480, -260),
        "format": "legacy",
        "data_version": 3465,
    },
}

AIR = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
REGION_RE = re.compile(r"r\.(-?\d+)\.(-?\d+)\.mca$")
U64_MASK = (1 << 64) - 1


def floor_div(value: int, divisor: int) -> int:
    return math.floor(value / divisor)


def decompress_chunk(payload: bytes, compression: int) -> bytes:
    if compression == 1:
        return gzip.decompress(payload)
    if compression == 2:
        return zlib.decompress(payload)
    if compression == 3:
        return payload
    raise ValueError(f"Unsupported Anvil chunk compression type {compression}")


def region_files(world: Path, box: tuple[int, int, int, int, int, int]) -> Iterable[Path]:
    min_x, max_x, _, _, min_z, max_z = box
    min_rx = floor_div(floor_div(min_x, 16), 32)
    max_rx = floor_div(floor_div(max_x, 16), 32)
    min_rz = floor_div(floor_div(min_z, 16), 32)
    max_rz = floor_div(floor_div(max_z, 16), 32)
    region_dir = world / "region"
    for rx in range(min_rx, max_rx + 1):
        for rz in range(min_rz, max_rz + 1):
            path = region_dir / f"r.{rx}.{rz}.mca"
            if path.is_file():
                yield path


def iter_chunks(world: Path, box: tuple[int, int, int, int, int, int]):
    min_x, max_x, _, _, min_z, max_z = box
    min_cx = floor_div(min_x, 16)
    max_cx = floor_div(max_x, 16)
    min_cz = floor_div(min_z, 16)
    max_cz = floor_div(max_z, 16)

    for region_path in region_files(world, box):
        match = REGION_RE.match(region_path.name)
        if not match:
            continue
        rx = int(match.group(1))
        rz = int(match.group(2))
        with region_path.open("rb") as region:
            header = region.read(8192)
            for cx in range(max(min_cx, rx * 32), min(max_cx, rx * 32 + 31) + 1):
                for cz in range(max(min_cz, rz * 32), min(max_cz, rz * 32 + 31) + 1):
                    local_x = cx - rx * 32
                    local_z = cz - rz * 32
                    header_index = 4 * (local_x + local_z * 32)
                    offset = int.from_bytes(header[header_index:header_index + 3], "big")
                    sectors = header[header_index + 3]
                    if offset == 0 or sectors == 0:
                        continue
                    region.seek(offset * 4096)
                    length = int.from_bytes(region.read(4), "big")
                    compression = region.read(1)[0]
                    payload = region.read(length - 1)
                    chunk = nbtlib.File.parse(io.BytesIO(decompress_chunk(payload, compression)))
                    level = chunk.get("Level", chunk)
                    yield int(level["xPos"]), int(level["zPos"]), level


def section_intersects(section_y: int, box: tuple[int, int, int, int, int, int]) -> bool:
    _, _, min_y, max_y, _, _ = box
    sy = section_y * 16
    return sy <= max_y and sy + 15 >= min_y


def block_index(lx: int, ly: int, lz: int) -> int:
    return (ly << 8) | (lz << 4) | lx


def modern_palette_state(palette_entry) -> tuple[str, tuple[tuple[str, str], ...]]:
    name = str(palette_entry["Name"])
    properties = palette_entry.get("Properties")
    if not properties:
        return name, ()
    return name, tuple(sorted((str(key), str(value)) for key, value in properties.items()))


def modern_palette_index(states, palette_len: int, index: int) -> int:
    if states is None or len(states) == 0:
        return 0
    bits = max(4, (palette_len - 1).bit_length())
    values_per_long = 64 // bits
    long_index = index // values_per_long
    if long_index >= len(states):
        return 0
    bit_offset = (index % values_per_long) * bits
    word = int(states[long_index]) & U64_MASK
    return (word >> bit_offset) & ((1 << bits) - 1)


def legacy_meta(data, index: int) -> int:
    if not data:
        return 0
    packed = int(data[index // 2]) & 0xFF
    if index & 1:
        return (packed >> 4) & 0xF
    return packed & 0xF


COLORS = (
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
    "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
)

WOODS = ("oak", "spruce", "birch", "jungle", "acacia", "dark_oak")


def axis_from_legacy_log(meta: int) -> tuple[str, str]:
    axis = (meta >> 2) & 3
    if axis == 1:
        return "axis", "x"
    if axis == 2:
        return "axis", "z"
    return "axis", "y"


def horizontal_from_meta(meta: int) -> str:
    return ("south", "west", "north", "east")[meta & 3]


def facing_2_to_5(meta: int) -> str:
    return {
        2: "north",
        3: "south",
        4: "west",
        5: "east",
    }.get(meta & 7, "north")


def stair_state(name: str, meta: int) -> tuple[str, tuple[tuple[str, str], ...]]:
    facing = ("east", "west", "south", "north")[meta & 3]
    half = "top" if meta & 4 else "bottom"
    return name, (("facing", facing), ("half", half), ("shape", "straight"), ("waterlogged", "false"))


def slab_state(name: str, meta: int) -> tuple[str, tuple[tuple[str, str], ...]]:
    slab_type = "top" if meta & 8 else "bottom"
    return name, (("type", slab_type), ("waterlogged", "false"))


def legacy_state(block_id: int, meta: int) -> tuple[str, tuple[tuple[str, str], ...]]:
    if block_id == 0:
        return "minecraft:air", ()
    if block_id == 1:
        return "minecraft:stone", ()
    if block_id == 2:
        return "minecraft:grass_block", (("snowy", "false"),)
    if block_id == 3:
        return ("minecraft:coarse_dirt" if meta == 1 else "minecraft:podzol" if meta == 2 else "minecraft:dirt"), ()
    if block_id == 4:
        return "minecraft:cobblestone", ()
    if block_id == 5:
        return f"minecraft:{WOODS[meta & 3]}_planks", ()
    if block_id == 7:
        return "minecraft:bedrock", ()
    if block_id in (8, 9):
        return "minecraft:water", (("level", "0"),)
    if block_id in (10, 11):
        return "minecraft:lava", (("level", "0"),)
    if block_id == 12:
        return ("minecraft:red_sand" if meta == 1 else "minecraft:sand"), ()
    if block_id == 13:
        return "minecraft:gravel", ()
    if block_id == 14:
        return "minecraft:gold_ore", ()
    if block_id == 15:
        return "minecraft:iron_ore", ()
    if block_id == 16:
        return "minecraft:coal_ore", ()
    if block_id == 17:
        wood = ("oak", "spruce", "birch", "jungle")[meta & 3]
        return f"minecraft:{wood}_log", (axis_from_legacy_log(meta),)
    if block_id == 18:
        leaves = ("oak", "spruce", "birch", "jungle")[meta & 3]
        return f"minecraft:{leaves}_leaves", (("distance", "7"), ("persistent", "true"),)
    if block_id == 20:
        return "minecraft:glass", ()
    if block_id == 21:
        return "minecraft:lapis_ore", ()
    if block_id == 24:
        return ("minecraft:chiseled_sandstone" if meta == 1 else "minecraft:cut_sandstone" if meta == 2 else "minecraft:sandstone"), ()
    if block_id == 26:
        color = "red" if meta & 8 else "white"
        part = "head" if meta & 8 else "foot"
        return f"minecraft:{color}_bed", (("facing", horizontal_from_meta(meta)), ("occupied", "false"), ("part", part))
    if block_id == 31:
        return ("minecraft:fern" if meta == 2 else "minecraft:dead_bush" if meta == 0 else "minecraft:short_grass"), ()
    if block_id == 32:
        return "minecraft:dead_bush", ()
    if block_id == 35:
        return f"minecraft:{COLORS[meta & 15]}_wool", ()
    if block_id == 37:
        return "minecraft:dandelion", ()
    if block_id == 38:
        flower = ("poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy")[min(meta, 8)]
        return f"minecraft:{flower}", ()
    if block_id == 39:
        return "minecraft:brown_mushroom", ()
    if block_id == 40:
        return "minecraft:red_mushroom", ()
    if block_id == 41:
        return "minecraft:gold_block", ()
    if block_id == 42:
        return "minecraft:iron_block", ()
    if block_id == 43:
        return "minecraft:smooth_stone", ()
    if block_id == 44:
        variants = ("smooth_stone_slab", "sandstone_slab", "petrified_oak_slab", "cobblestone_slab", "brick_slab", "stone_brick_slab", "nether_brick_slab", "quartz_slab")
        return slab_state(f"minecraft:{variants[meta & 7]}", meta)
    if block_id == 45:
        return "minecraft:bricks", ()
    if block_id == 47:
        return "minecraft:bookshelf", ()
    if block_id == 48:
        return "minecraft:mossy_cobblestone", ()
    if block_id == 49:
        return "minecraft:obsidian", ()
    if block_id == 50:
        return "minecraft:torch", ()
    if block_id == 51:
        return "minecraft:fire", (("age", "0"), ("east", "false"), ("north", "false"), ("south", "false"), ("up", "false"), ("west", "false"))
    if block_id == 53:
        return stair_state("minecraft:oak_stairs", meta)
    if block_id == 54:
        return "minecraft:chest", (("facing", facing_2_to_5(meta)), ("type", "single"), ("waterlogged", "false"))
    if block_id == 56:
        return "minecraft:diamond_ore", ()
    if block_id == 58:
        return "minecraft:crafting_table", ()
    if block_id in (61, 62):
        return "minecraft:furnace", (("facing", facing_2_to_5(meta)), ("lit", "true" if block_id == 62 else "false"))
    if block_id == 63:
        return "minecraft:oak_sign", (("rotation", str(meta & 15)), ("waterlogged", "false"))
    if block_id == 64:
        return "minecraft:oak_door", (("facing", horizontal_from_meta(meta)), ("half", "upper" if meta & 8 else "lower"), ("hinge", "left"), ("open", "false"), ("powered", "false"))
    if block_id == 65:
        return "minecraft:ladder", (("facing", facing_2_to_5(meta)), ("waterlogged", "false"))
    if block_id == 67:
        return stair_state("minecraft:cobblestone_stairs", meta)
    if block_id == 68:
        return "minecraft:oak_wall_sign", (("facing", facing_2_to_5(meta)), ("waterlogged", "false"))
    if block_id == 71:
        return "minecraft:iron_door", (("facing", horizontal_from_meta(meta)), ("half", "upper" if meta & 8 else "lower"), ("hinge", "left"), ("open", "false"), ("powered", "false"))
    if block_id == 73 or block_id == 74:
        return "minecraft:redstone_ore", (("lit", "true" if block_id == 74 else "false"),)
    if block_id == 81:
        return "minecraft:cactus", (("age", "0"),)
    if block_id == 82:
        return "minecraft:clay", ()
    if block_id == 85:
        return "minecraft:oak_fence", (("east", "false"), ("north", "false"), ("south", "false"), ("waterlogged", "false"), ("west", "false"))
    if block_id == 87:
        return "minecraft:netherrack", ()
    if block_id == 89:
        return "minecraft:glowstone", ()
    if block_id == 95:
        return f"minecraft:{COLORS[meta & 15]}_stained_glass", ()
    if block_id == 96:
        return "minecraft:oak_trapdoor", (("facing", horizontal_from_meta(meta)), ("half", "bottom"), ("open", "false"), ("powered", "false"), ("waterlogged", "false"))
    if block_id == 98:
        return ("minecraft:mossy_stone_bricks" if meta == 1 else "minecraft:cracked_stone_bricks" if meta == 2 else "minecraft:chiseled_stone_bricks" if meta == 3 else "minecraft:stone_bricks"), ()
    if block_id == 101:
        return "minecraft:iron_bars", (("east", "false"), ("north", "false"), ("south", "false"), ("waterlogged", "false"), ("west", "false"))
    if block_id == 106:
        return "minecraft:vine", (("east", "false"), ("north", "false"), ("south", "false"), ("up", "false"), ("west", "false"))
    if block_id == 108:
        return stair_state("minecraft:brick_stairs", meta)
    if block_id == 109:
        return stair_state("minecraft:stone_brick_stairs", meta)
    if block_id == 111:
        return "minecraft:lily_pad", ()
    if block_id == 125:
        return f"minecraft:{WOODS[meta & 7]}_planks", ()
    if block_id == 126:
        return slab_state(f"minecraft:{WOODS[meta & 7]}_slab", meta)
    if block_id == 128:
        return stair_state("minecraft:sandstone_stairs", meta)
    if block_id == 129:
        return "minecraft:emerald_ore", ()
    if block_id == 134:
        return stair_state("minecraft:spruce_stairs", meta)
    if block_id == 136:
        return stair_state("minecraft:jungle_stairs", meta)
    if block_id == 139:
        return ("minecraft:mossy_cobblestone_wall" if meta else "minecraft:cobblestone_wall"), (("east", "none"), ("north", "none"), ("south", "none"), ("up", "true"), ("waterlogged", "false"), ("west", "none"))
    if block_id == 144:
        return "minecraft:skeleton_skull", (("rotation", str(meta & 15)),)
    if block_id == 145:
        return "minecraft:anvil", (("facing", horizontal_from_meta(meta)),)
    if block_id == 155:
        return ("minecraft:chiseled_quartz_block" if meta == 1 else "minecraft:quartz_pillar" if meta == 2 else "minecraft:quartz_block"), ()
    if block_id == 156:
        return stair_state("minecraft:quartz_stairs", meta)
    if block_id == 159:
        return f"minecraft:{COLORS[meta & 15]}_terracotta", ()
    if block_id == 160:
        return f"minecraft:{COLORS[meta & 15]}_stained_glass_pane", (("east", "false"), ("north", "false"), ("south", "false"), ("waterlogged", "false"), ("west", "false"))
    if block_id == 161:
        leaves = "acacia" if meta & 1 == 0 else "dark_oak"
        return f"minecraft:{leaves}_leaves", (("distance", "7"), ("persistent", "true"),)
    if block_id == 162:
        wood = "acacia" if meta & 1 == 0 else "dark_oak"
        return f"minecraft:{wood}_log", (axis_from_legacy_log(meta),)
    if block_id == 164:
        return stair_state("minecraft:dark_oak_stairs", meta)
    if block_id == 169:
        return "minecraft:sea_lantern", ()
    if block_id == 170:
        return "minecraft:hay_block", (("axis", "y"),)
    if block_id == 171:
        return f"minecraft:{COLORS[meta & 15]}_carpet", ()
    if block_id == 172:
        return "minecraft:terracotta", ()
    if block_id == 175:
        return ("minecraft:sunflower" if meta == 0 else "minecraft:lilac" if meta == 1 else "minecraft:tall_grass" if meta == 2 else "minecraft:large_fern" if meta == 3 else "minecraft:rose_bush" if meta == 4 else "minecraft:peony"), (("half", "lower"),)
    if block_id == 177:
        return "minecraft:acacia_slab", (("type", "bottom"), ("waterlogged", "false"))
    if block_id == 180:
        return stair_state("minecraft:red_sandstone_stairs", meta)
    if block_id == 182:
        return slab_state("minecraft:red_sandstone_slab", meta)
    if block_id == 186:
        return "minecraft:dark_oak_fence_gate", (("facing", horizontal_from_meta(meta)), ("in_wall", "false"), ("open", "false"), ("powered", "false"))
    if block_id == 188:
        return "minecraft:spruce_fence", (("east", "false"), ("north", "false"), ("south", "false"), ("waterlogged", "false"), ("west", "false"))
    if block_id == 190:
        return "minecraft:jungle_fence", (("east", "false"), ("north", "false"), ("south", "false"), ("waterlogged", "false"), ("west", "false"))
    if block_id == 191:
        return "minecraft:dark_oak_fence", (("east", "false"), ("north", "false"), ("south", "false"), ("waterlogged", "false"), ("west", "false"))
    if block_id == 193:
        return "minecraft:spruce_door", (("facing", horizontal_from_meta(meta)), ("half", "upper" if meta & 8 else "lower"), ("hinge", "left"), ("open", "false"), ("powered", "false"))
    if block_id == 197:
        return "minecraft:dark_oak_door", (("facing", horizontal_from_meta(meta)), ("half", "upper" if meta & 8 else "lower"), ("hinge", "left"), ("open", "false"), ("powered", "false"))
    return "minecraft:stone", ()


def iter_modern_blocks(world: Path, box: tuple[int, int, int, int, int, int]):
    min_x, max_x, min_y, max_y, min_z, max_z = box
    for chunk_x, chunk_z, chunk in iter_chunks(world, box):
        for section in chunk.get("Sections", []):
            if "Palette" not in section or not section_intersects(int(section["Y"]), box):
                continue
            section_y = int(section["Y"]) * 16
            palette = [modern_palette_state(entry) for entry in section["Palette"]]
            states = section.get("BlockStates")
            for ly in range(16):
                y = section_y + ly
                if y < min_y or y > max_y:
                    continue
                for lz in range(16):
                    z = chunk_z * 16 + lz
                    if z < min_z or z > max_z:
                        continue
                    for lx in range(16):
                        x = chunk_x * 16 + lx
                        if x < min_x or x > max_x:
                            continue
                        index = block_index(lx, ly, lz)
                        palette_index = modern_palette_index(states, len(palette), index)
                        if palette_index >= len(palette):
                            continue
                        state = palette[palette_index]
                        if state[0] in AIR:
                            continue
                        yield x - min_x, y - min_y, z - min_z, state


def iter_legacy_blocks(world: Path, box: tuple[int, int, int, int, int, int]):
    min_x, max_x, min_y, max_y, min_z, max_z = box
    for chunk_x, chunk_z, chunk in iter_chunks(world, box):
        for section in chunk.get("Sections", []):
            if "Blocks" not in section or not section_intersects(int(section["Y"]), box):
                continue
            section_y = int(section["Y"]) * 16
            blocks = section["Blocks"]
            data = section.get("Data")
            for ly in range(16):
                y = section_y + ly
                if y < min_y or y > max_y:
                    continue
                for lz in range(16):
                    z = chunk_z * 16 + lz
                    if z < min_z or z > max_z:
                        continue
                    for lx in range(16):
                        x = chunk_x * 16 + lx
                        if x < min_x or x > max_x:
                            continue
                        index = block_index(lx, ly, lz)
                        block_id = int(blocks[index]) & 0xFF
                        if block_id == 0:
                            continue
                        state = legacy_state(block_id, legacy_meta(data, index))
                        if state[0] in AIR:
                            continue
                        yield x - min_x, y - min_y, z - min_z, state


def write_byte(out, value: int) -> None:
    out.write(struct.pack(">B", value))


def write_short(out, value: int) -> None:
    out.write(struct.pack(">H", value))


def write_int(out, value: int) -> None:
    out.write(struct.pack(">i", value))


def write_string_payload(out, value: str) -> None:
    encoded = value.encode("utf-8")
    write_short(out, len(encoded))
    out.write(encoded)


def write_tag_header(out, tag_type: int, name: str) -> None:
    write_byte(out, tag_type)
    write_string_payload(out, name)


def write_state_compound(out, state: tuple[str, tuple[tuple[str, str], ...]]) -> None:
    name, properties = state
    write_tag_header(out, 8, "Name")
    write_string_payload(out, name)
    if properties:
        write_tag_header(out, 10, "Properties")
        for key, value in properties:
            write_tag_header(out, 8, key)
            write_string_payload(out, value)
        write_byte(out, 0)
    write_byte(out, 0)


def scan_palette(iterator_factory: Callable[[], Iterable[tuple[int, int, int, tuple[str, tuple[tuple[str, str], ...]]]]]):
    palette: dict[tuple[str, tuple[tuple[str, str], ...]], int] = {}
    count = 0
    for _, _, _, state in iterator_factory():
        if state not in palette:
            palette[state] = len(palette)
        count += 1
    return palette, count


def write_structure(
    output: Path,
    size: tuple[int, int, int],
    data_version: int,
    palette: dict[tuple[str, tuple[tuple[str, str], ...]], int],
    count: int,
    iterator_factory: Callable[[], Iterable[tuple[int, int, int, tuple[str, tuple[tuple[str, str], ...]]]]],
) -> None:
    output.parent.mkdir(parents=True, exist_ok=True)
    ordered_palette = sorted(palette.items(), key=lambda item: item[1])
    with gzip.open(output, "wb", compresslevel=6) as gz:
        write_byte(gz, 10)
        write_string_payload(gz, "")

        write_tag_header(gz, 9, "size")
        write_byte(gz, 3)
        write_int(gz, 3)
        write_int(gz, size[0])
        write_int(gz, size[1])
        write_int(gz, size[2])

        write_tag_header(gz, 9, "entities")
        write_byte(gz, 10)
        write_int(gz, 0)

        write_tag_header(gz, 9, "blocks")
        write_byte(gz, 10)
        write_int(gz, count)
        for x, y, z, state in iterator_factory():
            write_tag_header(gz, 9, "pos")
            write_byte(gz, 3)
            write_int(gz, 3)
            write_int(gz, x)
            write_int(gz, y)
            write_int(gz, z)
            write_tag_header(gz, 3, "state")
            write_int(gz, palette[state])
            write_byte(gz, 0)

        write_tag_header(gz, 9, "palette")
        write_byte(gz, 10)
        write_int(gz, len(ordered_palette))
        for state, _ in ordered_palette:
            write_state_compound(gz, state)

        write_tag_header(gz, 3, "DataVersion")
        write_int(gz, data_version)
        write_byte(gz, 0)


def export_structure(name: str) -> None:
    config = BOXES[name]
    world = config["world"]
    output = config["output"]
    box = config["box"]
    if not world.is_dir():
        raise FileNotFoundError(f"World not found: {world}")
    iterator = iter_modern_blocks if config["format"] == "modern" else iter_legacy_blocks
    iterator_factory = lambda: iterator(world, box)
    size = (box[1] - box[0] + 1, box[3] - box[2] + 1, box[5] - box[4] + 1)
    palette, count = scan_palette(iterator_factory)
    write_structure(output, size, config["data_version"], palette, count, iterator_factory)
    print(f"{name}: wrote {output} size={size} blocks={count} palette={len(palette)}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("targets", nargs="*", choices=sorted(BOXES), default=sorted(BOXES))
    args = parser.parse_args()
    for target in args.targets:
        export_structure(target)


if __name__ == "__main__":
    main()
