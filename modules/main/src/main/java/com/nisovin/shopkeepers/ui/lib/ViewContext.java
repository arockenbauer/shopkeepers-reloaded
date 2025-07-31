package com.nisovin.shopkeepers.ui.lib;

import com.nisovin.shopkeepers.api.ui.UISession;
import com.nisovin.shopkeepers.text.Text;

/**
 * A description of the {@link #getObject() context object} of a {@link View}.
 * <p>
 * The {@link #getObject() context object} can be any arbitrary object, but is usually the object
 * that the view is primarily associated with, which is usually the object for which the view
 * displays data or provides some kind of control.
 * <p>
 * The context description of a view can be accessed via {@link View#getContext()} or
 * {@link ViewProvider#getContext()}. {@link UISession}s for a specific context object can be
 * queried via {@link UISessionManager#getUISessionsForContext(Object)}.
 */
public interface ViewContext {

	/**
	 * An identifier of this context, for example based on the identity and/or type of the
	 * {@link #getObject() context object}.
	 * <p>
	 * This can for example be used in log and debug messages.
	 * 
	 * @return the name, not <code>null</code> or empty
	 */
	public String getName();

	/**
	 * Gets the context object.
	 * 
	 * @return the context object, not <code>null</code>
	 */
	public Object getObject();

	/**
	 * A log prefix for messages related to this view context.
	 * 
	 * @return the log prefix, not <code>null</code> but can be empty
	 */
	public default String getLogPrefix() {
		return this.getName() + ": ";
	}

	/**
	 * Verifies that the context is still "valid".
	 * <p>
	 * This is for example checked before inventory events are forwarded to the view.
	 * 
	 * @return <code>true</code> if the context is still valid
	 */
	public boolean isValid();

	/**
	 * Gets the message to show the player when the UI is closed because the context is no longer
	 * {@link #isValid() valid}.
	 * 
	 * @return the 'no longer valid' message, not <code>null</code>
	 */
	public Text getNoLongerValidMessage();
}
