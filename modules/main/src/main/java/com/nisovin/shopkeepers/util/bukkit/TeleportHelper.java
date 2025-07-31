package com.nisovin.shopkeepers.util.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.debug.Debug;
import com.nisovin.shopkeepers.debug.DebugOptions;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Helper for finding safe teleport locations.
 * <p>
 * Implementation notes:
 * <ul>
 * <li>We search for a suited teleport location within a certain radius of block locations, starting
 * with locations closest to the specified target destination. It is possible to configure a
 * different radius for the x-z plane and the y axis.
 * <li>In order to reduce the implementation and maintenance effort, the location safety check is
 * relatively simple: We only check if there is enough space available and a safe block for the
 * entity to stand on. Various circumstances may still make the selected teleport location "unsafe".
 * For example, it might be a closed room without direct view or safe passage to the actual
 * destination, or there might be unsafe blocks or traps nearby, etc.
 * <li>If a suitable location is found, we return the suggested teleport location. The yaw and pitch
 * of the returned teleport location are <code>0</code>.
 * <li>In certain contexts, more sophisticated rules for how to pick the teleport location may be
 * preferred. For example, when teleporting a player to a shop location it may be preferred to
 * teleport them to a location a few blocks away from the actual shop location and have them face
 * the shop. This generic helper can be used as part of a more sophisticated and context-specific
 * teleporting helper.
 * </ul>
 */
public final class TeleportHelper {

	private static final double GROUND_DISTANCE_CHECK_OFFSET = 0.98D;
	private static final double GROUND_DISTANCE_CHECK_RANGE = 1.0D;
	private static final Location TEMP_LOCATION = new Location(null, 0, 0, 0);

	private static final Set<Material> AVOIDED_BLOCK_TYPES = new HashSet<>();

	public static final TeleportHelper DEFAULT = new TeleportHelper(3, 2);

	static {
		// Damaging blocks:
		AVOIDED_BLOCK_TYPES.add(Material.CACTUS);
		AVOIDED_BLOCK_TYPES.add(Material.CAMPFIRE);
		AVOIDED_BLOCK_TYPES.add(Material.FIRE);
		AVOIDED_BLOCK_TYPES.add(Material.MAGMA_BLOCK);
		AVOIDED_BLOCK_TYPES.add(Material.SOUL_CAMPFIRE);
		AVOIDED_BLOCK_TYPES.add(Material.SOUL_FIRE);
		AVOIDED_BLOCK_TYPES.add(Material.SWEET_BERRY_BUSH);
		AVOIDED_BLOCK_TYPES.add(Material.WITHER_ROSE);
		AVOIDED_BLOCK_TYPES.add(Material.LAVA);
		// Liquids:
		AVOIDED_BLOCK_TYPES.add(Material.WATER);
		// Portals:
		AVOIDED_BLOCK_TYPES.add(Material.END_PORTAL);
		AVOIDED_BLOCK_TYPES.add(Material.NETHER_PORTAL);
		// Avoid trampling farmland:
		AVOIDED_BLOCK_TYPES.add(Material.FARMLAND);
	}

	// Gets dynamically re-sorted based on the preference function passed to #findSafeDestination.
	private final @NonNull BlockLocation[] blockOffsets;

	public TeleportHelper(int radiusXZ, int radiusY) {
		Validate.isTrue(radiusXZ >= 0, "radiusXZ cannot be negative");
		Validate.isTrue(radiusY >= 0, "radiusY cannot be negative");

		List<BlockLocation> blockOffsetsList = new ArrayList<>();
		for (int x = -radiusXZ; x <= radiusXZ; x++) {
			for (int z = -radiusXZ; z <= radiusXZ; z++) {
				for (int y = -radiusY; y <= radiusY; y++) {
					blockOffsetsList.add(new BlockLocation(x, y, z));
				}
			}
		}
		this.blockOffsets = blockOffsetsList.toArray(new @NonNull BlockLocation[0]);
	}

	private int getSortKey(BlockLocation location) {
		int x = location.getX();
		int y = location.getY();
		int z = location.getZ();

		// Sort by distance:
		return x * x + y * y + z * z;
	}

	/**
	 * Tries to find a safe location near the specified destination for the entity to teleport to.
	 * 
	 * @param destination
	 *            the target destination
	 * @param entity
	 *            the entity to teleport
	 * @param preference
	 *            Preference function to additional sort the candidate block location offsets by, in
	 *            addition to the default distance-based sorting criteria. If this returns
	 *            {@link Integer#MAX_VALUE} for a given offset, the offset is completed skipped.
	 * @return a nearby safe teleport location, or <code>null</code> if no such location is found
	 */
	public @Nullable Location findSafeDestination(
			Location destination,
			Entity entity,
			ToIntFunction<BlockLocation> preference
	) {
		Validate.notNull(destination, "destination");
		World world = null;
		Validate.isTrue(destination.isWorldLoaded() && (world = destination.getWorld()) != null,
				"destination world not loaded");
		assert world != null;
		Validate.notNull(preference, "preference");

		// Sort the candidate block location offsets based on the given preference function:
		Arrays.sort(
				blockOffsets,
				Comparator.comparingInt(preference).thenComparingInt(this::getSortKey)
		);

		final int destinationX = destination.getBlockX();
		final int destinationY = destination.getBlockY();
		final int destinationZ = destination.getBlockZ();

		// Reused entity bounding box for checks against block collision shapes:
		var boundingBox = entity.getBoundingBox();

		// Check each candidate location in the order they are sorted:
		for (int i = 0; i < blockOffsets.length; i++) {
			var offset = blockOffsets[i];

			// Skip if the caller prefers to skip this offset:
			if (preference.applyAsInt(offset) == Integer.MAX_VALUE) continue;

			var block = offset.getBlockAtOffset(
					world,
					destinationX,
					destinationY,
					destinationZ
			);
			var teleportLocation = this.getSafeTeleportLocation(block, entity, boundingBox);
			if (teleportLocation != null) {
				return teleportLocation;
			}
		}

		// No suitable teleport location found:
		return null;
	}

