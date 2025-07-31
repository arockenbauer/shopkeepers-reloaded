package com.nisovin.shopkeepers.util.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.logging.NullLogger;

/**
 * File related utilities.
 * <p>
 * Several methods in this class wrap underlying IO exceptions into IO exceptions that replicate the
 * original exception messages but prepend it with a more general and therefore user-friendly
 * description of the failed operation.
 */
public final class FileUtils {

	/**
	 * A {@link DateTimeFormatter} that formats date and time information (to the second) in a
	 * format that is compatible with the naming restrictions of typical file systems.
	 */
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-DD_HH-mm-ss");

	/**
	 * Checks if the specified file is {@link Files#isWritable(Path) writable} and throws an
	 * {@link IOException} if this is not the case.
	 * <p>
	 * This assumes that the given path refers to an existing file.
	 * 
	 * @param file
	 *            the file path
	 * @throws IOException
	 *             if the file is not writable
	 */
	public static void checkIsFileWritable(Path file) throws IOException {
		if (!Files.isWritable(file)) {
			throw new IOException("Missing write permission for file " + file);
		}
	}

	/**
	 * Checks if the specified directory is both {@link Files#isWritable(Path) writable} and
	 * {@link Files#isExecutable(Path) executable} (i.e. accessible) and throws an
	 * {@link IOException} if this is not the case.
	 * <p>
	 * This assumes that the given path refers to an existing directory.
	 * <p>
	 * This set of permissions is for example required to rename files within the directory.
	 * 
	 * @param directory
	 *            the directory path
	 * @throws IOException
	 *             if the directory is not writable or executable (i.e. accessible)
	 */
	public static void checkIsDirectoryWritable(Path directory) throws IOException {
		if (!Files.isWritable(directory)) {
			throw new IOException("Missing write permission for directory " + directory);
		}
		try {
			if (!Files.isExecutable(directory)) {
				throw new IOException("Missing execute (i.e. access) permission for directory "
						+ directory);
			}
		} catch (SecurityException e) {
			// Some SecurityManager implementations blindly deny the 'execute' permission without
			// differentiating between files and directories. This should have no effect on whether
			// we can write to the directory, so we can safely ignore it.
		}
	}

	/**
	 * Ensures that any pending writes to the file at the specified path are physically persisted to
	 * the underlying storage device (i.e. instructs the operating system to flush any write buffers
	 * related to that file).
	 * <p>
	 * For some file / operating systems it may also be required to invoke fsync on directories to
	 * ensure that the names of any contained newly created or renamed files have been properly
	 * persisted. However, not all operating systems (e.g. Windows) support to open or fsync
	 * directories. We therefore ignore any thrown {@link IOException} if the given path points to a
	 * directory.
	 * 
	 * @param path
	 *            the file path
	 * @throws IOException
	 *             if the operation fails
	 */
	public static void fsync(Path path) throws IOException {
		// References regarding data consistency and fsync:
		// http://blog.httrack.com/blog/2013/11/15/everything-you-always-wanted-to-know-about-fsync/
		// https://thunk.org/tytso/blog/2009/03/15/dont-fear-the-fsync/
		// http://danluu.com/file-consistency/
		boolean isDirectory = Files.isDirectory(path); // Only true if the directory exists
		// Directories are opened in read-only mode, whereas regular files require write mode:
		StandardOpenOption fileAccess = isDirectory ? StandardOpenOption.READ : StandardOpenOption.WRITE;
		// Note: This also checks for file existence.
		try (FileChannel file = FileChannel.open(path, fileAccess)) {
			file.force(true);
		} catch (IOException e) {
			if (isDirectory) {
				// Ignored for directories, since this is not supported on all operating systems
				// (e.g. Windows):
				return;
			}
			throw new IOException("Could not fsync file '" + path + "': "
					+ ThrowableUtils.getDescription(e), e);
		}
	}

	/**
	 * Invokes {@link #fsync(Path)} for the given path's parent, if it has a parent.
	 * 
	 * @param path
	 *            the path
	 * @throws IOException
	 *             if the operation fails
	 */
	public static void fsyncParentDirectory(Path path) throws IOException {
		Validate.notNull(path, "path is null");
		Path parent = path.getParent();
		if (parent != null) {
			fsync(parent);
		}
	}

