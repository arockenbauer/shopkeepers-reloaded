package com.nisovin.shopkeepers.villagers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.ShopType;
import com.nisovin.shopkeepers.api.shopobjects.ShopObjectType;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Gère les interactions avec l'éditeur d'œuf de création de shopkeeper.
 */
public class ShopCreationEggEditListener implements Listener {

	private final SKShopkeepersPlugin plugin;

	public ShopCreationEggEditListener(SKShopkeepersPlugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		Player player = (Player) event.getWhoClicked();

		Inventory inventory = event.getInventory();
		InventoryHolder holder = inventory.getHolder();
		
		// Vérifier si c'est notre inventaire d'éditeur d'œuf
		if (!(holder instanceof ShopCreationEggEditInventoryHolder)) return;
		ShopCreationEggEditInventoryHolder eggEditHolder = (ShopCreationEggEditInventoryHolder) holder;

		// Annuler l'événement pour empêcher la prise d'objets
		event.setCancelled(true);

		ItemStack clickedItem = event.getCurrentItem();
		if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

		ItemMeta meta = clickedItem.getItemMeta();
		if (meta == null) return;

		String displayName = meta.getDisplayName();
		if (displayName == null) return;

		// Gérer les différents types de boutons
		if (displayName.contains("✓ Valider la configuration")) {
			// Bouton de validation - fermer l'inventaire et confirmer
			player.closeInventory();
			TextUtils.sendMessage(player, Messages.shopCreationEggConfigured);
			return;
		}

		if (displayName.contains("✗ Annuler")) {
			// Bouton d'annulation - fermer l'inventaire sans sauvegarder
			player.closeInventory();
			return;
		}

		if (displayName.contains("← Page précédente")) {
			// Navigation vers la page précédente
			int currentPage = eggEditHolder.getCurrentPage();
			if (currentPage > 0) {
				reopenEggEditor(player, eggEditHolder.getOriginalEgg(), currentPage - 1);
			}
			return;
		}

		if (displayName.contains("Page suivante →")) {
			// Navigation vers la page suivante
			int currentPage = eggEditHolder.getCurrentPage();
			if (currentPage < eggEditHolder.getTotalPages() - 1) {
				reopenEggEditor(player, eggEditHolder.getOriginalEgg(), currentPage + 1);
			}
			return;
		}

		// Simplifier la détection en utilisant le displayName nettoyé
		String cleanedName = com.nisovin.shopkeepers.util.bukkit.TextUtils.stripColor(displayName);
		Log.debug("Cleaned display name: '" + cleanedName + "'");
		
		// Vérifier si c'est un bouton de type de shopkeeper
		// Les boutons de type de shopkeeper utilisent WHITE_CONCRETE ou LIME_CONCRETE
		if ((cleanedName.startsWith("Type: ") || 
			(cleanedName.startsWith("Sélection actuelle: ") && !cleanedName.contains("Type d'objet:"))) &&
			(clickedItem.getType() == Material.WHITE_CONCRETE || clickedItem.getType() == Material.LIME_CONCRETE)) {
			Log.debug("Detected shop type button click: " + displayName + " (Material: " + clickedItem.getType() + ")");
			handleShopTypeSelection(player, displayName, eggEditHolder);
			return;
		}

		// Vérifier si c'est un bouton de type d'objet
		// Les boutons de type d'objet utilisent LIGHT_BLUE_CONCRETE ou LIME_CONCRETE
		if ((cleanedName.startsWith("Objet: ") || 
			(cleanedName.startsWith("Sélection actuelle: ") && cleanedName.contains("Type d'objet:"))) &&
			(clickedItem.getType() == Material.LIGHT_BLUE_CONCRETE || clickedItem.getType() == Material.LIME_CONCRETE)) {
			Log.debug("Detected shop object type button click: " + displayName + " (Material: " + clickedItem.getType() + ")");
			handleShopObjectTypeSelection(player, displayName, eggEditHolder);
			return;
		}
		
		Log.debug("Unhandled click on item: " + clickedItem.getType() + " with display name: " + displayName);
	}

