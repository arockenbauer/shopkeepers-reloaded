package com.nisovin.shopkeepers.api.shopkeeper;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.events.UpdateItemEvent;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.api.shopobjects.ShopObject;
import com.nisovin.shopkeepers.api.storage.ShopkeeperStorage;
import com.nisovin.shopkeepers.api.ui.UIRegistry;
import com.nisovin.shopkeepers.api.ui.UISession;
import com.nisovin.shopkeepers.api.ui.UIType;
import com.nisovin.shopkeepers.api.util.ChunkCoords;

/**
 * A shopkeeper.
 */
public interface Shopkeeper {

	// STORAGE

	/**
	 * Requests a {@link ShopkeeperStorage#save() save} of all shopkeepers data.
	 * 
	 * @see ShopkeeperStorage#save()
	 */
	public void save();

	/**
	 * Requests a {@link ShopkeeperStorage#saveDelayed() delayed save} of all shopkeepers data.
	 * 
	 * @see ShopkeeperStorage#saveDelayed()
	 */
	public void saveDelayed();

	// ITEM UPDATES

	/**
	 * Calls an {@link UpdateItemEvent} for each item stored by this shopkeeper, i.e. the items in
	 * trade offers, the {@link PlayerShopkeeper#getHireCost()} of player shopkeepers, the shop
	 * object equipment items, items stored inside {@link #getSnapshots() snapshots}, etc.
	 * <p>
	 * This may {@link UIRegistry#abortUISessions(Shopkeeper) abort} any currently open UI sessions
	 * (trading, editor, etc.), because they might be affected by changes to the item data.
	 * <p>
	 * Note: This does not update items stored in the containers linked to the shopkeeper, e.g. for
	 * player shops.
	 * <p>
	 * This modifies but does not automatically {@link #save() save} the shopkeeper.
	 * 
	 * @return the number of updated items
	 */
	public int updateItems();

	// LIFE CYCLE

	/**
	 * Checks whether this shopkeeper instance is currently valid.
	 * <p>
	 * The shopkeeper is marked as valid after it has been freshly created or loaded from the
	 * storage and then added to the {@link ShopkeeperRegistry}. It is marked as 'invalid' once it
	 * is removed from the {@link ShopkeeperRegistry} again, for example because it is being deleted
	 * or unloaded.
	 * 
	 * @return <code>true</code> if the shopkeeper is currently valid
	 */
	public boolean isValid();

	/**
	 * Persistently removes this shopkeeper.
	 */
	public void delete();

	/**
	 * Persistently removes this shopkeeper.
	 * 
	 * @param player
	 *            the player responsible for the deletion, can be <code>null</code>
	 */
	public void delete(@Nullable Player player);

	// ATTRIBUTES

	/**
	 * Gets the shop's id.
	 * <p>
	 * This id is unique across all currently loaded shops, but there is no guarantee for it to be
	 * globally unique across server sessions.
	 * 
	 * @return the shop's id
	 */
	public int getId();

	/**
	 * Gets the shop's unique id.
	 * <p>
	 * This id is globally unique across all shopkeepers.
	 * 
	 * @return the shop's unique id, not <code>null</code>
	 */
	public UUID getUniqueId();

	/**
	 * Gets a String containing both the shopkeeper's id and unique id.
	 * 
	 * @return a String representation of this shopkeeper's id and unique id
	 */
	public String getIdString();

	/**
	 * Gets a short prefix that can be used for log messages related to this shopkeeper.
	 * <p>
	 * Example: {@literal "Shopkeeper 12: "}.
	 * 
	 * @return the log prefix
	 */
	public String getLogPrefix();

	/**
	 * Gets a prefix that can be used for log messages related to this shopkeeper.
	 * <p>
	 * This prefix contains the shopkeeper's unique id. Example:
	 * {@literal "Shopkeeper 12 (12345678-1234-1234-1234-1234567890ab): "}.
	 * 
	 * @return the log prefix
	 */
	public String getUniqueIdLogPrefix();

