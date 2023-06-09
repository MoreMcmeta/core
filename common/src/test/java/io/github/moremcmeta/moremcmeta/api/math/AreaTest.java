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

package io.github.moremcmeta.moremcmeta.api.math;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link Area} as a rectangle and as a free-form area.
 * @author soir20
 */
public final class AreaTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NegativeWidth_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new Area(0, 0, -1, 10);
    }

    @Test
    public void construct_NegativeHeight_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new Area(0, 0, 10, -1);
    }

    @Test
    public void construct_NegativeWidthAndHeight_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new Area(0, 0, -1, -1);
    }

    @Test
    public void construct_OverflowWidth_RectangleOverflowException() {
        expectedException.expect(RectangleOverflowException.class);
        new Area(Integer.MAX_VALUE - 10, 0, 11, 10);
    }

    @Test
    public void construct_OverflowHeight_RectangleOverflowException() {
        expectedException.expect(RectangleOverflowException.class);
        new Area(0, Integer.MAX_VALUE - 10, 10, 11);
    }

    @Test
    public void construct_OverflowWidthMaxValue_RectangleOverflowException() {
        expectedException.expect(RectangleOverflowException.class);
        new Area(1, 0, Integer.MAX_VALUE, 10);
    }

    @Test
    public void construct_OverflowHeightMaxValue_RectangleOverflowException() {
        expectedException.expect(RectangleOverflowException.class);
        new Area(0, 1, 10, Integer.MAX_VALUE);
    }

    @Test
    public void construct_EmptyArea_ConstructedCorrectly() {
        Area rect = new Area(0, 0, 0, 0);
        assertFalse(rect.iterator().hasNext());
    }

    @Test
    public void construct_EmptyWidthArea_ConstructedCorrectly() {
        Area rect = new Area(0, 0, 0, 10);
        assertFalse(rect.iterator().hasNext());
    }

    @Test
    public void construct_EmptyHeightArea_ConstructedCorrectly() {
        Area rect = new Area(0, 0, 10, 0);
        assertFalse(rect.iterator().hasNext());
    }

    @Test
    public void iterator_PositiveAreaAtCorner_AllPointsIterated() {
        Area rect = new Area(Integer.MAX_VALUE - 10, Integer.MAX_VALUE - 20, 10, 20);
        testIteratedRectangle(rect, Integer.MAX_VALUE - 10, Integer.MAX_VALUE - 20, 10, 20);
    }

    @Test
    public void iterator_NegativeAreaAtCorner_AllPointsIterated() {
        Area rect = new Area(Integer.MIN_VALUE, Integer.MIN_VALUE, 10, 20);
        testIteratedRectangle(rect, Integer.MIN_VALUE, Integer.MIN_VALUE, 10, 20);
    }

    @Test
    public void iterator_PositiveAndNegativeArea_AllPointsIterated() {
        Area rect = new Area(-5, -15, 11, 19);
        testIteratedRectangle(rect, -5, -15, 11, 19);
    }

    @Test
    public void iterator_NextAfterHasNextFalse_NoSuchElementException() {
        Area rect = new Area(-5, -15, 10, 20);
        LongIterator iterator = rect.iterator();
        while (iterator.hasNext()) {
            iterator.nextLong();
        }

        expectedException.expect(NoSuchElementException.class);
        iterator.nextLong();
    }

    @Test
    public void buildArea_NoPoints_Empty() {
        Area.Builder builder = new Area.Builder();
        Area area = builder.build();
        assertFalse(area.iterator().hasNext());
    }

    @Test
    public void buildArea_OrderedPoints_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(1, 4));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 3));

        for (long point : points) {
            builder.addPixel(Point.x(point), Point.y(point));
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildArea_UnorderedPoints_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(2, 2));
        points.add(Point.pack(0, 0));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(2, 3));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(1, 4));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(1, 9));

        for (long point : points) {
            builder.addPixel(Point.x(point), Point.y(point));
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildArea_NegativePoints_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(-2, 0));
        points.add(Point.pack(-2, 1));
        points.add(Point.pack(-2, 2));
        points.add(Point.pack(-2, 3));
        points.add(Point.pack(-1, -9));
        points.add(Point.pack(-1, -8));
        points.add(Point.pack(-1, -4));
        points.add(Point.pack(-1, -3));
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        for (long point : points) {
            builder.addPixel(Point.x(point), Point.y(point));
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildArea_DuplicatePoints_AreaDoesNotDuplicatePoints() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(1, 4));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 3));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(1, 9));
        for (long point : points) {
            builder.addPixel(Point.x(point), Point.y(point));
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertTrue(areaPoints.stream().allMatch(new HashSet<>()::add));
    }

    @Test
    public void buildArea_SinglePixelFirstInRow_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 3));

        for (long point : points) {
            builder.addPixel(Point.x(point), Point.y(point));
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildArea_SinglePixelMiddleInRow_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(1, 4));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(1, 12));
        points.add(Point.pack(1, 13));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 3));

        for (long point : points) {
            builder.addPixel(Point.x(point), Point.y(point));
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildArea_SinglePixelLastInRow_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(1, 4));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(1, 13));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 3));

        for (long point : points) {
            builder.addPixel(Point.x(point), Point.y(point));
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildAreaPointObjects_OrderedPoints_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(1, 4));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 3));

        for (long point : points) {
            builder.addPixel(point);
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildAreaPointObjects_UnorderedPoints_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(2, 2));
        points.add(Point.pack(0, 0));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(2, 3));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(1, 4));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(1, 9));

        for (long point : points) {
            builder.addPixel(point);
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildAreaPointObjects_NegativePoints_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(-2, 0));
        points.add(Point.pack(-2, 1));
        points.add(Point.pack(-2, 2));
        points.add(Point.pack(-2, 3));
        points.add(Point.pack(-1, -9));
        points.add(Point.pack(-1, -8));
        points.add(Point.pack(-1, -4));
        points.add(Point.pack(-1, -3));
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        for (long point : points) {
            builder.addPixel(point);
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildAreaPointObjects_DuplicatePoints_AreaDoesNotDuplicatePoints() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(1, 4));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 3));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(1, 9));
        for (long point : points) {
            builder.addPixel(point);
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertTrue(areaPoints.stream().allMatch(new HashSet<>()::add));
    }

    @Test
    public void buildAreaPointObjects_SinglePixelFirstInRow_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 3));

        for (long point : points) {
            builder.addPixel(point);
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildAreaPointObjects_SinglePixelMiddleInRow_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(1, 4));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(1, 12));
        points.add(Point.pack(1, 13));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 3));

        for (long point : points) {
            builder.addPixel(point);
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildAreaPointObjects_SinglePixelLastInRow_AllAdded() {
        Area.Builder builder = new Area.Builder();
        List<Long> points = new ArrayList<>();
        points.add(Point.pack(0, 0));
        points.add(Point.pack(0, 1));
        points.add(Point.pack(0, 2));
        points.add(Point.pack(1, 3));
        points.add(Point.pack(1, 4));
        points.add(Point.pack(1, 8));
        points.add(Point.pack(1, 9));
        points.add(Point.pack(1, 13));
        points.add(Point.pack(2, 0));
        points.add(Point.pack(2, 1));
        points.add(Point.pack(2, 2));
        points.add(Point.pack(2, 3));

        for (long point : points) {
            builder.addPixel(point);
        }

        Area area = builder.build();

        List<Long> areaPoints = new ArrayList<>();
        for (long point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    private static void testIteratedRectangle(Area rect, int topLeftX, int topLeftY, int width, int height) {
        Set<Long> expectedPoints = new HashSet<>();
        for (int x = 0; x <= width - 1; x++) {
            for (int y = 0; y <= height - 1; y++) {

                // The Area class guarantees these coordinates will not overflow an integer
                expectedPoints.add(Point.pack(x + topLeftX, y + topLeftY));

            }
        }

        Set<Long> actualPoints = new HashSet<>();
        for (long point : rect) {
            actualPoints.add(point);
        }

        assertEquals(expectedPoints, actualPoints);
    }

}