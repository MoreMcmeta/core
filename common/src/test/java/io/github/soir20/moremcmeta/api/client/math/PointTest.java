/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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

package io.github.soir20.moremcmeta.api.client.math;

import io.github.soir20.moremcmeta.api.math.Point;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the {@link Point}.
 * @author soir20
 */
public class PointTest {

    @Test
    public void getX_SomePoint_CorrectCoordinate() {
        Point point = new Point(1, 2);
        assertEquals(1, point.getX());
    }

    @Test
    public void getY_SomePoint_CorrectCoordinate() {
        Point point = new Point(1, 2);
        assertEquals(2, point.getY());
    }

    @Test
    public void equals_SamePoints_Reflexive() {
        Point first = new Point(1, 2);
        assertEquals(first, first);
    }

    @Test
    public void equals_SamePoints_Symmetric() {
        Point first = new Point(1, 2);
        Point second = new Point(1, 2);
        assertEquals(first, second);
        assertEquals(second, first);
    }

    @Test
    public void equals_SamePoints_Transitive() {
        Point first = new Point(1, 2);
        Point second = new Point(1, 2);
        Point third = new Point(1, 2);
        assertEquals(first, second);
        assertEquals(second, third);
        assertEquals(first, third);
    }

    @Test
    public void equals_DiffPoints_Symmetric() {
        Point first = new Point(1, 2);
        Point second = new Point(3, 4);
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    public void equals_DiffTypes_Symmetric() {
        Point first = new Point(1, 2);
        Object second = new Object();
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    public void hashCode_SamePoints_Reflexive() {
        Point first = new Point(1, 2);
        assertEquals(first.hashCode(), first.hashCode());
    }

    @Test
    public void hashCode_SamePoints_Symmetric() {
        Point first = new Point(1, 2);
        Point second = new Point(1, 2);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), first.hashCode());
    }

    @Test
    public void hashCode_SamePoints_Transitive() {
        Point first = new Point(1, 2);
        Point second = new Point(1, 2);
        Point third = new Point(1, 2);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), third.hashCode());
        assertEquals(first.hashCode(), third.hashCode());
    }

    @Test
    public void hashCode_DiffPoints_Symmetric() {
        Point first = new Point(1, 2);
        Point second = new Point(3, 4);
        assertNotEquals(first.hashCode(), second.hashCode());
        assertNotEquals(second.hashCode(), first.hashCode());
    }

    @Test
    public void hashCode_DiffTypes_Symmetric() {
        Point first = new Point(1, 2);
        Object second = new Object();
        assertNotEquals(first.hashCode(), second.hashCode());
        assertNotEquals(second.hashCode(), first.hashCode());
    }

    @Test
    public void toString_PositiveValues_PositiveString() {
        assertEquals("(2, 3)", new Point(2, 3).toString());
    }

    @Test
    public void toString_NegativeValues_PositiveString() {
        assertEquals("(-2, -3)", new Point(-2, -3).toString());
    }

}