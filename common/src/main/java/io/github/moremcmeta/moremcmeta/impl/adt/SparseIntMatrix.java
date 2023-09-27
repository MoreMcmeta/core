/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moremcmeta.moremcmeta.impl.adt;

import java.util.BitSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * <p>Stores integers in a two-dimensional matrix. At each position, an integer may or may not be set.
 * By default, no integers are set. The integer at a position cannot be retrieved until it is set.
 * This class is designed to handle integers that are sparse. A 2D array may be a more suitable
 * data structure when the integers are dense or when there is a default value for integers that have
 * not been set.</p>
 *
 * <p>The sparse matrix stores integers in square "sectors" of equal size. When the first integer inside
 * the sector is set, the entire sector is allocated. The length of the side of each sector is a power
 * of 2. This power is called the sector power; for example, if sectors are 8x8, the sector power is 3
 * because 2^3 = 8. The maximum sector power is configurable, but a smaller power may be used if the
 * maximum power is unnecessarily large or too large to work correctly with the provided dimensions.</p>
 *
 * <p>Supports concurrent access and modification.</p>
 * @author soir20
 */
public final class SparseIntMatrix {
    private final BitSet[] IS_PRESENT;
    private final ReentrantLock[] LOCKS;
    private final int WIDTH;
    private final int HEIGHT;
    private final int[][] MATRIX;
    private final int SECTOR_POWER;
    private final int SECTORS_PER_ROW;
    private final int POINTS_PER_SECTOR_ROW;
    private final int SECTOR_SIZE;
    private final int SECTOR_COORD_MASK;

