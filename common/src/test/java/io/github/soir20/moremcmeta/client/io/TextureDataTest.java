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

package io.github.soir20.moremcmeta.client.io;

import io.github.soir20.moremcmeta.client.texture.MockRGBAImage;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Tests the link {@link TextureData} container.
 * @author soir20
 */
public class TextureDataTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NegativeFrameWidth_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new TextureData<>(-1, 10, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_ZeroFrameWidth_NoException() {
        new TextureData<>(0, 10, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_ImageNotMultipleOfFrameWidth_NoException() {
        new TextureData<>(7, 10, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_NegativeFrameHeight_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new TextureData<>(10, -1, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_FrameWidthSameAsImage_NoException() {
        new TextureData<>(100, 10, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_FrameWidthLargerThanImage_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new TextureData<>(101, 10, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_FrameWidthMaxInt_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new TextureData<>(Integer.MAX_VALUE, 10, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_ZeroFrameHeight_NoException() {
        new TextureData<>(10, 0, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_ImageNotMultipleOfFrameHeight_NoException() {
        new TextureData<>(10, 7, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_FrameHeightSameAsImage_NoException() {
        new TextureData<>(10, 100, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_FrameHeightLargerThanImage_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new TextureData<>(10, 101, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_FrameHeightMaxInt_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new TextureData<>(10, Integer.MAX_VALUE, new MockRGBAImage(100, 100));
    }

    @Test
    public void construct_NullImage_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureData<>(10, 10, null);
    }

    @Test
    public void getFrameWidth_WidthProvided_RetrievesWidth() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));
        assertEquals(10, data.getFrameWidth());
    }

    @Test
    public void getFrameHeight_HeightProvided_RetrievesHeight() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));
        assertEquals(20, data.getFrameHeight());
    }

    @Test
    public void getImage_ImageProvided_RetrievesSameImage() {
        MockRGBAImage image = new MockRGBAImage(100, 100);
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, image);
        assertEquals(image, data.getImage());
    }

    @Test
    public void addMetadataSection_SectionClassNull_NullPointerException() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));
        expectedException.expect(NullPointerException.class);
        data.addMetadataSection(null, AnimationMetadataSection.EMPTY);
    }

    @Test
    public void addMetadataSection_SectionNull_SectionNotAdded() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));
        data.addMetadataSection(AnimationMetadataSection.class, null);

        assertFalse(data.getMetadata(AnimationMetadataSection.class).isPresent());
    }

    @Test
    public void addMetadataSection_ClassAndSectionNull_NullPointerException() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));
        expectedException.expect(NullPointerException.class);
        data.addMetadataSection(null, null);
    }

    @Test
    public void addMetadataSection_NewSection_SectionAdded() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));

        AnimationMetadataSection metadata = new AnimationMetadataSection(new ArrayList<>(), 1, 1, 1, true);
        data.addMetadataSection(AnimationMetadataSection.class, metadata);

        assertEquals(metadata, data.getMetadata(AnimationMetadataSection.class).orElse(null));
    }

    @Test
    public void addMetadataSection_AddSectionWithSameClass_SectionOverwritten() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));

        AnimationMetadataSection metadata = new AnimationMetadataSection(new ArrayList<>(), 1, 1, 1, true);
        AnimationMetadataSection metadata2 = new AnimationMetadataSection(new ArrayList<>(), 2, 2, 2, false);
        data.addMetadataSection(AnimationMetadataSection.class, metadata);
        data.addMetadataSection(AnimationMetadataSection.class, metadata2);

        assertEquals(metadata2, data.getMetadata(AnimationMetadataSection.class).orElse(null));

        // Make sure the original metadata section was not changed
        assertTrue(metadata.isInterpolatedFrames());

    }

    @Test
    public void addMetadataSection_AddSecondSectionWithSameClass_SectionOverwrittenTwice() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));

        AnimationMetadataSection metadata = new AnimationMetadataSection(new ArrayList<>(), 1, 1, 1, true);
        AnimationMetadataSection metadata2 = new AnimationMetadataSection(new ArrayList<>(), 2, 2, 2, false);
        AnimationMetadataSection metadata3 = new AnimationMetadataSection(new ArrayList<>(), 3, 3, 3, true);
        data.addMetadataSection(AnimationMetadataSection.class, metadata);
        data.addMetadataSection(AnimationMetadataSection.class, metadata2);
        data.addMetadataSection(AnimationMetadataSection.class, metadata3);

        assertEquals(metadata3, data.getMetadata(AnimationMetadataSection.class).orElse(null));

        // Make sure the original metadata section was not changed
        assertFalse(metadata2.isInterpolatedFrames());

    }

    @Test
    public void addMetadataSection_SeveralSections_AllAdded() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));

        AnimationMetadataSection metadata1 = new AnimationMetadataSection(new ArrayList<>(), 1, 1, 1, true);
        TextureMetadataSection metadata2 = new TextureMetadataSection(true, true);
        data.addMetadataSection(AnimationMetadataSection.class, metadata1);
        data.addMetadataSection(TextureMetadataSection.class, metadata2);

        assertEquals(metadata1, data.getMetadata(AnimationMetadataSection.class).orElse(null));
        assertEquals(metadata2, data.getMetadata(TextureMetadataSection.class).orElse(null));
    }

    @Test
    public void getMetadata_MetadataNotAdded_MetadataNotPresent() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));
        assertFalse(data.getMetadata(AnimationMetadataSection.class).isPresent());
    }

    @Test
    public void getMetadata_NullSectionClass_NullPointerException() {
        TextureData<MockRGBAImage> data = new TextureData<>(10, 20, new MockRGBAImage(100, 100));
        expectedException.expect(NullPointerException.class);
        data.getMetadata(null);
    }

}