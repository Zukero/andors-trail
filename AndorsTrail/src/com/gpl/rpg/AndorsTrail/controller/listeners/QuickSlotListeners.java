package com.gpl.rpg.AndorsTrail.controller.listeners;

import com.gpl.rpg.AndorsTrail.util.ListOfListeners;

public final class QuickSlotListeners extends ListOfListeners<QuickSlotListener> implements QuickSlotListener {

	private final Function1<QuickSlotListener, Integer> onQuickSlotChanged = new Function1<QuickSlotListener, Integer>() {
		@Override public void call(QuickSlotListener listener, Integer slotId) { listener.onQuickSlotChanged(slotId); }
	};

	private final Function1<QuickSlotListener, Integer> onQuickSlotUsed = new Function1<QuickSlotListener, Integer>() {
		@Override public void call(QuickSlotListener listener, Integer slotId) { listener.onQuickSlotUsed(slotId); }
	};
	private final Function2<QuickSlotListener, Integer, String> onPresetSelected = new Function2<QuickSlotListener, Integer,String>() {
		@Override public void call(QuickSlotListener listener, Integer index, String name) { listener.onPresetSelected(index, name); }
	};

	private final Function2<QuickSlotListener, Integer, String> onPresetLoaded = new Function2<QuickSlotListener, Integer, String>() {
		@Override public void call(QuickSlotListener listener, Integer presetIndex, String name) { listener.onPresetLoaded(presetIndex, name); }
	};
	private final Function2<QuickSlotListener, Integer, String> onPresetLoadFailed = new Function2<QuickSlotListener, Integer, String>() {
		@Override public void call(QuickSlotListener listener, Integer presetIndex, String name) { listener.onPresetLoaded(presetIndex, name); }
	};


	@Override
	public void onQuickSlotChanged(int slotId) {
		callAllListeners(this.onQuickSlotChanged, slotId);
	}

	@Override
	public void onQuickSlotUsed(int slotId) {
		callAllListeners(this.onQuickSlotUsed, slotId);
	}

	@Override
	public void onPresetLoaded(Integer presetNumber, String name) {
		callAllListeners(this.onPresetLoaded, presetNumber, name);
	}

	@Override
	public void onPresetLoadFailed(Integer presetNumber, String name) {
		callAllListeners(this.onPresetLoadFailed, presetNumber, name);
	}

	@Override
	public void onPresetSelected(Integer index, String namePreset) {
		callAllListeners(this.onPresetSelected, index, namePreset);
	}
}
