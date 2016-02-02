package com.gpl.rpg.AndorsTrail.controller;

import android.test.AndroidTestCase;
import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.ModelContainer;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.util.TestUtils;
import org.junit.Before;
import org.junit.Test;

public class ItemControllerTest extends AndroidTestCase {

    private ItemController itemcontroller;
    private WorldContext world;
    private PredefinedMap map;

    @Before
    public void setUp() {

        // Create the App :
        AndorsTrailApplication andor = new AndorsTrailApplication();

        // Create the world :
        this.world = new WorldContext();

        // Create a map :
        this.map = TestUtils.createPredefinedMap();

        // Init the World :
        this.world.model = new ModelContainer();
        this.world.model.currentMap = map;

        // Create all controllers :
        ControllerContext context = new ControllerContext(andor, world);

        // Retrieve the controller for tests :
        this.itemcontroller = context.itemController;

    }

    @Test
    public void testDropItemInInventoryWithBiggerQuantity() {

        // Create an item :
        ItemType item = TestUtils.createNonEquipableOrUsableItemType();

        // Add two items in the inventory :
        this.world.model.player.inventory.addItem(item, 2);

        // Verification : 2 items in the inventory :
        assertEquals(2, this.world.model.player.inventory.getItemQuantity(item.id));

        // Try to remove 4 items :
        this.itemcontroller.dropItem(item, 4);

        // Verification : already 2 items in the inventory :
        assertEquals(2, this.world.model.player.inventory.getItemQuantity(item.id));

        // Verification : no items in the map at current position :
        assertNull(this.map.getBagAt(this.world.model.player.position));
    }

    @Test
    public void testDropItemInInventoryWithCorrectQuantity() {

        // Create an item :
        ItemType item = TestUtils.createNonEquipableOrUsableItemType();

        // Add two items in the inventory :
        this.world.model.player.inventory.addItem(item, 2);

        // Verification : 2 items in the inventory :
        assertEquals(2, this.world.model.player.inventory.getItemQuantity(item.id));

        // Try to remove one item :
        this.itemcontroller.dropItem(item, 1);

        // Verification : there is only one item in the inventory :
        assertEquals(1, this.world.model.player.inventory.getItemQuantity(item.id));

        // Verification : an item is in the map at current position :
        assertEquals(1, this.map.getBagAt(this.world.model.player.position).items.getItemQuantity(item.id));
    }

    @Test
    public void testDropItemNotInInventory() {

        // Create an item :
        ItemType item = TestUtils.createNonEquipableOrUsableItemType();

        // Try to remove one item :
        this.itemcontroller.dropItem(item, 1);

        // Verification : there is no item in the inventory :
        assertEquals(0, this.world.model.player.inventory.getItemQuantity(item.id));

        // Verification : an item is in the map at current position :
        assertNull(this.map.getBagAt(this.world.model.player.position));
    }

    /*
    BOOOUUUUMMMMMM !!!!!!!!!! A CRASH !

    @Test
    public void testDropItemInInventoryWithNegativeQuantity() {

        // Create an item :
        ItemType item = ToolsForTests.createItemType();

        // Add two items in the inventory :
        world.model.player.inventory.addItem(item, 2);

        // Verification : 2 items in the inventory :
        assertEquals(2, world.model.player.inventory.getItemQuantity(item.id));

        // Try to remove 4 items :
        itemcontroller.dropItem(item, -2);

        // Verification : already 2 items in the inventory :
        assertEquals(2, world.model.player.inventory.getItemQuantity(item.id));

        // Verification : no items in the map at current position :
        assertNull(map.getBagAt(world.model.player.position));
    }
    */

    @Test
    public void testEquipItemNotInInventory() {

        // Create an item :
        ItemType item = TestUtils.createEquipableItemType();

        // Try to equip the player with the item :
        this.itemcontroller.equipItem(item, Inventory.WearSlot.body);

        // Verification : the player doesn't wear the item :
        assertFalse(this.world.model.player.inventory.isWearing(item.id));
    }

    @Test
    public void testEquipItemNonEquipableItem() {

        // Create an item :
        ItemType item = TestUtils.createNonEquipableOrUsableItemType();

        // Add the item in the inventory :
        this.world.model.player.inventory.addItem(item, 1);

        // Verification : the item is in the inventory :
        assertEquals(1, this.world.model.player.inventory.getItemQuantity(item.id));

        // Try to equip the player with the item :
        this.itemcontroller.equipItem(item, Inventory.WearSlot.body);

        // Verification : the player doesn't wear the item :
        assertFalse(this.world.model.player.inventory.isWearing(item.id));
    }
    
