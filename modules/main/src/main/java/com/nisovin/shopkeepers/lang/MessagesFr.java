package com.nisovin.shopkeepers.lang;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.nisovin.shopkeepers.config.lib.Config;
import com.nisovin.shopkeepers.config.lib.ConfigData;
import com.nisovin.shopkeepers.config.lib.ConfigLoadException;
import com.nisovin.shopkeepers.config.lib.annotation.WithDefaultValueType;
import com.nisovin.shopkeepers.config.lib.annotation.WithValueTypeProvider;
import com.nisovin.shopkeepers.config.lib.value.ValueLoadException;
import com.nisovin.shopkeepers.config.lib.value.types.ColoredStringListValue;
import com.nisovin.shopkeepers.config.lib.value.types.ColoredStringValue;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * VERSION FRANÇAISE DES MESSAGES - SHOPKEEPERS
 * Traduction française avec codes couleur selon la logique :
 * - Texte normal : blanc (§f)
 * - Mots un peu importants : jaune (§6 ou §e)
 * - Mots très importants : orange (§c)
 */
@WithDefaultValueType(fieldType = String.class, valueType = ColoredStringValue.class)
@WithValueTypeProvider(ColoredStringListValue.Provider.class)
public class MessagesFr extends Config {

	// Format date/heure
	public static String dateTimeFormat = c("yyyy-MM-dd HH:mm:ss");

	// Types de boutiques
	public static String shopTypeAdminRegular = c("§6Boutique §cAdmin");
	public static String shopTypeSelling = c("§6Boutique de §evente");
	public static String shopTypeBuying = c("§6Boutique d'§eachat");
	public static String shopTypeTrading = c("§6Boutique d'§eéchange");
	public static String shopTypeBook = c("§6Boutique de §elivres");

	// Description des types de boutiques
	public static String shopTypeDescAdminRegular = c("§fa un §cstock §filimité");
	public static String shopTypeDescSelling = c("§fvend des §6objets §faux joueurs");
	public static String shopTypeDescBuying = c("§fachète des §6objets §faux joueurs");
	public static String shopTypeDescTrading = c("§féchange des §6objets §favec les joueurs");
	public static String shopTypeDescBook = c("§fvend des §6copies §fde livres");

	// Types d'objets de boutique
	public static String shopObjectTypeLiving = c("§6{type}");
	public static String shopObjectTypeSign = c("§6panneau");
	public static String shopObjectTypeHangingSign = c("§6panneau suspendu");
	public static String shopObjectTypeNpc = c("§6pnj");

	// Messages de sélection
	public static Text selectedShopType = Text.parse("§aType de boutique sélectionné : §6{type} §7({description})");
	public static Text selectedShopObjectType = Text.parse("§aType d'objet sélectionné : §6{type}");

	// Message d'aide pour la création
	public static Text creationItemSelected = Text.parse("§aCréation de boutique :\n"
			+ "§e  Ne visez aucun bloc. Ensuite :\n"
			+ "§e  Clic gauche/droit pour sélectionner le type de boutique.\n"
			+ "§e  Accroupi + clic gauche/droit pour sélectionner le type d'objet.\n"
			+ "§e  Clic droit sur un conteneur pour le sélectionner.\n"
			+ "§e  Puis clic droit sur un bloc pour placer le marchand."
	);

	// États
	public static String stateEnabled = c("§2Activé");
	public static String stateDisabled = c("§4Désactivé");

	// Titre de l'éditeur
	public static String editorTitle = c("§6Éditeur de Marchand");

	// Boutons de navigation
	public static String buttonPreviousPage = c("§6<- Page précédente ({prev_page} sur {max_page})");
	public static List<String> buttonPreviousPageLore = c(Arrays.asList());
	public static String buttonNextPage = c("§6Page suivante ({next_page} sur {max_page}) ->");
	public static List<String> buttonNextPageLore = c(Arrays.asList());
	public static String buttonCurrentPage = c("§6Page {page} sur {max_page}");
	public static List<String> buttonCurrentPageLore = c(Arrays.asList());

