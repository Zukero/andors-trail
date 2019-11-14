package com.gpl.rpg.AndorsTrail.controller;

import com.gpl.rpg.AndorsTrail.model.actor.Actor;
import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionEffect;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionType;

import java.lang.reflect.Method;
import java.util.concurrent.locks.Condition;

import static org.junit.Assert.*;

public class ActorStatsControllerTest {
    @org.junit.Test
    public void addNonStackableActorCondition() throws Exception {
        Method m = Class.forName("com.gpl.rpg.AndorsTrail.controller.ActorStatsController").getDeclaredMethod("addNonStackableActorCondition", Actor.class, ActorConditionEffect.class, int.class);
        m.setAccessible(true);

        ActorStatsController ctrl = new ActorStatsController(null,null);
        ActorConditionType t = new ActorConditionType("typeDummy", "Dummy", 1, null, false, false, null, null, null);
        Actor a = new Actor(null, true, false);

        // add a permanent conditon
        m.invoke(ctrl, a, new ActorConditionEffect(t, 1, 999, null), 999);
        assertTrue(a.conditions.size() == 1 && a.conditions.get(0).magnitude == 1);

        // adding the same permanent conditon with the same magnitude has no effect
        m.invoke(ctrl, a, new ActorConditionEffect(t, 1, 999, null), 999);
        assertTrue(a.conditions.size() == 1 && a.conditions.get(0).magnitude == 1);

        // add a permanent conditon of another type
        ActorConditionType t2 = new ActorConditionType("typeDummy2", "Dummy2", 1, null, false, false, null, null, null);
        m.invoke(ctrl, a, new ActorConditionEffect(t2, 1, 999, null), 999);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 1 && a.conditions.get(1).magnitude == 1);
        a.conditions.remove(1);

        // adding a permanent condition with a higher magnitude than an existing one of the same type, replaces it
        m.invoke(ctrl, a, new ActorConditionEffect(t, 2, 999, null), 999);
        assertTrue(a.conditions.size() == 1 && a.conditions.get(0).magnitude == 2);

        // adding a short running condition with lower magnitude has no effect
        m.invoke(ctrl, a, new ActorConditionEffect(t, 1, 10, null), 10);
        assertTrue(a.conditions.size() == 1 && a.conditions.get(0).magnitude == 2);

        // adding a short running condition with the same magnitude has no effect
        m.invoke(ctrl, a, new ActorConditionEffect(t, 2, 10, null), 10);
        assertTrue(a.conditions.size() == 1 && a.conditions.get(0).magnitude == 2);

        // adding a short running condition with a higher magnitude gets added but reduced
        // by the magnitude of the long running one
        m.invoke(ctrl, a, new ActorConditionEffect(t, 3, 10, null), 10);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 2 && a.conditions.get(1).magnitude == 1);
        m.invoke(ctrl, a, new ActorConditionEffect(t, 4, 10, null), 10);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 2 && a.conditions.get(1).magnitude == 2);

        // adding a short running condition with a lower magnitude than an existing one with the same or shorter duration has no effect
        m.invoke(ctrl, a, new ActorConditionEffect(t, 3, 10, null), 10);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 2 && a.conditions.get(1).magnitude == 2);
        m.invoke(ctrl, a, new ActorConditionEffect(t, 3, 7, null), 7);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 2 && a.conditions.get(1).magnitude == 2);

        // adding a longer runnig condition with a higher magnitude than a existing long running condition reduces the magnitude of shorter running conditions
        m.invoke(ctrl, a, new ActorConditionEffect(t, 3, 999, null), 999);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 3 && a.conditions.get(1).magnitude == 1);

        m.invoke(ctrl, a, new ActorConditionEffect(t, 3, 999, null), 999);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 3 && a.conditions.get(1).magnitude == 1);

        m.invoke(ctrl, a, new ActorConditionEffect(t, 4, 999, null), 999);
        assertTrue(a.conditions.size() == 1 && a.conditions.get(0).magnitude == 4);

        m.invoke(ctrl, a, new ActorConditionEffect(t, 4, 1000, null), 1000);
        assertTrue(a.conditions.size() == 1 && a.conditions.get(0).magnitude == 4 && a.conditions.get(0).duration == 1000);

        m.invoke(ctrl, a, new ActorConditionEffect(t, 5, 1001, null), 1001);
        assertTrue(a.conditions.size() == 1 && a.conditions.get(0).magnitude == 5 && a.conditions.get(0).duration == 1001);

        m.invoke(ctrl, a, new ActorConditionEffect(t, 6, 10, null), 10);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 5 && a.conditions.get(1).magnitude == 1);

        m.invoke(ctrl, a, new ActorConditionEffect(t, 6, 100, null), 100);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 5 && a.conditions.get(1).magnitude == 1 && a.conditions.get(1).duration == 100);

        m.invoke(ctrl, a, new ActorConditionEffect(t, 7, 100, null), 100);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 5 && a.conditions.get(1).magnitude == 2 && a.conditions.get(1).duration == 100);

        m.invoke(ctrl, a, new ActorConditionEffect(t, 6, 200, null), 200);
        assertTrue(a.conditions.size() == 3 && a.conditions.get(0).magnitude == 5 && a.conditions.get(1).magnitude == 1 && a.conditions.get(2).magnitude == 1);

        m.invoke(ctrl, a, new ActorConditionEffect(t, 7, 300, null), 300);
        assertTrue(a.conditions.size() == 2 && a.conditions.get(0).magnitude == 5 && a.conditions.get(1).magnitude == 2);

    }
}