package com.nisovin.shopkeepers.ui.lib;

import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * A {@link ViewContext} implementation for views that are not associated with any particular
 * object: The {@link SimpleViewContext} instance itself acts as the context object.
 */
public class SimpleViewContext implements ViewContext {

	// Name for identification purposes:
	private final String name;

	public SimpleViewContext(String name) {
		Validate.notEmpty(name, "name is empty");
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getObject() {
		return this;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public Text getNoLongerValidMessage() {
		// Unexpected: This type of context never becomes invalid.
		return Text.EMPTY;
	}
}
