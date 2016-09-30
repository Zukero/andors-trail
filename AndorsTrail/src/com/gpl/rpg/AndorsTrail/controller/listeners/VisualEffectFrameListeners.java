package com.gpl.rpg.AndorsTrail.controller.listeners;

import com.gpl.rpg.AndorsTrail.controller.VisualEffectController.SpriteMoveAnimation;
import com.gpl.rpg.AndorsTrail.controller.VisualEffectController.VisualEffectAnimation;
import com.gpl.rpg.AndorsTrail.util.ListOfListeners;

public final class VisualEffectFrameListeners extends ListOfListeners<VisualEffectFrameListener> implements VisualEffectFrameListener {

	private final Function3<VisualEffectFrameListener, VisualEffectAnimation, Integer, Integer> onNewAnimationFrame = new Function3<VisualEffectFrameListener, VisualEffectAnimation, Integer, Integer>() {
		@Override public void call(VisualEffectFrameListener listener, VisualEffectAnimation animation, Integer tileID, Integer textYOffset) { listener.onNewAnimationFrame(animation, tileID, textYOffset); }
	};

	private final Function1<VisualEffectFrameListener, VisualEffectAnimation> onAnimationCompleted = new Function1<VisualEffectFrameListener, VisualEffectAnimation>() {
		@Override public void call(VisualEffectFrameListener listener, VisualEffectAnimation animation) { listener.onAnimationCompleted(animation); }
	};
	
	private final Function1<VisualEffectFrameListener, SpriteMoveAnimation> onNewSpriteMoveFrame = new Function1<VisualEffectFrameListener, SpriteMoveAnimation>() {
		@Override public void call(VisualEffectFrameListener listener, SpriteMoveAnimation animation) { listener.onNewSpriteMoveFrame(animation); }
	};
	
	private final Function1<VisualEffectFrameListener, SpriteMoveAnimation> onSpriteMoveCompleted = new Function1<VisualEffectFrameListener, SpriteMoveAnimation>() {
		@Override public void call(VisualEffectFrameListener listener, SpriteMoveAnimation animation) { listener.onSpriteMoveCompleted(animation); }
	};
	
	@Override
	public void onNewAnimationFrame(VisualEffectAnimation animation, int tileID, int textYOffset) {
		callAllListeners(this.onNewAnimationFrame, animation, tileID, textYOffset);
	}

	@Override
	public void onAnimationCompleted(VisualEffectAnimation animation) {
		callAllListeners(this.onAnimationCompleted, animation);
	}
	
	@Override
	public void onNewSpriteMoveFrame(SpriteMoveAnimation animation) {
		callAllListeners(this.onNewSpriteMoveFrame, animation);
	}
	
	@Override
	public void onSpriteMoveCompleted(SpriteMoveAnimation animation) {
		callAllListeners(this.onSpriteMoveCompleted, animation);
	}
}
