package com.nisovin.shopkeepers.commands.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentParseException;
import com.nisovin.shopkeepers.commands.lib.argument.ArgumentsReader;
import com.nisovin.shopkeepers.commands.lib.argument.CommandArgument;
import com.nisovin.shopkeepers.commands.lib.argument.fallback.FallbackArgument;
import com.nisovin.shopkeepers.commands.lib.argument.fallback.FallbackArgumentException;
import com.nisovin.shopkeepers.commands.lib.context.BufferedCommandContext;
import com.nisovin.shopkeepers.commands.lib.context.CommandContext;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.commands.lib.context.SimpleCommandContext;
import com.nisovin.shopkeepers.debug.DebugOptions;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.bukkit.PermissionUtils;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.MapUtils;
import com.nisovin.shopkeepers.util.java.StringUtils;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;
import com.nisovin.shopkeepers.util.text.MessageArguments;

public abstract class Command {

	public static final String COMMAND_PREFIX = "/";
	public static final String ARGUMENTS_SEPARATOR = " ";

	private static final Text DEFAULT_HELP_TITLE_FORMAT = Text
			.color(ChatColor.AQUA).text("-------[ ")
			.color(ChatColor.DARK_GREEN).text("Command Help: ")
			.color(ChatColor.GOLD).formatting(ChatColor.ITALIC)
			.placeholder("command")
			.color(ChatColor.AQUA).text(" ]-------")
			.buildRoot();
	private static final Text DEFAULT_HELP_USAGE_FORMAT = Text
			.color(ChatColor.YELLOW).placeholder("usage")
			.buildRoot();
	private static final Text DEFAULT_HELP_DESC_FORMAT = Text
			.color(ChatColor.DARK_GRAY).text(" - ")
			.color(ChatColor.DARK_AQUA).placeholder("description")
			.buildRoot();
	private static final Text HELP_ENTRY_FORMAT = Text
			.placeholder("usage") // The usage format
			.placeholder("description") // The description format
			.buildRoot();

	private final String name;
	private final List<? extends String> aliases; // Unmodifiable
	private Text description = Text.EMPTY;
	// Null if no permission is required:
	private @Nullable String permission = null;
	private final List<CommandArgument<?>> arguments = new ArrayList<>();
	private @Nullable Command parent = null;
	private final CommandRegistry childCommands = Unsafe.initialized(new CommandRegistry(this));

	// Hides this command from the help page:
	private boolean hiddenInParentHelp = false;
	private boolean hiddenInOwnHelp = false;
	// Makes the parent help content display this command's child commands:
	private boolean includeChildsInParentHelp = false;

	// Common message arguments:
	private final MessageArguments commonMessageArgs;
	{
		// Dynamically evaluated:
		Map<String, Supplier<@NonNull ?>> commonMessageArgs = new HashMap<>();
		commonMessageArgs.put("name", Unsafe.initialized(this)::getName);
		commonMessageArgs.put("description", Unsafe.initialized(this)::getDescription);
		commonMessageArgs.put("command", Unsafe.initialized(this)::getCommandFormat);
		commonMessageArgs.put("usage", Unsafe.initialized(this)::getUsageFormat);
		commonMessageArgs.put("arguments", Unsafe.initialized(this)::getArgumentsFormat);
		this.commonMessageArgs = MessageArguments.ofMap(commonMessageArgs);
	}

	// Formatting (null results in the parent's formatting to be used):
	private @Nullable Text helpTitleFormat = null;
	private @Nullable Text helpUsageFormat = null;
	private @Nullable Text helpDescFormat = null;
	private @Nullable Text helpChildUsageFormat = null;
	private @Nullable Text helpChildDescFormat = null;

	public Command(String name) {
		this(name, Collections.emptyList());
	}

	public Command(String name, List<? extends String> aliases) {
		Validate.notEmpty(name, "name is null or empty");
		Validate.notNull(aliases, "aliases is null");
		this.name = name;

		// Validate and copy aliases:
		if (aliases.isEmpty()) {
			this.aliases = Collections.emptyList();
		} else {
			List<String> aliasesCopy = new ArrayList<>(aliases);
			// Validate aliases:
			for (String alias : aliasesCopy) {
				Validate.notEmpty(alias, "aliases contains null or empty alias");
				Validate.isTrue(!StringUtils.containsWhitespace(alias),
						"aliases contains alias with whitespace");
			}
			this.aliases = Collections.unmodifiableList(aliasesCopy);
		}
	}

	/**
	 * Gets the name of this command.
	 * <p>
	 * For {@link BaseCommand base commands} this name is supposed to be unique among the commands
	 * of the same plugin. However, there can be conflicts with the commands of other plugins.
	 * <p>
	 * For child commands this name is supposed to be unique among the child commands of the same
	 * parent command.
	 * 
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Gets all the aliases of this command.
	 * <p>
	 * Depending on the names and aliases of other commands, not all aliases might actually be
	 * active for this command.
	 * 
	 * @return an unmodifiable view on the aliases, might be empty (but not <code>null</code>)
	 */
	public final List<? extends String> getAliases() {
		return aliases;
	}

	/**
	 * A short one-line description of what this command does.
	 * <p>
	 * Might be used in command listings and the command help.
	 * 
	 * @return the short description, might be {@link Text#isPlainTextEmpty() empty} to indicate
	 *         that no description is available
	 */
	public final Text getDescription() {
		return description;
	}

	/**
	 * Sets the short description for this command.
	 * 
	 * @param description
	 *            the description
	 * @see #getDescription()
	 */
	protected final void setDescription(
			@UnknownInitialization Command this,
			@Nullable Text description
	) {
		this.description = (description == null) ? Text.EMPTY : description;
	}

	/**
	 * Gets the permission that is required for executing this command.
	 * <p>
	 * This only affects execution of this specific command, and not those of the child commands.
	 * Each child command has to specify its requirements and behavior separately.
	 * <p>
	 * This permission is for example used by {@link #testPermission(CommandSender)}.
	 * 
	 * @return the required permission, or <code>null</code> if no required permission is set
	 */
	public final @Nullable String getPermission() {
		return permission;
	}

	/**
	 * Sets the required permission for being able to execute this command.
	 * 
	 * @param permission
	 *            the permission
	 * @see #getPermission()
	 */
	protected final void setPermission(
			@UnknownInitialization Command this,
			@Nullable String permission
	) {
		this.permission = permission;
	}

	/**
	 * Checks whether the given {@link CommandSender} is potentially allowed to execute this
	 * command.
	 * <p>
	 * This only affects execution of this specific command, and not those of the child commands.
	 * Each child command has to specify its requirements and behavior separately.
	 * <p>
	 * By default the implementation checks the permission returned by {@link #getPermission()}, but
	 * it may be overridden to check for additional {@link CommandSender} specific conditions. If
	 * implementations are unsure if the {@link CommandSender} can execute this command,
	 * <code>true</code> should be returned.
	 * <p>
	 * The result of this method may be used to determine whether this command is listed in command
	 * listings for the given {@link CommandSender}.
	 * 
	 * @param sender
	 *            the sender
	 * @return <code>true</code> if the given {@link CommandSender} might be allowed to execute this
	 *         command
	 */
	public boolean testPermission(CommandSender sender) {
		Validate.notNull(sender, "sender is null");
		return (permission != null) ? PermissionUtils.hasPermission(sender, permission) : true;
	}

