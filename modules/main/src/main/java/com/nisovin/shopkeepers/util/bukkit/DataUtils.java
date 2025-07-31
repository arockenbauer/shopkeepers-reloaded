package com.nisovin.shopkeepers.util.bukkit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.util.annotations.ReadOnly;
import com.nisovin.shopkeepers.util.data.container.DataContainer;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;

/**
 * Utility functions related to loading and saving Bukkit, Minecraft, and plugin related objects
 * from and to {@link DataContainer}s.
 */
public final class DataUtils {

	public static @Nullable Material loadMaterial(DataContainer dataContainer, String key) {
		String materialName = dataContainer.getString(key);
		if (materialName == null) return null;
		Material material = ItemUtils.parseMaterial(materialName); // Can be null
		if (material != null && material.isLegacy()) {
			return null;
		}
		return material;
	}

	// Additional processing whenever we load serialized item stacks.
	// ReadOnly: Returns a new ItemStack instance if modifications are necessary.
	public static @Nullable ItemStack processLoadedItemStack(
			@ReadOnly @Nullable ItemStack loadedItemStack
	) {
		if (loadedItemStack == null) return null;
		// Note: Spigot creates Bukkit ItemStacks, whereas Paper automatically replaces the
		// deserialized Bukkit ItemStacks with CraftItemStacks. However, as long as the deserialized
		// item stack is not compared directly to an unmodifiable item stack (at least not without
		// first being wrapped into an unmodifiable item stack itself), and assuming that there are
		// no inconsistencies in how CraftItemStacks and Bukkit ItemStacks are compared with each
		// other, this difference should not be relevant to us.

		// TODO SPIGOT-6716, PAPER-6437: The order of stored enchantments of enchanted books is not
		// consistent. On Paper, where the deserialized ItemStacks end up being CraftItemStacks,
		// this difference in enchantment order can cause issues when these deserialized item stacks
		// are compared to other CraftItemStacks. Converting these deserialized CraftItemStacks back
		// to Bukkit ItemStacks ensures that the comparisons with other CraftItemStacks ignore the
		// enchantment order.
		ItemStack processed = loadedItemStack;
		if (loadedItemStack.getType() == Material.ENCHANTED_BOOK) {
			processed = ItemUtils.ensureBukkitItemStack(loadedItemStack);
		}
		return processed;
	}

	public static ItemStack processNonNullLoadedItemStack(@ReadOnly ItemStack loadedItemStack) {
		return Unsafe.assertNonNull(processLoadedItemStack(loadedItemStack));
	}

	private DataUtils() {
	}
}