	/**
	 * Gets a prefix that can be used for log messages related to this shopkeeper.
	 * <p>
	 * This prefix contains the shopkeeper's location. Example:
	 * {@literal "Shopkeeper 12 at world,123,123,123: "}.
	 * <p>
	 * For {@link #isVirtual() virtual} shopkeepers, this prefix contains an indication that the
	 * shopkeeper is virtual.
	 * 
	 * @return the log prefix
	 */
	public String getLocatedLogPrefix();

	/**
	 * Gets a display name for this shopkeeper that can for example be used in command feedback
	 * messages.
	 * <p>
	 * The format of the display name is not defined, but may for example include the shopkeeper's
	 * {@link #getName() name} (if set), {@link #getId() id}, or {@link #getUniqueId() unique id}.
	 * 
	 * @return a display name for this shopkeeper
	 */
	public String getDisplayName();

	/**
	 * Gets the type of this shopkeeper (e.g.: admin, selling player, buying player, trading player,
	 * etc.).
	 * 
	 * @return the shopkeeper type
	 */
	public ShopType<?> getType();

	/**
	 * Checks whether this shopkeeper is virtual.
	 * <p>
	 * Virtual shopkeepers are not located in any world.
	 * 
	 * @return <code>true</code> if the shopkeeper is virtual
	 */
	public boolean isVirtual();

	/**
	 * Gets the name of the world this shopkeeper is located in.
	 * 
	 * @return the world name, not empty, but <code>null</code> if the shopkeeper is
	 *         {@link #isVirtual() virtual}
	 */
	public @Nullable String getWorldName();

	/**
	 * Gets the x coordinate of the shopkeeper.
	 * 
	 * @return the x coordinate
	 */
	public int getX();

	/**
	 * Gets the y coordinate of the shopkeeper.
	 * 
	 * @return the y coordinate
	 */
	public int getY();

	/**
	 * Gets the z coordinate of the shopkeeper.
	 * 
	 * @return the z coordinate
	 */
	public int getZ();

	/**
	 * Gets the yaw of the shopkeeper, i.e. its horizontal orientation.
	 * <p>
	 * The yaw is set when the shopkeeper is created or repositioned. This is the default horizontal
	 * orientation that the shopkeeper spawns its shop object in. If the shop object is able to
	 * rotate, this yaw may not match the shop object's current yaw.
	 * 
	 * @return the yaw
	 */
	public float getYaw();

	/**
	 * Gets a String representation of the shopkeeper's location.
	 * 
	 * @return the String representation
	 */
	public String getPositionString();

	/**
	 * Gets the shopkeeper's location.
	 * <p>
	 * This returns <code>null</code> if the shopkeeper is {@link #isVirtual() virtual} or if the
	 * world is not loaded.
	 * 
	 * @return the location of the shopkeeper, or <code>null</code> if the shopkeeper is virtual or
	 *         if the world isn't loaded
	 */
	public @Nullable Location getLocation();

	/**
	 * Gets the {@link ChunkCoords} identifying the chunk this shopkeeper spawns in.
	 * 
	 * @return the chunk coordinates, or <code>null</code> if this shopkeeper is virtual
	 * @see #isVirtual()
	 */
	public @Nullable ChunkCoords getChunkCoords();

	// NAMING

	/**
	 * Gets the shopkeeper's name.
	 * <p>
	 * The name may include color codes.
	 * 
	 * @return the shopkeeper's name, not <code>null</code> but may be empty if not set
	 */
	public String getName();

	/**
	 * Sets the name of the shopkeeper.
	 * 
	 * @param name
	 *            the new name, or <code>null</code> or empty to clear the shopkeeper's name
	 */
	public void setName(@Nullable String name);

	// SHOP OBJECT