	/**
	 * Similar to {@link Command#testPermission(CommandSender)}, but throws an exception with
	 * feedback message in case the given {@link CommandSender} is not allowed to execute this
	 * command.
	 * 
	 * @param sender
	 *            the sender
	 * @throws NoPermissionException
	 *             if the sender is not allowed to execute this command
	 */
	public void checkPermission(CommandSender sender) throws NoPermissionException {
		Validate.notNull(sender, "sender is null");
		if (!this.testPermission(sender)) {
			throw this.noPermissionException();
		}
	}

	/**
	 * Checks if the given {@link CommandSender} has the specified permission, and throws a
	 * {@link NoPermissionException} with corresponding error message if not.
	 * 
	 * @param sender
	 *            the sender
	 * @param permission
	 *            the permission, can be <code>null</code>
	 * @throws NoPermissionException
	 *             if the sender does not have the permission
	 */
	public void checkPermission(
			CommandSender sender,
			@Nullable String permission
	) throws NoPermissionException {
		Validate.notNull(sender, "sender is null");
		if (permission != null && !PermissionUtils.hasPermission(sender, permission)) {
			throw this.noPermissionException();
		}
	}

	protected NoPermissionException noPermissionException() {
		return new NoPermissionException(Messages.noPermission);
	}

	/**
	 * Checks whether the given type of {@link CommandSender} is accepted to execute this command.
	 * <p>
	 * This only affects execution of this specific command, and not those of the child commands.
	 * Each child command has to specify its requirements and behavior separately.
	 * <p>
	 * If overridden, it is recommended to also override {@link #checkCommandSource(CommandSender)}
	 * in order to use a more specific error message there in case the command sender cannot use the
	 * command.
	 * 
	 * @param sender
	 *            the command sender
	 * @return <code>true</code> of the command sender is accepted
	 */
	public boolean isAccepted(CommandSender sender) {
		Validate.notNull(sender, "sender is null");
		// By default, all command senders are accepted:
		return true;
	}

	private static final Text MSG_COMMAND_SOURCE_REJECTED = Text.of("You cannot execute this command here!");

	/**
	 * Checks whether the given type of {@link CommandSender} is accepted to execute this command.
	 * <p>
	 * If the {@link CommandSender} is not accepted, the thrown
	 * {@link CommandSourceRejectedException} contains a user-friendly rejection message.
	 * 
	 * @param sender
	 *            the sender, receives feedback
	 * @throws CommandSourceRejectedException
	 *             if the given type of command sender is not accepted to execute this command
	 */
	public void checkCommandSource(CommandSender sender) throws CommandSourceRejectedException {
		Validate.notNull(sender, "sender is null");
		if (!this.isAccepted(sender)) {
			throw new CommandSourceRejectedException(MSG_COMMAND_SOURCE_REJECTED);
		}
	}

	/**
	 * Gets the command format of this command.
	 * <p>
	 * The command format depends on the chain of parent commands and does not include this
	 * command's arguments.<br>
	 * Example: For a command {@code '/mail send <player> <message>'} this would be
	 * {@code '/mail send'}.
	 * 
	 * @return the command format
	 */
	public final String getCommandFormat() {
		if (parent != null) {
			// Append primary alias to the format of the parent:
			return parent.getCommandFormat() + ARGUMENTS_SEPARATOR + this.getName();
		} else {
			// This is a base command:
			return COMMAND_PREFIX + this.getName();
		}
	}

	/**
	 * Gets the arguments of this command.
	 * 
	 * @return an unmodifiable view on the arguments of this command
	 */
	public final List<? extends CommandArgument<?>> getArguments() {
		return Collections.unmodifiableList(arguments);
	}

	/**
	 * Gets the argument with the specified name.
	 * 
	 * @param name
	 *            the argument name
	 * @return the argument, or <code>null</code> if there is no such argument
	 */
	public final @Nullable CommandArgument<?> getArgument(
			@UnknownInitialization(Command.class) Command this,
			String name
	) {
		for (CommandArgument<?> argument : arguments) {
			if (argument.getName().equals(name)) {
				return argument;
			}
		}
		return null;
	}

	/**
	 * Adds an {@link CommandArgument} to this command.
	 * 
	 * @param argument
	 *            the argument
	 */
	protected final void addArgument(
			@UnknownInitialization(Command.class) Command this,
			CommandArgument<?> argument
	) {
		Validate.notNull(argument, "argument is null");
		Validate.isTrue(this.getArgument(argument.getName()) == null,
				"There is already another argument with this name: " + argument.getName());
		Validate.isTrue(argument.getParent() == null, "argument already has a parent");
		// Make sure that no parent can be set once the argument has been added:
		argument.setParent(null); // Parent can only be set once
		arguments.add(argument);
	}

	/**
	 * Gets the arguments format of this command.
	 * <p>
	 * Example: For a command {@code '/message <player> <message>'}, this would be
	 * {@code '<player> <message>'}.<br>
	 * If this command is a child-command, the argument format is meant to only contain the
	 * arguments for this child-command.
	 * <p>
	 * The returned format is empty if this command does not use any arguments, if all arguments are
	 * hidden, or if this command only acts as parent for other commands.
	 * 
	 * @return the arguments format, possibly empty
	 */
	public final String getArgumentsFormat() {
		if (arguments.isEmpty()) return "";
		StringBuilder argumentsFormat = new StringBuilder();
		for (CommandArgument<?> argument : arguments) {
			String argumentFormat = argument.getFormat();
			if (!argumentFormat.isEmpty()) {
				argumentsFormat.append(argumentFormat).append(ARGUMENTS_SEPARATOR);
			}
		}
		if (argumentsFormat.length() == 0) {
			return "";
		} else {
			return argumentsFormat.substring(0, argumentsFormat.length() - ARGUMENTS_SEPARATOR.length());
		}
	}

	/**
	 * Gets the usage format of this command.
	 * <p>
	 * This is basically the command format appended with the arguments format.
	 * 
	 * @return the usage format
	 */
	public final String getUsageFormat() {
		String usageFormat = this.getCommandFormat();
		// Append arguments:
		String argsFormat = this.getArgumentsFormat();
		if (!argsFormat.isEmpty()) {
			usageFormat += ARGUMENTS_SEPARATOR + argsFormat;
		}
		return usageFormat;
	}

	/**
	 * Gets the common message arguments for this {@link Command}.
	 * <p>
	 * This includes:
	 * <ul>
	 * <li>{name}: The command's {@link #getName() name}.
	 * <li>{description}: The command's {@link #getDescription() description}.
	 * <li>{command}: The command's {@link #getCommandFormat() format}.
	 * <li>{usage}: The command's {@link #getUsageFormat() usage format}.
	 * <li>{arguments}: The command's {@link #getArgumentsFormat() arguments format}.
	 * </ul>
	 * 
	 * @return the common message arguments
	 */
	public final MessageArguments getCommonMessageArgs() {
		return commonMessageArgs;
	}

	public final @Nullable Command getParent() {
		return parent;
	}

	// Gets set by the parent command during registration of this command as child-command:
	final void setParent(@Nullable Command parent) {
		this.parent = parent;
	}

	/**
	 * Gets the root command by following the chain of parent commands.
	 * 
	 * @return the root command
	 */
	public final Command getRootCommand() {
		if (parent != null) {
			return parent.getRootCommand();
		} else {
			return this;
		}
	}

	public final CommandRegistry getChildCommands(
			@UnknownInitialization(Command.class) Command this
	) {
		return childCommands;
	}

