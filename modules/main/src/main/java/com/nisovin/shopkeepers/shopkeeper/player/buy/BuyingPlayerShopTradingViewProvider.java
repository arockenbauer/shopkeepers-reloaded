package com.nisovin.shopkeepers.shopkeeper.player.buy;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.shopkeeper.player.PlayerShopTradingViewProvider;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;

public class BuyingPlayerShopTradingViewProvider extends PlayerShopTradingViewProvider {

	protected BuyingPlayerShopTradingViewProvider(SKBuyingPlayerShopkeeper shopkeeper) {
		super(shopkeeper);
	}

	@Override
	public SKBuyingPlayerShopkeeper getShopkeeper() {
		return (SKBuyingPlayerShopkeeper) super.getShopkeeper();
	}

	@Override
	protected @Nullable View createView(Player player, UIState uiState) {
		return new BuyingPlayerShopTradingView(this, player, uiState);
	}
}
