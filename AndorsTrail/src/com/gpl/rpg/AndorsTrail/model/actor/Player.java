package com.gpl.rpg.AndorsTrail.model.actor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import android.util.FloatMath;
import android.util.SparseIntArray;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.Constants;
import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.SkillCollection;
import com.gpl.rpg.AndorsTrail.model.item.DropListCollection;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.Loot;
import com.gpl.rpg.AndorsTrail.model.quest.QuestProgress;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileManager;
import com.gpl.rpg.AndorsTrail.savegames.LegacySavegameFormatReaderForPlayer;
import com.gpl.rpg.AndorsTrail.util.Coord;
import com.gpl.rpg.AndorsTrail.util.Range;
import com.gpl.rpg.AndorsTrail.util.Size;

public final class Player extends Actor {

	public static final int DEFAULT_PLAYER_ATTACKCOST = 4;
	public final Coord nextPosition;

	// TODO: Should be privates
	public int level;
	public final PlayerBaseTraits baseTraits = new PlayerBaseTraits();
	public final Range levelExperience; // ranges from 0 to the delta-amount of exp required for next level
	public final Inventory inventory;
	private final SparseIntArray skillLevels = new SparseIntArray();
	public int availableSkillIncreases = 0;
	public int useItemCost;
	public int reequipCost;
	public int totalExperience;

	public int maxTelepathyRange = 20;
	//public int increaseMaxRange;

	public boolean inTeleportMode = false;
	public boolean inTelepathyMode = false;
	//public boolean hasRangedWeaponEquippedInInventory = false; //enabled through debug button menu
	//allows player to attack from afar
	public boolean inAimMode = false; //needs to be enabled in order to initiate ranged-attack outside combat
	public boolean justEquippedIsEnough = true; //toggled through debug menu, allows attacking without aim-mode


	private final HashMap<String, HashSet<Integer> > questProgress = new HashMap<String, HashSet<Integer> >();
	private String spawnMap;
	private String spawnPlace;
	private final HashMap<String, Integer> alignments = new HashMap<String, Integer>();
	public int maxTeleportRange = 20;

	/*public boolean toggleEquipOfRangedWeapon() {
		if(!this.hasRangedWeaponEquippedInInventory) this.hasRangedWeaponEquippedInInventory = true;
		else this.hasRangedWeaponEquippedInInventory = false;
		return this.hasRangedWeaponEquippedInInventory;
	}*/

	public boolean toggleTeleportMode() {
		if(!this.inTeleportMode) this.inTeleportMode = true;
		else this.inTeleportMode = false;
		return this.inTeleportMode;
	}

	public boolean toggleAimMode() {
		if(!this.inAimMode
				&& this.isWieldingRangedWeapon())
			this.inAimMode = true;
		else
			this.inAimMode = false;
		return this.inAimMode;
	}
	public boolean toggleNeedAiming() {
		if(!this.justEquippedIsEnough)
			this.justEquippedIsEnough = true;
		else
			this.justEquippedIsEnough = false;
		return this.justEquippedIsEnough;
	}

	public boolean isInAimMode() {
		if(!this.isWieldingRangedWeapon())
			inAimMode = false;
		return inAimMode;
	}

	public int maxRangeOfWeapon() {
		return increaseMaxRange;
	}

	public boolean justEquippedIsEnough() {
		if(!isWieldingRangedWeapon())
			return false;
		else
			return this.justEquippedIsEnough;
	}

	// Unequipped stats
	public static final class PlayerBaseTraits {
		public int iconID;
		public int maxAP;
		public int maxHP;
		public int moveCost;
		public int attackCost;
		public int attackChance;
		public int criticalSkill;
		public float criticalMultiplier;
		public final Range damagePotential = new Range();
		public int blockChance;
		public int damageResistance;
		public int useItemCost;
		public int reequipCost;
		public int increaseMaxRange =1;
	}

	public void resetStatsToBaseTraits() {
		this.iconID = this.baseTraits.iconID;
		this.ap.max = this.baseTraits.maxAP;
		this.health.max = this.baseTraits.maxHP;
		this.moveCost = this.baseTraits.moveCost;
		this.attackCost = this.baseTraits.attackCost;
		this.attackChance = this.baseTraits.attackChance;
		this.criticalSkill = this.baseTraits.criticalSkill;
		this.criticalMultiplier = this.baseTraits.criticalMultiplier;
		this.damagePotential.set(this.baseTraits.damagePotential);
		this.blockChance = this.baseTraits.blockChance;
		this.damageResistance = this.baseTraits.damageResistance;
		this.useItemCost = this.baseTraits.useItemCost;
		this.reequipCost = this.baseTraits.reequipCost;
		this.increaseMaxRange = this.baseTraits.increaseMaxRange; //is not a base trait because share with actor class
	}

