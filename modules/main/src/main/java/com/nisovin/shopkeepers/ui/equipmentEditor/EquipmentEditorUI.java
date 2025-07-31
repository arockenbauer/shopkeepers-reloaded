package com.nisovin.shopkeepers.ui.equipmentEditor;

import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.ui.lib.UISessionManager;
import com.nisovin.shopkeepers.util.java.Validate;

public class EquipmentEditorUI {

	public static boolean request(
			AbstractShopkeeper shopkeeper,
			Player player,
			EquipmentEditorUIState config
	) {
		Validate.notNull(shopkeeper, "shopkeeper is null");
		Validate.notNull(player, "player is null");
		Validate.notNull(config, "config is null");
		var viewProvider = new ShopkeeperEquipmentEditorViewProvider(shopkeeper);
		return UISessionManager.getInstance().requestUI(viewProvider, player, config);
	}

	private EquipmentEditorUI() {
	}
}
