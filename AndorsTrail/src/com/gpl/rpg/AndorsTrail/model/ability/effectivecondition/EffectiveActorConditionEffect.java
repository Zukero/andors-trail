package com.gpl.rpg.AndorsTrail.model.ability.effectivecondition;

import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;

public final class EffectiveActorConditionEffect {
    public final int magnitude;
    public final int duration;

    public EffectiveActorConditionEffect(int magnitude, int duration) {
        this.magnitude = magnitude;
        this.duration = duration;
    }

    public boolean isTemporaryEffect() { return ActorCondition.isTemporaryEffect(duration); }
}
