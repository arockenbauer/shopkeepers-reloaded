package com.nisovin.shopkeepers.commands.arguments;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentParseException;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentsReader;
import com.nisovin.shopkeepers.commands.lib.argument.CommandArgument;
import com.nisovin.shopkeepers.commands.lib.argument.ambiguity.AmbiguousInputHandler;
import com.nisovin.shopkeepers.commands.lib.argument.filter.ArgumentRejectedException;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeepersResult;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.java.ObjectUtils;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * A {@link CommandArgument} that returns the targeted shopkeeper without consuming any arguments.
 * <p>
 * If the sender is not a player, a 'requires a player' error message is thrown. If no shopkeeper is
 * targeted or the targeted shopkeeper is not accepted, the filter's corresponding error message is
 * used.
 */
public class TargetShopkeeperArgument extends CommandArgument<Shopkeeper> {

	private final TargetShopkeeperFilter filter; // Not null

	public TargetShopkeeperArgument(String name) {
		this(name, TargetShopkeeperFilter.ANY);
	}

	public TargetShopkeeperArgument(String name, TargetShopkeeperFilter filter) {
		super(name);
		Validate.notNull(filter, "filter is null");
		this.filter = filter;
	}

	@Override
	public boolean isOptional() {
		return true; // Does not require user input
	}

	/**
	 * Gets the {@link AmbiguousInputHandler} for the targeted shopkeepers.
	 * <p>
	 * When overriding this method, consider applying the {@link #getDefaultErrorMsgArgs() common
	 * message arguments} to the error message returned by the {@link AmbiguousInputHandler} (if
	 * any).
	 * 
	 * @param matchedShopkeepers
	 *            the matched shopkeepers
	 * @return the ambiguous target shopkeeper handler, not <code>null</code>
	 */
	protected AmbiguousInputHandler<Shopkeeper> getAmbiguousTargetShopkeeperHandler(
			Iterable<? extends Shopkeeper> matchedShopkeepers
	) {
		var ambiguousTargetShopkeeperHandler = new AmbiguousTargetShopkeeperHandler(matchedShopkeepers);
		if (ambiguousTargetShopkeeperHandler.isInputAmbiguous()) {
			// Apply common message arguments:
			Text errorMsg = ambiguousTargetShopkeeperHandler.getErrorMsg();
			assert errorMsg != null;
			errorMsg.setPlaceholderArguments(this.getDefaultErrorMsgArgs());
		}
		return ambiguousTargetShopkeeperHandler;
	}

	@Override
	public Shopkeeper parseValue(
			CommandInput input,
			CommandContextView context,
			ArgumentsReader argsReader
	) throws ArgumentParseException {
		Player player = ObjectUtils.castOrNull(input.getSender(), Player.class);
		if (player == null) {
			throw this.requiresPlayerError();
		}

		TargetShopkeepersResult result = ShopkeeperArgumentUtils.findTargetedShopkeepers(
				player,
				filter
		);
		if (!result.isSuccess()) {
			Text error = Unsafe.assertNonNull(result.getErrorMessage());
			throw new ArgumentParseException(this, error);
		} else {
			assert !result.getShopkeepers().isEmpty();

			var ambiguousTargetShopkeeperHandler = this.getAmbiguousTargetShopkeeperHandler(
					result.getShopkeepers()
			);
			if (ambiguousTargetShopkeeperHandler.isInputAmbiguous()) {
				Text errorMsg = ambiguousTargetShopkeeperHandler.getErrorMsg();
				assert errorMsg != null;
				throw new ArgumentRejectedException(this, errorMsg);
			} else {
				var shopkeeper = ambiguousTargetShopkeeperHandler.getFirstMatch();
				assert shopkeeper != null;
				return shopkeeper;
			}
		}
	}

	@Override
	public List<? extends String> complete(
			CommandInput input,
			CommandContextView context,
			ArgumentsReader argsReader
	) {
		return Collections.emptyList();
	}
}
