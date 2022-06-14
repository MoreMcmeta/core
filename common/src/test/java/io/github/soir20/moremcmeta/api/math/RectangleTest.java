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

package io.github.soir20.moremcmeta.api.math;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests the {@link Rectangle}.
 * @author soir20
 */
public class RectangleTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NegativeWidth_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new Rectangle(0, 0, -1, 10);
    }

    @Test
    public void construct_NegativeHeight_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new Rectangle(0, 0, 10, -1);
    }

    @Test
    public void construct_NegativeWidthAndHeight_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new Rectangle(0, 0, -1, -1);
    }

    @Test
    public void construct_OverflowWidth_RectangleOverflowException() {
        expectedException.expect(Rectangle.RectangleOverflowException.class);
        new Rectangle(Integer.MAX_VALUE - 10, 0, 11, 10);
    }

    @Test
    public void construct_OverflowHeight_RectangleOverflowException() {
        expectedException.expect(Rectangle.RectangleOverflowException.class);
        new Rectangle(0, Integer.MAX_VALUE - 10, 10, 11);
    }

    @Test
    public void construct_OverflowWidthMaxValue_RectangleOverflowException() {
        expectedException.expect(Rectangle.RectangleOverflowException.class);
        new Rectangle(1, 0, Integer.MAX_VALUE, 10);
    }

    @Test
    public void construct_OverflowHeightMaxValue_RectangleOverflowException() {
        expectedException.expect(Rectangle.RectangleOverflowException.class);
        new Rectangle(0, 1, 10, Integer.MAX_VALUE);
    }

    @Test
    public void construct_LargestPossibleRectangleFromOrigin_ConstructedCorrectly() {
        Rectangle rect = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, rect.width());
        assertEquals(Integer.MAX_VALUE, rect.height());
        assertEquals(0, rect.topLeftX());
        assertEquals(0, rect.topLeftY());
        assertFalse(rect.isEmpty());
    }

    @Test
    public void construct_LargestPossibleRectangleFromSmallestPoint_ConstructedCorrectly() {
        Rectangle rect = new Rectangle(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, rect.width());
        assertEquals(Integer.MAX_VALUE, rect.height());
        assertEquals(Integer.MIN_VALUE, rect.topLeftX());
        assertEquals(Integer.MIN_VALUE, rect.topLeftY());
        assertFalse(rect.isEmpty());
    }

    @Test
    public void construct_EmptyRectangle_ConstructedCorrectly() {
        Rectangle rect = new Rectangle(0, 0, 0, 0);
        assertEquals(0, rect.width());
        assertEquals(0, rect.height());
        assertEquals(0, rect.topLeftX());
        assertEquals(0, rect.topLeftY());
        assertTrue(rect.isEmpty());
    }

    @Test
    public void construct_EmptyWidthRectangle_ConstructedCorrectly() {
        Rectangle rect = new Rectangle(0, 0, 0, 10);
        assertEquals(0, rect.width());
        assertEquals(10, rect.height());
        assertEquals(0, rect.topLeftX());
        assertEquals(0, rect.topLeftY());
        assertTrue(rect.isEmpty());
    }

    @Test
    public void construct_EmptyHeightRectangle_ConstructedCorrectly() {
        Rectangle rect = new Rectangle(0, 0, 10, 0);
        assertEquals(10, rect.width());
        assertEquals(0, rect.height());
        assertEquals(0, rect.topLeftX());
        assertEquals(0, rect.topLeftY());
        assertTrue(rect.isEmpty());
    }

    @Test
    public void construct_PositiveRectangle_ConstructedCorrectly() {
        Rectangle rect = new Rectangle(5, 1, 10, 20);
        assertEquals(10, rect.width());
        assertEquals(20, rect.height());
        assertEquals(5, rect.topLeftX());
        assertEquals(1, rect.topLeftY());
        assertFalse(rect.isEmpty());
    }

    @Test
    public void construct_PositiveRectangleAtCorner_ConstructedCorrectly() {
        Rectangle rect = new Rectangle(Integer.MAX_VALUE - 10, Integer.MAX_VALUE - 20, 10, 20);
        assertEquals(10, rect.width());
        assertEquals(20, rect.height());
        assertEquals(Integer.MAX_VALUE - 10, rect.topLeftX());
        assertEquals(Integer.MAX_VALUE - 20, rect.topLeftY());
        assertFalse(rect.isEmpty());
    }

    @Test
    public void construct_RectangleFromOrigin_ConstructedCorrectly() {
        Rectangle rect = new Rectangle(0, 0, 10, 20);
        assertEquals(10, rect.width());
        assertEquals(20, rect.height());
        assertEquals(0, rect.topLeftX());
        assertEquals(0, rect.topLeftY());
        assertFalse(rect.isEmpty());
    }

    @Test
    public void construct_NegativeRectangleAtCorner_ConstructedCorrectly() {
        Rectangle rect = new Rectangle(Integer.MIN_VALUE, Integer.MIN_VALUE, 10, 20);
        assertEquals(10, rect.width());
        assertEquals(20, rect.height());
        assertEquals(Integer.MIN_VALUE, rect.topLeftX());
        assertEquals(Integer.MIN_VALUE, rect.topLeftY());
        assertFalse(rect.isEmpty());
    }

    @Test
    public void containsPoint_NullPoint_NullPointerException() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        expectedException.expect(NullPointerException.class);
        rect.contains(null);
    }

    @Test
    public void containsPoint_PointIntMax_FalseIfNotInside() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(new Point(Integer.MAX_VALUE, Integer.MAX_VALUE)));
    }

    @Test
    public void containsPoint_PointIntMin_FalseIfNotInside() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(new Point(Integer.MIN_VALUE, Integer.MIN_VALUE)));
    }

    @Test
    public void containsPoint_PointTooFarLeft_False() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(new Point(-6, 1)));
    }

    @Test
    public void containsPoint_PointTooFarRight_False() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(new Point(11, 1)));
    }

    @Test
    public void containsPoint_PointTooFarUp_False() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(new Point(5, -3)));
    }

    @Test
    public void containsPoint_PointTooFarDown_False() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(new Point(5, 19)));
    }

    @Test
    public void containsPoint_PointOnLeftBorder_True() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertTrue(rect.contains(new Point(-5, -2)));
        assertTrue(rect.contains(new Point(-5, -1)));
        assertTrue(rect.contains(new Point(-5, 0)));
        assertTrue(rect.contains(new Point(-5, 1)));
        assertTrue(rect.contains(new Point(-5, 2)));
        assertTrue(rect.contains(new Point(-5, 3)));
        assertTrue(rect.contains(new Point(-5, 4)));
        assertTrue(rect.contains(new Point(-5, 5)));
        assertTrue(rect.contains(new Point(-5, 6)));
        assertTrue(rect.contains(new Point(-5, 7)));
        assertTrue(rect.contains(new Point(-5, 8)));
        assertTrue(rect.contains(new Point(-5, 9)));
        assertTrue(rect.contains(new Point(-5, 10)));
        assertTrue(rect.contains(new Point(-5, 11)));
        assertTrue(rect.contains(new Point(-5, 12)));
        assertTrue(rect.contains(new Point(-5, 13)));
        assertTrue(rect.contains(new Point(-5, 14)));
        assertTrue(rect.contains(new Point(-5, 15)));
        assertTrue(rect.contains(new Point(-5, 16)));
        assertTrue(rect.contains(new Point(-5, 17)));
    }

    @Test
    public void containsPoint_PointOnRightBorder_True() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertTrue(rect.contains(new Point(4, -2)));
        assertTrue(rect.contains(new Point(4, -1)));
        assertTrue(rect.contains(new Point(4, 0)));
        assertTrue(rect.contains(new Point(4, 1)));
        assertTrue(rect.contains(new Point(4, 2)));
        assertTrue(rect.contains(new Point(4, 3)));
        assertTrue(rect.contains(new Point(4, 4)));
        assertTrue(rect.contains(new Point(4, 5)));
        assertTrue(rect.contains(new Point(4, 6)));
        assertTrue(rect.contains(new Point(4, 7)));
        assertTrue(rect.contains(new Point(4, 8)));
        assertTrue(rect.contains(new Point(4, 9)));
        assertTrue(rect.contains(new Point(4, 10)));
        assertTrue(rect.contains(new Point(4, 11)));
        assertTrue(rect.contains(new Point(4, 12)));
        assertTrue(rect.contains(new Point(4, 13)));
        assertTrue(rect.contains(new Point(4, 14)));
        assertTrue(rect.contains(new Point(4, 15)));
        assertTrue(rect.contains(new Point(4, 16)));
        assertTrue(rect.contains(new Point(4, 17)));
    }

    @Test
    public void containsPoint_PointOnTopBorder_True() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertTrue(rect.contains(new Point(-5, -2)));
        assertTrue(rect.contains(new Point(-4, -2)));
        assertTrue(rect.contains(new Point(-3, -2)));
        assertTrue(rect.contains(new Point(-2, -2)));
        assertTrue(rect.contains(new Point(-1, -2)));
        assertTrue(rect.contains(new Point(0, -2)));
        assertTrue(rect.contains(new Point(1, -2)));
        assertTrue(rect.contains(new Point(2, -2)));
        assertTrue(rect.contains(new Point(3, -2)));
        assertTrue(rect.contains(new Point(4, -2)));
    }

    @Test
    public void containsPoint_PointOnBottomBorder_True() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertTrue(rect.contains(new Point(-5, 17)));
        assertTrue(rect.contains(new Point(-4, 17)));
        assertTrue(rect.contains(new Point(-3, 17)));
        assertTrue(rect.contains(new Point(-2, 17)));
        assertTrue(rect.contains(new Point(-1, 17)));
        assertTrue(rect.contains(new Point(0, 17)));
        assertTrue(rect.contains(new Point(1, 17)));
        assertTrue(rect.contains(new Point(2, 17)));
        assertTrue(rect.contains(new Point(3, 17)));
        assertTrue(rect.contains(new Point(4, 17)));
    }

    @Test
    public void containsPoint_PointInside_True() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        for (int x = -4; x <= 4; x++) {
            for (int y = -1; y <= 17; y++) {
                assertTrue(rect.contains(new Point(x, y)));
            }
        }
    }

    @Test
    public void containsCoordinates_PointIntMax_FalseIfNotInside() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    public void containsCoordinates_PointIntMin_FalseIfNotInside() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test
    public void containsCoordinates_PointTooFarLeft_False() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(-6, 1));
    }

    @Test
    public void containsCoordinates_PointTooFarRight_False() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(11, 1));
    }

    @Test
    public void containsCoordinates_PointTooFarUp_False() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(5, -3));
    }

    @Test
    public void containsCoordinates_PointTooFarDown_False() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertFalse(rect.contains(5, 19));
    }

    @Test
    public void containsCoordinates_PointOnLeftBorder_True() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertTrue(rect.contains(-5, -2));
        assertTrue(rect.contains(-5, -1));
        assertTrue(rect.contains(-5, 0));
        assertTrue(rect.contains(-5, 1));
        assertTrue(rect.contains(-5, 2));
        assertTrue(rect.contains(-5, 3));
        assertTrue(rect.contains(-5, 4));
        assertTrue(rect.contains(-5, 5));
        assertTrue(rect.contains(-5, 6));
        assertTrue(rect.contains(-5, 7));
        assertTrue(rect.contains(-5, 8));
        assertTrue(rect.contains(-5, 9));
        assertTrue(rect.contains(-5, 10));
        assertTrue(rect.contains(-5, 11));
        assertTrue(rect.contains(-5, 12));
        assertTrue(rect.contains(-5, 13));
        assertTrue(rect.contains(-5, 14));
        assertTrue(rect.contains(-5, 15));
        assertTrue(rect.contains(-5, 16));
        assertTrue(rect.contains(-5, 17));
    }

    @Test
    public void containsCoordinates_PointOnRightBorder_True() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertTrue(rect.contains(4, -2));
        assertTrue(rect.contains(4, -1));
        assertTrue(rect.contains(4, 0));
        assertTrue(rect.contains(4, 1));
        assertTrue(rect.contains(4, 2));
        assertTrue(rect.contains(4, 3));
        assertTrue(rect.contains(4, 4));
        assertTrue(rect.contains(4, 5));
        assertTrue(rect.contains(4, 6));
        assertTrue(rect.contains(4, 7));
        assertTrue(rect.contains(4, 8));
        assertTrue(rect.contains(4, 9));
        assertTrue(rect.contains(4, 10));
        assertTrue(rect.contains(4, 11));
        assertTrue(rect.contains(4, 12));
        assertTrue(rect.contains(4, 13));
        assertTrue(rect.contains(4, 14));
        assertTrue(rect.contains(4, 15));
        assertTrue(rect.contains(4, 16));
        assertTrue(rect.contains(4, 17));
    }

    @Test
    public void containsCoordinates_PointOnTopBorder_True() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertTrue(rect.contains(-5, -2));
        assertTrue(rect.contains(-4, -2));
        assertTrue(rect.contains(-3, -2));
        assertTrue(rect.contains(-2, -2));
        assertTrue(rect.contains(-1, -2));
        assertTrue(rect.contains(0, -2));
        assertTrue(rect.contains(1, -2));
        assertTrue(rect.contains(2, -2));
        assertTrue(rect.contains(3, -2));
        assertTrue(rect.contains(4, -2));
    }

    @Test
    public void containsCoordinates_PointOnBottomBorder_True() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        assertTrue(rect.contains(-5, 17));
        assertTrue(rect.contains(-4, 17));
        assertTrue(rect.contains(-3, 17));
        assertTrue(rect.contains(-2, 17));
        assertTrue(rect.contains(-1, 17));
        assertTrue(rect.contains(0, 17));
        assertTrue(rect.contains(1, 17));
        assertTrue(rect.contains(2, 17));
        assertTrue(rect.contains(3, 17));
        assertTrue(rect.contains(4, 17));
    }

    @Test
    public void containsCoordinates_PointInside_True() {
        Rectangle rect = new Rectangle(-5, -2, 10, 20);
        for (int x = -4; x <= 3; x++) {
            for (int y = -1; y <= 16; y++) {
                assertTrue(rect.contains(x, y));
            }
        }
    }

    @Test
    public void iterator_PositiveRectangleAtCorner_AllPointsIterated() {
        Rectangle rect = new Rectangle(Integer.MAX_VALUE - 10, Integer.MAX_VALUE - 20, 10, 20);
        testIteratedPoints(rect);
    }

    @Test
    public void iterator_NegativeRectangleAtCorner_AllPointsIterated() {
        Rectangle rect = new Rectangle(Integer.MIN_VALUE, Integer.MIN_VALUE, 10, 20);
        testIteratedPoints(rect);
    }

    @Test
    public void iterator_PositiveAndNegativeRectangle_AllPointsIterated() {
        Rectangle rect = new Rectangle(-5, -15, 10, 20);
        testIteratedPoints(rect);
    }

    @Test
    public void iterator_MaxWidthRectangleAtCorner_AllPointsIterated() {
        Rectangle rect = new Rectangle(0, Integer.MAX_VALUE - 2, Integer.MAX_VALUE, 2);

        // Storing all the points requires more than reasonable heap space, so just check the number of points
        long numExpectedPoints = Integer.MAX_VALUE * 2L;
        long numActualPoints = 0;
        for (Point ignored : rect) {
            numActualPoints++;
        }

        assertEquals(numExpectedPoints, numActualPoints);
    }

    @Test
    public void iterator_MaxHeightRectangleAtCorner_AllPointsIterated() {
        Rectangle rect = new Rectangle(Integer.MAX_VALUE - 2, 0, 2, Integer.MAX_VALUE);

        // Storing all the points requires more than reasonable heap space, so just check the number of points
        long numExpectedPoints = Integer.MAX_VALUE * 2L;
        long numActualPoints = 0;
        for (Point ignored : rect) {
            numActualPoints++;
        }

        assertEquals(numExpectedPoints, numActualPoints);
    }

    @Test
    public void iterator_NextAfterHasNextFalse_NoSuchElementException() {
        Rectangle rect = new Rectangle(-5, -15, 10, 20);
        Iterator<Point> iterator = rect.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }

        expectedException.expect(NoSuchElementException.class);
        iterator.next();
    }

    private static void testIteratedPoints(Rectangle rect) {
        Set<Point> expectedPoints = new HashSet<>();
        for (int x = 0; x <= rect.width() - 1; x++) {
            for (int y = 0; y <= rect.height() - 1; y++) {

                // The Rectangle class guarantees these coordinates will not overflow an integer
                expectedPoints.add(new Point(x + rect.topLeftX(), y + rect.topLeftY()));

            }
        }

        Set<Point> actualPoints = new HashSet<>();
        for (Point point : rect) {
            actualPoints.add(point);
        }

        assertEquals(expectedPoints, actualPoints);
    }

}