    /**
     * Creates a new sparse integer matrix.
     * @param width             width of the matrix
     * @param height            height of the matrix
     * @param maxSectorPower    maximum power of two for the side length of each sector.
     *                          A smaller sector power may be used if the provided one is
     *                          unnecessarily large or too large to work correctly.
     */
    public SparseIntMatrix(int width, int height, int maxSectorPower) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    String.format("Width and height must be positive: %sx%s", width, height)
            );
        }

        WIDTH = width;
        HEIGHT = height;

        if (maxSectorPower <= 0) {
            throw new IllegalArgumentException(
                    String.format("Max sector power must be positive: %s", maxSectorPower)
            );
        }

        /* We may need to use a smaller sector power if bit-shifting the width or height by the
           provided power would not leave enough bits to index the sectors.*/
        SECTOR_POWER = min(
                maxSectorPower,
                largestPowerOf2LessThanOrEqual(WIDTH),
                largestPowerOf2LessThanOrEqual(HEIGHT)
        );

        SECTORS_PER_ROW = shiftRightRoundUp(WIDTH, SECTOR_POWER);
        int rows = shiftRightRoundUp(HEIGHT, SECTOR_POWER);
        POINTS_PER_SECTOR_ROW = 1 << SECTOR_POWER;
        SECTOR_COORD_MASK = POINTS_PER_SECTOR_ROW - 1;
        SECTOR_SIZE = POINTS_PER_SECTOR_ROW * POINTS_PER_SECTOR_ROW;

        MATRIX = new int[SECTORS_PER_ROW * rows][];
        IS_PRESENT = Stream.generate(() -> new BitSet(SECTOR_SIZE))
                .limit((long) SECTORS_PER_ROW * rows)
                .toArray(BitSet[]::new);
        LOCKS = Stream.generate(ReentrantLock::new)
                .limit((long) SECTORS_PER_ROW * rows)
                .toArray(ReentrantLock[]::new);
    }

    /**
     * Retrieves the integer at the given coordinates in the matrix.
     * @param x     horizontal coordinate to access
     * @param y     vertical coordinate to access
     * @return the integer at the given coordinates
     * @throws IllegalStateException if the point has not been set.
     */
    public int get(int x, int y) {
        checkInBounds(x, y);
        int sectorIndex = sectorIndex(x, y);
        int indexInSector = indexInSector(x, y);

        ReentrantLock lock = LOCKS[sectorIndex];
        lock.lock();

        if (!IS_PRESENT[sectorIndex].get(indexInSector)) {
            throw new IllegalStateException(String.format("Point (%s, %s) has not been set", x, y));
        }

        int value = MATRIX[sectorIndex][indexInSector];
        lock.unlock();

        return value;
    }

    /**
     * Checks if an integer has been set at the given coordinates.
     * @param x     horizontal coordinate to check
     * @param y     vertical coordinate to check
     * @return true if an integer has been set or otherwise false
     */
    public boolean isSet(int x, int y) {
        checkInBounds(x, y);
        int sectorIndex = sectorIndex(x, y);
        int indexInSector = indexInSector(x, y);

        ReentrantLock lock = LOCKS[sectorIndex];

        lock.lock();
        boolean isSet = IS_PRESENT[sectorIndex].get(indexInSector);
        lock.unlock();

        return isSet;
    }

    /**
     * Sets the integer at the given coordinates. If the sector containing the integer
     * has not already been allocated, it will definitely be allocated after this method
     * call.
     * @param x         horizontal coordinate to set
     * @param y         vertical coordinate to set
     * @param value     value to put at the given coordinates
     */
    public void set(int x, int y, int value) {
        checkInBounds(x, y);
        int sectorIndex = sectorIndex(x, y);
        int indexInSector = indexInSector(x, y);

        ReentrantLock lock = LOCKS[sectorIndex];
        lock.lock();

        if (MATRIX[sectorIndex] == null) {
            MATRIX[sectorIndex] = new int[SECTOR_SIZE];
        }

        MATRIX[sectorIndex][indexInSector] = value;
        IS_PRESENT[sectorIndex].set(indexInSector);
        lock.unlock();
    }

    /**
     * Checks if coordinates are inside the matrix bounds.
     * @param x         horizontal coordinate to check
     * @param y         vertical coordinate to check
     */
    private void checkInBounds(int x, int y) {
        if (x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT) {
            throw new IllegalArgumentException(
                    String.format("Point (%s, %s) is out of bounds in a %sx%s matrix", x, y, WIDTH, HEIGHT)
            );
        }
    }

    /**
     * Computes the index of a sector that contains the given point.
     * @param x     x-coordinate of the point
     * @param y     y-coordinate of the point
     * @return index of the sector
     */
    private int sectorIndex(int x, int y) {
        return (y >> SECTOR_POWER) * SECTORS_PER_ROW + (x >> SECTOR_POWER);
    }

    /**
     * Converts coordinates to an index inside a sector.
     * @param x     horizontal coordinate to convert
     * @param y     vertical coordinate to convert
     * @return the index of the value at the given point inside the sector
     */
    private int indexInSector(int x, int y) {
        int hCoord = x & SECTOR_COORD_MASK;
        int vCoord = y & SECTOR_COORD_MASK;
        return vCoord * POINTS_PER_SECTOR_ROW + hCoord;
    }

    /**
     * Computes the largest power of 2 less than or equal to the given number, assuming
     * that the given number is positive.
     * @param num       number
     * @return the largest power of 2 less than or equal to the given number
     */
    private static int largestPowerOf2LessThanOrEqual(int num) {
        return Integer.numberOfTrailingZeros(Integer.highestOneBit(num));
    }

    /**
     * Shifts a positive number right by the given amount, rounding up if there is a remainder
     * from division.
     * @param num       number
     * @param amount    positions to shift the number right
     * @return the number divided by 2^amount, rounded up if there is a remainder
     */
    private static int shiftRightRoundUp(int num, int amount) {
        int original = num >> amount;

        // If the original number is a power of two, there is no remainder.
        if ((num & (num - 1)) == 0) {
            return original;
        }

        return original + 1;
    }

    /**
     * Computes the minimum of three values.
     * @param one       first value to compare
     * @param two       second value to compare
     * @param three     third value to compare
     * @return the smallest of the three values
     */
    private static int min(int one, int two, int three) {
        return Math.min(one, Math.min(two, three));
    }

}
