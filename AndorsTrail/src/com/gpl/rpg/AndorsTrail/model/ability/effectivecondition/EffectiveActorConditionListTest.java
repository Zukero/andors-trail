package com.gpl.rpg.AndorsTrail.model.ability.effectivecondition;

import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionType;

import org.junit.Test;

import static org.junit.Assert.*;


public class EffectiveActorConditionListTest {

    @Test
    public void mainUsecasesForNonStackingConditions() throws Exception {

        EffectiveActorConditionList l = new EffectiveActorConditionList();
        ActorConditionType t = new ActorConditionType("typeDummy", "Dummy", 1, null, false, false, null, null, null);

        // add a permanent condition
        EffectiveActorCondition c = l.applyEffect(t, new EffectiveActorConditionEffect(1, 999));
        assertTrue(l.size() == 1 && l.get(0) == c && c.getMagnitude() == 1 && c.getDuration() == 999
                && c.appliedEffectsCount() == 1
                && c.getAppliedEffectsAsUnmodifiableList().get(0).duration == 999
                && c.getAppliedEffectsAsUnmodifiableList().get(0).magnitude == 1
                && !c.hasMultipleDurations());

        // adding the same permanent condition with the same magnitude has no effect
        c = l.applyEffect(t, new EffectiveActorConditionEffect(1, 999));
        assertTrue(l.size() == 1 && c.getMagnitude() == 1 && c.getDuration() == 999
                && c.appliedEffectsCount() == 2
                && c.getAppliedEffectsAsUnmodifiableList().get(1).duration == 999
                && c.getAppliedEffectsAsUnmodifiableList().get(1).magnitude == 1
                && !c.hasMultipleDurations());

        // add a permanent condition of another type
        ActorConditionType t2 = new ActorConditionType("typeDummy2", "Dummy2", 1, null, false, false, null, null, null);
        c = l.applyEffect(t2, new EffectiveActorConditionEffect(1, 999));
        assertTrue(l.size() == 2 && l.get(0).getMagnitude() == 1 && l.get(1).getMagnitude() == 1);

        // remove the effective condition
        EffectiveActorCondition c2 = l.removeConditionOfType(t2.conditionTypeID);
        assertTrue(c == c2 && l.size() == 1);

        // adding a permanent condition with a higher magnitude than an existing one of the same type, replaces it
        c = l.applyEffect(t, new EffectiveActorConditionEffect(2, 999));
        assertTrue(l.size() == 1 && c == l.get(0) && c.getMagnitude() == 2
                && c.appliedEffectsCount() == 3
                && c.getAppliedEffectsAsUnmodifiableList().get(2).magnitude == 2
                && !c.hasMultipleDurations());

        // adding a short running condition with lower magnitude has no effect
        c = l.applyEffect(t, new EffectiveActorConditionEffect(1, 10));
        assertTrue(l.size() == 1 && c.getMagnitude() == 2
                && c.appliedEffectsCount() == 4
                && c.hasMultipleDurations());

        EffectiveActorCondition c3 = c.calculateFollowingCondition();
        assertTrue(c3.getMagnitude() == 1 && c3.appliedEffectsCount() == 1 && c3.getDuration() == 10);

        // adding a short running condition with the same magnitude has no effect
        c = l.applyEffect(t, new EffectiveActorConditionEffect(2, 10));
        assertTrue(l.size() == 1 && c.getMagnitude() == 2
                && c.appliedEffectsCount() == 5
                && c.hasMultipleDurations());

        // adding a short running condition with a higher magnitude gets added
        c = l.applyEffect(t, new EffectiveActorConditionEffect(3, 10));
        assertTrue(l.size() == 1 && c == l.get(0) && c.getMagnitude() == 3 && c.getDuration() == 10 && c.appliedEffectsCount() == 6);

        // adding a short running condition with lower magnitude has no effect
        c = l.applyEffect(t, new EffectiveActorConditionEffect(1, 11));
        assertTrue(l.size() == 1 && c == l.get(0) && c.getMagnitude() == 3 && c.getDuration() == 10 && c.appliedEffectsCount() == 7);


        c3 = c.calculateFollowingCondition();
        assertTrue(c3.getMagnitude() == 2 && c3.appliedEffectsCount() == 4
                && c3.getDuration() == 999
                && c3.hasMultipleDurations());
        c3 = c3.calculateFollowingCondition();
        assertTrue(c3.getMagnitude() == 1 && c3.appliedEffectsCount() == 1 && c3.getDuration() == 1);
        l.removeAppliedEffect(t, new EffectiveActorConditionEffect(1, 11));
        assertTrue(l.get(0).hasMultipleDurations());

        // temporary conditions get reduced and removed
        l.applyEffect(t, new EffectiveActorConditionEffect(1, 1));
        c = l.decreaseDurationAndRemoveEffects(0);
        assertTrue(l.size() == 1 && c == l.get(0) && c.getMagnitude() == 3 && c.getDuration() == 9 && c.appliedEffectsCount() == 6);
        assertTrue(c.getAppliedEffectsAsUnmodifiableList().get(0).duration == 999 && c.hasMultipleDurations());

        // adding a short running condition with a lower magnitude than an existing one with the same or shorter duration has no effect
        c = l.applyEffect(t, new EffectiveActorConditionEffect(3, 9));
        assertTrue(l.size() == 1 && c.getMagnitude() == 3 && c.getDuration() == 9 && c.appliedEffectsCount() == 7);
        c = l.applyEffect(t, new EffectiveActorConditionEffect(3, 7));
        assertTrue(l.size() == 1 && c.getMagnitude() == 3 && c.getDuration() == 9 && c.appliedEffectsCount() == 8);

        // adding a longer running condition with a higher magnitude than a existing long running condition
        c = l.applyEffect(t, new EffectiveActorConditionEffect(3, 999));
        assertTrue(l.size() == 1 && c.getMagnitude() == 3 && c.getDuration() == 999 && c.appliedEffectsCount() == 9);

        c = l.applyEffect(t, new EffectiveActorConditionEffect(4, 999));
        assertTrue(l.size() == 1 && c.getMagnitude() == 4 && c.getDuration() == 999 && c.appliedEffectsCount() == 10);

        c = l.removeAppliedEffect(t, new EffectiveActorConditionEffect(3, 999));
        assertTrue(l.size() == 1 && c.getMagnitude() == 4 && c.getDuration() == 999 && c.appliedEffectsCount() == 9);

        c = l.removeAppliedTemporaryEffects(0);
        assertTrue(c.getDuration() == 999 && c.appliedEffectsCount() == 4 && !c.hasMultipleDurations());
    }

