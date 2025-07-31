package com.nisovin.shopkeepers.util.bukkit;

import java.nio.file.Path;

import org.bukkit.plugin.Plugin;

import com.nisovin.shopkeepers.util.java.FileUtils;

public final class PluginUtils {

	/**
	 * Gets the path relative to the plugin data folder, but only if the given path starts with the
	 * path of the plugin's data folder.
	 * 
	 * @param plugin
	 *            the {@link Plugin}
	 * @param path
	 *            the path
	 * @return the relative path, or the path itself if it does not start with the path of the
	 *         plugin data folder
	 */
	public static Path relativize(Plugin plugin, Path path) {
		Path pluginDataFolder = plugin.getDataFolder().toPath();
		return FileUtils.relativize(pluginDataFolder, path);
	}

	private PluginUtils() {
	}
}
