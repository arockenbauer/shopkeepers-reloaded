package com.nisovin.shopkeepers.container.protection;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Handles container protection. Can be disabled via a config setting.
 */
class ContainerProtectionListener implements Listener {

	private final ProtectedContainers protectedContainers;

	ContainerProtectionListener(ProtectedContainers protectedContainers) {
		this.protectedContainers = protectedContainers;
	}

	/**
	 * Prevents unauthorized opening of protected containers.
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block block = Unsafe.assertNonNull(event.getClickedBlock());
		Player player = event.getPlayer();
		if (protectedContainers.isProtectedContainer(block, player)) {
			// TODO Always allow access to own shop containers, even if cancelled by other plugins?
			Log.debug(() -> "Cancelled container opening by '" + player.getName() + "' at '"
					+ TextUtils.getLocationString(block) + "': Protected container.");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (protectedContainers.isProtectedContainer(block, player)) {
			Log.debug(() -> "Cancelled breaking of container block by '" + player.getName()
					+ "' at '" + TextUtils.getLocationString(block) + "': Protected container.");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Material type = block.getType();
		Player player = event.getPlayer();

		if (ItemUtils.isChest(type)) {
			// Note: Unconnected chests can be placed.
			if (protectedContainers.isProtectedContainer(block, player)) {
				Log.debug(() -> "Cancelled placing of (double) chest block by '" + player.getName()
						+ "' at '" + TextUtils.getLocationString(block)
						+ "': Protected chest nearby.");
				event.setCancelled(true);
			}
		} else if (type == Material.HOPPER) {
			// Prevent placement of hoppers that could be used to extract or inject items from/into
			// a protected
			// container:
			Block upperBlock = block.getRelative(BlockFace.UP);
			if (protectedContainers.isProtectedContainer(upperBlock, player)
					|| protectedContainers.isProtectedContainer(this.getFacedBlock(block), player)) {
				Log.debug(() -> "Cancelled placing of hopper block by '" + player.getName()
						+ "' at '" + TextUtils.getLocationString(block)
						+ "': Protected container nearby.");
				event.setCancelled(true);
			}
		} else if (type == Material.DROPPER) {
			// Prevent placement of droppers that could be used to inject items into a protected
			// container:
			if (protectedContainers.isProtectedContainer(this.getFacedBlock(block), player)) {
				Log.debug(() -> "Cancelled placing of dropper block by '" + player.getName()
						+ "' at '" + TextUtils.getLocationString(block)
						+ "': Protected container nearby.");
				event.setCancelled(true);
			}
		} else if (ItemUtils.isRail(type)) {
			Block upperBlock = block.getRelative(BlockFace.UP);
			if (protectedContainers.isProtectedContainer(upperBlock, player)) {
				Log.debug(() -> "Cancelled placing of rail block by '" + player.getName() + "' at '"
						+ TextUtils.getLocationString(block) + "': Protected container nearby.");
				event.setCancelled(true);
			}
		}
	}

	private Block getFacedBlock(Block directionalBlock) {
		Directional directionalData = (Directional) directionalBlock.getBlockData();
		BlockFace facing = directionalData.getFacing();
		return directionalBlock.getRelative(facing);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	void onEntityExplosion(EntityExplodeEvent event) {
		List<Block> blockList = event.blockList();
		this.removeProtectedChests(blockList);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	void onBlockExplosion(BlockExplodeEvent event) {
		List<Block> blockList = event.blockList();
		this.removeProtectedChests(blockList);
	}

	// Block list has to be modifiable.
	private void removeProtectedChests(List<? extends Block> blockList) {
		blockList.removeIf(protectedContainers::isProtectedContainer);
	}
}
