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


	public double hpFleeThreshold;
	public double rageMultiplier;
	public double fearMultiplier =1;
	public int lineOfSight;

	public int rageDistance = 0;
	public int fearDistance = 0;
	public boolean hasRage = false; //for pathfinding purposes

	//	This value is for whether the monster will flee from ranged opponents
	public boolean isDesperate = false;
	public boolean hasFleePath = false;

	private boolean forceAggressive = false;
	private ItemContainer shopItems = null;

	private final MonsterType monsterType;

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

		this.rageMultiplier = monsterType.rageMultiplier;
		this.hpFleeThreshold = monsterType.hpFleeThreshold;
		this.lineOfSight = monsterType.lineOfSight;
	}

	public DropList getDropList() { return monsterType.dropList; }
	public int getExp() { return monsterType.exp; }
	public String getPhraseID() { return monsterType.phraseID; }
	public String getMonsterTypeID() { return monsterType.id; }
	public String getFaction() { return monsterType.faction; }
	public MonsterType.MonsterClass getMonsterClass() { return monsterType.monsterClass; }
	public MonsterType.AggressionType getMovementAggressionType() { return monsterType.aggressionType; }
	//public MonsterType.BraveryType getMovementBraveryType() {return monsterType.braveryType;}

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
	//public boolean isInRangeOf(Player p){
		//return this.rectPosition.isAdjacentTo(p.position);
		//return true;}

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

	public boolean isFleeing() {
		if(this.health.current <= this.health.max * this.hpFleeThreshold)
			return true;

		if(fearDistance >0)
			return true;
		if(fearDistance<0)
			fearDistance =0;
		if(fearDistance ==0)
			return false;
		return false;
	}

	public void setRageAndFear(Coord root, Coord here)
	{
		// Add -1 so monster comes next to you instead of trying to walk ON you
		int distanceX = Math.abs(root.x - here.x);
		int distanceY = Math.abs(root.y- here.y);

		if(distanceX > this.lineOfSight && distanceY > this.lineOfSight) { //Flee if outside line of sight
			int old_fearDistance = this.fearDistance;
			if(distanceX <= 0 && distanceY <=0)
				this.fearDistance = (int)fearMultiplier;
			else {
				if (distanceX >= distanceY) //&& distanceX>rageDistance)
					this.fearDistance = (int) ((distanceX) * this.fearMultiplier);
				else
				if (distanceY>rageDistance)
					this.fearDistance = (int) ((distanceY) * this.fearMultiplier);
			}
			if(old_fearDistance >this.rageDistance)
				this.fearDistance = old_fearDistance;

		}
		else{ //If within line of sight, attack.
			int old_rageDistance = this.rageDistance;
			if(distanceX <= 0 && distanceY <=0)
				this.rageDistance = (int)rageMultiplier;
			else {
				if (distanceX >= distanceY) //&& distanceX>rageDistance)
				this.rageDistance = (int) (distanceX * this.rageMultiplier);
				else
				if (distanceY>rageDistance)
				this.rageDistance = (int) (distanceY * this.rageMultiplier);
			}
			if(old_rageDistance >this.rageDistance)
				this.rageDistance = old_rageDistance;

			this.hasRage = !isFleeing();
		}
	}

	//These two can be used later to force Rage or Fleeing on non-aggressive monsters
	public void forceRageDistance(int distance){
		this.hasRage = true;
		this.rageDistance = distance;
	}
	public  void forceFleeDistance(int distance){
		fearDistance = distance;
	}

	public boolean isEnraged(){
		if(!this.hasRage)
			this.rageDistance = 0;
		if(this.rageDistance <= 0)
			this.hasRage = false;
		if(isFleeing())
			this.hasRage = false;
		return this.hasRage;
	}

	public boolean isCombatant(){
		return isEnraged() || isAgressive();
	}
	public boolean isDesperate(){
		return false; // Don't need this right now, other problems afoot
		//return Boolean.TRUE.equals(this.isDesperate);
	}
}
