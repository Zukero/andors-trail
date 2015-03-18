package com.gpl.rpg.AndorsTrail.resource.tiles;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.AndorsTrailPreferences;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionType;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer.ItemEntry;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.model.map.*;
import com.gpl.rpg.AndorsTrail.util.L;

import java.util.HashMap;
import java.util.HashSet;

public final class TileManager {
	public static final int CHAR_HERO = 1;
	public static final int iconID_selection_red = 2;
	public static final int iconID_selection_yellow = 3;
	public static final int iconID_attackselect = iconID_selection_red;
	public static final int iconID_moveselect = iconID_selection_yellow;
	public static final int iconID_groundbag = 4;
	public static final int iconID_boxopened = 5;
	public static final int iconID_boxclosed = 6;
	public static final int iconID_shop = iconID_groundbag;
	public static final int iconID_unassigned_quickslot = iconID_groundbag;
	public static final int iconID_selection_blue = 7;
	public static final int iconID_selection_purple = 8;
	public static final int iconID_selection_green = 9;

	public static final int iconID_splatter_red_1a = 10;
	public static final int iconID_splatter_red_1b = 11;
	public static final int iconID_splatter_red_2a = 12;
	public static final int iconID_splatter_red_2b = 13;
	public static final int iconID_splatter_brown_1a = 14;
	public static final int iconID_splatter_brown_1b = 15;
	public static final int iconID_splatter_brown_2a = 16;
	public static final int iconID_splatter_brown_2b = 17;
	public static final int iconID_splatter_white_1a = 18;
	public static final int iconID_splatter_white_1b = 19;

	public int tileSize;

	public int viewTileSize;
	public float scale;


	public final TileCache tileCache = new TileCache();
	public final TileCollection preloadedTiles = new TileCollection(113);
	public TileCollection currentMapTiles;
	public TileCollection adjacentMapTiles;
	private final HashSet<Integer> preloadedTileIDs = new HashSet<Integer>();


	public TileCollection loadTilesFor(HashSet<Integer> tileIDs, Resources r) {
		return tileCache.loadTilesFor(tileIDs, r);
	}

	public TileCollection loadTilesFor(ItemContainer container, Resources r) {
		return tileCache.loadTilesFor(getTileIDsFor(container), r);
	}

	public HashSet<Integer> getTileIDsFor(ItemContainer container) {
		HashSet<Integer> iconIDs = new HashSet<Integer>();
		for(ItemEntry i : container.items) {
			iconIDs.add(i.itemType.iconID);
		}
		return iconIDs;
	}

	public TileCollection loadTilesFor(Inventory inventory, Resources r) {
		HashSet<Integer> iconIDs = getTileIDsFor(inventory);
		for (Inventory.WearSlot slot : Inventory.WearSlot.values()) {
			ItemType t = inventory.getItemTypeInWearSlot(slot);
			if (t != null) iconIDs.add(t.iconID);
		}
		return tileCache.loadTilesFor(iconIDs, r);
	}

	public TileCollection loadTilesFor(PredefinedMap map, LayeredTileMap tileMap, WorldContext world, Resources r) {
		HashSet<Integer> iconIDs = getTileIDsFor(map, tileMap, world);
		TileCollection result = tileCache.loadTilesFor(iconIDs, r);
		for(int i : preloadedTileIDs) {
			result.setBitmap(i, preloadedTiles.getBitmap(i));
		}
		return result;
	}

	public HashSet<Integer> getTileIDsFor(PredefinedMap map, LayeredTileMap tileMap, WorldContext world) {
		HashSet<Integer> iconIDs = new HashSet<Integer>();
		for (MonsterSpawnArea a : map.spawnAreas) {
			for (String monsterTypeID : a.monsterTypeIDs) {
				iconIDs.add(world.monsterTypes.getMonsterType(monsterTypeID).iconID);
			}
			// Add icons for monsters that are already spawned, but that do not belong to the group of
			// monsters that usually spawn here. This could happen if we change the contents of spawn-
			// areas in a later release,
			for (Monster m : a.monsters) {
				iconIDs.add(m.iconID);
			}
		}
		iconIDs.addAll(tileMap.usedTileIDs);
		return iconIDs;
	}

	public void setDensity(Resources r) {
		float density = r.getDisplayMetrics().density;
		tileSize = (int) (32 * density);
	}

	public void updatePreferences(AndorsTrailPreferences prefs) {
		scale = prefs.scalingFactor;
		viewTileSize = (int) (tileSize * prefs.scalingFactor);
	}



