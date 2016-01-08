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

public final class MonsterMovementController implements EvaluateWalkable {
	private final ControllerContext controllers;
	private final WorldContext world;
	public final MonsterMovementListeners monsterMovementListeners = new MonsterMovementListeners();

	public boolean isRealMonsterMovementEmotional = true;

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
				if (!m.isCombatant() //|| !m.isEnraged()
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
		if (m.movementDestination == null && !(m.isFleeing()|| m.isEnraged())){
			// Monster has waited and should start to move again.
			int xLength = Constants.rnd.nextInt(area.area.size.width);
			int yLength = Constants.rnd.nextInt(area.area.size.height);
			int xDirection = sgn(xLength);
			int yDirection = sgn(yLength);

			m.movementDestination = new Coord(m.position);
			if (Constants.rnd.nextBoolean()) {
				m.movementDestination.x = xDirection * (area.area.topLeft.x +
						Math.abs(xLength));
			} else {
				m.movementDestination.y = yDirection * (area.area.topLeft.y +
						Math.abs(yLength));
			}
		} else if (m.movementDestination!= null && m.position.equals(m.movementDestination)) {
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
				if (!m.isCombatant() && !m.isDesperate()) {
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

			if (searchForPath || (m.isEnraged() && isRealMonsterMovementEmotional)) {
				if (findPathFor(m, playerPosition)) {
					world.model.player.isFollowed = true;
					m.rageDistance--;
					return;
				} else m.hasRage = false;
			}
		}


		if(m.movementDestination!=null)
		// Monster is fleeing in zig-zags.
		m.nextPosition.topLeft.set(
				m.position.x + sgn(m.movementDestination.x - m.position.x),
				m.position.y + sgn(m.movementDestination.y - m.position.y));

		if (m.isFleeing()) {
			if (!canFleeThere(m))
				if (findRandomFleePathFor(m, world.model.player.position))
					m.fearDistance--;
		}

	}
	private static void cancelCurrentMonsterMovement(final Monster m) {
		m.movementDestination = null;
		//int urgentFleeing = Constants.rollValue(Constants.monsterWaitTurns);

		// This hopefully makes monster fleeing fast enough
		//if(m.isFleeing())
		//	urgentFleeing = 1;

		m.nextActionTime = System.currentTimeMillis() +
				(getMillisecondsPerMove(m) * Constants.rollValue(Constants.monsterWaitTurns));
	}

	private static int getMillisecondsPerMove(Monster m) {
		int maxAP = m.getMaxAP();
		if(maxAP == 0)
			maxAP = 1;
		// Both increasing moveCost and decreasing maxAP will make them slower outside combat
		return Constants.MONSTER_MOVEMENT_TURN_DURATION_MS * m.getMoveCost() / maxAP;
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

	/*public boolean findFleePathFor(Monster m, Coord to) {
		int xDirection = sgn(m.position.x - to.x);
		int yDirection = sgn(m.position.y - to.y);

		m.nextPosition.topLeft.x = m.position.x + xDirection; // right
		m.nextPosition.topLeft.y = m.position.y;
		if (canFleeThere(m)) return true;

		m.nextPosition.topLeft.x -= xDirection; // up
		m.nextPosition.topLeft.y += yDirection;
		if (canFleeThere(m)) return true;

		m.nextPosition.topLeft.x += xDirection; // up right
		if (canFleeThere(m)) return true;

		m.nextPosition.topLeft.x -= 2 * xDirection; // up left
		if (canFleeThere(m)) return true;

		m.nextPosition.topLeft.y -= 2 * yDirection;
		m.nextPosition.topLeft.x += 2 * xDirection; // down right
		if (canFleeThere(m)) return true;

		// same y, null x
		m.nextPosition.topLeft.x -= xDirection; // down
		if (canFleeThere(m)) return true;

		// same x, null y
		m.nextPosition.topLeft.x -= xDirection; //left
		m.nextPosition.topLeft.y += yDirection;
		if (canFleeThere(m)) return true;

		m.nextPosition.topLeft.y -= yDirection; // down left
		if (canFleeThere(m)) return true;

		return false;
	}*/
	public boolean findRandomFleePathFor(Monster m, Coord to) {
		int xDirection = sgn(m.position.x - to.x);
		if(xDirection==0)
			xDirection = sgn(Constants.rnd.nextInt(21) - 10);

		int yDirection = sgn(m.position.y - to.y);
		if(yDirection==0)
			yDirection = sgn(Constants.rnd.nextInt(21) - 10);


		int[][] firstChoice = {{m.position.x + xDirection, m.position.y },
				{m.position.x, m.position.y+ yDirection},
				{m.position.x + xDirection, m.position.y+ yDirection}};
		int[][] secondChoice = {{m.position.x -xDirection, m.position.y + yDirection},
				{m.position.x + xDirection,m.position.y - yDirection}};
		int[][] thirdChoice = {{m.position.x, m.position.y - yDirection},
				{m.position.x - xDirection, m.position.y},
				{m.position.x - xDirection, m.position.y -yDirection}};

		if(iterateRandomFlee(m, firstChoice))
			return  true;
		if(iterateRandomFlee(m, secondChoice))
			return true;
		if(iterateRandomFlee(m, thirdChoice))
			return  true;

		m.nextPosition.topLeft.set(m.position);
		return false;
	}

	public boolean iterateRandomFlee(Monster m, int[][] possibilities){

		int preference;
		int[] nextCoord;

		int iterations = possibilities.length;
		while(iterations>0) {
			preference = Constants.rnd.nextInt(possibilities.length);
			nextCoord = possibilities[preference];
			if(nextCoord == null) continue;
			iterations--;
			m.nextPosition.topLeft.x = nextCoord[0];
			m.nextPosition.topLeft.y = nextCoord[1];
			if(canFleeThere(m)) return  true;
			possibilities[preference] = null;
		}
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
