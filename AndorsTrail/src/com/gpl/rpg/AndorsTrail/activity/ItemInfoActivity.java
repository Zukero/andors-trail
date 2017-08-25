package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.view.ItemEffectsView;

import java.util.Collections;

public final class ItemInfoActivity extends Activity {

	public static enum ItemInfoAction {
		none, use, equip, unequip, buy, sell
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
		if (!app.isInitialized()) { finish(); return; }
		final WorldContext world = app.getWorld();

		app.setWindowParameters(this);

		final Intent intent = getIntent();
		Bundle params = intent.getExtras();
		String itemTypeID = params.getString("itemTypeID");
		final ItemType itemType = world.itemTypes.getItemType(itemTypeID);
		final ItemType equippedType;
		final ItemType secondaryEquippedType;

		if (itemType.isEquippable()) {
			equippedType = world.model.player.inventory.getItemTypeInWearSlot(itemType.category.inventorySlot);
			if (itemType.category.inventorySlot == Inventory.WearSlot.leftring)
			{
				secondaryEquippedType = world.model.player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.rightring);
			} else if (itemType.category.inventorySlot == Inventory.WearSlot.weapon) {
				secondaryEquippedType = world.model.player.inventory.getItemTypeInWearSlot(Inventory.WearSlot.shield);
			} else {
				secondaryEquippedType = null;
			}
		} else {
			equippedType = null;
			secondaryEquippedType = null;
		}



		final String buttonText = params.getString("buttonText");
		boolean buttonEnabled = params.getBoolean("buttonEnabled");
		final Resources resources = getResources();

		setContentView(R.layout.iteminfo);

		fillTitle(world, resources, (TextView) findViewById(R.id.iteminfo_title), itemType, (itemType == equippedType || itemType == secondaryEquippedType));
		fillDescription((TextView) findViewById(R.id.iteminfo_description), itemType);
		fillCategory((TextView) findViewById(R.id.iteminfo_category), itemType);
		fillItemEffects((ItemEffectsView) findViewById(R.id.iteminfo_effects), itemType);
		fillDisplayType(resources, (TextView) findViewById(R.id.iteminfo_displaytype), itemType);

		if (equippedType != null && equippedType != itemType){
			findViewById(R.id.compareinfo).setVisibility(View.VISIBLE);
			fillTitle(world, resources, (TextView) findViewById(R.id.compareinfo_title), equippedType, true);
			fillDescription((TextView) findViewById(R.id.compareinfo_description), equippedType);
			fillCategory((TextView) findViewById(R.id.compareinfo_category), equippedType);
			fillItemEffects((ItemEffectsView) findViewById(R.id.compareinfo_effects), equippedType);
			fillDisplayType(resources, (TextView) findViewById(R.id.compareinfo_displaytype), equippedType);
		} else {
			findViewById(R.id.compareinfo).setVisibility(View.GONE);
		}

		if (secondaryEquippedType != null && itemType != secondaryEquippedType && equippedType != secondaryEquippedType) {
			findViewById(R.id.compare2info).setVisibility(View.VISIBLE);
			fillTitle(world, resources, (TextView) findViewById(R.id.compare2info_title), secondaryEquippedType, true);
			fillDescription((TextView) findViewById(R.id.compare2info_description), secondaryEquippedType);
			fillCategory((TextView) findViewById(R.id.compare2info_category), secondaryEquippedType);
			fillItemEffects((ItemEffectsView) findViewById(R.id.compare2info_effects), secondaryEquippedType);
			fillDisplayType(resources, (TextView) findViewById(R.id.compare2info_displaytype), secondaryEquippedType);
		} else {
			findViewById(R.id.compare2info).setVisibility(View.GONE);
		}

		Button b = (Button) findViewById(R.id.iteminfo_close);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
				ItemInfoActivity.this.finish();
			}
		});

		b = (Button) findViewById(R.id.iteminfo_action);
		if (buttonText != null && buttonText.length() > 0) {
			b.setVisibility(View.VISIBLE);
			b.setEnabled(buttonEnabled);
			b.setText(buttonText);
		} else {
			b.setVisibility(View.GONE);
		}

		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent result = new Intent();
				result.putExtras(intent);
				setResult(RESULT_OK, result);
				ItemInfoActivity.this.finish();
			}
		});

	}

	private static void fillTitle(WorldContext world, Resources resources, TextView tv, ItemType itemType, boolean equipped) {
		tv.setText(itemType.getName(world.model.player) + (equipped ? " " + resources.getString(R.string.iteminfo_title_equipped) : ""));
		world.tileManager.setImageViewTileForSingleItemType(resources, tv, itemType);

	}

	private static void fillCategory(TextView tv, ItemType itemType) {
		tv.setText(itemType.category.displayName);
	}

	private static void fillDisplayType(Resources resources, TextView tv, ItemType itemType) {
		if (itemType.isOrdinaryItem()) {
			tv.setVisibility(View.GONE);
		} else {
			tv.setVisibility(View.VISIBLE);
			final String diplayType = getDisplayTypeString(resources, itemType);
			tv.setText(diplayType);
		}
	}

	private static void fillItemEffects(ItemEffectsView iev, ItemType itemType) {
		iev.update(
				itemType.effects_equip,
				itemType.effects_use == null ? null : Collections.singletonList(itemType.effects_use),
				itemType.effects_hit == null ? null : Collections.singletonList(itemType.effects_hit),
				itemType.effects_kill == null ? null : Collections.singletonList(itemType.effects_kill),
				itemType.isWeapon()
		);
	}

	private static void fillDescription(TextView tv, ItemType itemType) {
		String description = itemType.getDescription();
		if (description != null) {
			tv.setText(description);
			tv.setVisibility(View.VISIBLE);
		} else {
			tv.setVisibility(View.GONE);
		}
	}


	public static String getDisplayTypeString(Resources res, ItemType itemType) {
		switch (itemType.displayType) {
			case rare: return res.getString(R.string.iteminfo_displaytypes_rare);
			case extraordinary: return res.getString(R.string.iteminfo_displaytypes_extraordinary);
			case legendary: return res.getString(R.string.iteminfo_displaytypes_legendary);
			case ordinary: return res.getString(R.string.iteminfo_displaytypes_ordinary);
			case quest: return res.getString(R.string.iteminfo_displaytypes_quest);
			default: return "";
		}
	}
}
