package io.github.soir20.moremcmeta.math;

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

}