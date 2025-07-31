package com.nisovin.shopkeepers.villagers;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.util.java.Validate;

/**
 * InventoryHolder personnalisé pour l'éditeur d'œuf de création de shopkeeper.
 */
public class ShopCreationEggEditInventoryHolder implements InventoryHolder {

	private final ItemStack originalEgg;
	private Inventory inventory;
	private int currentPage = 0;
	private int totalPages = 1;

	public ShopCreationEggEditInventoryHolder(ItemStack originalEgg) {
		Validate.notNull(originalEgg, "originalEgg is null");
		this.originalEgg = originalEgg.clone();
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public ItemStack getOriginalEgg() {
		return originalEgg.clone();
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
}