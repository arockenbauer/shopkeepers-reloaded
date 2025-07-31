package com.nisovin.shopkeepers.commands.lib.argument.ambiguity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.text.TextBuilder;
import com.nisovin.shopkeepers.util.java.Validate;

public abstract class AmbiguousInputHandler<O> {

	private static final String PLACEHOLDER_HEADER = "header";
	private static final String PLACEHOLDER_ENTRY = "entry";
	private static final String PLACEHOLDER_MORE = "more";

	protected static final int DEFAULT_MAX_ENTRIES = 5;

	protected final String input; // Not null

	private final Iterable<? extends O> matches; // Not null; only iterated once
	private final int maxEntries;

	private boolean alreadyProcessed = false;
	// Can be null if there is no match, or if null is a valid match.
	// Also set even if the input is ambiguous.
	private @Nullable O firstMatch = null;
	private @Nullable Text errorMsg = null; // Null if the input is not ambiguous

	public AmbiguousInputHandler(String input, Iterable<? extends O> matches) {
		this(input, matches, DEFAULT_MAX_ENTRIES);
	}

	public AmbiguousInputHandler(String input, Iterable<? extends O> matches, int maxEntries) {
		Validate.notNull(input, "input is null");
		Validate.notNull(matches, "matches is null");
		this.input = input;
		this.matches = matches;
		this.maxEntries = maxEntries;
	}

	// Assigns the 'firstMatch' and builds the error message if there are multiple matches.
	private void processMatches() {
		if (alreadyProcessed) return;
		alreadyProcessed = true;

		Iterator<? extends O> matchesIterator = matches.iterator();
		if (!matchesIterator.hasNext()) {
			// Empty -> Not ambiguous.
			// firstMatch and errorMsg remain null.
			return;
		}

		// Note: Null may be a valid match.
		this.firstMatch = matchesIterator.next();

		if (!matchesIterator.hasNext()) {
			// Only one element -> Not ambiguous.
			// errorMsg remains null.
			return;
		}

		this.errorMsg = this.buildErrorMessage(firstMatch, matchesIterator);
		assert errorMsg != null;
	}

	// Can return null to skip.
	protected abstract @Nullable Text getHeaderText();

	// Index starts at 1 for the first match.
	// If null can be a valid match, this method needs to be able to handle that.
	// Does not return null.
	// Be sure to return a new Text copy, because getEntryText may be invoked multiple times, once
	// for every match to list in the final message!
	protected abstract Text getEntryText(@Nullable O match, int index);

	// Can return null to skip.
	protected abstract @Nullable Text getMoreText();

	// This is only called if the input has been determined to actually be ambiguous. Does not
	// return null.
	protected Text buildErrorMessage(@Nullable O firstMatch, Iterator<? extends O> furtherMatches) {
		// Note: Null may be a valid match.
		assert furtherMatches != null && furtherMatches.hasNext();
		Map<String, Object> arguments = new HashMap<>();
		TextBuilder errorMsgBuilder = Text.text("");

		// Header:
		Text header = this.getHeaderText();
		if (header != null) {
			errorMsgBuilder = errorMsgBuilder.placeholder(PLACEHOLDER_HEADER);
			arguments.put(PLACEHOLDER_HEADER, header);
		}

		int index = 1;
		@Nullable O match = firstMatch;
		while (true) {
			// Limit the number of listed matches:
			if (index > maxEntries) {
				// Text indicating that there are more matches:
				Text more = this.getMoreText();
				if (more != null) {
					errorMsgBuilder = errorMsgBuilder.newline().reset().placeholder(PLACEHOLDER_MORE);
					arguments.put(PLACEHOLDER_MORE, more);
				}
				break;
			}

			// Entry for the current match:
			Text entry = this.getEntryText(match, index);
			String entryPlaceholderKey = PLACEHOLDER_ENTRY + index;
			errorMsgBuilder = errorMsgBuilder.newline().reset().placeholder(entryPlaceholderKey);
			arguments.put(entryPlaceholderKey, entry);

			if (furtherMatches.hasNext()) {
				match = furtherMatches.next();
				index++;
			} else {
				break;
			}
		}

		Text errorMsg = errorMsgBuilder.buildRoot();
		errorMsg.setPlaceholderArguments(arguments);
		return errorMsg;
	}

	/**
	 * Gets the first match.
	 * 
	 * @return the first match, can be <code>null</code> if there are no matches or if
	 *         <code>null</code> is a valid match
	 */
	public final @Nullable O getFirstMatch() {
		this.processMatches();
		return firstMatch;
	}

	/**
	 * Checks if the input is ambiguous.
	 * <p>
	 * No {@link #getErrorMsg() error message} is available in this case.
	 * 
	 * @return <code>true</code> if the input is ambiguous
	 */
	public final boolean isInputAmbiguous() {
		return (this.getErrorMsg() != null);
	}

	/**
	 * Gets the error message {@link Text} if the input is ambiguous.
	 * 
	 * @return the error message, or <code>null</code> if the input is not ambiguous
	 */
	public final @Nullable Text getErrorMsg() {
		this.processMatches();
		return errorMsg; // Can be null
	}
}
