package com.gpl.rpg.AndorsTrail.model.item;

import com.gpl.rpg.AndorsTrail.context.WorldContext;
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
    public String[] namePresets = new String[NUM_PRESETS];
    public int currentSelectedPreset = 0; // -1 means no preset

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

    public void assignToPreset(ItemType item, int presetIndex) {
        if (item == null) return;

        if (presetsAll[presetIndex] == null) {
            presetsAll[presetIndex] = new ArrayList<ItemType>();
            addToPreset(presetIndex, item);
            return;
        }

        if (!item.isEquippable()) {
            addToPreset(presetIndex, item);
            return;
        }
        boolean anotherWeapon = true;
        boolean anotherRing = true;
        WearSlot destSlot = item.category.inventorySlot;
        int neededSlots = 1;
        if (item.isTwohandWeapon())  neededSlots = 2;
        for (ItemType i : presetsAll[presetIndex]) {
            if(i == null) continue;
            if(i.needsWeaponSlot() && item.needsWeaponSlot()){
                if (getPresetWeaponSlotsUsed(presetIndex) >= neededSlots) {
                    presetsAll[presetIndex].remove(i);
                    break;
                }
            }
            else if((i.isRing()) && item.isRing()) {
                if(anotherRing) anotherRing = false;
                else{
                    presetsAll[presetIndex].remove(i);
                    break;
                }
            }
            else if (destSlot.equals(i.category.inventorySlot)) {
                    presetsAll[presetIndex].remove(i);
                    break;
            }
        }
        addToPreset(presetIndex, item);
    }

    public void addToPreset(int presetIndex, ItemType item) {
        if (presetsAll[presetIndex].indexOf(item) == -1)
            presetsAll[presetIndex].add(item);
    }

    public int getPresetWeaponSlotsUsed(int presetIndex){
        int slotsUsed = 0;
        for (ItemType i : presetsAll[presetIndex]) {
            if(!i.isEquippable()) continue;
            if(i.needsWeaponSlot()){
                if(i.isTwohandWeapon())
                    slotsUsed +=2;
                else
                    slotsUsed++;
            }
        }
        return slotsUsed;
    }

    // Move to item container?
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

    // Move to item container?
    public Inventory buildUsableItems() {
        Inventory usableItems = new Inventory();
        for (ItemEntry i : this.items) {
            if (i == null) break;
            if (i.itemType.isUsable())
                usableItems.items.add(i);
        }
        return usableItems;
    }

    // Move to item container?
    public Inventory buildWeaponItems() {
        Inventory weaponItems = new Inventory();
        for (ItemEntry i : this.items) {
            if (i == null) break;
            if (i.itemType.isWeapon())
                weaponItems.items.add(i);
        }
        return weaponItems;
    }

    // Move to item container?
    public Inventory buildArmorItems() {
        Inventory armorItems = new Inventory();
        for (ItemEntry i : this.items) {
            if (i == null) break;
            if (i.itemType.isEquippable() && !i.itemType.isWeapon())
                armorItems.items.add(i);
        }
        return armorItems;
    }

    // Move to item container?
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

    public ItemContainer getPresetItems(int presetIndex) {
        ItemContainer generatedPreset = new ItemContainer();
        if(presetIndex < 0 || presetIndex >= presetsAll.length) return generatedPreset;

        if (presetsAll[presetIndex] == null) presetsAll[presetIndex] = new ArrayList<>();
        //rebuildPresetIndices(presetNumber); // redundant, method assignToPreset doesn't do this after assign


        for (int i = 0; i < presetsAll[presetIndex].size(); i++)
            generatedPreset.addItem(presetsAll[presetIndex].get(i),
                    this.getItemQuantity(presetsAll[presetIndex].get(i).id));

        return generatedPreset;
    }

    public void unassignFromPresets(ItemType lastSelectedItem, int presetIndex) {
        if(presetIndex <0)
            for (int j = 0; j < presetsAll.length; j++)
                presetsAll[j].remove(lastSelectedItem);
        else if (presetIndex< presetsAll.length) presetsAll[presetIndex].remove(lastSelectedItem);

    }

    public void deletePreset(int currentWornPreset) {
        if (currentWornPreset<presetsAll.length && !(currentWornPreset <0))
            presetsAll[currentWornPreset] =  new ArrayList<>();
    }

    public String getCurrentPresetName() {
        getCurrentPresetIndex();
        if(currentSelectedPreset == -1 ) return "";
        return namePresets[getCurrentPresetIndex()];
    }

    public int getCurrentPresetIndex(){
        if(currentSelectedPreset <-1)
            currentSelectedPreset = namePresets.length -1;
        if(currentSelectedPreset >= namePresets.length)
            currentSelectedPreset = -1;
        return currentSelectedPreset;
    }

    public void setPresetNameByIndex(int presetIndex, String newName){
        if(presetIndex >= namePresets.length || presetIndex <0) return;
        namePresets[presetIndex] = newName;
    }
    public void setCurrentPresetNameByIndex(String newName){
        setPresetNameByIndex(currentSelectedPreset, newName);
    }

    public void resetCurrentPresetNameByIndex() {
        setPresetNameByIndex(currentSelectedPreset, "");
    }


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

        if (fileversion > 42) {
            for (int i = 0; i < NUM_PRESETS; i++)
                presetsAll[i] = new ArrayList<>();
            final int presetItems = src.readInt();
            for (int i = 0; i < presetItems; i++) {
                assignToPreset(world.itemTypes.getItemType(src.readUTF()), src.readByte());
            }
        }

        if(fileversion>43){
            int presetNb = src.readInt();
            namePresets = new String[presetNb];
            for(int i=0; i<presetNb; i++){
                int nameLength = src.readInt();
                this.namePresets[i] = "";
                for (int j=0; j<nameLength; j++){
                    this.namePresets[i] += src.readChar();
                }
            }
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
        for (int i = 0; i < presetsAll.length; i++) {
            if (presetsAll[i] != null)
                total += presetsAll[i].size();
        }

        dest.writeInt(total);
        for (int i = 0; i < presetsAll.length; ++i) {
            if (presetsAll[i] != null) {
                for (int j = 0; j < presetsAll[i].size(); j++) {
                    dest.writeUTF(presetsAll[i].get(j).id);
                    dest.writeByte(i);
                }
            }
        }

        dest.writeInt(namePresets.length);
        for(int i=0; i<namePresets.length; i++){
            if(namePresets[i] == null)
                dest.writeInt(0);
            else {
                dest.writeInt(namePresets[i].length());
                dest.writeChars(namePresets[i]);
            }
        }
    }
}
