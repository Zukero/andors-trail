package com.gpl.rpg.AndorsTrail.controller.listeners;

import com.gpl.rpg.AndorsTrail.controller.AttackResult;
import com.gpl.rpg.AndorsTrail.model.actor.Actor;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;

public interface CombatActionListener {
	void onPlayerAttackMissed(Monster target, AttackResult attackResult);
	void onPlayerAttackSuccess(Monster target, AttackResult attackResult);
	void onMonsterAttackMissed(Monster attacker, AttackResult attackResult);
	void onMonsterAttackSuccess(Monster attacker, AttackResult attackResult);
	void onMonsterMovedDuringCombat(Monster m);
	void onPlayerKilledMonster(Monster target);
	void onCombatTargetOutsideRange();
	void onPlayerStartedFleeing();
	void onPlayerFailedFleeing();
	void onPlayerDoesNotHaveEnoughAP();
}
