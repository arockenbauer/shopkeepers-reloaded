package com.nisovin.shopkeepers.util.java;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An immutable axis-aligned rectangle in the positive quadrant of a discrete coordinate system that
 * spans a specified width and height from a corner specified as an x and y offset from the origin
 * of the coordinate system.
 * <p>
 * The rectangle contains the points on its lower edges along the two axis but excludes the points
 * on its higher edges.
 * <p>
 * A coordinate inside the rectangle can also be referred to as a "slot", using the following
 * mapping schema: Slot <code>[0]</code> identifies the point at the rectangle's x and y offsets
 * <code>(x, y)</code>, slot <code>[width - 1]</code> identifies the point at coordinate
 * <code>(x + width - 1, y)</code>, slot <code>[width]</code> identifies the point at
 * <code>(x, y + 1)</code>, and slot <code>[width * height - 1]</code> identifies the point at
 * <code>(x + width - 1, y + height - 1)</code>.<br>
 * However, note that slot numbers are specific to the rectangle that they were created for: The
 * slot numbers of rectangles with different offsets or different widths are not compatible with
 * each other because they refer to different coordinates.
 */
public class Rectangle {

	private final int x;
	private final int y;
	private final int width;
	private final int height;

	/**
	 * Creates a new {@link Rectangle}.
	 * 
	 * @param x
	 *            the x offset
	 * @param y
	 *            the y offset
	 * @param width
	 *            the width, can be <code>0</code>
	 * @param height
	 *            the height, can be <code>0</code>
	 */
	public Rectangle(int x, int y, int width, int height) {
		Validate.isTrue(x >= 0 && y >= 0 && width >= 0 && height >= 0,
				() -> "Invalid rectangle: x=" + x + ", y=" + y
						+ ", width" + width + ", height=" + height);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Gets the x offset.
	 * 
	 * @return the x offset
	 */
	public int getX() {
		return x;
	}

	/**
	 * Gets the y offset.
	 * 
	 * @return the y offset
	 */
	public int getY() {
		return y;
	}

	/**
	 * Gets the width.
	 * 
	 * @return the width, can be <code>0</code>
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the height.
	 * 
	 * @return the height, can be <code>0</code>
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Checks if this rectangle has an empty {@link #getWidth() width} or {@link #getHeight()
	 * height}.
	 * 
	 * @return <code>true</code> if this rectangle is empty
	 */
	public boolean isEmpty() {
		return this.getWidth() == 0 || this.getHeight() == 0;
	}

	/**
	 * Checks if this rectangle contains the specified point.
	 * <p>
	 * The rectangle contains the points on its lower edges but excludes the points on its higher
	 * edges.
	 * 
	 * @param pointX
	 *            the point's x coordinate
	 * @param pointY
	 *            the point's y coordinate
	 * @return <code>true</code> if the point is located within this rectangle
	 */
	public boolean contains(int pointX, int pointY) {
		return pointX >= this.getX() && pointX < this.getX() + this.getWidth()
				&& pointY >= this.getY() && pointY < this.getY() + this.getHeight();
	}

	/**
	 * Checks if this rectangle fully contains the specified rectangle.
	 * 
	 * @param x
	 *            the x offset of the other rectangle
	 * @param y
	 *            the y offset of the other rectangle
	 * @param width
	 *            the width of the other rectangle
	 * @param height
	 *            the height of the other rectangle
	 * @return <code>true</code> if this rectangle fully contains the specified rectangle
	 */
	public boolean contains(int x, int y, int width, int height) {
		return x >= this.getX() && y >= this.getY()
				&& x + width <= this.getX() + this.getWidth()
				&& y + height <= this.getY() + this.getHeight();
	}

	/**
	 * Checks if this rectangle fully contains the given rectangle.
	 * 
	 * @param other
	 *            the other rectangle
	 * @return <code>true</code> if this rectangle fully contains the specified rectangle
	 */
	public boolean contains(Rectangle other) {
		Validate.notNull(other, "other is null");
		return this.contains(
				other.getX(),
				other.getY(),
				other.getWidth(),
				other.getHeight()
		);
	}

	/**
	 * Gets the maximum slot index.
	 * 
	 * @return the maximum slot index
	 * @throws IllegalArgumentException
	 *             if the rectangle is {@link #isEmpty() empty}
	 */
	public int getMaxSlot() {
		Validate.isTrue(!this.isEmpty(), "The rectangle is empty!");
		return this.getWidth() * this.getHeight() - 1;
	}

	/**
	 * Converts the given slot index to a x coordinate.
	 * 
	 * @param slot
	 *            the slot index
	 * @return the x coordinate
	 * @throws IllegalArgumentException
	 *             if the rectangle is empty or the slot index is outside the rectangle
	 */
	public int slotToX(int slot) {
		Validate.isTrue(this.containsSlot(slot), "Slot is outside the rectangle!");
		return this.getX() + slot % this.getWidth();
	}

	/**
	 * Converts the given slot index to a y coordinate.
	 * 
	 * @param slot
	 *            the slot index
	 * @return the y coordinate
	 * @throws IllegalArgumentException
	 *             if the rectangle is empty or the slot index is outside the rectangle
	 */
	public int slotToY(int slot) {
		Validate.isTrue(this.containsSlot(slot), "Slot is outside the rectangle!");
		return this.getY() + slot / this.getWidth();
	}

	/**
	 * Converts the given point coordinate to a slot index.
	 * 
	 * @param pointX
	 *            the point's x coordinate
	 * @param pointY
	 *            the point's y coordinate
	 * @return the slot index
	 * @throws IllegalArgumentException
	 *             if the point is outside the rectangle
	 */
	public int toSlot(int pointX, int pointY) {
		Validate.isTrue(this.contains(pointX, pointY), "Point is outside the rectangle!");
		return (pointY - this.getY()) * this.getWidth() + (pointX - this.getX());
	}

	/**
	 * Converts the given slot index of the given {@link Rectangle} into a slot index for this
	 * rectangle.
	 * 
	 * @param other
	 *            the other rectangle, not <code>null</code>
	 * @param otherSlot
	 *            the slot of the other rectangle
	 * @return the corresponding slot for this rectangle
	 * @throws IllegalArgumentException
	 *             if the given slot is invalid for the given rectangle
	 * @throws IllegalArgumentException
	 *             if there is no corresponding slot for this rectangle
	 */
	public int convertSlotFrom(Rectangle other, int otherSlot) {
		Validate.notNull(other, "other is null");
		int x = other.slotToX(otherSlot);
		int y = other.slotToY(otherSlot);
		return this.toSlot(x, y);
	}

	/**
	 * Checks if this rectangle contains the specified slot.
	 * 
	 * @param slot
	 *            the slot index
	 * @return <code>true</code> if the slot is located inside this rectangle
	 */
	public boolean containsSlot(int slot) {
		return slot >= 0 && slot <= this.getMaxSlot();
	}

	/**
	 * Checks if this rectangle contains the specified rectangle of slots.
	 * 
	 * @param startSlot
	 *            the index of the start slot
	 * @param width
	 *            the width of the other rectangle
	 * @param height
	 *            the height of the other rectangle
	 * @return <code>true</code> if this rectangle contains all slots
	 */
	public boolean containsSlots(int startSlot, int width, int height) {
		int startX = this.slotToX(startSlot);
		int startY = this.slotToY(startSlot);
		return this.contains(startX, startY, width, height);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getX();
		result = prime * result + this.getY();
		result = prime * result + this.getWidth();
		result = prime * result + this.getHeight();
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Rectangle)) return false;
		Rectangle other = (Rectangle) obj;
		if (this.getX() != other.getX()) return false;
		if (this.getY() != other.getY()) return false;
		if (this.getWidth() != other.getWidth()) return false;
		if (this.getHeight() != other.getHeight()) return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append(" [x=");
		builder.append(this.getX());
		builder.append(", y=");
		builder.append(this.getY());
		builder.append(", width=");
		builder.append(this.getWidth());
		builder.append(", height=");
		builder.append(this.getHeight());
		builder.append("]");
		return builder.toString();
	}
}