	/**
	 * Gets the object representing this shopkeeper in the world.
	 * 
	 * @return the shop object, not <code>null</code>
	 */
	public ShopObject getShopObject();

	// SNAPSHOTS

	/**
	 * Gets the shopkeeper's {@link ShopkeeperSnapshot snapshots}.
	 * 
	 * @return an unmodifiable view on the snapshots, not <code>null</code>, can be empty
	 */
	public List<? extends ShopkeeperSnapshot> getSnapshots();

	/**
	 * Gets the {@link ShopkeeperSnapshot snapshot} at the specified index.
	 * 
	 * @param index
	 *            the snapshot's index, starting at <code>0</code> for the first snapshot
	 * @return the snapshot, not <code>null</code>
	 * @throws IndexOutOfBoundsException
	 *             if there is no snapshot for the specified index
	 */
	public ShopkeeperSnapshot getSnapshot(int index);

	/**
	 * Gets the index of the {@link ShopkeeperSnapshot snapshot} with the specified name.
	 * <p>
	 * The name comparison is case-insensitive and normalizes whitespace and other common word
	 * separators.
	 * 
	 * @param name
	 *            the snapshot name
	 * @return the snapshot index, or <code>-1</code> if no snapshot with the specified name is
	 *         found
	 */
	public int getSnapshotIndex(String name);

	/**
	 * Gets the {@link ShopkeeperSnapshot snapshot} with the specified name.
	 * <p>
	 * The name comparison is case-insensitive and normalizes whitespace and other common word
	 * separators.
	 * 
	 * @param name
	 *            the snapshot name
	 * @return the snapshot, or <code>null</code> if no snapshot with the specified name is found
	 */
	public @Nullable ShopkeeperSnapshot getSnapshot(String name);

	/**
	 * Creates a new {@link ShopkeeperSnapshot snapshot} of this shopkeeper's dynamic state.
	 * <p>
	 * This does not automatically {@link #addSnapshot(ShopkeeperSnapshot) add} the newly created
	 * snapshot to this shopkeeper.
	 * 
	 * @param name
	 *            the name of the new snapshot, has to be
	 *            {@link ShopkeeperSnapshot#isNameValid(String) valid}
	 * @return the newly created snapshot, not <code>null</code>
	 * @throws IllegalArgumentException
	 *             if there already exists another snapshot with the same name (case-insensitive and
	 *             with word separators normalized)
	 */
	public ShopkeeperSnapshot createSnapshot(String name);

	/**
	 * Adds the given snapshot to this shopkeeper.
	 * 
	 * @param snapshot
	 *            the snapshot, not <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the given snapshot is not compatible with this shopkeeper (e.g. if it contains
	 *             data for a different type of shopkeeper)
	 * @throws IllegalArgumentException
	 *             if there already exists another snapshot with the same name (case-insensitive and
	 *             with word separators normalized)
	 */
	public void addSnapshot(ShopkeeperSnapshot snapshot);

	/**
	 * Removes the {@link ShopkeeperSnapshot snapshot} at the specified index.
	 * 
	 * @param index
	 *            the snapshot's index, starting at <code>0</code> for the first snapshot
	 * @return the removed snapshot, not <code>null</code>
	 * @throws IndexOutOfBoundsException
	 *             if there is no snapshot for the specified index
	 */
	public ShopkeeperSnapshot removeSnapshot(int index);

	/**
	 * Removes all {@link #getSnapshots() snapshots}.
	 */
	public void removeAllSnapshots();

	/**
	 * Applies the given {@link ShopkeeperSnapshot} to this shopkeeper.
	 * <p>
	 * It is also possible to apply the snapshot of another shopkeeper if the other shopkeeper is of
	 * the same {@link #getType() type}. If the snapshot contains data for a shop object of a
	 * different type, the snapshot's object data is silently ignored and the shopkeeper's shop
	 * object retains its current state.
	 * 
	 * @param snapshot
	 *            the snapshot, not <code>null</code>
	 * @throws ShopkeeperLoadException
	 *             if the snapshot cannot be applied
	 */
	public void applySnapshot(ShopkeeperSnapshot snapshot) throws ShopkeeperLoadException;

