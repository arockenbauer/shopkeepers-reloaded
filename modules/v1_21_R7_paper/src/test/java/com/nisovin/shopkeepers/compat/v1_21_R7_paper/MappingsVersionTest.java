package com.nisovin.shopkeepers.compat.v1_21_R7_paper;

import static org.junit.Assert.*;

import org.junit.Test;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.compat.CompatVersion;

import net.minecraft.SharedConstants;

public class MappingsVersionTest {

	private static String getMinecraftVersion() {
		SharedConstants.tryDetectVersion();
		return Unsafe.assertNonNull(SharedConstants.getCurrentVersion().id());
	}

	@Test
	public void testMappingsVersion() throws Exception {
		CompatProviderImpl compatProvider = new CompatProviderImpl();
		CompatVersion compatVersion = compatProvider.getCompatVersion();
		String expectedMappingsVersion = compatVersion.getFirstMappingsVersion();
		var actualMappingsVersion = getMinecraftVersion();
		assertEquals("Unexpected mappings version!",
				expectedMappingsVersion,
				actualMappingsVersion
		);
	}
}
