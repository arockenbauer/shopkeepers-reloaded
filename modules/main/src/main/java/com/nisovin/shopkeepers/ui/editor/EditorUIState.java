package com.nisovin.shopkeepers.ui.editor;

import com.nisovin.shopkeepers.ui.lib.UIState;

/**
 * The captured editor state of a player.
 */
public class EditorUIState implements UIState {

	private final int currentPage;

	public EditorUIState(int currentPage) {
		this.currentPage = currentPage;
	}

	public final int getCurrentPage() {
		return currentPage;
	}
}
