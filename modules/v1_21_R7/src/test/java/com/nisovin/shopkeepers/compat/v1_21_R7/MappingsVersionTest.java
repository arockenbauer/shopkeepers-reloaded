package com.nisovin.shopkeepers.compat.v1_21_R7;

import static org.junit.Assert.*;

import org.junit.Test;

import com.nisovin.shopkeepers.compat.CompatVersion;

public class MappingsVersionTest {

	@Test
	public void testMappingsVersion() throws Exception {
		CompatProviderImpl compatProvider = new CompatProviderImpl();
		CompatVersion compatVersion = compatProvider.getCompatVersion();
		String expectedMappingsVersion = compatVersion.getFirstMappingsVersion();
		String actualMappingsVersion = MappingsVersionExtractor.getMappingsVersion(
				compatProvider.getCraftMagicNumbersClass()
		);
		assertEquals("Unexpected mappings version!",
				expectedMappingsVersion,
				actualMappingsVersion
		);
	}
}
