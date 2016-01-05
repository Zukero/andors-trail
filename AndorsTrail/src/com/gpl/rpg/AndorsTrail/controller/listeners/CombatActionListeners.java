package com.gpl.rpg.AndorsTrail.controller.listeners;

import com.gpl.rpg.AndorsTrail.controller.AttackResult;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.util.ListOfListeners;

public final class CombatActionListeners extends ListOfListeners<CombatActionListener> implements CombatActionListener {

	private final Function2<CombatActionListener, Monster, AttackResult> onPlayerAttackMissed = new Function2<CombatActionListener, Monster, AttackResult>() {
		@Override public void call(CombatActionListener listener, Monster target, AttackResult attackResult) { listener.onPlayerAttackMissed(target, attackResult); }
	};

	private final Function2<CombatActionListener, Monster, AttackResult> onPlayerAttackSuccess = new Function2<CombatActionListener, Monster, AttackResult>() {
		@Override public void call(CombatActionListener listener, Monster target, AttackResult attackResult) { listener.onPlayerAttackSuccess(target, attackResult); }
	};

	private final Function2<CombatActionListener, Monster, AttackResult> onMonsterAttackMissed = new Function2<CombatActionListener, Monster, AttackResult>() {
		@Override public void call(CombatActionListener listener, Monster attacker, AttackResult attackResult) { listener.onMonsterAttackMissed(attacker, attackResult); }
	};

	private final Function2<CombatActionListener, Monster, AttackResult> onMonsterAttackSuccess = new Function2<CombatActionListener, Monster, AttackResult>() {
		@Override public void call(CombatActionListener listener, Monster attacker, AttackResult attackResult) { listener.onMonsterAttackSuccess(attacker, attackResult); }
	};

	private final Function1<CombatActionListener, Monster> onMonsterMovedDuringCombat = new Function1<CombatActionListener, Monster>() {
		@Override public void call(CombatActionListener listener, Monster m) { listener.onMonsterMovedDuringCombat(m); }
	};

	private final Function1<CombatActionListener, Monster> onPlayerKilledMonster = new Function1<CombatActionListener, Monster>() {
		@Override public void call(CombatActionListener listener, Monster target) { listener.onPlayerKilledMonster(target); }
	};

	private final Function<CombatActionListener> onPlayerStartedFleeing = new Function<CombatActionListener>() {
		@Override public void call(CombatActionListener listener) { listener.onPlayerStartedFleeing(); }
	};

	private final Function<CombatActionListener> onPlayerFailedFleeing = new Function<CombatActionListener>() {
		@Override public void call(CombatActionListener listener) { listener.onPlayerFailedFleeing(); }
	};

	private final Function<CombatActionListener> onPlayerDoesNotHaveEnoughAP = new Function<CombatActionListener>() {
		@Override public void call(CombatActionListener listener) { listener.onPlayerDoesNotHaveEnoughAP(); }
	};

	private final Function<CombatActionListener> onCombatTargetOutsideRange = new Function<CombatActionListener>() {
		@Override public void call(CombatActionListener listener) { listener.onCombatTargetOutsideRange(); }
	};

	@Override
	public void onPlayerAttackMissed(Monster target, AttackResult attackResult) {
		callAllListeners(this.onPlayerAttackMissed, target, attackResult);
	}

	@Override
	public void onPlayerAttackSuccess(Monster target, AttackResult attackResult) {
		callAllListeners(this.onPlayerAttackSuccess, target, attackResult);
	}

	@Override
	public void onMonsterAttackMissed(Monster attacker, AttackResult attackResult) {
		callAllListeners(this.onMonsterAttackMissed, attacker, attackResult);
	}

	@Override
	public void onMonsterAttackSuccess(Monster attacker, AttackResult attackResult) {
		callAllListeners(this.onMonsterAttackSuccess, attacker, attackResult);
	}

	@Override
	public void onMonsterMovedDuringCombat(Monster m) {
		callAllListeners(this.onMonsterMovedDuringCombat, m);
	}

	@Override
	public void onPlayerKilledMonster(Monster target) {
		callAllListeners(this.onPlayerKilledMonster, target);
	}

	@Override
	public void onPlayerStartedFleeing() {
		callAllListeners(this.onPlayerStartedFleeing);
	}

	@Override
	public void onPlayerFailedFleeing() {
		callAllListeners(this.onPlayerFailedFleeing);
	}

	@Override
	public void onPlayerDoesNotHaveEnoughAP() {
		callAllListeners(this.onPlayerDoesNotHaveEnoughAP);
	}

	@Override
	public void onCombatTargetOutsideRange() {
		callAllListeners(this.onCombatTargetOutsideRange);
	}
}
