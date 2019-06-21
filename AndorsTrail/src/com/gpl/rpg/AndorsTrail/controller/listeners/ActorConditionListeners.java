package com.gpl.rpg.AndorsTrail.controller.listeners;

import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.effectivecondition.EffectiveActorCondition;
import com.gpl.rpg.AndorsTrail.model.actor.Actor;
import com.gpl.rpg.AndorsTrail.util.ListOfListeners;

public final class ActorConditionListeners extends ListOfListeners<ActorConditionListener> implements ActorConditionListener {

	private final Function2<ActorConditionListener, Actor, EffectiveActorCondition> onActorConditionAdded = new Function2<ActorConditionListener, Actor, EffectiveActorCondition>() {
		@Override public void call(ActorConditionListener listener, Actor actor, EffectiveActorCondition condition) { listener.onActorConditionAdded(actor, condition); }
	};
	private final Function2<ActorConditionListener, Actor, EffectiveActorCondition> onActorConditionRemoved = new Function2<ActorConditionListener, Actor, EffectiveActorCondition>() {
		@Override public void call(ActorConditionListener listener, Actor actor, EffectiveActorCondition condition) { listener.onActorConditionRemoved(actor, condition); }
	};
	private final Function2<ActorConditionListener, Actor, EffectiveActorCondition> onActorConditionChanged = new Function2<ActorConditionListener, Actor, EffectiveActorCondition>() {
		@Override public void call(ActorConditionListener listener, Actor actor, EffectiveActorCondition condition) { listener.onActorConditionChanged(actor, condition); }
	};
	private final Function2<ActorConditionListener, Actor, EffectiveActorCondition> onActorConditionRoundEffectApplied = new Function2<ActorConditionListener, Actor, EffectiveActorCondition>() {
		@Override public void call(ActorConditionListener listener, Actor actor, EffectiveActorCondition condition) { listener.onActorConditionRoundEffectApplied(actor, condition); }
	};
	private final Function2<ActorConditionListener, Actor, ActorCondition> onActorConditionImmunityAdded = new Function2<ActorConditionListener, Actor, ActorCondition>() {
		@Override public void call(ActorConditionListener listener, Actor actor, ActorCondition condition) { listener.onActorConditionImmunityAdded(actor, condition); }
	};
	private final Function2<ActorConditionListener, Actor, ActorCondition> onActorConditionImmunityRemoved = new Function2<ActorConditionListener, Actor, ActorCondition>() {
		@Override public void call(ActorConditionListener listener, Actor actor, ActorCondition condition) { listener.onActorConditionImmunityRemoved(actor, condition); }
	};
	private final Function2<ActorConditionListener, Actor, ActorCondition> onActorConditionImmunityDurationChanged = new Function2<ActorConditionListener, Actor, ActorCondition>() {
		@Override public void call(ActorConditionListener listener, Actor actor, ActorCondition condition) { listener.onActorConditionImmunityDurationChanged(actor, condition); }
	};
	
	@Override
	public void onActorConditionAdded(Actor actor, EffectiveActorCondition condition) {
		callAllListeners(this.onActorConditionAdded, actor, condition);
	}

	@Override
	public void onActorConditionRemoved(Actor actor, EffectiveActorCondition condition) {
		callAllListeners(this.onActorConditionRemoved, actor, condition);
	}

	@Override
	public void onActorConditionChanged(Actor actor, EffectiveActorCondition condition) {
		callAllListeners(this.onActorConditionChanged, actor, condition);
	}

	@Override
	public void onActorConditionRoundEffectApplied(Actor actor, EffectiveActorCondition condition) {
		callAllListeners(this.onActorConditionRoundEffectApplied, actor, condition);
	}
	

	@Override
	public void onActorConditionImmunityAdded(Actor actor, ActorCondition condition) {
		callAllListeners(this.onActorConditionImmunityAdded, actor, condition);
	}

	@Override
	public void onActorConditionImmunityRemoved(Actor actor, ActorCondition condition) {
		callAllListeners(this.onActorConditionImmunityRemoved, actor, condition);
	}

	@Override
	public void onActorConditionImmunityDurationChanged(Actor actor, ActorCondition condition) {
		callAllListeners(this.onActorConditionImmunityDurationChanged, actor, condition);
	}
}
