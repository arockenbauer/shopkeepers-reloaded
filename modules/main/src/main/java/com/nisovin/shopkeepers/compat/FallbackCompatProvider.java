package com.nisovin.shopkeepers.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.util.annotations.ReadOnly;
import com.nisovin.shopkeepers.util.bukkit.ServerUtils;
import com.nisovin.shopkeepers.util.data.container.DataContainer;
import com.nisovin.shopkeepers.util.inventory.ItemStackComponentsData;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

// This assumes Spigot mappings. Fallback for server implementations with other mappings (e.g.
// Paper) is not supported currently.
public final class FallbackCompatProvider implements CompatProvider {

	public static final String VERSION_ID = "fallback";

	// Minecraft
	private final Class<?> nmsEntityClass;
	private final Method nmsEntitySetOnGroundMethod;
	private final Object nmsMinecraftRegistry;
	private final Class<?> nmsCompoundTagClass;
	private final Method nmsCompoundTagGetMethod;
	private final Method nmsCompoundTagGetStringOrMethod;
	private final Method nmsCompoundTagPutStringMethod;
	private final Method nmsCompoundTagPutIntMethod;
	private final Method nmsCompoundTagPutMethod;
	private final Method nmsCompoundTagForEachMethod;
	private final Method nmsHolderLookupProviderCreateSerializationContextMethod;
	private final Class<?> nmsItemStackClass;
	private final Object nmsItemStackCodec;
	private final Method nmsCodecEncodeStartMethod;
	private final Method nmsCodecParseMethod;
	private final Method nmsDataResultGetOrThrowMethod;
	private final Object nmsNbtOps;
	private final Object nmsTagParser;
	private final Method nmsTagParserParseFullyMethod;
	private final Object nmsDataFixer;
	private final Method nmsDataFixerUpdateMethod;
	private final Object nmsDataFixerTypeItemStack;
	private final Constructor<?> nmsDynamicConstructor;
	private final Method nmsDynamicGetValueMethod;

	// CraftBukkit
	private final Class<?> obcCraftItemStackClass;
	private final Field obcCraftItemStackHandleField;
	private final Method obcCraftItemStackAsNMSCopyMethod;
	private final Method obcCraftItemStackAsCraftMirrorMethod;

	private final Class<?> obcCraftEntityClass;
	private final Method obcGetHandleMethod;

	// Bukkit

	// null if not supported yet (e.g. pre 1.21):
	private @Nullable Method b_EntityExplodeEvent_GetExplosionResultMethod = null;
	private @Nullable Method b_BlockExplodeEvent_GetExplosionResultMethod = null;

