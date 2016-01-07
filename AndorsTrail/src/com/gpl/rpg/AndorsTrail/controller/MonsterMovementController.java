package com.gpl.rpg.AndorsTrail.controller;

import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.PathFinder.EvaluateWalkable;
import com.gpl.rpg.AndorsTrail.controller.listeners.MonsterMovementListeners;
import com.gpl.rpg.AndorsTrail.model.ability.SkillCollection;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.model.actor.MonsterType;
import com.gpl.rpg.AndorsTrail.model.map.LayeredTileMap;
import com.gpl.rpg.AndorsTrail.model.map.MapObject;
import com.gpl.rpg.AndorsTrail.model.map.MonsterSpawnArea;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.util.Coord;
import com.gpl.rpg.AndorsTrail.util.CoordRect;
import com.gpl.rpg.AndorsTrail.util.Size;

public final class MonsterMovementController implements EvaluateWalkable {
	private final ControllerContext controllers;
	private final WorldContext world;
	public final MonsterMovementListeners monsterMovementListeners = new MonsterMovementListeners();

	public boolean existAngryFollowingRealtime = true;

	public MonsterMovementController(ControllerContext controllers, WorldContext world) {
		this.controllers = controllers;
		this.world = world;
	}

	public void moveMonsters() {
		long currentTime = System.currentTimeMillis();

		for (MonsterSpawnArea a : world.model.currentMap.spawnAreas) {
			for (Monster m : a.monsters) {
				if (m.nextActionTime <= currentTime) {
					moveMonster(m, a);
				}
			}
		}
	}

	public void attackWithAgressiveMonsters() {
		for (MonsterSpawnArea a : world.model.currentMap.spawnAreas) {
			for (Monster m : a.monsters) {
				if (!m.isAgressive() //|| !m.IsEnraged()
						|| (m.isFleeing())) continue;
				if (!m.isAdjacentTo(world.model.player)) continue;
				// ^ this leaves out monster ranged-attacks
				int aggressionChanceBias = world.model.player.getSkillLevel(SkillCollection.SkillID.evasion) * SkillCollection.PER_SKILLPOINT_INCREASE_EVASION_MONSTER_ATTACK_CHANCE_PERCENTAGE;
				if (Constants.roll100(Constants.MONSTER_AGGRESSION_CHANCE_PERCENT - aggressionChanceBias)) {
					monsterMovementListeners.onMonsterSteppedOnPlayer(m);
					controllers.combatController.monsterSteppedOnPlayer(m);
					return;
				}
			}
		}
	}

	public static boolean monsterCanMoveTo(final PredefinedMap map, final LayeredTileMap tilemap, final CoordRect p) {
		if (tilemap != null) {
			if (!tilemap.isWalkable(p)) return false;
		}
		if (map.getMonsterAt(p) != null) return false;

		for (MapObject m : map.eventObjects) {
			if (m == null) continue;
			if (!m.position.intersects(p)) continue;
			switch (m.type) {
				case newmap:
				case keyarea:
				case rest:
					return false;
			}
		}
		return true;
	}

	private void moveMonster(final Monster m, final MonsterSpawnArea area) {
		PredefinedMap map = world.model.currentMap;
		LayeredTileMap tileMap = world.model.currentTileMap;
		m.nextActionTime = System.currentTimeMillis() + getMillisecondsPerMove(m);
		if (m.movementDestination == null //&& !( m.getIsEnraged() || m.isFleeing())
				) {
			// Monster has waited and should start to move again.
			int xLength = Constants.rnd.nextInt(area.area.size.width);
			int yLength = Constants.rnd.nextInt(area.area.size.height);
			int xDirection = sgn(xLength);
			int yDirection = sgn(yLength);

			if(m.isFleeing()){ // pick a random destination away
				xDirection= sgn(m.position.x- world.model.player.position.x);
				yDirection= sgn(m.position.y - world.model.player.position.y);
			}
			m.movementDestination = new Coord(m.position);
			if(m.isFleeing()){ // move diagonally to flee
				m.movementDestination.x = xDirection *(area.area.topLeft.x +
						Math.abs(xLength));
				m.movementDestination.y = yDirection *(area.area.topLeft.y +
						Math.abs(yLength));
			}
			else{ // straight line
				if (Constants.rnd.nextBoolean()) {
					m.movementDestination.x = xDirection *(area.area.topLeft.x +
							Math.abs(xLength));
				} else {
					m.movementDestination.y = yDirection *(area.area.topLeft.y +
							Math.abs(yLength));
				}
			}
		} else if (m.position.equals(m.movementDestination)) {
			// Monster has been moving and arrived at the destination.
			cancelCurrentMonsterMovement(m);
		} else {
			determineMonsterNextPosition(m, area, world.model.player.position);

			if (!monsterCanMoveTo(map, tileMap, m.nextPosition)) {
				cancelCurrentMonsterMovement(m);
				return;
			}
			if (m.nextPosition.contains(world.model.player.position)) {
				//do not step on player unless agressive or enraged
				if (!m.isAgressive() && !(m.IsEnraged())//||m.isDesperate))
						) {
					cancelCurrentMonsterMovement(m);
					return;
				}
				monsterMovementListeners.onMonsterSteppedOnPlayer(m);
				controllers.combatController.monsterSteppedOnPlayer(m);
			} else {
				moveMonsterToNextPosition(m, map);
			}
		}
	}