	// Boutons de l'éditeur
	public static String buttonName = c("§aDéfinir le nom de la boutique");
	public static List<String> buttonNameLore = c(Arrays.asList(
			"§fVous permet de §6renommer",
			"§fvotre marchand"
	));
	public static String buttonEquipment = c("§aÉquipement");
	public static List<String> buttonEquipmentLore = c(Arrays.asList(
			"§fAssigner de l'équipement.",
			"§fNote : Toutes les créatures ne supportent",
			"§fpas les mêmes emplacements d'équipement."
	));
	public static String buttonMove = c("§aDéplacer le marchand");
	public static List<String> buttonMoveLore = c(Arrays.asList(
			"§fVous permet de §6déplacer",
			"§fvotre marchand"
	));
	public static String buttonContainer = c("§aVoir l'inventaire de la boutique");
	public static List<String> buttonContainerLore = c(Arrays.asList(
			"§fVous permet de voir l'§6inventaire",
			"§fque votre marchand utilise"
	));
	public static String buttonTradeNotifications = c("§aNotifications d'Échange");
	public static List<String> buttonTradeNotificationsLore = c(Arrays.asList(
			"§fActive/désactive les §6notifications",
			"§fd'échange pour ce marchand.",
			"§fActuellement : §6{state}"
	));
	public static String buttonDelete = c("§4Supprimer");
	public static List<String> buttonDeleteLore = c(Arrays.asList(
			"§fFerme et §csupprime",
			"§fce marchand"
	));

	// Messages d'erreur courants
	public static Text mustTargetShop = Text.parse("§7Vous devez viser un marchand.");
	public static Text mustTargetAdminShop = Text.parse("§7Vous devez viser un marchand administrateur.");
	public static Text mustTargetPlayerShop = Text.parse("§7Vous devez viser un marchand de joueur.");
	public static Text targetEntityIsNoShop = Text.parse("§7L'entité visée n'est pas un marchand.");
	public static Text notOwner = Text.parse("§7Vous n'êtes pas le propriétaire de ce marchand.");

	// Messages de création de boutique
	public static Text mustTargetBlock = Text.parse("§7Vous devez regarder un bloc pour placer le marchand.");
	public static Text containerSelected = Text.parse("§aConteneur sélectionné ! Clic droit sur un bloc pour placer votre marchand.");
	public static Text unsupportedContainer = Text.parse("§7Ce type de conteneur ne peut pas être utilisé pour les boutiques.");
	public static Text mustSelectContainer = Text.parse("§7Vous devez faire un clic droit sur un conteneur avant de placer votre marchand.");
	public static Text invalidContainer = Text.parse("§7Le bloc sélectionné n'est pas un conteneur valide !");
	public static Text containerTooFarAway = Text.parse("§7Le conteneur du marchand est trop éloigné !");
	public static Text containerAlreadyInUse = Text.parse("§7Un autre marchand utilise déjà le conteneur sélectionné !");
	public static Text tooManyShops = Text.parse("§7Vous avez déjà atteint la limite du nombre de boutiques que vous pouvez posséder !");

	// Messages de nommage
	public static Text typeNewName = Text.parse("§aVeuillez entrer le nouveau nom de la boutique dans le chat.\n"
			+ "  §aEntrez un tiret (-) pour supprimer le nom actuel.");
	public static Text nameSet = Text.parse("§aLe nom de la boutique a été défini sur '§e{name}§a' !");
	public static Text nameHasNotChanged = Text.parse("§aLe nom de la boutique n'a pas changé.");
	public static Text nameInvalid = Text.parse("§cNom de boutique invalide : '§e{name}§c'");

	// Messages de déplacement
	public static Text clickNewShopLocation = Text.parse("§aVeuillez faire un clic droit sur le nouvel emplacement de la boutique.\n  §aClic gauche pour annuler.");
	public static Text shopkeeperMoved = Text.parse("§aLe marchand a été déplacé !");
	public static Text shopkeeperMoveAborted = Text.parse("§7Déplacement du marchand annulé.");

	// Messages d'échange
	public static Text missingTradePerm = Text.parse("§7Vous n'avez pas la permission d'échanger avec cette boutique.");
	public static Text cannotTradeNoOffers = Text.parse("§7Cette boutique n'a actuellement aucune offre. Vérifiez plus tard !");
	public static Text cannotTradeWithOwnShop = Text.parse("§7Vous ne pouvez pas échanger avec votre propre boutique.");
	public static Text cannotTradeWhileOwnerOnline = Text.parse("§7Vous ne pouvez pas échanger tant que le propriétaire de cette boutique ('§e{owner}§7') est en ligne.");
	public static Text cannotTradeWithShopMissingContainer = Text.parse("§7Vous ne pouvez pas échanger avec cette boutique, car son conteneur est manquant.");