	public FallbackCompatProvider() throws Exception {
		String cbPackage = ServerUtils.getCraftBukkitPackage();

		// Minecraft

		nmsEntityClass = Class.forName("net.minecraft.world.entity.Entity");
		nmsEntitySetOnGroundMethod = nmsEntityClass.getDeclaredMethod(
				"e", // setOnGround
				boolean.class
		);

		var obcCraftRegistryClass = Class.forName(cbPackage + ".CraftRegistry");
		var obcGetMinecraftRegistryMethod = obcCraftRegistryClass.getMethod("getMinecraftRegistry");
		nmsMinecraftRegistry = Unsafe.assertNonNull(obcGetMinecraftRegistryMethod.invoke(null));

		var nmsTagClass = Class.forName("net.minecraft.nbt.NBTBase"); // Tag
		nmsCompoundTagClass = Class.forName("net.minecraft.nbt.NBTTagCompound"); // CompoundTag
		nmsCompoundTagGetMethod = nmsCompoundTagClass.getDeclaredMethod("a", String.class); // get
		// getStringOr
		nmsCompoundTagGetStringOrMethod = nmsCompoundTagClass.getDeclaredMethod("b", String.class, String.class);
		// putString
		nmsCompoundTagPutStringMethod = nmsCompoundTagClass.getDeclaredMethod("a", String.class, String.class);
		// putInt
		nmsCompoundTagPutIntMethod = nmsCompoundTagClass.getDeclaredMethod("a", String.class, int.class);
		// put
		nmsCompoundTagPutMethod = nmsCompoundTagClass.getMethod("a", String.class, nmsTagClass);
		// forEach
		nmsCompoundTagForEachMethod = nmsCompoundTagClass.getDeclaredMethod("a", BiConsumer.class);

		var dynamicOpsClass = Class.forName("com.mojang.serialization.DynamicOps");
		// HolderLookup.Provider
		var nmsHolderLookupProviderClass = Class.forName("net.minecraft.core.HolderLookup$a");
		nmsHolderLookupProviderCreateSerializationContextMethod = nmsHolderLookupProviderClass.getDeclaredMethod(
				"a", // createSerializationContext
				dynamicOpsClass
		);

		nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");

		var nmsItemStackCodecField = nmsItemStackClass.getDeclaredField("b"); // CODEC
		nmsItemStackCodec = Unsafe.assertNonNull(nmsItemStackCodecField.get(null));
		nmsCodecEncodeStartMethod = nmsItemStackCodec.getClass().getMethod(
				"encodeStart",
				dynamicOpsClass,
				Object.class
		);
		nmsCodecParseMethod = nmsItemStackCodec.getClass().getMethod(
				"parse",
				dynamicOpsClass,
				Object.class
		);

		var nmsDataResultClass = Class.forName("com.mojang.serialization.DataResult");
		nmsDataResultGetOrThrowMethod = nmsDataResultClass.getDeclaredMethod("getOrThrow");

		var nmsNbtOpsClass = Class.forName("net.minecraft.nbt.DynamicOpsNBT"); // NbtOps
		var nmsNbtOpsInstanceField = nmsNbtOpsClass.getField("a"); // INSTANCE
		nmsNbtOps = Unsafe.assertNonNull(nmsNbtOpsInstanceField.get(null));

		var nmsTagParserClass = Class.forName("net.minecraft.nbt.MojangsonParser"); // TagParser
		// create
		var nmsTagParserCreateMethod = nmsTagParserClass.getDeclaredMethod("a", dynamicOpsClass);
		nmsTagParser = Unsafe.assertNonNull(nmsTagParserCreateMethod.invoke(null, nmsNbtOps));
		// parseFully
		nmsTagParserParseFullyMethod = nmsTagParserClass.getDeclaredMethod("b", String.class);

		var nmsDynamicClass = Class.forName("com.mojang.serialization.Dynamic");
		// DataFixers
		var nmsDataFixersClass = Class.forName("net.minecraft.util.datafix.DataConverterRegistry");
		// getDataFixer
		var nmsDataFixerGetDataFixerMethod = nmsDataFixersClass.getDeclaredMethod("a");
		nmsDataFixer = Unsafe.assertNonNull(nmsDataFixerGetDataFixerMethod.invoke(null));
		var nmsDataFixerTypeReferenceClass = Class.forName("com.mojang.datafixers.DSL$TypeReference");
		var nmsDataFixerTypeReferenceTypeNameMethod = nmsDataFixerTypeReferenceClass.getDeclaredMethod("typeName");
		var nmsDataFixerClass = Class.forName("com.mojang.datafixers.DataFixer");
		nmsDataFixerUpdateMethod = nmsDataFixerClass.getDeclaredMethod(
				"update",
				nmsDataFixerTypeReferenceClass,
				nmsDynamicClass,
				int.class,
				int.class
		);

		// References
		var nmsDataFixerReferencesClass = Class.forName("net.minecraft.util.datafix.fixes.DataConverterTypes");
		// ITEM_STACK
		nmsDataFixerTypeItemStack = Unsafe.assertNonNull(nmsDataFixerReferencesClass.getField("u").get(null));
		if (!"item_stack".equals(nmsDataFixerTypeReferenceTypeNameMethod.invoke(nmsDataFixerTypeItemStack))) {
			throw new IllegalStateException("Failed to retrieve the item stack datafixer type reference!");
		}

		nmsDynamicConstructor = nmsDynamicClass.getConstructor(dynamicOpsClass, Object.class);
		nmsDynamicGetValueMethod = nmsDynamicClass.getDeclaredMethod("getValue");

		// CraftBukkit

		obcCraftItemStackClass = Class.forName(cbPackage + ".inventory.CraftItemStack");
		obcCraftItemStackHandleField = obcCraftItemStackClass.getDeclaredField("handle");
		obcCraftItemStackHandleField.setAccessible(true);
		obcCraftItemStackAsNMSCopyMethod = obcCraftItemStackClass.getDeclaredMethod(
				"asNMSCopy",
				ItemStack.class
		);
		obcCraftItemStackAsCraftMirrorMethod = obcCraftItemStackClass.getDeclaredMethod(
				"asCraftMirror",
				nmsItemStackClass
		);

		obcCraftEntityClass = Class.forName(cbPackage + ".entity.CraftEntity");
		obcGetHandleMethod = obcCraftEntityClass.getDeclaredMethod("getHandle");

		// Bukkit

		try {
			b_EntityExplodeEvent_GetExplosionResultMethod = EntityExplodeEvent.class.getMethod("getExplosionResult");
			b_BlockExplodeEvent_GetExplosionResultMethod = BlockExplodeEvent.class.getMethod("getExplosionResult");
		} catch (NoSuchMethodException e) {
			// Not found, e.g. pre 1.21.
		}
	}

