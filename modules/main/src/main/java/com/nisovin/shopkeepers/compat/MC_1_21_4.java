package com.nisovin.shopkeepers.compat;

import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.logging.Log;

// TODO This can be removed once we only support Bukkit 1.21.4 upwards.
public final class MC_1_21_4 {

	public static final @Nullable Material PALE_OAK_SIGN = CompatUtils.getMaterial("PALE_OAK_SIGN");
	public static final @Nullable Material PALE_OAK_WALL_SIGN = CompatUtils.getMaterial("PALE_OAK_WALL_SIGN");
	public static final @Nullable Material PALE_OAK_HANGING_SIGN = CompatUtils.getMaterial("PALE_OAK_HANGING_SIGN");
	public static final @Nullable Material PALE_OAK_WALL_HANGING_SIGN = CompatUtils.getMaterial("PALE_OAK_WALL_HANGING_SIGN");

	public static void init() {
		if (isAvailable()) {
			Log.debug("MC 1.21.4 exclusive features are enabled.");
		} else {
			Log.debug("MC 1.21.4 exclusive features are disabled.");
		}
	}

	public static boolean isAvailable() {
		return PALE_OAK_SIGN != null;
	}

	private MC_1_21_4() {
	}
}
