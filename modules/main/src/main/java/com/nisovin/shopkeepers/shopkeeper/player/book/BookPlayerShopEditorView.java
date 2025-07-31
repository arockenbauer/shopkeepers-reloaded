package com.nisovin.shopkeepers.shopkeeper.player.book;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.config.Settings.DerivedSettings;
import com.nisovin.shopkeepers.currency.Currencies;
import com.nisovin.shopkeepers.shopkeeper.TradingRecipeDraft;
import com.nisovin.shopkeepers.shopkeeper.player.PlayerShopEditorView;
import com.nisovin.shopkeepers.ui.lib.UIState;

public class BookPlayerShopEditorView extends PlayerShopEditorView {

	protected BookPlayerShopEditorView(
			BookPlayerShopEditorViewProvider viewProvider,
			Player player,
			UIState uiState
	) {
		super(viewProvider, player, uiState);
	}

	@Override
	protected TradingRecipeDraft getEmptyTrade() {
		return DerivedSettings.bookEmptyTrade;
	}

	@Override
	protected TradingRecipeDraft getEmptyTradeSlotItems() {
		return DerivedSettings.bookEmptyTradeSlotItems;
	}

	@Override
	protected void handleTradesClick(InventoryClickEvent event) {
		var layout = this.getLayout();
		assert layout.isTradesArea(event.getRawSlot());
		Inventory inventory = this.getInventory();
		int rawSlot = event.getRawSlot();
		if (layout.isItem1Row(rawSlot)) {
			// Change the low cost, if this column contains a trade:
			ItemStack resultItem = this.getTradeResultItem(inventory, layout.getTradeColumn(rawSlot));
			if (resultItem == null) return;

			UnmodifiableItemStack emptySlotItem = this.getEmptyTradeSlotItems().getItem1();
			this.updateTradeCostItemOnClick(event, Currencies.getBase(), emptySlotItem);
		} else if (layout.isItem2Row(rawSlot)) {
			// Change the high cost, if this column contains a trade:
			ItemStack resultItem = this.getTradeResultItem(inventory, layout.getTradeColumn(rawSlot));
			if (resultItem == null) return;

			UnmodifiableItemStack emptySlotItem = this.getEmptyTradeSlotItems().getItem2();
			this.updateTradeCostItemOnClick(event, Currencies.getHighOrNull(), emptySlotItem);
		}
		// Result item row: Result items (books) are not modifiable.
	}
}