	@Override
	public String getVersionId() {
		return VERSION_ID;
	}

	@Override
	public void overwriteLivingEntityAI(LivingEntity entity) {
	}

	@Override
	public boolean supportsCustomMobAI() {
		// Not supported. Mobs will be stationary and not react towards nearby players due to the
		// NoAI flag.
		return false;
	}

	@Override
	public void tickAI(LivingEntity entity, int ticks) {
		// Not supported.
	}

	@Override
	public void setOnGround(Entity entity, boolean onGround) {
		try {
			Object mcEntity = Unsafe.assertNonNull(obcGetHandleMethod.invoke(entity));
			nmsEntitySetOnGroundMethod.invoke(mcEntity, onGround);
		} catch (Exception e) {
			// Ignoring, since this is not that important if it doesn't work.
		}
	}

	@Override
	public void setNoclip(Entity entity) {
		// Not supported, but also not necessarily required (just provides a small performance
		// benefit).
	}

	// For CraftItemStacks, this first tries to retrieve the underlying NMS item stack without
	// making a copy of it. Otherwise, this falls back to using CraftItemStack#asNMSCopy.
	private Object asNMSItemStack(ItemStack itemStack) {
		assert itemStack != null;
		if (obcCraftItemStackClass.isInstance(itemStack)) {
			try {
				return Unsafe.castNonNull(obcCraftItemStackHandleField.get(itemStack));
			} catch (Exception e) {
				Log.severe("Failed to retrieve the underlying Minecraft ItemStack!", e);
			}
		}
		try {
			return Unsafe.assertNonNull(obcCraftItemStackAsNMSCopyMethod.invoke(null, itemStack));
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Failed to convert item stack to NMS item stack!", e);
		}
	}

	@Override
	public boolean matches(@Nullable ItemStack provided, @Nullable ItemStack required) {
		if (provided == required) return true;
		// If the required item is empty, then the provided item has to be empty as well:
		if (ItemUtils.isEmpty(required)) return ItemUtils.isEmpty(provided);
		else if (ItemUtils.isEmpty(provided)) return false;
		assert required != null && provided != null;
		if (provided.getType() != required.getType()) return false;

		// TODO Minecraft 1.20.5+ uses DataComponentPredicates for matching items. Implement a
		// reflection-based fallback?

		// Fallback: Check for metadata equality. This behavior is stricter than vanilla Minecraft's
		// item comparison.
		return required.isSimilar(provided);
	}

	@Override
	public void updateTrades(Player player) {
		// Not supported.
	}

	private Object getItemStackTag(Object nmsItemStack) throws Exception {
		var serializationContext = nmsHolderLookupProviderCreateSerializationContextMethod.invoke(
				nmsMinecraftRegistry,
				nmsNbtOps
		);
		var itemTagResult = nmsCodecEncodeStartMethod.invoke(
				nmsItemStackCodec,
				serializationContext,
				nmsItemStack
		);
		var itemTag = nmsDataResultGetOrThrowMethod.invoke(itemTagResult);
		return Unsafe.assertNonNull(itemTag);
	}

	@Override
	public @Nullable String getItemSNBT(@ReadOnly ItemStack itemStack) {
		return null; // Not supported.
	}

	@Override
	public @Nullable ItemStackComponentsData getItemStackComponentsData(@ReadOnly ItemStack itemStack) {
		Validate.notNull(itemStack, "itemStack is null!");
		if (ItemUtils.isEmpty(itemStack)) {
			return null;
		}

		try {
			var nmsItem = this.asNMSItemStack(itemStack);
			var itemTag = this.getItemStackTag(nmsItem);
			var componentsTag = nmsCompoundTagGetMethod.invoke(itemTag, "components");
			if (componentsTag == null) {
				return null;
			}

			var componentsData = ItemStackComponentsData.ofNonNull(DataContainer.create());
			var consumer = new BiConsumer<String, Object>() {
				@Override
				public void accept(String componentKey, Object componentValue) {
					assert componentKey != null;
					// Serialized as SNBT:
					componentsData.set(componentKey, componentValue.toString());
				}
			};
			nmsCompoundTagForEachMethod.invoke(componentsTag, consumer);
			return componentsData;
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize item stack!", e);
		}
	}

