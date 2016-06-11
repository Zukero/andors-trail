package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.AndorsTrailPreferences;
import com.gpl.rpg.AndorsTrail.Dialogs;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.AttackResult;
import com.gpl.rpg.AndorsTrail.controller.CombatController;
import com.gpl.rpg.AndorsTrail.controller.MovementController;
import com.gpl.rpg.AndorsTrail.controller.listeners.ActorConditionListener;
import com.gpl.rpg.AndorsTrail.controller.listeners.CombatActionListener;
import com.gpl.rpg.AndorsTrail.controller.listeners.CombatTurnListener;
import com.gpl.rpg.AndorsTrail.controller.listeners.PlayerMovementListener;
import com.gpl.rpg.AndorsTrail.controller.listeners.QuickSlotListener;
import com.gpl.rpg.AndorsTrail.controller.listeners.WorldEventListener;
import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.actor.Actor;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer.ItemEntry;
import com.gpl.rpg.AndorsTrail.model.item.Loot;
import com.gpl.rpg.AndorsTrail.model.map.MapObject;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.savegames.Savegames;
import com.gpl.rpg.AndorsTrail.util.Coord;
import com.gpl.rpg.AndorsTrail.view.*;
import com.gpl.rpg.AndorsTrail.view.QuickButton.QuickButtonContextMenuInfo;

import java.lang.ref.WeakReference;
import java.util.Collection;

