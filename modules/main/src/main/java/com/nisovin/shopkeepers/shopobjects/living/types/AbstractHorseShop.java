package com.nisovin.shopkeepers.shopobjects.living.types;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.ShopObjectData;
import com.nisovin.shopkeepers.shopobjects.living.LivingShops;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObjectType;
import com.nisovin.shopkeepers.ui.editor.Button;
import com.nisovin.shopkeepers.ui.editor.EditorView;
import com.nisovin.shopkeepers.ui.editor.ShopkeeperActionButton;
import com.nisovin.shopkeepers.util.bukkit.EquipmentUtils;
import com.nisovin.shopkeepers.util.data.property.BasicProperty;
import com.nisovin.shopkeepers.util.data.property.Property;
import com.nisovin.shopkeepers.util.data.property.value.PropertyValue;
import com.nisovin.shopkeepers.util.data.serialization.InvalidDataException;
import com.nisovin.shopkeepers.util.data.serialization.java.BooleanSerializers;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;

/**
 * Supports saddle.
 * <p>
 * Note: Llamas do not support saddles even though they derive from {@link AbstractHorse}.
 * 
 * @param <E>
 *            The mob type.
 */
public class AbstractHorseShop<E extends AbstractHorse> extends BabyableShop<E> {

	public static final Property<Boolean> SADDLE = new BasicProperty<Boolean>()
			.dataKeyAccessor("saddle", BooleanSerializers.LENIENT)
			.defaultValue(false)
			.omitIfDefault()
			.build();

	private final PropertyValue<Boolean> saddleProperty = new PropertyValue<>(SADDLE)
			.onValueChanged(Unsafe.initialized(this)::applySaddle)
			.build(properties);

	public AbstractHorseShop(
			LivingShops livingShops,
			SKLivingShopObjectType<? extends AbstractHorseShop<E>> livingObjectType,
			AbstractShopkeeper shopkeeper,
			@Nullable ShopCreationData creationData
	) {
		super(livingShops, livingObjectType, shopkeeper, creationData);
	}

	@Override
	public void load(ShopObjectData shopObjectData) throws InvalidDataException {
		super.load(shopObjectData);
		saddleProperty.load(shopObjectData);
	}

	@Override
	public void save(ShopObjectData shopObjectData, boolean saveAll) {
		super.save(shopObjectData, saveAll);
		saddleProperty.save(shopObjectData);
	}

	@Override
	protected void onSpawn() {
		super.onSpawn();
		this.applySaddle();
	}

	@Override
	public List<Button> createEditorButtons() {
		List<Button> editorButtons = super.createEditorButtons();
		if (EquipmentUtils.supportsSaddle(this.getEntityType())) {
			editorButtons.add(this.getSaddleEditorButton());
		}
		return editorButtons;
	}

	// SADDLE

	public boolean hasSaddle() {
		return saddleProperty.getValue();
	}

	public void setSaddle(boolean saddle) {
		if (!EquipmentUtils.supportsSaddle(this.getEntityType())) return;

		saddleProperty.setValue(saddle);
	}

	public void cycleSaddle() {
		this.setSaddle(!this.hasSaddle());
	}

	private void applySaddle() {
		if (!EquipmentUtils.supportsSaddle(this.getEntityType())) return;

		AbstractHorse entity = this.getEntity();
		if (entity == null) return; // Not spawned

		entity.getInventory().setSaddle(this.hasSaddle() ? new ItemStack(Material.SADDLE) : null);
	}

	private ItemStack getSaddleEditorItem() {
		ItemStack iconItem = new ItemStack(Material.SADDLE);
		ItemUtils.setDisplayNameAndLore(iconItem,
				Messages.buttonSaddle,
				Messages.buttonSaddleLore
		);
		return iconItem;
	}

	private Button getSaddleEditorButton() {
		return new ShopkeeperActionButton() {
			@Override
			public @Nullable ItemStack getIcon(EditorView editorView) {
				return getSaddleEditorItem();
			}

			@Override
			protected boolean runAction(EditorView editorView, InventoryClickEvent clickEvent) {
				cycleSaddle();
				return true;
			}
		};
	}
}
