/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link SparseIntMatrix}.
 * @author soir20
 */
public final class SparseIntMatrixTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NegativeMaxPower_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new SparseIntMatrix(100, 100, -1);
    }

    @Test
    public void construct_ZeroMaxPower_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new SparseIntMatrix(100, 100, 0);
    }

    @Test
    public void construct_NegativeWidth_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new SparseIntMatrix(-1, 100, 3);
    }

    @Test
    public void construct_ZeroWidth_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new SparseIntMatrix(0, 100, 3);
    }

    @Test
    public void construct_NegativeHeight_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new SparseIntMatrix(100, -1, 3);
    }

    @Test
    public void construct_ZeroHeight_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new SparseIntMatrix(100, 0, 3);
    }

    @Test
    public void get_OutOfBoundsXNegative_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.get(-10, 20);
    }

    @Test
    public void get_OutOfBoundsYNegative_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.get(10, -20);
    }

    @Test
    public void get_OutOfBoundsXPositive_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.get(100, 20);
    }

    @Test
    public void get_OutOfBoundsYPositive_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.get(10, 200);
    }

    @Test
    public void get_NotPresent_IllegalStateException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalStateException.class);
        matrix.get(10, 20);
    }

    @Test
    public void set_OutOfBoundsXNegative_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.set(-10, 20, 500);
    }

    @Test
    public void set_OutOfBoundsYNegative_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.set(10, -20, 500);
    }

    @Test
    public void set_OutOfBoundsXPositive_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.set(100, 20, 500);
    }

    @Test
    public void set_OutOfBoundsYPositive_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.set(10, 200, 500);
    }

    @Test
    public void isSet_OutOfBoundsXNegative_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.isSet(-10, 20);
    }

    @Test
    public void isSet_OutOfBoundsYNegative_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.isSet(10, -20);
    }

    @Test
    public void isSet_OutOfBoundsXPositive_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.isSet(100, 20);
    }

    @Test
    public void isSet_OutOfBoundsYPositive_IllegalArgException() {
        SparseIntMatrix matrix = new SparseIntMatrix(100, 200, 3);
        expectedException.expect(IllegalArgumentException.class);
        matrix.isSet(10, 200);
    }

    @Test
    public void all_ProvidedSectorPowerUsed_AllSectorsBehaveCorrectly() {

        // 4x2 sectors
        SparseIntMatrix matrix = new SparseIntMatrix(15, 7, 1);

        assertFalse(matrix.isSet(0, 0));
        assertFalse(matrix.isSet(4, 2));
        assertFalse(matrix.isSet(9, 1));
        assertFalse(matrix.isSet(12, 3));

        assertFalse(matrix.isSet(1, 6));
        assertFalse(matrix.isSet(6, 4));
        assertFalse(matrix.isSet(8, 6));
        assertFalse(matrix.isSet(14, 5));

        matrix.set(0, 0, 500);
        matrix.set(4, 2, -250);
        matrix.set(9, 1, -1);
        matrix.set(12, 3, 10000);

        matrix.set(1, 6, 100);
        matrix.set(6, 4, 50);
        matrix.set(8, 6, 30);
        matrix.set(14, 5, 700);

        assertTrue(matrix.isSet(0, 0));
        assertTrue(matrix.isSet(4, 2));
        assertTrue(matrix.isSet(9, 1));
        assertTrue(matrix.isSet(12, 3));

        assertTrue(matrix.isSet(1, 6));
        assertTrue(matrix.isSet(6, 4));
        assertTrue(matrix.isSet(8, 6));
        assertTrue(matrix.isSet(14, 5));

        assertEquals(500, matrix.get(0, 0));
        assertEquals(-250, matrix.get(4, 2));
        assertEquals(-1, matrix.get(9, 1));
        assertEquals(10000, matrix.get(12, 3));

        assertEquals(100, matrix.get(1, 6));
        assertEquals(50, matrix.get(6, 4));
        assertEquals(30, matrix.get(8, 6));
        assertEquals(700, matrix.get(14, 5));
    }

    @Test
    public void all_SectorPowerBasedOnWidthUsed_AllSectorsBehaveCorrectly() {

        // 4x2 sectors
        SparseIntMatrix matrix = new SparseIntMatrix(7, 15, 20);

        assertFalse(matrix.isSet(0, 0));
        assertFalse(matrix.isSet(2, 4));
        assertFalse(matrix.isSet(1, 9));
        assertFalse(matrix.isSet(3, 12));

        assertFalse(matrix.isSet(6, 1));
        assertFalse(matrix.isSet(4, 6));
        assertFalse(matrix.isSet(6, 8));
        assertFalse(matrix.isSet(5, 14));

        matrix.set(0, 0, 500);
        matrix.set(2, 4, -250);
        matrix.set(1, 9, -1);
        matrix.set(3, 12, 10000);

        matrix.set(6, 1, 100);
        matrix.set(4, 6, 50);
        matrix.set(6, 8, 30);
        matrix.set(5, 14, 700);

        assertTrue(matrix.isSet(0, 0));
        assertTrue(matrix.isSet(2, 4));
        assertTrue(matrix.isSet(1, 9));
        assertTrue(matrix.isSet(3, 12));

        assertTrue(matrix.isSet(6, 1));
        assertTrue(matrix.isSet(4, 6));
        assertTrue(matrix.isSet(6, 8));
        assertTrue(matrix.isSet(5, 14));

        assertEquals(500, matrix.get(0, 0));
        assertEquals(-250, matrix.get(2, 4));
        assertEquals(-1, matrix.get(1, 9));
        assertEquals(10000, matrix.get(3, 12));

        assertEquals(100, matrix.get(6, 1));
        assertEquals(50, matrix.get(4, 6));
        assertEquals(30, matrix.get(6, 8));
        assertEquals(700, matrix.get(5, 14));
    }

    @Test
    public void all_SectorPowerBasedOnHeightUsed_AllSectorsBehaveCorrectly() {

        // 4x2 sectors
        SparseIntMatrix matrix = new SparseIntMatrix(15, 7, 20);

        assertFalse(matrix.isSet(0, 0));
        assertFalse(matrix.isSet(4, 2));
        assertFalse(matrix.isSet(9, 1));
        assertFalse(matrix.isSet(12, 3));

        assertFalse(matrix.isSet(1, 6));
        assertFalse(matrix.isSet(6, 4));
        assertFalse(matrix.isSet(8, 6));
        assertFalse(matrix.isSet(14, 5));

        matrix.set(0, 0, 500);
        matrix.set(4, 2, -250);
        matrix.set(9, 1, -1);
        matrix.set(12, 3, 10000);

        matrix.set(1, 6, 100);
        matrix.set(6, 4, 50);
        matrix.set(8, 6, 30);
        matrix.set(14, 5, 700);

        assertTrue(matrix.isSet(0, 0));
        assertTrue(matrix.isSet(4, 2));
        assertTrue(matrix.isSet(9, 1));
        assertTrue(matrix.isSet(12, 3));

        assertTrue(matrix.isSet(1, 6));
        assertTrue(matrix.isSet(6, 4));
        assertTrue(matrix.isSet(8, 6));
        assertTrue(matrix.isSet(14, 5));

        assertEquals(500, matrix.get(0, 0));
        assertEquals(-250, matrix.get(4, 2));
        assertEquals(-1, matrix.get(9, 1));
        assertEquals(10000, matrix.get(12, 3));

        assertEquals(100, matrix.get(1, 6));
        assertEquals(50, matrix.get(6, 4));
        assertEquals(30, matrix.get(8, 6));
        assertEquals(700, matrix.get(14, 5));
    }

    @Test
    public void all_SectorPowerBasedOnWidthUsedPowerOf2_AllSectorsBehaveCorrectly() {

        // 4x2 sectors
        SparseIntMatrix matrix = new SparseIntMatrix(8, 16, 20);

        assertFalse(matrix.isSet(0, 0));
        assertFalse(matrix.isSet(2, 4));
        assertFalse(matrix.isSet(1, 9));
        assertFalse(matrix.isSet(3, 12));

        assertFalse(matrix.isSet(6, 1));
        assertFalse(matrix.isSet(4, 6));
        assertFalse(matrix.isSet(7, 8));
        assertFalse(matrix.isSet(5, 15));

        matrix.set(0, 0, 500);
        matrix.set(2, 4, -250);
        matrix.set(1, 9, -1);
        matrix.set(3, 12, 10000);

        matrix.set(6, 1, 100);
        matrix.set(4, 6, 50);
        matrix.set(7, 8, 30);
        matrix.set(5, 15, 700);

        assertTrue(matrix.isSet(0, 0));
        assertTrue(matrix.isSet(2, 4));
        assertTrue(matrix.isSet(1, 9));
        assertTrue(matrix.isSet(3, 12));

        assertTrue(matrix.isSet(6, 1));
        assertTrue(matrix.isSet(4, 6));
        assertTrue(matrix.isSet(7, 8));
        assertTrue(matrix.isSet(5, 15));

        assertEquals(500, matrix.get(0, 0));
        assertEquals(-250, matrix.get(2, 4));
        assertEquals(-1, matrix.get(1, 9));
        assertEquals(10000, matrix.get(3, 12));

        assertEquals(100, matrix.get(6, 1));
        assertEquals(50, matrix.get(4, 6));
        assertEquals(30, matrix.get(7, 8));
        assertEquals(700, matrix.get(5, 15));
    }

    @Test
    public void all_SectorPowerBasedOnHeightUsedPowerOf2_AllSectorsBehaveCorrectly() {

        // 4x2 sectors
        SparseIntMatrix matrix = new SparseIntMatrix(16, 8, 20);

        assertFalse(matrix.isSet(0, 0));
        assertFalse(matrix.isSet(4, 2));
        assertFalse(matrix.isSet(9, 1));
        assertFalse(matrix.isSet(12, 3));

        assertFalse(matrix.isSet(1, 6));
        assertFalse(matrix.isSet(6, 4));
        assertFalse(matrix.isSet(8, 7));
        assertFalse(matrix.isSet(15, 5));

        matrix.set(0, 0, 500);
        matrix.set(4, 2, -250);
        matrix.set(9, 1, -1);
        matrix.set(12, 3, 10000);

        matrix.set(1, 6, 100);
        matrix.set(6, 4, 50);
        matrix.set(8, 7, 30);
        matrix.set(15, 5, 700);

        assertTrue(matrix.isSet(0, 0));
        assertTrue(matrix.isSet(4, 2));
        assertTrue(matrix.isSet(9, 1));
        assertTrue(matrix.isSet(12, 3));

        assertTrue(matrix.isSet(1, 6));
        assertTrue(matrix.isSet(6, 4));
        assertTrue(matrix.isSet(8, 7));
        assertTrue(matrix.isSet(15, 5));

        assertEquals(500, matrix.get(0, 0));
        assertEquals(-250, matrix.get(4, 2));
        assertEquals(-1, matrix.get(9, 1));
        assertEquals(10000, matrix.get(12, 3));

        assertEquals(100, matrix.get(1, 6));
        assertEquals(50, matrix.get(6, 4));
        assertEquals(30, matrix.get(8, 7));
        assertEquals(700, matrix.get(15, 5));
    }

}