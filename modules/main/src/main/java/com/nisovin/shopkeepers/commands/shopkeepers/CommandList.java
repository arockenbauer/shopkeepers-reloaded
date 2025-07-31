package com.nisovin.shopkeepers.commands.shopkeepers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.ShopkeeperRegistry;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.commands.lib.Command;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.arguments.AnyStringFallback;
import com.nisovin.shopkeepers.commands.lib.arguments.FirstOfArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.LiteralArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.PlayerByNameArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.PlayerUUIDArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.PositiveIntegerArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.SenderPlayerNameFallback;
import com.nisovin.shopkeepers.commands.lib.arguments.TransformedArgument;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.lib.util.PlayerArgumentUtils;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils;
import com.nisovin.shopkeepers.commands.util.ShopkeeperArgumentUtils.OwnedPlayerShopsResult;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;

class CommandList extends Command {

	private static final String ARGUMENT_ALL = "all";
	private static final String ARGUMENT_ADMIN = "admin";
	private static final String ARGUMENT_PLAYER = "player";
	private static final String ARGUMENT_PLAYER_NAME = "player:name";
	private static final String ARGUMENT_PLAYER_UUID = "player:uuid";
	private static final String ARGUMENT_PAGE = "page";

	private static final int ENTRIES_PER_PAGE = 8;

	private final ShopkeeperRegistry shopkeeperRegistry;

	CommandList(ShopkeeperRegistry shopkeeperRegistry) {
		super("list");
		this.shopkeeperRegistry = shopkeeperRegistry;

		// Permission gets checked by testPermission and during execution.

		// Set description:
		this.setDescription(Messages.commandDescriptionList);

		// Arguments:
		this.addArgument(new FirstOfArgument("target", Arrays.asList(
				new LiteralArgument(ARGUMENT_ALL),
				new LiteralArgument(ARGUMENT_ADMIN),
				new FirstOfArgument(ARGUMENT_PLAYER, Arrays.asList(
						// TODO Provide completions for known shop owners?
						new PlayerUUIDArgument(ARGUMENT_PLAYER_UUID), // Accepts any uuid
						// Only accepts names of online players initially, but falls back to any
						// given name or the sender's name (using a fallback to give the following
						// page argument a chance to parse the input first)
						// TODO Add alias 'own'?
						new SenderPlayerNameFallback(new AnyStringFallback(
								new TransformedArgument<>(
										new PlayerByNameArgument(ARGUMENT_PLAYER_NAME),
										(player) -> Unsafe.assertNonNull(player.getName())
								)
						))
				), false) // Don't join formats
		), true, true)); // Join and reverse formats
		this.addArgument(new PositiveIntegerArgument(ARGUMENT_PAGE).orDefaultValue(1));
	}

	@Override
	public boolean testPermission(CommandSender sender) {
		if (!super.testPermission(sender)) return false;
		return PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_OWN_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_OTHERS_PERMISSION)
				|| PermissionUtils.hasPermission(sender, ShopkeepersPlugin.LIST_ADMIN_PERMISSION);
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		CommandSender sender = input.getSender();
		int page = context.get(ARGUMENT_PAGE);
		boolean listAllShops = context.has(ARGUMENT_ALL);
		boolean listAdminShops = context.has(ARGUMENT_ADMIN);
		UUID targetPlayerUUID = context.getOrNull(ARGUMENT_PLAYER_UUID); // Can be null
		String targetPlayerName = context.getOrNull(ARGUMENT_PLAYER_NAME); // Can be null
		assert listAllShops ^ listAdminShops ^ (targetPlayerUUID != null ^ targetPlayerName != null);

