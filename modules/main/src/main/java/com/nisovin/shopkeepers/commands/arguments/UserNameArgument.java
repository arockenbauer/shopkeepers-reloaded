package com.nisovin.shopkeepers.commands.arguments;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.user.User;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentFilter;
import com.nisovin.shopkeepers.commands.lib.arguments.ObjectNameArgument;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.util.UserArgumentUtils;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.StringUtils;

/**
 * Provides suggestions for (potentially offline) {@link User} names.
 * <p>
 * By default this accepts any name regardless of whether it corresponds to a known user.
 */
public class UserNameArgument extends ObjectNameArgument {

	public static final int DEFAULT_MINIMUM_COMPLETION_INPUT = ObjectNameArgument.DEFAULT_MINIMUM_COMPLETION_INPUT;

	// Note: Not providing a default argument filter that only accepts names of known users,
	// because this can be achieved more efficiently by using UserByNameArgument instead.

	public UserNameArgument(String name) {
		this(name, ArgumentFilter.acceptAny());
	}

	public UserNameArgument(String name, ArgumentFilter<? super String> filter) {
		this(name, filter, DEFAULT_MINIMUM_COMPLETION_INPUT);
	}

	// Joining remaining args doesn't make much sense for user names (and we normalize whitespace
	// in display names).
	public UserNameArgument(
			String name,
			ArgumentFilter<? super String> filter,
			int minimumCompletionInput
	) {
		super(name, false, filter, minimumCompletionInput);
	}

	@Override
	public Text getMissingArgumentErrorMsg() {
		Text text = Messages.commandPlayerArgumentMissing;
		text.setPlaceholderArguments(this.getDefaultErrorMsgArgs());
		return text;
	}

	// Using the filter's 'invalid argument' message if the name is not accepted.

	/**
	 * Gets the default name completion suggestions.
	 * 
	 * @param input
	 *            the command input, not <code>null</code>
	 * @param context
	 *            the command context, not <code>null</code>
	 * @param minimumCompletionInput
	 *            the minimum prefix length before completion suggestions are provided
	 * @param namePrefix
	 *            the name prefix, may be empty, not <code>null</code>
	 * @param userFilter
	 *            only suggestions for users accepted by this filter are included
	 * @param includeDisplayNames
	 *            <code>true</code> to include display name suggestions
	 * @return the user name completion suggestions
	 */
	public static Iterable<? extends String> getDefaultCompletionSuggestions(
			CommandInput input,
			CommandContextView context,
			int minimumCompletionInput,
			String namePrefix,
			ArgumentFilter<? super User> userFilter,
			boolean includeDisplayNames
	) {
		// Only provide suggestions if there is a minimum length input:
		if (namePrefix.length() < minimumCompletionInput) {
			return Collections.emptyList();
		}

		// Assumption: Name prefix does not contain color codes (users are not expected to specify
		// color codes).
		// Normalizes whitespace and converts to lowercase:
		String normalizedNamePrefix = StringUtils.normalize(namePrefix);
		Iterable<String> suggestions = UserArgumentUtils.getKnownUsers()
				.filter(user -> userFilter.test(input, context, user))
				.<@Nullable String>map(user -> {
					// Note: Not suggesting both the name and display name for the same user.
					// Assumption: User names don't contain whitespace or color codes
					String name = Unsafe.assertNonNull(user.getName());
					if (StringUtils.normalize(name).startsWith(normalizedNamePrefix)) {
						return name;
					} else if (includeDisplayNames) {
						String displayName = TextUtils.stripColor(user.getDisplayName());
						String normalizedWithCase = StringUtils.normalizeKeepCase(displayName);
						String normalized = normalizedWithCase.toLowerCase(Locale.ROOT);
						if (normalized.startsWith(normalizedNamePrefix)) {
							return normalizedWithCase;
						}
					}
					return null; // No match
				}).filter(Objects::nonNull)
				.map(Unsafe::assertNonNull)::iterator;
		return suggestions;
	}

	@Override
	protected Iterable<? extends String> getCompletionSuggestions(
			CommandInput input,
			CommandContextView context,
			String idPrefix
	) {
		return getDefaultCompletionSuggestions(
				input,
				context,
				minimumCompletionInput,
				idPrefix,
				ArgumentFilter.acceptAny(),
				true
		);
	}
}
