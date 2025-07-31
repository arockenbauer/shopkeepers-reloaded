package com.nisovin.shopkeepers.shopobjects.living.types;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.compat.Compat;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.ShopObjectData;
import com.nisovin.shopkeepers.shopobjects.living.LivingShops;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObjectType;
import com.nisovin.shopkeepers.ui.editor.Button;
import com.nisovin.shopkeepers.ui.editor.EditorView;
import com.nisovin.shopkeepers.ui.editor.ShopkeeperActionButton;
import com.nisovin.shopkeepers.util.data.property.BasicProperty;
import com.nisovin.shopkeepers.util.data.property.Property;
import com.nisovin.shopkeepers.util.data.property.value.PropertyValue;
import com.nisovin.shopkeepers.util.data.serialization.InvalidDataException;
import com.nisovin.shopkeepers.util.data.serialization.bukkit.NamespacedKeySerializers;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;

public class ChickenShop extends BabyableShop<Chicken> {

	// TODO Replace with the actual type once we only support MC 1.21.5+
	public static final Property<NamespacedKey> VARIANT = new BasicProperty<NamespacedKey>()
			.dataKeyAccessor("variant", NamespacedKeySerializers.DEFAULT)
			.defaultValue(NamespacedKey.minecraft("temperate"))
			.build();

	private final PropertyValue<NamespacedKey> variantProperty = new PropertyValue<>(VARIANT)
			.onValueChanged(Unsafe.initialized(this)::applyVariant)
			.build(properties);

	public ChickenShop(
			LivingShops livingShops,
			SKLivingShopObjectType<ChickenShop> livingObjectType,
			AbstractShopkeeper shopkeeper,
			@Nullable ShopCreationData creationData
	) {
		super(livingShops, livingObjectType, shopkeeper, creationData);
	}

	@Override
	public void load(ShopObjectData shopObjectData) throws InvalidDataException {
		super.load(shopObjectData);
		variantProperty.load(shopObjectData);
	}

	@Override
	public void save(ShopObjectData shopObjectData, boolean saveAll) {
		super.save(shopObjectData, saveAll);
		variantProperty.save(shopObjectData);
	}

	@Override
	protected void onSpawn() {
		super.onSpawn();
		this.applyVariant();
	}

	@Override
	public List<Button> createEditorButtons() {
		List<Button> editorButtons = super.createEditorButtons();
		editorButtons.add(this.getVariantEditorButton());
		return editorButtons;
	}

	// VARIANT

	public NamespacedKey getVariant() {
		return variantProperty.getValue();
	}

	public void setVariant(NamespacedKey variant) {
		variantProperty.setValue(variant);
	}

	public void cycleVariant(boolean backwards) {
		this.setVariant(Compat.getProvider().cycleChickenVariant(this.getVariant(), backwards));
	}

	private void applyVariant() {
		Chicken entity = this.getEntity();
		if (entity == null) return; // Not spawned

		Compat.getProvider().setChickenVariant(entity, this.getVariant());
	}

	private ItemStack getVariantEditorItem() {
		ItemStack iconItem = new ItemStack(Material.LEATHER_CHESTPLATE);
		switch (this.getVariant().getKey()) {
		case "warm":
			ItemUtils.setLeatherColor(iconItem, Color.fromRGB(230, 178, 100));
			break;
		case "cold":
			ItemUtils.setLeatherColor(iconItem, Color.fromRGB(116, 106, 125));
			break;
		case "temperate":
		default:
			ItemUtils.setLeatherColor(iconItem, Color.WHITE);
			break;
		}
		ItemUtils.setDisplayNameAndLore(iconItem,
				Messages.buttonChickenVariant,
				Messages.buttonChickenVariantLore
		);
		return iconItem;
	}

	private Button getVariantEditorButton() {
		return new ShopkeeperActionButton() {
			@Override
			public @Nullable ItemStack getIcon(EditorView editorView) {
				return getVariantEditorItem();
			}

			@Override
			protected boolean runAction(EditorView editorView, InventoryClickEvent clickEvent) {
				boolean backwards = clickEvent.isRightClick();
				cycleVariant(backwards);
				return true;
			}
		};
	}
}