		List<? extends Shopkeeper> shops;
		if (listAllShops) {
			// Permission check:
			this.checkPermission(sender, ShopkeepersPlugin.LIST_ADMIN_PERMISSION);
			this.checkPermission(sender, ShopkeepersPlugin.LIST_OTHERS_PERMISSION);

			shops = new ArrayList<>(shopkeeperRegistry.getAllShopkeepers());
		} else if (listAdminShops) {
			// Permission check:
			this.checkPermission(sender, ShopkeepersPlugin.LIST_ADMIN_PERMISSION);

			// Searching admin shops:
			List<Shopkeeper> adminShops = new ArrayList<>();
			for (Shopkeeper shopkeeper : shopkeeperRegistry.getAllShopkeepers()) {
				if (shopkeeper instanceof AdminShopkeeper) {
					adminShops.add(shopkeeper);
				}
			}
			shops = adminShops;
		} else {
			// Check if the target matches the sender player:
			boolean targetOwnShops = false;
			Player senderPlayer = (sender instanceof Player) ? (Player) sender : null;
			String senderName = Unsafe.assertNonNull(sender.getName());
			if (senderPlayer != null
					&& (senderPlayer.getUniqueId().equals(targetPlayerUUID)
							|| senderName.equalsIgnoreCase(targetPlayerName))) {
				targetOwnShops = true;
				// Get missing / exact player information:
				targetPlayerUUID = senderPlayer.getUniqueId();
				targetPlayerName = senderPlayer.getName();
			} else if (targetPlayerName != null) {
				// Check if the target matches an online player:
				// If the name matches an online player, list that player's shops (regardless of if
				// the name is ambiguous / if there are shops of other players with matching name):
				// Note: Case insensitive.
				Player onlinePlayer = Bukkit.getPlayerExact(targetPlayerName);
				if (onlinePlayer != null) {
					// Get missing / exact player information:
					targetPlayerUUID = onlinePlayer.getUniqueId();
					targetPlayerName = onlinePlayer.getName();
				}
			}

			// Permission check:
			if (targetOwnShops) {
				// List own player shopkeepers:
				this.checkPermission(sender, ShopkeepersPlugin.LIST_OWN_PERMISSION);
			} else {
				// List other player shopkeepers:
				this.checkPermission(sender, ShopkeepersPlugin.LIST_OTHERS_PERMISSION);
			}

			// Search for shops owned by the target player:
			OwnedPlayerShopsResult ownedPlayerShopsResult = ShopkeeperArgumentUtils.getOwnedPlayerShops(
					targetPlayerUUID,
					targetPlayerName
			);
			assert ownedPlayerShopsResult != null;

			// If the input name is ambiguous, we print an error and require the player to be
			// specified by uuid:
			Map<? extends UUID, ? extends String> matchingShopOwners = ownedPlayerShopsResult.getMatchingShopOwners();
			assert matchingShopOwners != null;
			if (matchingShopOwners.size() > 1) {
				assert targetPlayerName != null;
				boolean ambiguous = PlayerArgumentUtils.handleAmbiguousPlayerName(
						sender,
						targetPlayerName,
						matchingShopOwners.entrySet()
				);
				if (ambiguous) {
					return;
				}
			}

			// Get missing / exact player information:
			targetPlayerUUID = ownedPlayerShopsResult.getPlayerUUID();
			targetPlayerName = ownedPlayerShopsResult.getPlayerName();

			// Get found shops:
			shops = ownedPlayerShopsResult.getShops();
		}
		assert shops != null;

		int shopsCount = shops.size();
		int maxPage = Math.max(1, (int) Math.ceil((double) shopsCount / ENTRIES_PER_PAGE));
		page = Math.max(1, Math.min(page, maxPage));

		if (listAllShops) {
			// Listing all shops:
			TextUtils.sendMessage(sender, Messages.listAllShopsHeader,
					"shopsCount", shopsCount,
					"page", page,
					"maxPage", maxPage
			);
		} else if (listAdminShops) {
			// Listing admin shops:
			TextUtils.sendMessage(sender, Messages.listAdminShopsHeader,
					"shopsCount", shopsCount,
					"page", page,
					"maxPage", maxPage
			);
		} else {
			// Listing player shops:
			TextUtils.sendMessage(sender, Messages.listPlayerShopsHeader,
					"player", TextUtils.getPlayerText(targetPlayerName, targetPlayerUUID),
					"shopsCount", shopsCount,
					"page", page,
					"maxPage", maxPage
			);
		}

		int startIndex = (page - 1) * ENTRIES_PER_PAGE;
		int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, shopsCount);
		for (int index = startIndex; index < endIndex; index++) {
			Shopkeeper shopkeeper = shops.get(index);
			String shopName = shopkeeper.getName(); // Can be empty
			// TODO Add shop info as hover text.
			// TODO Add owner name/uuid as message arguments?
			// TODO Move into shopkeeper.
			TextUtils.sendMessage(sender, Messages.listShopsEntry,
					"shopIndex", (index + 1),
					"shopUUID", shopkeeper.getUniqueId().toString(),
					// deprecated, use {shopId} instead; TODO Remove at some point
					"shopSessionId", shopkeeper.getId(),
					"shopId", shopkeeper.getId(),
					// TODO Find a better solution for this special case, since this is specific to
					// the used format.
					// Maybe by supporting conditional prefixes/suffixes for placeholders inside the
					// format Strings?
					"shopName", (shopName.isEmpty() ? "" : (shopName + " ")),
					"location", shopkeeper.getPositionString(),
					"shopType", shopkeeper.getType().getIdentifier(),
					"objectType", shopkeeper.getShopObject().getType().getIdentifier()
			);
		}
	}
}
