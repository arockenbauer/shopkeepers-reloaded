package com.nisovin.shopkeepers.ui.confirmations;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.util.java.Validate;

public class ConfirmationUIState implements UIState {

	private final String title;
	private final @Nullable List<? extends String> confirmationLore;
	private final Runnable action;
	// This is invoked when the player explicitly presses the 'cancel' button. It is not invoked if
	// the player closes the inventory, either directly or because something else closes it (for
	// example when another inventory is opened for the player).
	private final Runnable onCancelled;

	public ConfirmationUIState(
			String title,
			@Nullable List<? extends String> confirmationLore,
			Runnable action,
			Runnable onCancelled
	) {
		Validate.notEmpty(title, "title is empty");
		Validate.notNull(action, "action is null");
		Validate.notNull(onCancelled, "onCancelled is null");
		this.title = title;
		this.confirmationLore = confirmationLore;
		this.action = action;
		this.onCancelled = onCancelled;
	}

	public String getTitle() {
		return title;
	}

	public @Nullable List<? extends String> getConfirmationLore() {
		return confirmationLore;
	}

	public Runnable getAction() {
		return action;
	}

	public Runnable getOnCancelled() {
		return onCancelled;
	}
}
