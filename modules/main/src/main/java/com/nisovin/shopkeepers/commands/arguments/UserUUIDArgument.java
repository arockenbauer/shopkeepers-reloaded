package com.nisovin.shopkeepers.commands.arguments;

import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

import com.nisovin.shopkeepers.api.user.User;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentFilter;
import com.nisovin.shopkeepers.commands.lib.arguments.ObjectUUIDArgument;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.util.UserArgumentUtils;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;

/**
 * Provides suggestions for the UUIDs of (potentially offline shop owner) {@link User}s.
 * <p>
 * By default this accepts any UUID regardless of whether it corresponds to a known user.
 * <p>
 * During argument completion, this does not take UUIDs of offline {@link OfflinePlayer}s into
 * account.
 */
public class UserUUIDArgument extends ObjectUUIDArgument {

	public static final int DEFAULT_MINIMUM_COMPLETION_INPUT = ObjectUUIDArgument.DEFAULT_MINIMUM_COMPLETION_INPUT;

	// Note: Not providing a default argument filter that only accepts uuids of known users,
	// because this can be achieved more efficiently by using UserByUUIDArgument instead.

	public UserUUIDArgument(String name) {
		this(name, ArgumentFilter.acceptAny());
	}

	public UserUUIDArgument(String name, ArgumentFilter<? super UUID> filter) {
		this(name, filter, DEFAULT_MINIMUM_COMPLETION_INPUT);
	}

	public UserUUIDArgument(
			String name,
			ArgumentFilter<? super UUID> filter,
			int minimumCompletionInput
	) {
		super(name, filter, minimumCompletionInput);
	}

	@Override
	public Text getMissingArgumentErrorMsg() {
		Text text = Messages.commandPlayerArgumentMissing;
		text.setPlaceholderArguments(this.getDefaultErrorMsgArgs());
		return text;
	}

	// Using the uuid argument's 'invalid argument' message if the uuid is invalid.
	// Using the filter's 'invalid argument' message if the uuid is not accepted.

	/**
	 * Gets the default uuid completion suggestions.
	 * 
	 * @param input
	 *            the command input, not <code>null</code>
	 * @param context
	 *            the command context, not <code>null</code>
	 * @param minimumCompletionInput
	 *            the minimum input length before completion suggestions are provided
	 * @param uuidPrefix
	 *            the uuid prefix, may be empty, not <code>null</code>
	 * @param userFilter
	 *            only suggestions for users accepted by this filter are included
	 * @return the user uuid completion suggestions
	 */
	public static Iterable<? extends UUID> getDefaultCompletionSuggestions(
			CommandInput input,
			CommandContextView context,
			int minimumCompletionInput,
			String uuidPrefix,
			ArgumentFilter<? super User> userFilter
	) {
		// Only provide suggestions if there is a minimum length input:
		if (uuidPrefix.length() < minimumCompletionInput) {
			return Collections.emptyList();
		}

		String normalizedUUIDPrefix = uuidPrefix.toLowerCase(Locale.ROOT);
		return UserArgumentUtils.getKnownUsers()
				.filter(user -> userFilter.test(input, context, user))
				.map(User::getUniqueId)
				.filter(uuid -> {
					// Assumption: UUID#toString is already lowercase (normalized).
					return uuid.toString().startsWith(normalizedUUIDPrefix);
				})::iterator;
	}

	@Override
	protected Iterable<? extends UUID> getCompletionSuggestions(
			CommandInput input,
			CommandContextView context,
			String idPrefix
	) {
		return getDefaultCompletionSuggestions(
				input,
				context,
				minimumCompletionInput,
				idPrefix,
				ArgumentFilter.acceptAny()
		);
	}
}