	/**
	 * Checks if the ArgumentsReader's next argument matches a child command.
	 * <p>
	 * If a matching child command is found, the ArgumentsReader's cursor is moved forward.
	 * 
	 * @param argsReader
	 *            the ArgumentsReader, not <code>null</code>
	 * @return the child command, or <code>null</code> if none was found
	 */
	protected @Nullable Command getChildCommand(ArgumentsReader argsReader) {
		String childCommandAlias = argsReader.peekIfPresent();
		if (childCommandAlias != null) {
			Command childcommand = this.getChildCommands().getCommand(childCommandAlias);
			if (childcommand != null) {
				// Move cursor forward for the successfully used-up argument:
				argsReader.next();
				return childcommand;
			}
		}
		// No matching child-command command was found:
		return null;
	}

	/**
	 * Processes the given inputs and then executes this command.
	 * <p>
	 * Unlike {@link #processCommand(CommandInput)} this includes handling of errors that occur
	 * during command processing.
	 * 
	 * @param input
	 *            the command input, not <code>null</code>
	 */
	public void handleCommand(CommandInput input) {
		Validate.notNull(input, "input is null");
		Validate.isTrue(input.getCommand() == this.getRootCommand(), "input is meant for a different command");

		CommandSender sender = input.getSender();
		CommandContext context = new SimpleCommandContext();
		ArgumentsReader argsReader = new ArgumentsReader(input);
		try {
			this.processCommand(input, context, argsReader);
			Log.debug(DebugOptions.commands, () -> "Command succeeded. Context: " + context);
		} catch (CommandException e) {
			TextUtils.sendMessage(sender, e.getMessageText());

			Log.debug(DebugOptions.commands, () -> {
				StringBuilder sb = new StringBuilder();
				sb.append("Command failed. Argument chain: ");
				CommandArgument<?> argument = null;
				if (e instanceof ArgumentParseException) {
					argument = ((ArgumentParseException) e).getArgument();
				}
				sb.append(this.getArgumentChain(argument));
				return sb.toString();
			});
			Log.debug(DebugOptions.commands, "Command exception: ", e);
			Log.debug(DebugOptions.commands, () -> "Context: " + context);
			Log.debug(DebugOptions.commands, () -> "ArgumentsReader: " + argsReader);
		} catch (Exception e) {
			// An unexpected exception was caught:
			TextUtils.sendMessage(
					sender,
					Text.color(ChatColor.RED)
							.text("An error occurred during command handling! Check the console log.")
			);
			Log.severe("An error occurred during command handling!", e);
			Log.severe("Context: " + context);
		}
	}

	private String getArgumentChain(@Nullable CommandArgument<?> argument) {
		if (argument == null) return "-";
		String delimiter = " < ";
		StringBuilder sb = new StringBuilder();
		CommandArgument<?> currentArgument = argument;
		while (currentArgument != null) {
			sb.append(currentArgument.getClass().getName());
			sb.append(" (");
			sb.append(currentArgument.getName());
			sb.append(")");
			sb.append(delimiter);
			currentArgument = currentArgument.getParent();
		}
		return sb.substring(0, sb.length() - delimiter.length());
	}

	/**
	 * Processes the given inputs and then executes this command.
	 * 
	 * @param input
	 *            the command input, not <code>null</code>
	 * @throws CommandException
	 *             if command execution failed
	 */
	public void processCommand(CommandInput input) throws CommandException {
		Validate.notNull(input, "input is null");
		Validate.isTrue(input.getCommand() == this.getRootCommand(),
				"input is meant for a different command");

		CommandContext context = new SimpleCommandContext();
		ArgumentsReader argsReader = new ArgumentsReader(input);
		this.processCommand(input, context, argsReader);
	}

	/**
	 * Processes the given inputs and then executes this command.
	 * <p>
	 * This handles a few common things before the command gets actually executed, like:
	 * <ul>
	 * <li>Passing the command handling to a matching child-command, otherwise:
	 * <li>Checking if the {@link CommandSender} is accepted.
	 * <li>Checking the command permission for the {@link CommandSender}.
	 * <li>Parsing the command arguments.
	 * <li>And finally executing this command via
	 * {@link #execute(CommandInput, CommandContextView)}.
	 * </ul>
	 * 
	 * @param input
	 *            the command input, not <code>null</code>
	 * @throws CommandException
	 *             if command execution failed
	 */
	protected void processCommand(
			CommandInput input,
			CommandContext context,
			ArgumentsReader argsReader
	) throws CommandException {
		assert input != null && context != null && argsReader != null;
		// The input is meant for this command:
		assert (input.getCommand() == this.getRootCommand());
		// ArgumentsReader is consistent with the input:
		assert (argsReader.getArgs() == input.getArguments());

		// Search for matching child-command:
		Command childCommand = this.getChildCommand(argsReader);
		if (childCommand != null) {
			// Delegate to child-command:
			childCommand.processCommand(input, context, argsReader);
		} else { // No applicable child-command was found.
			CommandSender sender = input.getSender();
			// Check if this type of command sender supported:
			this.checkCommandSource(sender);

			// Check if the command sender has the required permission to proceed:
			this.checkPermission(sender);

			// Parse arguments:
			this.parseArguments(input, context, argsReader);

			// Execute this command:
			this.execute(input, context.getView());
		}
	}

	// PARSING

	/**
	 * Used internally to keep track of the parsing state.
	 */
	protected static class ParsingContext {

		public final CommandInput input;
		public final CommandContext rootContext;
		public CommandContext context; // The currently active context
		public final ArgumentsReader argsReader;
		public final int argumentsCount;

		public int currentArgumentIndex = 0;
		public @Nullable ArgumentParseException currentParseException = null;

		// The first pending fallback.
		// See handleFallbacks for how this fallback is eventually processed.
		// Recursively holds references to subsequent fallbacks that are evaluated from front to
		// back if the parsing of the subsequent command arguments succeeds. If the parsing of the
		// subsequent command arguments failed but this first fallback succeeds, the parsing is
		// restarted from this point and the fallbacks for subsequent command arguments are
		// disregarded.
		private @Nullable Fallback pendingFallback = null;
		// If present and parsing fails, this exception overrides any other exception that would
		// otherwise be thrown.
		// This exception is usually associated with an earlier command argument that initially
		// failed to parse but was later able to restart the parsing due to a successful fallback.
		// This exception is usually expected to be a more relevant root cause of why the parsing
		// eventually failed.
		private @Nullable ArgumentParseException overrideParseException = null;
		private int overrideParseExceptionArgumentIndex = -1;

		protected ParsingContext(
				CommandInput input,
				CommandContext context,
				ArgumentsReader argsReader,
				int argumentsCount
		) {
			assert input != null && context != null && argsReader != null && argumentsCount >= 0;
			this.input = input;
			this.rootContext = context;
			this.context = context;
			this.argsReader = argsReader;
			this.argumentsCount = argumentsCount;
		}

		public boolean hasPendingFallback() {
			return (pendingFallback != null);
		}

		public @Nullable Fallback getPendingFallback() {
			return pendingFallback;
		}

		public boolean hasUnparsedCommandArguments() {
			return (currentArgumentIndex < (argumentsCount - 1));
		}

