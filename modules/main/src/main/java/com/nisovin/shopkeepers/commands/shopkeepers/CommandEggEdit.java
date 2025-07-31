package com.nisovin.shopkeepers.commands.shopkeepers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.ShopType;
import com.nisovin.shopkeepers.api.shopobjects.ShopObjectType;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.commands.PlayerCommand;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopcreation.ShopCreationItem;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.villagers.ShopCreationEggEditInventoryHolder;

/**
 * Ouvre l'éditeur d'œuf de création de shopkeeper pour configurer le type et les options.
 */
public class CommandEggEdit extends PlayerCommand {

	public CommandEggEdit() {
		super("eggedit");

		// Permission gets checked by testPermission.

		// Set description:
		this.setDescription(Messages.commandDescriptionEggEdit);
	}

	@Override
	public boolean testPermission(CommandSender sender) {
		if (!super.testPermission(sender)) return false;
		return PermissionUtils.hasPermission(sender, ShopkeepersPlugin.ADMIN_PERMISSION);
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		assert (input.getSender() instanceof Player);
		Player player = (Player) input.getSender();

		// Vérifier que le joueur tient un œuf de création de shopkeeper
		ItemStack itemInHand = player.getInventory().getItemInMainHand();
		if (!ShopCreationItem.isShopCreationItem(itemInHand)) {
			TextUtils.sendMessage(player, Messages.mustHoldShopCreationEgg);
			return;
		}

		// Ouvrir l'éditeur d'œuf de création
		openShopCreationEggEditor(player, itemInHand);
	}

	private void openShopCreationEggEditor(Player player, ItemStack shopCreationEgg) {
		openShopCreationEggEditor(player, shopCreationEgg, 0);
	}

	public void openShopCreationEggEditor(Player player, ItemStack shopCreationEgg, int page) {
		SKShopkeepersPlugin plugin = SKShopkeepersPlugin.getInstance();
		
		// Obtenir les types disponibles pour le joueur
		List<ShopType<?>> shopTypes = new ArrayList<>();
		List<ShopObjectType<?>> shopObjectTypes = new ArrayList<>();
		
		plugin.getShopTypeRegistry().getRegisteredTypes().forEach(shopType -> {
			if (shopType.hasPermission(player)) {
				shopTypes.add(shopType);
			}
		});
		
		plugin.getShopObjectTypeRegistry().getRegisteredTypes().forEach(shopObjectType -> {
			if (shopObjectType.hasPermission(player)) {
				shopObjectTypes.add(shopObjectType);
			}
		});
		
		// Combiner tous les éléments à afficher
		List<Object> allItems = new ArrayList<>();
		allItems.add("HEADER_SHOP_TYPES");
		allItems.addAll(shopTypes);
		allItems.add("SEPARATOR");
		allItems.add("HEADER_OBJECT_TYPES");
		allItems.addAll(shopObjectTypes);
		
		// Configuration de la pagination
		int itemsPerPage = 45; // 54 - 9 (ligne du bas pour navigation)
		int totalPages = (int) Math.ceil((double) allItems.size() / itemsPerPage);
		page = Math.max(0, Math.min(page, totalPages - 1));
		
		// Créer l'inventaire avec un holder personnalisé
		ShopCreationEggEditInventoryHolder holder = new ShopCreationEggEditInventoryHolder(shopCreationEgg.clone());
		holder.setCurrentPage(page);
		holder.setTotalPages(totalPages);
		
		String title = Messages.shopCreationEggEditorTitle + " (" + (page + 1) + "/" + totalPages + ")";
		Inventory inventory = Bukkit.createInventory(holder, 54, title);
		
		// Lier l'inventaire au holder
		holder.setInventory(inventory);
		
		// Obtenir les sélections actuelles du joueur
		ShopType<?> currentShopType = plugin.getShopTypeRegistry().getSelection(player);
		ShopObjectType<?> currentShopObjectType = plugin.getShopObjectTypeRegistry().getSelection(player);
		
		// Remplir la page actuelle
		int startIndex = page * itemsPerPage;
		int endIndex = Math.min(startIndex + itemsPerPage, allItems.size());
		
		int slot = 0;
		for (int i = startIndex; i < endIndex && slot < 45; i++) {
			Object item = allItems.get(i);
			
			if (item.equals("HEADER_SHOP_TYPES")) {
				inventory.setItem(slot++, createSectionHeader("&6Types de Shopkeeper", Material.EMERALD));
			} else if (item.equals("HEADER_OBJECT_TYPES")) {
				inventory.setItem(slot++, createSectionHeader("&6Types d'Objets", Material.DIAMOND));
			} else if (item.equals("SEPARATOR")) {
				inventory.setItem(slot++, createSeparator());
			} else if (item instanceof ShopType<?>) {
				ShopType<?> shopType = (ShopType<?>) item;
				ItemStack typeButton = createShopTypeButton(shopType, shopType.equals(currentShopType));
				inventory.setItem(slot++, typeButton);
			} else if (item instanceof ShopObjectType<?>) {
				ShopObjectType<?> shopObjectType = (ShopObjectType<?>) item;
				ItemStack objectButton = createShopObjectTypeButton(shopObjectType, shopObjectType.equals(currentShopObjectType));
				inventory.setItem(slot++, objectButton);
			}
		}
		
		// Ligne de navigation (slots 45-53)
		if (page > 0) {
			inventory.setItem(45, createPreviousPageButton(page));
		}
		
		if (page < totalPages - 1) {
			inventory.setItem(53, createNextPageButton(page));
		}
		
		// Bouton de validation au centre de la ligne de navigation
		inventory.setItem(49, createValidateButton());
		
		// Bouton d'annulation à gauche de la ligne de navigation
		inventory.setItem(46, createCancelButton());
		
		// Ouvrir l'inventaire
		player.openInventory(inventory);
	}

