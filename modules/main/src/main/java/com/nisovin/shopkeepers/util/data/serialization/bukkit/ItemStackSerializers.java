package com.nisovin.shopkeepers.util.data.serialization.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.compat.Compat;
import com.nisovin.shopkeepers.util.bukkit.DataUtils;
import com.nisovin.shopkeepers.util.data.container.DataContainer;
import com.nisovin.shopkeepers.util.data.property.BasicProperty;
import com.nisovin.shopkeepers.util.data.property.Property;
import com.nisovin.shopkeepers.util.data.serialization.DataSerializer;
import com.nisovin.shopkeepers.util.data.serialization.InvalidDataException;
import com.nisovin.shopkeepers.util.data.serialization.MissingDataException;
import com.nisovin.shopkeepers.util.data.serialization.java.DataContainerSerializers;
import com.nisovin.shopkeepers.util.data.serialization.java.NumberSerializers;
import com.nisovin.shopkeepers.util.inventory.ItemStackComponentsData;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * Default {@link DataSerializer}s for {@link ItemStack} values.
 */
public final class ItemStackSerializers {

	private static final Property<Integer> DATA_VERSION = new BasicProperty<Integer>()
			.dataKeyAccessor("DataVersion", NumberSerializers.INTEGER)
			// If no data version is specified, we assume the current data version (i.e. no data
			// migrations are applied).
			.defaultValue(Bukkit.getUnsafe().getDataVersion())
			.build();
	private static final Property<NamespacedKey> ID = new BasicProperty<NamespacedKey>()
			.dataKeyAccessor("id", NamespacedKeySerializers.DEFAULT)
			.build();
	private static final Property<Integer> COUNT = new BasicProperty<Integer>()
			.dataKeyAccessor("count", NumberSerializers.INTEGER)
			.defaultValue(1)
			.build();
	private static final Property<ItemStackComponentsData> COMPONENTS = new BasicProperty<ItemStackComponentsData>()
			.dataKeyAccessor("components", new DataSerializer<ItemStackComponentsData>() {
				@Override
				public @Nullable Object serialize(ItemStackComponentsData value) {
					return DataContainerSerializers.DEFAULT.serialize(value);
				}

				@Override
				public ItemStackComponentsData deserialize(Object data) throws InvalidDataException {
					DataContainer dataContainer = DataContainerSerializers.DEFAULT.deserialize(data);
					return ItemStackComponentsData.ofNonNull(dataContainer);
				}
			})
			.nullable()
			.build();

	/**
	 * A {@link DataSerializer} for {@link ItemStack} values. This uses the built-in Bukkit
	 * serialization.
	 */
	public static final DataSerializer<ItemStack> DEFAULT = new DataSerializer<ItemStack>() {
		@Override
		public @Nullable Object serialize(ItemStack value) {
			Validate.notNull(value, "value is null");
			return value;
		}

		@Override
		public ItemStack deserialize(Object data) throws InvalidDataException {
			Validate.notNull(data, "data is null");
			if (data instanceof ItemStack itemStack) {
				return DataUtils.processNonNullLoadedItemStack(itemStack);
			} else if (data instanceof UnmodifiableItemStack unmodifiableItemStack) {
				// We also support UnmodifiableItemStacks here, but return a copy of the item stack,
				// because we don't know how the returned ItemStack will be used by the caller, i.e.
				// whether it is expected to be modifiable.
				// Note: We don't expect the additional ItemStack processing of
				// DataUtils.processLoadedItemStack to be required in these cases, because the item
				// stack is not freshly deserialized.
				return unmodifiableItemStack.copy();
			} else {
				var dataContainer = DataContainerSerializers.DEFAULT.deserialize(data);
				try {
					var dataVersion = dataContainer.get(DATA_VERSION);
					var id = dataContainer.get(ID);
					var count = dataContainer.get(COUNT);
					var componentsData = dataContainer.get(COMPONENTS);

					@Nullable ItemStack itemStack;
					try {
						itemStack = Compat.getProvider()
								.deserializeItemStack(dataVersion, id, count, componentsData);
					} catch (Exception e) {
						throw new InvalidDataException("Failed to deserialize ItemStack!", e);
					}

					if (Unsafe.nullable(itemStack) == null) {
						throw new InvalidDataException("Loaded ItemStack is null!");
					}

					itemStack = DataUtils.processNonNullLoadedItemStack(itemStack);

					return itemStack;
				} catch (MissingDataException e) {
					throw new InvalidDataException(e.getMessage(), e);
				}
			}
		}
	};

	/**
	 * A {@link DataSerializer} for {@link UnmodifiableItemStack} values. This uses our own item
	 * stack serialization that saves item stack components as SNBT.
	 */
	public static final DataSerializer<UnmodifiableItemStack> UNMODIFIABLE = new DataSerializer<UnmodifiableItemStack>() {
		@Override
		public @Nullable Object serialize(UnmodifiableItemStack value) {
			Validate.notNull(value, "value is null");
			// Note: Be sure to always return a new object instance here to prevent SnakeYaml from
			// representing the item stack using anchors and aliases if the same item stack instance
			// is saved to the same Yaml document multiple times in different contexts.

			var dataContainer = DataContainer.create();
			dataContainer.set(DATA_VERSION, Bukkit.getUnsafe().getDataVersion());
			// getKey instead of getKeyOrThrow: Compatible with both Spigot and Paper.
			dataContainer.set(ID, value.getType().getKey());
			dataContainer.set(COUNT, value.getAmount());
			// TODO: Saving the itemstack to get its data can result in an error if Minecraft finds
			// the item data to be invalid. Example: Entity data component with missing "id".
			// Ideally, we want to detect such issues early, e.g. when loading shopkeepers, and we
			// want this to only affect the particular trade or shopkeeper and not prevent the
			// saving of other data.
			// However, it is unclear how the invalid item data can end up inside the shopkeeper in
			// the first place: Loading the shopkeeper with the invalid data already fails, and the
			// give command also already detects invalid data up-front.
			var componentsData = ItemStackComponentsData.of(ItemUtils.asItemStack(value));
			dataContainer.set(COMPONENTS, componentsData); // Omitted if null
			return dataContainer.serialize();
		}

		@Override
		public UnmodifiableItemStack deserialize(Object data) throws InvalidDataException {
			// If the data is already an UnmodifiableItemStack, return it without the copying that
			// would be done by DEFAULT.deserialize.
			// Note: We don't expect the additional ItemStack processing of
			// DataUtils.processLoadedItemStack to be required here, because the item stack is not
			// freshly deserialized.
			if (data instanceof UnmodifiableItemStack unmodifiableItemStack) {
				return unmodifiableItemStack;
			}

			// Else: Try to load it as a normal ItemStack:
			return UnmodifiableItemStack.ofNonNull(DEFAULT.deserialize(data));
		}
	};

	private ItemStackSerializers() {
	}
}
