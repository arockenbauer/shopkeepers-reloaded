package com.nisovin.shopkeepers.ui.trading;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.ui.AbstractShopkeeperViewProvider;
import com.nisovin.shopkeepers.ui.SKDefaultUITypes;
import com.nisovin.shopkeepers.ui.lib.AbstractUIType;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

public class TradingViewProvider extends AbstractShopkeeperViewProvider {

	private final List<TradingListener> tradingListeners = new ArrayList<>();

	public TradingViewProvider(AbstractShopkeeper shopkeeper) {
		this(SKDefaultUITypes.TRADING(), shopkeeper);
	}

	public TradingViewProvider(AbstractUIType uiType, AbstractShopkeeper shopkeeper) {
		super(uiType, shopkeeper);
	}

	/**
	 * Registers the given {@link TradingListener}.
	 * 
	 * @param listener
	 *            the listener, not <code>null</code>
	 */
	public final void addListener(TradingListener listener) {
		Validate.notNull(listener, "listener is null");
		tradingListeners.add(listener);
	}

	/**
	 * Gets the registered {@link TradingListener}s.
	 * 
	 * @return the {@link TradingListener}s, not <code>null</code>
	 */
	final List<TradingListener> getTradingListeners() {
		return tradingListeners;
	}

	@Override
	public boolean canAccess(Player player, boolean silent) {
		Validate.notNull(player, "player is null");
		if (!PermissionUtils.hasPermission(player, ShopkeepersPlugin.TRADE_PERMISSION)) {
			if (!silent) {
				this.debugNotOpeningUI(player, "Player is missing trade permission.");
				TextUtils.sendMessage(player, Messages.missingTradePerm);
			}
			return false;
		}

		return true;
	}

	@Override
	public boolean canOpen(Player player, boolean silent) {
		Validate.notNull(player, "player is null");
		if (!super.canOpen(player, silent)) return false;

		AbstractShopkeeper shopkeeper = this.getShopkeeper();
		if (!shopkeeper.hasTradingRecipes(player)) {
			if (!silent) {
				this.debugNotOpeningUI(player, "Shopkeeper has no offers.");
				TextUtils.sendMessage(player, Messages.cannotTradeNoOffers);

				// If the player can edit the shopkeeper, send instructions on how to open the
				// editor:
				if (shopkeeper.canEdit(player, true)) {
					TextUtils.sendMessage(player, Messages.noOffersOpenEditorDescription);
				}
			}
			return false;
		}

		return true;
	}

	@Override
	protected @Nullable View createView(Player player, UIState uiState) {
		return new TradingView(this, player, uiState);
	}
}
