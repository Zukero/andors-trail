package com.gpl.rpg.AndorsTrail.controller.listeners;

import com.gpl.rpg.AndorsTrail.util.ListOfListeners;

public final class QuickSlotListeners extends ListOfListeners<QuickSlotListener> implements QuickSlotListener {

	private final Function1<QuickSlotListener, Integer> onQuickSlotChanged = new Function1<QuickSlotListener, Integer>() {
		@Override public void call(QuickSlotListener listener, Integer slotId) { listener.onQuickSlotChanged(slotId); }
	};

	private final Function1<QuickSlotListener, Integer> onQuickSlotUsed = new Function1<QuickSlotListener, Integer>() {
		@Override public void call(QuickSlotListener listener, Integer slotId) { listener.onQuickSlotUsed(slotId); }
	};

	private final Function1<QuickSlotListener, String> onQuickSlotItemLooted = new Function1<QuickSlotListener, String>() {
		@Override public void call(QuickSlotListener listener, String itemID) { listener.onQuickSlotItemLooted(itemID); }
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
	public void onQuickSlotItemLooted(String itemID) {
		callAllListeners(this.onQuickSlotItemLooted, itemID);
	}
}