	// Messages de notifications d'échange
	public static Text tradeNotificationOneItem = Text.parse("§7Échange : §e{player}§7 [§6{item1Amount}x §a{item1}§7] → [§6{resultItemAmount}x §a{resultItem}§7] {shop}{trade_count}");
	public static Text tradeNotificationTwoItems = Text.parse("§7Échange : §e{player}§7 [§6{item1Amount}x §a{item1}§7] [§6{item2Amount}x §a{item2}§7] → [§6{resultItemAmount}x §a{resultItem}§7] {shop}{trade_count}");
	public static Text tradeNotificationAdminShop = Text.parse("§eBoutique Admin");
	public static Text tradeNotificationTradeCount = Text.parse("§7 (§6{count}x§7)");

	// Messages de suppression
	public static Text shopRemoved = Text.parse("§aLe marchand a été supprimé.");
	public static Text shopAlreadyRemoved = Text.parse("§7Le marchand a déjà été supprimé.");
	public static Text shopNoLongerExists = Text.parse("§7Le marchand n'existe plus.");

	// Messages de commandes
	public static Text noPermission = Text.parse("§cVous n'avez pas la permission de faire cela.");
	public static Text commandUnknown = Text.parse("§cCommande inconnue '§e{command}§c' !");
	public static Text commandArgumentMissing = Text.parse("§cArgument manquant '§e{argumentFormat}§c'.");
	public static Text commandArgumentInvalid = Text.parse("§cArgument invalide '§e{argument}§c'.");

	// Messages d'aide de commandes
	public static Text commandHelpTitle = Text.parse("§9***** §8[§6Shopkeepers v{version}§8] §9*****");
	public static Text commandDescriptionHelp = Text.parse("§fAffiche cette page d'§6aide§f.");
	public static Text commandDescriptionReload = Text.parse("§fRecharge ce §6plugin§f.");
	public static Text commandDescriptionList = Text.parse("§fListe toutes les boutiques d'un joueur spécifique, ou toutes les boutiques §6admin§f.");
	public static Text commandDescriptionRemove = Text.parse("§fSupprime une boutique §6spécifique§f.");
	public static Text commandDescriptionGive = Text.parse("§fDonne des objets de §6création§f de boutique au joueur spécifié.");

	// Messages de téléportation
	public static Text teleportSuccess = Text.parse("§aLe joueur '§e{player}§a' a été téléporté vers le marchand '§e{shop}§a' !");
	public static Text teleportFailed = Text.parse("§7La téléportation a échoué !");

	// Messages de confirmation
	public static Text confirmationRequired = Text.parse("§7Veuillez confirmer cette action en tapant §6/shopkeepers confirm");
	public static Text confirmationExpired = Text.parse("§cConfirmation expirée.");
	public static Text nothingToConfirm = Text.parse("§cIl n'y a rien à confirmer actuellement.");

	// Messages d'embauche
	public static Text hired = Text.parse("§aVous avez embauché ce marchand !");
	public static Text cannotHire = Text.parse("§7Vous ne pouvez pas vous permettre d'embaucher ce marchand.");
	public static Text missingHirePerm = Text.parse("§7Vous n'avez pas la permission d'embaucher des marchands.");

	// Titre de trading par défaut
	public static String tradingTitlePrefix = c("§2");
	public static String tradingTitleDefault = c("§6Marchand");

	// Messages divers
	public static Text unknownBookAuthor = Text.parse("§7Inconnu");
	public static Text noShopsFound = Text.parse("§7Aucune boutique trouvée.");

	// Messages d'objets vides pour les éditeurs (boutiques de vente)
	public static String sellingShopEmptyTradeResultItem = c("§dObjet à Vendre");
	public static List<String> sellingShopEmptyTradeResultItemLore = c(Arrays.asList(
			"§fL'objet que vous voulez §6vendre§f.",
			"§fAjoutez des objets au conteneur de la boutique.",
			"§fClic gauche/droit pour ajuster la quantité."
	));
	public static String sellingShopEmptyTradeItem1 = c("§dObjet à Acheter");
	public static List<String> sellingShopEmptyTradeItem1Lore = c(Arrays.asList(
			"§fL'objet que vous voulez §6acheter§f.",
			"§fClic gauche/droit pour ajuster la quantité."
	));

	// Messages d'objets vides pour les éditeurs (boutiques d'achat)
	public static String buyingShopEmptyTradeResultItem = c("§dObjet à Vendre");
	public static List<String> buyingShopEmptyTradeResultItemLore = c(Arrays.asList(
			"§fL'objet que vous voulez §6vendre§f.",
			"§fClic gauche/droit pour ajuster la quantité."
	));
	public static String buyingShopEmptyTradeItem1 = c("§dObjet à Acheter");
	public static List<String> buyingShopEmptyTradeItem1Lore = c(Arrays.asList(
			"§fL'objet que vous voulez §6acheter§f.",
			"§fAjoutez des objets au conteneur de la boutique.",
			"§fClic gauche/droit pour ajuster la quantité."
	));