    @Test
    public void mainUsecasesForStackingConditions() throws Exception {

        EffectiveActorConditionList l = new EffectiveActorConditionList();
        ActorConditionType t = new ActorConditionType("typeDummy", "Dummy", 1, null, true, false, null, null, null);

        // add a permanent condition
        EffectiveActorCondition c = l.applyEffect(t, new EffectiveActorConditionEffect(1, 999));
        assertTrue(l.size() == 1 && l.get(0) == c && c.getMagnitude() == 1 && c.getDuration() == 999
                && c.appliedEffectsCount() == 1
                && c.getAppliedEffectsAsUnmodifiableList().get(0).duration == 999
                && c.getAppliedEffectsAsUnmodifiableList().get(0).magnitude == 1
                && !c.hasMultipleDurations());

        // adding the same permanent condition with the same magnitude gets added
        c = l.applyEffect(t, new EffectiveActorConditionEffect(1, 999));
        assertTrue(l.size() == 1 && c.getMagnitude() == 2 && c.getDuration() == 999
                && c.appliedEffectsCount() == 2
                && c.getAppliedEffectsAsUnmodifiableList().get(1).duration == 999
                && c.getAppliedEffectsAsUnmodifiableList().get(1).magnitude == 1
                && !c.hasMultipleDurations());

        // adding a permanent condition with a higher magnitude than an existing one of the same type, gets also added
        c = l.applyEffect(t, new EffectiveActorConditionEffect(2, 999));
        assertTrue(l.size() == 1 && c == l.get(0) && c.getMagnitude() == 4
                && c.appliedEffectsCount() == 3
                && c.getAppliedEffectsAsUnmodifiableList().get(2).magnitude == 2
                && !c.hasMultipleDurations());

        // adding a short running condition with lower magnitude gets added
        c = l.applyEffect(t, new EffectiveActorConditionEffect(1, 10));
        assertTrue(l.size() == 1 && c.getMagnitude() == 5
                && c.getDuration() == 10
                && c.appliedEffectsCount() == 4
                && c.hasMultipleDurations());

        EffectiveActorCondition c3 = c.calculateFollowingCondition();
        assertTrue(c3.getMagnitude() == 4 && c3.appliedEffectsCount() == 3 && c3.getDuration() == 999);

        // temporary conditions get reduced and removed
        l.applyEffect(t, new EffectiveActorConditionEffect(1, 1));
        c = l.decreaseDurationAndRemoveEffects(0);
        assertTrue(l.size() == 1 && c == l.get(0) && c.getMagnitude() == 5 && c.getDuration() == 9
                && c.appliedEffectsCount() == 4
                && c.getAppliedEffectsAsUnmodifiableList().get(0).duration == 999 && c.hasMultipleDurations());

        c = l.removeAppliedEffect(t, new EffectiveActorConditionEffect(2, 999));
        assertTrue(l.size() == 1 && c.getMagnitude() == 3 && c.getDuration() == 9
                && c.appliedEffectsCount() == 3);

        c = l.removeAppliedTemporaryEffects(0);
        assertTrue(c.getDuration() == 999 && c.appliedEffectsCount() == 2
                && !c.hasMultipleDurations());
    }
}