package com.gpl.rpg.AndorsTrail.activity.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.Dialogs;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.activity.ItemInfoActivity;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.ItemController;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileCollection;
import com.gpl.rpg.AndorsTrail.view.ItemContainerAdapter;

public final class HeroinfoActivity_Inventory extends Fragment {

	private static final int INTENTREQUEST_ITEMINFO = 3;
	private static final int INTENTREQUEST_BULKSELECT_DROP = 11;

	private WorldContext world;
	private ControllerContext controllers;
	private TileCollection wornTiles;

	private Player player;
	private ListView inventoryList;
	private Spinner inventorylist_categories;
	private Spinner inventorylist_sort;
	private ItemContainerAdapter inventoryListAdapter;
	private ItemContainerAdapter inventoryFavoritesListAdapter;
	private ItemContainerAdapter inventoryWeaponsListAdapter;
	private ItemContainerAdapter inventoryArmorListAdapter;
	private ItemContainerAdapter inventoryUsableListAdapter;
	private ItemContainerAdapter inventoryQuestListAdapter;
	private ItemContainerAdapter inventoryOtherListAdapter;

	private Button inventory_preset_button;

	private TextView preset_quickswitch_save;
	private TextView preset_quickswitch_load;
	private TextView preset_quickswitch_delete;


	private TextView heroinfo_stats_gold;
	private TextView heroinfo_stats_attack;
	private TextView heroinfo_stats_defense;

	private ItemType lastSelectedItem; // Workaround android bug #7139

