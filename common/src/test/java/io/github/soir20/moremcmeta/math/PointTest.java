package io.github.soir20.moremcmeta.math;

import static org.junit.Assert.*;

/**
 * Tests the {@link Point}.
 * @author soir20
 */
public class PointTest {

    public void getX_SomePoint_CorrectCoordinate() {
        Point point = new Point(1, 2);
        assertEquals(1, point.getX());
    }

    public void getY_SomePoint_CorrectCoordinate() {
        Point point = new Point(1, 2);
        assertEquals(2, point.getY());
    }

    public void equals_SamePoints_Reflexive() {
        Point first = new Point(1, 2);
        assertEquals(first, first);
    }

    public void equals_SamePoints_Symmetric() {
        Point first = new Point(1, 2);
        Point second = new Point(1, 2);
        assertEquals(first, second);
        assertEquals(second, first);
    }

    public void equals_SamePoints_Transitive() {
        Point first = new Point(1, 2);
        Point second = new Point(1, 2);
        Point third = new Point(1, 2);
        assertEquals(first, second);
        assertEquals(second, third);
        assertEquals(first, third);
    }

    public void equals_DiffPoints_Symmetric() {
        Point first = new Point(1, 2);
        Point second = new Point(3, 4);
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    public void equals_DiffTypes_Symmetric() {
        Point first = new Point(1, 2);
        Object second = new Object();
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    public void hashCode_SamePoints_Reflexive() {
        Point first = new Point(1, 2);
        assertEquals(first.hashCode(), first.hashCode());
    }

    public void hashCode_SamePoints_Symmetric() {
        Point first = new Point(1, 2);
        Point second = new Point(1, 2);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), first.hashCode());
    }

    public void hashCode_SamePoints_Transitive() {
        Point first = new Point(1, 2);
        Point second = new Point(1, 2);
        Point third = new Point(1, 2);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), third.hashCode());
        assertEquals(first.hashCode(), third.hashCode());
    }

    public void hashCode_DiffPoints_Symmetric() {
        Point first = new Point(1, 2);
        Point second = new Point(3, 4);
        assertNotEquals(first.hashCode(), second.hashCode());
        assertNotEquals(second.hashCode(), first.hashCode());
    }

    public void hashCode_DiffTypes_Symmetric() {
        Point first = new Point(1, 2);
        Object second = new Object();
        assertNotEquals(first.hashCode(), second.hashCode());
        assertNotEquals(second.hashCode(), first.hashCode());
    }

}