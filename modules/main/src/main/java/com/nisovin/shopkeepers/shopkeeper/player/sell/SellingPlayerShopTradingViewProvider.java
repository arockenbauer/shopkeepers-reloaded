package com.nisovin.shopkeepers.shopkeeper.player.sell;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.shopkeeper.player.PlayerShopTradingViewProvider;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;

public class SellingPlayerShopTradingViewProvider extends PlayerShopTradingViewProvider {

	protected SellingPlayerShopTradingViewProvider(SKSellingPlayerShopkeeper shopkeeper) {
		super(shopkeeper);
	}

	@Override
	public SKSellingPlayerShopkeeper getShopkeeper() {
		return (SKSellingPlayerShopkeeper) super.getShopkeeper();
	}

	@Override
	protected @Nullable View createView(Player player, UIState uiState) {
		return new SellingPlayerShopTradingView(this, player, uiState);
	}
}
