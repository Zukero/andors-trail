package com.gpl.rpg.AndorsTrail.model.actor;

import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.SkillCollection;
import com.gpl.rpg.AndorsTrail.model.item.DropList;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer;
import com.gpl.rpg.AndorsTrail.model.item.Loot;
import com.gpl.rpg.AndorsTrail.savegames.LegacySavegameFormatReaderForMonster;
import com.gpl.rpg.AndorsTrail.util.Coord;
import com.gpl.rpg.AndorsTrail.util.CoordRect;
import com.gpl.rpg.AndorsTrail.util.Range;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class Monster extends Actor {

	public Coord movementDestination = null;
	public long nextActionTime = 0;
	public final CoordRect nextPosition;

	public boolean isEnraged = false; //for pathfinding purposes
	public double THRESHOLD_HP_FLEE_PERCENT = 0.2;

	//	This value is for whether the monster will flee from ranged opponents
	boolean isFearsRanged = true;

	private boolean forceAggressive = false;
	private ItemContainer shopItems = null;

	private final MonsterType monsterType;
	public boolean isDesperate = false;

	public Monster(MonsterType monsterType) {
		super(monsterType.tileSize, false, monsterType.isImmuneToCriticalHits());
		this.monsterType = monsterType;
		this.iconID = monsterType.iconID;
		this.nextPosition = new CoordRect(new Coord(), monsterType.tileSize);
		resetStatsToBaseTraits();
		this.ap.setMax();
		this.health.setMax();
	}

	public void resetStatsToBaseTraits() {
		this.name = monsterType.name;
		this.ap.max = monsterType.maxAP;
		this.health.max = monsterType.maxHP;
		this.moveCost = monsterType.moveCost;
		this.attackCost = monsterType.attackCost;
		this.attackChance = monsterType.attackChance;
		this.criticalSkill = monsterType.criticalSkill;
		this.criticalMultiplier = monsterType.criticalMultiplier;
		if (monsterType.damagePotential != null) this.damagePotential.set(monsterType.damagePotential);
		else this.damagePotential.set(0, 0);
		this.blockChance = monsterType.blockChance;
		this.damageResistance = monsterType.damageResistance;
		this.onHitEffects = monsterType.onHitEffects;
	}

	public DropList getDropList() { return monsterType.dropList; }
	public int getExp() { return monsterType.exp; }
	public String getPhraseID() { return monsterType.phraseID; }
	public String getMonsterTypeID() { return monsterType.id; }
	public String getFaction() { return monsterType.faction; }
	public MonsterType.MonsterClass getMonsterClass() { return monsterType.monsterClass; }
	public MonsterType.AggressionType getMovementAggressionType() { return monsterType.aggressionType; }
	public MonsterType.BraveryType getMovementBraveryType() {return monsterType.braveryType;}

	public void createLoot(Loot container, Player player) {
		int exp = this.getExp();
		exp += exp * player.getSkillLevel(SkillCollection.SkillID.moreExp) * SkillCollection.PER_SKILLPOINT_INCREASE_MORE_EXP_PERCENT / 100;
		container.exp += exp;
		DropList dropList = getDropList();
		if (dropList == null) return;
		dropList.createRandomLoot(container, player);
	}
	public ItemContainer getShopItems(Player player) {
		if (shopItems != null) return shopItems;
		Loot loot = new Loot();
		shopItems = loot.items;
		getDropList().createRandomLoot(loot, player);
		return shopItems;
	}
	public void resetShopItems() {
		this.shopItems = null;
	}
	public boolean isAdjacentTo(Player p) {
		return this.rectPosition.isAdjacentTo(p.position);
	}

	public boolean isAgressive() {
		return getPhraseID() == null || forceAggressive;
	}

	public void forceAggressive() {
		forceAggressive = true;
	}


	// ====== PARCELABLE ===================================================================

	public static Monster newFromParcel(DataInputStream src, WorldContext world, int fileversion) throws IOException {
		String monsterTypeId = src.readUTF();
		if (fileversion < 20) {
			monsterTypeId = monsterTypeId.replace(' ', '_').replace("\\'", "").toLowerCase();
		}
		MonsterType monsterType = world.monsterTypes.getMonsterType(monsterTypeId);

		if (fileversion < 25) return LegacySavegameFormatReaderForMonster.newFromParcel_pre_v25(src, fileversion, monsterType);

		return new Monster(src, world, fileversion, monsterType);
	}

	private Monster(DataInputStream src, WorldContext world, int fileversion, MonsterType monsterType) throws IOException {
		this(monsterType);

		boolean readCombatTraits = true;
		if (fileversion >= 25) readCombatTraits = src.readBoolean();
		if (readCombatTraits) {
			this.attackCost = src.readInt();
			this.attackChance = src.readInt();
			this.criticalSkill = src.readInt();
			if (fileversion <= 20) {
				this.criticalMultiplier = src.readInt();
			} else {
				this.criticalMultiplier = src.readFloat();
			}
			this.damagePotential.set(new Range(src, fileversion));
			this.blockChance = src.readInt();
			this.damageResistance = src.readInt();
		}

		this.ap.readFromParcel(src, fileversion);
		this.health.readFromParcel(src, fileversion);
		this.position.readFromParcel(src, fileversion);
		if (fileversion > 16) {
			final int numConditions = src.readInt();
			for(int i = 0; i < numConditions; ++i) {
				conditions.add(new ActorCondition(src, world, fileversion));
			}
		}

		if (fileversion >= 34) {
			this.moveCost = src.readInt();
		}

		this.forceAggressive = src.readBoolean();
		if (fileversion >= 31) {
			if (src.readBoolean()) {
				this.shopItems = ItemContainer.newFromParcel(src, world, fileversion);
			}
		}
	}

	public void writeToParcel(DataOutputStream dest) throws IOException {
		dest.writeUTF(getMonsterTypeID());
		if (attackCost == monsterType.attackCost
				&& attackChance == monsterType.attackChance
				&& criticalSkill == monsterType.criticalSkill
				&& criticalMultiplier == monsterType.criticalMultiplier
				&& damagePotential.equals(monsterType.damagePotential)
				&& blockChance == monsterType.blockChance
				&& damageResistance == monsterType.damageResistance
				) {
			dest.writeBoolean(false);
		} else {
			dest.writeBoolean(true);
			dest.writeInt(attackCost);
			dest.writeInt(attackChance);
			dest.writeInt(criticalSkill);
			dest.writeFloat(criticalMultiplier);
			damagePotential.writeToParcel(dest);
			dest.writeInt(blockChance);
			dest.writeInt(damageResistance);
		}
		ap.writeToParcel(dest);
		health.writeToParcel(dest);
		position.writeToParcel(dest);
		dest.writeInt(conditions.size());
		for (ActorCondition c : conditions) {
			c.writeToParcel(dest);
		}
		dest.writeInt(moveCost);

		dest.writeBoolean(forceAggressive);
		if (shopItems != null) {
			dest.writeBoolean(true);
			shopItems.writeToParcel(dest);
		} else {
			dest.writeBoolean(false);
		}
	}

	public boolean doesEngageWithRanged(){
		return (this.monsterType.braveryType == MonsterType.BraveryType.brave)
				|| (this.monsterType.braveryType  == MonsterType.BraveryType.kamikaze);
	}

	public boolean doesFleeFromLowHP(){
		return !((this.monsterType.braveryType == MonsterType.BraveryType.none)
				|| (this.monsterType.braveryType  == MonsterType.BraveryType.kamikaze));
	}

	public boolean isFleeing() {
		// todo,twirl add a status condition for it to check if attacked
		if(this.monsterType.braveryType == MonsterType.BraveryType.coward)
			return true;

		// Fear of ranged weapons
		if(!isFearsRanged && this.monsterType.braveryType == MonsterType.BraveryType.confused)
			return true;

		if(doesFleeFromLowHP()
				&& (this.health.current <= this.health.max * this.THRESHOLD_HP_FLEE_PERCENT))
			return true;

		return false;
	}

	public void setIsEnraged(boolean weaponType)
	{
		// Confusion caused by ranged weapons
		if(weaponType)
			isFearsRanged = doesEngageWithRanged();

		this.isEnraged = isFearsRanged && !isFleeing();
	}

	public boolean getIsEnraged(){
		if(isFleeing())
			this.isEnraged = false;
		return this.isEnraged;
	}
}
