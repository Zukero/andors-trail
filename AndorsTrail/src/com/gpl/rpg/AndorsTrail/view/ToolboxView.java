package com.gpl.rpg.AndorsTrail.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.AndorsTrailPreferences;
import com.gpl.rpg.AndorsTrail.Dialogs;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.WorldMapController;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileManager;

public final class ToolboxView extends LinearLayout implements OnClickListener {
	private final WorldContext world;
	private final ControllerContext controllers;
	private final AndorsTrailPreferences preferences;
	private final Animation showAnimation;
	private final Animation hideAnimation;
	private final ImageButton toolbox_aim;
	private final ImageButton toolbox_quickitems;
	private final ImageButton toolbox_map;
	private final ImageButton toolbox_save;
	private final ImageButton toolbox_combatlog;
	private ImageButton toggleToolboxVisibility;
	private QuickitemView quickitemView;
	private boolean hideQuickslotsWhenToolboxIsClosed = false;
	private static final int quickSlotIcon = R.drawable.ui_icon_equipment;
	private final Drawable quickSlotIconsLockedDrawable;
	private final Resources res;

	public ToolboxView(final Context context, AttributeSet attrs) {
		super(context, attrs);
		AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivityContext(context);
		this.world = app.getWorld();
		this.controllers = app.getControllerContext();
		this.preferences = app.getPreferences();

		inflate(context, R.layout.toolboxview, this);

		this.showAnimation = AnimationUtils.loadAnimation(context, R.anim.showtoolbox);
		this.hideAnimation = AnimationUtils.loadAnimation(context, R.anim.hidetoolbox);
		this.hideAnimation.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) { }
			@Override public void onAnimationRepeat(Animation animation) { }
			@Override public void onAnimationEnd(Animation animation) {
				ToolboxView.this.setVisibility(View.GONE);
			}
		});

		toolbox_aim = (ImageButton) findViewById(R.id.toolbox_safeAim);
		toolbox_aim.setOnClickListener(this);
		toolbox_quickitems = (ImageButton)findViewById(R.id.toolbox_quickitems);
		toolbox_quickitems.setOnClickListener(this);
		toolbox_map = (ImageButton)findViewById(R.id.toolbox_map);
		toolbox_map.setOnClickListener(this);
		toolbox_save = (ImageButton)findViewById(R.id.toolbox_save);
		toolbox_save.setOnClickListener(this);
		toolbox_combatlog = (ImageButton)findViewById(R.id.toolbox_combatlog);
		toolbox_combatlog.setOnClickListener(this);

		res = getResources();
		quickSlotIconsLockedDrawable = new LayerDrawable(new Drawable[] {
				res.getDrawable(quickSlotIcon)
				,new BitmapDrawable(res, world.tileManager.preloadedTiles.getBitmap(TileManager.iconID_moveselect))
		});
		hideQuickslotsWhenToolboxIsClosed = preferences.showQuickslotsWhenToolboxIsVisible;
	}

	public void registerToolboxViews(ImageButton toggleVisibility, QuickitemView quickitemView) {
		this.toggleToolboxVisibility = toggleVisibility;
		this.quickitemView = quickitemView;
		toggleVisibility.setOnClickListener(this);
		updateIcons();
	}

	@Override
	public void onClick(View btn) {
		Context context = getContext();
		if (btn == toggleToolboxVisibility) {
			toggleVisibility();
		} else if (btn == toolbox_aim){
			//if(!preferences.rangedLegacySafeAim)
			if(world.model.uiSelections.isInCombat)
				controllers.combatController.exitRangedCombat(true);
			else
				controllers.combatController.enterRangedCombatAsPlayer();
			//else controllers.movementController.playerMovementListeners.onToggledAimMode(world.model.player.toggleAimMode());
		} else if (btn == toolbox_quickitems) {
			toggleQuickslotItemView();
		} else if (btn == toolbox_map) {
			if (!WorldMapController.displayWorldMap(context, world)) return;
			hide(false);
		} else if (btn == toolbox_save) {
			if (Dialogs.showSave((Activity)getContext(), controllers, world)) {
				hide(false);
			}
		} else if (btn == toolbox_combatlog) {
			Dialogs.showCombatLog(getContext(), controllers, world);
			hide(false);
		}
	}

	private void toggleQuickslotItemView() {
		if (preferences.showQuickslotsWhenToolboxIsVisible) {
			hideQuickslotsWhenToolboxIsClosed = !hideQuickslotsWhenToolboxIsClosed;
			updateToggleQuickSlotItemsIcon();
		} else {
			if (quickitemView.getVisibility() == View.VISIBLE) {
				quickitemView.setVisibility(View.GONE);
			} else {
				quickitemView.setVisibility(View.VISIBLE);
			}
		}
	}

	private void toggleVisibility() {
		if (getVisibility() == View.VISIBLE) hide(preferences.enableUiAnimations);
		else show();
	}

	private void hide(boolean animate) {
		if (getVisibility() != View.GONE) {
			if (animate) {
				startAnimation(hideAnimation);
			} else {
				setVisibility(View.GONE);
			}
		}
		if (preferences.showQuickslotsWhenToolboxIsVisible) {
			if (hideQuickslotsWhenToolboxIsClosed) {
				quickitemView.setVisibility(View.GONE);
			}
		}
		setToolboxIcon(false);
	}

	private void show() {
		if (getVisibility() != View.VISIBLE) {
			setVisibility(View.VISIBLE);
			if (preferences.enableUiAnimations) {
				startAnimation(showAnimation);
			}
		}
		if (preferences.showQuickslotsWhenToolboxIsVisible) {
			quickitemView.setVisibility(View.VISIBLE);
		}
		setToolboxIcon(true);
	}

	public void updateIcons() {
		setToolboxIcon(getVisibility() == View.VISIBLE);
		if(!world.model.player.isWieldingRangedWeapon() && preferences.rangedHideUnusedAim){
			toolbox_aim.setVisibility(GONE);
		}
		else
			toolbox_aim.setVisibility(VISIBLE);
	}

	private void setToolboxIcon(boolean opened) {
		if (opened) {
			world.tileManager.setImageViewTileForUIIcon(res, toggleToolboxVisibility, TileManager.iconID_boxopened);
		} else {
			world.tileManager.setImageViewTileForUIIcon(res, toggleToolboxVisibility, TileManager.iconID_boxclosed);
		}
	}

	private void updateToggleQuickSlotItemsIcon() {
		if (preferences.showQuickslotsWhenToolboxIsVisible && !hideQuickslotsWhenToolboxIsClosed) {
			world.tileManager.setImageViewTile(toolbox_quickitems, quickSlotIconsLockedDrawable);
			return;
		}
		world.tileManager.setImageViewTile(toolbox_quickitems, getResources().getDrawable(quickSlotIcon));
	}
}
