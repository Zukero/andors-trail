package com.gpl.rpg.AndorsTrail.util;

import com.gpl.rpg.AndorsTrail.model.ability.traits.AbilityModifierTraits;
import com.gpl.rpg.AndorsTrail.model.item.*;
import com.gpl.rpg.AndorsTrail.model.map.MapObject;
import com.gpl.rpg.AndorsTrail.model.map.MonsterSpawnArea;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;

public class TestUtils {

    public static ItemType createNonEquipableOrUsableItemType() {

        // Properties for ItemCategory :
        String id_cat = "category";
        String displayName = "category 1";
        ItemCategory.ActionType actionType = ItemCategory.ActionType.none;
        Inventory.WearSlot inventorySlot = null;
        ItemCategory.ItemCategorySize size = ItemCategory.ItemCategorySize.large;

        // Properties for ItemType :
        String id = "id";
        int iconID = 1;
        String name = "name";
        String description = "description";
        ItemCategory category = new ItemCategory(id_cat, displayName, actionType, inventorySlot, size);
        ItemType.DisplayType displayType = ItemType.DisplayType.ordinary;
        boolean hasManualPrice = true;
        int fixedBaseMarketCost = 1;
        ItemTraits_OnEquip effects_equip = null;
        ItemTraits_OnUse effects_use = null;
        ItemTraits_OnUse effects_hit = null;
        ItemTraits_OnUse effects_kill = null;

        return new ItemType(id, iconID, name, description, category, displayType, hasManualPrice, fixedBaseMarketCost, effects_equip, effects_use, effects_hit, effects_kill);
    }

    public static ItemType createEquipableItemType() {
        // Properties for ItemCategory :
        String id_cat = "category";
        String displayName = "category 1";
        ItemCategory.ActionType actionType = ItemCategory.ActionType.equip;
        Inventory.WearSlot inventorySlot = null;
        ItemCategory.ItemCategorySize size = ItemCategory.ItemCategorySize.large;

        // Properties for ItemType :
        String id = "id1";
        int iconID = 1;
        String name = "name";
        String description = "description";
        ItemCategory category = new ItemCategory(id_cat, displayName, actionType, inventorySlot, size);
        ItemType.DisplayType displayType = ItemType.DisplayType.ordinary;
        boolean hasManualPrice = true;
        int fixedBaseMarketCost = 1;
        ItemTraits_OnEquip effects_equip = null;
        ItemTraits_OnUse effects_use = null;
        ItemTraits_OnUse effects_hit = null;
        ItemTraits_OnUse effects_kill = null;

        return new ItemType(id, iconID, name, description, category, displayType, hasManualPrice, fixedBaseMarketCost, effects_equip, effects_use, effects_hit, effects_kill);
    }

    public static ItemType createUsableItemType() {
        // Properties for ItemCategory :
        String id_cat = "category";
        String displayName = "category 1";
        ItemCategory.ActionType actionType = ItemCategory.ActionType.use;
        Inventory.WearSlot inventorySlot = null;
        ItemCategory.ItemCategorySize size = ItemCategory.ItemCategorySize.large;

        // Properties for ItemType :
        String id = "id2";
        int iconID = 1;
        String name = "name";
        String description = "description";
        ItemCategory category = new ItemCategory(id_cat, displayName, actionType, inventorySlot, size);
        ItemType.DisplayType displayType = ItemType.DisplayType.ordinary;
        boolean hasManualPrice = true;
        int fixedBaseMarketCost = 1;
        ItemTraits_OnEquip effects_equip = null;
        ItemTraits_OnUse effects_use = null;
        ItemTraits_OnUse effects_hit = null;
        ItemTraits_OnUse effects_kill = null;

        return new ItemType(id, iconID, name, description, category, displayType, hasManualPrice, fixedBaseMarketCost, effects_equip, effects_use, effects_hit, effects_kill);
    }
    
    public static ItemType createEquipableTwoHandWeaponItemType() {
        // Properties for ItemCategory :
        String id_cat = "category";
        String displayName = "category 1";
        ItemCategory.ActionType actionType = ItemCategory.ActionType.equip;
        Inventory.WearSlot inventorySlot = Inventory.WearSlot.weapon;
        ItemCategory.ItemCategorySize size = ItemCategory.ItemCategorySize.large;

        // Properties for ItemTraits_OnEquip
        AbilityModifierTraits ability = new AbilityModifierTraits(2,2,2,2,2,2,2,2,2,2,2,2,2);
        ItemTraits_OnEquip onequip = new ItemTraits_OnEquip(ability, null);

        // Properties for ItemType :
        String id = "id3";
        int iconID = 1;
        String name = "name";
        String description = "description";
        ItemCategory category = new ItemCategory(id_cat, displayName, actionType, inventorySlot, size);
        ItemType.DisplayType displayType = ItemType.DisplayType.ordinary;
        boolean hasManualPrice = true;
        int fixedBaseMarketCost = 1;
        ItemTraits_OnEquip effects_equip = onequip;
        ItemTraits_OnUse effects_use = null;
        ItemTraits_OnUse effects_hit = null;
        ItemTraits_OnUse effects_kill = null;

        return new ItemType(id, iconID, name, description, category, displayType, hasManualPrice, fixedBaseMarketCost, effects_equip, effects_use, effects_hit, effects_kill);
    }

    public static ItemType createEquipableShieldItemType() {
        // Properties for ItemCategory :
        String id_cat = "category";
        String displayName = "category 1";
        ItemCategory.ActionType actionType = ItemCategory.ActionType.equip;
        Inventory.WearSlot inventorySlot = Inventory.WearSlot.shield;
        ItemCategory.ItemCategorySize size = ItemCategory.ItemCategorySize.std;

        // Properties for ItemTraits_OnEquip
        AbilityModifierTraits ability = new AbilityModifierTraits(2,2,2,2,2,2,2,2,2,2,2,2,2);
        ItemTraits_OnEquip onequip = new ItemTraits_OnEquip(ability, null);

        // Properties for ItemType :
        String id = "id4";
        int iconID = 1;
        String name = "name";
        String description = "description";
        ItemCategory category = new ItemCategory(id_cat, displayName, actionType, inventorySlot, size);
        ItemType.DisplayType displayType = ItemType.DisplayType.ordinary;
        boolean hasManualPrice = true;
        int fixedBaseMarketCost = 1;
        ItemTraits_OnEquip effects_equip = onequip;
        ItemTraits_OnUse effects_use = null;
        ItemTraits_OnUse effects_hit = null;
        ItemTraits_OnUse effects_kill = null;

        return new ItemType(id, iconID, name, description, category, displayType, hasManualPrice, fixedBaseMarketCost, effects_equip, effects_use, effects_hit, effects_kill);
    }

    public static PredefinedMap createPredefinedMap() {
        int xmlResourceId = 1;
        String name = "map";
        Size size = new Size(50,50);
        MapObject[] eventObjects = new MapObject[1];
        MonsterSpawnArea[] spawnAreas = new MonsterSpawnArea[1];
        boolean isOutdoors = true;

        CoordRect position = new CoordRect(new Coord(0,0), new Size(1,1));
        MapObject.MapObjectType type = MapObject.MapObjectType.container;
        String place = "place";
        String group = "group";
        boolean isActiveForNewGame = false;

        eventObjects[0] = MapObject.createRestArea(position, place, group, isActiveForNewGame);

        return new PredefinedMap(xmlResourceId, name, size, eventObjects, spawnAreas, isOutdoors);
    }
}
