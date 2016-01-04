package com.gpl.rpg.AndorsTrail.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.model.ability.traits.AbilityModifierTraits;

import org.w3c.dom.Text;

public final class AbilityModifierInfoView extends LinearLayout {
	private final TextView abilitymodifierinfo_change_maxap;
	private final TextView abilitymodifierinfo_change_maxhp;
	private final TextView abilitymodifierinfo_change_movecost;
	private final TextView abilitymodifierinfo_change_use_cost;
	private final TextView abilitymodifierinfo_change_reequip_cost;
	private final TextView abilitymodifierinfo_change_attack_cost;
	private final TextView abilitymodifierinfo_change_attack_chance;
	private final TextView abilitymodifierinfo_change_attack_damage;
	private final TextView abilitymodifierinfo_change_critical_skill;
	private final TextView abilitymodifierinfo_change_critical_multiplier;
	private final TextView abilitymodifierinfo_change_block_chance;
	private final TextView abilitymodifierinfo_change_damage_resistance;
	private final TextView abilitymodifierinfo_change_attack_range;

	public AbilityModifierInfoView(Context context, AttributeSet attr) {
		super(context, attr);
		setFocusable(false);
		setOrientation(LinearLayout.VERTICAL);
		inflate(context, R.layout.abilitymodifierview, this);

		abilitymodifierinfo_change_maxap = (TextView) findViewById(R.id.abilitymodifierinfo_change_maxap);
		abilitymodifierinfo_change_maxhp = (TextView) findViewById(R.id.abilitymodifierinfo_change_maxhp);
		abilitymodifierinfo_change_movecost = (TextView) findViewById(R.id.abilitymodifierinfo_change_movecost);
		abilitymodifierinfo_change_use_cost = (TextView) findViewById(R.id.abilitymodifierinfo_change_use_cost);
		abilitymodifierinfo_change_reequip_cost = (TextView) findViewById(R.id.abilitymodifierinfo_change_reequip_cost);
		abilitymodifierinfo_change_attack_cost = (TextView) findViewById(R.id.abilitymodifierinfo_change_attack_cost);
		abilitymodifierinfo_change_attack_chance = (TextView) findViewById(R.id.abilitymodifierinfo_change_attack_chance);
		abilitymodifierinfo_change_attack_damage = (TextView) findViewById(R.id.abilitymodifierinfo_change_attack_damage);
		abilitymodifierinfo_change_critical_skill = (TextView) findViewById(R.id.abilitymodifierinfo_change_critical_skill);
		abilitymodifierinfo_change_critical_multiplier = (TextView) findViewById(R.id.abilitymodifierinfo_change_critical_multiplier);
		abilitymodifierinfo_change_block_chance = (TextView) findViewById(R.id.abilitymodifierinfo_change_block_chance);
		abilitymodifierinfo_change_damage_resistance = (TextView) findViewById(R.id.abilitymodifierinfo_change_damage_resistance);
		abilitymodifierinfo_change_attack_range = (TextView) findViewById(R.id.abilitymodifierinfo_change_attack_range);
	}

