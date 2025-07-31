package com.nisovin.shopkeepers.ui.confirmations;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.ui.SKDefaultUITypes;
import com.nisovin.shopkeepers.ui.lib.SimpleViewContext;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;
import com.nisovin.shopkeepers.ui.lib.ViewContext;
import com.nisovin.shopkeepers.ui.lib.ViewProvider;

public class ConfirmationViewProvider extends ViewProvider {

	private static final ViewContext VIEW_CONTEXT = new SimpleViewContext("confirmation");

	ConfirmationViewProvider() {
		super(SKDefaultUITypes.CONFIRMATION(), VIEW_CONTEXT);
	}

	@Override
	public boolean canAccess(Player player, boolean silent) {
		// Players cannot directly request this UI themselves. It is always opened for them in some
		// context.
		return true;
	}

	@Override
	protected @Nullable View createView(Player player, UIState uiState) {
		return new ConfirmationView(this, player, uiState);
	}
}