	/**
	 * Creates the specified directory and any not yet existing parents.
	 * <p>
	 * Does not throw an exception if the directory already exists.
	 * 
	 * @param directory
	 *            the directory path
	 * @throws IOException
	 *             if the operation fails
	 * @see Files#createDirectories(Path, java.nio.file.attribute.FileAttribute...)
	 */
	public static void createDirectories(Path directory) throws IOException {
		try {
			// This does nothing if the directory already exists.
			Files.createDirectories(directory);
		} catch (IOException e) {
			throw new IOException("Could not create directory '" + directory + "': "
					+ ThrowableUtils.getDescription(e), e);
		}
	}

	/**
	 * Creates all not yet existing parent directories for the specified path.
	 * <p>
	 * Does not throw an exception if the parent directories already exist. Does nothing if the
	 * specified path does not have a parent.
	 * 
	 * @param path
	 *            the path
	 * @throws IOException
	 *             if the operation fails
	 */
	public static void createParentDirectories(Path path) throws IOException {
		Path parent = path.getParent(); // Can be null
		if (parent != null) {
			createDirectories(parent);
		}
	}

	/**
	 * Deletes the specified file.
	 * 
	 * @param path
	 *            the file path
	 * @throws IOException
	 *             if the operation fails
	 * @see Files#delete(Path)
	 */
	public static void delete(Path path) throws IOException {
		try {
			Files.delete(path);
		} catch (IOException e) {
			throw new IOException("Could not delete file '" + path + "': "
					+ ThrowableUtils.getDescription(e), e);
		}
	}

	/**
	 * Deletes the specified file if it exists.
	 * 
	 * @param path
	 *            the file path
	 * @return <code>true</code> if the file existed and has been removed, <code>false</code> if it
	 *         did not exist
	 * @throws IOException
	 *             if the operation fails
	 * @see Files#deleteIfExists(Path)
	 */
	public static boolean deleteIfExists(Path path) throws IOException {
		try {
			return Files.deleteIfExists(path);
		} catch (IOException e) {
			throw new IOException("Could not delete file '" + path + "': "
					+ ThrowableUtils.getDescription(e), e);
		}
	}