        @Test
    public void testEquipItemInCombatTooBigReequipCost() {

        // Create an item :
        ItemType item = TestUtils.createEquipableItemType();

        // Add the item in the inventory :
        world.model.player.inventory.addItem(item, 1);

        // Change isInCombat to "true"
        world.model.uiSelections.isInCombat = true;

        // Change player attributes
        Player player = world.model.player;
        player.ap.current = 2; // not enough to reequip in combat
        player.reequipCost = 4;

        // Verification : the item is in the inventory :
        assertEquals(1, world.model.player.inventory.getItemQuantity(item.id));

        // Try to equip the player with the item :
        itemcontroller.equipItem(item, Inventory.WearSlot.body);

        // Verification : the player doesn't wear the item :
        assertFalse(world.model.player.inventory.isWearing(item.id));

    }

    @Test
    public void testEquipItemEnoughToReequipDuringCombat() {

        // Create an item :
        ItemType item = TestUtils.createEquipableItemType();

        // Add the item in the inventory :
        world.model.player.inventory.addItem(item, 1);

        // Change isInCombat to "true"
        world.model.uiSelections.isInCombat = true;

        // Change player attributes
        Player player = world.model.player;
        player.ap.current = 4; // enough to reequip in combat
        player.reequipCost = 2;

        // Verification : the item is in the inventory :
        assertEquals(1, world.model.player.inventory.getItemQuantity(item.id));

        // Try to equip the player with the item :
        itemcontroller.equipItem(item, Inventory.WearSlot.body);

        // Verification : the player wears the item :
        assertTrue(world.model.player.inventory.isWearing(item.id));

        // Verification : now the item isn't the inventory :
        assertEquals(0, world.model.player.inventory.getItemQuantity(item.id));

    }

