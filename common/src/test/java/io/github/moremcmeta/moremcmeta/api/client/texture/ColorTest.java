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

package io.github.moremcmeta.moremcmeta.api.client.texture;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link Color}.
 * @author soir20
 */
public class ColorTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void constructSeparate_0Red_IllegalRGBAComponentException() {
        int first = Color.pack(0, 200, 50, 100);
        assertEquals(0, Color.red(first));
        assertEquals(200, Color.green(first));
        assertEquals(50, Color.blue(first));
        assertEquals(100, Color.alpha(first));
    }

    @Test
    public void constructSeparate_TooHighRed_IllegalRGBAComponentException() {
        expectedException.expect(IllegalRGBAComponentException.class);
        Color.pack(256, 200, 50, 100);
    }

    @Test
    public void constructSeparate_NegativeRed_CorrectComponents() {
        expectedException.expect(IllegalRGBAComponentException.class);
        Color.pack(-1, 200, 50, 100);
    }

    @Test
    public void constructSeparate_0Green_CorrectComponents() {
        int first = Color.pack(75, 0, 50, 100);
        assertEquals(75, Color.red(first));
        assertEquals(0, Color.green(first));
        assertEquals(50, Color.blue(first));
        assertEquals(100, Color.alpha(first));
    }

    @Test
    public void constructSeparate_TooHighGreen_IllegalRGBAComponentException() {
        expectedException.expect(IllegalRGBAComponentException.class);
        Color.pack(75, 256, 50, 100);
    }

    @Test
    public void constructSeparate_NegativeGreen_CorrectComponents() {
        expectedException.expect(IllegalRGBAComponentException.class);
        Color.pack(75, -1, 50, 100);
    }

    @Test
    public void constructSeparate_0Blue_CorrectComponents() {
        int first = Color.pack(75, 200, 0, 100);
        assertEquals(75, Color.red(first));
        assertEquals(200, Color.green(first));
        assertEquals(0, Color.blue(first));
        assertEquals(100, Color.alpha(first));
    }

    @Test
    public void constructSeparate_TooHighBlue_IllegalRGBAComponentException() {
        expectedException.expect(IllegalRGBAComponentException.class);
        Color.pack(75, 200, 256, 100);
    }

    @Test
    public void constructSeparate_NegativeBlue_CorrectComponents() {
        expectedException.expect(IllegalRGBAComponentException.class);
        Color.pack(75, 200, -1, 100);
    }

    @Test
    public void constructSeparate_0Alpha_CorrectComponents() {
        int first = Color.pack(75, 200, 50, 0);
        assertEquals(75, Color.red(first));
        assertEquals(200, Color.green(first));
        assertEquals(50, Color.blue(first));
        assertEquals(0, Color.alpha(first));
    }

    @Test
    public void constructSeparate_TooHighAlpha_IllegalRGBAComponentException() {
        expectedException.expect(IllegalRGBAComponentException.class);
        Color.pack(75, 200, 50, 256);
    }

    @Test
    public void constructSeparate_NegativeAlpha_CorrectComponents() {
        expectedException.expect(IllegalRGBAComponentException.class);
        Color.pack(75, 200, 50, -1);
    }

    @Test
    public void equalsOrBothInvisible_SameColors_Reflexive() {
        int first = Color.pack(1, 2, 3, 4);
        assertTrue(Color.equalsOrBothInvisible(first, first));
    }

    @Test
    public void equalsOrBothInvisible_SameColors_Symmetric() {
        int first = Color.pack(1, 2, 3, 4);
        int second = Color.pack(1, 2, 3, 4);
        assertTrue(Color.equalsOrBothInvisible(first, second));
        assertTrue(Color.equalsOrBothInvisible(second, first));
    }

    @Test
    public void equalsOrBothInvisible_SameColors_Transitive() {
        int first = Color.pack(1, 2, 3, 4);
        int second = Color.pack(1, 2, 3, 4);
        int third = Color.pack(1, 2, 3, 4);
        assertTrue(Color.equalsOrBothInvisible(first, second));
        assertTrue(Color.equalsOrBothInvisible(second, third));
        assertTrue(Color.equalsOrBothInvisible(first, third));
    }

    @Test
    public void equalsOrBothInvisible_DiffColors_Symmetric() {
        int first = Color.pack(1, 2, 3, 4);
        int second = Color.pack(4, 3, 2, 1);
        assertFalse(Color.equalsOrBothInvisible(first, second));
        assertFalse(Color.equalsOrBothInvisible(second, first));
    }

    @Test
    public void equalsOrBothInvisible_BothTransparentDiffComponents_Symmetric() {
        int first = Color.pack(4, 2, 3, 0);
        int second = Color.pack(7, 3, 5, 0);
        assertTrue(Color.equalsOrBothInvisible(first, second));
        assertTrue(Color.equalsOrBothInvisible(second, first));
    }

    @Test
    public void equalsOrBothInvisible_BothPartiallyTransparentDiffComponents_Symmetric() {
        int first = Color.pack(4, 2, 3, 10);
        int second = Color.pack(7, 3, 5, 10);
        assertFalse(Color.equalsOrBothInvisible(first, second));
        assertFalse(Color.equalsOrBothInvisible(second, first));
    }

    @Test
    public void equals_SameColors_Reflexive() {
        int first = Color.pack(1, 2, 3, 4);
        assertEquals(first, first);
    }

    @Test
    public void equals_SameColors_Symmetric() {
        int first = Color.pack(1, 2, 3, 4);
        int second = Color.pack(1, 2, 3, 4);
        assertEquals(first, second);
        assertEquals(second, first);
    }

    @Test
    public void equals_SameColors_Transitive() {
        int first = Color.pack(1, 2, 3, 4);
        int second = Color.pack(1, 2, 3, 4);
        int third = Color.pack(1, 2, 3, 4);
        assertEquals(first, second);
        assertEquals(second, third);
        assertEquals(first, third);
    }

    @Test
    public void equals_DiffColors_Symmetric() {
        int first = Color.pack(1, 2, 3, 4);
        int second = Color.pack(4, 3, 2, 1);
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    public void equals_DiffTypes_Symmetric() {
        int first = Color.pack(1, 2, 3, 4);
        Object second = new Object();
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

}