	public Player() {
		super(
			new Size(1, 1)
			, true // isPlayer
			, false // isImmuneToCriticalHits
		);
		this.nextPosition = new Coord();
		this.levelExperience = new Range();
		this.inventory = new Inventory();
	}

	public void initializeNewPlayer(DropListCollection dropLists, String playerName) {
		baseTraits.iconID = TileManager.CHAR_HERO;
		baseTraits.maxAP = 10;
		baseTraits.maxHP = 25;
		baseTraits.moveCost = 6;
		baseTraits.attackCost = DEFAULT_PLAYER_ATTACKCOST;
		baseTraits.attackChance = 60;
		baseTraits.criticalSkill = 0;
		baseTraits.criticalMultiplier = 1;
		baseTraits.damagePotential.set(1, 1);
		baseTraits.blockChance = 0;
		baseTraits.damageResistance = 0;
		baseTraits.useItemCost = 5;
		baseTraits.reequipCost = 5;
		baseTraits.increaseMaxRange=1;
		this.name = playerName;
		this.level = 1;
		this.totalExperience = 1;
		this.inventory.clear();
		this.questProgress.clear();
		this.skillLevels.clear();
		this.availableSkillIncreases = 0;
		this.alignments.clear();
		this.ap.set(baseTraits.maxAP, baseTraits.maxAP);
		this.health.set(baseTraits.maxHP, baseTraits.maxHP);
		this.conditions.clear();

		Loot startItems = new Loot();
		dropLists.getDropList(DropListCollection.DROPLIST_STARTITEMS).createRandomLoot(startItems, this);
		inventory.add(startItems);

		if (AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES) {
			this.spawnMap = "debugmap";
			this.spawnPlace = "start";
		} else {
			this.spawnMap = "home";
			this.spawnPlace = "rest";
		}
	}

	public boolean hasExactQuestProgress(QuestProgress progress) { return hasExactQuestProgress(progress.questID, progress.progress); }
	public boolean hasExactQuestProgress(String questID, int progress) {
		if (!questProgress.containsKey(questID)) return false;
		return questProgress.get(questID).contains(progress);
	}
	public boolean hasAnyQuestProgress(String questID) {
		return questProgress.containsKey(questID);
	}
	public boolean isLatestQuestProgress(String questID, int progress) {
		if (!questProgress.containsKey(questID)) return false;
		if (!questProgress.get(questID).contains(progress)) return false;
		for (int i : questProgress.get(questID)) {
			if (i > progress) return false;
		}
		return true;
	}
	public boolean addQuestProgress(QuestProgress progress) {
		if (hasExactQuestProgress(progress.questID, progress.progress)) return false;
		if (!questProgress.containsKey(progress.questID)) questProgress.put(progress.questID, new HashSet<Integer>());
		questProgress.get(progress.questID).add(progress.progress);
		return true; //Progress was added.
	}

	public boolean removeQuestProgress(QuestProgress progress){
        if (!hasExactQuestProgress(progress.questID, progress.progress)) return false;
		questProgress.get(progress.questID).remove(progress.progress);
		return true; //Progress was removed.
	}

	public void recalculateLevelExperience() {
		int experienceRequiredToReachThisLevel = getRequiredExperience(level);
		levelExperience.set(getRequiredExperienceForNextLevel(level), totalExperience - experienceRequiredToReachThisLevel);
	}

	private static int getRequiredExperience(int currentLevel) {
		int v = 0;
		for(int i = 1; i < currentLevel; ++i) {
			v += getRequiredExperienceForNextLevel(i);
		}
		return v;
	}
	private static final int EXP_base = 55;
	private static final int EXP_D = 400;
	private static final int EXP_powbase = 2;
	private static int getRequiredExperienceForNextLevel(int currentLevel) {
		return (int) (EXP_base * Math.pow(currentLevel, EXP_powbase + currentLevel/EXP_D));
	}

	public boolean canLevelup() {
		return levelExperience.isMax();
	}

