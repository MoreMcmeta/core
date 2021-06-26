package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.math.Point;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the building and use of the {@link IRGBAImage.VisibleArea} class.
 * @author soir20
 */
public class VisibleAreaTest {

    @Test
    public void buildVisibleArea_NoPoints_Empty() {
        IRGBAImage.VisibleArea.Builder builder = new IRGBAImage.VisibleArea.Builder();
        IRGBAImage.VisibleArea area = builder.build();
        assertFalse(area.iterator().hasNext());
    }

    @Test
    public void buildVisibleArea_OrderedPoints_AllAdded() {
        IRGBAImage.VisibleArea.Builder builder = new IRGBAImage.VisibleArea.Builder();
        List<Point> points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(0, 1));
        points.add(new Point(0, 2));
        points.add(new Point(1, 3));
        points.add(new Point(1, 4));
        points.add(new Point(1, 8));
        points.add(new Point(1, 9));
        points.add(new Point(2, 0));
        points.add(new Point(2, 1));
        points.add(new Point(2, 2));
        points.add(new Point(2, 3));

        for (Point point : points) {
            builder.addPixel(point.getX(), point.getY());
        }

        IRGBAImage.VisibleArea area = builder.build();

        List<Point> areaPoints = new ArrayList<>();
        for (Point point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildVisibleArea_UnorderedPoints_AllAdded() {
        IRGBAImage.VisibleArea.Builder builder = new IRGBAImage.VisibleArea.Builder();
        List<Point> points = new ArrayList<>();
        points.add(new Point(2, 2));
        points.add(new Point(0, 0));
        points.add(new Point(1, 3));
        points.add(new Point(2, 3));
        points.add(new Point(1, 8));
        points.add(new Point(0, 1));
        points.add(new Point(2, 1));
        points.add(new Point(1, 4));
        points.add(new Point(0, 2));
        points.add(new Point(2, 0));
        points.add(new Point(1, 9));

        for (Point point : points) {
            builder.addPixel(point.getX(), point.getY());
        }

        IRGBAImage.VisibleArea area = builder.build();

        List<Point> areaPoints = new ArrayList<>();
        for (Point point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildVisibleArea_NegativePoints_AllAdded() {
        IRGBAImage.VisibleArea.Builder builder = new IRGBAImage.VisibleArea.Builder();
        List<Point> points = new ArrayList<>();
        points.add(new Point(-2, 0));
        points.add(new Point(-2, 1));
        points.add(new Point(-2, 2));
        points.add(new Point(-2, 3));
        points.add(new Point(-1, -9));
        points.add(new Point(-1, -8));
        points.add(new Point(-1, -4));
        points.add(new Point(-1, -3));
        points.add(new Point(0, 0));
        points.add(new Point(0, 1));
        points.add(new Point(0, 2));
        for (Point point : points) {
            builder.addPixel(point.getX(), point.getY());
        }

        IRGBAImage.VisibleArea area = builder.build();

        List<Point> areaPoints = new ArrayList<>();
        for (Point point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildVisibleArea_DuplicatePoints_AreaDoesNotDuplicatePoints() {
        IRGBAImage.VisibleArea.Builder builder = new IRGBAImage.VisibleArea.Builder();
        List<Point> points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(0, 1));
        points.add(new Point(0, 1));
        points.add(new Point(0, 2));
        points.add(new Point(1, 3));
        points.add(new Point(1, 4));
        points.add(new Point(1, 8));
        points.add(new Point(1, 9));
        points.add(new Point(1, 9));
        points.add(new Point(1, 9));
        points.add(new Point(2, 0));
        points.add(new Point(2, 1));
        points.add(new Point(2, 2));
        points.add(new Point(2, 2));
        points.add(new Point(2, 2));
        points.add(new Point(2, 3));
        points.add(new Point(1, 9));
        points.add(new Point(1, 9));
        for (Point point : points) {
            builder.addPixel(point.getX(), point.getY());
        }

        IRGBAImage.VisibleArea area = builder.build();

        List<Point> areaPoints = new ArrayList<>();
        for (Point point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertTrue(areaPoints.stream().allMatch(new HashSet<Point>()::add));
    }

    @Test
    public void buildVisibleArea_SinglePixelFirstInRow_AllAdded() {
        IRGBAImage.VisibleArea.Builder builder = new IRGBAImage.VisibleArea.Builder();
        List<Point> points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(0, 1));
        points.add(new Point(0, 2));
        points.add(new Point(1, 3));
        points.add(new Point(1, 8));
        points.add(new Point(1, 9));
        points.add(new Point(2, 0));
        points.add(new Point(2, 1));
        points.add(new Point(2, 2));
        points.add(new Point(2, 3));

        for (Point point : points) {
            builder.addPixel(point.getX(), point.getY());
        }

        IRGBAImage.VisibleArea area = builder.build();

        List<Point> areaPoints = new ArrayList<>();
        for (Point point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildVisibleArea_SinglePixelMiddleInRow_AllAdded() {
        IRGBAImage.VisibleArea.Builder builder = new IRGBAImage.VisibleArea.Builder();
        List<Point> points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(0, 1));
        points.add(new Point(0, 2));
        points.add(new Point(1, 3));
        points.add(new Point(1, 4));
        points.add(new Point(1, 8));
        points.add(new Point(1, 12));
        points.add(new Point(1, 13));
        points.add(new Point(2, 0));
        points.add(new Point(2, 1));
        points.add(new Point(2, 2));
        points.add(new Point(2, 3));

        for (Point point : points) {
            builder.addPixel(point.getX(), point.getY());
        }

        IRGBAImage.VisibleArea area = builder.build();

        List<Point> areaPoints = new ArrayList<>();
        for (Point point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }

    @Test
    public void buildVisibleArea_SinglePixelLastInRow_AllAdded() {
        IRGBAImage.VisibleArea.Builder builder = new IRGBAImage.VisibleArea.Builder();
        List<Point> points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(0, 1));
        points.add(new Point(0, 2));
        points.add(new Point(1, 3));
        points.add(new Point(1, 4));
        points.add(new Point(1, 8));
        points.add(new Point(1, 9));
        points.add(new Point(1, 13));
        points.add(new Point(2, 0));
        points.add(new Point(2, 1));
        points.add(new Point(2, 2));
        points.add(new Point(2, 3));

        for (Point point : points) {
            builder.addPixel(point.getX(), point.getY());
        }

        IRGBAImage.VisibleArea area = builder.build();

        List<Point> areaPoints = new ArrayList<>();
        for (Point point : area) {
            areaPoints.add(point);
        }

        assertTrue(areaPoints.containsAll(points));
        assertEquals(points.size(), areaPoints.size());
    }
}