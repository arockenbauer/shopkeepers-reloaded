package com.nisovin.shopkeepers.commands.lib.arguments;

import java.util.Map.Entry;
import java.util.UUID;

import com.nisovin.shopkeepers.util.bukkit.TextUtils;

public class AmbiguousPlayerEntryNameHandler
		extends AbstractAmbiguousPlayerNameHandler<Entry<? extends UUID, ? extends String>> {

	public AmbiguousPlayerEntryNameHandler(
			String input,
			Iterable<? extends Entry<? extends UUID, ? extends String>> matches
	) {
		this(input, matches, DEFAULT_MAX_ENTRIES);
	}

	public AmbiguousPlayerEntryNameHandler(
			String input,
			Iterable<? extends Entry<? extends UUID, ? extends String>> matches,
			int maxEntries
	) {
		super(input, matches, maxEntries);
	}

	@Override
	protected String getName(Entry<? extends UUID, ? extends String> match) {
		assert match != null;
		return TextUtils.getPlayerNameOrUnknown(match.getValue());
	}

	@Override
	protected UUID getUniqueId(Entry<? extends UUID, ? extends String> match) {
		assert match != null;
		return match.getKey();
	}
}
