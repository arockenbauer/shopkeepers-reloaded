package com.nisovin.shopkeepers.ui.lib;

/**
 * Configuration or the captured UI state of a previous UI session to restore.
 * <p>
 * A UI state is meant to only store state that is specific to a particular UI session. Any UI state
 * that depends on external data (e.g. the trades and items stored by a shopkeeper) is not captured
 * but needs to be freshly retrieved at the time the UI state is restored. Consequently, if any
 * external data has changed, a captured UI state may not be able to perfectly restore the previous
 * UI session. A captured UI state is restored in a best-effort manner, but should not be strictly
 * relied upon.
 * <p>
 * It is up to a specific UI type to define which types of {@link UIState}s it accepts. Passing an
 * incompatible UI state when requesting the UI results in a {@link IllegalArgumentException}.
 */
public interface UIState {

	/**
	 * An empty {@link UIState}.
	 */
	public static final UIState EMPTY = EmptyUIState.INSTANCE;
}
