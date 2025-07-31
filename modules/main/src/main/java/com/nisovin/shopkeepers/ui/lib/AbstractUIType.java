package com.nisovin.shopkeepers.ui.lib;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.ui.UIType;
import com.nisovin.shopkeepers.types.AbstractType;

// TODO Handle enabled state and permission of UI types generally? Currently, most UIs handle
// permission checks as part of their canAccess logic. And the enabled state is not used at all.
// However, UIs may want to use specific feedback messages or more sophisticated logic when checking
// if a player can open a certain UI. Remove the unused enabled state and permission from UI types?
// Or document them as unused? Or provide default behavior, but let each UI handle it more
// specifically if they want?
public abstract class AbstractUIType extends AbstractType implements UIType {

	protected AbstractUIType(String identifier, @Nullable String permission) {
		super(identifier, permission);
	}
}
