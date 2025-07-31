package com.nisovin.shopkeepers.commands.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.user.User;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentParseException;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentsReader;
import com.nisovin.shopkeepers.commands.lib.argument.CommandArgument;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentFilter;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentRejectedException;
import com.nisovin.shopkeepers.commands.lib.arguments.TypedFirstOfArgument;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * Accepts a {@link User} specified by either name (might not have to be exact, depending on the
 * used matching function) or UUID.
 * <p>
 * Accounts for known users, e.g. online players and shop owners. This does, however, not check for
 * matching offline players by name (see rationale in {@link UserByNameArgument}), nor does this
 * take {@link OfflinePlayer}s into account during argument completion.
 */
public class UserArgument extends CommandArgument<User> {

	protected final ArgumentFilter<? super User> filter; // Not null
	private final UserByNameArgument userNameArgument;
	private final UserByUUIDArgument userUUIDArgument;
	private final TypedFirstOfArgument<User> firstOfArgument;

	public UserArgument(String name) {
		this(name, ArgumentFilter.acceptAny());
	}

	public UserArgument(String name, ArgumentFilter<? super User> filter) {
		this(
				name,
				filter,
				UserNameArgument.DEFAULT_MINIMUM_COMPLETION_INPUT,
				UserUUIDArgument.DEFAULT_MINIMUM_COMPLETION_INPUT
		);
	}

	public UserArgument(
			String name,
			ArgumentFilter<? super User> filter,
			int minimalNameCompletionInput,
			int minimumUUIDCompletionInput
	) {
		super(name);
		Validate.notNull(filter, "filter is null");
		this.filter = filter;
		this.userNameArgument = new UserByNameArgument(
				name + ":name",
				filter,
				minimalNameCompletionInput
		) {
			@Override
			public @Nullable User getObject(
					CommandInput input,
					CommandContextView context,
					String nameInput
			) throws ArgumentParseException {
				return Unsafe.initialized(UserArgument.this).getUserByName(nameInput);
			}

			@Override
			protected Iterable<? extends String> getCompletionSuggestions(
					CommandInput input,
					CommandContextView context,
					int minimumCompletionInput,
					String idPrefix
			) {
				return Unsafe.initialized(UserArgument.this).getNameCompletionSuggestions(
						input,
						context,
						minimumCompletionInput,
						idPrefix
				);
			}
		};
		this.userUUIDArgument = new UserByUUIDArgument(
				name + ":uuid",
				filter,
				minimumUUIDCompletionInput
		) {
			@Override
			protected Iterable<? extends UUID> getCompletionSuggestions(
					CommandInput input,
					CommandContextView context,
					int minimumCompletionInput,
					String idPrefix
			) {
				return Unsafe.initialized(UserArgument.this).getUUIDCompletionSuggestions(
						input,
						context,
						minimumCompletionInput,
						idPrefix
				);
			}
		};
		this.firstOfArgument = new TypedFirstOfArgument<>(
				name + ":firstOf",
				Arrays.asList(userNameArgument, userUUIDArgument),
				false,
				false
		);
		firstOfArgument.setParent(this);
	}

	@Override
	public User parseValue(
			CommandInput input,
			CommandContextView context,
			ArgumentsReader argsReader
	) throws ArgumentParseException {
		// Also handles argument exceptions:
		return firstOfArgument.parseValue(input, context, argsReader);
	}

	@Override
	public List<? extends String> complete(
			CommandInput input,
			CommandContextView context,
			ArgumentsReader argsReader
	) {
		return firstOfArgument.complete(input, context, argsReader);
	}

	/**
	 * Gets a {@link User} that matches the given name input.
	 * <p>
	 * This can be overridden if a different behavior is required. You may also want to override
	 * {@link #getNameCompletionSuggestions(CommandInput, CommandContextView, int, String)} and
	 * {@link #getUUIDCompletionSuggestions(CommandInput, CommandContextView, int, String)} then.
	 * 
	 * @param nameInput
	 *            the name input
	 * @return the matched user, or <code>null</code>
	 * @throws ArgumentRejectedException
	 *             if the name is ambiguous
	 */
	public @Nullable User getUserByName(String nameInput) throws ArgumentRejectedException {
		return userNameArgument.getDefaultUserByName(nameInput);
	}

	/**
	 * Gets the name completion suggestions for the given name prefix.
	 * <p>
	 * This should take this argument's user filter into account.
	 * 
	 * @param input
	 *            the command input, not <code>null</code>
	 * @param context
	 *            the command context, not <code>null</code>
	 * @param minimumCompletionInput
	 *            the minimum input length before completion suggestions are provided
	 * @param idPrefix
	 *            the id prefix, may be empty, not <code>null</code>
	 * @return the suggestions
	 */
	protected Iterable<? extends String> getNameCompletionSuggestions(
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

	/**
	 * Gets the uuid completion suggestions for the given name prefix.
	 * <p>
	 * This should take this argument's User filter into account.
	 * 
	 * @param input
	 *            the command input, not <code>null</code>
	 * @param context
	 *            the command context, not <code>null</code>
	 * @param minimumCompletionInput
	 *            the minimum input length before completion suggestions are provided
	 * @param idPrefix
	 *            the id prefix, may be empty, not <code>null</code>
	 * @return the suggestions
	 */
	protected Iterable<? extends UUID> getUUIDCompletionSuggestions(
			CommandInput input,
			CommandContextView context,
			int minimumCompletionInput,
			String idPrefix
	) {
		return UserUUIDArgument.getDefaultCompletionSuggestions(
				input,
				context,
				minimumCompletionInput,
				idPrefix,
				filter
		);
	}
}
