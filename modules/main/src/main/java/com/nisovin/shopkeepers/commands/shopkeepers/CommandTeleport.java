package com.nisovin.shopkeepers.commands.shopkeepers;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.ui.DefaultUITypes;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperArgument;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperFilter;
import com.nisovin.shopkeepers.commands.arguments.TargetShopkeeperFallback;
import com.nisovin.shopkeepers.commands.lib.Command;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.arguments.LiteralArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.PlayerArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.SenderPlayerFallback;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.teleporting.ShopkeeperTeleporter;

class CommandTeleport extends Command {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";
	private static final String ARGUMENT_FORCE = "force";
	private static final String ARGUMENT_PLAYER = "player";

	CommandTeleport() {
		super("teleport", Arrays.asList("tp"));

		// Set permission:
		this.setPermission(ShopkeepersPlugin.TELEPORT_PERMISSION);

		// Set description:
		this.setDescription(Messages.commandDescriptionTeleport);

		// Arguments:
		// Shopkeeper filter: Only list shops that the executing player can trade with. Ignored for
		// non-player command senders.
		this.addArgument(new TargetShopkeeperFallback(
				new ShopkeeperArgument(ARGUMENT_SHOPKEEPER,
						ShopkeeperFilter.withAccess(DefaultUITypes.TRADING())),
				TargetShopkeeperFilter.ANY
		));
		this.addArgument(new SenderPlayerFallback(new PlayerArgument(ARGUMENT_PLAYER)));
		this.addArgument(new LiteralArgument(ARGUMENT_FORCE).optional());
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		CommandSender sender = input.getSender();

		Shopkeeper shopkeeper = context.get(ARGUMENT_SHOPKEEPER);
		boolean force = context.has(ARGUMENT_FORCE);
		Player targetPlayer = context.get(ARGUMENT_PLAYER);
		assert targetPlayer != null;
		if (targetPlayer != sender) {
			this.checkPermission(sender, ShopkeepersPlugin.TELEPORT_OTHERS_PERMISSION);
		}

		ShopkeeperTeleporter.teleport(targetPlayer, shopkeeper, force, sender);
	}
}