	public void update(AbilityModifierTraits traits, boolean isWeapon) {
		for(int i = 0; i < getChildCount(); ++i) {
			getChildAt(i).setVisibility(View.GONE);
		}
		if (traits == null) return;

		final Resources res = getResources();

		displayIfNonZero(traits.increaseMaxRange, abilitymodifierinfo_change_attack_range, R.string.iteminfo_effect_increase_max_range, R.string.iteminfo_effect_decrease_max_range);
		displayIfNonZero(traits.increaseMaxHP, abilitymodifierinfo_change_maxhp, R.string.iteminfo_effect_increase_max_hp, R.string.iteminfo_effect_decrease_max_hp);
		displayIfNonZero(traits.increaseMaxAP, abilitymodifierinfo_change_maxap, R.string.iteminfo_effect_increase_max_ap, R.string.iteminfo_effect_decrease_max_ap);
		displayIfNonZero(traits.increaseMoveCost, abilitymodifierinfo_change_movecost, R.string.iteminfo_effect_increase_movecost, R.string.iteminfo_effect_decrease_movecost);
		displayIfNonZero(traits.increaseUseItemCost, abilitymodifierinfo_change_use_cost, R.string.iteminfo_effect_increase_use_cost, R.string.iteminfo_effect_decrease_use_cost);
		displayIfNonZero(traits.increaseReequipCost, abilitymodifierinfo_change_reequip_cost, R.string.iteminfo_effect_increase_reequip_cost, R.string.iteminfo_effect_decrease_reequip_cost);
		displayIfNonZero(traits.increaseCriticalSkill, abilitymodifierinfo_change_critical_skill, R.string.iteminfo_effect_increase_critical_skill, R.string.iteminfo_effect_decrease_critical_skill);
		displayIfNonZero(traits.increaseBlockChance, abilitymodifierinfo_change_block_chance, R.string.iteminfo_effect_increase_block_chance, R.string.iteminfo_effect_decrease_block_chance);
		displayIfNonZero(traits.increaseDamageResistance, abilitymodifierinfo_change_damage_resistance, R.string.iteminfo_effect_increase_damage_resistance, R.string.iteminfo_effect_decrease_damage_resistance);

		if (isWeapon) {
			abilitymodifierinfo_change_attack_cost.setText(res.getString(R.string.iteminfo_effect_weapon_attack_cost, traits.increaseAttackCost));
			abilitymodifierinfo_change_attack_cost.setVisibility(View.VISIBLE);
			displayIfNonZero(traits.increaseAttackChance, abilitymodifierinfo_change_attack_chance, R.string.iteminfo_effect_weapon_attack_chance, R.string.iteminfo_effect_decrease_attack_chance);

			if (traits.setCriticalMultiplier != 0) {
				abilitymodifierinfo_change_critical_multiplier.setText(res.getString(R.string.iteminfo_effect_critical_multiplier, Math.abs(traits.setCriticalMultiplier)));
				abilitymodifierinfo_change_critical_multiplier.setVisibility(View.VISIBLE);
			}
		} else {
			displayIfNonZero(traits.increaseAttackCost, abilitymodifierinfo_change_attack_cost, R.string.iteminfo_effect_increase_attack_cost, R.string.iteminfo_effect_decrease_attack_cost);
			displayIfNonZero(traits.increaseAttackChance, abilitymodifierinfo_change_attack_chance, R.string.iteminfo_effect_increase_attack_chance, R.string.iteminfo_effect_decrease_attack_chance);
		}

		if (traits.increaseMinDamage != 0 || traits.increaseMaxDamage != 0) {
			if (traits.increaseMinDamage == traits.increaseMaxDamage) {
				int label = R.string.iteminfo_effect_increase_attack_damage;
				if (traits.increaseMinDamage < 0) label = R.string.iteminfo_effect_decrease_attack_damage;
				else if (isWeapon) label = R.string.iteminfo_effect_weapon_attack_damage;
				abilitymodifierinfo_change_attack_damage.setText(res.getString(label, Math.abs(traits.increaseMinDamage)));
			} else {
				int label = R.string.iteminfo_effect_increase_attack_damage_minmax;
				if (traits.increaseMinDamage < 0) label = R.string.iteminfo_effect_decrease_attack_damage_minmax;
				else if (isWeapon) label = R.string.iteminfo_effect_weapon_attack_damage_minmax;
				abilitymodifierinfo_change_attack_damage.setText(res.getString(label, Math.abs(traits.increaseMinDamage), Math.abs(traits.increaseMaxDamage)));
			}
			abilitymodifierinfo_change_attack_damage.setVisibility(View.VISIBLE);
		}
	}

	private void displayIfNonZero(int statChange, TextView textView, int stringresource_increase, int stringresource_decrease) {
		if (statChange == 0) return;

		final int label = statChange > 0 ? stringresource_increase : stringresource_decrease;
		textView.setText(getResources().getString(label, Math.abs(statChange)));
		textView.setVisibility(View.VISIBLE);
	}
}
