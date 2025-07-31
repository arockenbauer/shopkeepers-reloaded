package com.nisovin.shopkeepers.text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.text.MessageArguments;

/**
 * A text representation with support for various interaction and formatting features.
 * <p>
 * A text is represented as a chain of {@link Text}s, each linked to its subsequent {@link Text} via
 * its {@link #getNext() next} reference. Each {@link Text} in the chain represents one feature,
 * i.e. {@link PlainText text}, {@link FormattingText formatting}, or interaction features such as
 * {@link HoverEventText hover events}, {@link ClickEventText click events}, or {@link InsertionText
 * insertions}. Formatting and interaction features can be inherited to following {@link Text}s via
 * the {@link #getChild() child} reference, allowing them to encompass a series of {@link Text}s.
 * The next {@link Text} in the chain only inherits the formatting features from the previous
 * {@link Text}s (including the previous child {@link Text}s). The formatting can be reset by
 * inserting a {@link FormattingText} with a color code or {@link ChatColor#RESET}.
 * <p>
 * The structure and semantics of this text representation are closely oriented on plain text with
 * color codes, but still supports advanced text features such as {@link HoverEventText hover
 * events}. The goal is that {@link #toFormat()} for {@link Text}s produced by
 * {@link Text#parse(String)} will reproduce the original input text as closely as possible (with a
 * few minor exceptions, see {@link TextParser}). These differences to Minecraft's / Spigot's text
 * components need to be considered during conversions.
 */
public interface Text {

	// COMMON CONSTANTS

	/**
	 * An empty {@link Text}.
	 */
	public static final Text EMPTY = Text.of("");

	// FACTORY

	/**
	 * Shortcut for creating a new plain {@link Text}.
	 * <p>
	 * Consider using {@link #parse(String)} instead if the given text may contain features such as
	 * color codes or placeholders.
	 * <p>
	 * Consider using {@link #text(String)} if you intend to further modify the created {@link Text}
	 * before it is built.
	 * 
	 * @param text
	 *            the text, not <code>null</code>
	 * @return the new Text, not <code>null</code>
	 */
	public static Text of(String text) {
		return text(text).build();
	}

	/**
	 * Shortcut for converting the given object into a plain {@link Text}.
	 * <p>
	 * If the given object is a {@link Supplier}, it is invoked to obtain the actual object. If the
	 * object is already a {@link Text}, it is returned. Otherwise, a new plain {@link Text} is
	 * created from the Object's String representation. If the object is <code>null</code>, the
	 * String {@code "null"} is used.
	 * 
	 * @param object
	 *            the object to convert to a Text
	 * @return the new Text, not <code>null</code>
	 */
	public static Text of(@Nullable Object object) {
		Object resolved = object;
		if (object instanceof Supplier) {
			resolved = ((Supplier<?>) object).get();
		}
		if (resolved instanceof Text) return (Text) resolved;
		return Text.of(String.valueOf(resolved));
	}

	// PARSING

	/**
	 * Parses the given input text and creates a corresponding {@link Text}.
	 * <p>
	 * This takes into account:
	 * <ul>
	 * <li>Legacy color codes (starting with '{@literal &}' or '§')
	 * <li>Placeholders
	 * </ul>
	 * <p>
	 * The internal structure of the resulting {@link Text} is undefined and may change across
	 * different versions.
	 * 
	 * @param input
	 *            the text to parse, not <code>null</code>
	 * @return the parsed Text, not <code>null</code>
	 */
	public static Text parse(String input) {
		return TextParser.parse(input);
	}

	/**
	 * Parsed the given input texts.
	 * 
	 * @param inputs
	 *            the texts to parse
	 * @return the parsed Texts
	 * @see #parse(String)
	 */
	public static List<Text> parse(Collection<? extends String> inputs) {
		List<Text> texts = new ArrayList<>(inputs.size());
		for (String input : inputs) {
			texts.add(parse(input));
		}
		return texts;
	}

