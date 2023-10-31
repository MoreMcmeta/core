/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import io.github.moremcmeta.moremcmeta.api.math.NegativeDimensionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link GuiScaling}.
 * @author soir20
 */
@SuppressWarnings({"SimplifiableAssertion", "EqualsBetweenInconvertibleTypes"})
public final class GuiScalingTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void constructNineSlice_NegativeLeft_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new GuiScaling.NineSlice(-1, 2, 3, 4);
    }

    @Test
    public void constructNineSlice_NegativeRight_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new GuiScaling.NineSlice(1, -2, 3, 4);
    }

    @Test
    public void constructNineSlice_NegativeTop_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new GuiScaling.NineSlice(1, 2, -3, 4);
    }

    @Test
    public void constructNineSlice_NegativeBottom_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new GuiScaling.NineSlice(1, 2, 3, -4);
    }

    @Test
    public void constructNineSlice_ZeroDimensions_NoException() {
        GuiScaling.NineSlice nineSlice = new GuiScaling.NineSlice(0, 0, 0, 0);
        assertEquals(0, nineSlice.left());
        assertEquals(0, nineSlice.right());
        assertEquals(0, nineSlice.top());
        assertEquals(0, nineSlice.bottom());
    }

    @Test
    public void equals_BothStretch_True() {
        assertTrue(new GuiScaling.Stretch().equals(new GuiScaling.Stretch()));
    }

    @Test
    public void equals_OneStretch_False() {
        assertFalse(new GuiScaling.Stretch().equals(new GuiScaling.Tile()));
    }

    @Test
    public void equals_BothTile_True() {
        assertTrue(new GuiScaling.Tile().equals(new GuiScaling.Tile()));
    }

    @Test
    public void equals_OneTile_False() {
        assertFalse(new GuiScaling.Tile().equals(new GuiScaling.Stretch()));
    }

    @Test
    public void equals_BothNineSliceSameDimensions_True() {
        assertTrue(new GuiScaling.NineSlice(1, 2, 3, 4).equals(new GuiScaling.NineSlice(1, 2, 3, 4)));
    }

    @Test
    public void equals_BothNineSliceDiffLeft_False() {
        assertFalse(new GuiScaling.NineSlice(1, 2, 3, 4).equals(new GuiScaling.NineSlice(2, 2, 3, 4)));
    }

    @Test
    public void equals_BothNineSliceDiffRight_False() {
        assertFalse(new GuiScaling.NineSlice(1, 2, 3, 4).equals(new GuiScaling.NineSlice(1, 3, 3, 4)));
    }

    @Test
    public void equals_BothNineSliceDiffTop_False() {
        assertFalse(new GuiScaling.NineSlice(1, 2, 3, 4).equals(new GuiScaling.NineSlice(1, 2, 4, 4)));
    }

    @Test
    public void equals_BothNineSliceDiffBottom_False() {
        assertFalse(new GuiScaling.NineSlice(1, 2, 3, 4).equals(new GuiScaling.NineSlice(1, 2, 3, 5)));
    }

    @Test
    public void equals_OneNineSlice_False() {
        assertFalse(new GuiScaling.NineSlice(1, 2, 3, 4).equals(new GuiScaling.Stretch()));
    }

    @Test
    public void hasCenterSlice_NegativeFrameWidth_NegativeDimensionException() {
        GuiScaling.NineSlice nineSlice = new GuiScaling.NineSlice(1, 2, 3, 4);
        expectedException.expect(NegativeDimensionException.class);
        nineSlice.hasCenterSlice(-1, 100);
    }

    @Test
    public void hasCenterSlice_NegativeFrameHeight_NegativeDimensionException() {
        GuiScaling.NineSlice nineSlice = new GuiScaling.NineSlice(1, 2, 3, 4);
        expectedException.expect(NegativeDimensionException.class);
        nineSlice.hasCenterSlice(100, -1);
    }

    @Test
    public void hasCenterSlice_FrameWidthZero_False() {
        assertFalse(new GuiScaling.NineSlice(1, 2, 3, 4).hasCenterSlice(0, 200));
    }

    @Test
    public void hasCenterSlice_FrameHeightZero_False() {
        assertFalse(new GuiScaling.NineSlice(1, 2, 3, 4).hasCenterSlice(100, 0));
    }

    @Test
    public void hasCenterSlice_SumHorizontalSameAsWidth_False() {
        assertFalse(new GuiScaling.NineSlice(50, 50, 1, 1).hasCenterSlice(100, 200));
    }

    @Test
    public void hasCenterSlice_SumHorizontalMoreThanWidth_False() {
        assertFalse(new GuiScaling.NineSlice(51, 50, 1, 1).hasCenterSlice(100, 200));
    }

    @Test
    public void hasCenterSlice_SumVerticalSameAsHeight_False() {
        assertFalse(new GuiScaling.NineSlice(1, 1, 100, 100).hasCenterSlice(100, 200));
    }

    @Test
    public void hasCenterSlice_SumVerticalMoreThanHeight_False() {
        assertFalse(new GuiScaling.NineSlice(1, 1, 101, 100).hasCenterSlice(100, 200));
    }

    @Test
    public void hasCenterSlice_BothSumsLessThanDimensions_True() {
        assertTrue(new GuiScaling.NineSlice(1, 2, 3, 5).hasCenterSlice(100, 200));
    }

}