	private final ImageView[] wornItemImage = new ImageView[Inventory.WearSlot.values().length];
	private final int[] defaultWornItemImageResourceIDs = new int[Inventory.WearSlot.values().length];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this.getActivity());
		if (!app.isInitialized()) return;
		this.world = app.getWorld();
		this.controllers = app.getControllerContext();
		this.player = world.model.player;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.heroinfo_inventory, container, false);

		initialiseInventorySpinners(v);

		inventoryList = (ListView) v.findViewById(R.id.inventorylist_root);
		registerForContextMenu(inventoryList);
		inventoryList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				// Move this code to separate function? -- Done
				ItemType itemType = getSelectedItemType(position);
				showInventoryItemInfo(itemType.id);
			}
		});

		ItemContainer inv = player.inventory;
		wornTiles = world.tileManager.loadTilesFor(player.inventory, getResources());
		inventoryListAdapter = new ItemContainerAdapter(getActivity(), world.tileManager, inv, player, wornTiles);
		inventoryList.setAdapter(inventoryListAdapter);

		heroinfo_stats_gold = (TextView) v.findViewById(R.id.heroinfo_stats_gold);
		heroinfo_stats_attack = (TextView) v.findViewById(R.id.heroinfo_stats_attack);
		heroinfo_stats_defense = (TextView) v.findViewById(R.id.heroinfo_stats_defense);

		setWearSlot(v, Inventory.WearSlot.weapon, R.id.heroinfo_worn_weapon, R.drawable.equip_weapon);
		setWearSlot(v, Inventory.WearSlot.shield, R.id.heroinfo_worn_shield, R.drawable.equip_shield);
		setWearSlot(v, Inventory.WearSlot.head, R.id.heroinfo_worn_head, R.drawable.equip_head);
		setWearSlot(v, Inventory.WearSlot.body, R.id.heroinfo_worn_body, R.drawable.equip_body);
		setWearSlot(v, Inventory.WearSlot.feet, R.id.heroinfo_worn_feet, R.drawable.equip_feet);
		setWearSlot(v, Inventory.WearSlot.neck, R.id.heroinfo_worn_neck, R.drawable.equip_neck);
		setWearSlot(v, Inventory.WearSlot.hand, R.id.heroinfo_worn_hand, R.drawable.equip_hand);
		setWearSlot(v, Inventory.WearSlot.leftring, R.id.heroinfo_worn_ringleft, R.drawable.equip_ring);
		setWearSlot(v, Inventory.WearSlot.rightring, R.id.heroinfo_worn_ringright, R.drawable.equip_ring);


        initialisePresetControls(v);

		return v;
	}

    private void initialiseInventorySpinners(View v) {
        inventorylist_categories = (Spinner) v.findViewById(R.id.inventorylist_category_filters);
        ArrayAdapter<CharSequence> categoryFilterAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inventorylist_category_filters, android.R.layout.simple_spinner_item);
        categoryFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inventorylist_categories.setAdapter(categoryFilterAdapter);
        inventorylist_categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				world.model.uiSelections.selectedInventoryCategory = inventorylist_categories.getSelectedItemPosition();
				reloadShownCategory(world.model.uiSelections.selectedInventoryCategory);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				world.model.uiSelections.selectedInventoryCategory = 0;
			}
		});
        inventorylist_categories.setSelection(world.model.uiSelections.selectedInventoryCategory);


        inventorylist_sort = (Spinner) v.findViewById(R.id.inventorylist_sort_filters);
        ArrayAdapter<CharSequence> sortFilterAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inventorylist_sort_filters, android.R.layout.simple_spinner_item);
        sortFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inventorylist_sort.setAdapter(sortFilterAdapter);
        inventorylist_sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				world.model.uiSelections.selectedInventorySort = inventorylist_sort.getSelectedItemPosition();
				reloadShownSort(player.inventory);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Reset to "Custom" position
				world.model.uiSelections.selectedInventorySort = 0;
			}
		});
        inventorylist_sort.setSelection(world.model.uiSelections.selectedInventorySort);
    }

    private void initialisePresetControls(View v) {
        inventory_preset_button = (Button) v.findViewById(R.id.inventory_preset_button);

        preset_quickswitch_save = (TextView) v.findViewById(R.id.preset_quickswitch_save);
        preset_quickswitch_load = (TextView) v.findViewById(R.id.preset_quickswitch_load);
        preset_quickswitch_delete = (TextView) v.findViewById(R.id.preset_quickswitch_delete);

		inventory_preset_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				player.inventory.currentSelectedPreset++;
				controllers.itemController.equipNextPreset(player);
				update();
			}
		});

        inventory_preset_button.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (!world.model.uiSelections.presetListVisible) {
					world.model.uiSelections.presetListVisible = true;
					setHeroStatsVisiblity(View.GONE);
					setPresetListVisibility(View.VISIBLE);
					return true;
				}
				if (world.model.uiSelections.presetListVisible) {
					world.model.uiSelections.presetListVisible = false;
					setHeroStatsVisiblity(View.VISIBLE);
					setPresetListVisibility(View.GONE);
				}
				return true;
			}
		});

    }

    private void setPresetListVisibility(int visibility) {
		preset_quickswitch_save.setVisibility(visibility);
		preset_quickswitch_load.setVisibility(visibility);
		preset_quickswitch_delete.setVisibility(visibility);

		preset_quickswitch_save.setText(world.model.player.inventory.namePresets[0]);
		preset_quickswitch_load.setText(world.model.player.inventory.namePresets[1]);
		preset_quickswitch_delete.setText(world.model.player.inventory.namePresets[2]);

		// For boxes to seem equal & transitions smoother
		if (preset_quickswitch_save.getHeight() < heroinfo_stats_gold.getHeight())
			preset_quickswitch_save.setHeight(heroinfo_stats_gold.getHeight());
		if (preset_quickswitch_load.getHeight() < heroinfo_stats_gold.getHeight())
			preset_quickswitch_load.setHeight(heroinfo_stats_gold.getHeight());
		if (preset_quickswitch_delete.getHeight() < heroinfo_stats_gold.getHeight())
			preset_quickswitch_delete.setHeight(heroinfo_stats_gold.getHeight());
	}

	private void setHeroStatsVisiblity(int visibility) {
		heroinfo_stats_gold.setVisibility(visibility);
		heroinfo_stats_attack.setVisibility(visibility);
		heroinfo_stats_defense.setVisibility(visibility);
	}

	@Override
	public void onStart() {
		super.onStart();
		update();
	}

	private void setWearSlot(final View v, final Inventory.WearSlot inventorySlot, int viewId, int resourceId) {
		final ImageView imageView = (ImageView) v.findViewById(viewId);
		wornItemImage[inventorySlot.ordinal()] = imageView;
		defaultWornItemImageResourceIDs[inventorySlot.ordinal()] = resourceId;
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (player.inventory.isEmptySlot(inventorySlot)) return;
				imageView.setClickable(false); // Will be enabled again on update()
				showEquippedItemInfo(player.inventory.getItemTypeInWearSlot(inventorySlot), inventorySlot);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case INTENTREQUEST_ITEMINFO:
				if (resultCode != Activity.RESULT_OK) break;

				ItemType itemType = world.itemTypes.getItemType(data.getExtras().getString("itemTypeID"));
				ItemInfoActivity.ItemInfoAction actionType = ItemInfoActivity.ItemInfoAction.valueOf(data.getExtras().getString("actionType"));
				if (actionType == ItemInfoActivity.ItemInfoAction.unequip) {
					Inventory.WearSlot slot = Inventory.WearSlot.valueOf(data.getExtras().getString("inventorySlot"));
					controllers.itemController.unequipSlot(itemType, slot);
				} else if (actionType == ItemInfoActivity.ItemInfoAction.equip) {
					Inventory.WearSlot slot = suggestInventorySlot(itemType);
					controllers.itemController.equipItem(itemType, slot);
				} else if (actionType == ItemInfoActivity.ItemInfoAction.use) {
					controllers.itemController.useItem(itemType);
				}
				break;
			case INTENTREQUEST_BULKSELECT_DROP:
				if (resultCode != Activity.RESULT_OK) break;

				int quantity = data.getExtras().getInt("selectedAmount");
				String itemTypeID = data.getExtras().getString("itemTypeID");
				dropItem(itemTypeID, quantity);
				break;
		}
		update();
	}

	private Inventory.WearSlot suggestInventorySlot(ItemType itemType) {
		Inventory.WearSlot slot = itemType.category.inventorySlot;
		if (player.inventory.isEmptySlot(slot)) return slot;

		if (slot == Inventory.WearSlot.leftring) return Inventory.WearSlot.rightring;
		if (itemType.isOffhandCapableWeapon()) {
			ItemType mainWeapon = player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.weapon);
			if (mainWeapon != null && mainWeapon.isTwohandWeapon()) return slot;
			else if (player.inventory.isEmptySlot(Inventory.WearSlot.shield))
				return Inventory.WearSlot.shield;
		}
		return slot;
	}

	private void dropItem(String itemTypeID, int quantity) {
		ItemType itemType = world.itemTypes.getItemType(itemTypeID);
		controllers.itemController.dropItem(itemType, quantity);
	}

	private void update() {
		updateTraits();
		updateWorn();
		updateItemList();
	}

	private void updateTraits() {
		heroinfo_stats_gold.setText(getResources().getString(R.string.heroinfo_gold, player.inventory.gold));

		StringBuilder sb = new StringBuilder(10);
		ItemController.describeAttackEffect(
				player.getAttackChance(),
				player.getDamagePotential().current,
				player.getDamagePotential().max,
				player.getCriticalSkill(),
				player.getCriticalMultiplier(),
				sb);
		heroinfo_stats_attack.setText(sb.toString());

		sb = new StringBuilder(10);
		ItemController.describeBlockEffect(player.getBlockChance(), player.getDamageResistance(), sb);
		heroinfo_stats_defense.setText(sb.toString());
	}

	private void updateWorn() {
		for (Inventory.WearSlot slot : Inventory.WearSlot.values()) {
			updateWornImage(wornItemImage[slot.ordinal()], defaultWornItemImageResourceIDs[slot.ordinal()], player.inventory.getItemTypeInWearSlot(slot));
		}
	}


	private void updateWornImage(ImageView imageView, int resourceIDEmptyImage, ItemType type) {
		if (type != null) {
			world.tileManager.setImageViewTile(getResources(), imageView, type, wornTiles);
		} else {
			imageView.setImageResource(resourceIDEmptyImage);
		}
		imageView.setClickable(true);
	}

	private void updateItemList() {
		int currentScreen = world.model.uiSelections.selectedInventoryCategory;
		if (currentScreen == 0)
			inventoryListAdapter.notifyDataSetChanged();
		else
			reloadShownCategory(world.model.uiSelections.selectedInventoryCategory);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ItemType type = getSelectedItemType((AdapterContextMenuInfo) menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		switch (v.getId()) {
			case R.id.inventorylist_root:
				inflater.inflate(R.menu.inventoryitem, menu);
				if (type.isUsable()) {
					menu.findItem(R.id.inv_menu_use).setVisible(true);
					menu.findItem(R.id.inv_menu_assign).setVisible(true);
				}
				if (type.isEquippable()) {
					menu.findItem(R.id.inv_menu_equip).setVisible(true);
					if (type.isOffhandCapableWeapon())
						menu.findItem(R.id.inv_menu_equip_offhand).setVisible(true);
					else if (type.category.inventorySlot == Inventory.WearSlot.leftring)
						menu.findItem(R.id.inv_menu_equip_offhand).setVisible(true);
				}
				break;
			case R.id.inv_menu_assign:
				menu.findItem(R.id.inv_assign_slot1).setTitle(world.model.player.inventory.namePresets[0]);
				menu.findItem(R.id.inv_assign_slot2).setTitle(world.model.player.inventory.namePresets[1]);
				menu.findItem(R.id.inv_assign_slot3).setTitle(world.model.player.inventory.namePresets[2]);
				break;

		}
		lastSelectedItem = null;
	}

	private ItemType getSelectedItemType(int position) {
		int v = world.model.uiSelections.selectedInventoryCategory;

		if (v == 0) { //All items
			return inventoryListAdapter.getItem(position).itemType;
		} else if (v == 2) { //Weapon items
			return inventoryWeaponsListAdapter.getItem(position).itemType;
		} else if (v == 3) { //Armor items
			return inventoryArmorListAdapter.getItem(position).itemType;
		} else if (v == 4) { //Usable items
			return inventoryUsableListAdapter.getItem(position).itemType;
		} else if (v == 5) { //Quest items
			return inventoryQuestListAdapter.getItem(position).itemType;
		} else if (v == 6) { //Other items
			return inventoryOtherListAdapter.getItem(position).itemType;
		}

		// Better than crashing...
		return inventoryListAdapter.getItem(position).itemType;

	}


	private ItemType getSelectedItemType(AdapterContextMenuInfo info) {

		return getSelectedItemType(info.position);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ItemType itemType;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.inv_menu_info:
				showInventoryItemInfo(getSelectedItemType(info));
				//context.mapController.itemInfo(this, getSelectedItemType(info));
				break;
			case R.id.inv_menu_drop:
				String itemTypeID = getSelectedItemType(info).id;
				int quantity = player.inventory.getItemQuantity(itemTypeID);
				if (quantity > 1) {
					Intent intent = Dialogs.getIntentForBulkDroppingInterface(getActivity(), itemTypeID, quantity);
					startActivityForResult(intent, INTENTREQUEST_BULKSELECT_DROP);
				} else {
					dropItem(itemTypeID, quantity);
				}
				break;
			case R.id.inv_menu_equip:
				itemType = getSelectedItemType(info);
				controllers.itemController.equipItem(itemType, itemType.category.inventorySlot);
				break;
			case R.id.inv_menu_equip_offhand:
				itemType = getSelectedItemType(info);
				if (itemType.category.inventorySlot == Inventory.WearSlot.weapon) {
					controllers.itemController.equipItem(itemType, Inventory.WearSlot.shield);
				} else if (itemType.category.inventorySlot == Inventory.WearSlot.leftring) {
					controllers.itemController.equipItem(itemType, Inventory.WearSlot.rightring);
				}
				break;
		/*case R.id.inv_menu_unequip:
			context.mapController.unequipItem(this, getSelectedItemType(info));
			break;*/
			case R.id.inv_menu_use:
				controllers.itemController.useItem(getSelectedItemType(info));
				break;
			case R.id.inv_menu_assign: // More code duplication...
				lastSelectedItem = getSelectedItemType(info);
				break;
			case R.id.inv_assign_slot1:
				controllers.itemController.setQuickItem(lastSelectedItem, 0);
				break;
			case R.id.inv_assign_slot2:
				controllers.itemController.setQuickItem(lastSelectedItem, 1);
				break;
			case R.id.inv_assign_slot3:
				controllers.itemController.setQuickItem(lastSelectedItem, 2);
				break;
			case R.id.inv_menu_movetop:
				player.inventory.sortToTop(getSelectedItemType(info).id);
				break;
			case R.id.inv_menu_movebottom:
				player.inventory.sortToBottom(getSelectedItemType(info).id);
				break;
			case R.id.inv_menu_add_favorites:
				lastSelectedItem = getSelectedItemType(info);
				player.inventory.addToFavorites(lastSelectedItem);
				updateItemList();
				break;
			case R.id.inv_menu_remove_favorites:
				lastSelectedItem = getSelectedItemType(info);
				player.inventory.removeFromFavorites(lastSelectedItem);
				updateItemList();
				break;
			default:
				return super.onContextItemSelected(item);
		}
		update();
		return true;
	}

	private void showEquippedItemInfo(ItemType itemType, Inventory.WearSlot inventorySlot) {
		String text;
		boolean enabled = true;

		if (world.model.uiSelections.isInCombat) {
			int ap = world.model.player.getReequipCost();
			text = getResources().getString(R.string.iteminfo_action_unequip_ap, ap);
			if (ap > 0) {
				enabled = world.model.player.hasAPs(ap);
			}
		} else {
			text = getResources().getString(R.string.iteminfo_action_unequip);
		}
		Intent intent = Dialogs.getIntentForItemInfo(getActivity(), itemType.id, ItemInfoActivity.ItemInfoAction.unequip, text, enabled, inventorySlot);
		startActivityForResult(intent, INTENTREQUEST_ITEMINFO);
	}

	private void showInventoryItemInfo(String itemTypeID) {
		showInventoryItemInfo(world.itemTypes.getItemType(itemTypeID));
	}

	private void showInventoryItemInfo(ItemType itemType) {
		String text = "";
		int ap = 0;
		boolean enabled = true;
		ItemInfoActivity.ItemInfoAction action = ItemInfoActivity.ItemInfoAction.none;
		final boolean isInCombat = world.model.uiSelections.isInCombat;
		if (itemType.isEquippable()) {
			if (isInCombat) {
				ap = world.model.player.getReequipCost();
				text = getResources().getString(R.string.iteminfo_action_equip_ap, ap);
			} else {
				text = getResources().getString(R.string.iteminfo_action_equip);
			}
			action = ItemInfoActivity.ItemInfoAction.equip;
		} else if (itemType.isUsable()) {
			if (isInCombat) {
				ap = world.model.player.getUseItemCost();
				text = getResources().getString(R.string.iteminfo_action_use_ap, ap);
			} else {
				text = getResources().getString(R.string.iteminfo_action_use);
			}
			action = ItemInfoActivity.ItemInfoAction.use;
		}
		if (isInCombat && ap > 0) {
			enabled = world.model.player.hasAPs(ap);
		}

		Intent intent = Dialogs.getIntentForItemInfo(getActivity(), itemType.id, action, text, enabled, null);
		startActivityForResult(intent, INTENTREQUEST_ITEMINFO);
	}

	private void reloadShownCategory(int v) { // Apologies about the code duplication,
		// just didn't seem to make sense as an array, although I did create a nice array for skill category adapters.

		// Decide which category to show
		if (v == 0) { //All items
			inventoryList.setAdapter(inventoryListAdapter);
			inventoryListAdapter.notifyDataSetChanged();
		} else if (v == 1) { // Favorite items
			inventoryFavoritesListAdapter = new ItemContainerAdapter(getActivity(), world.tileManager, player.inventory.buildFavoriteItems(), player, wornTiles);
			inventoryList.setAdapter(inventoryFavoritesListAdapter);
			inventoryFavoritesListAdapter.notifyDataSetChanged();
		} else if (v == 2) { //Weapon items
			inventoryWeaponsListAdapter = new ItemContainerAdapter(getActivity(), world.tileManager, player.inventory.buildWeaponItems(), player, wornTiles);
			inventoryList.setAdapter(inventoryWeaponsListAdapter);
			inventoryWeaponsListAdapter.notifyDataSetChanged();
		} else if (v == 3) { //Armor items
			inventoryArmorListAdapter = new ItemContainerAdapter(getActivity(), world.tileManager, player.inventory.buildArmorItems(), player, wornTiles);
			inventoryList.setAdapter(inventoryArmorListAdapter);
			inventoryArmorListAdapter.notifyDataSetChanged();
		} else if (v == 4) { //Usable items
			inventoryUsableListAdapter = new ItemContainerAdapter(getActivity(), world.tileManager, player.inventory.buildUsableItems(), player, wornTiles);
			inventoryList.setAdapter(inventoryUsableListAdapter);
			inventoryUsableListAdapter.notifyDataSetChanged();
		} else if (v == 5) { //Quest items
			inventoryQuestListAdapter = new ItemContainerAdapter(getActivity(), world.tileManager, player.inventory.buildQuestItems(), player, wornTiles);
			inventoryList.setAdapter(inventoryQuestListAdapter);
			inventoryQuestListAdapter.notifyDataSetChanged();
		} else if (v == 6) { //Other items
			inventoryOtherListAdapter = new ItemContainerAdapter(getActivity(), world.tileManager, player.inventory.buildOtherItems(), player, wornTiles);
			inventoryList.setAdapter(inventoryOtherListAdapter);
			inventoryOtherListAdapter.notifyDataSetChanged();
		}
		//updateItemList();
	}

	private void reloadShownSort(Inventory inv) {
		int selected = world.model.uiSelections.selectedInventorySort;

		inventoryListAdapter.reloadShownSort(selected, world.model.uiSelections.oldSortSelection, player.inventory, player);

		// Currently not functional, perhaps because selection only updates when changed.
		if (world.model.uiSelections.oldSortSelection == selected)
			world.model.uiSelections.oldSortSelection = 0;
		else world.model.uiSelections.oldSortSelection = selected;
		updateItemList();
	}

}