	private void determineMonsterNextPosition(Monster m, MonsterSpawnArea area, Coord playerPosition) {
		if (!m.isFleeing()) {

			boolean searchForPath = false;
			if (m.isAgressive()) {
				if (m.getMovementAggressionType() == MonsterType.AggressionType.protectSpawn) {
					if (area.area.contains(playerPosition)) searchForPath = true;
				} else if (m.getMovementAggressionType() == MonsterType.AggressionType.wholeMap) {
					searchForPath = true;
				}
			}

			if (searchForPath || (m.IsEnraged() && existAngryFollowingRealtime)) {
				if (findPathFor(m, playerPosition)) {
					m.rageDistance--;
					return;
				} else m.hasRage = false;
			}
		}

		// Monster is moving in a straight line. (but only if not fleeing)
		m.nextPosition.topLeft.set(
				m.position.x + sgn(m.movementDestination.x - m.position.x),
				m.position.y + sgn(m.movementDestination.y - m.position.y));

		if(!m.isAgressive())
			return;
		if (!m.isFleeing())
			return;

		if(canFleeThere(m))
			return;

		findFleePathFor(m, world.model.player.position);
		}

	private static void cancelCurrentMonsterMovement(final Monster m) {
		m.movementDestination = null;
		int urgentFleeing = Constants.rollValue(Constants.monsterWaitTurns);

		// This hopefully makes monster fleeing fast enough
		//if(m.isFleeing())
		//	urgentFleeing = 1;

		m.nextActionTime = System.currentTimeMillis() + (getMillisecondsPerMove(m) * urgentFleeing);
	}

	private static int getMillisecondsPerMove(Monster m) {
		return Constants.MONSTER_MOVEMENT_TURN_DURATION_MS * m.getMoveCost() / m.getMaxAP();
	}


	private int getMillisecondsPerCombatMove(Monster m) {
		if (controllers.preferences.attackspeed_milliseconds <= 0) return 0;
		return controllers.preferences.attackspeed_milliseconds;
	}

	public static final int sgn(int i) {
		if (i <= -1) return -1;
		if (i >= 1) return 1;
		return 0;
	}

	private final PathFinder pathfinder = new PathFinder(Constants.MAX_MAP_WIDTH, Constants.MAX_MAP_HEIGHT, this);

	public boolean findPathFor(Monster m, Coord to) {
		return pathfinder.findPathBetween(m.rectPosition, to, m.nextPosition);
	}

	public boolean findFleePathFor(Monster m, Coord to) {
		int xDirection = sgn(m.position.x - to.x);
		int yDirection = sgn(m.position.y - to.y);

		m.nextPosition.topLeft.x = m.position.x + xDirection; // away x, null y
		m.nextPosition.topLeft.y = m.position.y;
		if (canFleeThere(m)) return true;

		m.nextPosition.topLeft.x -= xDirection; // away y, null x
		m.nextPosition.topLeft.y += yDirection;
		if (canFleeThere(m)) return true;

		m.nextPosition.topLeft.x += xDirection; //away x and y
		if (canFleeThere(m)) return true;

		m.nextPosition.topLeft.x -= 2 * xDirection; // same x away y
		if (canFleeThere(m)) return true;

		m.nextPosition.topLeft.y -= 2 * yDirection;
		m.nextPosition.topLeft.x += 2 * xDirection; // same y away x
		if (canFleeThere(m)) return true;

		// same y, null x
		m.nextPosition.topLeft.x -= xDirection;
		if (canFleeThere(m)) return true;

		// same x, null y
		m.nextPosition.topLeft.x -= xDirection;
		m.nextPosition.topLeft.y += yDirection;
		if (canFleeThere(m)) return true;


		return false;
	}

	public boolean canFleeThere(Monster m){
		if(isWalkable(m.nextPosition) && !m.nextPosition.contains(world.model.player.position))
			return true;
		return false;

	}

	@Override
	public boolean isWalkable(CoordRect r) {
		return monsterCanMoveTo(world.model.currentMap, world.model.currentTileMap, r);
	}

	public void moveMonsterToNextPosition(final Monster m, final PredefinedMap map) {
		moveMonsterToNextPositionWithCallback(m, map, getMillisecondsPerMove(m) / 4, null);
	}

	public void moveMonsterToNextPositionDuringCombat(final Monster m, final PredefinedMap map, final VisualEffectController.VisualEffectCompletedCallback callback) {
		moveMonsterToNextPositionWithCallback(m, map, getMillisecondsPerCombatMove(m) / 4, callback);
	}

	private void moveMonsterToNextPositionWithCallback(final Monster m, final PredefinedMap map, int duration, final VisualEffectController.VisualEffectCompletedCallback callback) {
		final CoordRect previousPosition = new CoordRect(new Coord(m.position), m.rectPosition.size);
		m.lastPosition.set(previousPosition.topLeft);
		m.position.set(m.nextPosition.topLeft);
		controllers.effectController.startActorMoveEffect(m, previousPosition.topLeft, m.position, duration, new VisualEffectController.VisualEffectCompletedCallback() {

			@Override
			public void onVisualEffectCompleted(int callbackValue) {
				if (callback != null) callback.onVisualEffectCompleted(callbackValue);
				monsterMovementListeners.onMonsterMoved(map, m, previousPosition);
			}
		}, 0);
	}
}