	@Override
	public ItemStack deserializeItemStack(
			int dataVersion,
			NamespacedKey id,
			int count,
			@Nullable ItemStackComponentsData componentsData
	) {
		Validate.notNull(id, "id is null!");
		try {
			var itemTag = nmsCompoundTagClass.getConstructor().newInstance();
			nmsCompoundTagPutStringMethod.invoke(itemTag, "id", id.toString());
			nmsCompoundTagPutIntMethod.invoke(itemTag, "count", count);

			var componentValues = componentsData != null ? componentsData.getValues() : null;
			if (componentValues != null && !componentValues.isEmpty()) {
				var componentsTag = nmsCompoundTagClass.getConstructor().newInstance();
				componentValues.forEach((componentKey, componentValue) -> {
					assert componentKey != null;
					assert componentValue != null;
					var componentSnbt = componentValue.toString();

					Object componentTag;
					try {
						componentTag = Unsafe.assertNonNull(
								nmsTagParserParseFullyMethod.invoke(nmsTagParser, componentSnbt)
						);
					} catch (Exception e) {
						throw new IllegalArgumentException(
								"Error parsing item stack component: '" + componentSnbt + "'",
								e
						);
					}
					try {
						nmsCompoundTagPutMethod.invoke(componentsTag, componentKey.toString(), componentTag);
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				});
				nmsCompoundTagPutMethod.invoke(itemTag, "components", componentsTag);
			}

			var currentDataVersion = Bukkit.getUnsafe().getDataVersion();
			var convertedItemTagDynamic = nmsDataFixerUpdateMethod.invoke(
					nmsDataFixer,
					nmsDataFixerTypeItemStack,
					nmsDynamicConstructor.newInstance(nmsNbtOps, itemTag),
					dataVersion,
					currentDataVersion
			);
			var convertedItemTag = nmsDynamicGetValueMethod.invoke(convertedItemTagDynamic);

			var idString = (String) nmsCompoundTagGetStringOrMethod.invoke(
					convertedItemTag,
					"id",
					"minecraft:air"
			);
			assert idString != null;
			if (idString.equals("minecraft:air")) {
				return new ItemStack(Material.AIR);
			}

			var serializationContext = nmsHolderLookupProviderCreateSerializationContextMethod.invoke(
					nmsMinecraftRegistry,
					nmsNbtOps
			);
			var nmsItemResult = nmsCodecParseMethod.invoke(
					nmsItemStackCodec,
					serializationContext,
					convertedItemTag
			);
			var nmsItem = nmsDataResultGetOrThrowMethod.invoke(nmsItemResult);
			return Unsafe.assertNonNull(
					(ItemStack) obcCraftItemStackAsCraftMirrorMethod.invoke(null, nmsItem)
			);
		} catch (Exception e) {
			throw new RuntimeException("Failed to deserialize item stack!", e);
		}
	}

	// MC 1.21+ TODO Can be removed once we only support Bukkit 1.21+

	@Override
	public boolean isDestroyingBlocks(EntityExplodeEvent event) {
		if (b_EntityExplodeEvent_GetExplosionResultMethod == null) return true;

		try {
			assert b_EntityExplodeEvent_GetExplosionResultMethod != null;
			var explosionResult = b_EntityExplodeEvent_GetExplosionResultMethod.invoke(event);
			assert explosionResult != null;
			return isDestroyingBlocks(explosionResult);
		} catch (Exception e) {
			// Something unexpected went wrong. Assume pre 1.21 behavior. Wind charges may break
			// player shops when container protection is disabled (#921).
			return true;
		}
	}

	@Override
	public boolean isDestroyingBlocks(BlockExplodeEvent event) {
		if (b_BlockExplodeEvent_GetExplosionResultMethod == null) return true;

		try {
			assert b_BlockExplodeEvent_GetExplosionResultMethod != null;
			var explosionResult = b_BlockExplodeEvent_GetExplosionResultMethod.invoke(event);
			assert explosionResult != null;
			return isDestroyingBlocks(explosionResult);
		} catch (Exception e) {
			// Something unexpected went wrong. Assume pre 1.21 behavior. Wind charges may break
			// player shops when container protection is disabled (#921).
			return true;
		}
	}

	private static boolean isDestroyingBlocks(Object explosionResult) {
		var explosionResultString = explosionResult.toString();
		return explosionResultString.equals("DESTROY")
				|| explosionResultString.equals("DESTROY_WITH_DECAY");
	}
}
