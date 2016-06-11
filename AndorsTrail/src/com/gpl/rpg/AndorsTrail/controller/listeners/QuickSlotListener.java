package com.gpl.rpg.AndorsTrail.controller.listeners;

public interface QuickSlotListener {
	void onQuickSlotChanged(int slotId);
	void onQuickSlotUsed(int slotId);
	void onPresetLoaded(Integer presetNumber, String name);
	void onPresetLoadFailed(Integer presetNumber, String name);

	void onPresetSelected(Integer index, String namePreset);
}
