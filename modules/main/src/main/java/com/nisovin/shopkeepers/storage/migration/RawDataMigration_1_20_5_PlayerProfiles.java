package com.nisovin.shopkeepers.storage.migration;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.logging.Log;
import com.nisovin.shopkeepers.util.yaml.YamlUtils;

/**
 * Clears invalid profile names and fills in missing unique ids. See GH #907.
 * <p>
 * In MC 1.20.5, Minecraft has become more strict and requires the profile names of head items to be
 * valid player names. For vanilla items, any invalid player names are simply replaced with an empty
 * string during the MC update. Bukkit's config deserialization fails to apply this vanilla data
 * migration automatically because:
 * <ul>
 * <li>On Spigot, the Minecraft DataFixer is invoked after the previous data has been deserialized
 * and already migrated to the new item component format. But the Minecraft DataFixer only matches
 * the invalid profile name in the old "tag"-based item data format.
 * <li>On Paper, the no longer valid profile data is already rejected during the deserialization.
 * The subsequent Minecraft DataFixer is therefore never reached.
 * </ul>
 * Head items with invalid profile names can result in all kinds of issues at runtime, such as
 * crashing clients whenever an inventory is opened that contains them, chunks failing to save, etc.
 * <p>
 * Additionally, player profiles always require either a valid non-empty name or a non-empty unique
 * id. If we clear an invalid name and no unique id is present, we insert a fixed dummy id. However,
 * if the profile has no name to begin with, we do not insert the dummy id, since having either a
 * non-empty name or a unique id was already a requirement previously.
 * <p>
 * Note: In certain Spigot versions, empty profile names are replaced with {@code null} during
 * serialization, which might not be considered equal to profiles with an empty name.
 */
public class RawDataMigration_1_20_5_PlayerProfiles implements RawDataMigration {

	// We only match profiles with a non-empty name and assume that any other profiles already have
	// a unique id, since having either a name or a unique id was already a requirement in previous
	// server versions.
	private static final Pattern PATTERN = Pattern.compile(
			"(?m)(^.*)(==: PlayerProfile$(?:[\r\n])*)"
					+ "(?:(\\1uniqueId: )(.*$)([\r\n])*)?"
					+ "(\\1name: )(.*$)([\r\n])*"
	);

	// Fixed version 3 UUID: Guaranteed to not clash with any player uuid and easy to identify in
	// the save data.
	// Not the NIL UUID, since Spigot was using this internally in previous versions as null value.
	// 5458ec26-8221-366d-8836-be7a07a5e29b
	private static final String UNIQUE_ID_STRING = UUID.nameUUIDFromBytes(
			StandardCharsets.UTF_8
					.encode("Shopkeepers_Migration_1_20_5_PlayerProfiles")
					.array()
	).toString();

	@Override
	public String getName() {
		return "MC 1.20.5 player profiles (head items)";
	}

	@Override
	public String apply(String data) throws RawDataMigrationException {
		var matcher = PATTERN.matcher(data);
		var migrated = matcher.replaceAll(matchResult -> {
			var uniqueIdYaml = matchResult.group(4); // Can be null or empty if missing
			var nameYaml = matchResult.group(7);
			assert nameYaml != null;

			var uniqueIdReplacement = "$4";

			@Nullable String name = YamlUtils.fromYaml(nameYaml);
			if (name != null && !isValidPlayerName(name)) {
				Log.warning("Removing invalid profile name '" + name + "' near position "
						+ matchResult.start() + "!");
				nameYaml = "\"\"";

				// Player profiles always require either a non-blank name or a unique id. If we
				// clear the name and no unique id is present, we add a dummy id in order for the
				// profile to load after our migration:
				if (uniqueIdYaml == null || uniqueIdYaml.isEmpty()) {
					Log.warning("Adding missing profile id near position " + matchResult.start()
							+ "!");
					uniqueIdReplacement = "$1"
							+ Matcher.quoteReplacement("uniqueId: " + UNIQUE_ID_STRING + "\n");
				}
			}

			return "$1$2$3" + uniqueIdReplacement + "$5$6" + Matcher.quoteReplacement(nameYaml)
					+ "$8";
		});
		return migrated;
	}

	private static boolean isValidPlayerName(String name) {
		if (name.length() > 16) return false;
		return name.chars().filter(c -> c <= 32 || c >= 127).findAny().isEmpty();
	}
}
