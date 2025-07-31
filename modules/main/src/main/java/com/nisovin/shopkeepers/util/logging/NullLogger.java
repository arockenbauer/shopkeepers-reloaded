package com.nisovin.shopkeepers.util.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;

/**
 * A {@link Logger} that logs nothing.
 */
public final class NullLogger extends Logger {

	private static final Logger INSTANCE = new NullLogger();

	public static Logger getInstance() {
		return INSTANCE;
	}

	private NullLogger() {
		super(Unsafe.assertNonNull(NullLogger.class.getCanonicalName()), null);
		super.setLevel(Level.OFF);
	}

	@Override
	public void setLevel(@Nullable Level level) {
		throw new UnsupportedOperationException("This logger cannot be modified!");
	}

	@Override
	public void log(@Nullable LogRecord logRecord) {
		assert logRecord != null;
		// Does not log anything.
	}
}