	// Messages d'objets vides pour les éditeurs (boutiques d'échange)
	public static String tradingShopEmptyTradeResultItem = c("§dObjet à Recevoir");
	public static List<String> tradingShopEmptyTradeResultItemLore = c(Arrays.asList(
			"§fL'objet que les joueurs §6recevront§f.",
			"§fClic gauche/droit pour ajuster la quantité."
	));
	public static String tradingShopEmptyTradeItem1 = c("§dPremier Objet Requis");
	public static List<String> tradingShopEmptyTradeItem1Lore = c(Arrays.asList(
			"§fLe premier objet que les joueurs",
			"§fdoivent §6donner§f.",
			"§fClic gauche/droit pour ajuster la quantité."
	));
	public static String tradingShopEmptyTradeItem2 = c("§dDeuxième Objet Requis");
	public static List<String> tradingShopEmptyTradeItem2Lore = c(Arrays.asList(
			"§fLe deuxième objet que les joueurs",
			"§fdoivent §6donner§f.",
			"§fClic gauche/droit pour ajuster la quantité."
	));

	// Messages d'objets vides pour les éditeurs (boutiques de livres)
	public static String bookShopEmptyTradeResultItem = c("§dLivre à Vendre");
	public static List<String> bookShopEmptyTradeResultItemLore = c(Arrays.asList(
			"§fLe livre que vous voulez §6vendre§f.",
			"§fClic gauche/droit pour ajuster la quantité."
	));
	public static String bookShopEmptyTradeItem1 = c("§dObjet Requis");
	public static List<String> bookShopEmptyTradeItem1Lore = c(Arrays.asList(
			"§fL'objet que les joueurs doivent §6donner§f",
			"§fen échange du livre.",
			"§fClic gauche/droit pour ajuster la quantité."
	));