public final class MainActivity
		extends Activity
		implements
		PlayerMovementListener
		, CombatActionListener
		, CombatTurnListener
		, WorldEventListener
		, ActorConditionListener
		, QuickSlotListener{

	public static final int INTENTREQUEST_MONSTERENCOUNTER = 2;
	public static final int INTENTREQUEST_CONVERSATION = 4;
	public static final int INTENTREQUEST_SAVEGAME = 8;

	public static final String repeatFiller =  "%1$s";

	private ControllerContext controllers;
	private WorldContext world;

	private MainView mainview;
	private StatusView statusview;
	private CombatView combatview;
	private QuickitemView quickitemview;
	private DisplayActiveActorConditionIcons activeConditions;
	private ToolboxView toolboxview;

	private TextView statusText;
	private WeakReference<Toast> lastToast = null;
	private ContextMenuInfo lastSelectedMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
		if (!app.isInitialized()) { finish(); return; }
		AndorsTrailPreferences preferences = app.getPreferences();
		this.world = app.getWorld();
		this.controllers = app.getControllerContext();
		app.setWindowParameters(this);

		setContentView(R.layout.main);
		mainview = (MainView) findViewById(R.id.main_mainview);
		statusview = (StatusView) findViewById(R.id.main_statusview);
		combatview = (CombatView) findViewById(R.id.main_combatview);
		quickitemview = (QuickitemView) findViewById(R.id.main_quickitemview);
		activeConditions = new DisplayActiveActorConditionIcons(controllers, world, this, (RelativeLayout) findViewById(R.id.statusview_activeconditions));
		VirtualDpadView dpad = (VirtualDpadView) findViewById(R.id.main_virtual_dpad);
		toolboxview = (ToolboxView) findViewById(R.id.main_toolboxview);
		statusview.registerToolboxViews(toolboxview, quickitemview);

		statusText = (TextView) findViewById(R.id.statusview_statustext);
		statusText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				statusText.setVisibility(View.GONE);
			}
		});
		clearMessages();

		if (AndorsTrailApplication.DEVELOPMENT_DEBUGBUTTONS)
			new DebugInterface(controllers, world, this).addDebugButtons();

		quickitemview.setVisibility(View.GONE);
		quickitemview.registerForContextMenu(this);

		dpad.updateVisibility(preferences);
		quickitemview.setPosition(preferences);

		// Define which views are in front of each other.
		dpad.bringToFront();
		quickitemview.bringToFront();
		toolboxview.bringToFront();
		combatview.bringToFront();
		statusview.bringToFront();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case INTENTREQUEST_MONSTERENCOUNTER:
			if (resultCode == Activity.RESULT_OK) {
				controllers.combatController.enterCombat(CombatController.BeginTurnAs.player);
			} else {
				controllers.combatController.exitCombat(false);
			}
			break;
		case INTENTREQUEST_CONVERSATION:
			controllers.mapController.applyCurrentMapReplacements(getResources(), true);
			break;
		case INTENTREQUEST_SAVEGAME:
			if (resultCode != Activity.RESULT_OK) break;
			final int slot = data.getIntExtra("slot", 1);
			if (save(slot)) {
				Toast.makeText(this, getResources().getString(R.string.menu_save_gamesaved, slot), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, R.string.menu_save_failed, Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	private boolean save(int slot) {
		final Player player = world.model.player;
		return Savegames.saveWorld(world, this, slot, getString(R.string.savegame_currenthero_displayinfo, player.getLevel(), player.getTotalExperience(), player.getGold()));
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!AndorsTrailApplication.getApplicationFromActivity(this).getWorldSetup().isSceneReady) return;
		subscribeToModelChanges();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unsubscribeFromModel();
	}

	@Override
	protected void onPause() {
		super.onPause();
		controllers.gameRoundController.pause();
		controllers.movementController.stopMovement();

		save(Savegames.SLOT_QUICKSAVE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!AndorsTrailApplication.getApplicationFromActivity(this).getWorldSetup().isSceneReady) return;

		controllers.gameRoundController.resume();

		updateStatus();
	}

	private void unsubscribeFromModel() {
		activeConditions.unsubscribe();
		combatview.unsubscribe();
		mainview.unsubscribe();
		quickitemview.unsubscribe();
		statusview.unsubscribe();
		controllers.movementController.playerMovementListeners.remove(this);
		controllers.combatController.combatActionListeners.remove(this);
		controllers.combatController.combatTurnListeners.remove(this);
		controllers.mapController.worldEventListeners.remove(this);
		controllers.actorStatsController.actorConditionListeners.remove(this);
		controllers.itemController.quickSlotListeners.remove(this);
	}

	private void subscribeToModelChanges() {
		controllers.mapController.worldEventListeners.add(this);
		controllers.combatController.combatTurnListeners.add(this);
		controllers.combatController.combatActionListeners.add(this);
		controllers.movementController.playerMovementListeners.add(this);
		controllers.actorStatsController.actorConditionListeners.add(this);
		controllers.itemController.quickSlotListeners.add(this);
		statusview.subscribe();
		quickitemview.subscribe();
		mainview.subscribe();
		combatview.subscribe();
		activeConditions.subscribe();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(quickitemview.isQuickButtonId(v.getId())){
			createQuickButtonMenu(menu);
		}
		lastSelectedMenu = null;
	}

	private void createQuickButtonMenu(ContextMenu menu){
		menu.add(Menu.NONE, R.id.quick_menu_unassign, Menu.NONE, R.string.inventory_unassign);
		SubMenu assignMenu = menu.addSubMenu(Menu.NONE, R.id.quick_menu_assign, Menu.NONE, R.string.inventory_assign);
		for(int i=0; i<world.model.player.inventory.items.size(); ++i){
			ItemEntry itemEntry = world.model.player.inventory.items.get(i);
			if(itemEntry.itemType.isUsable())
				assignMenu.add(R.id.quick_menu_assign_group, i, Menu.NONE, itemEntry.itemType.getName(world.model.player));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		QuickButtonContextMenuInfo menuInfo;
		if(item.getGroupId() == R.id.quick_menu_assign_group){
			menuInfo = (QuickButtonContextMenuInfo) lastSelectedMenu;
			controllers.itemController.setQuickItem(world.model.player.inventory.items.get(item.getItemId()).itemType, menuInfo.index);
			return true;
		}
		switch(item.getItemId()){
		case R.id.quick_menu_unassign:
			menuInfo = (QuickButtonContextMenuInfo) item.getMenuInfo();
			controllers.itemController.setQuickItem(null, menuInfo.index);
			break;
		case R.id.quick_menu_assign:
			menuInfo = (QuickButtonContextMenuInfo) item.getMenuInfo();
			lastSelectedMenu = menuInfo;
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private void updateStatus() {
		statusview.updateStatus();
		quickitemview.refreshQuickitems();
		combatview.updateStatus();
		toolboxview.updateIcons();
	}

	private void message(String msg) {
		world.model.combatLog.append(msg);
		statusText.setText(world.model.combatLog.getLastMessages());
		statusText.setVisibility(View.VISIBLE);
	}
	private void message(String msg, String repeatable) {
		world.model.combatLog.append(msg, repeatable);
		statusText.setText(world.model.combatLog.getLastMessages());
		statusText.setVisibility(View.VISIBLE);
	}

	private void clearMessages() {
		world.model.combatLog.appendCombatEnded();
		statusText.setVisibility(View.GONE);
	}

	private void showToast(String msg, int duration) {
		if (msg == null) return;
		if (msg.length() == 0) return;
		Toast t = null;
		if (lastToast != null) t = lastToast.get();
		if (t == null) {
			t = Toast.makeText(this, msg, duration);
			lastToast = new WeakReference<Toast>(t);
		} else {
			t.setText(msg);
			t.setDuration(duration);
		}
		t.show();
	}


	@Override
	public void onPlayerMoved(Coord newPosition, Coord previousPosition) { }

	@Override
	public void onPlayerEnteredNewMap(PredefinedMap map, Coord p) { }

	@Override
	public void onPlayerAimInvalid() {
		message(getString(R.string.player_aim_invalid),
				getString(R.string.player_aim_invalid_repeat, repeatFiller));
	}

	@Override
	public void onPlayerAimToofar() {
		message(getString(R.string.player_aim_toofar),
				getString(R.string.player_aim_toofar_repeat, repeatFiller));}

	/*@Override
	public void onToggledAimMode(Boolean change) {
		if(change.booleanValue())
			message(getString(R.string.player_toggled_aim_on));
		else
			message(getString(R.string.player_toggled_aim_off),
					getString(R.string.player_toggled_aim_off_repeat, repeatFiller));
	}*/

	@Override
	public void onPlayerCannotFindTargets(){
		message(getString(R.string.player_cannot_find_targets),
				getString(R.string.player_cannot_find_targets_repeat, repeatFiller));
	}

	@Override
	public void onCombatStarted() {
		clearMessages();
	}

	@Override
	public void onCombatEnded() {
		clearMessages();
	}

	@Override
	public void onPlayerAttackMissed(Monster target, AttackResult attackResult) {
		message(getString(R.string.combat_result_heromiss),
				getString(R.string.combat_result_heromiss_repeat, repeatFiller));
	}

	@Override
	public void onPlayerAttackSuccess(Monster target, AttackResult attackResult) {
		final String monsterName = target.getName();
		if (attackResult.isCriticalHit) {
			message(getString(R.string.combat_result_herohitcritical, monsterName, attackResult.damage),
					getString(R.string.combat_result_herohitcritical_repeat, monsterName, attackResult.damage,repeatFiller));
		} else {
			message(getString(R.string.combat_result_herohit, monsterName, attackResult.damage),
					getString(R.string.combat_result_herohit_repeat, monsterName, attackResult.damage, repeatFiller));
		}
		if (attackResult.targetDied) {
			message(getString(R.string.combat_result_herokillsmonster, monsterName, attackResult.damage),
					getString(R.string.combat_result_herokillsmonster_repeat, monsterName, attackResult.damage, repeatFiller));
		}
	}

	@Override
	public void onMonsterAttackMissed(Monster attacker, AttackResult attackResult) {
		message(getString(R.string.combat_result_monstermiss, attacker.getName()),
				getString(R.string.combat_result_monstermiss_repeat, attacker.getName(),repeatFiller));
	}

	@Override
	public void onCombatTargetOutsideRange() {
		message(getString(R.string.player_aim_toofar),
				getString(R.string.player_aim_toofar_repeat, repeatFiller));
	}

	@Override
	public void onMonsterAttackSuccess(Monster attacker, AttackResult attackResult) {
		final String monsterName = attacker.getName();
		if (attackResult.isCriticalHit) {
			message(getString(R.string.combat_result_monsterhitcritical, monsterName, attackResult.damage),
					getString(R.string.combat_result_monsterhitcritical, monsterName, attackResult.damage,repeatFiller));
		} else {
			message(getString(R.string.combat_result_monsterhit, monsterName, attackResult.damage),
					getString(R.string.combat_result_monsterhit_repeat, monsterName, attackResult.damage, repeatFiller));
		}
	}

	@Override
	public void onMonsterMovedDuringCombat(Monster m) {
		String monsterName = m.getName();
		message(getString(R.string.combat_result_monstermoved, monsterName),
				getString(R.string.combat_result_monstermoved_repeat, monsterName, repeatFiller));
	}

	@Override
	public void onMonsterFleedDuringCombat(Monster m) {
		if(MovementController.isAtRange( //Check if he is now at
				world.model.player.position, m.position,
				world.model.player.getMaxRange()+1)
				&& MovementController.areWithinRange(m.lastPosition,  //Then check if monster was in range before
				world.model.player.position,world.model.player.getMaxRange())){
			String monsterName = m.getName();
			message(getString(R.string.combat_result_monsterfleed, monsterName),
					getString(R.string.combat_result_monsterfleed_repeat, monsterName, repeatFiller));}
	}
	@Override
	public void onPlayerKilledMonster(Monster target) { }

	@Override
	public void onNewPlayerTurn() { }

	@Override
	public void onMonsterIsAttacking(Monster m) { }

	@Override
	public void onPlayerStartedConversation(Monster m, String phraseID) {
		Dialogs.showConversation(this, controllers, phraseID, m);
	}

	@Override
	public void onScriptAreaStartedConversation(String phraseID) {
		Dialogs.showMapScriptMessage(this, controllers, phraseID);
	}

	@Override
	public void onPlayerSteppedOnMonster(Monster m) {
		Dialogs.showMonsterEncounter(this, controllers, m);
	}

	@Override
	public void onPlayerSteppedOnMapSignArea(MapObject area) {
		Dialogs.showMapSign(this, controllers, area.id);
		Dialogs.showMapSign(this, controllers, area.id);
	}

	@Override
	public void onPlayerSteppedOnKeyArea(MapObject area) {
		Dialogs.showKeyArea(this, controllers, area.id);
	}

	@Override
	public void onPlayerSteppedOnRestArea(MapObject area) {
		Dialogs.showConfirmRest(this, controllers, area);
	}

	@Override
	public void onPlayerSteppedOnGroundLoot(Loot loot) {
		final String msg = Dialogs.getGroundLootFoundMessage(this, loot);
		Dialogs.showGroundLoot(this, controllers, world, loot, msg);
	}

	@Override
	public void onPlayerPickedUpGroundLoot(Loot loot) {
		if (controllers.preferences.displayLoot == AndorsTrailPreferences.DISPLAYLOOT_NONE) return;
		if (!showToastForPickedUpItems(loot)) return;

		final String msg = Dialogs.getGroundLootPickedUpMessage(this, loot);
		showToast(msg, Toast.LENGTH_LONG);
	}

	private boolean showToastForPickedUpItems(Loot loot) {
		switch (controllers.preferences.displayLoot) {
			case AndorsTrailPreferences.DISPLAYLOOT_TOAST:
			case AndorsTrailPreferences.DISPLAYLOOT_DIALOG_FOR_ITEMS_ELSE_TOAST:
				return true;
			case AndorsTrailPreferences.DISPLAYLOOT_TOAST_FOR_ITEMS:
				return loot.hasItems();
		}
		return false;
	}

	@Override
	public void onPlayerFoundMonsterLoot(Collection<Loot> loot, int exp) {
		final Loot combinedLoot = Loot.combine(loot);
		final String msg = Dialogs.getMonsterLootFoundMessage(this, combinedLoot, exp);
		Dialogs.showMonsterLoot(this, controllers, world, loot, combinedLoot, msg);
	}

	@Override
	public void onPlayerPickedUpMonsterLoot(Collection<Loot> loot, int exp) {
		if (controllers.preferences.displayLoot == AndorsTrailPreferences.DISPLAYLOOT_NONE) return;

		final Loot combinedLoot = Loot.combine(loot);
		if (!showToastForPickedUpItems(combinedLoot)) return;

		final String msg = Dialogs.getMonsterLootPickedUpMessage(this, combinedLoot, exp);
		showToast(msg, Toast.LENGTH_LONG);
	}

	@Override
	public void onPlayerRested() {
		Dialogs.showRested(this, controllers);
	}

	@Override
	public void onPlayerDied(int lostExp) {
		message(getString(R.string.combat_hero_dies, lostExp));
	}

	@Override
	public void onPlayerStartedFleeing() {
		message(getString(R.string.combat_begin_flee));
	}

	@Override
	public void onPlayerFailedFleeing() {
		message(getString(R.string.combat_flee_failed),
				getString(R.string.combat_flee_failed_repeat, repeatFiller));
	}

	@Override
	public void onPlayerDoesNotHaveEnoughAP() {
		message(getString(R.string.combat_not_enough_ap),
				getString(R.string.combat_not_enough_ap_repeat, repeatFiller));
	}

	@Override
	public void onActorConditionAdded(Actor actor, ActorCondition condition){
		message(getString(R.string.combat_actorcondition_added, actor.getName(), condition.conditionType.name),
				getString(R.string.combat_actorcondition_added_repeat, actor.getName(), condition.conditionType.name, repeatFiller));
	}
	@Override
	public void onActorConditionRemoved(Actor actor, ActorCondition condition){
		message(getString(R.string.combat_actorcondition_removed, actor.getName(), condition.conditionType.name),
				getString(R.string.combat_actorcondition_removed_repeat, actor.getName(), condition.conditionType.name, repeatFiller));
	}

	@Override
	public void onActorConditionDurationChanged(Actor actor, ActorCondition condition){
		message(getString(R.string.combat_actorcondition_duration, condition.conditionType.name, actor.getName()),
				getString(R.string.combat_actorcondition_duration_repeat, condition.conditionType.name, actor.getName(), repeatFiller));
	}

	@Override
	public void onActorConditionMagnitudeChanged(Actor actor, ActorCondition condition){
		message(getString(R.string.combat_actorcondition_magnitude, condition.conditionType.name, actor.getName()),
				getString(R.string.combat_actorcondition_magnitude_repeat, condition.conditionType.name, actor.getName(), repeatFiller));
	}

	@Override
	public void onActorConditionRoundEffectApplied(Actor actor, ActorCondition condition){
		message(getString(R.string.combat_actorcondition_applied, condition.conditionType.name,actor.getName()),
				getString(R.string.combat_actorcondition_applied_repeat, condition.conditionType.name, actor.getName(),  repeatFiller));
	}

	@Override
	public void onQuickSlotChanged(int slotId) {}

	@Override
	public void onQuickSlotUsed(int slotId) {}

	@Override
	public void onPresetLoaded(Integer presetIndex, String name) {
		/*if(name.equals("")){
			world.model.player.inventory.setCurrentPresetNameByIndex(
					getString(R.string.inventory_category_preset_default_repeat, presetIndex +1));
		}*/
		message(getString(R.string.inventory_preset_loaded_custom_name, name));
	}

	@Override
	public void onPresetLoadFailed(Integer presetIndex, String name){
		/*if(name.equals("")){
			world.model.player.inventory.setCurrentPresetNameByIndex(
					getString(R.string.inventory_category_preset_default_repeat, presetIndex + 1));
		}*/
		message(getString(R.string.inventory_preset_load_failed, name));
	}

	@Override
	public void onPresetSelected(Integer presetIndex, String name){
		/*if(name.equals("")){ // todo,twirl less hackish way of resetting.
			world.model.player.inventory.setCurrentPresetNameByIndex(
					getString(R.string.inventory_category_preset_default_repeat, presetIndex + 1));
		}
		message(getString(R.string.inventory_preset_selected, name));*/
	}
}
