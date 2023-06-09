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

package io.github.moremcmeta.moremcmeta.api.math;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the {@link Point}.
 * @author soir20
 */
public final class PointTest {

    @Test
    public void x_SomePoint_CorrectCoordinate() {
        long point = Point.pack(1, 2);
        assertEquals(1, Point.x(point));
    }

    @Test
    public void y_SomePoint_CorrectCoordinate() {
        long point = Point.pack(1, 2);
        assertEquals(2, Point.y(point));
    }

    @Test
    public void toString_PositiveValues_PositiveString() {
        assertEquals("(2, 3)", Point.toString(Point.pack(2, 3)));
    }

    @Test
    public void toString_NegativeValues_PositiveString() {
        assertEquals("(-2, -3)", Point.toString(Point.pack(-2, -3)));
    }

}