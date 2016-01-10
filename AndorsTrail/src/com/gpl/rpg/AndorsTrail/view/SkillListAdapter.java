package com.gpl.rpg.AndorsTrail.view;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.activity.SkillInfoActivity;
import com.gpl.rpg.AndorsTrail.controller.SkillController;
import com.gpl.rpg.AndorsTrail.model.ability.SkillCollection;
import com.gpl.rpg.AndorsTrail.model.ability.SkillInfo;
import com.gpl.rpg.AndorsTrail.model.actor.Player;

import java.util.*;

public final class SkillListAdapter extends ArrayAdapter<SkillInfo> {
	private final Resources r;
	private final Player player;


	public SkillListAdapter(Context context, Collection<SkillInfo> skills, Player player) {
		super(context, 0, filterNondisplayedSkills(skills, player));
		this.r = context.getResources();
		this.player = player;
	}

	public SkillListAdapter(Context context, Collection<SkillInfo> skills, Player player, int category) {
		super(context, 0, filterNondisplayedSkills(skills, player, category-1));
		this.r = context.getResources();
		this.player = player;
	}

	private static List<SkillInfo> filterNondisplayedSkills(Collection<SkillInfo> skills, Player player) {
		final ArrayList<SkillInfo> result = new ArrayList<SkillInfo>();
		for (SkillInfo skill : skills) {
			if (shouldDisplaySkill(skill, player)) result.add(skill);
		}
		Collections.sort(result, new Comparator<SkillInfo>() {
			@Override
			public int compare(SkillInfo a, SkillInfo b) {
				return a.id.ordinal() - b.id.ordinal();
			}
		});
		return result;
	}

	private static List<SkillInfo> filterNondisplayedSkills(Collection<SkillInfo> skills, Player player, int category) {
		final ArrayList<SkillInfo> result = new ArrayList<SkillInfo>();
		for (SkillInfo skill : skills) {
			if (shouldDisplaySkill(skill, player)){
				if(category <0)
					result.add(skill);
				else if(skill.categoryType.equals(SkillCollection.SkillCategory.values()[category+1]))
					result.add(skill);
			}
		}
		Collections.sort(result, new Comparator<SkillInfo>() {
			@Override
			public int compare(SkillInfo a, SkillInfo b) {
				return a.id.ordinal() - b.id.ordinal();
			}
		});
		return result;
	}