		public void setOverrideParseException(
				int argumentIndex,
				@Nullable ArgumentParseException overrideParseException
		) {
			// The currently set override parse exception cannot be replaced by later command
			// arguments.
			if (this.overrideParseException != null && argumentIndex > overrideParseExceptionArgumentIndex) {
				return;
			}
			this.overrideParseException = overrideParseException;
			this.overrideParseExceptionArgumentIndex = argumentIndex;
		}

		/**
		 * Appends the given {@link Fallback} to the end of the list of pending fallbacks.
		 * 
		 * @param fallback
		 *            the fallback to append
		 */
		public void appendFallback(Fallback fallback) {
			if (pendingFallback == null) {
				pendingFallback = fallback;
			} else {
				assert pendingFallback != null;
				pendingFallback.appendFallback(fallback);
			}
		}
	}

	protected static class Fallback {

		protected final int argumentIndex;

		private final FallbackArgumentException exception;
		private final BufferedCommandContext bufferedContext;
		private final ArgumentsReader originalArgsReader; // Snapshot from before the fallback

		// Linked list of subsequent fallbacks:
		private @Nullable Fallback nextPendingFallback;

		protected Fallback(
				int argumentIndex,
				FallbackArgumentException exception,
				BufferedCommandContext bufferedContext,
				ArgumentsReader originalArgsReader
		) {
			assert exception != null && bufferedContext != null && originalArgsReader != null;
			this.argumentIndex = argumentIndex;
			this.exception = exception;
			this.bufferedContext = bufferedContext;
			this.originalArgsReader = originalArgsReader;
		}

		/**
		 * Gets the {@link FallbackArgumentException} that initiated this fallback.
		 * 
		 * @return the fallback argument exception
		 */
		public FallbackArgumentException getException() {
			return exception;
		}

		/**
		 * Gets the {@link FallbackArgument} that created the exception and may be able to provide a
		 * fallback.
		 * 
		 * @return the fallback argument
		 */
		public FallbackArgument<?> getFallbackArgument() {
			return exception.getArgument();
		}

		/**
		 * Gets the {@link BufferedCommandContext} that tracks context changes that happen while the
		 * fallback is pending.
		 * 
		 * @return the buffered command context
		 */
		public BufferedCommandContext getBufferedContext() {
			return bufferedContext;
		}

		/**
		 * Gets the {@link ArgumentsReader} at the state from before the fallback.
		 * 
		 * @return the ArgumentsReader at the state from before the fallback
		 */
		public ArgumentsReader getOriginalArgsReader() {
			return originalArgsReader;
		}

		/**
		 * Appends the given {@link Fallback} to the end of the list of pending fallbacks.
		 * 
		 * @param fallback
		 *            the fallback to append
		 */
		public void appendFallback(Fallback fallback) {
			Fallback end = this;
			Fallback next = nextPendingFallback;
			while (next != null) {
				end = next;
				next = end.nextPendingFallback;
			}
			end.nextPendingFallback = fallback;
		}

		public @Nullable Fallback getNextFallback() {
			return nextPendingFallback;
		}
	}

	/**
	 * Parses all command arguments for this command.
	 * 
	 * @param input
	 *            the input, not <code>null</code>
	 * @param context
	 *            the command context to store the parsed values in, not <code>null</code>
	 * @param argsReader
	 *            the ArgumentsReader, not <code>null</code>
	 * @throws ArgumentParseException
	 *             if a required argument cannot be parsed or there are unparsed remaining arguments
	 */
	protected void parseArguments(
			CommandInput input,
			CommandContext context,
			ArgumentsReader argsReader
	) throws ArgumentParseException {
		// Setup parsing context:
		int argumentsCount = arguments.size();
		ParsingContext parsingContext = new ParsingContext(
				input,
				context,
				argsReader,
				argumentsCount
		);

		// Parse all arguments:
		for (; parsingContext.currentArgumentIndex < argumentsCount; ++parsingContext.currentArgumentIndex) {
			CommandArgument<?> argument = arguments.get(parsingContext.currentArgumentIndex);

			// Parse argument:
			this.parseArgument(argument, parsingContext);

			// Handle fallback(s) (if any):
			this.handleFallbacks(parsingContext);

			ArgumentParseException parseException = parsingContext.currentParseException;
			if (parseException != null) {
				// Parsing failed at the current argument.
				// Check for an override parse exception:
				if (parsingContext.overrideParseException != null) {
					throw parsingContext.overrideParseException;
				} else {
					throw parseException;
				}
			} else {
				// Parsing successful, continue with the next argument (if any)
			}
		}

		// Handle unparsed arguments (if any):
		this.handleUnparsedArguments(parsingContext);

		// Parsing succeeded.
	}

	protected void parseArgument(CommandArgument<?> argument, ParsingContext parsingContext) {
		CommandContext context = parsingContext.context;
		ArgumentsReader argsReader = parsingContext.argsReader;

		ArgumentsReader argsReaderState = argsReader.createSnapshot();
		ArgumentParseException parseException = null;
		try {
			// On success this stores any parsed values inside the context:
			argument.parse(parsingContext.input, context, argsReader);
		} catch (FallbackArgumentException e) {
			Log.debug(DebugOptions.commands,
					() -> "Fallback for argument '" + argument.getName() + "': " + e.getMessage());
			argsReader.setState(argsReaderState); // Restore previous args reader state

			// Keep track of context changes while continuing with the pending fallback:
			BufferedCommandContext bufferedContext = new BufferedCommandContext(context);
			parsingContext.context = bufferedContext;

			// Append fallback:
			Fallback fallback = new Fallback(
					parsingContext.currentArgumentIndex,
					e,
					bufferedContext,
					argsReaderState
			);
			parsingContext.appendFallback(fallback);
		} catch (ArgumentParseException e) { // Parsing failed
			argsReader.setState(argsReaderState); // Restore previous args reader state
			parseException = e;
		}
		assert !(parseException instanceof FallbackArgumentException);
		// Resets to null on success or fallback:
		parsingContext.currentParseException = parseException;
	}

