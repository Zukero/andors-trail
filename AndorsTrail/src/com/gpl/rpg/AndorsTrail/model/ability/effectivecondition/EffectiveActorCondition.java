package com.gpl.rpg.AndorsTrail.model.ability.effectivecondition;

import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EffectiveActorCondition {
    public final ActorConditionType conditionType;
    private ArrayList<EffectiveActorConditionEffect> appliedEffects = new ArrayList<EffectiveActorConditionEffect>();
    private int magnitude = 0;
    private int duration = 0;
    private boolean multipleDurations = false;

    EffectiveActorCondition(ActorConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public boolean isTemporaryEffect() {
        return ActorCondition.isTemporaryEffect(duration);
    }

    public int appliedEffectsCount() {
        return this.appliedEffects.size();
    }

    /**
     * Calculates how this effective conditions state will be when the current duration has expired.
     * Returns the calculated state as new effective condition. The original conditions state is not changed.
     */
    public EffectiveActorCondition calculateFollowingCondition() {
        EffectiveActorCondition ret = new EffectiveActorCondition(this.conditionType);
        for (EffectiveActorConditionEffect c : appliedEffects) {
            if (this.isTemporaryEffect() && c.duration > this.duration) {
                int duration = c.duration;
                if (c.isTemporaryEffect()) {
                    duration -= this.duration;
                }
                ret.addEffect(new EffectiveActorConditionEffect(c.magnitude, duration));
            } else if (!this.isTemporaryEffect() && c.isTemporaryEffect()) {
                ret.addEffect(new EffectiveActorConditionEffect(c.magnitude, c.duration));
            }
        }

        if (ret.appliedEffects.size() == 0) {
            return null;
        }
        return ret;
    }

    public int getMagnitude() {
        return this.magnitude;
    }

    public int getDuration() {
        return this.duration;
    }

    public boolean hasMultipleDurations() {
        return this.multipleDurations;
    }

    public List<EffectiveActorConditionEffect> getAppliedEffectsAsUnmodifiableList() {
        return Collections.unmodifiableList(this.appliedEffects);
    }

    EffectiveActorCondition removeTemporaryEffects() {
        boolean removedAtLeastOne = false;
        for (int i = appliedEffects.size() - 1; i >= 0; --i) {
            EffectiveActorConditionEffect c = appliedEffects.get(i);
            if (ActorCondition.isTemporaryEffect(c.duration)) {
                appliedEffects.remove(i);
                removedAtLeastOne = true;
            }
        }

        if (removedAtLeastOne) {
            return calculateEffectiveValues();
        }
        return null;
    }

    EffectiveActorCondition decreaseDurationAndRemoveEffects() {
        boolean changedAtLeastOne = false;
        for (int i = appliedEffects.size() - 1; i >= 0; --i) {
            EffectiveActorConditionEffect c = appliedEffects.get(i);
            if (ActorCondition.isTemporaryEffect(c.duration)) {
                if (c.duration > 1) {
                    appliedEffects.set(i, new EffectiveActorConditionEffect(c.magnitude, c.duration - 1));
                } else {
                    appliedEffects.remove(i);
                }
                changedAtLeastOne = true;
            }
        }

        if (changedAtLeastOne) {
            return calculateEffectiveValues();
        }
        return null;
    }

    EffectiveActorCondition removeEffect(EffectiveActorConditionEffect condition) {
        for (int i = appliedEffects.size() - 1; i >= 0; --i) {
            EffectiveActorConditionEffect c = appliedEffects.get(i);
            if (c.duration == condition.duration && c.magnitude == condition.magnitude) {
                appliedEffects.remove(i);
                return calculateEffectiveValues();
            }
        }
        return null;
    }

    private EffectiveActorCondition calculateEffectiveValues() {
        int magnitude = this.magnitude;
        int duration = this.duration;
        boolean multipleDurations = this.multipleDurations;
        this.magnitude = 0;
        this.duration = 0;
        this.multipleDurations = false;

        for (EffectiveActorConditionEffect c : appliedEffects) {
            applyEffect(c);
        }

        if (magnitude != this.magnitude || duration != this.duration || multipleDurations != this.multipleDurations) {
            return this;
        }
        return null;
    }

    public void addEffect(EffectiveActorConditionEffect effect) {
        this.appliedEffects.add(effect);
        applyEffect(effect);
    }

    private boolean applyEffect(EffectiveActorConditionEffect effect) {
        boolean changed = false;
        if (this.duration != effect.duration) {
            if (this.conditionType.isStacking) {
                if (!this.multipleDurations && this.duration != 0) {
                    this.multipleDurations = true;
                    changed = true;
                }
                if (this.duration == 0 || this.duration > effect.duration) {
                    this.duration = effect.duration;
                    changed = true;
                }
            } else {
                if (this.duration != 0 && !this.multipleDurations && this.magnitude != effect.magnitude) {
                    this.multipleDurations = true;
                    changed = true;
                }
                if (effect.duration > this.duration && effect.magnitude >= this.magnitude) {
                    this.duration = effect.duration;
                    changed = true;
                } else if (this.duration > effect.duration && this.magnitude >= effect.magnitude) {
                    // nothing to do, but it is easier to have the following if with this block
                } else if (this.duration > effect.duration) {
                    this.duration = effect.duration;
                    changed = true;
                }
            }
        }

        if (this.conditionType.isStacking) {
            this.magnitude += effect.magnitude;
            changed = true;
        } else if (this.magnitude < effect.magnitude) {
            this.magnitude = effect.magnitude;
            changed = true;
        }

        return changed;
    }
}
