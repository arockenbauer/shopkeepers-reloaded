package com.nisovin.shopkeepers.util.inventory;

import org.bukkit.event.inventory.InventoryType;

import com.nisovin.shopkeepers.util.java.MathUtils;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * Inventory layout utilities for chest-like inventories, i.e. inventories with rows of 9 slots.
 */
public final class ChestLayout {

	public static final int SLOTS_PER_ROW = 9;
	public static final int MAX_ROWS = 6;
	public static final int MAX_SLOTS = SLOTS_PER_ROW * MAX_ROWS;

	public static int toRows(int slots) {
		return 1 + ((slots - 1) / SLOTS_PER_ROW);
	}

	public static int toX(int slot) {
		return slot % SLOTS_PER_ROW;
	}

	public static int toY(int slot) {
		return slot / SLOTS_PER_ROW;
	}

	public static int toSlot(int x, int y) {
		return y * SLOTS_PER_ROW + x;
	}

	// Returns at least one row of slots, and at maximum MAX_SLOTS.
	public static int getRequiredSlots(int requestedSlots) {
		Validate.isTrue(requestedSlots >= 0, "requestedSlots must not be negative");
		int requiredRows = (requestedSlots / SLOTS_PER_ROW);
		if (requiredRows == 0 || requestedSlots % SLOTS_PER_ROW != 0) {
			requiredRows += 1;
		}
		return Math.min(requiredRows * SLOTS_PER_ROW, MAX_SLOTS);
	}

	public static boolean isChestLike(InventoryType inventoryType) {
		// Note: The player inventory is currently not considered 'chest-like' even though it has a
		// chest-like section, because it has additional slots (its size is not dividable by 9), and
		// the arrangement of the slots differs from the usual chest-like arrangement (hotbar slots
		// are located below the other container slots).
		switch (inventoryType) {
		// Also includes custom chest inventories, minecart chests, large chests, trapped chests:
		case CHEST:
		case ENDER_CHEST:
		case SHULKER_BOX:
		case BARREL:
			return true;
		default:
			return false;
		}
	}

	public static void validateSlotRange(int startSlot, int endSlot) {
		Validate.isTrue(startSlot >= 0 && startSlot <= endSlot,
				() -> "Invalid start or end slot: " + startSlot + ", " + endSlot);
	}

	// Rounds down.
	public static int getCenterSlot(int startSlot, int endSlot) {
		validateSlotRange(startSlot, endSlot);
		return MathUtils.middle(startSlot, endSlot);
	}

	private ChestLayout() {
	}
}
