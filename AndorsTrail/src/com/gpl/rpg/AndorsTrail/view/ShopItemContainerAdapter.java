package com.gpl.rpg.AndorsTrail.view;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.controller.ItemController;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer.ItemEntry;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileCollection;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileManager;

public final class ShopItemContainerAdapter extends ArrayAdapter<ItemEntry> {
	private final TileManager tileManager;
	private final TileCollection tileCollection;
	private final OnContainerItemClickedListener clickListener;
	private final boolean isSelling;
	private final Resources r;
	private final Player player;

	public ShopItemContainerAdapter(Context context, TileCollection tileCollection, TileManager tileManager, Player player, ItemContainer items, OnContainerItemClickedListener clickListener, boolean isSelling) {
		super(context, 0, items.items);
		this.tileManager = tileManager;
		this.tileCollection = tileCollection;
		this.player = player;
		this.clickListener = clickListener;
		this.isSelling = isSelling;
		this.r = context.getResources();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ItemEntry item = getItem(position);
		final ItemType itemType = item.itemType;

		View result = convertView;
		if (result == null) {
			result = View.inflate(getContext(), R.layout.shopitemview, null);
		}

		tileManager.setImageViewTile(r, (ImageView) result.findViewById(R.id.shopitem_image), itemType, tileCollection);
		((TextView) result.findViewById(R.id.shopitem_text)).setText(ItemController.describeItemForListView(item, player));
		Button b = (Button) result.findViewById(R.id.shopitem_shopbutton);
		if (isSelling) {
			b.setText(r.getString(R.string.shop_sellitem, ItemController.getSellingPrice(player, itemType)));
			b.setEnabled(ItemController.maySellItem(player, itemType));
		} else {
			int price = ItemController.getBuyingPrice(player, itemType);
			b.setText(r.getString(R.string.shop_buyitem, price));
			b.setEnabled(ItemController.canAfford(player, price));
		}
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				clickListener.onItemActionClicked(position, itemType);
			}
		});
		b = (Button) result.findViewById(R.id.shopitem_infobutton);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				clickListener.onItemInfoClicked(position, itemType);
			}
		});
		return result;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).itemType.id.hashCode();
	}

	public static interface OnContainerItemClickedListener {
		void onItemActionClicked(int position, ItemType itemType);
		void onItemInfoClicked(int position, ItemType itemType);
	}

	public void reloadShownSort(int selection, ItemContainer container, Player p){
		ItemContainerAdapter.reloadShownSort(selection, container, player);
	}
}
