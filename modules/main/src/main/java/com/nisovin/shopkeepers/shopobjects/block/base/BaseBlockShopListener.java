package com.nisovin.shopkeepers.shopobjects.block.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopkeeper.registry.SKShopkeeperRegistry;
import com.nisovin.shopkeepers.util.bukkit.BlockFaceUtils;
import com.nisovin.shopkeepers.util.bukkit.BlockLocation;
import com.nisovin.shopkeepers.util.bukkit.EventUtils;
import com.nisovin.shopkeepers.util.bukkit.MutableBlockLocation;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.interaction.InteractionUtils;
import com.nisovin.shopkeepers.util.interaction.TestPlayerInteractEvent;
import com.nisovin.shopkeepers.util.logging.Log;

class BaseBlockShopListener implements Listener {

	// Local copy as array: Enables high-performance iteration.
	// This includes all directions that physic updates can propagate from, regardless of whether
	// the block shops can be attached in that direction.
	private static final @NonNull BlockFace[] BLOCK_SIDES = BlockFaceUtils.getBlockSides()
			.toArray(new @NonNull BlockFace[0]);

	// The block faces that physic updates can propagate from and potentially affect nearby blocks.
	// Spigot changed the behavior of the physics event in MC 1.13 to reduce the number of event
	// calls (https://hub.spigotmc.org/jira/browse/SPIGOT-4256). We therefore need to check the
	// neighboring blocks ourselves.
	private static final @NonNull BlockFace[] PHYSICS_BLOCK_FACES
			= Stream.concat(Stream.of(BlockFace.SELF), BlockFaceUtils.getBlockSides().stream())
					.toArray(length -> new @NonNull BlockFace[length]);

	private static final MutableBlockLocation SHARED_BLOCK_LOCATION = new MutableBlockLocation();

	private final SKShopkeepersPlugin plugin;
	private final BaseBlockShops baseBlockShops;
	private final SKShopkeeperRegistry shopkeeperRegistry;

	// We cancel all physics events around block shops in order to e.g. prevent sign shops from
	// breaking.
	// For performance optimization purposes, instead of checking during each physics event whether
	// the event shall be cancelled for the block or one of its potentially affected neighbors
	// (7 block checks), we pre-calculate in this map the block locations for which physics events
	// shall be cancelled and then only do a single block lookup during the event.
	// Value: The number of "tickets" asking for block physics to be cancelled at the specific block
	// location. E.g. the number of block shopkeepers, but tickets can also be added for other
	// purposes. When this number reaches zero, the map entry is removed and block physics are no
	// longer cancelled for the location.
	private final Map<BlockLocation, Integer> cancelledBlockPhysics = new HashMap<>();

	BaseBlockShopListener(SKShopkeepersPlugin plugin, BaseBlockShops blockShops) {
		this.plugin = plugin;
		this.baseBlockShops = blockShops;
		this.shopkeeperRegistry = plugin.getShopkeeperRegistry();
	}

