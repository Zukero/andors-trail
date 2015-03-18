package com.gpl.rpg.AndorsTrail.resource;

import android.graphics.Color;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.util.ConstRange;

public final class VisualEffectCollection {

	public static enum VisualEffectID {
		redSplash
		,blueSwirl
		,greenSplash
		,miss;;

		public static VisualEffectID fromString(String s, VisualEffectID default_) {
			if (s == null) return default_;
			return valueOf(s);
		}
	}

	private final VisualEffect[] effects = new VisualEffect[VisualEffectID.values().length];

	public void initialize(DynamicTileLoader loader) {
		effects[VisualEffectID.redSplash.ordinal()] = createEffect(loader, R.drawable.effect_blood4, new ConstRange(14, 0), 400, Color.RED);
		effects[VisualEffectID.blueSwirl.ordinal()] = createEffect(loader, R.drawable.effect_heal2, new ConstRange(16, 0), 400, Color.rgb(150, 150, 255));
		effects[VisualEffectID.greenSplash.ordinal()] = createEffect(loader, R.drawable.effect_poison1, new ConstRange(16, 0), 400, Color.GREEN);
		effects[VisualEffectID.miss.ordinal()] = createEffect(loader, R.drawable.effect_miss1, new ConstRange(16, 0), 400, Color.GRAY);
	}

	public VisualEffect getVisualEffect(VisualEffectID effectID) {
		return effects[effectID.ordinal()];
	}

	private static VisualEffect createEffect(DynamicTileLoader loader, int drawableID, ConstRange frameRange, int duration, int textColor) {
		int[] frameIconIDs = new int[frameRange.max - frameRange.current];
		for(int i = 0; i < frameIconIDs.length; ++i) {
			frameIconIDs[i] = loader.prepareTileID(drawableID, frameRange.current + i);
		}
		return new VisualEffect(frameIconIDs, duration, textColor);
	}

	public static final class VisualEffect {
		public final int[] frameIconIDs;
		public final int duration; // milliseconds
		public final int textColor;
		//public final int fps = ModelContainer.attackAnimationFPS;
		//public final int millisecondPerFrame = 1000 / fps;
		//public final int totalFrames = duration / millisecondPerFrame;
		public final int fps;
		public final int millisecondPerFrame;
		public final int totalFrames;
		public final int lastFrame;

		public VisualEffect(int[] frameIconIDs, int duration, int textColor) {
			this.frameIconIDs = frameIconIDs;
			this.duration = duration;
			this.textColor = textColor;
			totalFrames = frameIconIDs.length;
			lastFrame = totalFrames - 1;
			millisecondPerFrame = duration / totalFrames;
			fps = 1000 / millisecondPerFrame;
		}
	}
}
