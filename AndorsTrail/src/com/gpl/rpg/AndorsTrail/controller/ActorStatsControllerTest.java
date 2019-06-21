package com.gpl.rpg.AndorsTrail.controller;

import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionType;
import com.gpl.rpg.AndorsTrail.model.ability.effectivecondition.EffectiveActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.effectivecondition.EffectiveActorConditionList;
import com.gpl.rpg.AndorsTrail.model.ability.effectivecondition.EffectiveActorConditionEffect;

import java.lang.reflect.Method;

import static org.junit.Assert.*;


public class ActorStatsControllerTest {
    @org.junit.Test
    public void rejuvenate() throws Exception {
        Method m = Class.forName("com.gpl.rpg.AndorsTrail.controller.ActorStatsController").getDeclaredMethod("getRandomConditionForRejuvenate", EffectiveActorConditionList.class);
        m.setAccessible(true);

        ActorConditionType t = new ActorConditionType("typeDummy", "Dummy", 1, null, true, false, null, null, null);
        ActorStatsController ctrl = new ActorStatsController(null, null);
        EffectiveActorConditionList conditions = new EffectiveActorConditionList();
        EffectiveActorCondition e = conditions.applyEffect(t, new EffectiveActorConditionEffect(2,1));
        e = conditions.applyEffect(t, new EffectiveActorConditionEffect(3,999));

        for (int i = 0; i< 3; i++) {
            ActorCondition c = (ActorCondition) m.invoke(ctrl, conditions);
            if (c != null) {
                e = conditions.getConditionOfType(c.conditionType.conditionTypeID);
                if (e != null && c.magnitude > 1) {
                    e.addEffect(new EffectiveActorConditionEffect(c.magnitude - 1, c.duration));
                }
                e = conditions.removeAppliedEffect(c.conditionType, new EffectiveActorConditionEffect(c.magnitude, c.duration));
            }

            if (i == 0) {
                assertTrue(e.appliedEffectsCount() == 2 && e.getMagnitude() == 4);
            } else if (i == 1){
                assertTrue(e.appliedEffectsCount() == 1 && e.getMagnitude() == 3);
            } else {
                assertTrue(e.appliedEffectsCount() == 1 && e.getMagnitude() == 3);
            }
        }
    }
}