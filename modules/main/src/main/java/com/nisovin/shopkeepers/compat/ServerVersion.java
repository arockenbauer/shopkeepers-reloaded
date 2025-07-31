package com.nisovin.shopkeepers.compat;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.java.Validate;

/**
 * A supported server version.
 */
public class ServerVersion {

	private final String minecraftVersion;
	private final String mappingsVersion;

	public ServerVersion(String minecraftVersion, String mappingsVersion) {
		Validate.notEmpty(minecraftVersion, "minecraftVersion is empty");
		Validate.notEmpty(mappingsVersion, "mappingsVersion is empty");
		this.minecraftVersion = minecraftVersion;
		this.mappingsVersion = mappingsVersion;
	}

	/**
	 * Gets the Minecraft server version.
	 * 
	 * @return the Minecraft server version
	 */
	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	/**
	 * Gets the server mappings version.
	 * <p>
	 * Note: On Paper, since 1.21.6, the server no longer supports the mappings version, so this
	 * returns the Minecraft version instead.
	 * 
	 * @return the server mappings version
	 */
	public String getMappingsVersion() {
		return mappingsVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + minecraftVersion.hashCode();
		result = prime * result + mappingsVersion.hashCode();
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ServerVersion)) return false;
		ServerVersion other = (ServerVersion) obj;
		if (!mappingsVersion.equals(other.mappingsVersion)) return false;
		if (!minecraftVersion.equals(other.minecraftVersion)) return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServerVersion [minecraftVersion=");
		builder.append(minecraftVersion);
		builder.append(", mappingsVersion=");
		builder.append(mappingsVersion);
		builder.append("]");
		return builder.toString();
	}
}
