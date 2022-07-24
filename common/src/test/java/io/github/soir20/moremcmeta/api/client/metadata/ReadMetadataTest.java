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

package io.github.soir20.moremcmeta.api.client.metadata;

import io.github.soir20.moremcmeta.impl.client.io.MockMetadataView;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link MetadataReader.ReadMetadata}, primarily for null-handling.
 * @author soir20
 */
public class ReadMetadataTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullTextureLocation_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new MetadataReader.ReadMetadata(null, new MockMetadataView(Collections.emptyList()));
    }

    @Test
    public void construct_NullMetadataView_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new MetadataReader.ReadMetadata(new ResourceLocation("test"), null);
    }

    @Test
    public void construct_HasTextureLocationAndMetadata_BothRetrieved() {
        MetadataView metadata = new MockMetadataView(Arrays.asList("animation", "texture"));

        MetadataReader.ReadMetadata readMetadata = new MetadataReader.ReadMetadata(
                new ResourceLocation("test"),
                metadata
        );

        assertEquals(new ResourceLocation("test"), readMetadata.textureLocation());
        assertEquals(metadata, readMetadata.metadata());
    }

}