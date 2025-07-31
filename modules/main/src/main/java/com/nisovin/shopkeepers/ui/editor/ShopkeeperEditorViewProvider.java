package com.nisovin.shopkeepers.ui.editor;

import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.ui.ShopkeeperViewContext;
import com.nisovin.shopkeepers.ui.ShopkeeperViewProvider;
import com.nisovin.shopkeepers.ui.lib.AbstractUIType;

public abstract class ShopkeeperEditorViewProvider extends AbstractEditorViewProvider
		implements ShopkeeperViewProvider {

	protected ShopkeeperEditorViewProvider(
			AbstractUIType uiType,
			AbstractShopkeeper shopkeeper,
			TradingRecipesAdapter tradingRecipesAdapter
	) {
		super(uiType, new ShopkeeperViewContext(shopkeeper), tradingRecipesAdapter);
	}

	@Override
	public ShopkeeperViewContext getContext() {
		return (ShopkeeperViewContext) super.getContext();
	}

	@Override
	public AbstractShopkeeper getShopkeeper() {
		return this.getContext().getObject();
	}

	// EDITOR LAYOUT

	@Override
	protected ShopkeeperEditorLayout createLayout() {
		return new ShopkeeperEditorLayout(this.getShopkeeper());
	}

	@Override
	protected void setupButtons() {
		super.setupButtons();

		var layout = (ShopkeeperEditorLayout) this.getLayout();
		layout.setupShopkeeperButtons();
		layout.setupShopObjectButtons();
	}
}
