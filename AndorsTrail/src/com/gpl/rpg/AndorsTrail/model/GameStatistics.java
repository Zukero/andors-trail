package com.gpl.rpg.AndorsTrail.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.content.res.Resources;

import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.actor.MonsterType;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.model.quest.Quest;

public final class GameStatistics {
	private int deaths = 0;
	private final HashMap<String, Integer> killedMonsters = new HashMap<String, Integer>();
	private final HashMap<String, Integer> usedItems = new HashMap<String, Integer>();
	private int spentGold = 0;
	private boolean unlimitedSaves = true;
	private int startLives = -1; // -1 --> unlimited

	public GameStatistics(boolean unlimitedSaves, int startLives) {
		this.unlimitedSaves = unlimitedSaves;
		this.startLives = startLives;
	}

	public void addMonsterKill(String monsterTypeID) {
		if (!killedMonsters.containsKey(monsterTypeID)) killedMonsters.put(monsterTypeID, 1);
		else killedMonsters.put(monsterTypeID, killedMonsters.get(monsterTypeID) + 1);
	}
	public void addPlayerDeath(int lostExp) {
		++deaths;
	}
	public void addGoldSpent(int amount) {
		spentGold += amount;
	}
	public void addItemUsage(ItemType type) {
		final String n = type.id;
		if (!usedItems.containsKey(n)) usedItems.put(n, 1);
		else usedItems.put(n, usedItems.get(n) + 1);
	}

	public int getDeaths() {
		return deaths;
	}

	public int getSpentGold() {
		return spentGold;
	}

	public boolean hasUnlimitedSaves() { return unlimitedSaves; }

	public boolean hasUnlimitedLives() { return startLives == -1; }

	public int getStartLives() { return startLives; }

	public int getLivesLeft() { return hasUnlimitedLives() ? -1 : startLives - deaths; }

	public boolean isDead() { return !hasUnlimitedLives() && getLivesLeft() < 1; }

	public int getNumberOfKillsForMonsterType(String monsterTypeID) {
		Integer v = killedMonsters.get(monsterTypeID);
		if (v == null) return 0;
		return v;
	}

	public String getTop5MostCommonlyKilledMonsters(WorldContext world, Resources res) {
		if (killedMonsters.isEmpty()) return null;
		List<Entry<String, Integer>> entries = new ArrayList<Entry<String, Integer>>(killedMonsters.entrySet());
		Collections.sort(entries, descendingValueComparator);
		StringBuilder sb = new StringBuilder(100);
		int i = 0;
		for (Entry<String, Integer> e : entries) {
			if (i++ >= 5) break;
			MonsterType t = world.monsterTypes.getMonsterType(e.getKey());
			if (t == null) continue;
			sb.append(res.getString(R.string.heroinfo_gamestats_name_and_qty, t.name, e.getValue())).append('\n');
		}
		return sb.toString();
	}

	public String getMostPowerfulKilledMonster(WorldContext world) {
		if (killedMonsters.isEmpty()) return null;
		HashMap<String, Integer> expPerMonsterType = new HashMap<String, Integer>(killedMonsters.size());
		for (String monsterTypeID : killedMonsters.keySet()) {
			MonsterType t = world.monsterTypes.getMonsterType(monsterTypeID);
			expPerMonsterType.put(monsterTypeID, t != null ? t.exp : 0);
		}
		String monsterTypeID = Collections.min(expPerMonsterType.entrySet(), descendingValueComparator).getKey();
		MonsterType t = world.monsterTypes.getMonsterType(monsterTypeID);
		return t != null ? t.name : null;
	}

	public String getMostCommonlyUsedItem(WorldContext world, Resources res) {
		if (usedItems.isEmpty()) return null;
		Entry<String, Integer> e = Collections.min(usedItems.entrySet(), descendingValueComparator);
		String itemTypeID = e.getKey();
		ItemType t = world.itemTypes.getItemType(itemTypeID);
		if (t == null) return null;
		return res.getString(R.string.heroinfo_gamestats_name_and_qty, t.getName(world.model.player), e.getValue());
	}

	public int getNumberOfUsedBonemealPotions() {
		int result = 0;
		Integer v;
		if ((v = usedItems.get("bonemeal_potion")) != null) result += v;
		if ((v = usedItems.get("pot_bm_lodar")) != null) result += v;
		return result;
	}

	public int getNumberOfCompletedQuests(WorldContext world) {
		int result = 0;
		for (Quest q : world.quests.getAllQuests()) {
			if (!q.showInLog) continue;
			if (q.isCompleted(world.model.player)) ++result;
		}
		return result;
	}

	public int getNumberOfVisitedMaps(WorldContext world) {
		int result = 0;
		for (PredefinedMap m : world.maps.getAllMaps()) {
			if (m.visited) ++result;
		}
		return result;
	}

	public int getNumberOfUsedItems() {
		int result = 0;
		for (int v : usedItems.values()) result += v;
		return result;
	}

	public int getNumberOfTimesItemHasBeenUsed(String itemId) {
		if (!usedItems.containsKey(itemId)) return 0;
		return usedItems.get(itemId);
	}

	public int getNumberOfKilledMonsters() {
		int result = 0;
		for (int v : killedMonsters.values()) result += v;
		return result;
	}

	private static final Comparator<Entry<String, Integer>> descendingValueComparator = new Comparator<Entry<String, Integer>>() {
		@Override
		public int compare(Entry<String, Integer> a, Entry<String, Integer> b) {
			return b.getValue().compareTo(a.getValue());
		}
	};


	// ====== PARCELABLE ===================================================================

	public GameStatistics(DataInputStream src, WorldContext world, int fileversion) throws IOException {
		this.deaths = src.readInt();
		final int numMonsters = src.readInt();
		for(int i = 0; i < numMonsters; ++i) {
			String id = src.readUTF();
			final int value = src.readInt();
			if(fileversion <= 23) {
				MonsterType type = world.monsterTypes.guessMonsterTypeFromName(id);
				if (type == null) continue;
				id = type.id;
			}
			this.killedMonsters.put(id, value);
		}
		if (fileversion <= 17) return;

		final int numItems = src.readInt();
		for(int i = 0; i < numItems; ++i) {
			final String name = src.readUTF();
			final int value = src.readInt();
			this.usedItems.put(name, value);
		}
		this.spentGold = src.readInt();

		if (fileversion < 48) return;

		this.startLives = src.readInt();
		this.unlimitedSaves = src.readBoolean();
	}

	public void writeToParcel(DataOutputStream dest) throws IOException {
		dest.writeInt(deaths);
		Set<Entry<String, Integer> > set = killedMonsters.entrySet();
		dest.writeInt(set.size());
		for (Entry<String, Integer> e : set) {
			dest.writeUTF(e.getKey());
			dest.writeInt(e.getValue());
		}
		set = usedItems.entrySet();
		dest.writeInt(set.size());
		for (Entry<String, Integer> e : set) {
			dest.writeUTF(e.getKey());
			dest.writeInt(e.getValue());
		}
		dest.writeInt(spentGold);
		dest.writeInt(startLives);
		dest.writeBoolean(unlimitedSaves);
	}
}
