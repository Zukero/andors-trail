package com.gpl.rpg.AndorsTrail.model.ability.effectivecondition;

import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Handles all data and logic for the effective conditions of an actor and tracks applied effects.
 * All applied effects of one type are combined into one effective condition of that type.
 */
public final class EffectiveActorConditionList implements Iterable<EffectiveActorCondition> {
    private ArrayList<EffectiveActorCondition> effectiveActorConditions = new ArrayList<EffectiveActorCondition>();

    public int size() {
        return this.effectiveActorConditions.size();
    }

    public boolean isEmpty() {
        return this.effectiveActorConditions.isEmpty();
    }

    public EffectiveActorCondition get(int index) {
        return this.effectiveActorConditions.get(index);
    }

    public void clear() {
        this.effectiveActorConditions.clear();
    }

    /**
     * Returns null if no condition of that type is present.
     */
    public EffectiveActorCondition getConditionOfType(final String conditionTypeID) {
        for (int i = this.effectiveActorConditions.size() - 1; i >= 0; --i) {
            EffectiveActorCondition c = this.effectiveActorConditions.get(i);
            if (c.conditionType.conditionTypeID.equals(conditionTypeID)) {
                return c;
            }
        }
        return null;
    }

    /**
     * If the condition of the given type exists it will be removed and the removed effective condition returned.
     * If no condition of that type existed then null is returned.
     */
    public EffectiveActorCondition removeConditionOfType(final String conditionTypeID) {
        for (int i = this.effectiveActorConditions.size() - 1; i >= 0; --i) {
            EffectiveActorCondition c = this.effectiveActorConditions.get(i);
            if (c.conditionType.conditionTypeID.equals(conditionTypeID)) {
                this.effectiveActorConditions.remove(i);
                return c;
            }
        }
        return null;
    }

    /**
     * Removes all temporary effects from the by index given effective condition
     * and returns the condition in the state without the temporary effects.
     * The effective condition is removed if only temporary effects existed.
     */
    public EffectiveActorCondition removeAppliedTemporaryEffects(int index) {
        EffectiveActorCondition c = this.effectiveActorConditions.get(index).removeTemporaryEffects();
        if (c != null && c.appliedEffectsCount() < 1) {
            this.effectiveActorConditions.remove(index);
        }
        return c;
    }

    /**
     * Reduces the duration of all temporary effects from the by index given effective condition
     * and returns the condition in the new state.
     * An effect is removed if it reaches a duration of 0 and the effective condition is removed
     * if all of its effects got removed.
     */
    public EffectiveActorCondition decreaseDurationAndRemoveEffects(int index) {
        EffectiveActorCondition c = this.effectiveActorConditions.get(index).decreaseDurationAndRemoveEffects();
        if (c != null && c.appliedEffectsCount() < 1) {
            this.effectiveActorConditions.remove(index);
        }
        return c;
    }

    /**
     * Removes one effect with the given magnitude and duration from the effective condition of the given type
     * and returns the condition in the new state. Returns null if the effect was not found.
     * If more than one effect of the same type, magnitude and duration exists then only the first one is removed.
     * The effective condition is removed if all of its effects got removed.
     */
    public EffectiveActorCondition removeAppliedEffect(ActorConditionType type, EffectiveActorConditionEffect effect) {
        for (int i = this.effectiveActorConditions.size() - 1; i >= 0; --i) {
            EffectiveActorCondition c = this.effectiveActorConditions.get(i);
            if (type.conditionTypeID.equals(c.conditionType.conditionTypeID)) {
                c.removeEffect(effect);
                if (c.appliedEffectsCount() < 1) {
                    this.effectiveActorConditions.remove(i);
                }
                return c;
            }
        }
        return null;
    }

    /**
     * Adds an effect to the effective condition of the given type.
     * If no condition of that type existed then it is created.
     */
    public EffectiveActorCondition applyEffect(ActorConditionType type, EffectiveActorConditionEffect effect) {
        EffectiveActorCondition c = null;
        for (EffectiveActorCondition e : this.effectiveActorConditions) {
            if (e.conditionType.conditionTypeID.equals(type.conditionTypeID)) {
                c = e;
                break;
            }
        }

        if (c == null) {
            c = new EffectiveActorCondition(type);
            effectiveActorConditions.add(c);
        }
        c.addEffect(effect);
        return c;
    }


    // ====== PARCELABLE ===================================================================

    public void FromParcel(DataInputStream src, WorldContext world, int fileversion) throws IOException {
        this.effectiveActorConditions.clear();
        final int numConditions = src.readInt();
        for (int i = 0; i < numConditions; ++i) {
            ActorCondition c = new ActorCondition(src, world, fileversion);
            applyEffect(c.conditionType, new EffectiveActorConditionEffect(c.magnitude, c.duration));
        }
    }

    public void writeToParcel(DataOutputStream dest) throws IOException {
        ArrayList<ActorCondition> conditions = new ArrayList<ActorCondition>();
        for (EffectiveActorCondition e : effectiveActorConditions) {
            ActorConditionType type = e.conditionType;
            for (EffectiveActorConditionEffect r : e.getAppliedEffectsAsUnmodifiableList()) {
                conditions.add(new ActorCondition(type, r.magnitude, r.duration));
            }
        }

        dest.writeInt(conditions.size());
        for (ActorCondition e : conditions) {
            e.writeToParcel(dest);
        }
    }

    @Override
    public Iterator<EffectiveActorCondition> iterator() {
        return effectiveActorConditions.iterator();
    }
}