	// BUILDER

	/**
	 * Creates a new plain {@link TextBuilder} with the given text.
	 * 
	 * @param text
	 *            the text, not <code>null</code>
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder text(String text) {
		return new PlainText(text);
	}

	/**
	 * Creates a new {@link TextBuilder} which uses the String representation of the given object as
	 * its text.
	 * <p>
	 * If the given object is a {@link Supplier}, it gets invoked to obtain the actual object. If
	 * the object is <code>null</code>, the String {@code "null"} is used.
	 * 
	 * @param object
	 *            the object to convert to a Text
	 * @return the new {@link TextBuilder}
	 * @throws IllegalArgumentException
	 *             if the given object is already a Text
	 */
	public static TextBuilder text(@Nullable Object object) {
		Object resolved = object;
		if (object instanceof Supplier) {
			resolved = ((Supplier<?>) object).get();
		}
		// Testing this explicitly since this might be a common usage error:
		Validate.isTrue(!(resolved instanceof Text), "object is already a Text");
		return text(String.valueOf(resolved));
	}

	/**
	 * Creates a new {@link TextBuilder} with the newline symbol as text.
	 * 
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder newline() {
		return text("\n");
	}

	/**
	 * Creates a new {@link TextBuilder} with the given formatting code.
	 * <p>
	 * The formatting code can be the code of a {@link ChatColor#isColor() color}, a
	 * {@link ChatColor#isFormat() format}, or {@link ChatColor#RESET}, or a hex color in the format
	 * "#aabbcc".
	 * 
	 * @param code
	 *            the formatting code, not <code>null</code>
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder formatting(String code) {
		return new FormattingText(code);
	}

	/**
	 * Creates a new {@link TextBuilder} with the given {@link ChatColor} formatting.
	 * <p>
	 * The formatting can be a {@link ChatColor#isColor() color}, a {@link ChatColor#isFormat()
	 * format}, or {@link ChatColor#RESET}.
	 * 
	 * @param formatting
	 *            the formatting, not <code>null</code>
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder formatting(ChatColor formatting) {
		return formatting(String.valueOf(formatting.getChar()));
	}

	/**
	 * Creates a new {@link TextBuilder} with the given {@link ChatColor}.
	 * <p>
	 * This is an alias for {@link #formatting(ChatColor)} and accepts any type of {@link ChatColor}
	 * formatting.
	 * 
	 * @param color
	 *            the color, not <code>null</code>
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder color(ChatColor color) {
		return formatting(color);
	}

	/**
	 * Creates a new {@link TextBuilder} with the given color.
	 * 
	 * @param color
	 *            the color, not <code>null</code>
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder color(Color color) {
		return formatting("#" + String.format("%08x", color.getRGB()).substring(2));
	}

	/**
	 * Creates a new {@link TextBuilder} with a formatting reset.
	 * <p>
	 * This is a shortcut for {@link #formatting(ChatColor)} with {@link ChatColor#RESET}.
	 * 
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder reset() {
		return formatting(ChatColor.RESET);
	}

	/**
	 * Creates a new translatable {@link TextBuilder}.
	 * 
	 * @param translationKey
	 *            the translation key, not <code>null</code> or empty
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder translatable(String translationKey) {
		return new TranslatableText(translationKey);
	}

	/**
	 * Creates a new placeholder {@link TextBuilder}.
	 * 
	 * @param placeholderKey
	 *            the placeholder key, not <code>null</code> or empty
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder placeholder(String placeholderKey) {
		return new PlaceholderText(placeholderKey);
	}

	/**
	 * Creates a new {@link TextBuilder} with the specified hover event.
	 * 
	 * @param content
	 *            the hover event content, not <code>null</code>
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder hoverEvent(HoverEventText.Content content) {
		return new HoverEventText(content);
	}

	/**
	 * Creates a new {@link TextBuilder} with the specified hover text.
	 * <p>
	 * This is a shortcut for the corresponding {@link #hoverEvent(HoverEventText.Content)}.
	 * 
	 * @param hoverText
	 *            the hover text, not <code>null</code>
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder hoverEvent(Text hoverText) {
		return hoverEvent(new HoverEventText.TextContent(hoverText));
	}

	/**
	 * Creates a new {@link TextBuilder} with the specified click event.
	 * 
	 * @param action
	 *            the click event action, not <code>null</code>
	 * @param value
	 *            the click event value, not <code>null</code>
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder clickEvent(ClickEventText.Action action, String value) {
		return new ClickEventText(action, value);
	}

	/**
	 * Creates a new {@link TextBuilder} with the given insertion text.
	 * <p>
	 * Returns an empty {@link #text(String)} if the insertion is <code>null</code> or empty.
	 * 
	 * @param insertion
	 *            the insertion text
	 * @return the new {@link TextBuilder}
	 */
	public static TextBuilder insertion(@Nullable String insertion) {
		return (insertion == null || insertion.isEmpty()) ? text("") : new InsertionText(insertion);
	}

