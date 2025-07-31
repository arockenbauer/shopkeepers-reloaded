package com.nisovin.shopkeepers.shopkeeper.player;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;
import com.nisovin.shopkeepers.ui.trading.TradingViewProvider;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;

public abstract class PlayerShopTradingViewProvider extends TradingViewProvider {

	protected PlayerShopTradingViewProvider(AbstractPlayerShopkeeper shopkeeper) {
		super(shopkeeper);
	}

	@Override
	public AbstractPlayerShopkeeper getShopkeeper() {
		return (AbstractPlayerShopkeeper) super.getShopkeeper();
	}

	@Override
	public boolean canOpen(Player player, boolean silent) {
		if (!super.canOpen(player, silent)) return false;
		PlayerShopkeeper shopkeeper = this.getShopkeeper();

		// Stop opening if trading shall be prevented while the owner is offline:
		if (Settings.preventTradingWhileOwnerIsOnline
				&& !PermissionUtils.hasPermission(player, ShopkeepersPlugin.BYPASS_PERMISSION)) {
			Player ownerPlayer = shopkeeper.getOwner();
			if (ownerPlayer != null) {
				if (!silent) {
					this.debugNotOpeningUI(player, "Shop owner is online.");
					TextUtils.sendMessage(player, Messages.cannotTradeWhileOwnerOnline,
							"owner", Unsafe.assertNonNull(ownerPlayer.getName())
					);
				}
				return false;
			}
		}

		return true;
	}

	@Override
	protected @Nullable View createView(Player player, UIState uiState) {
		return new PlayerShopTradingView(this, player, uiState);
	}
}
