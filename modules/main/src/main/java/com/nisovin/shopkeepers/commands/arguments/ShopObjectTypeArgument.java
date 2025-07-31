package com.nisovin.shopkeepers.commands.arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopobjects.ShopObjectType;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentParseException;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentsReader;
import com.nisovin.shopkeepers.commands.lib.argument.CommandArgument;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.java.ObjectUtils;
import com.nisovin.shopkeepers.util.java.StringUtils;

public class ShopObjectTypeArgument extends CommandArgument<ShopObjectType<?>> {

	public ShopObjectTypeArgument(String name) {
		super(name);
	}

	@Override
	protected Text getInvalidArgumentErrorMsgText() {
		return Messages.commandShopObjectTypeArgumentInvalid;
	}

	@Override
	public ShopObjectType<?> parseValue(
			CommandInput input,
			CommandContextView context,
			ArgumentsReader argsReader
	) throws ArgumentParseException {
		if (!argsReader.hasNext()) {
			throw this.missingArgumentError();
		}
		String argument = argsReader.next();
		ShopObjectType<?> value = ShopkeepersPlugin.getInstance().getShopObjectTypeRegistry().match(argument);
		if (value == null) {
			throw this.invalidArgumentError(argument);
		}
		return value;
	}

	@Override
	public List<? extends String> complete(
			CommandInput input,
			CommandContextView context,
			ArgumentsReader argsReader
	) {
		if (argsReader.getRemainingSize() != 1) {
			return Collections.emptyList();
		}

		Player senderPlayer = ObjectUtils.castOrNull(input.getSender(), Player.class);
		List<String> suggestions = new ArrayList<>();
		String partialArg = StringUtils.normalize(argsReader.next());
		for (ShopObjectType<?> shopObjectType : ShopkeepersPlugin.getInstance().getShopObjectTypeRegistry().getRegisteredTypes()) {
			if (suggestions.size() >= MAX_SUGGESTIONS) break;
			if (!shopObjectType.isEnabled()) continue;
			if (senderPlayer != null && !shopObjectType.hasPermission(senderPlayer)) continue;

			String displayName = shopObjectType.getDisplayName();
			displayName = StringUtils.normalizeKeepCase(displayName);
			String displayNameNorm = displayName.toLowerCase(Locale.ROOT);
			if (displayNameNorm.startsWith(partialArg)) {
				suggestions.add(displayName);
			} else {
				String identifier = shopObjectType.getIdentifier();
				if (identifier.startsWith(partialArg)) {
					suggestions.add(identifier);
				}
			}
		}
		return Collections.unmodifiableList(suggestions);
	}
}
