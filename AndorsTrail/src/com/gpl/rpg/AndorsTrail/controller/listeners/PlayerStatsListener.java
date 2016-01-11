package com.gpl.rpg.AndorsTrail.controller.listeners;

import com.gpl.rpg.AndorsTrail.model.actor.Player;

public interface PlayerStatsListener {
	void onPlayerExperienceChanged(Player p);
	void onPlayerChangedRangedWeapon();
}
