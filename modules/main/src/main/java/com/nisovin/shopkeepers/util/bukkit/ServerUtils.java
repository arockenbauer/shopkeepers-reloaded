package com.nisovin.shopkeepers.util.bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;

public final class ServerUtils {

	private static final boolean IS_PAPER;
	private static final String MAPPINGS_VERSION;

	static {
		boolean isPaper;
		try {
			Class.forName("io.papermc.paper.registry.RegistryAccess");
			isPaper = true;
		} catch (ClassNotFoundException e) {
			isPaper = false;
		}
		IS_PAPER = isPaper;

		String mappingsVersion;
		try {
			mappingsVersion = findMappingsVersion();
		} catch (Exception e) {
			// Since Paper 1.21.6, the server no longer supports the mappings version. We use the
			// Minecraft version instead.
			if (isPaper) {
				mappingsVersion = findPaperMinecraftVersion();
			} else {
				throw e;
			}
		}

		MAPPINGS_VERSION = mappingsVersion;
	}

	private static String findMappingsVersion() {
		UnsafeValues unsafeValues = Bukkit.getUnsafe();
		Method getMappingsVersionMethod;
		try {
			getMappingsVersionMethod = unsafeValues.getClass().getDeclaredMethod("getMappingsVersion");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(
					"Could not find method 'getMappingsVersion' in the UnsafeValues implementation!",
					e
			);
		}
		try {
			return Unsafe.cast(getMappingsVersionMethod.invoke(unsafeValues));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Could not retrieve the server's mappings version!", e);
		}
	}

	private static String findPaperMinecraftVersion() {
		try {
			var serverBuildInfoClass = Class.forName("io.papermc.paper.ServerBuildInfo");
			var serverBuildInfo = serverBuildInfoClass.getMethod("buildInfo").invoke(null);
			var serverVersion = serverBuildInfoClass.getMethod("minecraftVersionId").invoke(serverBuildInfo);
			assert serverVersion != null;
			return Unsafe.assertNonNull(serverVersion.toString());
		} catch (Exception e) {
			throw new RuntimeException("Could not retrieve the server's Minecraft version!", e);
		}
	}

	/**
	 * Checks if the server has access to Paper-specific API.
	 * 
	 * @return <code>true</code> if the server provides the Paper API
	 */
	public static boolean isPaper() {
		return IS_PAPER;
	}

	/**
	 * Gets the server's mappings version.
	 * <p>
	 * On Paper, since 1.21.6, the server no longer supports the mappings version, so this returns
	 * the Minecraft version instead.
	 * 
	 * @return the server's mappings version
	 */
	public static String getMappingsVersion() {
		return MAPPINGS_VERSION;
	}

	public static String getCraftBukkitPackage() {
		Package pkg = Unsafe.assertNonNull(Bukkit.getServer().getClass().getPackage());
		return pkg.getName();
	}

	private ServerUtils() {
	}
}
