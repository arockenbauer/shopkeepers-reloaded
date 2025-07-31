package com.nisovin.shopkeepers.commands.arguments;

import java.util.stream.Stream;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentParseException;
import com.nisovin.shopkeepers.commands.lib.argument.ambiguity.AmbiguousInputHandler;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentFilter;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentRejectedException;
import com.nisovin.shopkeepers.commands.lib.arguments.ObjectByIdArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.ObjectIdArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.PlayerNameArgument;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.ShopkeeperNameMatchers;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.java.CollectionUtils;

/**
 * Determines a shopkeeper by the given name input.
 */
public class ShopkeeperByNameArgument extends ObjectByIdArgument<String, Shopkeeper> {

	public ShopkeeperByNameArgument(String name) {
		this(name, ArgumentFilter.acceptAny());
	}

	public ShopkeeperByNameArgument(
			String name,
			ArgumentFilter<? super Shopkeeper> filter
	) {
		this(name, false, filter, PlayerNameArgument.DEFAULT_MINIMUM_COMPLETION_INPUT);
	}

	public ShopkeeperByNameArgument(
			String name,
			boolean joinRemainingArgs,
			ArgumentFilter<? super Shopkeeper> filter,
			int minimumCompletionInput
	) {
		super(name, filter, new IdArgumentArgs(minimumCompletionInput, joinRemainingArgs));
	}

	@Override
	protected ObjectIdArgument<String> createIdArgument(
			@UnknownInitialization ShopkeeperByNameArgument this,
			String name,
			IdArgumentArgs args
	) {
		return new ShopkeeperNameArgument(
				name,
				args.joinRemainingArgs,
				ArgumentFilter.acceptAny(),
				args.minimumCompletionInput
		) {
			@Override
			protected Iterable<? extends String> getCompletionSuggestions(
					CommandInput input,
					CommandContextView context,
					String idPrefix
			) {
				return ShopkeeperByNameArgument.this.getCompletionSuggestions(
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
		return Messages.commandShopkeeperArgumentInvalid;
	}

	/**
	 * Gets the {@link AmbiguousInputHandler} for the shopkeepers matched by name.
	 * <p>
	 * When overriding this method, consider applying the {@link #getDefaultErrorMsgArgs() common
	 * message arguments} to the error message returned by the {@link AmbiguousInputHandler} (if
	 * any).
	 * 
	 * @param argumentInput
	 *            the argument input
	 * @param matchedShopkeepers
	 *            the matched shopkeepers
	 * @return the ambiguous shopkeeper name handler, not <code>null</code>
	 */
	protected AmbiguousInputHandler<Shopkeeper> getAmbiguousShopkeeperNameHandler(
			String argumentInput,
			Iterable<? extends Shopkeeper> matchedShopkeepers
	) {
		var ambiguousShopkeeperNameHandler = new AmbiguousShopkeeperNameHandler(
				argumentInput,
				matchedShopkeepers
		);
		if (ambiguousShopkeeperNameHandler.isInputAmbiguous()) {
			// Apply common message arguments:
			Text errorMsg = ambiguousShopkeeperNameHandler.getErrorMsg();
			assert errorMsg != null;
			errorMsg.setPlaceholderArguments(this.getDefaultErrorMsgArgs());
			errorMsg.setPlaceholderArguments("argument", argumentInput);
		}
		return ambiguousShopkeeperNameHandler;
	}

	/**
	 * The default implementation of getting a {@link Shopkeeper} by name.
	 * 
	 * @param nameInput
	 *            the name input
	 * @return the matched Shopkeeper, or <code>null</code> if no match is found
	 * @throws ArgumentRejectedException
	 *             if the name is ambiguous
	 */
	public final @Nullable Shopkeeper getDefaultShopkeeperByName(String nameInput)
			throws ArgumentRejectedException {
		Stream<? extends Shopkeeper> shopkeepers = ShopkeeperNameMatchers.DEFAULT.match(nameInput);
		var ambiguousShopkeeperNameHandler = this.getAmbiguousShopkeeperNameHandler(
				nameInput,
				CollectionUtils.toIterable(shopkeepers)
		);
		if (ambiguousShopkeeperNameHandler.isInputAmbiguous()) {
			Text errorMsg = ambiguousShopkeeperNameHandler.getErrorMsg();
			assert errorMsg != null;
			throw new ArgumentRejectedException(this, errorMsg);
		} else {
			return ambiguousShopkeeperNameHandler.getFirstMatch();
		}
	}

	@Override
	protected @Nullable Shopkeeper getObject(
			CommandInput input,
			CommandContextView context,
			String nameInput
	) throws ArgumentParseException {
		return this.getDefaultShopkeeperByName(nameInput);
	}

	@Override
	protected Iterable<? extends String> getCompletionSuggestions(
			CommandInput input,
			CommandContextView context,
			int minimumCompletionInput,
			String idPrefix
	) {
		return ShopkeeperNameArgument.getDefaultCompletionSuggestions(
				input,
				context,
				minimumCompletionInput,
				idPrefix,
				filter
		);
	}
}
