package com.nisovin.shopkeepers.util.data.persistence;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.nisovin.shopkeepers.util.data.container.DataContainer;
import com.nisovin.shopkeepers.util.java.FileUtils;
import com.nisovin.shopkeepers.util.java.Validate;

/**
 * Base type for {@link DataStore} implementations.
 * <p>
 * This is an interface with default methods instead of an abstract class in order to allow data
 * store implementations to additionally derive from some concrete {@link DataContainer}
 * implementation.
 */
public interface DataStoreBase extends DataStore {

	@Override
	public default void load(File file) throws IOException, InvalidDataFormatException {
		Validate.notNull(file, "file is null");
		this.load(file.toPath());
	}

	@Override
	public default void load(Path path) throws IOException, InvalidDataFormatException {
		Validate.notNull(path, "path is null");
		this.load(Files.newBufferedReader(path, StandardCharsets.UTF_8));
	}

	@Override
	public default void load(Reader reader) throws IOException, InvalidDataFormatException {
		var content = FileUtils.read(reader);
		this.loadFromString(content);
	}

	@Override
	public default void save(File file) throws IOException {
		Validate.notNull(file, "file is null");
		this.save(file.toPath());
	}

	@Override
	public default void save(Path path) throws IOException {
		Validate.notNull(path, "path is null");
		FileUtils.createParentDirectories(path);
		this.save(Files.newBufferedWriter(path, StandardCharsets.UTF_8));
	}

	@Override
	public default void save(Writer writer) throws IOException {
		Validate.notNull(writer, "writer is null");
		String data = this.saveToString();
		try {
			writer.write(data);
		} finally {
			writer.close();
		}
	}
}
