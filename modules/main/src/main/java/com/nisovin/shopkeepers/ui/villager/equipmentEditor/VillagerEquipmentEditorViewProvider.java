package com.nisovin.shopkeepers.ui.villager.equipmentEditor;

import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.ui.SKDefaultUITypes;
import com.nisovin.shopkeepers.ui.lib.UIState;
import com.nisovin.shopkeepers.ui.lib.View;
import com.nisovin.shopkeepers.ui.villager.AbstractVillagerViewProvider;

public class VillagerEquipmentEditorViewProvider extends AbstractVillagerViewProvider {

	VillagerEquipmentEditorViewProvider(AbstractVillager villager) {
		super(SKDefaultUITypes.VILLAGER_EQUIPMENT_EDITOR(), villager);
	}

	@Override
	public boolean canAccess(Player player, boolean silent) {
		return true;
	}

	@Override
	protected @Nullable View createView(Player player, UIState uiState) {
		return new VillagerEquipmentEditorView(this, player, uiState);
	}
}