	// Boutons pour les variantes d'animaux
	public static String buttonBaby = c("§aToggle bébé");
	public static List<String> buttonBabyLore = c(Arrays.asList(
			"Définit si la créature",
			"est un bébé ou un adulte"
	));
	public static String buttonSitting = c("§aToggle assis");
	public static List<String> buttonSittingLore = c(Arrays.asList(
			"Définit si l'animal",
			"est assis ou debout"
	));
	public static String buttonSize = c("§aToggle taille");
	public static List<String> buttonSizeLore = c(Arrays.asList(
			"Fait basculer la taille",
			"entre normale et petite"
	));
	public static String buttonCharged = c("§aToggle chargé");
	public static List<String> buttonChargedLore = c(Arrays.asList(
			"Fait basculer l'état",
			"chargé du creeper"
	));
	public static String buttonWither = c("§aToggle wither");
	public static List<String> buttonWitherLore = c(Arrays.asList(
			"Fait basculer l'effet",
			"wither du squelette"
	));
	public static String buttonTamed = c("§aToggle apprivoisé");
	public static List<String> buttonTamedLore = c(Arrays.asList(
			"Fait basculer l'état",
			"apprivoisé de l'animal"
	));
	public static String buttonCollarColor = c("§aChoisir couleur du collier");
	public static List<String> buttonCollarColorLore = c(Arrays.asList(
			"Change la couleur",
			"du collier de l'animal"
	));
	public static String buttonAngry = c("§aToggle en colère");
	public static List<String> buttonAngryLore = c(Arrays.asList(
			"Fait basculer l'état",
			"de colère de la créature"
	));
	public static String buttonElderGuardian = c("§aToggle gardien ancien");
	public static List<String> buttonElderGuardianLore = c(Arrays.asList(
			"Fait basculer entre gardien",
			"normal et ancien"
	));
	public static String buttonCatVariant = c("§aChoisir variante de chat");
	public static List<String> buttonCatVariantLore = c(Arrays.asList(
			"Change l'apparence",
			"du chat"
	));
	public static String buttonHorseColor = c("§aChoisir couleur du cheval");
	public static List<String> buttonHorseColorLore = c(Arrays.asList(
			"Change la couleur",
			"du cheval"
	));
	public static String buttonHorseStyle = c("§aChoisir style du cheval");
	public static List<String> buttonHorseStyleLore = c(Arrays.asList(
			"Change le style",
			"du cheval"
	));
	public static String buttonLlamaVariant = c("§aChoisir variante de lama");
	public static List<String> buttonLlamaVariantLore = c(Arrays.asList(
			"Change l'apparence",
			"du lama"
	));
	public static String buttonCarpetColor = c("§aChoisir couleur du tapis");
	public static List<String> buttonCarpetColorLore = c(Arrays.asList(
			"Change la couleur",
			"du tapis du lama"
	));
	public static String buttonRabbitVariant = c("§aChoisir variante de lapin");
	public static List<String> buttonRabbitVariantLore = c(Arrays.asList(
			"Change l'apparence",
			"du lapin"
	));
	public static String buttonMooshroomVariant = c("§aChoisir variante de champimeuh");
	public static List<String> buttonMooshroomVariantLore = c(Arrays.asList(
			"Change l'apparence",
			"du champimeuh"
	));
	public static String buttonPandaVariant = c("§aChoisir variante de panda");
	public static List<String> buttonPandaVariantLore = c(Arrays.asList(
			"Change l'apparence du panda"
	));
	public static String buttonParrotVariant = c("§aChoisir variante de perroquet");
	public static List<String> buttonParrotVariantLore = c(Arrays.asList(
			"Change l'apparence",
			"du perroquet"
	));
	public static String buttonPigVariant = c("§aChoisir variante de cochon");
	public static List<String> buttonPigVariantLore = c(Arrays.asList(
			"Change l'apparence du cochon"
	));
	public static String buttonChickenVariant = c("§aChoisir variante de poule");
	public static List<String> buttonChickenVariantLore = c(Arrays.asList(
			"Change l'apparence de la poule"
	));
	public static String buttonSheepColor = c("§aChoisir couleur du mouton");
	public static List<String> buttonSheepColorLore = c(Arrays.asList(
			"Change la couleur",
			"de la laine du mouton"
	));
	public static String buttonSheepSheared = c("§aToggle mouton tondu");
	public static List<String> buttonSheepShearedLore = c(Arrays.asList(
			"Fait basculer l'état",
			"tondu du mouton"
	));
	public static String buttonVillagerProfession = c("§aChoisir profession du villageois");
	public static List<String> buttonVillagerProfessionLore = c(Arrays.asList(
			"Change la profession",
			"du villageois"
	));
	public static String buttonVillagerVariant = c("§aChoisir variante du villageois");
	public static List<String> buttonVillagerVariantLore = c(Arrays.asList(
			"Change l'apparence",
			"du villageois"
	));
	public static String buttonVillagerLevel = c("§aChoisir couleur du badge");
	public static List<String> buttonVillagerLevelLore = c(Arrays.asList(
			"Change la couleur du badge",
			"du villageois"
	));
	public static String buttonZombieVillagerProfession = c("§aChoisir profession du villageois");
	public static List<String> buttonZombieVillagerProfessionLore = c(Arrays.asList(
			"Change la profession",
			"du villageois zombie"
	));
	public static String buttonSlimeSize = c("§aChoisir taille du slime");
	public static List<String> buttonSlimeSizeLore = c(Arrays.asList(
			"Change la taille du slime.",
			"Taille actuelle : §e{size}"
	));
	public static String buttonMagmaCubeSize = c("§aChoisir taille du cube de magma");
	public static List<String> buttonMagmaCubeSizeLore = c(Arrays.asList(
			"Change la taille du cube de magma.",
			"Taille actuelle : §e{size}"
	));
	public static String buttonSnowmanPumpkinHead = c("§aToggle tête de citrouille");
	public static List<String> buttonSnowmanPumpkinHeadLore = c(Arrays.asList(
			"Fait basculer la tête",
			"de citrouille du bonhomme de neige"
	));
	public static String buttonShulkerColor = c("§aChoisir couleur du shulker");
	public static List<String> buttonShulkerColorLore = c(Arrays.asList(
			"Change la couleur",
			"du shulker"
	));
	public static String buttonAxolotlVariant = c("§aChoisir variante d'axolotl");
	public static List<String> buttonAxolotlVariantLore = c(Arrays.asList(
			"Change l'apparence de l'axolotl"
	));
	public static String buttonGlowSquidDark = c("§aToggle lueur");
	public static List<String> buttonGlowSquidDarkLore = c(Arrays.asList(
			"Active/désactive la lueur",
			"du calmar lumineux"
	));
	public static String buttonGoatScreaming = c("§aToggle chèvre criarde");
	public static List<String> buttonGoatScreamingLore = c(Arrays.asList(
			"Bascule entre une chèvre",
			"normale et criarde"
	));
	public static String buttonGoatLeftHorn = c("§aToggle corne gauche");
	public static List<String> buttonGoatLeftHornLore = c(Arrays.asList(
			"Fait basculer la corne",
			"gauche de la chèvre"
	));
	public static String buttonGoatRightHorn = c("§aToggle corne droite");
	public static List<String> buttonGoatRightHornLore = c(Arrays.asList(
			"Fait basculer la corne",
			"droite de la chèvre"
	));
	public static String buttonTropicalFishPattern = c("§aChoisir variante");
	public static List<String> buttonTropicalFishPatternLore = c(Arrays.asList(
			"Change la forme et le motif",
			"du poisson tropical.",
			"Actuellement : §e{pattern}"
	));
	public static String buttonTropicalFishBodyColor = c("§aChoisir couleur du corps");
	public static List<String> buttonTropicalFishBodyColorLore = c(Arrays.asList(
			"Change la couleur du corps",
			"du poisson tropical"
	));
	public static String buttonTropicalFishPatternColor = c("§aChoisir couleur du motif");
	public static List<String> buttonTropicalFishPatternColorLore = c(Arrays.asList(
			"Change la couleur du motif",
			"du poisson tropical"
	));
	public static String buttonPufferFishPuffState = c("§aChoisir état de gonflement");
	public static List<String> buttonPufferFishPuffStateLore = c(Arrays.asList(
			"Change l'état de gonflement",
			"du poisson-globe.",
			"Actuellement : §e{puffState}"
	));
	public static String buttonSalmonVariant = c("§aChoisir variante");
	public static List<String> buttonSalmonVariantLore = c(Arrays.asList(
			"Change la taille",
			"du saumon."
	));
	public static String buttonFrogVariant = c("§aChoisir variante de grenouille");
	public static List<String> buttonFrogVariantLore = c(Arrays.asList(
			"Change l'apparence de la grenouille"
	));
	public static String buttonWolfVariant = c("§aChoisir variante de loup");
	public static List<String> buttonWolfVariantLore = c(Arrays.asList(
			"Change l'apparence du loup"
	));
	public static String buttonSaddle = c("§aToggle selle");
	public static List<String> buttonSaddleLore = c(Arrays.asList(
			"Fait basculer la selle de la créature"
	));

