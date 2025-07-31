package com.nisovin.shopkeepers.ui;

import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.events.PlayerOpenUIEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperOpenUIEvent;
import com.nisovin.shopkeepers.ui.lib.UISessionManager;
import com.nisovin.shopkeepers.ui.lib.UISessionManager.SessionHandler;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.ViewProvider;
import com.nisovin.shopkeepers.util.java.Validate;

public class SKUISystem {

	private static final SessionHandler UI_SESSION_HANDLER = new SessionHandler() {
		@Override
		public PlayerOpenUIEvent createPlayerOpenUIEvent(
				ViewProvider viewProvider,
				Player player,
				boolean silentRequest,
				UIState uiState
		) {
			if (viewProvider instanceof ShopkeeperViewProvider) {
				var shopkeeper = ((ShopkeeperViewProvider) viewProvider).getShopkeeper();
				return new ShopkeeperOpenUIEvent(
						shopkeeper,
						viewProvider.getUIType(),
						player,
						silentRequest
				);
			} else {
				return SessionHandler.super.createPlayerOpenUIEvent(
						viewProvider,
						player,
						silentRequest,
						uiState
				);
			}
		}
	};

	public SKUISystem(ShopkeepersPlugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		UISessionManager.initialize(plugin, UI_SESSION_HANDLER);
	}

	public void onEnable() {
		UISessionManager.getInstance().onEnable();
	}

	public void onDisable() {
		UISessionManager.getInstance().onDisable();
	}
}
