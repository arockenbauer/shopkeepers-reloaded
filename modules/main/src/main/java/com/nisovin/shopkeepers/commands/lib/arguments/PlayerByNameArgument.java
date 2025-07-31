package com.nisovin.shopkeepers.commands.lib.arguments;

import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentParseException;
import com.nisovin.shopkeepers.commands.lib.argument.ambiguity.AmbiguousInputHandler;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentFilter;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentRejectedException;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.lib.util.PlayerArgumentUtils.PlayerNameMatcher;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;

/**
 * Determines an online player by the given name input.
 */
public class PlayerByNameArgument extends ObjectByIdArgument<String, Player> {

	public PlayerByNameArgument(String name) {
		this(name, ArgumentFilter.acceptAny());
	}

	public PlayerByNameArgument(String name, ArgumentFilter<? super Player> filter) {
		this(name, filter, PlayerNameArgument.DEFAULT_MINIMUM_COMPLETION_INPUT);
	}

	public PlayerByNameArgument(
			String name,
			ArgumentFilter<? super Player> filter,
			int minimumCompletionInput
	) {
		super(name, filter, new IdArgumentArgs(minimumCompletionInput));
	}

	@Override
	protected ObjectIdArgument<String> createIdArgument(
			@UnknownInitialization PlayerByNameArgument this,
			String name,
			IdArgumentArgs args
	) {
		return new PlayerNameArgument(
				name,
				ArgumentFilter.acceptAny(),
				args.minimumCompletionInput
		) {
			@Override
			protected Iterable<? extends String> getCompletionSuggestions(
					CommandInput input,
					CommandContextView context,
					String idPrefix
			) {
				return PlayerByNameArgument.this.getCompletionSuggestions(
						input,
						context,
						minimumCompletionInput,
						idPrefix
				);
			}
		};
	}

	@Override
	protected Text getInvalidArgumentErrorMsgText() {
		return Messages.commandPlayerArgumentInvalid;
	}

	/**
	 * Gets the {@link AmbiguousInputHandler} for players matched by name.
	 * <p>
	 * When overriding this method, consider applying the {@link #getDefaultErrorMsgArgs() common
	 * message arguments} to the error message returned by the {@link AmbiguousInputHandler} (if
	 * any).
	 * 
	 * @param argumentInput
	 *            the argument input
	 * @param matchedPlayers
	 *            the matched players
	 * @return the ambiguous player name handler, not <code>null</code>
	 */
	protected AmbiguousInputHandler<Player> getAmbiguousPlayerNameHandler(
			String argumentInput,
			Iterable<? extends Player> matchedPlayers
	) {
		var ambiguousPlayerNameHandler = new AmbiguousPlayerNameHandler<Player>(
				argumentInput,
				matchedPlayers
		);
		if (ambiguousPlayerNameHandler.isInputAmbiguous()) {
			// Apply common message arguments:
			Text errorMsg = ambiguousPlayerNameHandler.getErrorMsg();
			assert errorMsg != null;
			errorMsg.setPlaceholderArguments(this.getDefaultErrorMsgArgs());
			errorMsg.setPlaceholderArguments("argument", argumentInput);
		}
		return ambiguousPlayerNameHandler;
	}

	/**
	 * The default implementation of getting a {@link Player} by name.
	 * 
	 * @param nameInput
	 *            the name input
	 * @return the matched player, or <code>null</code> if no match is found
	 * @throws ArgumentRejectedException
	 *             if the name is ambiguous
	 */
	public final @Nullable Player getDefaultPlayerByName(String nameInput)
			throws ArgumentRejectedException {
		// The name input can be either player name or display name:
		Stream<Player> players = PlayerNameMatcher.EXACT.match(nameInput);
		var ambiguousPlayerNameHandler = this.getAmbiguousPlayerNameHandler(nameInput, players::iterator);
		if (ambiguousPlayerNameHandler.isInputAmbiguous()) {
			Text errorMsg = ambiguousPlayerNameHandler.getErrorMsg();
			assert errorMsg != null;
			throw new ArgumentRejectedException(this, errorMsg);
		} else {
			return ambiguousPlayerNameHandler.getFirstMatch();
		}
	}

	@Override
	protected @Nullable Player getObject(
			CommandInput input,
			CommandContextView context,
			String nameInput
	) throws ArgumentParseException {
		return this.getDefaultPlayerByName(nameInput);
	}

	@Override
	protected Iterable<? extends String> getCompletionSuggestions(
			CommandInput input,
			CommandContextView context,
			int minimumCompletionInput,
			String idPrefix
	) {
		// Note: Whether to include display name suggestions usually depends on whether the used
		// matching function considers display names.
		return PlayerNameArgument.getDefaultCompletionSuggestions(
				input,
				context,
				minimumCompletionInput,
				idPrefix,
				filter,
				true
		);
	}
}