	private static void debugUnsafeTeleportLocation(Block block, String message) {
		if (Debug.isDebugging(DebugOptions.unsafeTeleports)) {
			Log.info("Unsafe teleport location " + TextUtils.getLocationString(block) + ": "
					+ message);
		}
	}

	// boundingBox: Reused instance.
	private @Nullable Location getSafeTeleportLocation(Block block, Entity entity, BoundingBox boundingBox) {
		try {
			// TODO If the entity bounding box spans multiple blocks, check all of the blocks below
			// it?
			Block blockBelow = block.getRelative(0, -1, 0);

			// Skip if the block below is air:
			if (blockBelow.getType().isAir()) {
				debugUnsafeTeleportLocation(block, "Block below is air!");
				return null;
			}

			// Skip if the block below is unsafe / to be avoided:
			// TODO Not all of these blocks are unsafe for all entity types, e.g. striders can stand
			// on lava and magma blocks.
			if (AVOIDED_BLOCK_TYPES.contains(blockBelow.getType())) {
				debugUnsafeTeleportLocation(block, "Block below is avoided!");
				return null;
			}

			// Avoid teleporting into or above liquids:
			// The blocks above are checked later as part of the bounding box check.
			if (block.isLiquid()) {
				debugUnsafeTeleportLocation(block, "Block is liquid!");
				return null;
			}

			if (blockBelow.isLiquid()) {
				debugUnsafeTeleportLocation(block, "Block below is liquid!");
				return null;
			}

			if (!WorldUtils.isBlockInsideWorldHeightBounds(block)) {
				debugUnsafeTeleportLocation(block, "Block is outside of world height bounds!");
				return null;
			}

			if (!WorldUtils.isBlockInsideWorldBorder(block)) {
				debugUnsafeTeleportLocation(block, "Block is  outside of world border!");
				return null;
			}

			// We check for collisions from slightly below the top of the block:
			Location location = Unsafe.assertNonNull(block.getLocation(TEMP_LOCATION))
					.add(0.5, GROUND_DISTANCE_CHECK_OFFSET, 0.5);

			// TODO Collidable fluids (which liquids the entity can stand on): No effect currently
			// since we always reject teleporting entities above of liquids currently.
			double distanceToGround = WorldUtils.getCollisionDistanceToGround(
					location,
					GROUND_DISTANCE_CHECK_RANGE,
					EntityUtils.getCollidableFluids(entity.getType())
			);
			if (distanceToGround == GROUND_DISTANCE_CHECK_RANGE) {
				// No collision within the checked range, i.e. no block for the entity to stand on:
				debugUnsafeTeleportLocation(block, "No block to stand on!");
				return null;
			}

			// Adjust the location:
			location.add(0.0D, -distanceToGround, 0.0D);

			// Check if there is enough space, taking the block collision shapes into account:

			// Shift the entity bounding box to the origin's center block location, at the found
			// height above the ground: Block collision shapes are not shifted by the block's
			// location.
			var bbShiftX = 0.5 - boundingBox.getCenterX();
			var bbShiftY = location.getY() - location.getBlockY() - boundingBox.getMinY();
			var bbShiftZ = 0.5 - boundingBox.getCenterZ();
			boundingBox.shift(bbShiftX, bbShiftY, bbShiftZ);
			// Note: No slight shrinking of the bounding box required: The overlap checks only
			// return true if the bounding boxes actually overlap, and not only touch (i.e. if the
			// edge coordinates are the same).

			// TODO We can end up checking block collision shapes multiple times when the entity
			// bounding box overlaps with multiple checked blocks (e.g. at different heights).
			// Optimize?

			// TODO Some entity bounding boxes span more than one block wide.
			int blockHeight = (int) Math.ceil(boundingBox.getHeight());
			for (int modY = 0; modY < blockHeight; modY++) {
				// Shift the entity bounding box according to the offset from the checked block
				// (i.e. by 1 in each iteration except the first iteration), since the block
				// collision shapes are not offset from the origin:
				if (modY != 0) {
					boundingBox.shift(0, -1, 0);
				}

				Block blockInBoundingBox = block.getRelative(0, modY, 0);
				if (blockInBoundingBox.isLiquid()) {
					debugUnsafeTeleportLocation(block, "Block above is liquid!");
					return null;
				}

				if (Debug.isDebugging(DebugOptions.unsafeTeleports)) {
					Log.info("Block shape: "
							+ Arrays.toString(blockInBoundingBox.getCollisionShape().getBoundingBoxes().toArray())
							+ " | Entity bounding box: " + boundingBox.toString());
				}

				if (blockInBoundingBox.getCollisionShape().overlaps(boundingBox)) {
					debugUnsafeTeleportLocation(block, "Not enough space available!");
					return null;
				}
			}

			return location.clone();
		} finally {
			// Cleanup temporarily used location
			TEMP_LOCATION.setWorld(null);
		}
	}
}