	private static boolean shouldDisplaySkill(SkillInfo skill, Player player) {
		if (player.hasSkill(skill.id)) return true;
		if (skill.levelupVisibility == SkillInfo.LevelUpType.alwaysShown) return true;
		return false;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final SkillInfo skill = getItem(position);
		final SkillCollection.SkillID skillID = skill.id;

		View result = convertView;
		if (result == null) {
			result = View.inflate(getContext(), R.layout.skill_listentry_view, null);
		}

		final ImageView icon = (ImageView) result.findViewById(R.id.skillentry_icon);
		final TextView title = (TextView) result.findViewById(R.id.skillentry_title);
		final TextView description = (TextView) result.findViewById(R.id.skillentry_description);

		String skillTitle = r.getString(SkillInfoActivity.getSkillTitleResourceID(skillID));
		final int skillLevel = player.getSkillLevel(skillID);
		if (skillLevel > 0) {
			skillTitle += " (" + skillLevel + ')';
		}
		title.setText(skillTitle);
		description.setText(getSkillShortDescriptionResourceID(skillID));

		boolean enabled;
		if (player.hasAvailableSkillpoints()) {
			enabled = SkillController.canLevelupSkillManually(player, skill);
		} else {
			enabled = player.hasSkill(skillID);
		}
		icon.setEnabled(enabled);
		title.setEnabled(enabled);
		description.setEnabled(enabled);

		return result;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).id.ordinal();
	}

	private static int getSkillShortDescriptionResourceID(SkillCollection.SkillID skill) {
		switch (skill) {
		case weaponChance: return R.string.skill_shortdescription_weapon_chance;
		case weaponDmg: return R.string.skill_shortdescription_weapon_dmg;
		case barter: return R.string.skill_shortdescription_barter;
		case dodge: return R.string.skill_shortdescription_dodge;
		case barkSkin: return R.string.skill_shortdescription_barkskin;
		case moreCriticals: return R.string.skill_shortdescription_more_criticals;
		case betterCriticals: return R.string.skill_shortdescription_better_criticals;
		case speed: return R.string.skill_shortdescription_speed;
		case coinfinder: return R.string.skill_shortdescription_coinfinder;
		case moreExp: return R.string.skill_shortdescription_more_exp;
		case cleave: return R.string.skill_shortdescription_cleave;
		case eater: return R.string.skill_shortdescription_eater;
		case fortitude: return R.string.skill_shortdescription_fortitude;
		case evasion: return R.string.skill_shortdescription_evasion;
		case regeneration: return R.string.skill_shortdescription_regeneration;
		case lowerExploss: return R.string.skill_shortdescription_lower_exploss;
		case magicfinder: return R.string.skill_shortdescription_magicfinder;
		case resistanceMental: return R.string.skill_shortdescription_resistance_mental;
		case resistancePhysical: return R.string.skill_shortdescription_resistance_physical_capacity;
		case resistanceBlood: return R.string.skill_shortdescription_resistance_blood_disorder;
		case shadowBless: return R.string.skill_shortdescription_shadow_bless;
		case crit1: return R.string.skill_shortdescription_crit1;
		case crit2: return R.string.skill_shortdescription_crit2;
		case rejuvenation: return R.string.skill_shortdescription_rejuvenation;
		case taunt: return R.string.skill_shortdescription_taunt;
		case concussion: return R.string.skill_shortdescription_concussion;
		case weaponProficiencyDagger: return R.string.skill_shortdescription_weapon_prof_dagger;
		case weaponProficiency1hsword: return R.string.skill_shortdescription_weapon_prof_1hsword;
		case weaponProficiency2hsword: return R.string.skill_shortdescription_weapon_prof_2hsword;
		case weaponProficiencyAxe: return R.string.skill_shortdescription_weapon_prof_axe;
		case weaponProficiencyBlunt: return R.string.skill_shortdescription_weapon_prof_blunt;
		case weaponProficiencyUnarmed: return R.string.skill_shortdescription_weapon_prof_unarmed;
		case armorProficiencyShield: return R.string.skill_shortdescription_armor_prof_shield;
		case armorProficiencyUnarmored: return R.string.skill_shortdescription_armor_prof_unarmored;
		case armorProficiencyLight: return R.string.skill_shortdescription_armor_prof_light;
		case armorProficiencyHeavy: return R.string.skill_shortdescription_armor_prof_heavy;
		case fightstyleDualWield: return R.string.skill_shortdescription_fightstyle_dualwield;
		case fightstyle2hand: return R.string.skill_shortdescription_fightstyle_2hand;
		case fightstyleWeaponShield: return R.string.skill_shortdescription_fightstyle_weapon_shield;
		case specializationDualWield: return R.string.skill_shortdescription_specialization_dualwield;
		case specialization2hand: return R.string.skill_shortdescription_specialization_2hand;
		case specializationWeaponShield: return R.string.skill_shortdescription_specialization_weapon_shield;
		default:
			return -1;
		}
	}

	public void sortByName(){
		Comparator<SkillInfo> comparatorName = new Comparator<SkillInfo>() {
			@Override
			public int compare(SkillInfo item1, SkillInfo item2) {
				return  r.getString( SkillInfoActivity.getSkillTitleResourceID(item1.id)).compareTo(
						r.getString( SkillInfoActivity.getSkillTitleResourceID(item2.id)));
			}
		};
		this.sort(comparatorName);
	}

	public void sortByPoints(){
		Comparator<SkillInfo> comparatorPoints = new Comparator<SkillInfo>() {
			@Override
			public int compare(SkillInfo item1, SkillInfo item2) {
				if(player.getSkillLevel(item1.id) > player.getSkillLevel(item2.id))
					return -1;
				else if(player.getSkillLevel(item1.id) < player.getSkillLevel(item2.id))
					return 1;
				else
					return  r.getString( SkillInfoActivity.getSkillTitleResourceID(item1.id)).compareTo(
							r.getString( SkillInfoActivity.getSkillTitleResourceID(item2.id)));
			}
		};
		this.sort(comparatorPoints);
	}

	public void sortByUnlocked(){
		Comparator<SkillInfo> comparatorUnlocked = new Comparator<SkillInfo>() {
			@Override
			public int compare(SkillInfo item1, SkillInfo item2) {
				if(item1.canLevelUpSkillTo(player, player.getSkillLevel(item1.id) + 1)
				&& !(item2.canLevelUpSkillTo(player, player.getSkillLevel(item2.id) +1)))
					return -1;
				else if(!(item1.canLevelUpSkillTo(player, player.getSkillLevel(item1.id) +1))
					&& item2.canLevelUpSkillTo(player, player.getSkillLevel(item2.id) +1))
					return 1;
				else
					return  r.getString(SkillInfoActivity.getSkillTitleResourceID(item1.id)).compareTo(
							r.getString(SkillInfoActivity.getSkillTitleResourceID(item2.id)));
			}
		};
		this.sort(comparatorUnlocked);
	}
}
