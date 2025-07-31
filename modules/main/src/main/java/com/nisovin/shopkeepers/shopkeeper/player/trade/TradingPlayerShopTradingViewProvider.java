package com.nisovin.shopkeepers.shopkeeper.player.trade;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.shopkeeper.player.PlayerShopTradingViewProvider;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;

public class TradingPlayerShopTradingViewProvider extends PlayerShopTradingViewProvider {

	protected TradingPlayerShopTradingViewProvider(SKTradingPlayerShopkeeper shopkeeper) {
		super(shopkeeper);
	}

	@Override
	public SKTradingPlayerShopkeeper getShopkeeper() {
		return (SKTradingPlayerShopkeeper) super.getShopkeeper();
	}

	@Override
	protected @Nullable View createView(Player player, UIState uiState) {
		return new TradingPlayerShopTradingView(this, player, uiState);
	}
}
