package com.nisovin.shopkeepers.testutil;

import java.nio.file.Paths;
import java.util.logging.Logger;

import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.internal.ApiInternals;
import com.nisovin.shopkeepers.api.internal.InternalShopkeepersAPI;
import com.nisovin.shopkeepers.api.internal.InternalShopkeepersPlugin;
import com.nisovin.shopkeepers.compat.Compat;
import com.nisovin.shopkeepers.internals.SKApiInternals;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Mocks the Shopkeepers plugin functionality that is required during tests.
 */
class ShopkeepersPluginMock extends ProxyHandler<InternalShopkeepersPlugin> {

	// Static initializer: Ensures that this is only setup once across all tests.
	static {
		// Set up and test the Log:
		Log.setLogger(Logger.getLogger(SKShopkeepersPlugin.class.getCanonicalName()));
		Log.info("Setting up the Shopkeepers plugin mock for tests...");

		// Set up the plugin mock:
		InternalShopkeepersPlugin pluginMock = new ShopkeepersPluginMock().newProxy();

		// Create the plugin's data folder:
		pluginMock.getDataFolder().mkdirs();

		// Enable the API:
		InternalShopkeepersAPI.enable(pluginMock);

		// Enable the compat provider:
		Compat.load(pluginMock);
	}

	// Calling this method ensures that the static initializer is invoked.
	public static void setup() {
	}

	private ShopkeepersPluginMock() {
		super(InternalShopkeepersPlugin.class);
	}

	@Override
	protected void setupMethodHandlers() throws Exception {
		ApiInternals apiInternals = new SKApiInternals();
		this.addHandler(
				InternalShopkeepersPlugin.class.getMethod("getApiInternals"),
				(proxy, args) -> {
					return apiInternals;
				}
		);
		this.addHandler(
				InternalShopkeepersPlugin.class.getMethod("getDataFolder"),
				(proxy, args) -> {
					return Paths.get("plugins/Shopkeepers").toFile();
				}
		);
		this.addHandler(
				InternalShopkeepersPlugin.class.getMethod("getDescription"),
				(proxy, args) -> {
					try {
						var classLoader = this.getClass().getClassLoader();
						assert classLoader != null;
						var pluginYmlFile = classLoader.getResourceAsStream("plugin.yml");
						assert pluginYmlFile != null;
						return new PluginDescriptionFile(pluginYmlFile);
					} catch (InvalidDescriptionException e) {
						throw new RuntimeException("Failed to load plugin description", e);
					}
				}
		);
	}
}
