package com.gpl.rpg.AndorsTrail.controller;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.Handler;

import com.gpl.rpg.AndorsTrail.AndorsTrailPreferences;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.listeners.VisualEffectFrameListeners;
import com.gpl.rpg.AndorsTrail.model.actor.Actor;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.model.actor.MonsterType;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.resource.VisualEffectCollection;
import com.gpl.rpg.AndorsTrail.resource.VisualEffectCollection.VisualEffect;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileManager;
import com.gpl.rpg.AndorsTrail.util.Coord;
import com.gpl.rpg.AndorsTrail.util.CoordRect;
import com.gpl.rpg.AndorsTrail.util.Size;

public final class VisualEffectController {
	private int effectCount = 0;

	private final ControllerContext controllers;
	private final WorldContext world;
	private final VisualEffectCollection effectTypes;

	public final VisualEffectFrameListeners visualEffectFrameListeners = new VisualEffectFrameListeners();
	
	public VisualEffectController(ControllerContext controllers, WorldContext world) {
		this.controllers = controllers;
		this.world = world;
		this.effectTypes = world.visualEffectTypes;
	}

	public void startEffect(Coord position, VisualEffectCollection.VisualEffectID effectID, String displayValue, VisualEffectCompletedCallback callback, int callbackValue) {
		++effectCount;
		(new VisualEffectAnimation(effectTypes.getVisualEffect(effectID), position, displayValue, callback, callbackValue))
		.start();
	}

	private VisualEffectCollection.VisualEffectID enqueuedEffectID = null;
	private int enqueuedEffectValue = 0;
	public void enqueueEffect(VisualEffectCollection.VisualEffectID effectID, int displayValue) {
		if (enqueuedEffectID == null) {
			enqueuedEffectID = effectID;
		} else if (Math.abs(displayValue) > Math.abs(enqueuedEffectValue)) {
			enqueuedEffectID = effectID;
		}
		enqueuedEffectValue += displayValue;
	}
	public void startEnqueuedEffect(Coord position) {
		if (enqueuedEffectID == null) return;
		startEffect(position, enqueuedEffectID, (enqueuedEffectValue == 0) ? null : String.valueOf(enqueuedEffectValue), null, 0);
		enqueuedEffectID = null;
		enqueuedEffectValue = 0;
	}
	
	public void startActorMoveEffect(Actor actor, Coord origin, Coord destination, int duration, VisualEffectCompletedCallback callback, int callbackValue) {
		++effectCount;
		(new SpriteMoveAnimation(origin, destination, duration, actor, callback, callbackValue))
		.start();
	}

	public final class SpriteMoveAnimation extends Handler implements Runnable {
		
		private static final int millisecondsPerFrame=25;
		
		private final VisualEffectCompletedCallback callback;
		private final int callbackValue;

		public final int duration;
		public final Actor actor;
		public final Coord origin;
		public final Coord destination;
		
		
		@Override
		public void run() {
			update();
			if (System.currentTimeMillis() - actor.vfxStartTime >= duration) {
				onCompleted();
			} else {
				postDelayed(this, millisecondsPerFrame);
			}
		}
		
		public SpriteMoveAnimation(Coord origin, Coord destination, int duration, Actor actor, VisualEffectCompletedCallback callback, int callbackValue) {
			this.callback = callback;
			this.callbackValue = callbackValue;
			this.duration = duration;
			this.actor = actor;
			this.origin = origin;
			this.destination = destination;

		}
		
		private void update() {
			
			visualEffectFrameListeners.onNewSpriteMoveFrame(this);
		}

		private void onCompleted() {
			--effectCount;
			actor.hasVFXRunning = false;
			if (callback != null) callback.onVisualEffectCompleted(callbackValue);
			visualEffectFrameListeners.onSpriteMoveCompleted(this);
		}
		

		public void start() {
			actor.hasVFXRunning = true;
			actor.vfxDuration = duration;
			actor.vfxStartTime = System.currentTimeMillis();
			if (duration == 0 || !controllers.preferences.enableUiAnimations) onCompleted();
			else postDelayed(this, 0);
		}
		
		
		
	}

	public static final Paint textPaint = new Paint();
	static {
		textPaint.setShadowLayer(2, 1, 1, Color.DKGRAY);
		textPaint.setAlpha(255);
		textPaint.setTextAlign(Align.CENTER);
	}
	
	public final class VisualEffectAnimation extends Handler implements Runnable {

		@Override
		public void run() {
			if (currentFrame >= effect.lastFrame) {
				onCompleted();
			} else {
				postDelayed(this, effect.millisecondPerFrame * controllers.preferences.attackspeed_milliseconds / AndorsTrailPreferences.ATTACKSPEED_DEFAULT_MILLISECONDS);
				update();
			}
		}