	// Éditeur d'équipement
	public static String equipmentEditorTitle = c("Éditeur d'Équipement");
	public static List<String> equipmentSlotLore = c(Arrays.asList(
			"Placez un objet pour l'équiper.",
			"Clic droit pour vider",
			"l'emplacement d'équipement."
	));

	public static String equipmentSlotMainhand = c("§aMain principale");
	public static String equipmentSlotOffhand = c("§aMain secondaire");
	public static String equipmentSlotFeet = c("§aPieds");
	public static String equipmentSlotLegs = c("§aJambes");
	public static String equipmentSlotChest = c("§aPoitrine");
	public static String equipmentSlotHead = c("§aTête");
	public static String equipmentSlotBody = c("§aCorps");
	public static String equipmentSlotSaddle = c("§aSelle");

	// Panneaux de boutique admin
	public static String adminSignShopLine1 = c("§2[BOUTIQUE]");
	public static String adminSignShopLine2 = c("§7{shopName}");
	public static String adminSignShopLine3 = c("");
	public static String adminSignShopLine4 = c("§eClic droit !");

	// Panneaux de boutique joueur
	public static String playerSignShopLine1 = c("§2[BOUTIQUE]");
	public static String playerSignShopLine2 = c("§7{shopName}");
	public static String playerSignShopLine3 = c("§7{owner}");
	public static String playerSignShopLine4 = c("§eClic droit !");

	// Embauche
	public static String forHireTitle = c("À Embaucher");
	public static String buttonHire = c("§aEmbaucher");
	public static List<String> buttonHireLore = c(Arrays.asList(
			"Acheter ce marchand"
	));

	// Messages d'erreur supplémentaires
	public static Text missingSpawnLocation = Text.parse("§7Vous devez spécifier un emplacement d'apparition pour ce type de boutique.");
	public static Text spawnBlockNotEmpty = Text.parse("§7L'emplacement d'apparition doit être vide.");
	public static Text invalidSpawnBlockFace = Text.parse("§7Le marchand ne peut pas être placé de ce côté du bloc.");
	public static Text mobCannotSpawnOnPeacefulDifficulty = Text.parse("§7Le type de créature sélectionné ne peut pas apparaître ici en difficulté paisible.");
	public static Text restrictedArea = Text.parse("§7Vous ne pouvez pas placer un marchand dans cette zone.");
	public static Text locationAlreadyInUse = Text.parse("§7Cet emplacement est déjà utilisé par un autre marchand.");

