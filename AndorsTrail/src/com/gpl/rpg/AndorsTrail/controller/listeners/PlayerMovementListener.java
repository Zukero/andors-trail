package com.gpl.rpg.AndorsTrail.controller.listeners;

import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.util.Coord;

public interface PlayerMovementListener {
	void onPlayerMoved(Coord newPosition, Coord previousPosition);
	void onPlayerEnteredNewMap(PredefinedMap map, Coord p);
	void onPlayerAimToofar();
	void onPlayerAimInvalid();
}
