package com.nisovin.shopkeepers.ui.hiring;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;

public abstract class HiringView extends View {

	protected HiringView(HiringViewProvider provider, Player player, UIState uiState) {
		super(provider, player, uiState);
	}

	@Override
	protected void onInventoryClose(@Nullable InventoryCloseEvent closeEvent) {
		// Nothing to do by default.
	}

	@Override
	protected void onInventoryClickEarly(InventoryClickEvent event) {
		event.setCancelled(true);
	}

	@Override
	protected void onInventoryDragEarly(InventoryDragEvent event) {
		event.setCancelled(true);
	}
}
