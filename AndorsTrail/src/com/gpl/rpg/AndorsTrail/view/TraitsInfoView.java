package com.gpl.rpg.AndorsTrail.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.model.actor.Actor;
import com.gpl.rpg.AndorsTrail.util.Range;

public final class TraitsInfoView {

	public static void update(ViewGroup group, Actor actor) {
		TableLayout actorinfo_stats_table = (TableLayout) group.findViewById(R.id.actorinfo_stats_table);

		updateTraitsTable(
			actorinfo_stats_table
			,actor.getMoveCost()
			,actor.getAttackCost()
			,actor.getAttackChance()
			,actor.getDamagePotential()
			,actor.getCriticalSkill()
			,actor.getCriticalMultiplier()
			,actor.getBlockChance()
			,actor.getDamageResistance()
			,actor.increaseMaxRange
			,actor.isImmuneToCriticalHits());

		TextView actorinfo_currentconditions_title = (TextView) group.findViewById(R.id.actorinfo_currentconditions_title);
		ActorConditionList actorinfo_currentconditions = (ActorConditionList) group.findViewById(R.id.actorinfo_currentconditions);
		if (actor.conditions.isEmpty()) {
			actorinfo_currentconditions_title.setVisibility(View.GONE);
			actorinfo_currentconditions.setVisibility(View.GONE);
		} else {
			actorinfo_currentconditions_title.setVisibility(View.VISIBLE);
			actorinfo_currentconditions.setVisibility(View.VISIBLE);
			actorinfo_currentconditions.update(actor.conditions);
		}
	}

	public static void updateTraitsTable(
			ViewGroup group
			,int moveCost
			,int attackCost
			,int attackChance
			,Range damagePotential
			,int criticalSkill
			,float criticalMultiplier
			,int blockChance
			,int damageResistance
			,int increasedMaxRange
			,boolean isImmuneToCriticalHits

		) {
		TableRow row;
		TextView tv;

		tv = (TextView) group.findViewById(R.id.traitsinfo_move_cost);
		tv.setText(Integer.toString(moveCost));

		tv = (TextView) group.findViewById(R.id.traitsinfo_attack_cost);
		tv.setText(Integer.toString(attackCost));

		row = (TableRow) group.findViewById(R.id.traitsinfo_attack_chance_row);
		if (attackChance == 0) {
			row.setVisibility(View.GONE);
		} else {
			row.setVisibility(View.VISIBLE);
			tv = (TextView) group.findViewById(R.id.traitsinfo_attack_chance);
			tv.setText(Integer.toString(attackChance));
		}

		row = (TableRow) group.findViewById(R.id.traitsinfo_attack_damage_row);
		if (damagePotential != null && damagePotential.max != 0) {
			row.setVisibility(View.VISIBLE);
			tv = (TextView) group.findViewById(R.id.traitsinfo_attack_damage);
			tv.setText(damagePotential.toMinMaxString());
		} else {
			row.setVisibility(View.GONE);
		}

		row = (TableRow) group.findViewById(R.id.traitsinfo_criticalhit_skill_row);
		if (criticalSkill == 0) {
			row.setVisibility(View.GONE);
		} else {
			row.setVisibility(View.VISIBLE);
			tv = (TextView) group.findViewById(R.id.traitsinfo_criticalhit_skill);
			tv.setText(Integer.toString(criticalSkill));
		}

		row = (TableRow) group.findViewById(R.id.traitsinfo_criticalhit_multiplier_row);
		if (criticalMultiplier != 0 && criticalMultiplier != 1) {
			row.setVisibility(View.VISIBLE);
			tv = (TextView) group.findViewById(R.id.traitsinfo_criticalhit_multiplier);
			tv.setText(Float.toString(criticalMultiplier));
		} else {
			row.setVisibility(View.GONE);
		}

		row = (TableRow) group.findViewById(R.id.traitsinfo_criticalhit_effectivechance_row);
		if (criticalSkill != 0 && criticalMultiplier != 0 && criticalMultiplier != 1) {
			row.setVisibility(View.VISIBLE);
			tv = (TextView) group.findViewById(R.id.traitsinfo_criticalhit_effectivechance);
			tv.setText(Integer.toString(Actor.getEffectiveCriticalChance(criticalSkill)) + '%');
		} else {
			row.setVisibility(View.GONE);
		}

		row = (TableRow) group.findViewById(R.id.traitsinfo_block_chance_row);
		if (blockChance == 0) {
			row.setVisibility(View.GONE);
		} else {
			row.setVisibility(View.VISIBLE);
			tv = (TextView) group.findViewById(R.id.traitsinfo_block_chance);
			tv.setText(Integer.toString(blockChance));
		}

		row = (TableRow) group.findViewById(R.id.traitsinfo_max_range_row);
		/*if (increasedMaxRange == 1) {
			row.setVisibility(View.GONE);
		} else {*/
			row.setVisibility(View.VISIBLE);
			tv = (TextView) group.findViewById(R.id.traitsinfo_max_range);
			tv.setText(Integer.toString(increasedMaxRange));
		//}

		row = (TableRow) group.findViewById(R.id.traitsinfo_damageresist_row);
		if (damageResistance == 0) {
			row.setVisibility(View.GONE);
		} else {
			row.setVisibility(View.VISIBLE);
			tv = (TextView) group.findViewById(R.id.traitsinfo_damageresist);
			tv.setText(Integer.toString(damageResistance));
		}

		row = (TableRow) group.findViewById(R.id.traitsinfo_is_immune_to_critical_hits_row);
		row.setVisibility(isImmuneToCriticalHits ? View.VISIBLE : View.GONE);
	}
}
