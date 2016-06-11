package com.gpl.rpg.AndorsTrail.model.ability;

import com.gpl.rpg.AndorsTrail.model.actor.Player;

public final class SkillInfo {
	public static final int MAXLEVEL_NONE = -1;
	public static enum LevelUpType {
		alwaysShown
		,onlyByQuests
		,firstLevelRequiresQuest
	}

	public final SkillCollection.SkillID id;
	public final int maxLevel;
	public final LevelUpType levelupVisibility;
	public final SkillLevelRequirement[] levelupRequirements;
	public final SkillCollection.SkillCategory categoryType;
	public SkillInfo(
			SkillCollection.SkillID id
			, int maxLevel
			, LevelUpType levelupVisibility
			, SkillCollection.SkillCategory categoryType
			, SkillLevelRequirement[] levelupRequirements
	) {
		this.id = id;
		this.maxLevel = maxLevel;
		this.levelupVisibility = levelupVisibility;
		this.levelupRequirements = levelupRequirements;
		this.categoryType = categoryType;
	}

	public boolean hasMaxLevel() {
		if (maxLevel == MAXLEVEL_NONE) return false;
		else return true;
	}

	public boolean hasLevelupRequirements() {
		return levelupRequirements != null;
	}

	public boolean canLevelUpSkillTo(Player player, int requestedSkillLevel) {
		if (!hasLevelupRequirements()) return true;

		for (SkillLevelRequirement requirement : levelupRequirements) {
			if (!requirement.isSatisfiedByPlayer(player, requestedSkillLevel)) return false;
		}
		return true;
	}

	public static final class SkillLevelRequirement {
		public static enum RequirementType {
			skillLevel
			,experienceLevel
			,playerStat
		}
		public final RequirementType requirementType;
		public final String skillOrStatID;
		public final int everySkillLevelRequiresThisAmount;
		public final int initialRequiredAmount;

		private SkillLevelRequirement(RequirementType requirementType, int everySkillLevelRequiresThisAmount, int initialRequiredAmount, String skillOrStatID) {
			this.requirementType = requirementType;
			this.skillOrStatID = skillOrStatID;
			this.everySkillLevelRequiresThisAmount = everySkillLevelRequiresThisAmount;
			this.initialRequiredAmount = initialRequiredAmount;
		}

		public static SkillLevelRequirement requireOtherSkill(SkillCollection.SkillID skillID, int everySkillLevelRequiresThisAmount) {
			return new SkillLevelRequirement(RequirementType.skillLevel, everySkillLevelRequiresThisAmount, 0, skillID.name());
		}
		public static SkillLevelRequirement requireExperienceLevels(int everySkillLevelRequiresThisAmount, int initialRequiredAmount) {
			return new SkillLevelRequirement(RequirementType.experienceLevel, everySkillLevelRequiresThisAmount, initialRequiredAmount, null);
		}
		public static SkillLevelRequirement requirePlayerStats(Player.StatID statID, int everySkillLevelRequiresThisAmount, int initialRequiredAmount) {
			return new SkillLevelRequirement(RequirementType.playerStat, everySkillLevelRequiresThisAmount, initialRequiredAmount, statID.name());
		}

		public boolean isSatisfiedByPlayer(Player player, int requestedSkillLevel) {
			final int minimumValueRequired = getRequiredValue(requestedSkillLevel);
			final int playerValue = getRequirementActualValue(player);
			if (playerValue >= minimumValueRequired) return true;
			return false;
		}

		public int getRequiredValue(int requestedSkillLevel) {
			return requestedSkillLevel * everySkillLevelRequiresThisAmount + initialRequiredAmount;
		}

		private int getRequirementActualValue(Player player) {
			switch (requirementType) {
			case skillLevel: return player.getSkillLevel(SkillCollection.SkillID.valueOf(skillOrStatID));
			case experienceLevel: return player.getLevel();
			case playerStat: return player.getStatValue(Player.StatID.valueOf(skillOrStatID));
			default: return 0;
			}
		}
	}
}