	private ItemStack createSectionHeader(String title, Material material) {
		ItemStack header = new ItemStack(material);
		ItemMeta meta = header.getItemMeta();
		meta.setDisplayName(TextUtils.colorize(title));
		header.setItemMeta(meta);
		return header;
	}

	private ItemStack createSeparator() {
		ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = separator.getItemMeta();
		meta.setDisplayName(TextUtils.colorize("&7"));
		separator.setItemMeta(meta);
		return separator;
	}

	private ItemStack createShopTypeButton(ShopType<?> shopType, boolean isSelected) {
		Material material = isSelected ? Material.LIME_CONCRETE : Material.WHITE_CONCRETE;
		ItemStack button = new ItemStack(material);
		ItemMeta meta = button.getItemMeta();

		// Nom du bouton
		String displayName = Messages.shopTypePrefix + shopType.getDisplayName();
		if (isSelected) {
			displayName = Messages.currentSelectionPrefix + shopType.getDisplayName();
		}
		meta.setDisplayName(TextUtils.colorize(displayName));

		// Lore avec détails
		List<String> lore = new ArrayList<>();
		lore.add(TextUtils.colorize("&7Description: &f" + shopType.getDescription()));
		if (isSelected) {
			lore.add(TextUtils.colorize("&a✓ Actuellement sélectionné"));
		} else {
			lore.add(TextUtils.colorize("&7" + Messages.clickToSelect));
		}
		meta.setLore(lore);

		button.setItemMeta(meta);
		return button;
	}

	private ItemStack createShopObjectTypeButton(ShopObjectType<?> shopObjectType, boolean isSelected) {
		Material material = isSelected ? Material.LIME_CONCRETE : Material.LIGHT_BLUE_CONCRETE;
		ItemStack button = new ItemStack(material);
		ItemMeta meta = button.getItemMeta();

		// Nom du bouton
		String displayName = Messages.shopObjectTypePrefix + shopObjectType.getDisplayName();
		if (isSelected) {
			displayName = Messages.currentSelectionPrefix + shopObjectType.getDisplayName();
		}
		meta.setDisplayName(TextUtils.colorize(displayName));

		// Lore avec détails
		List<String> lore = new ArrayList<>();
		lore.add(TextUtils.colorize("&7Type d'objet: &f" + shopObjectType.getDisplayName()));
		if (isSelected) {
			lore.add(TextUtils.colorize("&a✓ Actuellement sélectionné"));
		} else {
			lore.add(TextUtils.colorize("&7" + Messages.clickToSelect));
		}
		meta.setLore(lore);

		button.setItemMeta(meta);
		return button;
	}

	private ItemStack createValidateButton() {
		ItemStack button = new ItemStack(Material.GREEN_CONCRETE);
		ItemMeta meta = button.getItemMeta();
		meta.setDisplayName(TextUtils.colorize("&a✓ Valider la configuration"));
		
		List<String> lore = new ArrayList<>();
		lore.add(TextUtils.colorize("&7Applique la configuration actuelle"));
		lore.add(TextUtils.colorize("&7à votre œuf de création"));
		meta.setLore(lore);
		
		button.setItemMeta(meta);
		return button;
	}

	private ItemStack createCancelButton() {
		ItemStack button = new ItemStack(Material.RED_CONCRETE);
		ItemMeta meta = button.getItemMeta();
		meta.setDisplayName(TextUtils.colorize("&c✗ Annuler"));
		
		List<String> lore = new ArrayList<>();
		lore.add(TextUtils.colorize("&7Ferme l'éditeur sans"));
		lore.add(TextUtils.colorize("&7sauvegarder les changements"));
		meta.setLore(lore);
		
		button.setItemMeta(meta);
		return button;
	}

	private ItemStack createPreviousPageButton(int currentPage) {
		ItemStack button = new ItemStack(Material.ARROW);
		ItemMeta meta = button.getItemMeta();
		meta.setDisplayName(TextUtils.colorize("&6← Page précédente"));
		
		List<String> lore = new ArrayList<>();
		lore.add(TextUtils.colorize("&7Page actuelle: &e" + (currentPage + 1)));
		lore.add(TextUtils.colorize("&7Cliquez pour aller à la page &e" + currentPage));
		meta.setLore(lore);
		
		button.setItemMeta(meta);
		return button;
	}

	private ItemStack createNextPageButton(int currentPage) {
		ItemStack button = new ItemStack(Material.ARROW);
		ItemMeta meta = button.getItemMeta();
		meta.setDisplayName(TextUtils.colorize("&6Page suivante →"));
		
		List<String> lore = new ArrayList<>();
		lore.add(TextUtils.colorize("&7Page actuelle: &e" + (currentPage + 1)));
		lore.add(TextUtils.colorize("&7Cliquez pour aller à la page &e" + (currentPage + 2)));
		meta.setLore(lore);
		
		button.setItemMeta(meta);
		return button;
	}
}