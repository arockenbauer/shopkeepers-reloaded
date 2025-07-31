package com.nisovin.shopkeepers.commands.arguments;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.commands.lib.argument.ambiguity.AmbiguousInputHandler;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;

public class AmbiguousShopkeeperNameHandler extends AmbiguousInputHandler<Shopkeeper> {

	public AmbiguousShopkeeperNameHandler(
			String input,
			Iterable<? extends Shopkeeper> matches
	) {
		this(input, matches, DEFAULT_MAX_ENTRIES);
	}

	public AmbiguousShopkeeperNameHandler(
			String input,
			Iterable<? extends Shopkeeper> matches,
			int maxEntries
	) {
		super(input, matches, maxEntries);
	}

	@Override
	protected @Nullable Text getHeaderText() {
		Text header = Messages.ambiguousShopkeeperName;
		header.setPlaceholderArguments("name", input);
		return header;
	}

	@Override
	protected Text getEntryText(@Nullable Shopkeeper match, int index) {
		assert match != null;
		String id = String.valueOf(match.getId());
		String name = match.getName();
		String uniqueId = match.getUniqueId().toString();
		Text entry = Messages.ambiguousShopkeeperNameEntry;
		entry.setPlaceholderArguments(
				"index", index,
				"id", Text.insertion(id).childText(id).buildRoot(),
				"name", Text.insertion(name).childText(name).buildRoot(),
				"uuid", Text.insertion(uniqueId).childText(uniqueId).buildRoot()
		);
		return entry.copy(); // Copy required!
	}

	@Override
	protected @Nullable Text getMoreText() {
		return Messages.ambiguousShopkeeperNameMore;
	}
}
