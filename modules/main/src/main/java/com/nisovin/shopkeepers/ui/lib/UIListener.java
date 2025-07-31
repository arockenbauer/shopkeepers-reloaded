package com.nisovin.shopkeepers.ui.lib;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.util.bukkit.EventUtils;
import com.nisovin.shopkeepers.util.interaction.TestPlayerInteractEvent;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

class UIListener implements Listener {

	/**
	 * The default handled types of inventory events. Any other types of inventory events need to be
	 * explicitly requested via {@link View#getAdditionalInventoryEvents()}.
	 */
	private static final Set<? extends Class<? extends InventoryEvent>> DEFAULT_INVENTORY_EVENTS
			= Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
					InventoryClickEvent.class,
					InventoryDragEvent.class,
					InventoryCloseEvent.class
			)));

	// This object indicates on the eventHandlerStack that no view is handling a particular
	// inventory event.
	private static final Object NO_VIEW = new Object();

	private final Plugin plugin;
	private final UISessionManager uiSessionManager;
	private final Set<Class<? extends Event>> handledEventTypes = new HashSet<>();

	// Stores the view (or NO_VIEW) that handles the currently processed inventory event.
	// The handling view is determined once during the early processing of the event, added to the
	// stack, and then retrieved from the stack during the late processing of the event. We use a
	// stack here to account for plugins that might recursively call other inventory events from
	// within their event handler.
	// Usually, the views are expected to still be valid during the late event handling. This
	// assumption is in accordance with the description of the InventoryClickEvent, which states
	// that event handlers are supposed to not invoke any operations that might close the player's
	// current inventory view. However, in order to guard against plugins that ignore this Bukkit
	// API note, we check if the view is still valid and skip the late event handling if it is not.
	private final Deque<Object> eventHandlerStack = new ArrayDeque<>();

	UIListener(Plugin plugin, UISessionManager uiSessionManager) {
		Validate.notNull(plugin, "plugin is null");
		Validate.notNull(uiSessionManager, "uiSessionManager is null");
		this.plugin = plugin;
		this.uiSessionManager = uiSessionManager;
	}

	void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		DEFAULT_INVENTORY_EVENTS.forEach(this::registerEventType);
	}

	void onDisable() {
		HandlerList.unregisterAll(this);
		handledEventTypes.clear(); // Reset
	}

	// Note: It is safe to dynamically register event handlers for new types of not yet handled
	// events: Bukkit takes a snapshot of the currently registered event handlers when it calls an
	// event. Even if an event of the particular type is already being processed by the server, and
	// has already passed the early event priority phase without having been noticed by this
	// listener and accounted for in the eventHandlerStack, our newly registered event handlers
	// won't be called yet for this particular event instance, not even during the late event
	// priority phase.
	void registerEventType(Class<? extends InventoryEvent> eventClass) {
		Validate.notNull(eventClass, "eventClass is null");
		if (handledEventTypes.contains(eventClass)) return; // Already handled
		Class<? extends Event> registrationClass = EventUtils.getEventRegistrationClass(eventClass);
		// Already handled as part of a parent class:
		if (!handledEventTypes.add(registrationClass)) return;

		// Also remember the original event class for faster lookups in the future:
		handledEventTypes.add(eventClass);

		// Register two new event handlers, at low and high priority, for the specified event type,
		// and any other parent event types that share the same registration class. Just in case
		// that the registration class is unexpectedly a parent instead of a subclass of
		// InventoryEvent, the created event executors filter for InventoryEvent.
		Bukkit.getPluginManager().registerEvent(
				registrationClass,
				this,
				EventPriority.LOW,
				EventUtils.eventExecutor(InventoryEvent.class, this::onInventoryEventEarly),
				plugin,
				false
		);

		// Priority HIGH instead of HIGHEST, because we might cancel the event and other plugins
		// might want to react to that (see for example the trading view).
		Bukkit.getPluginManager().registerEvent(
				registrationClass,
				this,
				EventPriority.HIGH,
				EventUtils.eventExecutor(InventoryEvent.class, this::onInventoryEventLate),
				plugin,
				false
		);
	}

	private @Nullable View getView(HumanEntity human) {
		if (human.getType() != EntityType.PLAYER) return null;

		Player player = (Player) human;
		return uiSessionManager.getUISession(player);
	}

	private void onInventoryEventEarly(InventoryEvent event) {
		// Check if there is a view that handles this event:
		View view = this.getView(event.getView().getPlayer());
		if (view != null) {
			// Inform the view:
			var handled = view.informOnInventoryEventEarly(event);
			if (!handled) {
				view = null;
			}
		}

		// Keep track of the handling view:
		eventHandlerStack.push(view != null ? view : NO_VIEW);
	}

	private void onInventoryEventLate(InventoryEvent event) {
		// Not expected to be empty:
		Object handlingView = Unsafe.assertNonNull(eventHandlerStack.pop());
		if (handlingView == NO_VIEW) return; // Ignore the event

		View view = (View) handlingView;

		// Inform the view:
		view.informOnInventoryEventLate(event);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onInventoryClose(InventoryCloseEvent event) {
		// Inform the UISessionManager so that it can clean up any corresponding UI session:
		uiSessionManager.onInventoryClose(event);
	}

	// TODO SPIGOT-5610: The event is not firing under certain circumstances.
	// Cannot ignore cancelled events here, because the cancellation state only considers
	// useInteractedBlock.
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	void onPlayerInteract(PlayerInteractEvent event) {
		// Ignore our own fake interact event:
		if (event instanceof TestPlayerInteractEvent) return;

		// When a player interacts with a shopkeeper entity while holding an item in hand, we may
		// first receive the entity interaction event, which opens a view, and then the interaction
		// event for the item.
		// In order to not trigger any item actions for the held item, we cancel any interaction
		// events while a view is open.
		Player player = event.getPlayer();
		View view = this.getView(player);
		if (view != null) {
			Log.debug(() -> "Canceling interaction of player '" + player.getName()
					+ "' while a UI is open.");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onPlayerQuit(PlayerQuitEvent event) {
		uiSessionManager.onPlayerQuit(event.getPlayer());
	}
}
