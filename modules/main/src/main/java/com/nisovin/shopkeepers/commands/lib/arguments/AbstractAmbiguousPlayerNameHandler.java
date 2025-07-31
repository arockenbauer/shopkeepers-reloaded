package com.nisovin.shopkeepers.commands.lib.arguments;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.commands.lib.argument.ambiguity.AmbiguousInputHandler;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;

// Allows for reuse among any type of matched object that represents players and is able to provide
// the player's name and unique id.
public abstract class AbstractAmbiguousPlayerNameHandler<P>
		extends AmbiguousInputHandler<@NonNull P> {

	public AbstractAmbiguousPlayerNameHandler(
			String input,
			Iterable<? extends @NonNull P> matches
	) {
		this(input, matches, DEFAULT_MAX_ENTRIES);
	}

	public AbstractAmbiguousPlayerNameHandler(
			String input,
			Iterable<? extends @NonNull P> matches,
			int maxEntries
	) {
		super(input, matches, maxEntries);
	}

	@Override
	protected @Nullable Text getHeaderText() {
		Text header = Messages.ambiguousPlayerName;
		header.setPlaceholderArguments("name", input);
		return header;
	}

	protected abstract String getName(P match);

	protected abstract UUID getUniqueId(P match);

	@Override
	protected Text getEntryText(P match, int index) {
		assert match != null;
		String matchName = this.getName(match);
		UUID matchUUID = this.getUniqueId(match);
		String matchUUIDString = matchUUID.toString();
		Text entry = Messages.ambiguousPlayerNameEntry;
		entry.setPlaceholderArguments(
				"index", index,
				"name", Text.insertion(matchName).childText(matchName).buildRoot(),
				"uuid", Text.insertion(matchUUIDString).childText(matchUUIDString).buildRoot()
		);
		return entry.copy(); // Copy required!
	}

	@Override
	protected @Nullable Text getMoreText() {
		return Messages.ambiguousPlayerNameMore;
	}
}
