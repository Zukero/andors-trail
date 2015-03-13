package com.gpl.rpg.AndorsTrail.controller.listeners;

public interface QuickSlotListener {
	void onQuickSlotChanged(int slotId);
	void onQuickSlotUsed(int slotId);
	void onQuickSlotItemLooted(String itemID);
}