	// Messages de conteneur supplémentaires
	public static Text mustTargetContainer = Text.parse("§7Vous devez regarder un conteneur pour placer ce type de boutique.");
	public static Text containerNotPlaced = Text.parse("§7Vous devez sélectionner un conteneur que vous avez récemment placé !");
	public static Text noContainerAccess = Text.parse("§7Vous ne pouvez pas accéder au conteneur sélectionné !");
	public static Text noPlayerShopsViaCommand = Text.parse("§7Les boutiques de joueurs ne peuvent être créées que via l'objet de création de boutique !");

	// Messages de nommage supplémentaires
	public static String nameplatePrefix = c("§2");

	// Messages de désactivation
	public static Text shopTypeDisabled = Text.parse("§7Le type de boutique '§6{type}§7' est désactivé.");
	public static Text shopObjectTypeDisabled = Text.parse("§7Le type d'objet de boutique '§6{type}§7' est désactivé.");

	// Messages de récompenses d'embauche
	public static Text hireOtherShopType = Text.parse("§7Vous ne pouvez embaucher que des boutiques à vendre !");
	public static Text hireOtherShopObject = Text.parse("§7Vous ne pouvez embaucher que des citoyens !");
	public static Text hireShopAlreadyHired = Text.parse("§7Cette boutique a déjà été embauchée !");

	// Messages d'inventaire plein
	public static Text cannotTradeInsufficientStorageSpace = Text.parse("§7Votre inventaire est plein. Libérez de l'espace et réessayez !");
	public static Text cannotTradeInsufficientCurrency = Text.parse("§7Vous n'avez pas assez d'argent.");
	public static Text cannotTradeInsufficientStock = Text.parse("§7Stock insuffisant.");
	public static Text cannotTradeInsufficientItems = Text.parse("§7Vous n'avez pas les objets requis.");

	// Messages de commandes supplémentaires
	public static Text commandOnlyByPlayers = Text.parse("§cCette commande ne peut être utilisée que par des joueurs.");
	public static Text commandArgument = Text.parse("§e{argumentFormat}");
	public static Text commandArgumentOptional = Text.parse("§7[{argumentFormat}]");
	public static Text commandPlayerNameRequired = Text.parse("§cNom du joueur requis.");
	public static Text commandPlayerNotFound = Text.parse("§cJoueur '§e{player}§c' non trouvé.");
	public static Text commandPlayerOnlyShops = Text.parse("§cPas de boutiques trouvées pour le joueur '§e{player}§c'.");
	public static Text commandShopTypeInvalid = Text.parse("§cType de boutique invalide '§e{type}§c'.");
	public static Text commandShopObjectTypeInvalid = Text.parse("§cType d'objet de boutique invalide '§e{type}§c'.");
	public static Text commandShopkeeperArgumentInvalid = Text.parse("§cArgument de marchand invalide '§e{argument}§c'.");
	public static Text commandShopkeeperNotFound = Text.parse("§cMarchand non trouvé : {argument}");
	public static Text commandRemoveShopConfirmation = Text.parse("§eÊtes-vous sûr de vouloir supprimer le marchand '§6{shopSessionId}§e' de '§6{owner}§e' ?");

	// Messages de liste de boutiques
	public static Text listAdminShopsHeader = Text.parse("§9========== Boutiques Admin ==========");
	public static Text listAllShopsHeader = Text.parse("§9========== Toutes les Boutiques ==========");
	public static Text listPlayerShopsHeader = Text.parse("§9========== Boutiques de {player} ==========");
	public static Text listShopsEntry = Text.parse("  §e{shopSessionId}) §7{shopName} §r{shopType} §7à §f({location})");

	// Messages de création d'objets
	public static Text shopCreationItemsReceived = Text.parse("§aVous avez reçu §e{amount}§a objets de création de boutique !");
	public static Text shopkeeperCreated = Text.parse("§aMarchand créé : {type}");

	// Messages de debug et erreurs diverses
	public static Text debugModeEnabled = Text.parse("§aMode debug activé !");
	public static Text debugModeDisabled = Text.parse("§7Mode debug désactivé !");

	// Messages de sauvegardes
	public static Text savedPlayerShopsStats = Text.parse("§aSauvegardé {shopsCount} boutiques de joueurs (§e{additions}§a ajouts, §e{updates}§a mises à jour, §e{deletions}§a suppressions, {duration}ms)");
	public static Text savedAdminShopsStats = Text.parse("§aSauvegardé {shopsCount} boutiques admin (§e{additions}§a ajouts, §e{updates}§a mises à jour, §e{deletions}§a suppressions, {duration}ms)");

