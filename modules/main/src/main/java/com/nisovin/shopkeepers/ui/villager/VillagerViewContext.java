package com.nisovin.shopkeepers.ui.villager;

import org.bukkit.entity.AbstractVillager;

import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.ui.lib.ViewContext;
import com.nisovin.shopkeepers.util.java.Validate;

public class VillagerViewContext implements ViewContext {

	private final AbstractVillager villager;

	public VillagerViewContext(AbstractVillager villager) {
		Validate.notNull(villager, "villager is null");
		this.villager = villager;
	}

	@Override
	public String getName() {
		return "Villager " + villager.getUniqueId();
	}

	@Override
	public AbstractVillager getObject() {
		return villager;
	}

	@Override
	public boolean isValid() {
		return villager.isValid();
	}

	@Override
	public Text getNoLongerValidMessage() {
		return Messages.villagerNoLongerExists;
	}
}
