from __future__ import annotations

import argparse
import csv
import math
import statistics
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

import numpy as np
from PIL import Image, ImageDraw, ImageFont


IMAGE_EXTS = {".jpg", ".jpeg", ".png", ".bmp"}


@dataclass
class BBox:
    xmin: int
    ymin: int
    xmax: int
    ymax: int

    @property
    def width(self) -> int:
        return max(0, self.xmax - self.xmin)

    @property
    def height(self) -> int:
        return max(0, self.ymax - self.ymin)

    @property
    def area(self) -> int:
        return self.width * self.height


def parse_boxes(xml_path: Path) -> list[BBox]:
    if not xml_path.exists():
        return []
    root = ET.parse(xml_path).getroot()
    boxes: list[BBox] = []
    for obj in root.findall("object"):
        bnd = obj.find("bndbox")
        if bnd is None:
            continue
        boxes.append(
            BBox(
                int(float(bnd.findtext("xmin", "0"))),
                int(float(bnd.findtext("ymin", "0"))),
                int(float(bnd.findtext("xmax", "0"))),
                int(float(bnd.findtext("ymax", "0"))),
            )
        )
    return boxes


def union_box(boxes: Iterable[BBox]) -> BBox | None:
    boxes = list(boxes)
    if not boxes:
        return None
    return BBox(
        min(b.xmin for b in boxes),
        min(b.ymin for b in boxes),
        max(b.xmax for b in boxes),
        max(b.ymax for b in boxes),
    )


def clamp_box(box: BBox, width: int, height: int) -> BBox:
    return BBox(
        max(0, min(width, box.xmin)),
        max(0, min(height, box.ymin)),
        max(0, min(width, box.xmax)),
        max(0, min(height, box.ymax)),
    )


def expand_box(box: BBox, width: int, height: int, margin_ratio: float, min_side: int) -> BBox:
    cx = (box.xmin + box.xmax) / 2.0
    cy = (box.ymin + box.ymax) / 2.0
    side = max(box.width, box.height)
    crop_w = max(min_side, int(round(box.width + margin_ratio * side * 2)))
    crop_h = max(min_side, int(round(box.height + margin_ratio * side * 2)))
    xmin = int(round(cx - crop_w / 2))
    xmax = xmin + crop_w
    ymin = int(round(cy - crop_h / 2))
    ymax = ymin + crop_h

    if xmin < 0:
        xmax -= xmin
        xmin = 0
    if ymin < 0:
        ymax -= ymin
        ymin = 0
    if xmax > width:
        xmin -= xmax - width
        xmax = width
    if ymax > height:
        ymin -= ymax - height
        ymax = height
    return clamp_box(BBox(xmin, ymin, xmax, ymax), width, height)


def box_ratio(box: BBox, width: int, height: int) -> float:
    denom = max(1, width * height)
    return box.area / denom


def crop_mask(mask: np.ndarray, box: BBox) -> np.ndarray:
    return mask[box.ymin : box.ymax, box.xmin : box.xmax]


def density_bounds(mask: np.ndarray, min_density: float = 0.055, pad: int = 8) -> BBox | None:
    height, width = mask.shape[:2]
    col_density = mask.mean(axis=0)
    row_density = mask.mean(axis=1)
    cols = np.where(col_density > min_density)[0]
    rows = np.where(row_density > min_density)[0]
    if len(cols) == 0 or len(rows) == 0:
        return None
    return clamp_box(
        BBox(int(cols.min()) - pad, int(rows.min()) - pad, int(cols.max()) + pad + 1, int(rows.max()) + pad + 1),
        width,
        height,
    )


