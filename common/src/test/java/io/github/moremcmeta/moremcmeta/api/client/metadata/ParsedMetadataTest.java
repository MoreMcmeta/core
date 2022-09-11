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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import io.github.moremcmeta.moremcmeta.api.math.NegativeDimensionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the default methods of the {@link ParsedMetadata} class and {@link .ParsedMetadata.FrameSize}.
 * @author soir20
 */
public class ParsedMetadataTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void frameSize_NotOverridden_Empty() {
        assertFalse((new ParsedMetadata() {}).frameSize().isPresent());
    }

    @Test
    public void blur_NotOverridden_Empty() {
        assertFalse((new ParsedMetadata() {}).blur().isPresent());
    }

    @Test
    public void clamp_NotOverridden_Empty() {
        assertFalse((new ParsedMetadata() {}).clamp().isPresent());
    }

    @Test
    public void frameSizeConstruct_NegativeWidth_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new ParsedMetadata.FrameSize(-1, 2);
    }

    @Test
    public void frameSizeConstruct_NegativeHeight_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new ParsedMetadata.FrameSize(1, -2);
    }

    @Test
    public void frameSizeConstruct_NegativeWidthAndHeight_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new ParsedMetadata.FrameSize(-1, -2);
    }

    @Test
    public void frameSizeWidth_WidthDifferentThanHeight_GetsWidth() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        assertEquals(1, first.width());
    }

    @Test
    public void frameSizeWidth_WidthSameAsHeight_GetsWidth() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(2, 2);
        assertEquals(2, first.width());
    }

    @Test
    public void frameSizeHeight_HeightDifferentThanWidth_GetsHeight() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        assertEquals(2, first.height());
    }

    @Test
    public void frameSizeHeight_HeightSameAsWidth_GetsHeight() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(2, 2);
        assertEquals(2, first.height());
    }

    @Test
    public void frameSizeEquals_SameFrameSizes_Reflexive() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        assertEquals(first, first);
    }

    @Test
    public void frameSizeEquals_SameFrameSizes_Symmetric() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        ParsedMetadata.FrameSize second = new ParsedMetadata.FrameSize(1, 2);
        assertEquals(first, second);
        assertEquals(second, first);
    }

    @Test
    public void frameSizeEquals_SameFrameSizes_Transitive() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        ParsedMetadata.FrameSize second = new ParsedMetadata.FrameSize(1, 2);
        ParsedMetadata.FrameSize third = new ParsedMetadata.FrameSize(1, 2);
        assertEquals(first, second);
        assertEquals(second, third);
        assertEquals(first, third);
    }

    @Test
    public void frameSizeEquals_DiffFrameSizes_Symmetric() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        ParsedMetadata.FrameSize second = new ParsedMetadata.FrameSize(3, 4);
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    public void frameSizeEquals_DiffTypes_Symmetric() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        Object second = new Object();
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    public void frameSizeHashCode_SameFrameSizes_Reflexive() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        assertEquals(first.hashCode(), first.hashCode());
    }

    @Test
    public void frameSizeHashCode_SameFrameSizes_Symmetric() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        ParsedMetadata.FrameSize second = new ParsedMetadata.FrameSize(1, 2);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), first.hashCode());
    }

    @Test
    public void frameSizeHashCode_SameFrameSizes_Transitive() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        ParsedMetadata.FrameSize second = new ParsedMetadata.FrameSize(1, 2);
        ParsedMetadata.FrameSize third = new ParsedMetadata.FrameSize(1, 2);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), third.hashCode());
        assertEquals(first.hashCode(), third.hashCode());
    }

    @Test
    public void frameSizeHashCode_DiffFrameSizes_Symmetric() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        ParsedMetadata.FrameSize second = new ParsedMetadata.FrameSize(3, 4);
        assertNotEquals(first.hashCode(), second.hashCode());
        assertNotEquals(second.hashCode(), first.hashCode());
    }

    @Test
    public void frameSizeHashCode_DiffTypes_Symmetric() {
        ParsedMetadata.FrameSize first = new ParsedMetadata.FrameSize(1, 2);
        Object second = new Object();
        assertNotEquals(first.hashCode(), second.hashCode());
        assertNotEquals(second.hashCode(), first.hashCode());
    }

}