	void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		// Ensure that our interact event handler is always executed first, even after plugin
		// reloads:
		// In order to not change the order among the already registered event handlers of our own
		// plugin, we move them all together to the front of the handler list.
		EventUtils.enforceExecuteFirst(PlayerInteractEvent.class, EventPriority.LOWEST, plugin);
	}

	void onDisable() {
		HandlerList.unregisterAll(this);
	}

	void addBlockPhysicsCancellation(Block block) {
		var blockLocation = SHARED_BLOCK_LOCATION;
		blockLocation.set(block);
		this.addBlockPhysicsCancellation(blockLocation);
	}

	void removeBlockPhysicsCancellation(Block block) {
		var blockLocation = SHARED_BLOCK_LOCATION;
		blockLocation.set(block);
		this.removeBlockPhysicsCancellation(blockLocation);
	}

	void addBlockPhysicsCancellation(BlockLocation blockLocation) {
		for (BlockFace blockFace : PHYSICS_BLOCK_FACES) {
			int adjacentX = blockLocation.getX() + blockFace.getModX();
			int adjacentY = blockLocation.getY() + blockFace.getModY();
			int adjacentZ = blockLocation.getZ() + blockFace.getModZ();
			var adjacentBlockLocation = new BlockLocation(
					Unsafe.assertNonNull(blockLocation.getWorldName()),
					adjacentX,
					adjacentY,
					adjacentZ
			);
			this.addSpecificBlockPhysicsCancellation(adjacentBlockLocation);
		}
	}

	void removeBlockPhysicsCancellation(BlockLocation blockLocation) {
		for (BlockFace blockFace : PHYSICS_BLOCK_FACES) {
			int adjacentX = blockLocation.getX() + blockFace.getModX();
			int adjacentY = blockLocation.getY() + blockFace.getModY();
			int adjacentZ = blockLocation.getZ() + blockFace.getModZ();
			var adjacentBlockLocation = new BlockLocation(
					Unsafe.assertNonNull(blockLocation.getWorldName()),
					adjacentX,
					adjacentY,
					adjacentZ
			);
			this.removeSpecificBlockPhysicsCancellation(adjacentBlockLocation);
		}
	}

	private void addSpecificBlockPhysicsCancellation(BlockLocation blockLocation) {
		cancelledBlockPhysics.compute(
				blockLocation.immutable(),
				(k, v) -> v == null ? 1 : (v + 1)
		);
	}

	private void removeSpecificBlockPhysicsCancellation(BlockLocation blockLocation) {
		cancelledBlockPhysics.compute(
				blockLocation.immutable(),
				(k, v) -> (v == null || v <= 1) ? null : (v - 1)
		);
	}

	// See LivingEntityShopListener for the reasoning behind using event priority LOWEST and
	// ignoring cancelled events.
	// The shop creation item reacts to player interactions as well. If a player interacts with a
	// base block shop while holding a shop creation item in his hand, we want the base block shop
	// interaction to take precedence. This listener therefore has to be registered before the shop
	// creation listener.
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	void onPlayerInteract(PlayerInteractEvent event) {
		// Ignore our own fake interact event:
		if (event instanceof TestPlayerInteractEvent) return;

		// Check for base block shop interaction:
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Player player = event.getPlayer();
		Block block = Unsafe.assertNonNull(event.getClickedBlock());
		Log.debug(() -> "Player " + player.getName() + " is interacting (" + event.getHand()
				+ ") with block at " + TextUtils.getLocationString(block));

		AbstractShopkeeper shopkeeper = shopkeeperRegistry.getShopkeeperByBlock(block);
		if (shopkeeper == null) {
			Log.debug("  Non-shopkeeper");
			return;
		}
		if (!baseBlockShops.isBaseBlockShop(shopkeeper)) {
			Log.debug("  Not using default block shop behaviors");
			return;
		}

		// Keep track of the previous interaction result:
		boolean useInteractedBlock = (event.useInteractedBlock() != Result.DENY);

		// Always cancel interactions with shopkeepers, to prevent any default behavior:
		event.setCancelled(true); // Also cancels the item interaction
		// Update inventory in case the interaction would trigger an item action normally:
		player.updateInventory();

		// Ignore if already cancelled. This resolves conflicts with other event handlers that also
		// run at LOWEST priority, such as for example Shopkeepers' shop creation item listener.
		if (!useInteractedBlock) {
			Log.debug("  Ignoring already cancelled block interaction");
			return;
		}

		// Only trigger shopkeeper interaction for main-hand events:
		if (event.getHand() != EquipmentSlot.HAND) {
			Log.debug("  Ignoring off-hand interaction");
			return;
		}

		// Check the block interaction result by calling another interact event:
		if (Settings.checkShopInteractionResult) {
			if (!InteractionUtils.checkBlockInteract(player, block, false)) {
				Log.debug("  Cancelled by another plugin");
				return;
			}
		}

		// Handle interaction:
		shopkeeper.onPlayerInteraction(player);
	}

	// Protect shop blocks:

	// TODO Pre-calculate the set of protected blocks similar to how we pre-calculate the set of
	// block locations affected by block physics? We would need to also take the attached block face
	// into account.
	private boolean isProtectedBlock(Block block) {
		// Check if the block itself is a base block shop:
		if (baseBlockShops.isBaseBlockShop(block)) {
			return true;
		}

		// Check if there is a base block shop attached to this block:
		String worldName = block.getWorld().getName();
		int blockX = block.getX();
		int blockY = block.getY();
		int blockZ = block.getZ();
		for (BlockFace blockFace : BLOCK_SIDES) {
			// Note: Avoiding getting the adjacent block slightly improves the performance.
			int adjacentX = blockX + blockFace.getModX();
			int adjacentY = blockY + blockFace.getModY();
			int adjacentZ = blockZ + blockFace.getModZ();
			Shopkeeper shopkeeper = shopkeeperRegistry.getShopkeeperByBlock(
					worldName,
					adjacentX,
					adjacentY,
					adjacentZ
			);
			if (shopkeeper == null || !baseBlockShops.isBaseBlockShop(shopkeeper)) continue;

			BaseBlockShopObject blockShop = (BaseBlockShopObject) shopkeeper.getShopObject();
			BlockFace attachedFace = blockShop.getAttachedBlockFace();
			if (blockFace == attachedFace) {
				// The block shop is attached to the given block:
				return true;
			}
			// Else continue: There might be other block shops that are actually attached to the
			// block in the remaining block directions.
		}
		return false;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (this.isProtectedBlock(block)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		if (baseBlockShops.isBaseBlockShop(block)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	void onBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		World world = block.getWorld();
		String worldName = world.getName();
		int blockX = block.getX();
		int blockY = block.getY();
		int blockZ = block.getZ();

		var blockLocation = SHARED_BLOCK_LOCATION;
		SHARED_BLOCK_LOCATION.set(worldName, blockX, blockY, blockZ);

		if (cancelledBlockPhysics.containsKey(blockLocation)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	void onEntityExplosion(EntityExplodeEvent event) {
		List<Block> blockList = event.blockList();
		this.removeProtectedBlocks(blockList);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	void onBlockExplosion(BlockExplodeEvent event) {
		List<Block> blockList = event.blockList();
		this.removeProtectedBlocks(blockList);
	}

	private void removeProtectedBlocks(List<? extends Block> blockList) {
		assert blockList != null;
		blockList.removeIf(this::isProtectedBlock);
	}
}
