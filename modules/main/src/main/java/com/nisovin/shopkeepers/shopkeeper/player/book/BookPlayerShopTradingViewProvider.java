package com.nisovin.shopkeepers.shopkeeper.player.book;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.shopkeeper.player.PlayerShopTradingViewProvider;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;

public class BookPlayerShopTradingViewProvider extends PlayerShopTradingViewProvider {

	protected BookPlayerShopTradingViewProvider(SKBookPlayerShopkeeper shopkeeper) {
		super(shopkeeper);
	}

	@Override
	public SKBookPlayerShopkeeper getShopkeeper() {
		return (SKBookPlayerShopkeeper) super.getShopkeeper();
	}

	@Override
	protected @Nullable View createView(Player player, UIState uiState) {
		return new BookPlayerShopTradingView(this, player, uiState);
	}
}
