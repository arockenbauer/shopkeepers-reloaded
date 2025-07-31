package com.nisovin.shopkeepers.storage.migration;

import java.util.Arrays;
import java.util.List;

import com.nisovin.shopkeepers.util.logging.Log;

public class RawDataMigrations {

	private static final List<? extends RawDataMigration> migrations = Arrays.asList(
			new RawDataMigration_1_20_5_PlayerProfiles()
	);

	/**
	 * Applies all {@link RawDataMigration}s.
	 * 
	 * @param data
	 *            the shopkeeper data to migrate
	 * @return the migrated shopkeeper data
	 * @throws RawDataMigrationException
	 *             if the migration fails
	 */
	public static String applyMigrations(String data) throws RawDataMigrationException {
		if (data.isEmpty()) return data;

		// TODO Extract the data version and pass it to migrations?

		// Apply migrations:
		var migratedData = data;
		for (var migration : migrations) {
			Log.debug("Applying raw shopkeeper data migration: " + migration.getName());
			try {
				migratedData = migration.apply(migratedData);
			} catch (Exception e) {
				throw new RawDataMigrationException(
						"Raw shopkeeper data migration failed with an error!",
						e
				);
			}
		}

		return migratedData;
	}

	private RawDataMigrations() {
	}
}
