package com.nisovin.shopkeepers.commands.arguments;

import java.util.stream.Stream;

import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.user.User;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentParseException;
import com.nisovin.shopkeepers.commands.lib.argument.ambiguity.AmbiguousInputHandler;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentFilter;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentRejectedException;
import com.nisovin.shopkeepers.commands.lib.arguments.ObjectByIdArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.ObjectIdArgument;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.util.UserArgumentUtils.UserNameMatcher;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;

/**
 * Determines a (potentially offline) user by the given name input.
 * <p>
 * Note: This account for potentially offline current shop owners, but this does not check for
 * matching {@link OfflinePlayer}s since during command completion this would then perform a lot of
 * slow remote name lookups (for each input character), lagging the server. If needed, consider
 * using a {@link UserNameArgument} instead and doing the offline player lookup manually inside the
 * command for the final argument value.
 */
public class UserByNameArgument extends ObjectByIdArgument<String, User> {

	public UserByNameArgument(String name) {
		this(name, ArgumentFilter.acceptAny());
	}

	public UserByNameArgument(String name, ArgumentFilter<? super User> filter) {
		this(name, filter, UserNameArgument.DEFAULT_MINIMUM_COMPLETION_INPUT);
	}

	public UserByNameArgument(
			String name,
			ArgumentFilter<? super User> filter,
			int minimumCompletionInput
	) {
		super(name, filter, new IdArgumentArgs(minimumCompletionInput));
	}

	@Override
	protected ObjectIdArgument<String> createIdArgument(
			@UnknownInitialization UserByNameArgument this,
			String name,
			IdArgumentArgs args
	) {
		return new UserNameArgument(
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
				return UserByNameArgument.this.getCompletionSuggestions(
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
	 * Gets the {@link AmbiguousInputHandler} for users matched by name.
	 * <p>
	 * When overriding this method, consider applying the {@link #getDefaultErrorMsgArgs() common
	 * message arguments} to the error message returned by the {@link AmbiguousInputHandler} (if
	 * any).
	 * 
	 * @param argumentInput
	 *            the argument input
	 * @param matchedUsers
	 *            the matched users
	 * @return the ambiguous user name handler, not <code>null</code>
	 */
	protected AmbiguousInputHandler<User> getAmbiguousUserNameHandler(
			String argumentInput,
			Iterable<? extends User> matchedUsers
	) {
		var ambiguousUserNameHandler = new AmbiguousUserNameHandler(
				argumentInput,
				matchedUsers
		);
		if (ambiguousUserNameHandler.isInputAmbiguous()) {
			// Apply common message arguments:
			Text errorMsg = ambiguousUserNameHandler.getErrorMsg();
			assert errorMsg != null;
			errorMsg.setPlaceholderArguments(this.getDefaultErrorMsgArgs());
			errorMsg.setPlaceholderArguments("argument", argumentInput);
		}
		return ambiguousUserNameHandler;
	}

	/**
	 * The default implementation of getting a {@link User} by name.
	 * 
	 * @param nameInput
	 *            the name input
	 * @return the matched user, or <code>null</code> if no match is found
	 * @throws ArgumentRejectedException
	 *             if the name is ambiguous
	 */
	public final @Nullable User getDefaultUserByName(String nameInput)
			throws ArgumentRejectedException {
		// The name input can be either player name or display name:
		// Note: We do not lookup offline players here, since this is also invoked during command
		// completion and can perform a lot of slow remote lookups then, lagging the server.
		// If needed, consider using a UserNameArgument instead and doing the offline player lookup
		// manually inside the command for the final argument value.
		Stream<User> users = UserNameMatcher.EXACT.match(nameInput);
		var ambiguousUserNameHandler = this.getAmbiguousUserNameHandler(nameInput, users::iterator);
		if (ambiguousUserNameHandler.isInputAmbiguous()) {
			Text errorMsg = ambiguousUserNameHandler.getErrorMsg();
			assert errorMsg != null;
			throw new ArgumentRejectedException(this, errorMsg);
		} else {
			return ambiguousUserNameHandler.getFirstMatch();
		}
	}

	@Override
	protected @Nullable User getObject(
			CommandInput input,
			CommandContextView context,
			String nameInput
	) throws ArgumentParseException {
		// TODO !!! During command completion, we also call parse/getObject, which will trigger a
		// online name lookup for each input character change!
		return this.getDefaultUserByName(nameInput);
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
		return UserNameArgument.getDefaultCompletionSuggestions(
				input,
				context,
				minimumCompletionInput,
				idPrefix,
				filter,
				true
		);
	}
}