	/////

	// PARENT

	/**
	 * Gets the parent of this Text.
	 * <p>
	 * There are multiple possibilities for the relationship this Text has with its parent. For
	 * example, it may be the {@link #getNext() next} Text or the {@link #getChild() child} Text or
	 * something else.
	 * 
	 * @param <T>
	 *            the expected type of Text
	 * @return the parent, or <code>null</code>
	 */
	public <T extends Text> @Nullable T getParent();

	/**
	 * Gets the root Text by following the chain of parents.
	 * 
	 * @param <T>
	 *            the root text's expected type
	 * @return the root Text, not <code>null</code>, may be this Text itself
	 */
	public <T extends Text> @NonNull T getRoot();

	// Maybe also useful (e.g. when building Texts):
	// TODO Add getLeaf which returns the last Text in the chain of next Texts?
	// TODO Add getLast which returns the last Text in the sequential order of Texts (goes to the
	// leaf, but then also considers the child Texts of that leaf)

	// CHILD

	/**
	 * Gets the child Text.
	 * <p>
	 * The child Text (and its childs and subsequent segments) inherits all the interaction and
	 * formatting features of this Text.
	 * 
	 * @return the child Text, or <code>null</code>
	 */
	public @Nullable Text getChild();

	// NEXT

	/**
	 * Gets the subsequent Text.
	 * <p>
	 * The subsequent Text inherits the last encountered color and formatting of this Text and its
	 * childs.
	 * 
	 * @return the subsequent Text, or <code>null</code>
	 */
	public @Nullable Text getNext();

	// PLACEHOLDER ARGUMENTS

	/**
	 * Assigns the given arguments to their corresponding {@link PlaceholderText placeholders} used
	 * inside this {@link Text}, its {@link #getChild() child} and {@link #getNext() subsequent}
	 * Texts, and any {@link HoverEventText hover events}.
	 * <p>
	 * Any placeholders for which no corresponding argument is provided will retain their currently
	 * assigned placeholder argument if any.
	 * <p>
	 * Any assigned non-{@link Text} argument gets first converted to a corresponding Text by using
	 * its {@link Object#toString() String representation}. If the argument is a {@link Supplier} it
	 * will be invoked to obtain the actual argument.
	 * <p>
	 * Any {@link Text} placeholder arguments that were not yet {@link TextBuilder#isBuilt() built}
	 * may get built by this method.
	 * 
	 * @param arguments
	 *            the message arguments mapped by their placeholder keys, not <code>null</code>
	 * @return this Text
	 */
	public Text setPlaceholderArguments(MessageArguments arguments);