	/**
	 * Moves the specified source file to the given target path, replacing any already existing file
	 * at that path.
	 * <p>
	 * This attempts to atomically rename the file, but may fall back to a non-atomic move
	 * operation. In the latter case, any occurring IO exception or severe system failure (crash,
	 * power loss, etc.) may leave the target file in an undefined state. This logs a warning for
	 * each attempted fallback solution using the given {@link Logger}.
	 * <p>
	 * To account for transient issues that may occasionally prevent this operation from succeeding
	 * (such as in the presence of other processes concurrently interacting with these files), it is
	 * recommended to wrap this operation into a suitable retry loop.
	 * <p>
	 * This method does not guarantee that the file name changes are actually persisted to disk once
	 * the method returns. To ensure that the caller has to subsequently invoke {@link #fsync(Path)}
	 * on the directory containing the target path.
	 * 
	 * @param source
	 *            the path of the source file
	 * @param target
	 *            the path of the target file
	 * @param logger
	 *            the logger used to log warnings when atomic moving is not possible, can be
	 *            {@link NullLogger} to not log anything
	 * @throws IOException
	 *             if the operation fails for some reason and we are not able to recover by applying
	 *             some fallback
	 */
	public static void moveFile(Path source, Path target, Logger logger) throws IOException {
		Validate.notNull(source, "source is null");
		Validate.notNull(target, "target is null");
		Validate.notNull(logger, "logger is null");

		// Create the parent directories if necessary:
		createParentDirectories(target);

		try {
			// Attempt atomic move / rename:
			try {
				Files.move(
						source,
						target,
						StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING
				);
			} catch (AtomicMoveNotSupportedException e) {
				// Attempt non-atomic move:
				// TODO Turn this into a debug message? Might spam if this is logged repeatedly on a
				// system that is known to not support atomic moves. Or maybe only print this once
				// as warning, and then only in debug mode.
				logger.warning(() -> "Could not atomically move file '" + source + "' to '" + target
						+ "' (" + ThrowableUtils.getDescription(e)
						+ ")! Attempting non-atomic move.");
				Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			// Attempt File#renameTo(File):
			if (!source.toFile().renameTo(target.toFile())) {
				// Attempt copy and delete:
				// TODO Turn this into a debug message?
				logger.warning(() -> "Could not move file '" + source + "' to '" + target + "' ("
						+ ThrowableUtils.getDescription(e) + ")! Attempting copy and delete.");
				try {
					copy(source, target, StandardCopyOption.REPLACE_EXISTING);
					delete(source);
				} catch (IOException e2) {
					throw new IOException("Could not copy-and-delete file '" + source + "' to '"
							+ target + "': " + ThrowableUtils.getDescription(e2), e2);
				}
			}
		}
	}

	/**
	 * Copies the file at the specified path to the specified destination.
	 * <p>
	 * Unlike {@link Files#copy(Path, Path, CopyOption...)}, this additionally creates parent
	 * directories if missing and ensures that the changes are persisted to disk (see
	 * {@link #fsync(Path)} and {@link #fsyncParentDirectory(Path)}.
	 * 
	 * @param source
	 *            the file to copy
	 * @param target
	 *            the destination
	 * @param copyOptions
	 *            the {@link CopyOption}s
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static void copy(Path source, Path target, CopyOption... copyOptions) throws IOException {
		Validate.notNull(source, "source is null");
		Validate.notNull(target, "target is null");

		// Create the parent directories if necessary:
		createParentDirectories(target);

		Files.copy(source, target, copyOptions);

		// Ensure that the data has been written to disk:
		fsync(target);
		// Also fsync the containing directory since we might have freshly created the target file:
		fsyncParentDirectory(target);
	}

	/**
	 * Opens or creates a file for writing.
	 * <p>
	 * This mimics {@link Files#newBufferedWriter(Path, Charset, OpenOption...)}, but returns an
	 * unbuffered {@link Writer}.
	 * 
	 * @param path
	 *            the path to the file
	 * @param cs
	 *            the charset to use for encoding
	 * @param options
	 *            options specifying how the file is opened
	 * @return the unbuffered writer to write text to the file
	 * @throws IOException
	 *             see {@link Files#newBufferedWriter(Path, Charset, OpenOption...)}
	 */
	public static Writer newUnbufferedWriter(
			Path path,
			Charset cs,
			OpenOption... options
	) throws IOException {
		// Unlike the OutputStreamWriter constructor that accepts a Charset directly, which creates
		// an encoder that removes or replaces invalid data from the input, this encoder throws
		// exceptions when it encounters invalid data.
		CharsetEncoder encoder = cs.newEncoder();
		Writer writer = new OutputStreamWriter(Files.newOutputStream(path, options), encoder);
		return writer;
	}

	/**
	 * Reads the content from the given {@link Reader} to a string.
	 * <p>
	 * Unless the given reader is already buffered, it is wrapped in a new {@link BufferedReader}.
	 * The reader is closed once the content has been read.
	 * <p>
	 * All line separators are replaced by {@code \n} in the returned string.
	 * 
	 * @param reader
	 *            the reader
	 * @return the read string, not <code>null</code>
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static String read(Reader reader) throws IOException {
		Validate.notNull(reader, "reader is null");
		BufferedReader bufferedReader;
		if (reader instanceof BufferedReader) {
			bufferedReader = (BufferedReader) reader;
		} else {
			bufferedReader = new BufferedReader(reader);
		}

		StringBuilder data = new StringBuilder();
		try {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				data.append(line).append('\n');
			}
			return data.toString();
		} finally {
			bufferedReader.close();
		}
	}

	/**
	 * Gets the path to a sibling of the given file path by appending {@code .tmp}.
	 * 
	 * @param path
	 *            the file path
	 * @return the sibling temporary file path
	 */
	public static Path getTempSibling(Path path) {
		var fileName = path.getFileName();
		if (fileName == null) {
			throw new IllegalArgumentException("Path is empty!");
		}
		return path.resolveSibling(fileName + ".tmp");
	}

	/**
	 * Gets the path relative to the specified base path, but only if the given path starts with the
	 * given base path.
	 * 
	 * @param basePath
	 *            the base path
	 * @param path
	 *            the path
	 * @return the relative path, or the path itself if it does not start with the given base path
	 */
	public static Path relativize(@Nullable Path basePath, Path path) {
		if (basePath == null || !path.startsWith(basePath)) {
			return path;
		}

		return basePath.relativize(path);
	}

	/**
	 * Safely writes the given text to a file at the specified path.
	 * <p>
	 * This first writes the text to a temporary intermediate file in the same directory before
	 * deleting any currently existing file at the specified destination and (ideally atomically)
	 * moving the temporary file to the specified destination.
	 * <p>
	 * If a file at the temporary path already exists, e.g. from a previous writing attempt,
	 * depending on whether a file exists at the destination path, the temporary file is either
	 * deleted or first moved to the destination path before we write the new file contents.
	 * <p>
	 * This method also takes care of first checking the necessary file system permissions for more
	 * detailed error feedback, creating any missing parent directories, and ensuring that the
	 * changes are physically persisted to disk.
	 * 
	 * @param path
	 *            the file path
	 * @param content
	 *            the file content
	 * @param charset
	 *            the {@link Charset}
	 * @param logger
	 *            the {@link Logger} to use for certain warnings
	 * @param basePath
	 *            if specified, any error or warning messages that include path strings will use the
	 *            path relative to this base path instead
	 * @throws IOException
	 *             if the operation fails
	 */
	public static void writeSafely(
			Path path,
			String content,
			Charset charset,
			Logger logger,
			@Nullable Path basePath
	) throws IOException {
		var tempPath = getTempSibling(path);
		assert tempPath != null;

		// Handle already existing temporary file:
		handleExistingTempFile(path, logger, basePath);

		// Ensure that the temporary file's parent directories exist:
		FileUtils.createParentDirectories(tempPath);

		// Check write permissions for the involved directories:
		Path tempDirectory = tempPath.getParent();
		if (tempDirectory != null) {
			FileUtils.checkIsDirectoryWritable(tempDirectory);
		}

		Path directory = path.getParent();
		if (directory != null && !directory.equals(tempDirectory)) {
			FileUtils.checkIsDirectoryWritable(directory);
		}

		// Create new temporary file and write data to it:
		try (Writer writer = Files.newBufferedWriter(tempPath, charset)) {
			writer.write(content);
		} catch (IOException e) {
			throw new IOException("Could not write temporary file ("
					+ relativize(basePath, tempPath) + "): " + ThrowableUtils.getDescription(e), e);
		}

		// Fsync the temporary file and the containing directory (ensures that the data is actually
		// persisted to disk):
		FileUtils.fsync(tempPath);
		FileUtils.fsyncParentDirectory(tempPath);

		// Delete the old file (if it exists):
		FileUtils.deleteIfExists(path);

		// Ensure that the file's parent directories exist:
		FileUtils.createParentDirectories(path);

		// Rename the temporary file (ideally atomically):
		FileUtils.moveFile(tempPath, path, logger);

		// Fsync the file's parent directory (ensures that the rename operation is persisted to
		// disk):
		FileUtils.fsyncParentDirectory(path);
	}

	// If a temporary file already exists, this might indicate an issue during a previous writing
	// attempt. Depending on whether the destination file exists, we either rename or delete the
	// temporary file.
	private static void handleExistingTempFile(Path path, Logger logger, @Nullable Path basePath)
			throws IOException {
		var tempPath = getTempSibling(path);

		if (!Files.exists(tempPath)) return;

		// Check write permissions:
		FileUtils.checkIsFileWritable(tempPath);

		Path tempDirectory = tempPath.getParent();
		if (tempDirectory != null) {
			FileUtils.checkIsDirectoryWritable(tempDirectory);
		}

		Path directory = path.getParent();
		if (directory != null && !directory.equals(tempDirectory)) {
			FileUtils.checkIsDirectoryWritable(directory);
		}

		if (!Files.exists(path)) {
			// Renaming the temporary file might have failed during an earlier write attempt. It
			// might contain the only backup of previously written data. -> Do not remove it!
			// Instead, we try to rename it to make it the new 'old data' and then continue the
			// writing procedure.
			logger.warning("Found an existing temporary file (" + relativize(basePath, tempPath)
					+ "), but no file at the destination (" + relativize(basePath, path) + ")!"
					+ " This might indicate an issue during a previous write attempt!"
					+ " We rename the temporary file and then continue the writing!");

			// Rename the temporary file:
			FileUtils.moveFile(tempPath, path, logger);
		} else {
			logger.warning("Found an existing temporary file (" + relativize(basePath, tempPath)
					+ "), but also a file at the destination (" + relativize(basePath, path) + ")!"
					+ " This might indicate an issue during a previous write attempt!"
					+ " We delete the temporary file and then continue the writing!");

			// Delete the old temporary file:
			FileUtils.delete(tempPath);
		}
	}

	private FileUtils() {
	}
}
