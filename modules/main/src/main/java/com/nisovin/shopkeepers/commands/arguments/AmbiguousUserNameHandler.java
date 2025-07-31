package com.nisovin.shopkeepers.commands.arguments;

import java.util.UUID;

import com.nisovin.shopkeepers.api.user.User;
import com.nisovin.shopkeepers.commands.lib.arguments.AbstractAmbiguousPlayerNameHandler;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;

public class AmbiguousUserNameHandler extends AbstractAmbiguousPlayerNameHandler<User> {

	public AmbiguousUserNameHandler(String input, Iterable<? extends User> matches) {
		this(input, matches, DEFAULT_MAX_ENTRIES);
	}

	public AmbiguousUserNameHandler(
			String input,
			Iterable<? extends User> matches,
			int maxEntries
	) {
		super(input, matches, maxEntries);
	}

	@Override
	protected String getName(User match) {
		assert match != null;
		// getName should not be null since its a name-based match. However, we handle this case as
		// well just in case:
		return TextUtils.getPlayerNameOrUnknown(match.getName());
	}

	@Override
	protected UUID getUniqueId(User match) {
		assert match != null;
		return match.getUniqueId();
	}
}
