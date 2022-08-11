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

package io.github.soir20.moremcmeta.impl.client.io;

import io.github.soir20.moremcmeta.api.client.MoreMcmetaTexturePlugin;
import io.github.soir20.moremcmeta.api.client.metadata.MetadataParser;
import io.github.soir20.moremcmeta.api.client.metadata.MetadataReader;
import io.github.soir20.moremcmeta.api.client.metadata.MetadataView;
import io.github.soir20.moremcmeta.api.client.metadata.ParsedMetadata;
import io.github.soir20.moremcmeta.api.client.texture.ComponentProvider;
import io.github.soir20.moremcmeta.api.client.texture.CurrentFrameView;
import io.github.soir20.moremcmeta.api.client.texture.FrameGroup;
import io.github.soir20.moremcmeta.api.client.texture.MutableFrameView;
import io.github.soir20.moremcmeta.api.client.texture.TextureComponent;
import io.github.soir20.moremcmeta.api.client.texture.UploadableFrameView;
import io.github.soir20.moremcmeta.impl.client.texture.MockCloseableImage;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link TextureDataReader}.
 * @author soir20
 */
public class TextureDataReaderTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    
    private final InputStream DEMO_TEXTURE_STREAM = makeStream();

    @Test
    public void test_NullPluginsList_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureDataReader<>(
                null,
                (stream, blur, clamp) -> new MockCloseableImage()
        );
    }

    @Test
    public void test_NullImageReader_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureDataReader<>(
                List.of(new MockPlugin()),
                null
        );
    }

    @Test
    public void read_NullTextureStream_NullPointerException() throws IOException, MetadataReader.InvalidMetadataException {
        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                List.of(new MockPlugin()),
                (stream, blur, clamp) -> new MockCloseableImage()
        );
        expectedException.expect(NullPointerException.class);
        reader.read(null, new MockMetadataView(Collections.singletonList("texture")));
    }

    @Test
    public void read_NullMetadataStream_NullPointerException() throws IOException, MetadataReader.InvalidMetadataException {
        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                List.of(new MockPlugin()),
                (stream, blur, clamp) -> new MockCloseableImage()
        );
        expectedException.expect(NullPointerException.class);
        reader.read(DEMO_TEXTURE_STREAM, null);
    }

    @Test
    public void read_NullImageRead_NullPointerException() throws IOException, MetadataReader.InvalidMetadataException {
        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                List.of(new MockPlugin()),
                (stream, blur, clamp) -> null
        );
        expectedException.expect(NullPointerException.class);
        reader.read(DEMO_TEXTURE_STREAM, new MockMetadataView(Collections.singletonList("texture")));
    }

    @Test
    public void read_ReaderIOException_IOException() throws IOException, MetadataReader.InvalidMetadataException {
        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                List.of(new MockPlugin()),
                (stream, blur, clamp) -> { throw new IOException("dummy"); }
        );
        expectedException.expect(IOException.class);
        reader.read(DEMO_TEXTURE_STREAM, new MockMetadataView(Collections.singletonList("texture")));
    }

    @Test
    public void read_NoSections_NoMetadata() throws IOException, MetadataReader.InvalidMetadataException {
        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                List.of(new MockPlugin()),
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(new ArrayList<>())
        );

        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(0, numParsed);
    }

    @Test
    public void read_NoPluginForSection_MetadataForPresentPlugins() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("texture", null, null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id()), foundIds);
        assertEquals(1, numParsed);
    }

    @Test
    public void read_NoSectionForPlugin_MetadataForPresentSections() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, null, null, null),
                new MockPlugin("texture", null, null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Collections.singletonList("texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(1).id()), foundIds);
        assertEquals(1, numParsed);
    }

    @Test
    public void read_ConflictingFrameSize_InvalidMetadataException() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", new ParsedMetadata.FrameSize(100, 100), null, null, null),
                new MockPlugin("texture", new ParsedMetadata.FrameSize(50, 50), null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        expectedException.expect(MetadataReader.InvalidMetadataException.class);
        reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );
    }

    @Test
    public void read_ConflictingBlur_InvalidMetadataException() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, true, null, null),
                new MockPlugin("texture", null, false, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        expectedException.expect(MetadataReader.InvalidMetadataException.class);
        reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );
    }

    @Test
    public void read_ConflictingClamp_InvalidMetadataException() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, null, false, null),
                new MockPlugin("texture", null, null, true, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        expectedException.expect(MetadataReader.InvalidMetadataException.class);
        reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );
    }

    @Test
    public void read_AllPluginsProvideSameFrameSize_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", new ParsedMetadata.FrameSize(100, 100), null, null, null),
                new MockPlugin("texture", new ParsedMetadata.FrameSize(100, 100), null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id()), foundIds);
        assertEquals(2, numParsed);
    }

    @Test
    public void read_AllPluginsProvideTrueBlur_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, true, null, null),
                new MockPlugin("texture", null, true, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> { assertTrue(blur); return new MockCloseableImage(); }
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id()), foundIds);
        assertEquals(2, numParsed);
    }

    @Test
    public void read_AllPluginsProvideFalseBlur_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, false, null, null),
                new MockPlugin("texture", null, false, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> { assertFalse(blur); return new MockCloseableImage(); }
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id()), foundIds);
        assertEquals(2, numParsed);
    }

    @Test
    public void read_AllPluginsProvideTrueClamp_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, null, true, null),
                new MockPlugin("texture", null, null, true, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> { assertTrue(clamp); return new MockCloseableImage(); }
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id()), foundIds);
        assertEquals(2, numParsed);
    }

    @Test
    public void read_AllPluginsProvideFalseClamp_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, null, false, null),
                new MockPlugin("texture", null, null, false, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> { assertFalse(clamp); return new MockCloseableImage(); }
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id()), foundIds);
        assertEquals(2, numParsed);
    }

    @Test
    public void read_SomePluginsProvideSameFrameSize_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", new ParsedMetadata.FrameSize(100, 100), null, null, null),
                new MockPlugin("other", new ParsedMetadata.FrameSize(100, 100), null, null, null),
                new MockPlugin("texture", null, null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "other", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id(), plugins.get(2).id()), foundIds);
        assertEquals(3, numParsed);
    }

    @Test
    public void read_SomePluginsProvideTrueBlur_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, true, null, null),
                new MockPlugin("other", null, true, null, null),
                new MockPlugin("texture", null, null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "other", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id(), plugins.get(2).id()), foundIds);
        assertEquals(3, numParsed);
    }

    @Test
    public void read_SomePluginsProvideFalseBlur_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, false, null, null),
                new MockPlugin("other", null, false, null, null),
                new MockPlugin("texture", null, null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "other", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id(), plugins.get(2).id()), foundIds);
        assertEquals(3, numParsed);
    }

    @Test
    public void read_SomePluginsProvideTrueClamp_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, null, true, null),
                new MockPlugin("other", null, null, true, null),
                new MockPlugin("texture", null, null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "other", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id(), plugins.get(2).id()), foundIds);
        assertEquals(3, numParsed);
    }

    @Test
    public void read_SomePluginsProvideFalseClamp_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, null, false, null),
                new MockPlugin("other", null, null, false, null),
                new MockPlugin("texture", null, null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "other", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id(), plugins.get(2).id()), foundIds);
        assertEquals(3, numParsed);
    }

    @Test
    public void read_NoPluginsProvideMinParams_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, null, null, null),
                new MockPlugin("other", null, null, null, null),
                new MockPlugin("texture", null, null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "other", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id(), plugins.get(2).id()), foundIds);
        assertEquals(3, numParsed);
    }

    @Test
    public void read_NoPluginsProvideClamp_NoConflict() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", new ParsedMetadata.FrameSize(100, 100), true, null, null),
                new MockPlugin("other", null, true, null, null),
                new MockPlugin("texture", null, true, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "other", "texture"))
        );

        Set<Integer> foundIds = new HashSet<>();
        int numParsed = 0;
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
            numParsed++;
        }

        assertEquals(Set.of(plugins.get(0).id(), plugins.get(1).id(), plugins.get(2).id()), foundIds);
        assertEquals(3, numParsed);
    }

    @Test
    public void read_ParsedMetadataNull_NullPointerException() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, null, null, null),
                new MockPlugin("other", null, null, null, null, true, (view) -> {})
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        expectedException.expect(NullPointerException.class);
        reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "other", "texture"))
        );
    }

    @Test
    public void read_InvalidReasonProvided_InvalidMetadataException() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", null, null, null, null),
                new MockPlugin("other", null, null, null, "dummy")
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        expectedException.expect(MetadataReader.InvalidMetadataException.class);
        reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "other", "texture"))
        );
    }

    @Test
    public void read_OrderInView_MetadataOrderedByViewOrder() throws IOException, MetadataReader.InvalidMetadataException {
        AtomicBoolean checked = new AtomicBoolean(false);

        Consumer<MetadataView> viewCheckFunction = (view) -> {
            checked.set(true);

            List<String> sectionNames = new ArrayList<>();
            for (String key : view.keys()) {
                sectionNames.add(key);
            }

            assertEquals(List.of("texture", "animation", "other"), sectionNames);
        };

        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", new ParsedMetadata.FrameSize(100, 100), true, null, null),
                new MockPlugin("other", null, true, null, null, false, viewCheckFunction),
                new MockPlugin("texture", null, true, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        TextureData<MockCloseableImage> data = reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("texture", "animation", "other"))
        );

        // Make sure that the view was checked for correct priority ordering
        assertTrue(checked.get());

        // Check the parsed metadata for priority ordering
        List<Integer> foundIds = new ArrayList<>();
        for (Triple<String, ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            foundIds.add(((MockParsedMetadata) metadata.getMiddle()).id());
            assertEquals(
                    ((MockParsedMetadata) metadata.getMiddle()).id(),
                    ((MockComponentProvider) metadata.getRight()).id()
            );
        }
        assertEquals(List.of(plugins.get(2).id(), plugins.get(0).id(), plugins.get(1).id()), foundIds);

    }

    @Test
    public void read_FrameWidthLargerThanImage_InvalidMetadataException() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", new ParsedMetadata.FrameSize(1000, 100), null, null, null),
                new MockPlugin("texture", new ParsedMetadata.FrameSize(1000, 100), null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        expectedException.expect(MetadataReader.InvalidMetadataException.class);
        reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );
    }

    @Test
    public void read_FrameHeightLargerThanImage_InvalidMetadataException() throws IOException, MetadataReader.InvalidMetadataException {
        List<MockPlugin> plugins = List.of(
                new MockPlugin("animation", new ParsedMetadata.FrameSize(100, 1000), null, null, null),
                new MockPlugin("texture", new ParsedMetadata.FrameSize(100, 1000), null, null, null)
        );

        TextureDataReader<MockCloseableImage> reader = new TextureDataReader<>(
                plugins,
                (stream, blur, clamp) -> new MockCloseableImage()
        );

        expectedException.expect(MetadataReader.InvalidMetadataException.class);
        reader.read(
                DEMO_TEXTURE_STREAM,
                new MockMetadataView(Arrays.asList("animation", "texture"))
        );
    }

    private InputStream makeStream() {
        return new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Mock implementation of {@link MockPlugin} for easy creation in tests.
     * @author soir20
     */
    private static final class MockPlugin implements MoreMcmetaTexturePlugin {
        private static int nextId;
        private final int ID;
        private final String SECTION;
        private final ParsedMetadata.FrameSize FRAME_SIZE;
        private final Boolean BLUR;
        private final Boolean CLAMP;
        private final String INVALID_REASON;
        private final boolean NULL_METADATA;
        private final Consumer<MetadataView> VIEW_CHECK_FUNCTION;

        public MockPlugin() {
            this("texture", null, null, null, null);
        }

        public MockPlugin(String sectionName, ParsedMetadata.FrameSize frameSize, Boolean blur, Boolean clamp,
                          String invalidReason) {
            this(sectionName, frameSize, blur, clamp, invalidReason, false, (view) -> {});
        }

        public MockPlugin(String sectionName, ParsedMetadata.FrameSize frameSize, Boolean blur, Boolean clamp,
                          String invalidReason, boolean nullMetadata, Consumer<MetadataView> viewCheckFunction) {
            ID = nextId++;
            SECTION = sectionName;
            FRAME_SIZE = frameSize;
            BLUR = blur;
            CLAMP = clamp;
            INVALID_REASON = invalidReason;
            NULL_METADATA = nullMetadata;
            VIEW_CHECK_FUNCTION = viewCheckFunction;
        }

        @Override
        public String displayName() {
            return "dummy plugin";
        }

        @Override
        public String sectionName() {
            return SECTION;
        }

        @Override
        public MetadataParser parser() {
            return (view) -> {
                VIEW_CHECK_FUNCTION.accept(view);
                return NULL_METADATA ? null : new MockParsedMetadata(ID, FRAME_SIZE, BLUR, CLAMP, INVALID_REASON);
            };
        }

        @Override
        public ComponentProvider componentProvider() {
            return new MockComponentProvider(ID);
        }

        public int id() {
            return ID;
        }

    }

    /**
     * Mock implementation of {@link ParsedMetadata} for easy creation in tests.
     * @author soir20
     */
    private static final class MockParsedMetadata implements ParsedMetadata {
        private final int ID;
        private final ParsedMetadata.FrameSize FRAME_SIZE;
        private final Boolean BLUR;
        private final Boolean CLAMP;
        private final String INVALID_REASON;

        public MockParsedMetadata(int id, ParsedMetadata.FrameSize frameSize, Boolean blur, Boolean clamp,
                                  String invalidReason) {
            ID = id;
            FRAME_SIZE = frameSize;
            BLUR = blur;
            CLAMP = clamp;
            INVALID_REASON = invalidReason;
        }

        @Override
        public Optional<FrameSize> frameSize() {
            return Optional.ofNullable(FRAME_SIZE);
        }

        @Override
        public Optional<Boolean> blur() {
            return Optional.ofNullable(BLUR);
        }

        @Override
        public Optional<Boolean> clamp() {
            return Optional.ofNullable(CLAMP);
        }

        @Override
        public Optional<String> invalidReason() {
            return Optional.ofNullable(INVALID_REASON);
        }

        public int id() {
            return ID;
        }

    }

    /**
     * Mock implementation of {@link ComponentProvider} for easy creation in tests.
     * @author soir20
     */
    private static final class MockComponentProvider implements ComponentProvider {
        private final int ID;

        public MockComponentProvider(int id) {
            ID = id;
        }

        @Override
        public TextureComponent<CurrentFrameView, UploadableFrameView>
        assemble(ParsedMetadata metadata, FrameGroup<? extends MutableFrameView> frames) {
            return new TextureComponent<>() {};
        }

        public int id() {
            return ID;
        }

    }

}