	// TRADING

	/**
	 * Checks whether this shopkeeper has {@link #getTradingRecipes(Player) trading recipes} for the
	 * given player.
	 * <p>
	 * Ignoring exceptional cases, this method is expected to behave consistently with
	 * {@link #getTradingRecipes(Player)}, but will usually be cheaper to invoke.
	 * 
	 * @param player
	 *            the trading player, or <code>null</code> to not take player-specific trading
	 *            recipes into account
	 * @return <code>true</code> if there are trading recipes for the given player
	 */
	public boolean hasTradingRecipes(@Nullable Player player);

	/**
	 * Gets the shopkeeper's current trading recipes for the given player.
	 * <p>
	 * Depending on the type of this shopkeeper, this might access the world (e.g. check container
	 * contents) in order to determine the available stock.
	 * <p>
	 * Managing (adding, removing, editing) the trading recipes usually differs depending on the
	 * type of shopkeeper and is therefore not part of the general {@link Shopkeeper} interface.
	 * <p>
	 * The <code>player</code> parameter can be used to request player-specific trading recipes, if
	 * this type of shopkeeper supports that.
	 * 
	 * @param player
	 *            the trading player, or <code>null</code> to not take player-specific trading
	 *            recipes into account
	 * @return an unmodifiable view on the current trading recipes of this shopkeeper for the given
	 *         player
	 */
	public List<? extends TradingRecipe> getTradingRecipes(@Nullable Player player);

	// SHOPKEEPER UIs

	/**
	 * Gets all currently active {@link UISession UI sessions} involving this shopkeeper.
	 * 
	 * @return an unmodifiable view on the current UI sessions
	 * @see UIRegistry#getUISessions(Shopkeeper)
	 */
	public Collection<? extends UISession> getUISessions();

	/**
	 * Gets all currently active {@link UISession UI sessions} involving this shopkeeper and the
	 * specified {@link UIType}.
	 * 
	 * @param uiType
	 *            the UI type
	 * @return an unmodifiable view on the current UI sessions
	 * @see UIRegistry#getUISessions(Shopkeeper, UIType)
	 */
	public Collection<? extends UISession> getUISessions(UIType uiType);

	/**
	 * {@link UISession#deactivateUI() Deactivates} all currently active UIs (trading, editing,
	 * hiring, etc.) involving this shopkeeper and {@link UISession#abort() aborts} them within the
	 * next tick.
	 */
	public void abortUISessionsDelayed();

	/**
	 * Attempts to open the interface for the given {@link UIType} for the specified player.
	 * <p>
	 * This fails if this shopkeeper doesn't support the specified interface type, if the player
	 * cannot open this interface type for this shopkeeper (for example because of missing
	 * permissions), or if something else goes wrong.
	 * 
	 * @param uiType
	 *            the requested UI type
	 * @param player
	 *            the player requesting the interface
	 * @return <code>true</code> the player's request was successful and the interface was opened,
	 *         false otherwise
	 */
	public boolean openWindow(UIType uiType, Player player);

	// Shortcuts for the default UI types:

	/**
	 * Attempts to open the editor interface of this shopkeeper for the specified player.
	 * 
	 * @param player
	 *            the player requesting the editor interface
	 * @return <code>true</code> if the interface was successfully opened for the player
	 */
	public boolean openEditorWindow(Player player);

	/**
	 * Attempts to open the trading interface of this shopkeeper for the specified player.
	 * 
	 * @param player
	 *            the player requesting the trading interface
	 * @return <code>true</code> if the interface was successfully opened for the player
	 */
	public boolean openTradingWindow(Player player);
}
