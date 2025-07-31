package com.nisovin.shopkeepers.ui.lib;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.ui.UIType;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Provides a {@link View} implementation for a specific {@link UIType} and {@link ViewContext}.
 * <p>
 * There may exist different implementations for the same {@link UIType}: {@link UIType} declares a
 * type of UI that can be requested in a specific context (e.g. for a {@link Shopkeeper}). The
 * {@link ViewProvider} that responds to the requests can be dynamically configured for each
 * context. The actual setup of an {@link InventoryView} and the handling of inventory interactions
 * is then implemented by a {@link View}, which the selected {@link ViewProvider} instantiates.
 */
public abstract class ViewProvider {

	private final AbstractUIType uiType;
	private final ViewContext viewContext;

	protected ViewProvider(AbstractUIType uiType, ViewContext viewContext) {
		Validate.notNull(uiType, "uiType is null");
		Validate.notNull(viewContext, "viewContext is null");
		this.uiType = uiType;
		this.viewContext = viewContext;
	}

	/**
	 * Gets the {@link UIType}.
	 * 
	 * @return the UI type, not <code>null</code>
	 */
	public final AbstractUIType getUIType() {
		return uiType;
	}

	/**
	 * Gets the {@link ViewContext}.
	 * 
	 * @return the view context, not <code>null</code>
	 */
	public ViewContext getContext() {
		return viewContext;
	}

	protected void debugNotOpeningUI(Player player, String reason) {
		Validate.notNull(player, "player is null");
		Validate.notEmpty(reason, "reason is null or empty");
		Log.debug(() -> this.getContext().getLogPrefix() + "Not opening UI '"
				+ this.getUIType().getIdentifier() + "' for player " + player.getName() + ": "
				+ reason);
	}

	/**
	 * Checks whether the given player is allowed to access to this UI.
	 * <p>
	 * This may for example perform any necessary permission checks.
	 * <p>
	 * This is for example checked as part of {@link #canOpen(Player, boolean)}, but limited to
	 * access permission related checks.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 * @param silent
	 *            <code>false</code> to inform the player if the access is denied
	 * @return <code>true</code> if the given player has access to this UI
	 */
	public abstract boolean canAccess(Player player, boolean silent);

	/**
	 * Checks whether the given player can open this UI.
	 * <p>
	 * This is for example called when a player requests this UI.
	 * <p>
	 * This is expected to call {@link #canAccess(Player, boolean)}, but may perform additional
	 * checks. For example, the trading UI of a shop may return <code>false</code> if there are
	 * currently no trades available.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 * @param silent
	 *            <code>false</code> to inform the player if they cannot open the UI currently
	 * @return <code>true</code> if the given player is currently allowed to open this UI
	 */
	public boolean canOpen(Player player, boolean silent) {
		return this.canAccess(player, silent);
	}

	/**
	 * Instantiates a new {@link View}.
	 * <p>
	 * Generally, {@link #canOpen(Player, boolean)} should be checked before this method is called.
	 * However, this method should not rely on that.
	 * 
	 * @param player
	 *            the player for whom the view is created, not <code>null</code>
	 * @param uiState
	 *            the initial {@link UIState}, not <code>null</code>, can be {@link UIState#EMPTY}
	 *            if accepted by the view
	 * @return the created {@link View}, or <code>null</code> if something went wrong
	 */
	protected abstract @Nullable View createView(Player player, UIState uiState);
}
