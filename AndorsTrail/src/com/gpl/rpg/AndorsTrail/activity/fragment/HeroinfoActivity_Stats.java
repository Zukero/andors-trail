package com.gpl.rpg.AndorsTrail.activity.fragment;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.Dialogs;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.ItemTraits_OnUse;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.view.ItemEffectsView;
import com.gpl.rpg.AndorsTrail.view.RangeBar;
import com.gpl.rpg.AndorsTrail.view.TraitsInfoView;

import java.util.ArrayList;

public final class HeroinfoActivity_Stats extends Fragment {

	private static final int INTENTREQUEST_LEVELUP = 6;

	private WorldContext world;
	private Player player;

	private View view;
	private Button levelUpButton;
	private TextView heroinfo_ap;
	private TextView heroinfo_reequip_cost;
	private TextView heroinfo_useitem_cost;
	private TextView heroinfo_level;
	private TextView heroinfo_totalexperience;
	private TextView basetraitsinfo_max_hp;
	private TextView basetraitsinfo_max_ap;
	private TextView heroinfo_base_reequip_cost;
	private TextView heroinfo_base_useitem_cost;
	private RangeBar rangebar_hp;
	private RangeBar rangebar_exp;
	private ItemEffectsView actorinfo_onhiteffects;
	private TableLayout heroinfo_basestats_table;
	private ViewGroup heroinfo_container;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this.getActivity());
		if (!app.isInitialized()) return;
		this.world = app.getWorld();
		this.player = world.model.player;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.heroinfo_stats, container, false);
		view = v;

		TextView tv = (TextView) v.findViewById(R.id.heroinfo_title);
		tv.setText(player.getName());
		world.tileManager.setImageViewTile(getResources(), tv, player);

		heroinfo_container = (ViewGroup) v.findViewById(R.id.heroinfo_container);
		heroinfo_ap = (TextView) v.findViewById(R.id.heroinfo_ap);
		heroinfo_reequip_cost = (TextView) v.findViewById(R.id.heroinfo_reequip_cost);
		heroinfo_useitem_cost = (TextView) v.findViewById(R.id.heroinfo_useitem_cost);
		basetraitsinfo_max_hp = (TextView) v.findViewById(R.id.basetraitsinfo_max_hp);
		basetraitsinfo_max_ap = (TextView) v.findViewById(R.id.basetraitsinfo_max_ap);
		heroinfo_base_reequip_cost = (TextView) v.findViewById(R.id.heroinfo_base_reequip_cost);
		heroinfo_base_useitem_cost = (TextView) v.findViewById(R.id.heroinfo_base_useitem_cost);
		heroinfo_level = (TextView) v.findViewById(R.id.heroinfo_level);
		heroinfo_totalexperience = (TextView) v.findViewById(R.id.heroinfo_totalexperience);
		actorinfo_onhiteffects = (ItemEffectsView) v.findViewById(R.id.actorinfo_onhiteffects);
		heroinfo_basestats_table = (TableLayout) v.findViewById(R.id.heroinfo_basestats_table);

		rangebar_hp = (RangeBar) v.findViewById(R.id.heroinfo_healthbar);
		rangebar_hp.init(R.drawable.ui_progress_health, R.string.status_hp);
		rangebar_exp = (RangeBar) v.findViewById(R.id.heroinfo_expbar);
		rangebar_exp.init(R.drawable.ui_progress_exp, R.string.status_exp);

		levelUpButton = (Button) v.findViewById(R.id.heroinfo_levelup);
		levelUpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = Dialogs.getIntentForLevelUp(getActivity());
				startActivityForResult(intent, INTENTREQUEST_LEVELUP);
				// We disable the button temporarily, so that there is no possibility
				// of clicking it again before the levelup activity has started.
				// See issue:
				// http://code.google.com/p/andors-trail/issues/detail?id=42
				levelUpButton.setEnabled(false);
			}
		});

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		update();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		update();
	}

	private void update() {
		updateTraits();
		updateLevelup();
	}

	private void updateLevelup() {
		levelUpButton.setEnabled(player.canLevelup());
	}

	private void updateTraits() {
		final Resources res = getResources();

		heroinfo_level.setText(Integer.toString(player.getLevel()));
		heroinfo_totalexperience.setText(Integer.toString(player.getTotalExperience()));
		heroinfo_ap.setText(player.getMaxAP() + "/" + player.getCurrentAP());
		heroinfo_reequip_cost.setText(Integer.toString(player.getReequipCost()));
		heroinfo_useitem_cost.setText(Integer.toString(player.getUseItemCost()));
		basetraitsinfo_max_hp.setText(Integer.toString(player.baseTraits.maxHP));
		basetraitsinfo_max_ap.setText(Integer.toString(player.baseTraits.maxAP));
		heroinfo_base_reequip_cost.setText(Integer.toString(player.baseTraits.reequipCost));
		heroinfo_base_useitem_cost.setText(Integer.toString(player.baseTraits.useItemCost));
		rangebar_hp.update(player.getMaxHP(), player.getCurrentHP());
		rangebar_exp.update(player.getMaxLevelExperience(), player.getCurrentLevelExperience());

		TraitsInfoView.update(heroinfo_container, player);
		TraitsInfoView.updateTraitsTable(
			heroinfo_basestats_table
			, player.baseTraits.moveCost
			, player.baseTraits.attackCost
			, player.baseTraits.attackChance
			, player.baseTraits.damagePotential
			, player.baseTraits.criticalSkill
			, player.baseTraits.criticalMultiplier
			, player.baseTraits.blockChance
			, player.baseTraits.damageResistance
			, player.baseTraits.increaseMaxRange
			, false
		);

		ArrayList<ItemTraits_OnUse> effects_hit = new ArrayList<ItemTraits_OnUse>();
		ArrayList<ItemTraits_OnUse> effects_kill = new ArrayList<ItemTraits_OnUse>();
		for (Inventory.WearSlot slot : Inventory.WearSlot.values()) {
			ItemType type = player.inventory.getItemTypeInWearSlot(slot);
			if (type == null) continue;
			if (type.effects_hit != null) effects_hit.add(type.effects_hit);
			if (type.effects_kill != null) effects_kill.add(type.effects_kill);
		}
		if (effects_hit.isEmpty()) effects_hit = null;
		if (effects_kill.isEmpty()) effects_kill = null;
		actorinfo_onhiteffects.update(null, null, effects_hit, effects_kill, false);


		updateStatsTableRow(world.model.statistics.getNumberOfCompletedQuests(world), R.id.heroinfo_gamestats_quests, R.id.heroinfo_gamestats_quests_row);
		updateStatsTableRow(world.model.statistics.getNumberOfVisitedMaps(world), R.id.heroinfo_gamestats_visited_maps, R.id.heroinfo_gamestats_visited_maps_row);
		updateStatsTableRow(world.model.statistics.getDeaths(), R.id.heroinfo_gamestats_deaths, R.id.heroinfo_gamestats_deaths_row);
		updateStatsTableRow(world.model.statistics.getSpentGold(), R.id.heroinfo_gamestats_spent_gold, R.id.heroinfo_gamestats_spent_gold_row);
		updateStatsTableRow(world.model.statistics.getNumberOfUsedItems(), R.id.heroinfo_gamestats_num_used_items, R.id.heroinfo_gamestats_num_used_items_row);
		updateStatsTableRow(world.model.statistics.getNumberOfUsedBonemealPotions(), R.id.heroinfo_gamestats_bonemeals, R.id.heroinfo_gamestats_bonemeals_row);
		updateStatsTableRow(world.model.statistics.getNumberOfKilledMonsters(), R.id.heroinfo_gamestats_num_killed_monsters, R.id.heroinfo_gamestats_num_killed_monsters_row);
		updateStatsTableRow(world.model.statistics.getMostCommonlyUsedItem(world, res), R.id.heroinfo_gamestats_fav_item, R.id.heroinfo_gamestats_fav_item_row1, R.id.heroinfo_gamestats_fav_item_row2);
		updateStatsTableRow(world.model.statistics.getMostPowerfulKilledMonster(world), R.id.heroinfo_gamestats_top_boss, R.id.heroinfo_gamestats_top_boss_row1, R.id.heroinfo_gamestats_top_boss_row2);
		updateStatsTableRow(world.model.statistics.getTop5MostCommonlyKilledMonsters(world, res), R.id.heroinfo_gamestats_fav_monsters, R.id.heroinfo_gamestats_fav_monsters_row1, R.id.heroinfo_gamestats_fav_monsters_row2);
	}

	private void updateStatsTableRow(int value, int textView, int tableRow) {
		String s = (value > 0) ? Integer.toString(value) : null;
		updateStatsTableRow(s, textView, tableRow, 0);
	}

	private void updateStatsTableRow(String value, int textView, int tableRow1, int tableRow2) {
		TextView tv = (TextView) view.findViewById(textView);
		TableRow tr1 = (TableRow) view.findViewById(tableRow1);
		TableRow tr2 = null;
		if (tableRow2 != 0) tr2 = (TableRow) view.findViewById(tableRow2);
		if (value != null) {
			tv.setText(value);
			tr1.setVisibility(View.VISIBLE);
			if (tr2 != null) tr2.setVisibility(View.VISIBLE);
		} else {
			tr1.setVisibility(View.GONE);
			if (tr2 != null) tr2.setVisibility(View.GONE);
		}
	}
}
