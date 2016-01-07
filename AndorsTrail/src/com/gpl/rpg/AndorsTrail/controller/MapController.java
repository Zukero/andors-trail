package com.gpl.rpg.AndorsTrail.controller;

import android.content.res.Resources;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.listeners.MapLayoutListeners;
import com.gpl.rpg.AndorsTrail.controller.listeners.WorldEventListeners;
import com.gpl.rpg.AndorsTrail.model.ability.SkillCollection;
import com.gpl.rpg.AndorsTrail.model.actor.Actor;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.conversation.Reply;
import com.gpl.rpg.AndorsTrail.model.map.LayeredTileMap;
import com.gpl.rpg.AndorsTrail.model.map.MapObject;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.model.map.ReplaceableMapSection;
import com.gpl.rpg.AndorsTrail.util.Coord;

import java.util.List;

public final class MapController {

	private final ControllerContext controllers;
	private final WorldContext world;
	public final WorldEventListeners worldEventListeners = new WorldEventListeners();
	public final MapLayoutListeners mapLayoutListeners = new MapLayoutListeners();
	private ConversationController.ConversationStatemachine mapScriptExecutor;

	public MapController(ControllerContext controllers, WorldContext world) {
		this.controllers = controllers;
		this.world = world;
	}

	public void handleMapEventsAfterMovement(PredefinedMap currentMap, Coord newPosition, Coord lastPosition) {
		// Several map objects can now overlap. Inactive ones won't be returned by getActiveEventObjectsAt()
		List<MapObject> objects = currentMap.getActiveEventObjectsAt(newPosition);
		if (objects == null) return;
		for (MapObject mapObject : objects) {

			switch (mapObject.evaluateWhen) {
			case afterEveryRound:
				return;
			case whenEntering:
				// Do not trigger event if the player already was on the same MapObject before.
				if (mapObject.position.contains(lastPosition)) return;
				break;
			}
			handleMapEvent(mapObject, newPosition);
		}
	}

	public void handleMapEvents(PredefinedMap currentMap, Coord position, MapObject.MapObjectEvaluationType evaluationType) {
		List<MapObject> objects = currentMap.getActiveEventObjectsAt(position);
		if (objects == null) return;
		for (MapObject mapObject : objects) {
			if (mapObject == null) return;
			if (mapObject.evaluateWhen != evaluationType) return;
			handleMapEvent(mapObject, position);
		}
	}

	private void handleMapEvent(MapObject o, Coord position) {
		if (!shouldHandleMapEvent(o)) return;
		switch (o.type) {
		case sign:
			if (o.id == null || o.id.length() <= 0) return;
			worldEventListeners.onPlayerSteppedOnMapSignArea(o);
			break;
		case newmap:
			if (o.map == null || o.place == null) return;
			int offset_x = position.x - o.position.topLeft.x;
			int offset_y = position.y - o.position.topLeft.y;
			controllers.movementController.placePlayerAsyncAt(MapObject.MapObjectType.newmap, o.map, o.place, offset_x, offset_y);
			break;
		case rest:
			steppedOnRestArea(o);
			break;
		case script:
			runScriptArea(o);
			break;
		}
	}

	private boolean shouldHandleMapEvent(MapObject mapObject) {
		if (!mapObject.isActive) return false;
		if (world.model.uiSelections.isInCombat) {
			// Only "script" events may run while in combat.
			if (mapObject.type != MapObject.MapObjectType.script) return false;
		}
		return true;
	}

	private void runScriptArea(MapObject o) {
		Resources res = controllers.getResources();
		mapScriptExecutor.proceedToPhrase(res, o.id, true, true);
		controllers.mapController.applyCurrentMapReplacements(res, true);
	}

	private void steppedOnRestArea(MapObject area) {
		if (controllers.preferences.confirmRest) {
			worldEventListeners.onPlayerSteppedOnRestArea(area);
		} else {
			rest(area);
		}
	}

	public void steppedOnMonster(Monster m, Coord p) {
		if(m == null)
			return;

		if (m.isAgressive() || (m.IsEnraged())) {
			controllers.combatController.setCombatSelection(m, p);
			if (controllers.preferences.confirmAttack) {
				worldEventListeners.onPlayerSteppedOnMonster(m);
			} else {
				controllers.combatController.enterCombat(CombatController.BeginTurnAs.player);
			}
		} else {
			if(world.model.player.isInAimMode()){
				// cancel aiming when player tries
				// to ranged-attack non-aggressive monster
				//world.model.player.cancelAimMode();
				}
			else {
				worldEventListeners.onPlayerStartedConversation(m, m.getPhraseID());
			}
		}
	}

