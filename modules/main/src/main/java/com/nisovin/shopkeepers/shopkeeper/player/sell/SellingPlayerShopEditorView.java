package com.nisovin.shopkeepers.shopkeeper.player.sell;

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

public class SellingPlayerShopEditorView extends PlayerShopEditorView {

	protected SellingPlayerShopEditorView(
			SellingPlayerShopEditorViewProvider viewProvider,
			Player player,
			UIState uiState
	) {
		super(viewProvider, player, uiState);
	}

	@Override
	protected TradingRecipeDraft getEmptyTrade() {
		return DerivedSettings.sellingEmptyTrade;
	}

	@Override
	protected TradingRecipeDraft getEmptyTradeSlotItems() {
		return DerivedSettings.sellingEmptyTradeSlotItems;
	}

	@Override
	protected void handleTradesClick(InventoryClickEvent event) {
		var layout = this.getLayout();
		assert layout.isTradesArea(event.getRawSlot());
		Inventory inventory = this.getInventory();
		int rawSlot = event.getRawSlot();
		if (layout.isResultRow(rawSlot)) {
			// Change the stack size of the sold item, if this column contains a trade:
			UnmodifiableItemStack emptySlotItem = this.getEmptyTrade().getResultItem();
			this.updateItemAmountOnClick(event, 1, emptySlotItem);
		} else if (layout.isItem1Row(rawSlot)) {
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
	}
}
