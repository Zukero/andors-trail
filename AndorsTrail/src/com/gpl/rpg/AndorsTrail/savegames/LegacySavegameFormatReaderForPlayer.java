package com.gpl.rpg.AndorsTrail.savegames;

import java.io.DataInputStream;
import java.io.IOException;

import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.Constants;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionEffect;
import com.gpl.rpg.AndorsTrail.model.ability.SkillInfo;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.model.map.MonsterSpawnArea;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.model.quest.QuestProgress;
import com.gpl.rpg.AndorsTrail.util.Range;

public final class LegacySavegameFormatReaderForPlayer {

	public static void readQuestProgressPreV13(Player player, DataInputStream src, WorldContext world, int fileversion) throws IOException {
		final int size1 = src.readInt();
		for(int i = 0; i < size1; ++i) {
			String keyName = src.readUTF();
			if ("mikhail_visited".equals(keyName)) addQuestProgress(player, "andor", 1);
			else if ("qmikhail_bread_complete".equals(keyName)) addQuestProgress(player, "mikhail_bread", 100);
			else if ("qmikhail_bread".equals(keyName)) addQuestProgress(player, "mikhail_bread", 10);
			else if ("qmikhail_rats_complete".equals(keyName)) addQuestProgress(player, "mikhail_rats", 100);
			else if ("qmikhail_rats".equals(keyName)) addQuestProgress(player, "mikhail_rats", 10);
			else if ("oromir".equals(keyName)) addQuestProgress(player, "leta", 20);
			else if ("qleta_complete".equals(keyName)) addQuestProgress(player, "leta", 100);
			else if ("qodair".equals(keyName)) addQuestProgress(player, "odair", 10);
			else if ("qodair_complete".equals(keyName)) addQuestProgress(player, "odair", 100);
			else if ("qleonid_bonemeal".equals(keyName)) {
				addQuestProgress(player, "bonemeal", 10);
				addQuestProgress(player, "bonemeal", 20);
			}
			else if ("qtharal_complete".equals(keyName)) addQuestProgress(player, "bonemeal", 30);
			else if ("qthoronir_complete".equals(keyName)) addQuestProgress(player, "bonemeal", 100);
			else if ("qleonid_andor".equals(keyName)) addQuestProgress(player, "andor", 10);
			else if ("qgruil_andor".equals(keyName)) addQuestProgress(player, "andor", 20);
			else if ("qgruil_andor_complete".equals(keyName)) addQuestProgress(player, "andor", 30);
			else if ("qleonid_crossglen".equals(keyName)) addQuestProgress(player, "crossglen", 1);
			else if ("qjan".equals(keyName)) addQuestProgress(player, "jan", 10);
			else if ("qjan_complete".equals(keyName)) addQuestProgress(player, "jan", 100);
			else if ("qbucus_thieves".equals(keyName)) addQuestProgress(player, "andor", 40);
			else if ("qfallhaven_derelict".equals(keyName)) addQuestProgress(player, "andor", 50);
			else if ("qfallhaven_drunk".equals(keyName)) addQuestProgress(player, "fallhavendrunk", 10);
			else if ("qfallhaven_drunk_complete".equals(keyName)) addQuestProgress(player, "fallhavendrunk", 100);
			else if ("qnocmar_unnmir".equals(keyName)) addQuestProgress(player, "nocmar", 10);
			else if ("qnocmar".equals(keyName)) addQuestProgress(player, "nocmar", 20);
			else if ("qnocmar_complete".equals(keyName)) addQuestProgress(player, "nocmar", 200);
			else if ("qfallhaven_tavern_room2".equals(keyName)) addQuestProgress(player, "fallhaventavern", 10);
			else if ("qarcir".equals(keyName)) addQuestProgress(player, "arcir", 10);
			else if ("qfallhaven_oldman".equals(keyName)) addQuestProgress(player, "calomyran", 10);
			else if ("qcalomyran_tornpage".equals(keyName)) addQuestProgress(player, "calomyran", 20);
			else if ("qfallhaven_oldman_complete".equals(keyName)) addQuestProgress(player, "calomyran", 100);
			else if ("qbucus".equals(keyName)) addQuestProgress(player, "bucus", 10);
			else if ("qthoronir_catacombs".equals(keyName)) addQuestProgress(player, "bucus", 20);
			else if ("qathamyr_complete".equals(keyName)) addQuestProgress(player, "bucus", 40);
			else if ("qfallhaven_church".equals(keyName)) addQuestProgress(player, "bucus", 50);
			else if ("qbucus_complete".equals(keyName)) addQuestProgress(player, "bucus", 100);
		}
	}

