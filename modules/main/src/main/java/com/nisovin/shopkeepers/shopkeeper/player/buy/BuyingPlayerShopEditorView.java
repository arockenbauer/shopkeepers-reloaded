package com.nisovin.shopkeepers.shopkeeper.player.buy;

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

public class BuyingPlayerShopEditorView extends PlayerShopEditorView {

	protected BuyingPlayerShopEditorView(
			BuyingPlayerShopEditorViewProvider viewProvider,
			Player player,
			UIState uiState
	) {
		super(viewProvider, player, uiState);
	}

	@Override
	protected TradingRecipeDraft getEmptyTrade() {
		return DerivedSettings.buyingEmptyTrade;
	}

	@Override
	protected TradingRecipeDraft getEmptyTradeSlotItems() {
		return DerivedSettings.buyingEmptyTradeSlotItems;
	}

	@Override
	protected void handleTradesClick(InventoryClickEvent event) {
		var layout = this.getLayout();
		assert layout.isTradesArea(event.getRawSlot());
		Inventory inventory = this.getInventory();
		int rawSlot = event.getRawSlot();
		if (layout.isResultRow(rawSlot)) {
			// Modify the cost, if this column contains a trade:
			ItemStack tradedItem = this.getTradeItem1(inventory, layout.getTradeColumn(rawSlot));
			if (tradedItem == null) return;

			UnmodifiableItemStack emptySlotItem = this.getEmptyTradeSlotItems().getResultItem();
			this.updateTradeCostItemOnClick(event, Currencies.getBase(), emptySlotItem);
		} else if (layout.isItem1Row(rawSlot)) {
			// Modify the bought item quantity, if this column contains a trade:
			UnmodifiableItemStack emptySlotItem = this.getEmptyTradeSlotItems().getItem1();
			this.updateItemAmountOnClick(event, 1, emptySlotItem);
		}
		// Item2 row: Not used by the buying shop.
	}
}
