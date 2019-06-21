package com.gpl.rpg.AndorsTrail.controller.listeners;

import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.effectivecondition.EffectiveActorCondition;
import com.gpl.rpg.AndorsTrail.model.actor.Actor;

public interface ActorConditionListener {
	public void onActorConditionAdded(Actor actor, EffectiveActorCondition condition);
	public void onActorConditionRemoved(Actor actor, EffectiveActorCondition condition);
	public void onActorConditionChanged(Actor actor, EffectiveActorCondition condition);
	public void onActorConditionRoundEffectApplied(Actor actor, EffectiveActorCondition condition);
	public void onActorConditionImmunityAdded(Actor actor, ActorCondition condition);
	public void onActorConditionImmunityRemoved(Actor actor, ActorCondition condition);
	public void onActorConditionImmunityDurationChanged(Actor actor, ActorCondition condition);
}
