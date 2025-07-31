package com.nisovin.shopkeepers.util.inventory;

import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.compat.Compat;
import com.nisovin.shopkeepers.util.annotations.ReadOnly;
import com.nisovin.shopkeepers.util.data.container.DataContainer;
import com.nisovin.shopkeepers.util.data.container.DelegateDataContainer;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * A wrapper around the {@link DataContainer} that contains an {@link ItemStack}'s component data.
 * <p>
 * This wrapper is expected to read and write through to the underlying data container. It shall not
 * copy or derive any state, because other components may directly access the underlying data
 * container and thereby bypass this wrapper.
 * <p>
 * The exact representation of the item stack component data is undefined, but is guaranteed to be
 * serializable. See the serializable types in {@link DataContainer}.
 */
public final class ItemStackComponentsData extends DelegateDataContainer {

	/**
	 * Creates an {@link ItemStackComponentsData} for the given {@link ItemStack}.
	 * 
	 * @param itemStack
	 *            the item stack, not <code>null</code>
	 * @return the {@link ItemStackComponentsData}, or <code>null</code> if the item has no
	 *         components data
	 */
	public static @Nullable ItemStackComponentsData of(@ReadOnly ItemStack itemStack) {
		return Compat.getProvider().getItemStackComponentsData(itemStack);
	}

	/**
	 * Gets an {@link ItemStackComponentsData} for the given data container.
	 * <p>
	 * If the given data container is already an {@link ItemStackComponentsData}, this returns the
	 * given data container itself. Otherwise, this returns a wrapper around the given data
	 * container.
	 * 
	 * @param dataContainer
	 *            the data container that contains the item stack components data, or
	 *            <code>null</code>
	 * @return the {@link ItemStackComponentsData} or <code>null</code> if the given data container
	 *         is <code>null</code>
	 */
	public static @PolyNull ItemStackComponentsData of(@PolyNull DataContainer dataContainer) {
		if (dataContainer == null) return null;
		if (dataContainer instanceof ItemStackComponentsData itemStackComponentsData) {
			return itemStackComponentsData;
		} else {
			return new ItemStackComponentsData(dataContainer);
		}
	}

	/**
	 * Gets an {@link ItemStackComponentsData} for the given data container.
	 * <p>
	 * Unlike {@link #of(DataContainer)}, this method does not accept <code>null</code> as input and
	 * ensures that no <code>null</code> value is returned.
	 * 
	 * @param dataContainer
	 *            the data container that contains the item stack components data, not
	 *            <code>null</code>
	 * @return the {@link ItemStackComponentsData} data, not <code>null</code>
	 */
	public static ItemStackComponentsData ofNonNull(DataContainer dataContainer) {
		Validate.notNull(dataContainer, "dataContainer is null");
		return Unsafe.assertNonNull(of(dataContainer));
	}

	/**
	 * Creates a new {@link ItemStackComponentsData}.
	 * 
	 * @param dataContainer
	 *            the underlying data container, not <code>null</code>
	 */
	protected ItemStackComponentsData(DataContainer dataContainer) {
		super(dataContainer);
	}
}
