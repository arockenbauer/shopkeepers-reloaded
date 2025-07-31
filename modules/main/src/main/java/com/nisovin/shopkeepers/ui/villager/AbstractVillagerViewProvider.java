package com.nisovin.shopkeepers.ui.villager;

import org.bukkit.entity.AbstractVillager;

import com.nisovin.shopkeepers.ui.lib.AbstractUIType;
import com.nisovin.shopkeepers.ui.lib.ViewProvider;

/**
 * A {@link ViewProvider} that handles one specific type of UI for a particular
 * {@link AbstractVillager}.
 */
public abstract class AbstractVillagerViewProvider extends ViewProvider {

	protected AbstractVillagerViewProvider(AbstractUIType uiType, AbstractVillager villager) {
		super(uiType, new VillagerViewContext(villager));
	}

	@Override
	public VillagerViewContext getContext() {
		return (VillagerViewContext) super.getContext();
	}

	/**
	 * Gets the villager.
	 * 
	 * @return the villager, not <code>null</code>
	 */
	public AbstractVillager getVillager() {
		return this.getContext().getObject();
	}
}
