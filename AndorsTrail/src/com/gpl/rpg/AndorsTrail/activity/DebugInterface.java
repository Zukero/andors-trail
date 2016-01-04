package com.gpl.rpg.AndorsTrail.activity;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.activity.fragment.HeroinfoActivity_Inventory;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;

public final class DebugInterface {
	private final ControllerContext controllerContext;
	private final MainActivity mainActivity;
	private final Resources res;
	private final WorldContext world;

	public DebugInterface(ControllerContext controllers, WorldContext world, MainActivity mainActivity) {
		this.controllerContext = controllers;
		this.world = world;
		this.res = mainActivity.getResources();
		this.mainActivity = mainActivity;
	}

	public void addDebugButtons() {
		if (!AndorsTrailApplication.DEVELOPMENT_DEBUGBUTTONS) return;
		world.model.player.moveCost = 5; //move to suitable location
		addDebugButtons(new DebugButton[] {
				new DebugButton("teleport", new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean change = world.model.player.toggleTeleportMode();
				if(change){
					showToast(mainActivity, "DEBUG: teleport ON. \nLiterally unplayable.", Toast.LENGTH_SHORT);
				}else{
					showToast(mainActivity, "DEBUG: teleport OFF.", Toast.LENGTH_SHORT);
				}
			}
				})
				/*,new DebugButton("hp", new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				world.model.player.baseTraits.maxHP = 500;
				world.model.player.health.max = world.model.player.baseTraits.maxHP;
				controllerContext.actorStatsController.setActorMaxHealth(world.model.player);
				world.model.player.conditions.clear();
				showToast(mainActivity, "DEBUG: hp set to max", Toast.LENGTH_SHORT);
			}
		})*/
			/*,new DebugButton("dmg", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					world.model.player.damagePotential.set(500, 500);
					world.model.player.attackChance = 500;
					world.model.player.attackCost = 1;
					showToast(mainActivity, "DEBUG: damagePotential=500, chance=500%, cost=1", Toast.LENGTH_SHORT);
				}
			})*/
			/*,new DebugButton("dmg=1", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					world.model.player.damagePotential.set(1, 1);
					showToast(mainActivity, "DEBUG: damagePotential=1", Toast.LENGTH_SHORT);
				}
			})*/
			/*,new DebugButton("items", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					world.model.player.inventory.addItem(world.itemTypes.getItemType("elytharan_redeemer"));
					world.model.player.inventory.addItem(world.itemTypes.getItemType("ring_shadow0"));
					world.model.player.inventory.addItem(world.itemTypes.getItemType("shadow_slayer"));
					world.model.player.inventory.addItem(world.itemTypes.getItemType("pot_blind_rage"), 10);
					world.model.player.inventory.addItem(world.itemTypes.getItemType("clouded_rage"));
					world.model.player.inventory.addItem(world.itemTypes.getItemType("pot_fatigue_restore"), 20);
					world.model.player.inventory.addItem(world.itemTypes.getItemType("quickdagger1"));
					world.model.player.inventory.addItem(world.itemTypes.getItemType("bonemeal_potion"));
					world.model.player.inventory.addItem(world.itemTypes.getItemType("calomyran_secrets"));
					world.model.player.inventory.addItem(world.itemTypes.getItemType("tail_caverat"));
					world.model.player.inventory.addItem(world.itemTypes.getItemType("bwm_leather_cap"));
					world.model.player.inventory.addItem(world.itemTypes.getItemType("chaosreaper"));

					showToast(mainActivity, "DEBUG: added items", Toast.LENGTH_SHORT);
				}
			})*/
			/*,new DebugButton("prim", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					controllerContext.movementController.placePlayerAsyncAt(MapObject.MapObjectType.newmap, "blackwater_mountain29", "south", 0, 0);
				}
			})*/
			/*,new DebugButton("exp+=10000", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					controllerContext.actorStatsController.addExperience(10000);
					showToast(mainActivity, "DEBUG: given 10000 exp", Toast.LENGTH_SHORT);
				}
			})*/
			/*,new DebugButton("reset", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					for(PredefinedMap map : world.maps.getAllMaps()) {
						map.resetTemporaryData();
					}
					showToast(mainActivity, "DEBUG: maps respawned", Toast.LENGTH_SHORT);
				}
			})*/
			,new DebugButton("+1 bow", new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ItemType itemType = world.itemTypes.getItemType("wooden_longbow");
				world.model.player.inventory.addItem(itemType);
				controllerContext.itemController.equipItem(itemType, Inventory.WearSlot.weapon);
				showToast(mainActivity, "DEBUG: equipped a new bow. \n" +
						"Range atm: " + +world.model.player.increaseMaxRange, Toast.LENGTH_SHORT);
					/*boolean change = world.model.player.toggleEquipOfRangedWeapon();
					if(change){
						showToast(mainActivity, "DEBUG: ranged weapon ON", Toast.LENGTH_SHORT);
					}else{
						showToast(mainActivity, "DEBUG: ranged weapon OFF", Toast.LENGTH_SHORT);
					}*/
			}
			})
				,new DebugButton("+range", new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				world.model.player.increaseMaxRange +=1;
				showToast(mainActivity, "DEBUG: Range +1 \n" +
						"New range: "+ world.model.player.increaseMaxRange, Toast.LENGTH_SHORT);
			}
		})
			,new DebugButton("start aim", new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean change = world.model.player.toggleAimMode();
				if(change){
					showToast(mainActivity, "DEBUG: aim-mode is ON. \nYou stand immobile as you pick a target.", Toast.LENGTH_SHORT);
				}else{
					showToast(mainActivity, "DEBUG: aim-mode is OFF. ", Toast.LENGTH_SHORT);
				}
			}
			})
				,new DebugButton("need 2 aim", new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean aimNotNeeded = world.model.player.toggleNeedAiming();
				if(!aimNotNeeded){
					showToast(mainActivity, "DEBUG: aim-mode is NEEDED. \n" +
							"You need to stand immobile to aim before shooting.", Toast.LENGTH_SHORT);
				}else{
					showToast(mainActivity, "DEBUG: aim-mode is NOT needed. \n" +
							"You can directly shoot monsters within attack range.", Toast.LENGTH_SHORT);
				}
			}
		})
				,new DebugButton("real following", new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				controllerContext.monsterMovementController.existAngryFollowingRealtime =
						!controllerContext.monsterMovementController.existAngryFollowingRealtime;
				boolean change = controllerContext.monsterMovementController.existAngryFollowingRealtime;
				if(change){
					showToast(mainActivity, "DEBUG: realtime following ON. \nPissed-off monsters stalk you when you flee.", Toast.LENGTH_SHORT);
				}else{
					showToast(mainActivity, "DEBUG: realtime following OFF.", Toast.LENGTH_SHORT);
				}
			}
		})
			/*,new DebugButton("NPC telepathy", new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			//Can interact with any NPC or monster without moving.
				world.model.player.inTelepathyMode = !world.model.player.inTelepathyMode;
				boolean change = world.model.player.inTelepathyMode;
				if(change){
					showToast(mainActivity, "DEBUG: NPC telepathy ON", Toast.LENGTH_SHORT);
				}else{
					showToast(mainActivity, "DEBUG: NPC telepathy OFF", Toast.LENGTH_SHORT);
				}
			}
		})*/
				/*
				,new DebugButton("gold", new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				world.model.player.inventory.gold += 9999;
				showToast(mainActivity, "DEBUG: gold increased", Toast.LENGTH_SHORT);
			}
		})

			,new DebugButton("cg", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					controllerContext.movementController.placePlayerAsyncAt(MapObject.MapObjectType.newmap, "crossglen", "hall", 0, 0);
				}
			})
			,new DebugButton("vg", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					controllerContext.movementController.placePlayerAsyncAt(MapObject.MapObjectType.newmap, "vilegard_s", "tavern", 0, 0);
				}
			})
			,new DebugButton("cr", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					controllerContext.movementController.placePlayerAsyncAt(MapObject.MapObjectType.newmap, "houseatcrossroads4", "down", 0, 0);
				}
			})
			,new DebugButton("lf", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					controllerContext.movementController.placePlayerAsyncAt(MapObject.MapObjectType.newmap, "loneford9", "south", 0, 0);
				}
			})
			,new DebugButton("fh", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					controllerContext.movementController.placePlayerAsyncAt(MapObject.MapObjectType.newmap, "fallhaven_ne", "clothes", 0, 0);
				}
			})
			,new DebugButton("rc", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					controllerContext.movementController.placePlayerAsyncAt(MapObject.MapObjectType.newmap, "roadtocarntower1", "left3", 0, 0);
				}
			})
			*/
		});
	}

	private void showToast(Context context, String msg, int duration) {
		Toast.makeText(context, msg, duration).show();
	}

	private static class DebugButton {
		public final String text;
		public final OnClickListener listener;
		public DebugButton(String text, OnClickListener listener) {
			this.text = text;
			this.listener = listener;
		}
	}

	private void addDebugButton(DebugButton button, int id, RelativeLayout layout) {
		if (!AndorsTrailApplication.DEVELOPMENT_DEBUGBUTTONS) return;

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, res.getDimensionPixelSize(R.dimen.smalltext_buttonheight));
		if (id == 1)
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		else
			lp.addRule(RelativeLayout.RIGHT_OF, id - 1);
		lp.addRule(RelativeLayout.ABOVE, R.id.main_statusview);
		Button b = new Button(mainActivity);
		b.setText(button.text);
		b.setTextSize(10);//res.getDimension(R.dimen.actionbar_text));
		b.setId(id);
		b.setLayoutParams(lp);
		b.setOnClickListener(button.listener);
		layout.addView(b);
	}

	private void addDebugButtons(DebugButton[] buttons) {
		if (!AndorsTrailApplication.DEVELOPMENT_DEBUGBUTTONS) return;

		if (buttons == null || buttons.length <= 0) return;
		RelativeLayout layout = (RelativeLayout) mainActivity.findViewById(R.id.main_container);

		int id = 1;
		for (DebugButton b : buttons) {
			addDebugButton(b, id, layout);
			++id;
		}
	}
}