def component_count_small(mask: np.ndarray, max_components: int = 4000) -> tuple[int, int]:
    """Return number and total area of small/medium connected components.

    This avoids external OpenCV/scipy dependencies. It is intentionally simple:
    enough to estimate bright text/caliper clutter, not for segmentation.
    """
    height, width = mask.shape
    seen = np.zeros(mask.shape, dtype=bool)
    coords = np.argwhere(mask)
    count = 0
    area_sum = 0
    for y0, x0 in coords:
        if seen[y0, x0]:
            continue
        stack = [(int(y0), int(x0))]
        seen[y0, x0] = True
        area = 0
        while stack:
            y, x = stack.pop()
            area += 1
            for yy in (y - 1, y, y + 1):
                for xx in (x - 1, x, x + 1):
                    if yy == y and xx == x:
                        continue
                    if yy < 0 or yy >= height or xx < 0 or xx >= width:
                        continue
                    if mask[yy, xx] and not seen[yy, xx]:
                        seen[yy, xx] = True
                        stack.append((yy, xx))
        if 4 <= area <= 900:
            count += 1
            area_sum += area
        if count > max_components:
            break
    return count, area_sum


def summarize_image(image_path: Path, annotation_dir: Path) -> dict[str, object]:
    with Image.open(image_path) as img:
        rgb_img = img.convert("RGB")
        rgb = np.asarray(rgb_img)

    height, width = rgb.shape[:2]
    gray = (0.299 * rgb[:, :, 0] + 0.587 * rgb[:, :, 1] + 0.114 * rgb[:, :, 2]).astype(np.uint8)
    maxc = rgb.max(axis=2).astype(np.float32)
    minc = rgb.min(axis=2).astype(np.float32)
    saturation = np.divide(maxc - minc, np.maximum(maxc, 1), out=np.zeros_like(maxc), where=maxc > 0)
    color_mask = (maxc > 45) & ((maxc - minc) > 24) & (saturation > 0.18)
    red_mask = color_mask & (rgb[:, :, 0] > rgb[:, :, 1] + 20) & (rgb[:, :, 0] > rgb[:, :, 2] + 20)
    green_mask = color_mask & (rgb[:, :, 1] > rgb[:, :, 0] + 15) & (rgb[:, :, 1] > rgb[:, :, 2] + 15)
    blue_mask = color_mask & (rgb[:, :, 2] > rgb[:, :, 0] + 15) & (rgb[:, :, 2] > rgb[:, :, 1] + 15)
    yellow_mask = color_mask & (rgb[:, :, 0] > 120) & (rgb[:, :, 1] > 100) & (rgb[:, :, 2] < 110)

    bright_mask = gray > 235
    very_bright_mask = gray > 248
    foreground_mask = gray > 20
    content = density_bounds(foreground_mask)

    boxes = parse_boxes(annotation_dir / f"{image_path.stem}.xml")
    ubox = union_box(boxes)
    if ubox is not None:
        ubox = clamp_box(ubox, width, height)
        analysis_crop = expand_box(ubox, width, height, margin_ratio=1.25, min_side=430)
        tight_context = expand_box(ubox, width, height, margin_ratio=0.55, min_side=256)
    else:
        analysis_crop = content or BBox(0, 0, width, height)
        tight_context = analysis_crop

    roi_color = crop_mask(color_mask, analysis_crop)
    roi_red = crop_mask(red_mask, analysis_crop)
    roi_green = crop_mask(green_mask, analysis_crop)
    roi_blue = crop_mask(blue_mask, analysis_crop)
    roi_yellow = crop_mask(yellow_mask, analysis_crop)
    roi_bright = crop_mask(bright_mask, analysis_crop)
    roi_very_bright = crop_mask(very_bright_mask, analysis_crop)

    near_color_ratio = 0.0
    near_bright_ratio = 0.0
    near_bright_components = 0
    if ubox is not None:
        near = expand_box(ubox, width, height, margin_ratio=0.25, min_side=max(96, max(ubox.width, ubox.height)))
        near_color = crop_mask(color_mask, near)
        near_bright = crop_mask(very_bright_mask, near)
        near_color_ratio = float(near_color.mean())
        near_bright_ratio = float(near_bright.mean())
        if near_bright.any() or near_color.any():
            component_mask = near_bright | near_color
            near_bright_components, _ = component_count_small(component_mask)

    color_ratio = float(color_mask.mean())
    crop_color_ratio = float(roi_color.mean())
    crop_bright_ratio = float(roi_bright.mean())
    crop_very_bright_ratio = float(roi_very_bright.mean())
    side_w = max(1, int(width * 0.16))
    bottom_h = max(1, int(height * 0.13))
    top_h = max(1, int(height * 0.10))
    side_bright_ratio = float(np.concatenate([bright_mask[:, :side_w], bright_mask[:, width - side_w :]], axis=1).mean())
    bottom_bright_ratio = float(bright_mask[height - bottom_h :, :].mean())
    top_bright_ratio = float(bright_mask[:top_h, :].mean())

    touches_edge = False
    bbox_area_ratio = 0.0
    content_contains_bbox = True
    if ubox is not None:
        bbox_area_ratio = box_ratio(ubox, width, height)
        touches_edge = (
            ubox.xmin < width * 0.05
            or ubox.xmax > width * 0.95
            or ubox.ymin < height * 0.05
            or ubox.ymax > height * 0.95
        )
        if content is not None:
            content_contains_bbox = (
                ubox.xmin >= content.xmin
                and ubox.ymin >= content.ymin
                and ubox.xmax <= content.xmax
                and ubox.ymax <= content.ymax
            )

    crop_needs_side_cleanup = side_bright_ratio > 0.015 or top_bright_ratio > 0.02 or bottom_bright_ratio > 0.015
    crop_contains_color = crop_color_ratio > 0.00012
    crop_contains_heavy_color = crop_color_ratio > 0.001
    near_overlay = near_color_ratio > 0.0002 or near_bright_components > 20 or near_bright_ratio > 0.018
    small_bbox = bbox_area_ratio > 0 and bbox_area_ratio < 0.008

    score = 0
    reasons: list[str] = []
    if ubox is None:
        score += 4
        reasons.append("no_annotation_object")
    if len(boxes) > 1:
        score += 1
        reasons.append("multiple_boxes")
    if touches_edge:
        score += 2
        reasons.append("bbox_near_image_edge")
    if not content_contains_bbox:
        score += 2
        reasons.append("bbox_outside_dense_ultrasound_field")
    if crop_needs_side_cleanup:
        score += 1
        reasons.append("side_or_header_footer_overlay")
    if crop_contains_color:
        score += 2
        reasons.append("colored_overlay_in_candidate_crop")
    if crop_contains_heavy_color:
        score += 1
        reasons.append("heavy_colored_overlay")
    if near_overlay:
        score += 2
        reasons.append("overlay_close_to_bbox")
    if crop_very_bright_ratio > 0.035:
        score += 1
        reasons.append("many_very_bright_pixels_in_crop")
    if small_bbox:
        score += 1
        reasons.append("small_bbox_needs_high_resolution_crop")

    if ubox is None:
        group = "D_review_no_object"
    elif score <= 2:
        group = "A_easy_clean_crop"
    elif score <= 5:
        group = "B_medium_crop_and_mask"
    else:
        group = "C_hard_manual_or_inpaint"

    if ubox is None:
        tumor_annotation_status = "khong_co_bbox_u_trong_xml"
        tumor_type_annotation = "khong_co_object"
        benign_tumor_image = 0
        malignant_tumor_image = 0
        no_annotated_tumor_image = 1
    else:
        tumor_annotation_status = "co_bbox_u_xo_tu_cung"
        tumor_type_annotation = "lanh_tinh_myoma"
        benign_tumor_image = 1
        malignant_tumor_image = 0
        no_annotated_tumor_image = 0

    return {
        "filename": image_path.name,
        "width": width,
        "height": height,
        "object_count": len(boxes),
        "tumor_annotation_status": tumor_annotation_status,
        "tumor_type_annotation": tumor_type_annotation,
        "benign_tumor_image": benign_tumor_image,
        "malignant_tumor_image": malignant_tumor_image,
        "no_annotated_tumor_image": no_annotated_tumor_image,
        "bbox": "" if ubox is None else f"{ubox.xmin},{ubox.ymin},{ubox.xmax},{ubox.ymax}",
        "bbox_area_ratio": round(bbox_area_ratio, 6),
        "candidate_crop": f"{analysis_crop.xmin},{analysis_crop.ymin},{analysis_crop.xmax},{analysis_crop.ymax}",
        "tight_context_crop": f"{tight_context.xmin},{tight_context.ymin},{tight_context.xmax},{tight_context.ymax}",
        "content_box": "" if content is None else f"{content.xmin},{content.ymin},{content.xmax},{content.ymax}",
        "class": group,
        "score": score,
        "reasons": "|".join(reasons) if reasons else "clean_enough",
        "global_color_ratio": round(color_ratio, 7),
        "crop_color_ratio": round(crop_color_ratio, 7),
        "crop_red_px": int(roi_red.sum()),
        "crop_green_px": int(roi_green.sum()),
        "crop_blue_px": int(roi_blue.sum()),
        "crop_yellow_px": int(roi_yellow.sum()),
        "near_bbox_color_ratio": round(near_color_ratio, 7),
        "near_bbox_very_bright_ratio": round(near_bright_ratio, 7),
        "near_bbox_overlay_components": near_bright_components,
        "crop_bright_ratio": round(crop_bright_ratio, 6),
        "crop_very_bright_ratio": round(crop_very_bright_ratio, 6),
        "side_bright_ratio": round(side_bright_ratio, 6),
        "top_bright_ratio": round(top_bright_ratio, 6),
        "bottom_bright_ratio": round(bottom_bright_ratio, 6),
    }


