package com.nisovin.shopkeepers.compat;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Salmon;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.util.annotations.ReadOnly;
import com.nisovin.shopkeepers.util.inventory.ItemStackComponentsData;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * The Shopkeepers plugin relies on functionality that is specific to the server version, e.g. to
 * support multiple Minecraft versions, and specific to the server implementation, such as internal
 * aspects of NMS and CraftBukkit that are not exposed via the Bukkit API. Additionally, there may
 * be differences between the Spigot and Paper server implementations and APIs.
 * <p>
 * {@link CompatProvider} hides these version and server implementation specific aspects behind an
 * interface that must be implemented for every supported combination of server implementation and
 * version.
 * <p>
 * Every implementation, except the fallback implementation, must reside in the package
 * {@code com.nisovin.shopkeepers.compat.v<version>}, be named {@code CompatProviderImpl}, and
 * provide a default constructor without parameters.
 */
public interface CompatProvider {

	/**
	 * This is invoked when the plugin is enabled, after the config and language file have been
	 * loaded.
	 */
	public default void onEnable() {
	}

	/**
	 * This is invoked when the plugin is disabled.
	 */
	public default void onDisable() {

	}

	// The compat version string.
	public String getVersionId();

	// This does not return null.
	public default CompatVersion getCompatVersion() {
		CompatVersion compatVersion = Compat.getCompatVersion(this.getVersionId());
		// Not finding the compat version indicates a bug.
		return Validate.State.notNull(compatVersion, "Could not find CompatVersion for '"
				+ this.getVersionId() + "'!");
	}

	public void overwriteLivingEntityAI(LivingEntity entity);

	// Whether tickAI and getCollisionDistance are supported.
	public default boolean supportsCustomMobAI() {
		return true;
	}

	public void tickAI(LivingEntity entity, int ticks);

	public void setOnGround(Entity entity, boolean onGround);

	// On some MC versions (e.g. MC 1.9, 1.10) NoAI only disables AI.
	public default boolean isNoAIDisablingGravity() {
		return true;
	}

	public void setNoclip(Entity entity);

	// Performs any version-specific setup that needs to happen before the entity is spawned. The
	// available operations may be limited during this phase of the entity spawning.
	public default void prepareEntity(Entity entity) {
	}

	// Performs any version-specific setup of the entity that needs to happen right after the entity
	// was spawned.
	public default void setupSpawnedEntity(Entity entity) {
	}

	public default boolean matches(
			@ReadOnly @Nullable ItemStack provided,
			@Nullable UnmodifiableItemStack required
	) {
		return this.matches(provided, ItemUtils.asItemStackOrNull(required));
	}

	/**
	 * Checks if the <code>provided</code> item stack fulfills the requirements of a trading recipe
	 * requiring the given <code>required</code> item stack.
	 * <p>
	 * This mimics Minecraft's item comparison: This checks if the item stacks are either both
	 * empty, or of same type and the provided item stack's metadata contains all the contents of
	 * the required item stack's metadata (with any list metadata being equal).
	 * 
	 * @param provided
	 *            the provided item stack
	 * @param required
	 *            the required item stack, this may be an unmodifiable item stack
	 * @return <code>true</code> if the provided item stack matches the required item stack
	 */
	public boolean matches(
			@ReadOnly @Nullable ItemStack provided,
			@ReadOnly @Nullable ItemStack required
	);

	// Note: It is not safe to reduce the number of trading recipes! Reducing the size below the
	// selected index can crash the client. It's left to the caller to ensure that the number of
	// recipes does not get reduced, for example by inserting dummy entries.
	public void updateTrades(Player player);

	// For use in chat hover messages, null if not supported.
	// TODO: Bukkit 1.20.6 also contains ItemMeta#getAsString now. However, this only includes the
	// item's NBT data, not the full item stack NBT. And BungeeCord's HoverEvent Item content does
	// not correctly serialize the data currently
	// (https://github.com/SpigotMC/BungeeCord/issues/3688).
	public @Nullable String getItemSNBT(@ReadOnly ItemStack itemStack);

	public @Nullable ItemStackComponentsData getItemStackComponentsData(@ReadOnly ItemStack itemStack);

	public ItemStack deserializeItemStack(
			int dataVersion,
			NamespacedKey id,
			int count,
			@Nullable ItemStackComponentsData componentsData
	);

	// MC 1.21+ TODO Can be removed once we only support Bukkit 1.21+

	public boolean isDestroyingBlocks(EntityExplodeEvent event);

	public boolean isDestroyingBlocks(BlockExplodeEvent event);

	// MC 1.21.3+ TODO Can be removed once we only support Bukkit 1.21.3+

	public default void setSalmonVariant(Salmon salmon, String variant) {
		// Not supported by default.
	}

	// MC 1.21.5+ TODO Can be removed once we only support Bukkit 1.21.5+

	/**
	 * Whether this MC version supports item hover events with the item SNBT in the "value" field.
	 * This is no longer supported in MC version 1.21.5 and above.
	 * 
	 * @return <code>true</code> if item SNBT hover event values are supported
	 */
	public default boolean supportsItemSNBTHoverEvents() {
		return false;
	}

	public default void setCowVariant(Cow cow, NamespacedKey variant) {
		// Not supported by default.
	}

	public default NamespacedKey cycleCowVariant(NamespacedKey variant, boolean backwards) {
		// Not supported by default.
		return variant;
	}

	public default void setPigVariant(Pig pig, NamespacedKey variant) {
		// Not supported by default.
	}

	public default NamespacedKey cyclePigVariant(NamespacedKey variant, boolean backwards) {
		// Not supported by default.
		return variant;
	}

	public default void setChickenVariant(Chicken chicken, NamespacedKey variant) {
		// Not supported by default.
	}

	public default NamespacedKey cycleChickenVariant(NamespacedKey variant, boolean backwards) {
		// Not supported by default.
		return variant;
	}
}