	public void handlePlayerDeath() {
		controllers.combatController.exitCombat(false);
		final Player player = world.model.player;
		int lostExp = player.getCurrentLevelExperience() * Constants.PERCENT_EXP_LOST_WHEN_DIED / 100;
		lostExp -= lostExp * player.getSkillLevel(SkillCollection.SkillID.lowerExploss) * SkillCollection.PER_SKILLPOINT_INCREASE_EXPLOSS_PERCENT / 100;

		if (lostExp < 0) lostExp = 0;
		controllers.actorStatsController.addExperience(-lostExp);
		world.model.statistics.addPlayerDeath(lostExp);
		controllers.movementController.respawnPlayerAsync();
		lotsOfTimePassed();
		worldEventListeners.onPlayerDied(lostExp);
	}

	public void lotsOfTimePassed() {
		final Player player = world.model.player;
		controllers.actorStatsController.removeAllTemporaryConditions(player);
		controllers.actorStatsController.recalculatePlayerStats(player);
		controllers.actorStatsController.setActorMaxAP(player);
		controllers.actorStatsController.setActorMaxHealth(player);
		for (PredefinedMap m : world.maps.getAllMaps()) {
			m.resetTemporaryData();
		}
		controllers.monsterSpawnController.spawnAll(world.model.currentMap, world.model.currentTileMap);
		world.model.worldData.tickWorldTime(20);
		controllers.gameRoundController.resetRoundTimers();
	}

	public void rest(MapObject area) {
		lotsOfTimePassed();
		world.model.player.setSpawnPlace(world.model.currentMap.name, area.id);
		worldEventListeners.onPlayerRested();
	}

	public boolean canEnterKeyArea(MapObject area) {
		if (ConversationController.canFulfillRequirement(world, area.enteringRequirement)) {
			ConversationController.requirementFulfilled(world, area.enteringRequirement);
			return true;
		}
		worldEventListeners.onPlayerSteppedOnKeyArea(area);
		return false;
	}

	public void resetMapsNotRecentlyVisited() {
		for (PredefinedMap m : world.maps.getAllMaps()) {
			if (m == world.model.currentMap) continue;
			if (m.isRecentlyVisited()) continue;
			if (m.hasResetTemporaryData()) continue;
			m.resetTemporaryData();
		}
	}

	public void applyCurrentMapReplacements(final Resources res, boolean updateWorldmap) {
		if (!applyReplacements(world.model.currentMap, world.model.currentTileMap)) return;
		world.maps.worldMapRequiresUpdate = true;

		if (!updateWorldmap) return;
		WorldMapController.updateWorldMap(world, res);
		mapLayoutListeners.onMapTilesChanged(world.model.currentMap, world.model.currentTileMap);
	}

	private boolean applyReplacements(PredefinedMap map, LayeredTileMap tileMap) {
		boolean hasUpdated = false;
		if (tileMap.replacements != null) {
			for(ReplaceableMapSection replacement : tileMap.replacements) {
				if (replacement.isApplied) continue;
				if (!satisfiesCondition(replacement)) continue;
				tileMap.applyReplacement(replacement);
				hasUpdated = true;
			}
		}
		map.lastSeenLayoutHash = tileMap.getCurrentLayoutHash();
		return hasUpdated;
	}

	public boolean satisfiesCondition(ReplaceableMapSection replacement) {
		return world.model.player.hasExactQuestProgress(replacement.requireQuestStage);
	}

	private final ConversationController.ConversationStatemachine.ConversationStateListener conversationStateListener = new ConversationController.ConversationStatemachine.ConversationStateListener() {
		@Override
		public void onTextPhraseReached(String message, Actor actor, String phraseID) {
			worldEventListeners.onScriptAreaStartedConversation(phraseID);
		}
		@Override public void onScriptEffectsApplied(ConversationController.ScriptEffectResult scriptEffectResult) { }
		@Override public void onConversationEnded() { }
		@Override public void onConversationEndedWithShop(Monster npc) { }
		@Override public void onConversationEndedWithCombat(Monster npc) { }
		@Override public void onConversationEndedWithRemoval(Monster npc) { }
		@Override public void onConversationCanProceedWithNext() { }
		@Override public void onConversationHasReply(Reply r, String message) { }
	};
	public void prepareScriptsOnCurrentMap() {
		mapScriptExecutor = new ConversationController.ConversationStatemachine(world, controllers, conversationStateListener);
	}

	public void activateMapObject(PredefinedMap map, MapObject o) {
		if (o.isActive) return;
		o.isActive = true;
		if (o.type == MapObject.MapObjectType.container) map.createContainerLoot(o);
	}

	public void deactivateMapObject(MapObject o) {
		o.isActive = false;
	}
}