def parse_box_string(value: str) -> BBox | None:
    if not value:
        return None
    parts = [int(float(x)) for x in value.split(",")]
    return BBox(*parts)


def draw_preview(image_path: Path, row: dict[str, object], thumb_size: tuple[int, int]) -> Image.Image:
    with Image.open(image_path) as img:
        img = img.convert("RGB")
    original_w, original_h = img.size
    scale = min(thumb_size[0] / original_w, thumb_size[1] / original_h)
    new_w = max(1, int(original_w * scale))
    new_h = max(1, int(original_h * scale))
    thumb = img.resize((new_w, new_h), Image.Resampling.BILINEAR)
    canvas = Image.new("RGB", thumb_size, (18, 18, 18))
    xoff = (thumb_size[0] - new_w) // 2
    yoff = (thumb_size[1] - new_h) // 2
    canvas.paste(thumb, (xoff, yoff))
    draw = ImageDraw.Draw(canvas)

    def scale_box(box: BBox) -> tuple[int, int, int, int]:
        return (
            int(xoff + box.xmin * scale),
            int(yoff + box.ymin * scale),
            int(xoff + box.xmax * scale),
            int(yoff + box.ymax * scale),
        )

    crop = parse_box_string(str(row.get("candidate_crop", "")))
    bbox = parse_box_string(str(row.get("bbox", "")))
    content = parse_box_string(str(row.get("content_box", "")))
    if content is not None:
        draw.rectangle(scale_box(content), outline=(80, 120, 255), width=1)
    if crop is not None:
        draw.rectangle(scale_box(crop), outline=(255, 190, 50), width=2)
    if bbox is not None:
        draw.rectangle(scale_box(bbox), outline=(30, 255, 110), width=3)
    label = f"{row['class'].split('_')[0]} s{row['score']} {Path(str(row['filename'])).stem[-8:]}"
    draw.rectangle((0, 0, thumb_size[0], 18), fill=(0, 0, 0))
    draw.text((4, 3), label, fill=(255, 255, 255))
    return canvas