	protected void handleFallbacks(ParsingContext parsingContext) throws ArgumentParseException {
		Fallback fallback = parsingContext.getPendingFallback();
		if (fallback == null) {
			// No pending fallback(s):
			return;
		}

		// Note: We attempt to fully parse all command arguments after the fallback before
		// evaluating the fallback.
		// Evaluating the fallback right after the first subsequent command argument either failed
		// or was able to parse arguments is not sufficient.
		// Consider for example the command "/list [player] [page]" with inputs "/list 2" and "/list
		// 123 2": If the first argument in the input doesn't match any online player, a fallback is
		// used to check if the argument matches a page number instead (first case) and then the
		// executing player is used as convenient fallback value.
		// However, this heuristic fails in the second case, because numbers can (in the unlikely
		// case) also be valid player names. In order to keep using the heuristic fallback mechanism
		// (which works fine most of the time), but still be able to correctly parse the second
		// example, where both arguments are specified explicitly, we need to continue parsing past
		// the page argument and detect the 'unexpected argument'. We can then use a fallback that
		// accepts any input for the player name.

		// Evaluate the pending fallback if parsing of the current command argument failed (without
		// providing a fallback itself), or if there are no more command arguments to parse.
		// Otherwise continue parsing:
		ArgumentParseException parseException = parsingContext.currentParseException;
		assert !(parseException instanceof FallbackArgumentException);
		boolean currentParsingFailed = (parseException != null);
		boolean hasUnparsedCommandArguments = parsingContext.hasUnparsedCommandArguments();
		if (!currentParsingFailed && hasUnparsedCommandArguments) {
			return; // Continue parsing the next argument
		} // Else: Continue here, evaluating the pending fallback.
		assert currentParsingFailed || !hasUnparsedCommandArguments;

		// Parsing past the fallback failed if either the current command argument failed, or if
		// there are unparsed remaining arguments:
		ArgumentsReader argsReader = parsingContext.argsReader;
		boolean parsingFailed = (currentParsingFailed || argsReader.hasNext());

		// Reset the pending fallback:
		parsingContext.pendingFallback = null;

		// Reset the context to the state before the fallback:
		// Since the currently evaluated fallback is always the first pending fallback, we can use
		// the root context here, since any previous fallbacks have already applied their changes to
		// the root context.
		// We cannot use the fallback's buffer's parent context, since the parent can be the
		// buffered context of an earlier fallback that was already evaluated and applied, so any
		// new additions to this parent buffered context will not be applied to the root context.
		parsingContext.context = parsingContext.rootContext;

		// Note: If some command argument after the fallback was able to parse something, the args
		// reader might no longer match the state from before the fallback. If parsing past the
		// fallback failed, reset the args reader to the state from before the fallback:
		// Capture current args reader state:
		ArgumentsReader prevArgsState = argsReader.createSnapshot();
		if (parsingFailed) {
			argsReader.setState(fallback.getOriginalArgsReader());
		} else {
			// Parsing past the fallback succeeded. -> There are no remaining arguments for the
			// fallback argument to consume.
			assert !currentParsingFailed && !hasUnparsedCommandArguments && !argsReader.hasNext();
		}

		// Reset the override parse exception if the index of the current fallback argument is
		// smaller than the index of the command argument that previously set the override parse
		// exception:
		parsingContext.setOverrideParseException(fallback.argumentIndex, null);

		// Parse fallback:
		FallbackArgument<?> fallbackArgument = fallback.getFallbackArgument();
		ArgumentParseException fallbackError = null;
		// hasRemainingArgs: Only the case if args got reset and there were remaining args
		// originally as well.
		boolean hasRemainingArgs = argsReader.hasNext();
		ArgumentsReader argsReaderState = argsReader.createSnapshot();
		try {
			// On success this stores any parsed values inside the context:
			fallbackArgument.parseFallback(
					parsingContext.input,
					parsingContext.context,
					argsReader,
					fallback.exception,
					parsingFailed
			);
		} catch (FallbackArgumentException e) {
			// Fallback is not allowed to throw another fallback exception here.
			Validate.State.error("Argument '" + fallbackArgument.getName()
					+ "' threw another FallbackArgumentException while parsing fallback: " + e);
		} catch (ArgumentParseException e) { // Fallback failed
			argsReader.setState(argsReaderState); // Restore previous args reader state
			fallbackError = e;
		}
		boolean fallbackConsumedArgs = (argsReaderState.getCursor() != argsReader.getCursor());
		// Assumption: args state got restored (no args consumed) if parsing failed.
		assert (fallbackError == null) || !fallbackConsumedArgs;

		if (hasRemainingArgs && !fallbackConsumedArgs) {
			// There are arguments remaining but the fallback did not consume any of them.
			// Unless the fallback succeeded and restarting the parsing from the current argument
			// would now succeed as well, it is very likely that continuing would either use the
			// fallback error (if the fallback failed), or the parsing error that lead to the
			// evaluation of the current fallback (otherwise there would be no remaining args).
			// In most cases, however, we will want to use the original (root) parsing error of the
			// current fallback argument instead. Consider for example the command "/list [player,
			// default: self] [page, default: 1]" with input "/list a 2", 'a' being an invalid
			// player name: The player name fallback is evaluated as result of an 'invalid page
			// argument' error, and the fallback will produce 'self' as the default value. Simply
			// restarting the parsing from here would result in the 'invalid page argument' error to
			// be propagated.
			// Instead, by remembering the original root parsing exception and using it if the
			// following page argument still fails to parse, we get the expected 'invalid player'
			// error in this case.

			// However, simply aborting the parsing here right away with the root parsing exception
			// is not an option:
			// There are cases in which the following command arguments depend on context provided
			// by the current fallback. So if the fallback succeeded, even if it did not consume any
			// arguments, we need to restart the parsing in order to check if the following command
			// arguments are now able to successfully parse the remaining arguments.

			if (fallbackError != null) {
				// Use the original root exception in place of the fallback error:
				fallbackError = fallback.exception.getRootException();
			} else {
				// Remember the original root exception before we restart the parsing. If the
				// parsing still ends up failing, this root exception will replace any other parsing
				// exception that would otherwise be thrown.
				parsingContext.setOverrideParseException(
						fallback.argumentIndex,
						fallback.exception.getRootException()
				);
			}
		}

		if (fallbackError != null) {
			// Fallback argument failed.
			parsingContext.currentArgumentIndex = fallback.argumentIndex;
			parsingContext.currentParseException = fallbackError;
			// Return: This will abort the parsing with the currentParseException.
			return;
		}

		// The fallback successfully parsed something.
		// Heuristic: If the following arguments did previously already successfully parse with no
		// arguments remaining, we assume that they do not depend on the new context provided by the
		// current fallback and that restarting the parsing would result in the same outcome as
		// before. We can therefore apply the previously captured state and skip parsing these
		// arguments again.
		// TODO One can easily construct examples in which this heuristic fails (e.g. if the context
		// is optional, but would result in a different outcome when present). Remove this
		// heuristic?
		if (!parsingFailed) {
			// Apply buffered context changes that happened after the fallback argument to the
			// current parsing context (i.e. the root context):
			// Note: Applying the buffered changes to the buffer's parent context is not sufficient,
			// because the parent can itself be a buffered context of an earlier fallback: Since all
			// parent fallbacks have already been evaluated and applied, any new additions to the
			// parent buffered context are not getting applied to the root context.
			fallback.getBufferedContext().applyBuffer(parsingContext.context);
			// Restore the previous args state (from before the evaluation of this fallback):
			argsReader.setState(prevArgsState);

			// If there are other pending fallbacks, evaluate them. Otherwise, this leads to parsing
			// success.
			parsingContext.pendingFallback = fallback.getNextFallback();
			this.handleFallbacks(parsingContext);
			return;
		}

		// Freshly restart the parsing from the next command argument after the fallback. Any
		// fallbacks and buffered context changes for subsequent command arguments are disregarded.
		// Note: Even if the fallback did not consume any arguments and parsing of the following
		// command arguments previously failed, the parsing might succeed now if the following
		// command arguments depend on context that is provided by the current fallback.
		parsingContext.currentArgumentIndex = fallback.argumentIndex;
		parsingContext.currentParseException = null;
	}