	// Messages de taxe
	public static Text taxRate = Text.parse("§7Taux de taxe : §e{rate}%");
	public static Text taxMissing = Text.parse("§cImpossible de payer la taxe de {costs} !");

	// Messages de monde
	public static Text worldNotLoaded = Text.parse("§7Le monde '§e{worldName}§7' n'est pas chargé.");
	public static Text worldDisabled = Text.parse("§7Les boutiques sont désactivées dans le monde '§e{worldName}§7'.");

	// Messages divers complets
	public static Text enabledWorlds = Text.parse("§7Mondes activés : §a{worlds}");
	public static Text disabledWorlds = Text.parse("§7Mondes désactivés : §c{worlds}");
	public static Text allWorlds = Text.parse("§7tous");

	// Messages de validation
	public static Text cannotTradeWithShopMissingChest = Text.parse("§7Vous ne pouvez pas échanger avec cette boutique, car son coffre est manquant.");
	public static Text tooManyTradesFromPlayer = Text.parse("§7Trop d'échanges de ce joueur. Réessayez plus tard.");
	public static Text cannotTradeInvalidTrade = Text.parse("§7Cette offre d'échange n'est plus valide. Réessayez !");
	public static Text cannotTradeItemsNotStrictlyMatching = Text.parse("§7Les objets ne correspondent pas parfaitement !");
	public static Text cannotTradeShopkeeperNoLongerExists = Text.parse("§7Ce marchand n'existe plus !");

	// Messages de confirmation supplémentaires
	public static Text confirmationUI = Text.parse("§7Veuillez confirmer cette action en tapant §6/shopkeepers confirm");
	public static Text confirmationTextEditing = Text.parse("§7Tapez votre message. Tapez §6'cancel'§7 pour annuler.");

	// Messages de session
	public static Text sessionExpired = Text.parse("§cVotre session a expiré. Veuillez recommencer.");
	public static Text uiSessionExpired = Text.parse("§cL'interface utilisateur a expiré. Veuillez rouvrir l'interface.");

	// Méthodes utilitaires (maintenues identiques à l'original)
	private static final MessagesFr INSTANCE = new MessagesFr();

	public static void loadLanguageFile() {
		// Implementation de chargement...
	}

	protected static String c(String text) {
		return TextUtils.colorize(text);
	}

	protected static List<String> c(List<? extends String> list) {
		return TextUtils.colorize(list);
	}

	// Méthodes de la classe parente Config...
	@Override
	public String getLogPrefix() {
		return "[Shopkeepers] [Français] ";
	}

	@Override
	protected String msgMissingValue(String configKey) {
		return this.getLogPrefix() + "Message manquant : " + configKey;
	}

	@Override
	protected String msgUsingDefaultForMissingValue(String configKey, Object defaultValue) {
		return this.getLogPrefix() + "Utilisation de la valeur par défaut pour le message manquant : " + configKey;
	}

	@Override
	protected String msgValueLoadException(String configKey, ValueLoadException e) {
		return this.getLogPrefix() + "Impossible de charger le message '" + configKey + "' : "
				+ e.getMessage();
	}

	@Override
	protected String msgDefaultValueLoadException(String configKey, ValueLoadException e) {
		return this.getLogPrefix() + "Impossible de charger la valeur par défaut pour le message '" + configKey
				+ "' : " + e.getMessage();
	}

	@Override
	protected String msgInsertingDefault(String configKey) {
		return this.getLogPrefix() + "Insertion de la valeur par défaut pour le message manquant : " + configKey;
	}

	@Override
	protected String msgMissingDefault(String configKey) {
		return this.getLogPrefix() + "Valeur par défaut manquante pour le message : " + configKey;
	}

	@Override
	public void load(ConfigData configData) throws ConfigLoadException {
		Validate.notNull(configData, "configData is null");
		// Vérifier les clés de message inattendues (possiblement inexistantes) :
		Set<? extends String> configKeys = configData.getKeys();
		for (String configKey : configKeys) {
			if (this.getSetting(configKey) == null) {
				Log.warning(this.getLogPrefix() + "Message inconnu : " + configKey);
			}
		}

		// Charger la configuration :
		super.load(configData);
	}

	public static MessagesFr getInstance() {
		return INSTANCE;
	}

	private MessagesFr() {
	}
}