	private void handleShopTypeSelection(Player player, String displayName, ShopCreationEggEditInventoryHolder holder) {
		Log.debug("handleShopTypeSelection called with displayName: " + displayName);
		
		// Extraire le nom du type depuis le displayName
		String typeName = extractTypeName(displayName);
		Log.debug("Extracted type name: " + typeName);
		
		if (typeName == null) {
			Log.debug("Type name is null, returning");
			return;
		}

		// Trouver le type de shopkeeper correspondant
		for (ShopType<?> shopType : plugin.getShopTypeRegistry().getRegisteredTypes()) {
			String shopTypeDisplayName = shopType.getDisplayName();
			Log.debug("Checking shop type: '" + shopTypeDisplayName + "' against: '" + typeName + "'");
			
			// Comparaison exacte d'abord
			if (shopTypeDisplayName.equals(typeName)) {
				Log.debug("Found exact matching shop type, selecting: " + shopTypeDisplayName);
				selectShopType(player, shopType);
				reopenEggEditor(player, holder.getOriginalEgg(), holder.getCurrentPage());
				return;
			}
			
			// Comparaison sans couleurs et espaces
			String cleanShopTypeName = TextUtils.stripColor(shopTypeDisplayName).trim();
			String cleanTypeName = TextUtils.stripColor(typeName).trim();
			if (cleanShopTypeName.equalsIgnoreCase(cleanTypeName)) {
				Log.debug("Found matching shop type (case insensitive), selecting: " + shopTypeDisplayName);
				selectShopType(player, shopType);
				reopenEggEditor(player, holder.getOriginalEgg(), holder.getCurrentPage());
				return;
			}
		}
		Log.debug("No matching shop type found for: " + typeName);
	}

	private void handleShopObjectTypeSelection(Player player, String displayName, ShopCreationEggEditInventoryHolder holder) {
		// Extraire le nom du type depuis le displayName
		String typeName = extractTypeName(displayName);
		if (typeName == null) return;

		// Trouver le type d'objet correspondant
		for (ShopObjectType<?> shopObjectType : plugin.getShopObjectTypeRegistry().getRegisteredTypes()) {
			String shopObjectTypeDisplayName = shopObjectType.getDisplayName();
			Log.debug("Checking shop object type: '" + shopObjectTypeDisplayName + "' against: '" + typeName + "'");
			
			// Comparaison exacte d'abord
			if (shopObjectTypeDisplayName.equals(typeName)) {
				Log.debug("Found exact matching shop object type, selecting: " + shopObjectTypeDisplayName);
				selectShopObjectType(player, shopObjectType);
				reopenEggEditor(player, holder.getOriginalEgg(), holder.getCurrentPage());
				return;
			}
			
			// Comparaison sans couleurs et espaces
			String cleanShopObjectTypeName = TextUtils.stripColor(shopObjectTypeDisplayName).trim();
			String cleanTypeName = TextUtils.stripColor(typeName).trim();
			if (cleanShopObjectTypeName.equalsIgnoreCase(cleanTypeName)) {
				Log.debug("Found matching shop object type (case insensitive), selecting: " + shopObjectTypeDisplayName);
				selectShopObjectType(player, shopObjectType);
				reopenEggEditor(player, holder.getOriginalEgg(), holder.getCurrentPage());
				return;
			}
		}
		Log.debug("No matching shop object type found for: " + typeName);
	}

	private String extractTypeName(String displayName) {
		// Retirer les codes de couleur et les préfixes
		String cleaned = TextUtils.stripColor(displayName);
		Log.debug("Extracting type name from cleaned display name: '" + cleaned + "'");
		
		if (cleaned.contains("Type: ")) {
			String result = cleaned.substring(cleaned.indexOf("Type: ") + 6).trim();
			Log.debug("Extracted from 'Type: ' prefix: '" + result + "'");
			return result;
		}
		if (cleaned.contains("Objet: ")) {
			String result = cleaned.substring(cleaned.indexOf("Objet: ") + 7).trim();
			Log.debug("Extracted from 'Objet: ' prefix: '" + result + "'");
			return result;
		}
		if (cleaned.contains("Sélection actuelle: ")) {
			String result = cleaned.substring(cleaned.indexOf("Sélection actuelle: ") + 20).trim();
			Log.debug("Extracted from 'Sélection actuelle: ' prefix: '" + result + "'");
			return result;
		}
		
		Log.debug("No matching prefix found for extraction");
		return null;
	}

