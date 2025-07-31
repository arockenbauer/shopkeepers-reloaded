package com.nisovin.shopkeepers.ui;

import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.ui.lib.ViewContext;
import com.nisovin.shopkeepers.util.java.Validate;

public class ShopkeeperViewContext implements ViewContext {

	private final AbstractShopkeeper shopkeeper;

	public ShopkeeperViewContext(AbstractShopkeeper shopkeeper) {
		Validate.notNull(shopkeeper, "shopkeeper is null");
		this.shopkeeper = shopkeeper;
	}

	@Override
	public String getName() {
		return "Shopkeeper " + shopkeeper.getId();
	}

	@Override
	public AbstractShopkeeper getObject() {
		return shopkeeper;
	}

	@Override
	public String getLogPrefix() {
		return shopkeeper.getLogPrefix();
	}

	@Override
	public boolean isValid() {
		return shopkeeper.isValid();
	}

	@Override
	public Text getNoLongerValidMessage() {
		// Unexpected: We abort all UI sessions before a shopkeeper is deleted.
		return Messages.shopNoLongerExists;
	}
}
