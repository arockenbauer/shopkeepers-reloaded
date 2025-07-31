package com.nisovin.shopkeepers.storage.migration;

/**
 * A migration that operates on the raw string-based shopkeeper data, i.e. before the data is
 * deserialized by Bukkit.
 * <p>
 * The deserialized data version is not yet available at this stage, so these kinds of migrations
 * usually operate by matching and replacing patterns in the string data.
 */
public interface RawDataMigration {

	/**
	 * Gets a short user-friendly name for this migration.
	 * 
	 * @return the name of this migration
	 */
	public String getName();

	/**
	 * Applies the migration.
	 * <p>
	 * If an issue prevents the migration of individual elements in the data, it is usually
	 * preferred to abort the migration by throwing a {@link RawDataMigrationException}, and thereby
	 * abort the data loading as a whole, rather then trying to continue with partially migrated
	 * data.
	 * 
	 * @param data
	 *            the current shopkeeper data
	 * @return the migrated shopkeeper data
	 * @throws RawDataMigrationException
	 *             if the migration fails
	 */
	public String apply(String data) throws RawDataMigrationException;
}
