package com.nisovin.shopkeepers.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.java.Validate;

/**
 * Information about a compatibility provider.
 */
public class CompatVersion {

	public static final String VARIANT_PAPER = "paper";

	private static String getVariant(String compatVersion) {
		// Expected version format: "1_21_R5_some_variant_tag"
		var variantSeparatorIndex = compatVersion.indexOf('_', 7);
		if (variantSeparatorIndex < 0) return "";

		return compatVersion.substring(variantSeparatorIndex + 1);
	}

	private final String compatVersion;
	private final String variant;
	private final List<ServerVersion> supportedServerVersions;

	// supportedServerVersions: Expected to be sorted from oldest to newest
	public CompatVersion(String compatVersion, List<ServerVersion> supportedServerVersions) {
		Validate.notEmpty(compatVersion, "compatVersion is empty");
		Validate.notNull(supportedServerVersions, "supportedServerVersions is null");
		Validate.isTrue(!supportedServerVersions.isEmpty(), "supportedServerVersions is empty");
		this.compatVersion = compatVersion;
		this.variant = getVariant(compatVersion);
		// Immutable copy:
		this.supportedServerVersions
				= Collections.unmodifiableList(new ArrayList<>(supportedServerVersions));
	}

	public CompatVersion(String compatVersion, String minecraftVersion, String mappingsVersion) {
		this(
				compatVersion,
				Collections.singletonList(new ServerVersion(minecraftVersion, mappingsVersion))
		);
	}

	/**
	 * Gets the Shopkeepers compatibility version.
	 * 
	 * @return the Shopkeepers compatibility version
	 */
	public String getCompatVersion() {
		return compatVersion;
	}

	/**
	 * Gets the variant component of the compatibility version, e.g. {@link #VARIANT_PAPER}.
	 * 
	 * @return the variant component of the compatibility version, or an empty String if there is no
	 *         variant
	 */
	public String getVariant() {
		return variant;
	}

	/**
	 * Checks whether this compatibility version has a non-empty {@link #getVariant() variant}.
	 * 
	 * @return <code>true</code> if this compatibility version has a non-empty variant
	 */
	public boolean hasVariant() {
		return !this.getVariant().isEmpty();
	}

	/**
	 * Gets the supported server versions.
	 * 
	 * @return the supported server versions, not <code>null</code> or empty, sorted from oldest to
	 *         newest
	 */
	public List<ServerVersion> getSupportedServerVersions() {
		return supportedServerVersions;
	}

	/**
	 * Gets the {@link ServerVersion#getMinecraftVersion()} of the first
	 * {@link #getSupportedServerVersions() supported server version}.
	 * 
	 * @return the Minecraft version of the first supported server version
	 */
	public String getFirstMinecraftVersion() {
		return supportedServerVersions.getFirst().getMinecraftVersion();
	}

	/**
	 * Gets the {@link ServerVersion#getMappingsVersion()} of the first
	 * {@link #getSupportedServerVersions() supported server version}.
	 * 
	 * @return the mappings version of the first supported server version
	 */
	public String getFirstMappingsVersion() {
		return supportedServerVersions.getFirst().getMappingsVersion();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + compatVersion.hashCode();
		result = prime * result + supportedServerVersions.hashCode();
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CompatVersion)) return false;
		CompatVersion other = (CompatVersion) obj;
		if (!compatVersion.equals(other.compatVersion)) return false;
		if (!supportedServerVersions.equals(other.supportedServerVersions)) return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CompatVersion [compatVersion=");
		builder.append(compatVersion);
		builder.append(", serverVersions=");
		builder.append(supportedServerVersions);
		builder.append("]");
		return builder.toString();
	}
}
