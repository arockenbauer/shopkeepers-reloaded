package com.nisovin.shopkeepers.ui.equipmentEditor;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.bukkit.inventory.EquipmentSlot;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.util.java.Validate;

public class EquipmentEditorUIState implements UIState {

	// Assumption: These collections do not get externally modified while the editor is in-use!
	// Element order matches order in the editor.
	private final List<? extends EquipmentSlot> supportedSlots;
	private final Map<? extends EquipmentSlot, ? extends UnmodifiableItemStack> currentEquipment;
	private final BiConsumer<EquipmentSlot, @Nullable UnmodifiableItemStack> onEquipmentChanged;

	public EquipmentEditorUIState(
			List<? extends EquipmentSlot> supportedSlots,
			Map<? extends EquipmentSlot, ? extends UnmodifiableItemStack> currentEquipment,
			BiConsumer<EquipmentSlot, @Nullable UnmodifiableItemStack> onEquipmentChanged
	) {
		Validate.notNull(supportedSlots, "supportedSlots is null");
		Validate.notNull(currentEquipment, "currentEquipment is null");
		Validate.notNull(onEquipmentChanged, "onEquipmentChanged is null");
		this.supportedSlots = supportedSlots;
		this.currentEquipment = currentEquipment;
		this.onEquipmentChanged = onEquipmentChanged;
	}

	public List<? extends EquipmentSlot> getSupportedSlots() {
		return supportedSlots;
	}

	public Map<? extends EquipmentSlot, ? extends UnmodifiableItemStack> getCurrentEquipment() {
		return currentEquipment;
	}

	public BiConsumer<EquipmentSlot, @Nullable UnmodifiableItemStack> getOnEquipmentChanged() {
		return onEquipmentChanged;
	}
}