	private void handleUnparsedArguments(ParsingContext parsingContext) throws ArgumentParseException {
		ArgumentsReader argsReader = parsingContext.argsReader;
		if (argsReader.getRemainingSize() == 0) return; // No unparsed arguments

		// Check for an override exception:
		if (parsingContext.overrideParseException != null) {
			throw parsingContext.overrideParseException;
		}

		// Remaining unexpected/unparsed arguments:
		String firstUnparsedArg = argsReader.peek();
		if (!this.getChildCommands().getCommands().isEmpty()) {
			// Has child commands: Throw an 'unknown command' exception.
			// TODO Only use this exception if no arguments got parsed by this command?
			throw new ArgumentParseException(null, this.getUnknownCommandMessage(firstUnparsedArg));
		} else {
			// Throw an 'invalid argument' exception for the first unparsed argument after the last
			// parsed argument:
			CommandContext context = parsingContext.context;
			CommandArgument<?> firstUnparsedArgument = null;
			ListIterator<CommandArgument<?>> argumentsIter = arguments.listIterator(arguments.size());
			while (argumentsIter.hasPrevious()) {
				@Nullable CommandArgument<?> argument = argumentsIter.previous();
				assert argument != null;
				if (!context.has(argument.getName())) {
					firstUnparsedArgument = argument;
				} else {
					break; // Stop at the first (from behind) found parsed argument.
				}
			}
			if (firstUnparsedArgument != null) {
				throw firstUnparsedArgument.invalidArgumentError(firstUnparsedArg);
			} else {
				// Throw an 'unexpected argument' exception:
				Text errorMsg = Messages.commandArgumentUnexpected;
				errorMsg.setPlaceholderArguments("argument", firstUnparsedArg);
				throw new ArgumentParseException(null, errorMsg);
			}
		}
	}

	protected Text getUnknownCommandMessage(String command) {
		Text text = Messages.commandUnknown;
		text.setPlaceholderArguments("command", command);
		return text;
	}

	//

