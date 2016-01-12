package com.gpl.rpg.AndorsTrail.model.item;

import android.content.ClipData;

import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.ItemController;
import com.gpl.rpg.AndorsTrail.savegames.LegacySavegameFormatReaderForItemContainer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public final class Inventory extends ItemContainer {

    public static enum WearSlot {
        weapon, shield, head, body, hand, feet, neck, leftring, rightring;

        public static WearSlot fromString(String s, WearSlot default_) {
            if (s == null) return default_;
            return valueOf(s);
        }
    }

    public int gold = 0;
    private static final int NUM_WORN_SLOTS = WearSlot.values().length;
    public static final int NUM_QUICK_SLOTS = 3;
    public static final int NUM_PRESETS = 3; // Not enough to just change this, must also add UI buttons and cross-links
    public static final int NUM_PRESET_SLOTS = 99;
    private final ItemType[] wear = new ItemType[NUM_WORN_SLOTS];
    public final ItemType[] quickitem = new ItemType[NUM_QUICK_SLOTS];
    public final ArrayList<ItemType>[] presetsAll = (ArrayList<ItemType>[]) new ArrayList[NUM_PRESETS];
    public int currentWornPreset = 0;


    public Inventory() {
    }

    public void clear() {
        for (int i = 0; i < NUM_WORN_SLOTS; ++i) wear[i] = null;
        for (int i = 0; i < NUM_QUICK_SLOTS; ++i) quickitem[i] = null;
        gold = 0;
        items.clear();
    }

    public void add(final Loot loot) {
        this.gold += loot.gold;
        this.add(loot.items);
    }

    public boolean isEmptySlot(WearSlot slot) {
        return wear[slot.ordinal()] == null;
    }

    public ItemType getItemTypeInWearSlot(WearSlot slot) {
        return wear[slot.ordinal()];
    }

    public void setItemTypeInWearSlot(WearSlot slot, ItemType type) {
        wear[slot.ordinal()] = type;
    }

    public boolean isWearing(String itemTypeID) {
        for (int i = 0; i < NUM_WORN_SLOTS; ++i) {
            if (wear[i] == null) continue;
            if (wear[i].id.equals(itemTypeID)) return true;
        }
        return false;
    }

    public boolean isWearing(String itemTypeID, int minNumber) {
        if (minNumber == 0) return isWearing(itemTypeID);
        for (int i = 0; i < NUM_WORN_SLOTS; ++i) {
            if (wear[i] == null) continue;
            if (wear[i].id.equals(itemTypeID)) minNumber--;
        }
        return minNumber <= 0;
    }

    public static boolean isArmorSlot(WearSlot slot) {
        if (slot == null) return false;
        switch (slot) {
            case head:
            case body:
            case hand:
            case feet:
                return true;
            default:
                return false;
        }
    }

    public void assignToPreset(ItemType item, int presetNumber) {
        assignToPreset(item, (byte) presetNumber);
    }

    public void assignToPreset(ItemType item, byte presetNumber) {
        if (item == null) return;
        int presetIndex = presetNumber - 1;

        if (presetsAll[presetIndex] == null){
            presetsAll[presetIndex] = new ArrayList<ItemType>();
            presetsAll[presetIndex].add(item);
            return;
        }

        if (!item.isEquippable()) {
            addToPreset(presetIndex, item);
            return;
        }
        boolean firstWeapon = true;
        WearSlot destSlot = item.category.inventorySlot;
        for (ItemType i : presetsAll[presetIndex]) {
            if(item.category.inventorySlot.equals(i.category.inventorySlot)){
                if (item.isWeapon()){
                    if (item.isTwohandWeapon() || i.isTwohandWeapon()) { //Only one two-handed weapon
                        presetsAll[presetIndex].remove(i);
                        addToPreset(presetIndex, item);
                        return;
                    }
                    else if(firstWeapon)
                            firstWeapon = false;
                    else {
                        presetsAll[presetIndex].remove(i);
                        addToPreset(presetIndex, item);
                        return;
                    }
                }
                else {
                    presetsAll[presetIndex].remove(i);
                    addToPreset(presetIndex, item);
                    return;
                }
            }
        }
        addToPreset(presetIndex, item);
    }
    public void addToPreset(int presetIndex, ItemType item){
        if (presetsAll[presetIndex].indexOf(item) == -1)
            presetsAll[presetIndex].add(item);
    }

    public Inventory buildQuestItems() {
        Inventory questItems = new Inventory();
        for (ItemEntry i : this.items) {
            if (i == null) break;
            if (i.itemType.isQuestItem()) {
                questItems.items.add(i);
            }
        }
        return questItems;
    }

	/*public Inventory buildEquipableItems(){
        Inventory equipableItems = new Inventory();
		for(ItemEntry i: this.items){
			if(i.itemType.isEquippable())
				equipableItems.items.add(i);
		}
		return equipableItems;
	}*/

    public Inventory buildUsableItems() {
        Inventory usableItems = new Inventory();
        for (ItemEntry i : this.items) {
            if (i == null) break;
            if (i.itemType.isUsable())
                usableItems.items.add(i);
        }
        return usableItems;
    }

    public Inventory buildWeaponItems() {
        Inventory weaponItems = new Inventory();
        for (ItemEntry i : this.items) {
            if (i == null) break;
            if (i.itemType.isWeapon())
                weaponItems.items.add(i);
        }
        return weaponItems;
    }

    public Inventory buildArmorItems() {
        Inventory armorItems = new Inventory();
        for (ItemEntry i : this.items) {
            if (i == null) break;
            if (i.itemType.isEquippable() && !i.itemType.isWeapon())
                armorItems.items.add(i);
        }
        return armorItems;
    }

    public Inventory buildOtherItems() {
        Inventory otherItems = new Inventory();
        for (ItemEntry i : this.items) {
            if (i == null) break;
            if (i.itemType.isEquippable() || i.itemType.isUsable() || i.itemType.isQuestItem())
                continue;
            otherItems.items.add(i);
        }
        return otherItems;
    }

    public ItemContainer getPresetItems(int presetNumber) {
        return getPresetItems((byte) presetNumber);
    }

    public ItemContainer getPresetItems(byte presetNumber) {
        if (presetsAll[presetNumber - 1] == null) presetsAll[presetNumber - 1] = new ArrayList<>();
        //rebuildPresetIndices(presetNumber); // redundant, method assignToPreset doesn't do this after assign

        ItemContainer generatedPreset = new ItemContainer();
        for (int i = 0; i < presetsAll[presetNumber - 1].size(); i++)
            generatedPreset.addItem(presetsAll[presetNumber - 1].get(i),
                    this.getItemQuantity(presetsAll[presetNumber - 1].get(i).id));

        return generatedPreset;
    }

    public void unassignFromPresets(ItemType lastSelectedItem) {
        for (int j = 0; j < presetsAll.length; j++)
            presetsAll[j].remove(lastSelectedItem);
    }

    /*public int getIndexOfContainingPreset(ItemType item) {
        if (item == null) return 0;
        for (int i = 0; i < presetsAll.length; i++) {
            for (int j = 0; j < presetsAll[i].size(); j++) {
                if (item.equals(presetsAll[i].get(j)))
                    return i + 1;
            }
        }
        return 0;
    }*/


    // ====== PARCELABLE ===================================================================

    public Inventory(DataInputStream src, WorldContext world, int fileversion) throws IOException {
        this.readFromParcel(src, world, fileversion);
    }

    @Override
    public void readFromParcel(DataInputStream src, WorldContext world, int fileversion) throws IOException {
        super.readFromParcel(src, world, fileversion);
        gold = src.readInt();

        if (fileversion < 23) LegacySavegameFormatReaderForItemContainer.refundUpgradedItems(this);

        for (int i = 0; i < NUM_WORN_SLOTS; ++i) {
            wear[i] = null;
        }
        final int numWornSlots = src.readInt();
        for (int i = 0; i < numWornSlots; ++i) {
            if (src.readBoolean()) {
                wear[i] = world.itemTypes.getItemType(src.readUTF());
            }
        }
        for (int i = 0; i < NUM_QUICK_SLOTS; ++i) {
            quickitem[i] = null;
        }
        if (fileversion >= 19) {
            final int quickSlots = src.readInt();
            for (int i = 0; i < quickSlots; ++i) {
                if (src.readBoolean()) {
                    quickitem[i] = world.itemTypes.getItemType(src.readUTF());
                }
            }
        }

        for(int i =0; i< NUM_PRESETS; i++)
            presetsAll[i] = new ArrayList<>();
        final int presetItems = src.readInt();
        for (int i = 0; i < presetItems; i++) {
            assignToPreset(world.itemTypes.getItemType(src.readUTF()), src.readByte());
        }
    }

    @Override
    public void writeToParcel(DataOutputStream dest) throws IOException {
        super.writeToParcel(dest);
        dest.writeInt(gold);
        dest.writeInt(NUM_WORN_SLOTS);
        for (int i = 0; i < NUM_WORN_SLOTS; ++i) {
            if (wear[i] != null) {
                dest.writeBoolean(true);
                dest.writeUTF(wear[i].id);
            } else {
                dest.writeBoolean(false);
            }
        }
        dest.writeInt(NUM_QUICK_SLOTS);
        for (int i = 0; i < NUM_QUICK_SLOTS; ++i) {
            if (quickitem[i] != null) {
                dest.writeBoolean(true);
                dest.writeUTF(quickitem[i].id);
            } else {
                dest.writeBoolean(false);
            }
        }

        int total = 0;
        for (int i = 0; i < presetsAll.length; i++){
            if(presetsAll[i] != null)
                total += presetsAll[i].size();
        }

        dest.writeInt(total);
        for (int i = 0; i < presetsAll.length; ++i) {
            if(presetsAll[i] != null){
                for (int j = 0; j < presetsAll[i].size(); j++) {
                dest.writeUTF(presetsAll[i].get(j).id);
                dest.writeByte(i + 1);
                }
            }
        }
    }
}
