package com.nisovin.shopkeepers.ui;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.ui.lib.ViewProvider;

/**
 * A {@link ViewProvider} that handles one specific type of UI for a particular {@link Shopkeeper}.
 */
public interface ShopkeeperViewProvider {

	/**
	 * Gets the shopkeeper.
	 * 
	 * @return the shopkeeper, not <code>null</code>
	 */
	public AbstractShopkeeper getShopkeeper();
}