	/**
	 * Executes this specific command.
	 * <p>
	 * By default this simply calls {@link #sendHelp(CommandSender)} when executed. Override this
	 * method for custom command behavior.
	 * 
	 * @param input
	 *            the command input, not <code>null</code>
	 * @param context
	 *            the context containing the parsed argument values, not <code>null</code>
	 * @throws CommandException
	 *             if command execution failed
	 */
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		// Default command behavior: Print command help information.
		this.sendHelp(input.getSender());
	}

	/**
	 * Gets tab completion suggestions for the last (possibly partial or empty) argument of the
	 * given input.
	 * 
	 * @param input
	 *            the command input, not <code>null</code>
	 * @return the suggestions for the final argument, or an empty list to indicate 'no suggestions'
	 *         (not <code>null</code> and not containing <code>null</code>)
	 */
	public List<? extends String> handleTabCompletion(CommandInput input) {
		Validate.notNull(input, "input is null");
		Validate.isTrue(input.getCommand() == this.getRootCommand(),
				"input is meant for a different command");

		CommandContext commandContext = new SimpleCommandContext();
		ArgumentsReader argsReader = new ArgumentsReader(input);
		return this.handleTabCompletion(input, commandContext, argsReader);
	}

	/**
	 * Gets tab completion suggestions for the last (possibly partial or empty) argument of the
	 * given input.
	 * 
	 * @param input
	 *            the command input, not <code>null</code>
	 * @param context
	 *            the context, not <code>null</code>
	 * @param argsReader
	 *            the ArgumentsReader, not <code>null</code>
	 * @return the suggestions for the final argument, or an empty list to indicate 'no suggestions'
	 *         (not <code>null</code> and not containing <code>null</code>)
	 */
	protected List<? extends String> handleTabCompletion(
			CommandInput input,
			CommandContext context,
			ArgumentsReader argsReader
	) {
		assert input != null && context != null && argsReader != null;
		// The input is meant for this command:
		assert (input.getCommand() == this.getRootCommand());
		// ArgumentsReader is consistent with the input:
		assert (argsReader.getArgs() == input.getArguments());

		// Search for matching child-command:
		Command childCommand = this.getChildCommand(argsReader);
		if (childCommand != null) {
			// Delegate to child-command:
			return childCommand.handleTabCompletion(input, context, argsReader);
		}

		// No applicable child-command was found:
		CommandSender sender = input.getSender();
		// Check if this type of command sender supported:
		if (!this.isAccepted(sender)) {
			// Not supported type of command sender:
			return Collections.emptyList();
		}

		// Check if this command sender has the required permission to proceed:
		if (!this.testPermission(sender)) {
			// Command sender not allowed:
			return Collections.emptyList();
		}

		List<String> suggestions = new ArrayList<>();
		if (argsReader.getRemainingSize() == 1) {
			String finalArgument = CommandUtils.normalize(argsReader.peek());
			// Include matching child-command aliases (max one per command):
			// Asserts that the aliases-map provides all aliases for the same command in succession.
			Command lastMatchingCommand = null;
			for (Entry<? extends String, ? extends Command> aliasEntry : this.getChildCommands().getAliasesMap().entrySet()) {
				String alias = aliasEntry.getKey(); // Normalized
				Command aliasCommand = aliasEntry.getValue();
				if (lastMatchingCommand != null && lastMatchingCommand == aliasCommand) {
					// We have already included a suggestion for this child command, skip:
					continue;
				}
				// We have reached an alias for a new child command:
				lastMatchingCommand = null;

				// Does the alias match the input?
				if (alias.startsWith(finalArgument)) {
					// Exclude further aliases for this command:
					lastMatchingCommand = aliasCommand;

					// Check if recipient even has required permission for this command:
					if (!aliasCommand.testPermission(sender)) {
						// Missing permission for this command, skip:
						continue;
					}

					// Add this alias to the suggestions:
					// TODO Maybe use the original alias here, and not the normalized one?
					suggestions.add(alias);
				}
			}
		}

		// Parse and complete arguments:
		CommandContextView contextView = context.getView();
		for (CommandArgument<?> argument : arguments) {
			int remainingArgs = argsReader.getRemainingSize();
			if (remainingArgs == 0) {
				// No argument left that could be completed:
				break;
			}
			ArgumentsReader argsReaderState = argsReader.createSnapshot();
			try {
				argument.parse(input, context, argsReader);
				// Successfully parsed:
				if (!argsReader.hasNext()) {
					// This consumed the last argument:
					// Reset args reader and provide alternative completions for the last argument
					// instead:
					argsReader.setState(argsReaderState);
					suggestions.addAll(argument.complete(input, contextView, argsReader));
					break;
				} else if (argsReader.getRemainingSize() == remainingArgs) {
					// No error during parsing, but none of the remaining args used up:
					// -> This is an optional argument that was skipped.
					// Include suggestions (if it has any), but continue:
					suggestions.addAll(argument.complete(input, contextView, argsReader));

					// Reset args reader and then let the following arguments also try to complete
					// the same arg(s):
					argsReader.setState(argsReaderState);
					continue;
				}
			} catch (FallbackArgumentException e) {
				// Parsing failed, but registered a fallback:
				// Check for completions, but continue parsing.
				// TODO Also include suggestions for the following arguments with the fallback
				// evaluated? (E.g. if the following arguments depend on context provided by the
				// fallback argument).
				argsReader.setState(argsReaderState);
				suggestions.addAll(argument.complete(input, contextView, argsReader));
				argsReader.setState(argsReaderState);
				continue;
			} catch (ArgumentParseException e) {
				if (argument.getReducedFormat().isEmpty()) {
					// Argument is hidden, check for completions but continue parsing:
					argsReader.setState(argsReaderState);
					suggestions.addAll(argument.complete(input, contextView, argsReader));
					argsReader.setState(argsReaderState);
					continue;
				} else {
					// Parsing might have failed because of an invalid partial last argument.
					// -> Check for and include suggestions.
					argsReader.setState(argsReaderState);
					suggestions.addAll(argument.complete(input, contextView, argsReader));
					// Parsing might also have failed because of an invalid argument inside the
					// sequence of arguments.
					// -> Skip later arguments (current argument will not provide suggestions in
					// that case, because it isn't using up the last argument).
					break;
				}
			}
		}

		return Collections.unmodifiableList(suggestions);
	}

	// Help page related:

	/**
	 * Checks whether the child-commands of this {@link Command} are included in the parent's help
	 * content.
	 * <p>
	 * By default only direct child commands are included in the help content.
	 * 
	 * @return <code>true</code> if child-commands are included in the parent's help content
	 */
	public final boolean isIncludeChildsInParentHelp() {
		return includeChildsInParentHelp;
	}

	/**
	 * Sets whether the child-commands of this {@link Command} are included in the parent's help
	 * content.
	 * <p>
	 * By default only direct child commands are included in the help content.
	 * 
	 * @param includeChilds
	 *            <code>true</code> to include the child commands of this command in the parent's
	 *            help content
	 */
	protected final void setIncludeChildsInParentHelp(
			@UnknownInitialization Command this,
			boolean includeChilds
	) {
		this.includeChildsInParentHelp = includeChilds;
	}

	/**
	 * Checks whether this {@link Command} is hidden in the parent's help contents.
	 * <p>
	 * Hidden commands won't show up on the help pages. Tab completion however is not affected by
	 * this.
	 * 
	 * @return <code>true</code> if this command is hidden in the parent's help contents
	 */
	public final boolean isHiddenInParentHelp() {
		return hiddenInParentHelp;
	}

	/**
	 * Sets whether this {@link Command} is hidden in the parent's help contents.
	 * <p>
	 * Hidden commands won't show up on the help pages. Tab completion however is not affected by
	 * this.
	 * 
	 * @param hiddenInParentHelp
	 *            <code>true</code> to exclude this command in the parent's help contents
	 */
	protected final void setHiddenInParentHelp(
			@UnknownInitialization Command this,
			boolean hiddenInParentHelp
	) {
		this.hiddenInParentHelp = hiddenInParentHelp;
	}

	/**
	 * Checks whether this {@link Command} is hidden in its own help page.
	 * <p>
	 * Hidden commands won't show up on the help pages. Tab completion however is not affected by
	 * this.
	 * 
	 * @return <code>true</code> if this command is hidden in its own help page
	 */
	public final boolean isHiddenInOwnHelp() {
		return hiddenInOwnHelp;
	}

	/**
	 * Sets whether this {@link Command} is hidden in its own help page.
	 * <p>
	 * Hidden commands won't show up on the help pages. Tab completion however is not affected by
	 * this.
	 * 
	 * @param hiddenInOwnHelp
	 *            <code>true</code> to exclude this command in its own help contents
	 */
	protected final void setHiddenInOwnHelp(
			@UnknownInitialization Command this,
			boolean hiddenInOwnHelp
	) {
		this.hiddenInOwnHelp = hiddenInOwnHelp;
	}

	// Help page formatting:

	/**
	 * Sets the format to use for the title when sending the help via
	 * {@link #sendHelp(CommandSender)}.
	 * <p>
	 * See {@link #getCommonMessageArgs()} for the available placeholders.
	 * <p>
	 * If the format is {@link Text#isPlainTextEmpty() empty}, no title will be used in the help
	 * pages. If the format is <code>null</code>, the format of the parent command gets used. If no
	 * parent is available or the parent format is {@link Text#isPlainTextEmpty() empty}, a default
	 * format gets used.
	 * 
	 * @param helpTitleFormat
	 *            the format
	 */
	protected void setHelpTitleFormat(@UnknownInitialization Command this, Text helpTitleFormat) {
		this.helpTitleFormat = helpTitleFormat;
	}

	/**
	 * Sets the format to use for this command's usage when sending the help via
	 * {@link #sendHelp(CommandSender)}.
	 * <p>
	 * See {@link #getCommonMessageArgs()} for the available placeholders.
	 * <p>
	 * If the format is {@link Text#isPlainTextEmpty() empty}, no command usage will be printed in
	 * the help pages. If the format is <code>null</code>, the format of the parent command gets
	 * used. If no parent is available or the parent format is {@link Text#isPlainTextEmpty()
	 * empty}, a default format gets used.
	 * 
	 * @param helpUsageFormat
	 *            the format
	 */
	protected void setHelpUsageFormat(@UnknownInitialization Command this, Text helpUsageFormat) {
		this.helpUsageFormat = helpUsageFormat;
	}

	/**
	 * Sets the format to use for this command's description when sending the help via
	 * {@link #sendHelp(CommandSender)}.
	 * <p>
	 * See {@link #getCommonMessageArgs()} for the available placeholders.
	 * <p>
	 * If the format is {@link Text#isPlainTextEmpty() empty}, no command description will be
	 * printed in the help pages. If the format is <code>null</code>, the format of the parent
	 * command gets used. If no parent is available or the parent format is
	 * {@link Text#isPlainTextEmpty() empty}, a default format gets used.
	 * 
	 * @param helpDescFormat
	 *            the format
	 */
	protected void setHelpDescFormat(@UnknownInitialization Command this, Text helpDescFormat) {
		this.helpDescFormat = helpDescFormat;
	}

	/**
	 * Sets the format to use for a child-command's usage when sending the help via
	 * {@link #sendHelp(CommandSender)}.
	 * <p>
	 * See {@link #getCommonMessageArgs()} for the available placeholders.
	 * <p>
	 * If the format is {@link Text#isPlainTextEmpty() empty}, no child commands will be included in
	 * the help pages. If the format is <code>null</code>, the format of the parent command gets
	 * used. If no parent is available or the parent format is {@link Text#isPlainTextEmpty()
	 * empty}, a default format gets used.
	 * 
	 * @param helpChildUsageFormat
	 *            the format
	 */
	protected void setHelpChildUsageFormat(
			@UnknownInitialization Command this,
			Text helpChildUsageFormat
	) {
		this.helpChildUsageFormat = helpChildUsageFormat;
	}

	/**
	 * Sets the format to use for a child-command's description when sending the help via
	 * {@link #sendHelp(CommandSender)}.
	 * <p>
	 * See {@link #getCommonMessageArgs()} for the available placeholders.
	 * <p>
	 * If the format is {@link Text#isPlainTextEmpty() empty}, no child command descriptions will be
	 * included in the help pages. If the format is <code>null</code>, the format of the parent
	 * command gets used. If no parent is available or the parent format is
	 * {@link Text#isPlainTextEmpty() empty}, a default format gets used.
	 * 
	 * @param helpChildDescFormat
	 *            the format
	 */
	protected void setHelpChildDescFormat(
			@UnknownInitialization Command this,
			Text helpChildDescFormat
	) {
		this.helpChildDescFormat = helpChildDescFormat;
	}

	/**
	 * Gets the format to use for the title when sending the help via
	 * {@link #sendHelp(CommandSender)}.
	 * 
	 * @return the format, not <code>null</code>
	 * @see #setHelpTitleFormat(Text)
	 */
	protected final Text getHelpTitleFormat() {
		Text format = this.helpTitleFormat;
		if (format == null) {
			Text parentFormat;
			if (parent != null && !(parentFormat = parent.getHelpTitleFormat()).isPlainTextEmpty()) {
				format = parentFormat;
			} else {
				// Default:
				format = DEFAULT_HELP_TITLE_FORMAT;
			}
		}
		assert format != null;
		return format;
	}

	/**
	 * Gets the format to use for this command's usage when sending the help via
	 * {@link #sendHelp(CommandSender)}.
	 * 
	 * @return the format, not <code>null</code>
	 * @see #setHelpUsageFormat(Text)
	 */
	protected final Text getHelpUsageFormat() {
		Text format = this.helpUsageFormat;
		if (format == null) {
			Text parentFormat;
			if (parent != null && !(parentFormat = parent.getHelpUsageFormat()).isPlainTextEmpty()) {
				format = parentFormat;
			} else {
				// Default:
				format = DEFAULT_HELP_USAGE_FORMAT;
			}
		}
		assert format != null;
		return format;
	}

	/**
	 * Gets the format to use for this command's description when sending the help via
	 * {@link #sendHelp(CommandSender)}.
	 * 
	 * @return the format, not <code>null</code>
	 * @see #setHelpDescFormat(Text)
	 */
	protected final Text getHelpDescFormat() {
		Text format = this.helpDescFormat;
		if (format == null) {
			Text parentFormat;
			if (parent != null && !(parentFormat = parent.getHelpDescFormat()).isPlainTextEmpty()) {
				format = parentFormat;
			} else {
				// Default:
				format = DEFAULT_HELP_DESC_FORMAT;
			}
		}
		assert format != null;
		return format;
	}

	/**
	 * Gets the format to use for a child-command's usage when sending the help via
	 * {@link #sendHelp(CommandSender)}.
	 * 
	 * @return the format, not <code>null</code>
	 * @see #setHelpChildUsageFormat(Text)
	 */
	protected final Text getHelpChildUsageFormat() {
		Text format = this.helpChildUsageFormat;
		if (format == null) {
			Text parentFormat;
			if (parent != null && !(parentFormat = parent.getHelpChildUsageFormat()).isPlainTextEmpty()) {
				format = parentFormat;
			} else {
				// Default: Use this command's help usage format.
				format = this.getHelpUsageFormat();
			}

		}
		assert format != null;
		return format;
	}

	/**
	 * Gets the format to use for a child-command's description when sending the help via
	 * {@link #sendHelp(CommandSender)}.
	 * 
	 * @return the format, not <code>null</code>
	 * @see #setHelpChildDescFormat(Text)
	 */
	protected final Text getHelpChildDescFormat() {
		Text format = this.helpChildDescFormat;
		if (format == null) {
			Text parentFormat;
			if (parent != null && !(parentFormat = parent.getHelpChildDescFormat()).isPlainTextEmpty()) {
				format = parentFormat;
			} else {
				// Default: Use this command's help description format.
				format = this.getHelpDescFormat();
			}
		}
		assert format != null;
		return format;
	}

	/**
	 * Checks if the recipient has the permission to view the help of this command.
	 * <p>
	 * This checks if the recipient has the permission to execute this command or any of the child
	 * commands.
	 * 
	 * @param recipient
	 *            the recipient
	 * @return <code>true</code> if the recipient is allowed to view the help
	 */
	protected final boolean hasHelpPermission(CommandSender recipient) {
		if (this.testPermission(recipient)) return true;
		for (Command childCommand : this.getChildCommands().getCommands()) {
			if (childCommand.hasHelpPermission(recipient)) return true;
		}
		return false;
	}

	/**
	 * Sends usage information about this command and its child-commands to the given
	 * {@link CommandSender}.
	 * 
	 * @param recipient
	 *            the recipient, not <code>null</code>
	 * @throws NoPermissionException
	 *             if the sender is not allowed to view the help of this command
	 */
	public void sendHelp(CommandSender recipient) throws NoPermissionException {
		Validate.notNull(recipient, "recipient is null");

		// Make sure the recipient has the required permission:
		if (!this.hasHelpPermission(recipient)) {
			throw this.noPermissionException();
		}

		MessageArguments commonMsgArgs = this.getCommonMessageArgs();

		// Title:
		Text titleFormat = this.getHelpTitleFormat();
		assert titleFormat != null;
		if (!titleFormat.isPlainTextEmpty()) {
			TextUtils.sendMessage(recipient, titleFormat, commonMsgArgs);
		}

		// Skip info about the command if it is hidden or the recipient does not have the required
		// permission:
		if (!this.isHiddenInOwnHelp() && this.testPermission(recipient)) {
			// Command usage:
			Text usageFormat = this.getHelpUsageFormat();
			assert usageFormat != null;
			usageFormat.setPlaceholderArguments(commonMsgArgs);

			// Command description:
			@NonNull Text descriptionFormat;
			Text description = this.getDescription();
			if (description.isPlainTextEmpty()) {
				descriptionFormat = Text.EMPTY;
			} else {
				descriptionFormat = this.getHelpDescFormat();
				assert descriptionFormat != null;
				descriptionFormat.setPlaceholderArguments(commonMsgArgs);
			}

			Text helpEntryFormat = HELP_ENTRY_FORMAT;
			helpEntryFormat.setPlaceholderArguments(MapUtils.createMap(
					"usage", usageFormat,
					"description", descriptionFormat
			));

			// Skip if both usage and description formats are empty:
			if (!helpEntryFormat.isPlainTextEmpty()) {
				TextUtils.sendMessage(recipient, helpEntryFormat);
			}
		}

		// Include child-commands help:
		Text childUsageFormat = this.getHelpChildUsageFormat();
		Text childDescFormat = this.getHelpChildDescFormat();
		this.sendChildCommandsHelp(recipient, childUsageFormat, childDescFormat, this);
	}

	protected void sendChildCommandsHelp(
			CommandSender recipient,
			@Nullable Text childUsageFormat,
			@Nullable Text childDescFormat,
			Command command
	) {
		Validate.notNull(recipient, "recipient is null");
		if (childUsageFormat == null || childUsageFormat.isPlainTextEmpty()) {
			// Not including child commands at all:
			return;
		}
		boolean childDescFormatEmpty = (childDescFormat == null || childDescFormat.isPlainTextEmpty());

		// Print usage and description of child-commands:
		for (Command childCommand : command.getChildCommands().getCommands()) {
			// Skip info about the command if it is hidden or the recipient does not have the
			// required permission:
			if (!childCommand.isHiddenInParentHelp() && childCommand.testPermission(recipient)) {
				MessageArguments childCommonMsgArgs = childCommand.getCommonMessageArgs();

				// Command usage:
				childUsageFormat.setPlaceholderArguments(childCommonMsgArgs);

				// Command description:
				Text childDescriptionFormat;
				Text childDescription = childCommand.getDescription();
				if (childDescFormatEmpty || childDescription.isPlainTextEmpty()) {
					childDescriptionFormat = Text.EMPTY;
				} else {
					childDescriptionFormat = Unsafe.assertNonNull(childDescFormat);
					childDescriptionFormat.setPlaceholderArguments(childCommonMsgArgs);
				}

				Text helpEntryFormat = HELP_ENTRY_FORMAT;
				assert childUsageFormat != null;
				helpEntryFormat.setPlaceholderArguments(MapUtils.<String, Object>createMap(
						"usage", childUsageFormat,
						"description", childDescriptionFormat
				));

				TextUtils.sendMessage(recipient, helpEntryFormat);
			}

			// Optionally include the child-command's child-commands in help content:
			if (childCommand.isIncludeChildsInParentHelp()) {
				this.sendChildCommandsHelp(
						recipient,
						childUsageFormat,
						childDescFormat,
						childCommand
				);
			}
		}
	}
}