	public void setImageViewTile(Resources res, TextView textView, Monster monster) { setImageViewTileForMonster(res, textView, monster.iconID); }
	public void setImageViewTile(Resources res, TextView textView, Player player) { setImageViewTileForPlayer(res, textView, player.iconID); }
	public void setImageViewTileForMonster(Resources res, TextView textView, int iconID) { setImageViewTile(res, textView, currentMapTiles.getBitmap(iconID)); }
	public void setImageViewTileForPlayer(Resources res, TextView textView, int iconID) { setImageViewTile(res, textView, preloadedTiles.getBitmap(iconID)); }
	public void setImageViewTile(Resources res, TextView textView, ActorConditionType conditionType) { setImageViewTile(res, textView, preloadedTiles.getBitmap(conditionType.iconID)); }
	public void setImageViewTileForUIIcon(Resources res, TextView textView, int iconID) { setImageViewTile(res, textView, preloadedTiles.getBitmap(iconID)); }
	private void setImageViewTile(Resources res, TextView textView, Bitmap b) { textView.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(res, b), null, null, null); }

	public void setImageViewTileForSingleItemType(Resources res, TextView textView, ItemType itemType) {
		final Bitmap icon = tileCache.loadSingleTile(itemType.iconID, res);
		setImageViewTile(res, textView, itemType, icon);
	}
	public void setImageViewTile(Resources res, TextView textView, ItemType itemType, TileCollection itemTileCollection) {
		final Bitmap icon = itemTileCollection.getBitmap(itemType.iconID);
		setImageViewTile(res, textView, itemType, icon);
	}
	private void setImageViewTile(Resources res, TextView textView, ItemType itemType, Bitmap icon) {
		final int overlayIconID = itemType.getOverlayTileID();
		if (overlayIconID != -1) {
			textView.setCompoundDrawablesWithIntrinsicBounds(
				new LayerDrawable(new Drawable[] {
					new BitmapDrawable(res, preloadedTiles.getBitmap(overlayIconID))
					,new BitmapDrawable(res, icon)
				}), null, null, null
			);
		} else {
			setImageViewTile(res, textView, icon);
		}
	}

	public void setImageViewTile(ImageView imageView, Monster monster) { setImageViewTileForMonster(imageView, monster.iconID); }
	public void setImageViewTile(ImageView imageView, Player player) { setImageViewTileForPlayer(imageView, player.iconID); }
	public void setImageViewTileForMonster(ImageView imageView, int iconID) { imageView.setImageBitmap(currentMapTiles.getBitmap(iconID)); }
	public void setImageViewTileForPlayer(ImageView imageView, int iconID) { imageView.setImageBitmap(preloadedTiles.getBitmap(iconID)); }
	public void setImageViewTile(ImageView imageView, ActorConditionType conditionType) { imageView.setImageBitmap(preloadedTiles.getBitmap(conditionType.iconID)); }
	public void setImageViewTileForUIIcon(ImageView imageView, int iconID) { imageView.setImageBitmap(preloadedTiles.getBitmap(iconID)); }

	public void setImageViewTile(Resources res, ImageView imageView, ItemType itemType, TileCollection itemTileCollection) {
		final Bitmap icon = itemTileCollection.getBitmap(itemType.iconID);
		setImageViewTile(res, imageView, itemType, icon);
	}
	private void setImageViewTile(Resources res, ImageView imageView, ItemType itemType, Bitmap icon) {
		final int overlayIconID = itemType.getOverlayTileID();
		if (overlayIconID != -1) {
			imageView.setImageDrawable(
				new LayerDrawable(new Drawable[] {
					new BitmapDrawable(res, preloadedTiles.getBitmap(overlayIconID))
					,new BitmapDrawable(res, icon)
				})
			);
		} else {
			imageView.setImageBitmap(icon);
		}
	}

	public void loadPreloadedTiles(Resources r) {
		int maxTileID = tileCache.getMaxTileID();
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			if (maxTileID > preloadedTiles.maxTileID) {
				L.log("ERROR: TileManager.preloadedTiles needs to be initialized with at least " + maxTileID + " slots. Application will crash now.");
				throw new IndexOutOfBoundsException("ERROR: TileManager.preloadedTiles needs to be initialized with at least " + maxTileID + " slots. Application will crash now.");
			}
		}
		for(int i = TileManager.CHAR_HERO; i <= maxTileID; ++i) {
			preloadedTileIDs.add(i);
		}
		tileCache.loadTilesFor(preloadedTileIDs, r, preloadedTiles);
	}

	private final HashMap<String, HashSet<Integer>> tileIDsPerMap = new HashMap<String, HashSet<Integer>>();
	private void addTileIDsFor(HashSet<Integer> dest, String mapName, final Resources res, final WorldContext world) {
		HashSet<Integer> cachedTileIDs = tileIDsPerMap.get(mapName);
		if (cachedTileIDs == null) {
			PredefinedMap adjacentMap = world.maps.findPredefinedMap(mapName);
			if (adjacentMap == null) return;
			LayeredTileMap adjacentMapTiles = TMXMapTranslator.readLayeredTileMap(res, tileCache, adjacentMap);
			cachedTileIDs = getTileIDsFor(adjacentMap, adjacentMapTiles, world);
			tileIDsPerMap.put(mapName, cachedTileIDs);
		}
		dest.addAll(cachedTileIDs);
	}
	public void cacheAdjacentMaps(final Resources res, final WorldContext world, final PredefinedMap nextMap) {
		(new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... arg0) {
				adjacentMapTiles = null;

				HashSet<String> adjacentMapNames = new HashSet<String>();
				for (MapObject o : nextMap.eventObjects) {
					if (o.type != MapObject.MapObjectType.newmap) continue;
					if (o.map == null) continue;
					adjacentMapNames.add(o.map);
				}

				HashSet<Integer> tileIDs = new HashSet<Integer>();
				for (String mapName : adjacentMapNames) {
					addTileIDsFor(tileIDs, mapName, res, world);
				}

				adjacentMapTiles = tileCache.loadTilesFor(tileIDs, res);
				return null;
			}
		}).execute();
	}
}
