package com.nisovin.shopkeepers.shopkeeper.player;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.ui.SKDefaultUITypes;
import com.nisovin.shopkeepers.ui.hiring.HiringViewProvider;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;

public class PlayerShopHiringViewProvider extends HiringViewProvider {

	public PlayerShopHiringViewProvider(AbstractPlayerShopkeeper shopkeeper) {
		super(SKDefaultUITypes.HIRING(), shopkeeper);
	}

	@Override
	public AbstractPlayerShopkeeper getShopkeeper() {
		return (AbstractPlayerShopkeeper) super.getShopkeeper();
	}

	@Override
	public boolean canOpen(Player player, boolean silent) {
		if (!super.canOpen(player, silent)) return false;

		if (!this.getShopkeeper().isForHire()) {
			if (!silent) {
				this.debugNotOpeningUI(player, "The shopkeeper is not for hire.");
				// TODO User message "Not for hire"
			}
			return false;
		}

		return true;
	}

	@Override
	protected @Nullable View createView(Player player, UIState uiState) {
		return new PlayerShopHiringView(this, player, uiState);
	}
}