	private static void addQuestProgress(Player player, String questID, int progress) {
		player.addQuestProgress(new QuestProgress(questID, progress));
	}

	public static void upgradeSavegame(Player player, WorldContext world, ControllerContext controllers, int fileversion) {

		if (fileversion <= 12) {
			player.useItemCost = 5;
			player.health.max += 5;
			player.health.current += 5;
			player.baseTraits.maxHP += 5;
		}

		if (fileversion <= 21) {
			int assignedSkillpoints = 0;
			for(SkillInfo skill : world.skills.getAllSkills()) {
				assignedSkillpoints += player.getSkillLevel(skill.id);
			}
			player.availableSkillIncreases = getExpectedNumberOfSkillpointsForLevel(player.getLevel()) - assignedSkillpoints;
		}

		if (fileversion <= 21) {
			if (player.hasExactQuestProgress("prim_hunt", 240)) player.addQuestProgress(new QuestProgress("bwm_agent", 250));
			if (player.hasExactQuestProgress("bwm_agent", 240)) player.addQuestProgress(new QuestProgress("prim_hunt", 250));
		}

		if (fileversion <= 27) {
			correctActorConditionsFromItemsPre0611b1(player, "bless", world, controllers, "elytharan_redeemer");
			correctActorConditionsFromItemsPre0611b1(player, "blackwater_misery", world, controllers, "bwm_dagger");
			correctActorConditionsFromItemsPre0611b1(player, "regen", world, controllers, "ring_shadow0");
		}

		if (fileversion <= 30) {
			player.baseTraits.attackCost = Player.DEFAULT_PLAYER_ATTACKCOST;
		}

		if (fileversion <= 37) {
			if (player.hasExactQuestProgress("lodar13_rest", 30) && player.hasExactQuestProgress("lodar13_rest", 31)) {
				player.addQuestProgress(new QuestProgress("lodar13_rest", 65));
			}
		}

		if (fileversion <= 40) {
			if (player.hasExactQuestProgress("farrik", 70)) {
				deactivateSpawnArea(world, controllers, "fallhaven_prison", "fallhaven_prisoner");
			}
		}
	}

	private static void deactivateSpawnArea(WorldContext world, ControllerContext controllers, String mapName, String monsterTypeSpawnGroup) {
		PredefinedMap map = world.maps.findPredefinedMap(mapName);
		for (MonsterSpawnArea area : map.spawnAreas) {
			if (!area.areaID.equals(monsterTypeSpawnGroup)) continue;
			controllers.monsterSpawnController.deactivateSpawnArea(area, true);
		}
	}

	public static int getExpectedNumberOfSkillpointsForLevel(int level) {
		level -= Constants.FIRST_SKILL_POINT_IS_GIVEN_AT_LEVEL;
		if (level < 0) return 0;
		return 1 + (int) Math.floor((float) level / Constants.NEW_SKILL_POINT_EVERY_N_LEVELS);
	}

	private static void correctActorConditionsFromItemsPre0611b1(Player player, String conditionTypeID, WorldContext world, ControllerContext controllers, String itemTypeIDWithCondition) {
		if (!player.hasCondition(conditionTypeID)) return;
		boolean hasItemWithCondition = false;
		for (Inventory.WearSlot slot : Inventory.WearSlot.values()) {
			ItemType t = player.inventory.getItemTypeInWearSlot(slot);
			if (t == null) continue;
			if (t.effects_equip == null) continue;
			if (t.effects_equip.addedConditions == null) continue;
			for(ActorConditionEffect e : t.effects_equip.addedConditions) {
				if (!e.conditionType.conditionTypeID.equals(conditionTypeID)) continue;
				hasItemWithCondition = true;
				break;
			}
		}
		if (hasItemWithCondition) return;

		controllers.actorStatsController.removeConditionsFromUnequippedItem(player, world.itemTypes.getItemType(itemTypeIDWithCondition));
	}

	public static void readCombatTraitsPreV034(DataInputStream src, int fileversion) throws IOException {
		if (fileversion >= 25) {
			if (!src.readBoolean()) return;
		}

		/*attackCost = */src.readInt();
		/*attackChance = */src.readInt();
		/*criticalSkill = */src.readInt();
		if (fileversion <= 20) {
			/*criticalMultiplier = */src.readInt();
		} else {
			/*criticalMultiplier = */src.readFloat();
		}
		/*damagePotential = */new Range(src, fileversion);
		/*blockChance = */src.readInt();
		/*damageResistance = */src.readInt();
	}
}
