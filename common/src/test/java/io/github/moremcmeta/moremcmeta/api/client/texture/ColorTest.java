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
        Color first = new Color(0, 200, 50, 100);
        assertEquals(0, first.red());
        assertEquals(200, first.green());
        assertEquals(50, first.blue());
        assertEquals(100, first.alpha());
    }

    @Test
    public void constructSeparate_TooHighRed_IllegalRGBAComponentException() {
        expectedException.expect(Color.IllegalRGBAComponentException.class);
        new Color(256, 200, 50, 100);
    }

    @Test
    public void constructSeparate_NegativeRed_CorrectComponents() {
        expectedException.expect(Color.IllegalRGBAComponentException.class);
        new Color(-1, 200, 50, 100);
    }

    @Test
    public void constructSeparate_0Green_CorrectComponents() {
        Color first = new Color(75, 0, 50, 100);
        assertEquals(75, first.red());
        assertEquals(0, first.green());
        assertEquals(50, first.blue());
        assertEquals(100, first.alpha());
    }

    @Test
    public void constructSeparate_TooHighGreen_IllegalRGBAComponentException() {
        expectedException.expect(Color.IllegalRGBAComponentException.class);
        new Color(75, 256, 50, 100);
    }

    @Test
    public void constructSeparate_NegativeGreen_CorrectComponents() {
        expectedException.expect(Color.IllegalRGBAComponentException.class);
        new Color(75, -1, 50, 100);
    }

    @Test
    public void constructSeparate_0Blue_CorrectComponents() {
        Color first = new Color(75, 200, 0, 100);
        assertEquals(75, first.red());
        assertEquals(200, first.green());
        assertEquals(0, first.blue());
        assertEquals(100, first.alpha());
    }

    @Test
    public void constructSeparate_TooHighBlue_IllegalRGBAComponentException() {
        expectedException.expect(Color.IllegalRGBAComponentException.class);
        new Color(75, 200, 256, 100);
    }

    @Test
    public void constructSeparate_NegativeBlue_CorrectComponents() {
        expectedException.expect(Color.IllegalRGBAComponentException.class);
        new Color(75, 200, -1, 100);
    }

    @Test
    public void constructSeparate_0Alpha_CorrectComponents() {
        Color first = new Color(75, 200, 50, 0);
        assertEquals(75, first.red());
        assertEquals(200, first.green());
        assertEquals(50, first.blue());
        assertEquals(0, first.alpha());
    }

    @Test
    public void constructSeparate_TooHighAlpha_IllegalRGBAComponentException() {
        expectedException.expect(Color.IllegalRGBAComponentException.class);
        new Color(75, 200, 50, 256);
    }

    @Test
    public void constructSeparate_NegativeAlpha_CorrectComponents() {
        expectedException.expect(Color.IllegalRGBAComponentException.class);
        new Color(75, 200, 50, -1);
    }

    @Test
    public void constructCombined_Positive_CorrectComponents() {
        Color first = new Color(1682688050);
        assertEquals(75, first.red());
        assertEquals(200, first.green());
        assertEquals(50, first.blue());
        assertEquals(100, first.alpha());
    }

    @Test
    public void constructCombined_Negative_CorrectComponents() {
        Color first = new Color(-464795598);
        assertEquals(75, first.red());
        assertEquals(200, first.green());
        assertEquals(50, first.blue());
        assertEquals(228, first.alpha());
    }

    @Test
    public void constructCombined_All0_CorrectComponents() {
        Color first = new Color(0);
        assertEquals(0, first.red());
        assertEquals(0, first.green());
        assertEquals(0, first.blue());
        assertEquals(0, first.alpha());
    }

    @Test
    public void constructCombined_0Alpha_CorrectComponents() {
        Color first = new Color(4966450);
        assertEquals(75, first.red());
        assertEquals(200, first.green());
        assertEquals(50, first.blue());
        assertEquals(0, first.alpha());
    }

    @Test
    public void combine_CombinedPositive_CorrectColorReturned() {
        Color first = new Color(75, 200, 50, 100);
        assertEquals(1682688050, first.combine());
    }

    @Test
    public void combine_CombinedNegative_CorrectColorReturned() {
        Color first = new Color(75, 200, 50, 228);
        assertEquals(-464795598, first.combine());
    }

    @Test
    public void combine_Combined0_CorrectColorReturned() {
        Color first = new Color(0, 0, 0, 0);
        assertEquals(0, first.combine());
    }

    @Test
    public void combine_Combined0Alpha_CorrectColorReturned() {
        Color first = new Color(75, 200, 50, 0);
        assertEquals(4966450, first.combine());
    }

    @Test
    public void equals_SameColors_Reflexive() {
        Color first = new Color(1, 2, 3, 4);
        assertEquals(first, first);
    }

    @Test
    public void equals_SameColors_Symmetric() {
        Color first = new Color(1, 2, 3, 4);
        Color second = new Color(1, 2, 3, 4);
        assertEquals(first, second);
        assertEquals(second, first);
    }

    @Test
    public void equals_SameColors_Transitive() {
        Color first = new Color(1, 2, 3, 4);
        Color second = new Color(1, 2, 3, 4);
        Color third = new Color(1, 2, 3, 4);
        assertEquals(first, second);
        assertEquals(second, third);
        assertEquals(first, third);
    }

    @Test
    public void equals_DiffColors_Symmetric() {
        Color first = new Color(1, 2, 3, 4);
        Color second = new Color(4, 3, 2, 1);
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    public void equals_DiffTypes_Symmetric() {
        Color first = new Color(1, 2, 3, 4);
        Object second = new Object();
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    public void hashCode_SameColors_Reflexive() {
        Color first = new Color(1, 2, 3, 4);
        assertEquals(first.hashCode(), first.hashCode());
    }

    @Test
    public void hashCode_SameColors_Symmetric() {
        Color first = new Color(1, 2, 3, 4);
        Color second = new Color(1, 2, 3, 4);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), first.hashCode());
    }

    @Test
    public void hashCode_SameColors_Transitive() {
        Color first = new Color(1, 2, 3, 4);
        Color second = new Color(1, 2, 3, 4);
        Color third = new Color(1, 2, 3, 4);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), third.hashCode());
        assertEquals(first.hashCode(), third.hashCode());
    }

    @Test
    public void hashCode_DiffColors_Symmetric() {
        Color first = new Color(1, 2, 3, 4);
        Color second = new Color(4, 3, 2, 1);
        assertNotEquals(first.hashCode(), second.hashCode());
        assertNotEquals(second.hashCode(), first.hashCode());
    }

    @Test
    public void hashCode_DiffTypes_Symmetric() {
        Color first = new Color(1, 2, 3, 4);
        Object second = new Object();
        assertNotEquals(first.hashCode(), second.hashCode());
        assertNotEquals(second.hashCode(), first.hashCode());
    }

}