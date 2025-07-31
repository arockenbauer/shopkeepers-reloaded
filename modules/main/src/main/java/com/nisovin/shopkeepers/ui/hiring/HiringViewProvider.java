package com.nisovin.shopkeepers.ui.hiring;

import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.ui.AbstractShopkeeperViewProvider;
import com.nisovin.shopkeepers.ui.lib.AbstractUIType;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;

public abstract class HiringViewProvider extends AbstractShopkeeperViewProvider {

	protected HiringViewProvider(AbstractUIType uiType, AbstractShopkeeper shopkeeper) {
		super(uiType, shopkeeper);
	}

	@Override
	public boolean canAccess(Player player, boolean silent) {
		Validate.notNull(player, "player is null");
		// Check for hire permission:
		if (!PermissionUtils.hasPermission(player, ShopkeepersPlugin.HIRE_PERMISSION)) {
			if (!silent) {
				this.debugNotOpeningUI(player, "Player is missing the hire permission.");
				TextUtils.sendMessage(player, Messages.missingHirePerm);
			}
			return false;
		}
		return true;
	}
}