def create_contact_sheets(
    rows: list[dict[str, object]],
    image_dir: Path,
    output_dir: Path,
    per_group_limit: int | None,
) -> list[Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    sheets: list[Path] = []
    thumb_size = (190, 145)
    cols = 5
    rows_per_sheet = 5
    cells_per_sheet = cols * rows_per_sheet

    by_group: dict[str, list[dict[str, object]]] = {}
    for row in rows:
        by_group.setdefault(str(row["class"]), []).append(row)

    for group, group_rows in sorted(by_group.items()):
        sorted_rows = sorted(group_rows, key=lambda r: (-int(r["score"]), str(r["filename"])))
        if per_group_limit is not None:
            sorted_rows = sorted_rows[:per_group_limit]
        for sheet_idx in range(math.ceil(len(sorted_rows) / cells_per_sheet)):
            chunk = sorted_rows[sheet_idx * cells_per_sheet : (sheet_idx + 1) * cells_per_sheet]
            sheet = Image.new("RGB", (thumb_size[0] * cols, thumb_size[1] * rows_per_sheet), (28, 28, 28))
            draw = ImageDraw.Draw(sheet)
            for idx, row in enumerate(chunk):
                preview = draw_preview(image_dir / str(row["filename"]), row, thumb_size)
                x = (idx % cols) * thumb_size[0]
                y = (idx // cols) * thumb_size[1]
                sheet.paste(preview, (x, y))
            if not chunk:
                draw.text((16, 16), "No images", fill=(255, 255, 255))
            path = output_dir / f"{group}_sheet_{sheet_idx + 1:02d}.jpg"
            sheet.save(path, quality=92)
            sheets.append(path)
    return sheets


def write_report(rows: list[dict[str, object]], output_path: Path, csv_path: Path, sheets: list[Path]) -> None:
    total = len(rows)
    groups: dict[str, list[dict[str, object]]] = {}
    for row in rows:
        groups.setdefault(str(row["class"]), []).append(row)

    bbox_ratios = [float(r["bbox_area_ratio"]) for r in rows if float(r["bbox_area_ratio"]) > 0]
    color_cases = sum(1 for r in rows if float(r["crop_color_ratio"]) > 0.00012)
    near_cases = sum(1 for r in rows if "overlay_close_to_bbox" in str(r["reasons"]))
    no_objects = len(groups.get("D_review_no_object", []))
    multiple = sum(1 for r in rows if int(r["object_count"]) > 1)
    small = sum(1 for r in rows if "small_bbox_needs_high_resolution_crop" in str(r["reasons"]))
    object_boxes = sum(int(r["object_count"]) for r in rows)
    benign_images = sum(int(r.get("benign_tumor_image", 0)) for r in rows)
    malignant_images = sum(int(r.get("malignant_tumor_image", 0)) for r in rows)
    no_annotated_tumor_images = sum(int(r.get("no_annotated_tumor_image", 0)) for r in rows)

    lines = [
        "# Phân loại độ phức tạp tiền xử lý JPEGImages",
        "",
        f"- Tổng ảnh đọc được: **{total}**",
        f"- File CSV chi tiết: `{csv_path}`",
        f"- Contact sheets: `{sheets[0].parent if sheets else ''}`",
        f"- Ảnh có object annotation: **{total - no_objects}**; ảnh không có object: **{no_objects}**",
        f"- Tổng số bbox object: **{object_boxes}**",
        f"- Ảnh có nhiều bbox: **{multiple}**",
        f"- Ảnh có màu/annotation màu trong crop đề xuất: **{color_cases}**",
        f"- Ảnh có overlay sát bbox: **{near_cases}**",
        f"- Bbox nhỏ cần crop giữ độ phân giải cao: **{small}**",
        "",
        "## Thống kê nhãn u",
        "",
        f"- **Không có bbox u trong XML**: {no_annotated_tumor_images} ảnh.",
        f"- **U lành tính được annotate**: {benign_images} ảnh, {object_boxes} bbox.",
        f"- **U ác tính được annotate**: {malignant_images} ảnh.",
        "",
        "> Lưu ý: bộ XML chỉ có nhãn `肌瘤` / `Myoma` (u xơ/u cơ tử cung, thường là lành tính). "
        "Vì vậy 9 ảnh không có object chỉ nên hiểu là không có bbox u được annotate trong XML. "
        "Nếu bộ dữ liệu được gán nhãn đầy đủ, có thể dùng như ảnh negative cho bài toán detection; "
        "không nên tự diễn giải thành kết luận lâm sàng rằng bệnh nhân chắc chắn không bị u.",
        "",
        "## Phân bố nhóm xử lý",
        "",
    ]
    for group in ["A_easy_clean_crop", "B_medium_crop_and_mask", "C_hard_manual_or_inpaint", "D_review_no_object"]:
        count = len(groups.get(group, []))
        pct = 100 * count / max(1, total)
        lines.append(f"- **{group}**: {count} ảnh ({pct:.1f}%)")

    if bbox_ratios:
        lines += [
            "",
            "## Kích thước bbox",
            "",
            f"- Median bbox/image area: {statistics.median(bbox_ratios):.4f}",
            f"- Mean bbox/image area: {statistics.mean(bbox_ratios):.4f}",
            f"- Min/max bbox/image area: {min(bbox_ratios):.4f} / {max(bbox_ratios):.4f}",
        ]

    lines += [
        "",
        "## Định nghĩa nhóm xử lý",
        "",
        "- **A_easy_clean_crop**: dùng crop theo annotation + margin, cắt biên đen/UI; không cần inpaint.",
        "- **B_medium_crop_and_mask**: crop theo annotation, sau đó mask/inpaint chữ, thước, marker nằm ngoài bbox hoặc gần crop.",
        "- **C_hard_manual_or_inpaint**: overlay màu/chữ/thước nằm gần hoặc cắt qua bbox; cần review từng ảnh, ưu tiên inpaint theo mask hoặc loại khỏi training nếu làm sai rìa u.",
        "- **D_review_no_object**: XML không có object; dùng làm negative sample nếu bài toán detection cần, không crop quanh u.",
        "",
        "## Chiến lược đề xuất",
        "",
        "1. Luôn giữ bản gốc bất biến; tạo thư mục processed riêng và lưu mapping tọa độ.",
        "2. Dùng XML Pascal VOC làm neo chính: crop quanh bbox với margin 1.25 lần cạnh lớn, `min_side=430 px`; cập nhật lại bbox sau crop.",
        "3. Với nhóm A: chỉ crop + resize letterbox, không cần xóa text nếu text nằm ngoài crop.",
        "4. Với nhóm B: tạo mask cho text/scale/caliper/color overlay trong crop; inpaint hoặc blackout có kiểm soát, nhưng không để mask chạm vào bbox.",
        "5. Với nhóm C: ưu tiên review bằng contact sheet; nếu overlay nằm trong bbox thì không nên xóa tự động bằng denoise mạnh vì có thể phá cấu trúc khối u.",
        "6. Với nhóm D: tách thành negative set hoặc loại khỏi train positive; không dùng để crop positive.",
        "7. Sau mỗi bước, validate: bbox sau crop còn nằm trong ảnh, bbox >= 24 px sau resize, và không có màu/chữ cắt vào vùng bbox.",
        "",
        "## Màu sắc contact sheet",
        "",
        "- Viền xanh lá: bbox u từ XML.",
        "- Viền vàng: crop đề xuất theo bbox.",
        "- Viền xanh dương: vùng ultrasound dense field ước tính.",
        "",
        "## Top ảnh cần review đầu tiên",
        "",
    ]

    hard_rows = sorted(rows, key=lambda r: (-int(r["score"]), str(r["filename"])))[:30]
    for row in hard_rows:
        lines.append(f"- `{row['filename']}` -> {row['class']} score={row['score']} reason={row['reasons']}")

    output_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path("."))
    parser.add_argument("--image-dir", type=Path, default=Path("JPEGImages"))
    parser.add_argument("--annotation-dir", type=Path, default=Path("Annotations"))
    parser.add_argument("--output-dir", type=Path, default=Path("image_processing_audit"))
    parser.add_argument("--contact-limit", type=int, default=75)
    args = parser.parse_args()

    root = args.root.resolve()
    image_dir = (root / args.image_dir).resolve()
    annotation_dir = (root / args.annotation_dir).resolve()
    output_dir = (root / args.output_dir).resolve()
    output_dir.mkdir(parents=True, exist_ok=True)

    image_paths = sorted(p for p in image_dir.iterdir() if p.is_file() and p.suffix.lower() in IMAGE_EXTS)
    rows = [summarize_image(path, annotation_dir) for path in image_paths]

    csv_path = output_dir / "image_complexity_classification.csv"
    fieldnames = list(rows[0].keys()) if rows else []
    with csv_path.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    sheets = create_contact_sheets(rows, image_dir, output_dir / "contact_sheets", args.contact_limit)
    report_path = output_dir / "processing_strategy_report.md"
    write_report(rows, report_path, csv_path, sheets)
    print(f"images={len(rows)}")
    print(f"csv={csv_path}")
    print(f"report={report_path}")
    print(f"contact_sheets={len(sheets)}")


if __name__ == "__main__":
    main()
