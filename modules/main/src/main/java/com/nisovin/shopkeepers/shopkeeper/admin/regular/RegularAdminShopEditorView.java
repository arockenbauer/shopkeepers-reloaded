package com.nisovin.shopkeepers.shopkeeper.admin.regular;

import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.shopkeeper.TradingRecipeDraft;
import com.nisovin.shopkeepers.ui.editor.ShopkeeperEditorView;
import com.nisovin.shopkeepers.ui.lib.UIState;

public class RegularAdminShopEditorView extends ShopkeeperEditorView {

	protected RegularAdminShopEditorView(
			RegularAdminShopEditorViewProvider viewProvider,
			Player player,
			UIState uiState
	) {
		super(viewProvider, player, uiState);
	}

	// The admin shop editor does not restrict the interactions with the trade slots. If we were to
	// insert non-empty placeholder items here, the editing player would be able to pick them up.
	@Override
	protected TradingRecipeDraft getEmptyTrade() {
		return TradingRecipeDraft.EMPTY;
	}

	@Override
	protected TradingRecipeDraft getEmptyTradeSlotItems() {
		return TradingRecipeDraft.EMPTY;
	}
}
