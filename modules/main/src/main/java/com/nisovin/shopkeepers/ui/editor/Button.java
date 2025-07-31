package com.nisovin.shopkeepers.ui.editor;

import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.bukkit.SoundEffect;

public abstract class Button {

	// Volume 0.25 matches Minecraft's default button click volume.
	protected static final SoundEffect DEFAULT_BUTTON_CLICK_SOUND = new SoundEffect(Sound.UI_BUTTON_CLICK)
			.withVolume(0.25f);

	static final int NO_SLOT = -1;

	private final boolean placeAtEnd;

	private @Nullable EditorLayout editorLayout;
	private int slot = NO_SLOT;

	public Button() {
		this(false);
	}

	public Button(boolean placeAtEnd) {
		this.placeAtEnd = placeAtEnd;
	}

	void setEditorLayout(EditorLayout editorLayout) {
		if (this.editorLayout != null) {
			throw new IllegalStateException("Button was already added to some editor layout!");
		}
		this.editorLayout = editorLayout;
	}

	boolean isPlaceAtEnd() {
		return placeAtEnd;
	}

	int getSlot() {
		return slot;
	}

	void setSlot(int slot) {
		this.slot = slot;
	}

	protected boolean isApplicable(EditorLayout editorLayout) {
		return true;
	}

	protected @Nullable EditorLayout getEditorLayout() {
		return editorLayout;
	}

	public abstract @Nullable ItemStack getIcon(EditorView editorView);

	// Updates the icon in all editor views.
	// Note: Cannot deal with changes to the registered buttons (the button's slot) while the
	// inventory is open.
	protected final void updateIcon(EditorView editorView) {
		if (slot != NO_SLOT && editorLayout != null) {
			editorView.updateSlotInAllViews(slot);
		}
	}

	// Updates all icons in all editor views.
	protected final void updateAllIcons(EditorView editorView) {
		if (editorLayout != null) {
			editorView.updateButtonsInAllViews();
		}
	}

	protected abstract void onClick(EditorView editorView, InventoryClickEvent clickEvent);
}
