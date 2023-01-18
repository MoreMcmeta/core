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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import com.google.common.collect.ImmutableMap;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataReader;
import io.github.moremcmeta.moremcmeta.impl.client.io.MockMetadataView;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.moremcmeta.moremcmeta.impl.client.resource.TextureLoaderTest.makeMockRepository;
import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link TextureCache}.
 * @author soir20
 */
public class TextureCacheTest {
    private final MetadataReader MOCK_READER = (metadataLocation, metadataStream, resourceSearcher) -> Map.of(
            new ResourceLocation(
                    metadataLocation.getNamespace(),
                    metadataLocation.getPath().replace(".moremcmeta", "")
            ),
            new MockMetadataView(Collections.emptyList())
    );
    private final ImmutableMap<String, MetadataReader> MOCK_READERS = ImmutableMap.of(".moremcmeta", MOCK_READER);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final Logger LOGGER = LogManager.getLogger();

    @Test
    public void construct_NullLoader_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureCache<>(null);
    }

    @Test
    public void load_NullRepository_NullPointerException() {
        AtomicInteger texturesRead = new AtomicInteger();
        TextureCache<Integer, Integer> cache = new TextureCache<>(
                new TextureLoader<>((texStream, metadata, texAndMetadataInSamePack) -> {
                    texturesRead.incrementAndGet();
                    return 1;
                }, MOCK_READERS, LOGGER)
        );

        expectedException.expect(NullPointerException.class);
        cache.load(null, 1, "textures", "test");
    }

    @Test
    public void load_NullPaths_NullPointerException() {
        AtomicInteger texturesRead = new AtomicInteger();
        TextureCache<Integer, Integer> cache = new TextureCache<>(
                new TextureLoader<>((texStream, metadata, texAndMetadataInSamePack) -> {
                    texturesRead.incrementAndGet();
                    return 1;
                }, MOCK_READERS, LOGGER)
        );

        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png",
                "textures/bat.png.moremcmeta", "test/creeper.png", "test/creeper.png.moremcmeta"));

        expectedException.expect(NullPointerException.class);
        cache.load(repository, 1, (String[]) null);
    }

    @Test
    public void load_NullPath_NullPointerException() {
        AtomicInteger texturesRead = new AtomicInteger();
        TextureCache<Integer, Integer> cache = new TextureCache<>(
                new TextureLoader<>((texStream, metadata, texAndMetadataInSamePack) -> {
                    texturesRead.incrementAndGet();
                    return 1;
                }, MOCK_READERS, LOGGER)
        );

        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png",
                "textures/bat.png.moremcmeta", "test/creeper.png", "test/creeper.png.moremcmeta"));

        expectedException.expect(NullPointerException.class);
        cache.load(repository, 1, (String) null);
    }

    @Test
    public void load_NullState_NullPointerException() {
        AtomicInteger texturesRead = new AtomicInteger();
        TextureCache<Integer, Integer> cache = new TextureCache<>(
                new TextureLoader<>((texStream, metadata, texAndMetadataInSamePack) -> {
                    texturesRead.incrementAndGet();
                    return 1;
                }, MOCK_READERS, LOGGER)
        );

        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png",
                "textures/bat.png.moremcmeta", "test/creeper.png", "test/creeper.png.moremcmeta"));

        expectedException.expect(NullPointerException.class);
        cache.load(repository, null, "textures", "test");
    }

    @Test
    public void load_LoadTwiceSameState_LoaderUsedOnce() {
        AtomicInteger texturesRead = new AtomicInteger();
        TextureCache<Integer, Integer> cache = new TextureCache<>(
                new TextureLoader<>((texStream, metadata, texAndMetadataInSamePack) -> {
                    texturesRead.incrementAndGet();
                    return 1;
                }, MOCK_READERS, LOGGER)
        );

        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png",
                "textures/bat.png.moremcmeta", "test/creeper.png", "test/creeper.png.moremcmeta"));
        OrderedResourceRepository repository2 = makeMockRepository(Set.of("textures/cat.png",
                "textures/cat.png.moremcmeta", "test/zombie.png", "test/zombie.png.moremcmeta"));

        cache.load(repository, 2, "textures", "test");
        cache.load(repository2, 2, "textures", "test");

        assertEquals(2, texturesRead.get());
    }

    @Test
    public void load_GetAfterLoad_CacheRetrieved() {
        TextureCache<Integer, Integer> cache = new TextureCache<>(
                new TextureLoader<>((texStream, metadata, texAndMetadataInSamePack) -> 1, MOCK_READERS, LOGGER)
        );

        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png",
                "textures/bat.png.moremcmeta", "test/creeper.png", "test/creeper.png.moremcmeta"));

        cache.load(repository, 1, "textures", "test");

        ImmutableMap<ResourceLocation, Integer> actual = cache.get(1);
        ImmutableMap<ResourceLocation, Integer> expected = ImmutableMap.<ResourceLocation, Integer>builder()
                .put(new ResourceLocation("textures/bat.png"), 1)
                .put(new ResourceLocation("test/creeper.png"), 1)
                .build();

        assertEquals(expected, actual);
    }

    @Test
    public void load_LoadAfterGet_CacheRetrieved() throws InterruptedException {
        TextureCache<Integer, Integer> cache = new TextureCache<>(
                new TextureLoader<>((texStream, metadata, texAndMetadataInSamePack) -> 1, MOCK_READERS, LOGGER)
        );

        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png",
                "textures/bat.png.moremcmeta", "test/creeper.png", "test/creeper.png.moremcmeta"));

        AtomicReference<ImmutableMap<ResourceLocation, Integer>> actual = new AtomicReference<>();
        Thread thread = new Thread(() -> actual.set(cache.get(2)));
        thread.start();

        // Use a simple spin wait to ensure the desired ordering
        //noinspection LoopConditionNotUpdatedInsideLoop,StatementWithEmptyBody
        while (thread.getState() != Thread.State.WAITING) {}

        cache.load(repository, 2, "textures", "test");
        thread.join();

        ImmutableMap<ResourceLocation, Integer> expected = ImmutableMap.<ResourceLocation, Integer>builder()
                .put(new ResourceLocation("textures/bat.png"), 1)
                .put(new ResourceLocation("test/creeper.png"), 1)
                .build();

        assertEquals(expected, actual.get());
    }

    @Test
    public void load_GetAndLoadAfterDifferentStateLoaded_SecondCacheRetrieved() throws InterruptedException {
        TextureCache<Integer, Integer> cache = new TextureCache<>(
                new TextureLoader<>((texStream, metadata, texAndMetadataInSamePack) -> 1, MOCK_READERS, LOGGER)
        );

        OrderedResourceRepository repository = makeMockRepository(Set.of("textures/bat.png",
                "textures/bat.png.moremcmeta", "test/creeper.png", "test/creeper.png.moremcmeta"));
        OrderedResourceRepository repository2 = makeMockRepository(Set.of("textures/cat.png",
                "textures/cat.png.moremcmeta", "test/zombie.png", "test/zombie.png.moremcmeta"));

        cache.load(repository, 1, "textures", "test");

        AtomicReference<ImmutableMap<ResourceLocation, Integer>> actual = new AtomicReference<>();
        Thread thread = new Thread(() -> actual.set(cache.get(2)));
        thread.start();

        cache.load(repository, 3, "textures", "test");

        // Use a simple spin wait to ensure the desired ordering
        //noinspection LoopConditionNotUpdatedInsideLoop,StatementWithEmptyBody
        while (thread.getState() != Thread.State.WAITING) {}

        cache.load(repository2, 2, "textures", "test");
        thread.join();

        ImmutableMap<ResourceLocation, Integer> expected = ImmutableMap.<ResourceLocation, Integer>builder()
                .put(new ResourceLocation("textures/cat.png"), 1)
                .put(new ResourceLocation("test/zombie.png"), 1)
                .build();

        assertEquals(expected, actual.get());
    }


}