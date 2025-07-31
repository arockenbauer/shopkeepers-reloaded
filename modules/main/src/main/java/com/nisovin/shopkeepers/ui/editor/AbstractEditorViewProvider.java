package com.nisovin.shopkeepers.ui.editor;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.ui.lib.AbstractUIType;
import com.nisovin.shopkeepers.ui.lib.ViewContext;
import com.nisovin.shopkeepers.ui.lib.ViewProvider;
import com.nisovin.shopkeepers.ui.villager.editor.VillagerEditorViewProvider;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * Base class for editor UIs which support editing multiple pages of trades and offer additional
 * editor buttons.
 * <p>
 * For example used by {@link ShopkeeperEditorViewProvider} and {@link VillagerEditorViewProvider}.
 */
public abstract class AbstractEditorViewProvider extends ViewProvider {

	// Currently shared and cached across editor view instances for the same context (e.g.
	// shopkeeper).
	// TODO Support per-view layouts?
	private @Nullable EditorLayout layout; // Lazy setup

	protected final TradingRecipesAdapter tradingRecipesAdapter;

	protected AbstractEditorViewProvider(
			AbstractUIType uiType,
			ViewContext viewContext,
			TradingRecipesAdapter tradingRecipesAdapter
	) {
		super(uiType, viewContext);
		Validate.notNull(tradingRecipesAdapter, "tradingRecipesAdapter is null");
		this.tradingRecipesAdapter = tradingRecipesAdapter;
	}

	@Override
	public boolean canAccess(Player player, boolean silent) {
		Validate.notNull(player, "player is null");
		// Permission for the type of shopkeeper is checked in the AdminShopkeeper specific
		// ViewProvider.
		// Owner is checked in the PlayerShopkeeper specific ViewProvider.
		return true;
	}

	// EDITOR LAYOUT

	/**
	 * Gets the {@link EditorLayout}, creating it if not yet set up.
	 * 
	 * @return the editor layout, not <code>null</code>
	 */
	protected final EditorLayout getLayout() {
		if (layout == null) {
			this.layout = this.createLayout();
			this.setupButtons();
		}
		assert layout != null;
		return layout;
	}

	/**
	 * Creates the {@link EditorLayout} for this view provider.
	 * <p>
	 * The layout is created lazily once it is requested for the first time.
	 * 
	 * @return the editor layout, not <code>null</code>
	 */
	protected abstract EditorLayout createLayout();

	/**
	 * This is called once right after {@link #createLayout()} and can be used to register editor
	 * buttons.
	 */
	protected void setupButtons() {
		var layout = this.getLayout();
		layout.setupTradesPageBarButtons();
	}
}
