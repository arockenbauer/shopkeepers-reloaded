package com.nisovin.shopkeepers.currency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.util.annotations.ReadOnly;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

/**
 * Registry of {@link Currency}s.
 */
public final class Currencies {

	private static final Predicate<@ReadOnly ItemStack> MATCHES_ANY = Currencies::matchesAny;

	public static Predicate<@ReadOnly ItemStack> matchesAny() {
		return MATCHES_ANY;
	}

	// Not empty. Distinct normalized ids. Distinct items. Sorted by value.
	// First currency has a value of 1.
	private static final List<Currency> ALL = new ArrayList<>();
	private static final List<? extends Currency> ALL_VIEW = Collections.unmodifiableList(ALL);

	static {
		load();
	}

	public static void load() {
		ALL.clear();
		// TODO Load the display name from the config.
		add(new Currency("base", "base", Settings.currencyItem, 1));
		if (Settings.highCurrencyValue > 0 && Settings.highCurrencyItem.getType() != Material.AIR) {
			add(new Currency("high", "high", Settings.highCurrencyItem, Settings.highCurrencyValue));
		}

		// Sort by value:
		Collections.sort(ALL, (c1, c2) -> Integer.compare(c1.getValue(), c2.getValue()));

		// TODO Throwing an exception here might not be properly handled.
		Validate.State.isTrue(getBase().getValue() == 1, "There is no currency with value 1!");
	}

	private static void add(Currency currency) {
		assert currency != null;
		assert !ALL.contains(currency);
		if (getById(currency.getId()) != null) {
			Log.severe("Invalid currency '" + currency.getId()
					+ "': There is already another currency with the same id!");
			return;
		}
		for (Currency otherCurrency : ALL) {
			if (otherCurrency.getItemData().matches(currency.getItemData().asUnmodifiableItemStack())
					|| currency.getItemData().matches(otherCurrency.getItemData().asUnmodifiableItemStack())) {
				Log.severe("Invalid currency '" + currency.getId()
						+ "': There is already another currency with a matching item!");
				return;
			}
		}
		ALL.add(currency);
	}

	public static List<? extends Currency> getAll() {
		return ALL_VIEW;
	}

	public static @Nullable Currency getById(String id) {
		for (int i = 0; i < ALL.size(); i++) {
			Currency currency = ALL.get(i);
			if (currency.getId().matches(id)) {
				return currency;
			}
		}
		return null;
	}

	public static @Nullable Currency match(@ReadOnly @Nullable ItemStack itemStack) {
		if (ItemUtils.isEmpty(itemStack)) return null;
		for (int i = 0; i < ALL.size(); i++) {
			Currency currency = ALL.get(i);
			if (currency.getItemData().matches(itemStack)) {
				return currency;
			}
		}
		return null;
	}

	public static @Nullable Currency match(@Nullable UnmodifiableItemStack itemStack) {
		return match(ItemUtils.asItemStackOrNull(itemStack));
	}

	public static boolean matchesAny(@ReadOnly @Nullable ItemStack itemStack) {
		return (match(itemStack) != null);
	}

	public static boolean matchesAny(@Nullable UnmodifiableItemStack itemStack) {
		return matchesAny(ItemUtils.asItemStackOrNull(itemStack));
	}

	public static Currency getBase() {
		return ALL.get(0);
	}

	public static Currency getHigh() {
		Validate.State.isTrue(isHighCurrencyEnabled(), "The high currency is disabled!");
		return ALL.get(1);
	}

	public static @Nullable Currency getHighOrNull() {
		if (isHighCurrencyEnabled()) return getHigh();
		return null;
	}

	public static boolean isHighCurrencyEnabled() {
		return (ALL.size() > 1);
	}

	private Currencies() {
	}
}
