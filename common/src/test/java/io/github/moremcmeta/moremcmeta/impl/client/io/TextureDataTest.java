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

package io.github.moremcmeta.moremcmeta.impl.client.io;

import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentBuilder;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import io.github.moremcmeta.moremcmeta.api.math.NegativeDimensionException;
import io.github.moremcmeta.moremcmeta.impl.client.texture.MockCloseableImage;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the link {@link TextureData} container.
 * @author soir20
 */
public class TextureDataTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_FrameWidthSameAsImage_NoException() {
        new TextureData<>(
                new TextureData.FrameSize(100, 10),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );
    }

    @Test
    public void construct_FrameWidthLargerThanImage_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new TextureData<>(
                new TextureData.FrameSize(101, 10),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );
    }

    @Test
    public void construct_FrameWidthMaxInt_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new TextureData<>(
                new TextureData.FrameSize(Integer.MAX_VALUE, 10),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );
    }

    @Test
    public void construct_ZeroFrameHeight_NoException() {
        new TextureData<>(
                new TextureData.FrameSize(10, 0),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );
    }

    @Test
    public void construct_ImageNotMultipleOfFrameHeight_NoException() {
        new TextureData<>(
                new TextureData.FrameSize(10, 7),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );
    }

    @Test
    public void construct_FrameHeightSameAsImage_NoException() {
        new TextureData<>(
                new TextureData.FrameSize(10, 100),
                false,
                false,
                new MockCloseableImage(100, 100), List.of()
        );
    }

    @Test
    public void construct_FrameHeightLargerThanImage_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new TextureData<>(
                new TextureData.FrameSize(10, 101),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );
    }

    @Test
    public void construct_FrameHeightMaxInt_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new TextureData<>(
                new TextureData.FrameSize(10, Integer.MAX_VALUE),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void construct_NullImage_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureData<>(
                new TextureData.FrameSize(10, 10),
                false,
                false,
                null,
                List.of()
        );
    }

    @Test
    public void blur_False_RetrievesFalse() {
        TextureData<MockCloseableImage> data = new TextureData<>(
                new TextureData.FrameSize(10, 20),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );

        assertFalse(data.blur());
    }

    @Test
    public void blur_True_RetrievesTrue() {
        TextureData<MockCloseableImage> data = new TextureData<>(
                new TextureData.FrameSize(10, 20),
                true,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );

        assertTrue(data.blur());
    }

    @Test
    public void clamp_False_RetrievesFalse() {
        TextureData<MockCloseableImage> data = new TextureData<>(
                new TextureData.FrameSize(10, 20),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );

        assertFalse(data.clamp());
    }

    @Test
    public void clamp_True_RetrievesTrue() {
        TextureData<MockCloseableImage> data = new TextureData<>(
                new TextureData.FrameSize(10, 20),
                false,
                true,
                new MockCloseableImage(100, 100),
                List.of()
        );

        assertTrue(data.clamp());
    }

    @Test
    public void frameSize_WidthProvided_RetrievesWidth() {
        TextureData<MockCloseableImage> data = new TextureData<>(
                new TextureData.FrameSize(10, 20),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );
        assertEquals(10, data.frameSize().width());
    }

    @Test
    public void frameSize_HeightProvided_RetrievesHeight() {
        TextureData<MockCloseableImage> data = new TextureData<>(
                new TextureData.FrameSize(10, 20),
                false,
                false,
                new MockCloseableImage(100, 100),
                List.of()
        );
        assertEquals(20, data.frameSize().height());
    }

    @Test
    public void image_ImageProvided_RetrievesSameImage() {
        MockCloseableImage image = new MockCloseableImage(100, 100);
        TextureData<MockCloseableImage> data = new TextureData<>(
                new TextureData.FrameSize(10, 20),
                false,
                false,
                image,
                List.of()
        );
        assertEquals(image, data.image());
    }

    @Test
    public void analyzedMetadata_None_NoneReturned() {
        List<Triple<String, AnalyzedMetadata, ComponentBuilder>> expectedMetadata = List.of();

        MockCloseableImage image = new MockCloseableImage(100, 100);
        TextureData<MockCloseableImage> data = new TextureData<>(
                new TextureData.FrameSize(10, 20),
                false,
                false,
                image,
                expectedMetadata
        );

        assertEquals(0, data.analyzedMetadata().size());
    }

    @Test
    public void analyzedMetadata_Some_SameMetadataReturned() {
        List<Triple<String, AnalyzedMetadata, ComponentBuilder>> expectedMetadata = List.of(
                Triple.of("pluginOne", new AnalyzedMetadata() {}, (metadata, frames) -> new TextureComponent<>() {}),
                Triple.of("pluginTwo", new AnalyzedMetadata() {}, (metadata, frames) -> new TextureComponent<>() {}),
                Triple.of("pluginThree", new AnalyzedMetadata() {}, (metadata, frames) -> new TextureComponent<>() {}),
                Triple.of("pluginFour", new AnalyzedMetadata() {}, (metadata, frames) -> new TextureComponent<>() {})
        );

        MockCloseableImage image = new MockCloseableImage(100, 100);
        TextureData<MockCloseableImage> data = new TextureData<>(
                new TextureData.FrameSize(10, 20),
                false,
                false,
                image,
                expectedMetadata
        );

        assertEquals(expectedMetadata, data.analyzedMetadata());
    }

    @Test
    public void analyzedMetadata_ListModifiedExternally_InternalListNotModified() {
        List<Triple<String, AnalyzedMetadata, ComponentBuilder>> expectedMetadata = new ArrayList<>();
        expectedMetadata.add(Triple.of(
                "pluginOne",
                new AnalyzedMetadata() {},
                (metadata, frames) -> new TextureComponent<>() {}
        ));
        expectedMetadata.add(Triple.of(
                "pluginTwo",
                new AnalyzedMetadata() {},
                (metadata, frames) -> new TextureComponent<>() {}
        ));
        expectedMetadata.add(Triple.of(
                "pluginThree",
                new AnalyzedMetadata() {},
                (metadata, frames) -> new TextureComponent<>() {}
        ));
        expectedMetadata.add(Triple.of(
                "pluginFour",
                new AnalyzedMetadata() {},
                (metadata, frames) -> new TextureComponent<>() {}
        ));

        Triple<String, AnalyzedMetadata, ComponentBuilder> extraSection = Triple.of(
                "pluginFive",
                new AnalyzedMetadata() {},
                (metadata, frames) -> new TextureComponent<>() {}
        );

        MockCloseableImage image = new MockCloseableImage(100, 100);
        TextureData<MockCloseableImage> data = new TextureData<>(
                new TextureData.FrameSize(10, 20),
                false,
                false,
                image,
                expectedMetadata
        );

        expectedMetadata.add(extraSection);

        List<Triple<String, AnalyzedMetadata, ComponentBuilder>> actualMetadata = new ArrayList<>(data.analyzedMetadata());

        // If the actual metadata already contains the extra section, the lists won't be equal
        actualMetadata.add(extraSection);
        assertEquals(expectedMetadata, actualMetadata);

    }

    @Test
    public void frameSizeConstruct_NegativeWidth_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new TextureData.FrameSize(-1, 2);
    }

    @Test
    public void frameSizeConstruct_NegativeHeight_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new TextureData.FrameSize(1, -2);
    }

    @Test
    public void frameSizeConstruct_NegativeWidthAndHeight_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new TextureData.FrameSize(-1, -2);
    }

    @Test
    public void frameSizeWidth_WidthDifferentThanHeight_GetsWidth() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        assertEquals(1, first.width());
    }

    @Test
    public void frameSizeWidth_WidthSameAsHeight_GetsWidth() {
        TextureData.FrameSize first = new TextureData.FrameSize(2, 2);
        assertEquals(2, first.width());
    }

    @Test
    public void frameSizeHeight_HeightDifferentThanWidth_GetsHeight() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        assertEquals(2, first.height());
    }

    @Test
    public void frameSizeHeight_HeightSameAsWidth_GetsHeight() {
        TextureData.FrameSize first = new TextureData.FrameSize(2, 2);
        assertEquals(2, first.height());
    }

    @Test
    public void frameSizeEquals_SameFrameSizes_Reflexive() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        assertEquals(first, first);
    }

    @Test
    public void frameSizeEquals_SameFrameSizes_Symmetric() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        TextureData.FrameSize second = new TextureData.FrameSize(1, 2);
        assertEquals(first, second);
        assertEquals(second, first);
    }

    @Test
    public void frameSizeEquals_SameFrameSizes_Transitive() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        TextureData.FrameSize second = new TextureData.FrameSize(1, 2);
        TextureData.FrameSize third = new TextureData.FrameSize(1, 2);
        assertEquals(first, second);
        assertEquals(second, third);
        assertEquals(first, third);
    }

    @Test
    public void frameSizeEquals_DiffFrameSizes_Symmetric() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        TextureData.FrameSize second = new TextureData.FrameSize(3, 4);
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    public void frameSizeEquals_DiffTypes_Symmetric() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        Object second = new Object();
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    public void frameSizeHashCode_SameFrameSizes_Reflexive() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        assertEquals(first.hashCode(), first.hashCode());
    }

    @Test
    public void frameSizeHashCode_SameFrameSizes_Symmetric() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        TextureData.FrameSize second = new TextureData.FrameSize(1, 2);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), first.hashCode());
    }

    @Test
    public void frameSizeHashCode_SameFrameSizes_Transitive() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        TextureData.FrameSize second = new TextureData.FrameSize(1, 2);
        TextureData.FrameSize third = new TextureData.FrameSize(1, 2);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), third.hashCode());
        assertEquals(first.hashCode(), third.hashCode());
    }

    @Test
    public void frameSizeHashCode_DiffFrameSizes_Symmetric() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        TextureData.FrameSize second = new TextureData.FrameSize(3, 4);
        assertNotEquals(first.hashCode(), second.hashCode());
        assertNotEquals(second.hashCode(), first.hashCode());
    }

    @Test
    public void frameSizeHashCode_DiffTypes_Symmetric() {
        TextureData.FrameSize first = new TextureData.FrameSize(1, 2);
        Object second = new Object();
        assertNotEquals(first.hashCode(), second.hashCode());
        assertNotEquals(second.hashCode(), first.hashCode());
    }

}