	// Méthode helper pour sélectionner un type de shopkeeper
	private void selectShopType(Player player, ShopType<?> shopType) {
		try {
			// Utiliser la réflection pour accéder directement au map des sélections
			java.lang.reflect.Field selectionsField = plugin.getShopTypeRegistry().getClass().getSuperclass().getDeclaredField("selections");
			selectionsField.setAccessible(true);
			@SuppressWarnings("unchecked")
			java.util.Map<String, ShopType<?>> selections = (java.util.Map<String, ShopType<?>>) selectionsField.get(plugin.getShopTypeRegistry());
			
			// Sélectionner directement le type
			selections.put(player.getName(), shopType);
			
			// Appeler la méthode onSelect si elle existe
			try {
				java.lang.reflect.Method onSelectMethod = plugin.getShopTypeRegistry().getClass().getSuperclass().getDeclaredMethod("onSelect", Object.class, org.bukkit.entity.Player.class);
				onSelectMethod.setAccessible(true);
				onSelectMethod.invoke(plugin.getShopTypeRegistry(), shopType, player);
			} catch (Exception e) {
				// Méthode onSelect non trouvée, continuer sans
			}
			
			Log.debug("Successfully selected shop type: " + shopType.getDisplayName() + " for player " + player.getName());
			
			// Envoyer un message de confirmation au joueur
			com.nisovin.shopkeepers.util.bukkit.TextUtils.sendMessage(player, 
				"&aType de shopkeeper sélectionné: &6" + shopType.getDisplayName());
		} catch (Exception e) {
			Log.warning("Failed to select shop type via reflection, falling back to cycling method: " + e.getMessage());
			// Fallback: méthode de cyclage
			fallbackSelectShopType(player, shopType);
		}
	}
	
	// Méthode de fallback pour la sélection de type de shopkeeper
	private void fallbackSelectShopType(Player player, ShopType<?> shopType) {
		ShopType<?> current = plugin.getShopTypeRegistry().getSelection(player);
		if (shopType.equals(current)) return;
		
		int maxAttempts = plugin.getShopTypeRegistry().getRegisteredTypes().size();
		for (int i = 0; i < maxAttempts; i++) {
			current = plugin.getShopTypeRegistry().selectNext(player);
			if (shopType.equals(current)) {
				break;
			}
		}
	}

	// Méthode helper pour sélectionner un type d'objet
	private void selectShopObjectType(Player player, ShopObjectType<?> shopObjectType) {
		try {
			// Utiliser la réflection pour accéder directement au map des sélections
			java.lang.reflect.Field selectionsField = plugin.getShopObjectTypeRegistry().getClass().getSuperclass().getDeclaredField("selections");
			selectionsField.setAccessible(true);
			@SuppressWarnings("unchecked")
			java.util.Map<String, ShopObjectType<?>> selections = (java.util.Map<String, ShopObjectType<?>>) selectionsField.get(plugin.getShopObjectTypeRegistry());
			
			// Sélectionner directement le type
			selections.put(player.getName(), shopObjectType);
			
			// Appeler la méthode onSelect si elle existe
			try {
				java.lang.reflect.Method onSelectMethod = plugin.getShopObjectTypeRegistry().getClass().getSuperclass().getDeclaredMethod("onSelect", Object.class, org.bukkit.entity.Player.class);
				onSelectMethod.setAccessible(true);
				onSelectMethod.invoke(plugin.getShopObjectTypeRegistry(), shopObjectType, player);
			} catch (Exception e) {
				// Méthode onSelect non trouvée, continuer sans
			}
			
			Log.debug("Successfully selected shop object type: " + shopObjectType.getDisplayName() + " for player " + player.getName());
			
			// Envoyer un message de confirmation au joueur
			com.nisovin.shopkeepers.util.bukkit.TextUtils.sendMessage(player, 
				"&aType d'objet sélectionné: &6" + shopObjectType.getDisplayName());
		} catch (Exception e) {
			Log.warning("Failed to select shop object type via reflection, falling back to cycling method: " + e.getMessage());
			// Fallback: méthode de cyclage
			fallbackSelectShopObjectType(player, shopObjectType);
		}
	}
	
	// Méthode de fallback pour la sélection de type d'objet
	private void fallbackSelectShopObjectType(Player player, ShopObjectType<?> shopObjectType) {
		ShopObjectType<?> current = plugin.getShopObjectTypeRegistry().getSelection(player);
		if (shopObjectType.equals(current)) return;
		
		int maxAttempts = plugin.getShopObjectTypeRegistry().getRegisteredTypes().size();
		for (int i = 0; i < maxAttempts; i++) {
			current = plugin.getShopObjectTypeRegistry().selectNext(player);
			if (shopObjectType.equals(current)) {
				break;
			}
		}
	}

	// Méthode pour rouvrir l'éditeur d'œuf à une page spécifique
	private void reopenEggEditor(Player player, ItemStack originalEgg, int page) {
		// Fermer l'inventaire actuel
		player.closeInventory();
		
		// Programmer la réouverture pour le prochain tick
		plugin.getServer().getScheduler().runTask(plugin, () -> {
			// Créer une nouvelle instance de CommandEggEdit et appeler la méthode publique
			com.nisovin.shopkeepers.commands.shopkeepers.CommandEggEdit eggEditCommand = 
				new com.nisovin.shopkeepers.commands.shopkeepers.CommandEggEdit();
			
			eggEditCommand.openShopCreationEggEditor(player, originalEgg, page);
		});
	}
}