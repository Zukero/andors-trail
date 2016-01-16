package com.gpl.rpg.AndorsTrail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.gpl.rpg.AndorsTrail.activity.*;
import com.gpl.rpg.AndorsTrail.activity.fragment.HeroinfoActivity_Inventory;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionType;
import com.gpl.rpg.AndorsTrail.model.ability.SkillCollection;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.model.item.Loot;
import com.gpl.rpg.AndorsTrail.model.map.MapObject;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileManager;
import com.gpl.rpg.AndorsTrail.view.ItemContainerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class Dialogs {

	public static final String repeatFiller =  "%1$s";

	private static void showDialogAndPause(Dialog d, final ControllerContext context) {
		showDialogAndPause(d, context, null);
	}
	private static void showDialogAndPause(Dialog d, final ControllerContext context, final OnDismissListener onDismiss) {
		context.gameRoundController.pause();
		d.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				if (onDismiss != null) onDismiss.onDismiss(arg0);
				context.gameRoundController.resume();
			}
		});
		d.show();
	}

	public static void showKeyArea(final MainActivity currentActivity, final ControllerContext context, String phraseID) {
		showConversation(currentActivity, context, phraseID, null);
	}

	public static void showMapSign(final MainActivity currentActivity, final ControllerContext context, String phraseID) {
		showConversation(currentActivity, context, phraseID, null);
	}

	public static void showMapScriptMessage(final MainActivity currentActivity, final ControllerContext context, String phraseID) {
		showConversation(currentActivity, context, phraseID, null, false);
	}

	public static void showConversation(final MainActivity currentActivity, final ControllerContext context, final String phraseID, final Monster npc) {
		showConversation(currentActivity, context, phraseID, npc, true);
	}

	private static void showConversation(final MainActivity currentActivity, final ControllerContext context, final String phraseID, final Monster npc, boolean applyScriptEffectsForFirstPhrase) {
		context.gameRoundController.pause();
		Intent intent = new Intent(currentActivity, ConversationActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/conversation/" + phraseID));
		intent.putExtra("applyScriptEffectsForFirstPhrase", applyScriptEffectsForFirstPhrase);
		addMonsterIdentifiers(intent, npc);
		currentActivity.startActivityForResult(intent, MainActivity.INTENTREQUEST_CONVERSATION);
	}

	public static void addMonsterIdentifiers(Intent intent, Monster monster) {
		if (monster == null) return;
		intent.putExtra("x", monster.position.x);
		intent.putExtra("y", monster.position.y);
	}
	public static void addMonsterIdentifiers(Bundle bundle, Monster monster) {
		if (monster == null) return;
		bundle.putInt("x", monster.position.x);
		bundle.putInt("y", monster.position.y);
	}

	public static Monster getMonsterFromIntent(Intent intent, final WorldContext world) {
		return getMonsterFromBundle(intent.getExtras(), world);
	}
	public static Monster getMonsterFromBundle(Bundle params, final WorldContext world) {
		if (params == null) return null;
		if (!params.containsKey("x")) return null;
		int x = params.getInt("x");
		int y = params.getInt("y");
		return world.model.currentMap.getMonsterAt(x, y);
	}

	public static void showMonsterEncounter(final MainActivity currentActivity, final ControllerContext context, final Monster monster) {
		context.gameRoundController.pause();
		Intent intent = new Intent(currentActivity, MonsterEncounterActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/monsterencounter"));
		addMonsterIdentifiers(intent, monster);
		currentActivity.startActivityForResult(intent, MainActivity.INTENTREQUEST_MONSTERENCOUNTER);
	}

	public static void showMonsterInfo(final Context context, final Monster monster) {
		Intent intent = new Intent(context, MonsterInfoActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/monsterinfo"));
		addMonsterIdentifiers(intent, monster);
		context.startActivity(intent);
	}

	public static String getGroundLootFoundMessage(final Context ctx, final Loot loot) {
		StringBuilder sb = new StringBuilder(60);
		if (!loot.items.isEmpty()) {
			sb.append(ctx.getString(R.string.dialog_groundloot_message));
		}
		appendGoldPickedUpMessage(ctx, loot, sb);
		return sb.toString();
	}
	public static String getGroundLootPickedUpMessage(final Context ctx, final Loot loot) {
		StringBuilder sb = new StringBuilder(60);
		appendLootPickedUpMessage(ctx, loot, sb);
		return sb.toString();
	}
	public static String getMonsterLootFoundMessage(final Context ctx, final Loot combinedLoot, final int exp) {
		StringBuilder sb = new StringBuilder(60);
		appendMonsterEncounterSurvivedMessage(ctx, sb, exp);
		appendGoldPickedUpMessage(ctx, combinedLoot, sb);
		return sb.toString();
	}
	public static String getMonsterLootPickedUpMessage(final Context ctx, final Loot combinedLoot, final int exp) {
		StringBuilder sb = new StringBuilder(60);
		appendMonsterEncounterSurvivedMessage(ctx, sb, exp);
		appendLootPickedUpMessage(ctx, combinedLoot, sb);
		return sb.toString();
	}
	private static void appendMonsterEncounterSurvivedMessage(final Context ctx, final StringBuilder sb, final int exp) {
		sb.append(ctx.getString(R.string.dialog_monsterloot_message));
		if (exp > 0) {
			sb.append(' ');
			sb.append(ctx.getString(R.string.dialog_monsterloot_gainedexp, exp));
		}
	}
	private static void appendGoldPickedUpMessage(final Context ctx, final Loot loot, final StringBuilder sb) {
		if (loot.gold > 0) {
			sb.append(' ');
			sb.append(ctx.getString(R.string.dialog_loot_foundgold, loot.gold));
		}
	}
	private static void appendLootPickedUpMessage(final Context ctx, final Loot loot, final StringBuilder sb) {
		appendGoldPickedUpMessage(ctx, loot, sb);
		int numItems = loot.items.countItems();
		if (numItems == 1) {
			sb.append(' ');
			sb.append(ctx.getString(R.string.dialog_loot_pickedupitem));
		} else if (numItems > 1){
			sb.append(' ');
			sb.append(ctx.getString(R.string.dialog_loot_pickedupitems, numItems));
		}
	}

	public static void showMonsterLoot(final MainActivity mainActivity, final ControllerContext controllers, final WorldContext world, final Collection<Loot> lootBags, final Loot combinedLoot, final String msg) {
		// CombatController will do killedMonsterBags.clear() after this method has been called,
		// so we need to keep the list of objects. Therefore, we create a shallow copy of the list of bags.
		ArrayList<Loot> bags = new ArrayList<Loot>(lootBags);
		showLoot(mainActivity, controllers, world, combinedLoot, bags, R.string.dialog_monsterloot_title, msg);
	}

	public static void showGroundLoot(final MainActivity mainActivity, final ControllerContext controllers, final WorldContext world, final Loot loot, final String msg) {
		showLoot(mainActivity, controllers, world, loot, Collections.singletonList(loot), R.string.dialog_groundloot_title, msg);
 	}

	private static void showLoot(final MainActivity mainActivity, final ControllerContext controllers, final WorldContext world, final Loot combinedLoot, final Iterable<Loot> lootBags, final int title, final String msg) {
		final ListView itemList = new ListView(mainActivity);
		itemList.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
		itemList.setPadding(20, 0, 20, 20);
		itemList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				final String itemTypeID = ((ItemContainerAdapter) parent.getAdapter()).getItem(position).itemType.id;
				boolean removeFromCombinedLoot = true;
				for (Loot l : lootBags) {
					if (l == combinedLoot) removeFromCombinedLoot = false;
					if (l.items.removeItem(itemTypeID)) {
						controllers.itemController.removeLootBagIfEmpty(l);
						break;
					}
				}
				if (removeFromCombinedLoot) combinedLoot.items.removeItem(itemTypeID);
				ItemType type = world.itemTypes.getItemType(itemTypeID);
				world.model.player.inventory.addItem(type);
				((ItemContainerAdapter) itemList.getAdapter()).notifyDataSetChanged();
			}
		});
		itemList.setAdapter(new ItemContainerAdapter(mainActivity, world.tileManager, combinedLoot.items, world.model.player));

		AlertDialog.Builder db = new AlertDialog.Builder(mainActivity)
		.setTitle(title)
		.setMessage(msg)
		.setIcon(new BitmapDrawable(mainActivity.getResources(), world.tileManager.preloadedTiles.getBitmap(TileManager.iconID_groundbag)))
		.setNegativeButton(R.string.dialog_close, null)
		.setView(itemList);

		if (!combinedLoot.items.isEmpty()) {
			db.setPositiveButton(R.string.dialog_loot_pickall, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					controllers.itemController.pickupAll(lootBags);
				}
			});
		}

		final Dialog d = db.create();

		showDialogAndPause(d, controllers, new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				controllers.itemController.removeLootBagIfEmpty(lootBags);
			}
		});
	}

	public static Intent getIntentForItemInfo(final Context ctx, String itemTypeID, ItemInfoActivity.ItemInfoAction actionType, String buttonText, boolean buttonEnabled, Inventory.WearSlot inventorySlot) {
		Intent intent = new Intent(ctx, ItemInfoActivity.class);
		intent.putExtra("buttonText", buttonText);
		intent.putExtra("buttonEnabled", buttonEnabled);
		intent.putExtra("itemTypeID", itemTypeID);
		intent.putExtra("actionType", actionType.name());
		if (inventorySlot != null) intent.putExtra("inventorySlot", inventorySlot.name());
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/iteminfo/" + itemTypeID));
		return intent;
	}
	public static Intent getIntentForLevelUp(final Context ctx) {
		Intent intent = new Intent(ctx, LevelUpActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/levelup"));
		return intent;
	}

	public static void showConfirmRest(final Activity currentActivity, final ControllerContext controllerContext, final MapObject area) {
		Dialog d = new AlertDialog.Builder(currentActivity)
		.setTitle(R.string.dialog_rest_title)
		.setMessage(R.string.dialog_rest_confirm_message)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				controllerContext.mapController.rest(area);
			}
		})
		.setNegativeButton(android.R.string.no, null)
		.create();

		showDialogAndPause(d, controllerContext);
	}
	public static void showRested(final Activity currentActivity, final ControllerContext controllerContext) {
		Dialog d = new AlertDialog.Builder(currentActivity)
		.setTitle(R.string.dialog_rest_title)
		.setMessage(R.string.dialog_rest_message)
		.setNeutralButton(android.R.string.ok, null)
		.create();

		showDialogAndPause(d, controllerContext);
	}

	public static void showNewVersion(final Activity currentActivity) {
		new AlertDialog.Builder(currentActivity)
		.setTitle(R.string.dialog_newversion_title)
		.setMessage(R.string.dialog_newversion_message)
		.setNeutralButton(android.R.string.ok, null)
		.show();
	}

	public static boolean showSave(final Activity mainActivity, final ControllerContext controllerContext, final WorldContext world) {
		if (world.model.uiSelections.isInCombat) {
			Toast.makeText(mainActivity, R.string.menu_save_saving_not_allowed_in_combat, Toast.LENGTH_SHORT).show();
			return false;
		}
		controllerContext.gameRoundController.pause();
		Intent intent = new Intent(mainActivity, LoadSaveActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/save"));
		mainActivity.startActivityForResult(intent, MainActivity.INTENTREQUEST_SAVEGAME);
		return true;
	}

	public static void showLoad(final Activity currentActivity) {
		Intent intent = new Intent(currentActivity, LoadSaveActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/load"));
		currentActivity.startActivityForResult(intent, StartScreenActivity.INTENTREQUEST_LOADGAME);
	}

	public static Intent getIntentForPresetLoad(final Activity currentActivity) {
		Intent intent = new Intent(currentActivity, LoadSavePresetActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/preset_load"));
		return intent;
	}
	public static void showPresetLoad(final Activity mainActivity){
		Intent intent = new Intent(mainActivity, LoadSavePresetActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/preset_load_combat"));
		mainActivity.startActivityForResult(intent, HeroinfoActivity_Inventory.INTENTREQUEST_PRESETLOAD);
	}
	public static Intent getIntentForPresetSave(final Activity currentActivity) {
		Intent intent = new Intent(currentActivity, LoadSavePresetActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/preset_save"));
		return intent;
	}
	public static Intent getIntentForPresetDelete(final Activity currentActivity) {
		Intent intent = new Intent(currentActivity, LoadSavePresetActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/preset_delete"));
		return intent;
	}

	public static void showActorConditionInfo(final Context context, ActorConditionType conditionType) {
		Intent intent = new Intent(context, ActorConditionInfoActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/actorconditioninfo/" + conditionType.conditionTypeID));
		context.startActivity(intent);
	}

	public static Intent getIntentForBulkBuyingInterface(final Context ctx, String itemTypeID, int totalAvailableAmount) {
		return getIntentForBulkSelectionInterface(ctx, itemTypeID, totalAvailableAmount, BulkSelectionInterface.BulkInterfaceType.buy);
	}

	public static Intent getIntentForBulkSellingInterface(final Context ctx, String itemTypeID, int totalAvailableAmount) {
		return getIntentForBulkSelectionInterface(ctx, itemTypeID, totalAvailableAmount, BulkSelectionInterface.BulkInterfaceType.sell);
	}

	public static Intent getIntentForBulkDroppingInterface(final Context ctx, String itemTypeID, int totalAvailableAmount) {
		return getIntentForBulkSelectionInterface(ctx, itemTypeID, totalAvailableAmount, BulkSelectionInterface.BulkInterfaceType.drop);
	}

	private static Intent getIntentForBulkSelectionInterface(final Context ctx, String itemTypeID, int totalAvailableAmount, BulkSelectionInterface.BulkInterfaceType interfaceType) {
		Intent intent = new Intent(ctx, BulkSelectionInterface.class);
		intent.putExtra("itemTypeID", itemTypeID);
		intent.putExtra("totalAvailableAmount", totalAvailableAmount);
		intent.putExtra("interfaceType", interfaceType.name());
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/bulkselection/" + itemTypeID));
		return intent;
	}
	public static Intent getIntentForSkillInfo(final Context ctx, SkillCollection.SkillID skillID) {
		Intent intent = new Intent(ctx, SkillInfoActivity.class);
		intent.putExtra("skillID", skillID.name());
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/showskillinfo/" + skillID));
		return intent;
	}

	public static void showCombatLog(final Context context, final ControllerContext controllerContext, final WorldContext world) {
		final String[] combatLogMessages = world.model.combatLog.getAllMessages();

		final ListView itemList = new ListView(context);
		itemList.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
		itemList.setPadding(20, 0, 20, 20);
		itemList.setStackFromBottom(true);
		itemList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		itemList.setChoiceMode(ListView.CHOICE_MODE_NONE);
		itemList.setAdapter(new ArrayAdapter<String>(context, R.layout.combatlog_row, android.R.id.text1, combatLogMessages));

		final Dialog d = new AlertDialog.Builder(context)
				.setTitle(R.string.combat_log_title)
				.setIcon(R.drawable.ui_icon_combat)
				.setNegativeButton(R.string.dialog_close, null)
				.setView(itemList)
				.create();

		showDialogAndPause(d, controllerContext);
	}
}
