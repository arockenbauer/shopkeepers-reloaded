package com.nisovin.shopkeepers.ui.confirmations;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;
import com.nisovin.shopkeepers.ui.lib.ViewProvider;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;

public class ConfirmationView extends View {

	private static final int INVENTORY_SIZE = 9;
	private static final int SLOT_CONFIRM = 0;
	private static final int SLOT_CANCEL = 8;

	private boolean playerDecided = false;

	ConfirmationView(ViewProvider provider, Player player, UIState uiState) {
		super(provider, player, uiState);
	}

	@Override
	public boolean isAcceptedState(UIState uiState) {
		return uiState instanceof ConfirmationUIState;
	}

	// Restoring UI State: Not supported.

	private ConfirmationUIState getConfig() {
		return (ConfirmationUIState) this.getInitialUIState();
	}

	@Override
	protected @Nullable InventoryView openInventoryView() {
		var config = this.getConfig();
		var inventory = Bukkit.createInventory(null, INVENTORY_SIZE, config.getTitle());
		this.updateInventory(inventory);

		Player player = this.getPlayer();
		return player.openInventory(inventory);
	}

	private void updateInventory(Inventory inventory) {
		var config = this.getConfig();

		ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		confirmItem = ItemUtils.setDisplayNameAndLore(
				confirmItem,
				Messages.confirmationUiConfirm,
				config.getConfirmationLore()
		);
		inventory.setItem(SLOT_CONFIRM, confirmItem);

		ItemStack cancelItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		cancelItem = ItemUtils.setDisplayNameAndLore(
				cancelItem,
				Messages.confirmationUiCancel,
				Messages.confirmationUiCancelLore
		);
		inventory.setItem(SLOT_CANCEL, cancelItem);
	}

	@Override
	public void updateInventory() {
		var inventory = this.getInventory();
		this.updateInventory(inventory);
		this.syncInventory();
	}

	@Override
	protected void onInventoryClickEarly(InventoryClickEvent event) {
		event.setCancelled(true);
		if (this.isAutomaticShiftLeftClick()) {
			// Ignore automatically triggered shift left-clicks:
			return;
		}

		var config = this.getConfig();
		int slot = event.getRawSlot();
		if (slot == SLOT_CONFIRM) {
			playerDecided = true;
			this.closeDelayedAndRunTask(config.getAction());
		} else if (slot == SLOT_CANCEL) {
			playerDecided = true;
			this.closeDelayedAndRunTask(config.getOnCancelled());
		}
	}

	@Override
	protected void onInventoryDragEarly(InventoryDragEvent event) {
		event.setCancelled(true);
	}

	@Override
	protected void onInventoryClose(@Nullable InventoryCloseEvent closeEvent) {
		if (!playerDecided) {
			Player player = this.getPlayer();
			TextUtils.sendMessage(player, Messages.confirmationUiAborted);
		}
	}
}