	public void addSkillLevel(SkillCollection.SkillID skillID) {
		skillLevels.put(skillID.ordinal(), getSkillLevel(skillID) + 1);
	}
	public int getSkillLevel(SkillCollection.SkillID skillID) {
		return skillLevels.get(skillID.ordinal());
	}
	public boolean hasSkill(SkillCollection.SkillID skillID) {
		return getSkillLevel(skillID) > 0;
	}
	public boolean nextLevelAddsNewSkillpoint() {
		return thisLevelAddsNewSkillpoint(level + 1);
	}
	private static boolean thisLevelAddsNewSkillpoint(int level) {
		return ((level - Constants.FIRST_SKILL_POINT_IS_GIVEN_AT_LEVEL) % Constants.NEW_SKILL_POINT_EVERY_N_LEVELS == 0);
	}
	public boolean hasAvailableSkillpoints() {
		return availableSkillIncreases > 0;
	}

	public int getAlignment(String faction) {
		Integer v = alignments.get(faction);
		if (v == null) return 0;
		return v;
	}
	public void addAlignment(String faction, int delta) {
		int newValue = getAlignment(faction) + delta;
		alignments.put(faction, newValue);
	}

	public void setSpawnPlace(String spawnMap, String spawnPlace) {
		this.spawnPlace = spawnPlace;
		this.spawnMap = spawnMap;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getReequipCost() { return reequipCost; }
	public int getUseItemCost() { return useItemCost; }
	public int getAvailableSkillIncreases() { return availableSkillIncreases; }
	public int getLevel() { return level; }
	public int getTotalExperience() { return totalExperience; }
	public int getCurrentLevelExperience() { return levelExperience.current; }
	public int getMaxLevelExperience() { return levelExperience.max; }
	public int getGold() { return inventory.gold; }
	public String getSpawnMap() { return spawnMap; }
	public String getSpawnPlace() { return spawnPlace; }


	public static enum StatID {
		maxHP
		,maxAP
		,moveCost
		,attackCost
		,attackChance
		,criticalSkill
		,criticalMultiplier
		,damagePotentialMin
		,damagePotentialMax
		,blockChance
		,damageResistance
	}

	public int getStatValue(StatID stat) {
		switch (stat) {
		case maxHP: return baseTraits.maxHP;
		case maxAP: return baseTraits.maxAP;
		case moveCost: return baseTraits.moveCost;
		case attackCost: return baseTraits.attackCost;
		case attackChance: return baseTraits.attackChance;
		case criticalSkill: return baseTraits.criticalSkill;
		case criticalMultiplier: return (int) FloatMath.floor(baseTraits.criticalMultiplier);
		case damagePotentialMin: return baseTraits.damagePotential.current;
		case damagePotentialMax: return baseTraits.damagePotential.max;
		case blockChance: return baseTraits.blockChance;
		case damageResistance: return baseTraits.damageResistance;
		}
		return 0;
	}

	// ====== PARCELABLE ===================================================================

	public static Player newFromParcel(DataInputStream src, WorldContext world, ControllerContext controllers, int fileversion) throws IOException {
		Player player = new Player(src, world, fileversion);
		LegacySavegameFormatReaderForPlayer.upgradeSavegame(player, world, controllers, fileversion);
		return player;
	}

	public Player(DataInputStream src, WorldContext world, int fileversion) throws IOException {
		this();

		if (fileversion <= 33) LegacySavegameFormatReaderForPlayer.readCombatTraitsPreV034(src, fileversion);

		this.baseTraits.iconID = src.readInt();
		if (fileversion <= 33) /*this.tileSize = */new Size(src, fileversion);
		this.baseTraits.maxAP = src.readInt();
		this.baseTraits.maxHP = src.readInt();
		this.name = src.readUTF();
		this.moveCost = src.readInt();

		this.baseTraits.attackCost = src.readInt();
		this.baseTraits.attackChance = src.readInt();
		this.baseTraits.criticalSkill = src.readInt();
		if (fileversion <= 20) {
			this.baseTraits.criticalMultiplier = src.readInt();
		} else {
			this.baseTraits.criticalMultiplier = src.readFloat();
		}
		this.baseTraits.damagePotential.readFromParcel(src, fileversion);
		this.baseTraits.blockChance = src.readInt();
		this.baseTraits.damageResistance = src.readInt();

		if (fileversion <= 16) {
			this.baseTraits.moveCost = this.moveCost;
		} else {
			this.baseTraits.moveCost = src.readInt();
		}

		this.ap.set(new Range(src, fileversion));
		this.health.set(new Range(src, fileversion));
		this.position.set(new Coord(src, fileversion));
		if (fileversion > 16) {
			final int numConditions = src.readInt();
			for(int i = 0; i < numConditions; ++i) {
				this.conditions.add(new ActorCondition(src, world, fileversion));
			}
		}

		this.lastPosition.readFromParcel(src, fileversion);
		this.nextPosition.readFromParcel(src, fileversion);
		this.level = src.readInt();
		this.totalExperience = src.readInt();
		this.inventory.readFromParcel(src, world, fileversion);

		if (fileversion <= 13) LegacySavegameFormatReaderForPlayer.readQuestProgressPreV13(this, src, world, fileversion);

		this.baseTraits.useItemCost = src.readInt();
		this.baseTraits.reequipCost = src.readInt();
		final int numSkills = src.readInt();
		for(int i = 0; i < numSkills; ++i) {
			if (fileversion <= 21) {
				this.skillLevels.put(i, src.readInt());
			} else {
				final int skillID = src.readInt();
				this.skillLevels.put(skillID, src.readInt());
			}
		}
		this.spawnMap = src.readUTF();
		this.spawnPlace = src.readUTF();

		if (fileversion > 13) {
			final int numQuests = src.readInt();
			for(int i = 0; i < numQuests; ++i) {
				final String questID = src.readUTF();
				this.questProgress.put(questID, new HashSet<Integer>());
				final int numProgress = src.readInt();
				for(int j = 0; j < numProgress; ++j) {
					int progress = src.readInt();
					this.questProgress.get(questID).add(progress);
				}
			}
		}

		this.availableSkillIncreases = 0;
		if (fileversion > 21) {
			this.availableSkillIncreases = src.readInt();
		}

		if (fileversion >= 26) {
			final int numAlignments = src.readInt();
			for(int i = 0; i < numAlignments; ++i) {
				final String faction = src.readUTF();
				final int alignment = src.readInt();
				this.alignments.put(faction, alignment);
			}
		}
	}

	public void writeToParcel(DataOutputStream dest) throws IOException {
		dest.writeInt(baseTraits.iconID);
		dest.writeInt(baseTraits.maxAP);
		dest.writeInt(baseTraits.maxHP);
		dest.writeUTF(name);
		dest.writeInt(moveCost); // TODO: Should we really write this?
		dest.writeInt(baseTraits.attackCost);
		dest.writeInt(baseTraits.attackChance);
		dest.writeInt(baseTraits.criticalSkill);
		dest.writeFloat(baseTraits.criticalMultiplier);
		baseTraits.damagePotential.writeToParcel(dest);
		dest.writeInt(baseTraits.blockChance);
		dest.writeInt(baseTraits.damageResistance);
		dest.writeInt(baseTraits.moveCost);

		ap.writeToParcel(dest);
		health.writeToParcel(dest);
		position.writeToParcel(dest);
		dest.writeInt(conditions.size());
		for (ActorCondition c : conditions) {
			c.writeToParcel(dest);
		}
		lastPosition.writeToParcel(dest);
		nextPosition.writeToParcel(dest);
		dest.writeInt(level);
		dest.writeInt(totalExperience);
		inventory.writeToParcel(dest);
		dest.writeInt(baseTraits.useItemCost);
		dest.writeInt(baseTraits.reequipCost);
		dest.writeInt(skillLevels.size());
		for (int i = 0; i < skillLevels.size(); ++i) {
			dest.writeInt(skillLevels.keyAt(i));
			dest.writeInt(skillLevels.valueAt(i));
		}
		dest.writeUTF(spawnMap);
		dest.writeUTF(spawnPlace);
		dest.writeInt(questProgress.size());
		for(Entry<String, HashSet<Integer> > e : questProgress.entrySet()) {
			dest.writeUTF(e.getKey());
			dest.writeInt(e.getValue().size());
			for(int progress : e.getValue()) {
				dest.writeInt(progress);
			}
		}
		dest.writeInt(availableSkillIncreases);
		dest.writeInt(alignments.size());
		for(Entry<String, Integer> e : alignments.entrySet()) {
			dest.writeUTF(e.getKey());
			dest.writeInt(e.getValue());
		}
	}

	public void cancelAimMode(){
		// this makes player flee instead of moving in combat
		// allows player to flee from from combat mode
		this.inAimMode = false;
	}

	//public boolean isJokeWieldingRangedWeapon() {return this.hasRangedWeaponEquippedInInventory;}

	public boolean isWieldingRangedWeapon(){

		if( this.inventory.getItemTypeInWearSlot(
				Inventory.WearSlot.weapon) == null)
			return false;
		else if (this.inventory.getItemTypeInWearSlot(
				Inventory.WearSlot.weapon).isRangedWeapon())
			return true;
		return false;
	}
	/*public boolean isKindaWieldingRangedWeapon(){
		if(this.inventory.isWearing("wooden_longbow")){
			return true;
		}
		return false;
	}*/
}