		private void update() {
			++currentFrame;
			int frame = currentFrame;

			int tileID = effect.frameIconIDs[frame];
			int textYOffset = -2 * (frame);
			if (frame >= beginFadeAtFrame && displayText != null) {
				textPaint.setAlpha(255 * (effect.lastFrame - frame) / (effect.lastFrame - beginFadeAtFrame));
			}
			area.topLeft.y = position.y - 1;
			visualEffectFrameListeners.onNewAnimationFrame(this, tileID, textYOffset);
		}

		private void onCompleted() {
			--effectCount;
			visualEffectFrameListeners.onAnimationCompleted(this);
			if (callback != null) callback.onVisualEffectCompleted(callbackValue);
		}

		public void start() {
			if (!controllers.preferences.enableUiAnimations) onCompleted();
			else postDelayed(this, 0);
		}

		private int currentFrame = 0;

		private final VisualEffect effect;

		public final Coord position;
		public final String displayText;
		public final CoordRect area;
		private final int beginFadeAtFrame;
		private final VisualEffectCompletedCallback callback;
		private final int callbackValue;

		public VisualEffectAnimation(VisualEffect effect, Coord position, String displayValue, VisualEffectCompletedCallback callback, int callbackValue) {
			this.position = position;
			this.callback = callback;
			this.callbackValue = callbackValue;
			this.effect = effect;
			this.displayText = displayValue == null ? "" : displayValue;
			textPaint.setColor(effect.textColor);
			textPaint.setTextSize(world.tileManager.tileSize * 0.5f); // 32dp.
			Rect textBounds = new Rect();
			textPaint.getTextBounds(displayText, 0, displayText.length(), textBounds);
			int widthNeededInTiles = 1 + (textBounds.width() / world.tileManager.tileSize);
			if (widthNeededInTiles % 2 == 0) widthNeededInTiles++;
			this.area = new CoordRect(new Coord(position.x - (widthNeededInTiles / 2), position.y - 1), new Size(widthNeededInTiles, 2));
			this.beginFadeAtFrame = effect.lastFrame / 2;
		}
		
		public Paint getTextPaint(){
			return textPaint;
		}
	}

	public static interface VisualEffectCompletedCallback {
		public void onVisualEffectCompleted(int callbackValue);
	}

	public boolean isRunningVisualEffect() {
		return effectCount > 0;
	}


	public static final class BloodSplatter {
		public final long removeAfter;
		public final long reduceIconAfter;
		public final Coord position;
		public int iconID;
		public boolean reducedIcon = false;
		public BloodSplatter(int iconID, Coord position) {
			this.iconID = iconID;
			this.position = position;
			final long now = System.currentTimeMillis();
			removeAfter = now + Constants.SPLATTER_DURATION_MS;
			reduceIconAfter = now + Constants.SPLATTER_DURATION_MS / 2;
		}
	}

	public void updateSplatters(PredefinedMap map) {
		long now = System.currentTimeMillis();
		for (int i = map.splatters.size() - 1; i >= 0; --i) {
			BloodSplatter b = map.splatters.get(i);
			if (b.removeAfter <= now) {
				map.splatters.remove(i);
				controllers.monsterSpawnController.monsterSpawnListeners.onSplatterRemoved(map, b.position);
			} else if (!b.reducedIcon && b.reduceIconAfter <= now) {
				b.reducedIcon = true;
				b.iconID++;
				controllers.monsterSpawnController.monsterSpawnListeners.onSplatterChanged(map, b.position);
			}
		}
	}

	public void addSplatter(PredefinedMap map, Monster m) {
		int iconID = getSplatterIconFromMonsterClass(m.getMonsterClass());
		if (iconID > 0) {
			map.splatters.add(new BloodSplatter(iconID, m.position));
			controllers.monsterSpawnController.monsterSpawnListeners.onSplatterAdded(map, m.position);
		}
	}

	private static int getSplatterIconFromMonsterClass(MonsterType.MonsterClass monsterClass) {
		switch (monsterClass) {
		case insect:
		case undead:
		case reptile:
			return TileManager.iconID_splatter_brown_1a + Constants.rnd.nextInt(2) * 2;
		case humanoid:
		case animal:
		case giant:
			return TileManager.iconID_splatter_red_1a + Constants.rnd.nextInt(2) * 2;
		case demon:
		case construct:
		case ghost:
			return TileManager.iconID_splatter_white_1a;
		default:
			return -1;
		}
	}

	
}
