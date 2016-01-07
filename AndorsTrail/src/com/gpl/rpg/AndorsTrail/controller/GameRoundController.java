package com.gpl.rpg.AndorsTrail.controller;

import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.listeners.GameRoundListeners;
import com.gpl.rpg.AndorsTrail.model.map.MapObject;
import com.gpl.rpg.AndorsTrail.util.TimedMessageTask;

public final class GameRoundController implements TimedMessageTask.Callback {

	private final ControllerContext controllers;
	private final WorldContext world;
	private final TimedMessageTask roundTimer;
	public final GameRoundListeners gameRoundListeners = new GameRoundListeners();

	public GameRoundController(ControllerContext controllers, WorldContext world) {
		this.controllers = controllers;
		this.world = world;
		this.roundTimer = new TimedMessageTask(this, Constants.TICK_DELAY, true);
	}

	private int ticksUntilNextRound = Constants.TICKS_PER_ROUND;
	private int ticksUntilNextFullRound = Constants.TICKS_PER_FULLROUND;

	@Override
	public boolean onTick(TimedMessageTask task) {
		if (!world.model.uiSelections.isMainActivityVisible) return false;
		if (world.model.uiSelections.isInCombat) return false;

		onNewTick();

		--ticksUntilNextRound;
		if (ticksUntilNextRound <= 0) {
			onNewRound();
			restartWaitForNextRound();
		}

		--ticksUntilNextFullRound;
		if (ticksUntilNextFullRound <= 0) {
			onNewFullRound();
			restartWaitForNextFullRound();
		}

		return true;
	}

	public void resetRoundTimers() {
		restartWaitForNextRound();
		restartWaitForNextFullRound();
	}

	public void resume() {
		world.model.uiSelections.isMainActivityVisible = true;
		roundTimer.start();

		if (world.model.uiSelections.isInCombat) {
			controllers.combatController.setCombatSelection(world.model.uiSelections.selectedMonster, world.model.uiSelections.selectedPosition);
			controllers.combatController.enterCombat(CombatController.BeginTurnAs.continueLastTurn);
		}
	}

	private void restartWaitForNextFullRound() {
		ticksUntilNextFullRound = Constants.TICKS_PER_FULLROUND;
	}

	private void restartWaitForNextRound() {
		ticksUntilNextRound = Constants.TICKS_PER_ROUND;
	}

	public void pause() {
		roundTimer.stop();
		world.model.uiSelections.isMainActivityVisible = false;
	}

	public void onNewFullRound() {
		controllers.mapController.resetMapsNotRecentlyVisited();
		controllers.actorStatsController.applyConditionsToMonsters(world.model.currentMap, true);
		controllers.actorStatsController.applyConditionsToPlayer(world.model.player, true);
		gameRoundListeners.onNewFullRound();
	}

	private void onNewRound() {
		onNewMonsterRound();
		onNewPlayerRound();
		gameRoundListeners.onNewRound();
	}
	public void onNewPlayerRound() {
		world.model.worldData.tickWorldTime();
		controllers.actorStatsController.applyConditionsToPlayer(world.model.player, false);
		controllers.actorStatsController.applySkillEffectsForNewRound(world.model.player, world.model.currentMap);
		controllers.mapController.handleMapEvents(world.model.currentMap, world.model.player.position, MapObject.MapObjectEvaluationType.afterEveryRound);
	}
	public void onNewMonsterRound() {
		controllers.actorStatsController.applyConditionsToMonsters(world.model.currentMap, false);
	}

	private void onNewTick() {
		world.model.player.isFollowed = false;
		controllers.monsterMovementController.moveMonsters();
		controllers.monsterSpawnController.maybeSpawn(world.model.currentMap, world.model.currentTileMap);
		controllers.monsterMovementController.attackWithAgressiveMonsters();
		controllers.effectController.updateSplatters(world.model.currentMap);
		controllers.mapController.handleMapEvents(world.model.currentMap, world.model.player.position, MapObject.MapObjectEvaluationType.continuously);
		gameRoundListeners.onNewTick();
	}
}