    @Test
    public void testEquipItemShieldAlreadyWearAShield() {

        // Create a two hand weapon :
        ItemType twohand = TestUtils.createEquipableTwoHandWeaponItemType();
        // Create a shield :
        ItemType shield = TestUtils.createEquipableShieldItemType();

        // Add the shield in the inventory :
        world.model.player.inventory.addItem(shield, 1);
        // Add the weapon in the inventory :
        world.model.player.inventory.addItem(twohand, 1);

        // Equip the player with the two hand weapon
        itemcontroller.equipItem(twohand, Inventory.WearSlot.weapon);

        // Verification : the player wears the weapon
        assertEquals(twohand, world.model.player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.weapon));
        assertNull(world.model.player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.shield));

        // Equip the player with the shield
        itemcontroller.equipItem(shield, Inventory.WearSlot.shield);

        // Verification : the player wears the shield
        assertNull(world.model.player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.weapon));
        assertEquals(shield, world.model.player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.shield));

    }


    @Test
    public void testEquipItemTwoHandWeeaponAlreadyWearAShield() {
        // Create a two hand weapon :
        ItemType twohand = TestUtils.createEquipableTwoHandWeaponItemType();
        // Create a shield :
        ItemType shield = TestUtils.createEquipableShieldItemType();

        // Add the shield in the inventory :
        world.model.player.inventory.addItem(shield, 1);
        // Add the weapon in the inventory :
        world.model.player.inventory.addItem(twohand, 1);

        // Equip the player with the shield
        itemcontroller.equipItem(shield, Inventory.WearSlot.shield);

        // Verification : the player wears the shield
        assertEquals(shield, world.model.player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.shield));
        assertNull(world.model.player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.weapon));

        // Equip the player with the two hand weapon
        itemcontroller.equipItem(twohand, Inventory.WearSlot.weapon);

        // Verification : the player wears the weapon
        assertNull(world.model.player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.shield));
        assertEquals(twohand, world.model.player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.weapon));

    }

    /**
     * Test for the method useItem
     * usage of an unusable item
     */
    @Test
    public void testUseItemNotusableItem() {

        // Create an item
        ItemType item = TestUtils.createNonEquipableOrUsableItemType();

        // Add the item in the inventory :
        this.world.model.player.inventory.addItem(item, 1);

        // verify if the item is in the player's inventory
        assertNotNull(this.world.model.player.inventory.findItem(item.id));

        // use the item
        this.itemcontroller.useItem(item);

        // verify that the item was not removed from the player's inventory
        assertNotNull(this.world.model.player.inventory.findItem(item.id));
    }

    /**
     * Test for the method useItem
     * usage of an unusable item
     */
    @Test
    public void testUseItemUsableItem() {

        // Create an item
        ItemType item = TestUtils.createUsableItemType();

        // Add the item in the player inventory
        this.world.model.player.inventory.addItem(item, 1);

        // verify if the item is in the player's inventory
        assertNotNull(this.world.model.player.inventory.findItem(item.id));

        // use the item
        this.itemcontroller.useItem(item);

        // verify that the item was removed from the player's inventory
        assertNull(this.world.model.player.inventory.findItem(item.id));
    }

    /**
     * Test for the method useItem
     * usage of implied item used list
     */
    @Test
    public void testUseItemItemHasBeenUsed() {

        // Create an item
        ItemType item = TestUtils.createUsableItemType();

        // Add the item in the player inventory
        this.world.model.player.inventory.addItem(item, 1);

        // verify if the item is in the player's inventory
        assertNotNull(this.world.model.player.inventory.findItem(item.id));

        // use the item
        this.itemcontroller.useItem(item);

        assertEquals(this.world.model.statistics.getNumberOfTimesItemHasBeenUsed(item.id), 1);
    }


    @Test
    /*
    On essaie de déséquiper le personnage avec un élément qui ne se porte pas
    -> Aucun effet sur l'équipement
     */
    public void testUnequipSlotWithNoWearItem() throws Exception {
        // Etant donné un item qui ne peut être déséquipé
        ItemType noWearingItem = TestUtils.createUsableItemType();

        // et le personnage est équipé d'un item équipable
        ItemType wearingItem = TestUtils.createEquipableItemType();
        world.model.player.inventory.addItem(wearingItem, 1);
        itemcontroller.equipItem(wearingItem, Inventory.WearSlot.body);
        assertTrue(world.model.player.inventory.isWearing(wearingItem.id));

        // Quand on tente de déséquiper le personnage d'un item qui ne s'équipe pas
        itemcontroller.unequipSlot(noWearingItem, Inventory.WearSlot.body);

        // Alors le personnage n'est pas déséquipé
        assertTrue(world.model.player.inventory.isWearing(wearingItem.id));
        assertFalse(world.model.player.inventory.isWearing(noWearingItem.id));
    }

    @Test
    /*
    On essaie de déséquiper le personnage alors qu'il n'a aucun équipement
    -> Aucun effet sur l'équipement
     */
    public void testUnequipSlotWithAnyItem() throws Exception {
        // Etant donné un item qui peut être équipé/déséquipé
        ItemType wearingItem = TestUtils.createEquipableItemType();

        // et le personnage n'est pas équipé d'un item
        assertFalse(world.model.player.inventory.isWearing(wearingItem.id));

        // Quand on tente de déséquiper le personnage qui n'est pas équipé
        itemcontroller.unequipSlot(wearingItem, Inventory.WearSlot.body);

        // Alors le personnage n'est pas déséquipé
        assertFalse(world.model.player.inventory.isWearing(wearingItem.id));
    }

    @Test
    /*
    On essaie de déséquiper le personnage d'un élément usé en plein combat
    -> Le joueur ne porte plus l'élément
     */
    public void testUnequipSlotInCombatWithUsedItem() throws Exception {
        // Etant donné un item qui peut être équipé/déséquipé
        ItemType wearingItem = TestUtils.createEquipableItemType();
        // le personnage est en combat
        world.model.uiSelections.isInCombat = true;
        // et porte cet item
        world.model.player.inventory.addItem(wearingItem, 1);
        itemcontroller.equipItem(wearingItem, Inventory.WearSlot.body);
        assertTrue(world.model.player.inventory.isWearing(wearingItem.id));
        itemcontroller.useItem(wearingItem);

        // Quand on déséquipe le personnage de cet item
        itemcontroller.unequipSlot(wearingItem, Inventory.WearSlot.body);

        // Alors on vérifie que l'item a bien été déséquipé
        assertFalse(world.model.player.inventory.isWearing(wearingItem.id));
    }

    @Test
    /*
    On essaie de déséquiper le personnage d'un élément non usé en plein combat
    -> Aucun effet sur l'équipement
     */
    public void testUnequipSlotInCombatWithInusedItem() throws Exception {
        // Etant donné un item qui peut être équipé/déséquipé
        ItemType wearingItem = TestUtils.createEquipableItemType();
        // le personnage est en combat
        world.model.uiSelections.isInCombat = true;
        // et porte cet item
        world.model.player.inventory.addItem(wearingItem, 1);
        itemcontroller.equipItem(wearingItem, Inventory.WearSlot.body);
        assertTrue(world.model.player.inventory.isWearing(wearingItem.id));

        // Quand on déséquipe le personnage de cet item
        itemcontroller.unequipSlot(wearingItem, Inventory.WearSlot.body);

        // Alors on vérifie que l'item a bien été déséquipé
        assertFalse(world.model.player.inventory.isWearing(wearingItem.id));
    }

    @Test
    /*
    On essaie de déséquiper le personnage d'un élément
    -> Le joueur ne porte plus l'élément et l'élément est retombé dans son inventaire
     */
    public void testUnequipSlotAndItemGoesToInventory() throws Exception {
        // Etant donné un item qui peut être équipé/déséquipé
        ItemType wearingItem = TestUtils.createEquipableItemType();
        // et le personnage porte cet item
        world.model.player.inventory.addItem(wearingItem, 1);
        itemcontroller.equipItem(wearingItem, Inventory.WearSlot.body);
        assertTrue(world.model.player.inventory.isWearing(wearingItem.id));

        // Quand on déséquipe le joueur de cet item
        itemcontroller.unequipSlot(wearingItem, Inventory.WearSlot.body);

        // Alors on vérifie que le joueur ne porte plus l'item
        assertFalse(world.model.player.inventory.isWearing(wearingItem.id));
        // et l'item est revenu dans l'inventaire du joueur
        assertTrue(world.model.player.inventory.hasItem(wearingItem.id));
    }

}
