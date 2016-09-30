package com.gpl.rpg.AndorsTrail.model.map;

import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import com.gpl.rpg.AndorsTrail.util.Coord;
import com.gpl.rpg.AndorsTrail.util.CoordRect;
import com.gpl.rpg.AndorsTrail.util.Size;

import java.util.Collection;

public final class LayeredTileMap {
	private static final ColorFilter colorFilterBlack20 = createGrayScaleColorFilter(0.8f);
	private static final ColorFilter colorFilterBlack40 = createGrayScaleColorFilter(0.6f);
	private static final ColorFilter colorFilterBlack60 = createGrayScaleColorFilter(0.4f);
	private static final ColorFilter colorFilterBlack80 = createGrayScaleColorFilter(0.2f);
	private static final ColorFilter colorFilterInvert = createInvertColorFilter();
	private static final ColorFilter colorFilterBW = createBWColorFilter();
	private static final ColorFilter colorFilterRedTint = createRedTintColorFilter();
	private static final ColorFilter colorFilterGreenTint = createGreenTintColorFilter();
	private static final ColorFilter colorFilterBlueTint = createBlueTintColorFilter();

	private final Size size;
	public final MapSection currentLayout;
	private String currentLayoutHash;
	public final ReplaceableMapSection[] replacements;
	public final String colorFilter;
	public final Collection<Integer> usedTileIDs;
	public LayeredTileMap(
			Size size
			, MapSection layout
			, ReplaceableMapSection[] replacements
			, String colorFilter
			, Collection<Integer> usedTileIDs
	) {
		this.size = size;
		this.currentLayout = layout;
		this.replacements = replacements;
		this.colorFilter = colorFilter;
		this.usedTileIDs = usedTileIDs;
		this.currentLayoutHash = currentLayout.calculateHash();
	}

	public final boolean isWalkable(final Coord p) {
		if (isOutside(p.x, p.y)) return false;
		return currentLayout.isWalkable[p.x][p.y];
	}
	public final boolean isWalkable(final int x, final int y) {
		if (isOutside(x, y)) return false;
		return currentLayout.isWalkable[x][y];
	}
	public final boolean isWalkable(final CoordRect p) {
		for (int y = 0; y < p.size.height; ++y) {
			for (int x = 0; x < p.size.width; ++x) {
				if (!isWalkable(p.topLeft.x + x, p.topLeft.y + y)) return false;
			}
		}
		return true;
	}
	public final boolean isOutside(final Coord p) { return isOutside(p.x, p.y); }
	public final boolean isOutside(final int x, final int y) {
		if (x < 0) return true;
		if (y < 0) return true;
		if (x >= size.width) return true;
		if (y >= size.height) return true;
		return false;
	}
	public final boolean isOutside(final CoordRect area) {
		if (isOutside(area.topLeft)) return true;
		if (area.topLeft.x + area.size.width > size.width) return true;
		if (area.topLeft.y + area.size.height > size.height) return true;
		return false;
	}

	public void setColorFilter(Paint mPaint) {
		mPaint.setColorFilter(getColorFilter());
	}

	public ColorFilter getColorFilter() {
		if (colorFilter == null) return null;
		else if (colorFilter.length() <= 0) return null;
		else if (colorFilter.equals("black20")) return colorFilterBlack20;
		else if (colorFilter.equals("black40")) return colorFilterBlack40;
		else if (colorFilter.equals("black60")) return colorFilterBlack60;
		else if (colorFilter.equals("black80")) return colorFilterBlack80;
		else if (colorFilter.equals("invert")) return colorFilterInvert;
		else if (colorFilter.equals("bw")) return colorFilterBW;
		else if (colorFilter.equals("redtint")) return colorFilterRedTint;
		else if (colorFilter.equals("greentint")) return colorFilterGreenTint;
		else if (colorFilter.equals("bluetint")) return colorFilterBlueTint;
		return null;
	}

	private static ColorMatrixColorFilter createGrayScaleColorFilter(float blackOpacity) {
		final float f = blackOpacity;
		return new ColorMatrixColorFilter(new float[] {
			f,     0.00f, 0.00f, 0.0f, 0.0f,
			0.00f, f,     0.00f, 0.0f, 0.0f,
			0.00f, 0.00f, f,     0.0f, 0.0f,
			0.00f, 0.00f, 0.00f, 1.0f, 0.0f
		});
	}
	
	private static ColorMatrixColorFilter createInvertColorFilter() {
		return new ColorMatrixColorFilter(new float[] {
			-1.00f, 0.00f, 0.00f, 0.0f, 255.0f,
			0.00f, -1.00f, 0.00f, 0.0f, 255.0f,
			0.00f, 0.00f, -1.00f, 0.0f, 255.0f,
			0.00f, 0.00f, 0.00f, 1.0f, 0.0f
		});
	}
	
	private static ColorMatrixColorFilter createBWColorFilter() {
		return new ColorMatrixColorFilter(new float[] {
			0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
			0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
			0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
			0.00f, 0.00f, 0.00f, 1.0f, 0.0f
		});
	}

	private static ColorMatrixColorFilter createRedTintColorFilter() {
		return new ColorMatrixColorFilter(new float[] {
				1.20f, 0.20f, 0.20f, 0.0f, 25.0f,
				0.00f, 0.80f, 0.00f, 0.0f, 0.0f,
				0.00f, 0.00f, 0.80f, 0.0f, 0.0f,
				0.00f, 0.00f, 0.00f, 1.0f, 0.0f
		});
	}

	private static ColorMatrixColorFilter createGreenTintColorFilter() {
		return new ColorMatrixColorFilter(new float[] {
				0.85f, 0.00f, 0.00f, 0.0f, 0.0f,
				0.15f, 1.15f, 0.15f, 0.0f, 15.0f,
				0.00f, 0.00f, 0.85f, 0.0f, 0.0f,
				0.00f, 0.00f, 0.00f, 1.0f, 0.0f
		});
	}

	private static ColorMatrixColorFilter createBlueTintColorFilter() {
		return new ColorMatrixColorFilter(new float[] {
				0.70f, 0.00f, 0.00f, 0.0f, 0.0f,
				0.00f, 0.70f, 0.00f, 0.0f, 0.0f,
				0.30f, 0.30f, 1.30f, 0.0f, 40.0f,
				0.00f, 0.00f, 0.00f, 1.0f, 0.0f
		});
	}

	public String getCurrentLayoutHash() {
		return currentLayoutHash;
	}

	public void applyReplacement(ReplaceableMapSection replacement) {
		replacement.apply(currentLayout);
		currentLayoutHash = currentLayout.calculateHash();
	}
}
