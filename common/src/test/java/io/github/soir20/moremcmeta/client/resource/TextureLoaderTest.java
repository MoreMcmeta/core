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

package io.github.soir20.moremcmeta.client.resource;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonParseException;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Tests the {@link TextureLoader}. We assume that the retrieval of files with the correct extension
 * works because that is part of Minecraft's code.
 *
 * Note about low branch coverage for this class: The branches are almost all generated by the compiler
 * in the try-with-resources statement in the loader. Some of these branches might be unreachable.
 * Thus, it makes more sense to test representative cases here than to try to maximize branch coverage.
 * See https://stackoverflow.com/a/17356707 (StackOverflow explanation) and
 * https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html#jls-14.20.3.1 (standards definition
 * of try-with-resources).
 * @author soir20
 */
public class TextureLoaderTest {
    private final Logger LOGGER = LogManager.getLogger();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_TextureFactoryNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureLoader<>(null, LOGGER);
    }

    @Test
    public void construct_LoggerNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureLoader<>((texStream, metadataStream) -> 1, null);
    }

    @Test
    public void load_ResourceManagerNull_NullPointerException() {
        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        expectedException.expect(NullPointerException.class);
        loader.load(null, "textures");
    }

    @Test
    public void load_EmptyPath_IllegalArgException() {
        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png.moremcmeta",
                        "textures/creeper.png.moremcmeta", "textures/zombie.png.moremcmeta"));

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        expectedException.expect(IllegalArgumentException.class);
        loader.load(repository, "");
    }

    @Test
    public void load_SlashOnlyPath_IllegalArgException() {
        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png.moremcmeta",
                        "textures/creeper.png.moremcmeta", "textures/zombie.png.moremcmeta"));

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        expectedException.expect(IllegalArgumentException.class);
        loader.load(repository, "/");
    }

    @Test
    public void load_PathStartsWithSlash_IllegalArgException() {
        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png.moremcmeta",
                        "textures/creeper.png.moremcmeta", "textures/zombie.png.moremcmeta"));

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        expectedException.expect(IllegalArgumentException.class);
        loader.load(repository, "/textures");
    }

    @Test
    public void load_ValidLocations_LoadsAllTextures() {
        OrderedResourceRepository repository = makeMockRepository(Set.of(
                "textures/bat.png",
                "textures/bat.png.moremcmeta",
                "textures/creeper.png",
                "textures/creeper.png.moremcmeta",
                "textures/zombie.png",
                "textures/zombie.png.moremcmeta"
        ));

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        Map<ResourceLocation, Integer> locations = loader.load(repository, "textures");

        assertEquals(3, locations.size());
        assertTrue(locations.containsKey(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.containsKey(new ResourceLocation("textures/creeper.png")));
        assertTrue(locations.containsKey(new ResourceLocation("textures/zombie.png")));
    }

    @Test
    public void load_DifferentPath_LoadsAllTextures() {
        OrderedResourceRepository repository = makeMockRepository(Set.of(
                "other/bat.png",
                "other/bat.png.moremcmeta",
                "other/creeper.png",
                "other/creeper.png.moremcmeta",
                "other/zombie.png",
                "other/zombie.png.moremcmeta"
        ));

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        Map<ResourceLocation, Integer> locations = loader.load(repository, "other");

        assertEquals(3, locations.size());
        assertTrue(locations.containsKey(new ResourceLocation("other/bat.png")));
        assertTrue(locations.containsKey(new ResourceLocation("other/creeper.png")));
        assertTrue(locations.containsKey(new ResourceLocation("other/zombie.png")));
    }

    @Test
    public void load_FilteredLocations_LoadsFilteredTextures() {
        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png", "textures/bat.png.moremcmeta", "creeper",
                "creeper.moremcmeta", "zombie.jpg", "zombie.jpg.moremcmeta"));

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        Map<ResourceLocation, Integer> locations = loader.load(repository, "textures");

        assertEquals(1, locations.size());
        assertTrue(locations.containsKey(new ResourceLocation("textures/bat.png")));
    }

    @Test
    public void load_MissingTextureLocations_LoadsNoMissingTextures() {
        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png", "textures/bat.png.moremcmeta",
                "textures/creeper.png.moremcmeta", "textures/zombie.png.moremcmeta"));

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        Map<ResourceLocation, Integer> locations = loader.load(repository, "textures");

        assertEquals(1, locations.size());
        assertTrue(locations.containsKey(new ResourceLocation("textures/bat.png")));
    }

    @Test
    public void load_MissingMetadataLocations_LoadsNoMissingTextures() {
        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png", "textures/bat.png.moremcmeta",
                "textures/creeper.png", "textures/zombie.png"));

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        Map<ResourceLocation, Integer> locations = loader.load(repository, "textures");

        assertEquals(1, locations.size());
        assertTrue(locations.containsKey(new ResourceLocation("textures/bat.png")));
    }

    @Test
    public void load_InvalidJson_LoadsValidTextures() {
        OrderedResourceRepository repository = makeMockRepository(Set.of(
                "textures/bat.png",
                "textures/bat.png.moremcmeta",
                "textures/creeper.png",
                "textures/creeper.png.moremcmeta",
                "textures/zombie.png",
                "textures/zombie.png.moremcmeta"
        ));

        AtomicInteger texturesLoaded = new AtomicInteger();
        TextureLoader<Integer> loader = new TextureLoader<>(
                (texStream, metadataStream) -> {
                    if (texturesLoaded.getAndIncrement() < 1) {
                        throw new JsonParseException("Dummy exception");
                    }
                    return 1;
                },
                LOGGER
        );

        Map<ResourceLocation, Integer> locations = loader.load(repository, "textures");
        assertEquals(2, locations.size());
    }

    @Test
    public void load_InvalidMetadata_LoadsValidTextures() {
        OrderedResourceRepository repository = makeMockRepository(Set.of(
                "textures/bat.png",
                "textures/bat.png.moremcmeta",
                "textures/creeper.png",
                "textures/zombie.png",
                "textures/creeper.png.moremcmeta",
                "textures/zombie.png.moremcmeta"
        ));

        AtomicInteger texturesLoaded = new AtomicInteger();
        TextureLoader<Integer> loader = new TextureLoader<>(
                (texStream, metadataStream) -> {
                    if (texturesLoaded.getAndIncrement() < 1) {
                        throw new IllegalArgumentException("Dummy exception");
                    }
                    return 1;
                },
                LOGGER
        );

        Map<ResourceLocation, Integer> locations = loader.load(repository, "textures");
        assertEquals(2, locations.size());
    }

    @Test
    public void load_UnknownException_ExceptionNotCaught() {
        OrderedResourceRepository repository = makeMockRepository(Set.of(
                "textures/bat.png",
                "textures/bat.png.moremcmeta",
                "textures/creeper.png",
                "textures/creeper.png.moremcmeta",
                "textures/zombie.png",
                "textures/zombie.png.moremcmeta"
        ));


        AtomicInteger texturesLoaded = new AtomicInteger();
        TextureLoader<Integer> loader = new TextureLoader<>(
                (texStream, metadataStream) -> {
                    if (texturesLoaded.getAndIncrement() < 1) {
                        throw new RuntimeException("Dummy exception");
                    }
                    return 1;
                },
                LOGGER
        );

        expectedException.expect(RuntimeException.class);
        loader.load(repository, "textures");
    }

    @Test
    public void load_ResourceManagerThrowsUnknownException_ExceptionNotCaught() {
        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES,
                Set.of(new MockResourceCollection(Set.of(
                        new ResourceLocation("textures/bat.png"),
                        new ResourceLocation("textures/creeper.png"),
                        new ResourceLocation("textures/zombie.png"),
                        new ResourceLocation("textures/bat.png.moremcmeta"),
                        new ResourceLocation("textures/creeper.png.moremcmeta"),
                        new ResourceLocation("textures/zombie.png.moremcmeta")
                )))
        ) {
            @Override
            public Set<ResourceLocation> listResources(String pathIn, Predicate<String> fileFilter) {
                throw new RuntimeException();
            }
        };

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);


        expectedException.expect(RuntimeException.class);
        loader.load(repository, "textures");
    }

    @Test
    public void load_ResourceManagerReturnsNullTexture_NullPointerException() {
        ResourceCollection collection = new MockResourceCollection(Set.of(
                new ResourceLocation("textures/bat.png"),
                new ResourceLocation("textures/creeper.png"),
                new ResourceLocation("textures/zombie.png"),
                new ResourceLocation("textures/bat.png.moremcmeta"),
                new ResourceLocation("textures/creeper.png.moremcmeta"),
                new ResourceLocation("textures/zombie.png.moremcmeta")
        )) {
            public InputStream getResource(PackType resourceType, ResourceLocation location) throws IOException {
                if (hasResource(resourceType, location) && !location.getPath().endsWith(".moremcmeta")) {
                    return null;
                }

                return super.getResource(resourceType, location);
            }
        };
        OrderedResourceRepository repository = new OrderedResourceRepository(
                PackType.CLIENT_RESOURCES,
                Set.of(collection)
        );

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        expectedException.expect(NullPointerException.class);
        loader.load(repository, "textures");
    }

    @Test
    public void load_ResourceManagerReturnsNullMetadata_NullPointerException() {
        ResourceCollection collection = new MockResourceCollection(Set.of(
                new ResourceLocation("textures/bat.png"),
                new ResourceLocation("textures/creeper.png"),
                new ResourceLocation("textures/zombie.png"),
                new ResourceLocation("textures/bat.png.moremcmeta"),
                new ResourceLocation("textures/creeper.png.moremcmeta"),
                new ResourceLocation("textures/zombie.png.moremcmeta")
        )) {
            public InputStream getResource(PackType resourceType, ResourceLocation location) throws IOException {
                if (hasResource(resourceType, location) && location.getPath().endsWith(".moremcmeta")) {
                    return null;
                }

                return super.getResource(resourceType, location);
            }
        };
        OrderedResourceRepository repository = new OrderedResourceRepository(
                PackType.CLIENT_RESOURCES,
                Set.of(collection)
        );

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        expectedException.expect(NullPointerException.class);
        loader.load(repository, "textures");
    }

    @Test
    public void load_ClosureIOException_LoadsValidTextures() {
        ResourceCollection collection = new MockResourceCollection(Set.of(
                new ResourceLocation("textures/bat.png"),
                new ResourceLocation("textures/creeper.png"),
                new ResourceLocation("textures/zombie.png"),
                new ResourceLocation("textures/bat.png.moremcmeta"),
                new ResourceLocation("textures/creeper.png.moremcmeta"),
                new ResourceLocation("textures/zombie.png.moremcmeta")
        )) {
            public InputStream getResource(PackType resourceType, ResourceLocation location) throws IOException {
                if (hasResource(resourceType, location) && !location.getPath().contains("bat")) {
                    return new InputStream() {
                        @Override
                        public int read() {
                            return 0;
                        }

                        @Override
                        public void close() throws IOException {
                            throw new IOException("Dummy exception");
                        }
                    };
                }

                return super.getResource(resourceType, location);
            }
        };
        OrderedResourceRepository repository = new OrderedResourceRepository(
                PackType.CLIENT_RESOURCES,
                Set.of(collection)
        );

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        Map<ResourceLocation, Integer> locations = loader.load(repository, "textures");

        assertEquals(1, locations.size());
        assertTrue(locations.containsKey(new ResourceLocation("textures/bat.png")));
    }

    @Test
    public void load_ClosureUnknownException_ExceptionNotCaught() {
        ResourceCollection collection = new MockResourceCollection(Set.of(
                new ResourceLocation("textures/bat.png"),
                new ResourceLocation("textures/creeper.png"),
                new ResourceLocation("textures/zombie.png"),
                new ResourceLocation("textures/bat.png.moremcmeta"),
                new ResourceLocation("textures/creeper.png.moremcmeta"),
                new ResourceLocation("textures/zombie.png.moremcmeta")
        )) {
            public InputStream getResource(PackType resourceType, ResourceLocation location) throws IOException {
                if (hasResource(resourceType, location) && location.getPath().contains("bat")) {
                    return new InputStream() {
                        @Override
                        public int read() {
                            return 0;
                        }

                        @Override
                        public void close() {
                            throw new RuntimeException("Dummy exception");
                        }
                    };
                }

                return super.getResource(resourceType, location);
            }
        };
        OrderedResourceRepository repository = new OrderedResourceRepository(
                PackType.CLIENT_RESOURCES,
                Set.of(collection)
        );

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        expectedException.expect(RuntimeException.class);
        loader.load(repository, "textures");
    }

    @Test
    public void load_TextureAndMetadataInDifferentPacks_SkipsSeparatedTextures() {
        OrderedResourceRepository repository = makeMockRepository(
                Set.of("textures/bat.png", "textures/bat.png.moremcmeta", "textures/creeper.png", "textures/zombie.png",
                        "textures/zombie.png.moremcmeta"),
                Set.of("textures/creeper.png.moremcmeta")
        );

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        Map<ResourceLocation, Integer> results = loader.load(repository, "textures");

        assertEquals(2, results.size());
        assertTrue(results.containsKey(new ResourceLocation("textures/bat.png")));
        assertTrue(results.containsKey(new ResourceLocation("textures/zombie.png")));
    }

    @Test
    public void load_LaterPackHasMetadataAndTexture_SkipsSeparatedTextures() {
        OrderedResourceRepository repository = makeMockRepository(
                Set.of("textures/bat.png", "textures/bat.png.moremcmeta", "textures/creeper.png", "textures/zombie.png",
                        "textures/zombie.png.moremcmeta"),
                Set.of("textures/creeper.png.moremcmeta"),
                Set.of("textures/creeper.png", "textures/creeper.png.moremcmeta")
        );

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        Map<ResourceLocation, Integer> results = loader.load(repository, "textures");

        assertEquals(2, results.size());
        assertTrue(results.containsKey(new ResourceLocation("textures/bat.png")));
        assertTrue(results.containsKey(new ResourceLocation("textures/zombie.png")));
    }

    @Test
    public void load_ResourceLocationException_NothingLoaded() {
        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES,
                Set.of(new MockResourceCollection(Set.of(
                        new ResourceLocation("textures/bat.png"),
                        new ResourceLocation("textures/creeper.png"),
                        new ResourceLocation("textures/zombie.png"),
                        new ResourceLocation("textures/bat.png.moremcmeta"),
                        new ResourceLocation("textures/creeper.png.moremcmeta"),
                        new ResourceLocation("textures/zombie.png.moremcmeta")
                )))
        ) {
            @Override
            public Set<ResourceLocation> listResources(String pathIn, Predicate<String> fileFilter) {
                throw new ResourceLocationException("Dummy exception");
            }
        };

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);
        assertTrue(loader.load(repository, "textures").isEmpty());
    }

    @Test
    public void load_DiffNamespaces_AllLoaded() {
        OrderedResourceRepository repository = makeMockRepository(Set.of(
                "test:textures/bat.png",
                "test:textures/bat.png.moremcmeta",
                "moremcmeta:textures/creeper.png",
                "moremcmeta:textures/creeper.png.moremcmeta",
                "textures/zombie.png",
                "textures/zombie.png.moremcmeta"));

        TextureLoader<Integer> loader = new TextureLoader<>((texStream, metadataStream) -> 1, LOGGER);

        Map<ResourceLocation, Integer> locations = loader.load(repository, "textures");

        assertEquals(3, locations.size());
        assertTrue(locations.containsKey(new ResourceLocation("test", "textures/bat.png")));
        assertTrue(locations.containsKey(new ResourceLocation("moremcmeta", "textures/creeper.png")));
        assertTrue(locations.containsKey(new ResourceLocation("textures/zombie.png")));
    }

    @SafeVarargs
    public static OrderedResourceRepository makeMockRepository(Set<String>... presentFiles) {
        ImmutableSet.Builder<ResourceCollection> builder = new ImmutableSet.Builder<>();

        for (Set<String> files : presentFiles) {
            builder.add(new MockResourceCollection(
                    files.stream().map(ResourceLocation::new).collect(Collectors.toSet())
            ));
        }

        return new OrderedResourceRepository(PackType.CLIENT_RESOURCES, builder.build());
    }

}