	/**
	 * Assigns the given arguments to their corresponding {@link PlaceholderText placeholders} used
	 * inside this {@link Text}, its {@link #getChild() child} and {@link #getNext() subsequent}
	 * Texts, and any {@link HoverEventText hover events}.
	 * <p>
	 * This is provided as a convenience over having to manually prepare a {@link MessageArguments}
	 * when calling {@link #setPlaceholderArguments(MessageArguments)}. However, to simplify the
	 * implementation of sub-classes, the implementation of this method is supposed to delegate to
	 * {@link #setPlaceholderArguments(MessageArguments)}.
	 * 
	 * @param arguments
	 *            a mapping between placeholder keys and their arguments, not <code>null</code>
	 * @return this Text
	 */
	public Text setPlaceholderArguments(Map<? extends String, @NonNull ?> arguments);

	/**
	 * Assigns the given arguments to their corresponding {@link PlaceholderText placeholders} used
	 * inside this {@link Text}, its {@link #getChild() child} and {@link #getNext() subsequent}
	 * Texts, and any {@link HoverEventText hover events}.
	 * <p>
	 * This is provided as a convenience over having to manually prepare a {@link Map} and
	 * {@link MessageArguments} when calling {@link #setPlaceholderArguments(MessageArguments)}.
	 * However, to simplify the implementation of sub-classes, the implementation of this method is
	 * supposed to delegate (directly or indirectly) to
	 * {@link #setPlaceholderArguments(MessageArguments)}.
	 * 
	 * @param argumentPairs
	 *            an array that pairwise contains placeholder keys (of type String) and their
	 *            arguments in the format <code>[key1, value1, key2, value2, ...]</code>, not
	 *            <code>null</code>
	 * @return this Text
	 * @see #setPlaceholderArguments(Map)
	 */
	public Text setPlaceholderArguments(@NonNull Object... argumentPairs);

	/**
	 * Clears all placeholder arguments from this {@link Text} and its {@link #getChild() child} and
	 * {@link #getNext() subsequent} Texts.
	 * 
	 * @return this Text
	 */
	public Text clearPlaceholderArguments();

	// PLAIN TEXT CONVERSIONS

	/**
	 * Converts this {@link Text} to a plain String text that only includes color and formatting
	 * codes. Hex colors are converted to Bukkit's "§x§a§a§b§b§c§c" format.
	 * 
	 * @return the plain text
	 */
	public String toPlainText();

	/**
	 * Converts this {@link Text} to a plain format String.
	 * <p>
	 * Color and formatting codes are converted to use character '{@literal &}'.
	 * {@link PlaceholderText}s print their {@link PlaceholderText#getFormattedPlaceholderKey()
	 * formatted placeholder key} instead of their assigned argument.
	 * 
	 * @return the plain format string
	 */
	public String toFormat();

	/**
	 * Checks whether this {@link Text} or any of its childs uses non-plain text features such as
	 * hover events, click events, insertions, etc.
	 * 
	 * @return <code>true</code> if only plain text is used
	 */
	public boolean isPlainText();

	/**
	 * Checks if the plain text of this {@link Text} (including all its childs) is empty.
	 * 
	 * @return <code>true</code> if the plain text is empty
	 */
	public boolean isPlainTextEmpty();

	/**
	 * Converts this {@link Text} to a plain String text that also excludes any color and formatting
	 * codes.
	 * 
	 * @return the plain text without any formatting codes
	 */
	public String toUnformattedText();

	// COPY

	/**
	 * Creates a (deep) copy of this {@link Text}.
	 * <p>
	 * This also (deeply) copies the {@link #getChild() child} and {@link #getNext() subsequent}
	 * Texts and any {@link TranslatableText translation} or {@link PlaceholderText placeholder}
	 * arguments.
	 * 
	 * @return the copy
	 */
	public Text copy();

	// JAVA OBJECT

	/**
	 * A detailed String representation of this {@link Text}'s internals.
	 * <p>
	 * Use {@link #toPlainText()} to get a String representation of this {@link Text}'s text.
	 * 
	 * @return the string representation
	 */
	@Override
	public String toString();

	// Note on hashCode and equals: Text instances are identified by object identity.
}
