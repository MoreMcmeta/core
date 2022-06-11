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

package io.github.soir20.moremcmeta.client.adapter;

import io.github.soir20.moremcmeta.client.resource.MockPackResources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Tests the {@link PackResourcesAdapter}.
 * @author soir20
 */
public class PackResourcesAdapterTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_OriginalNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new PackResourcesAdapter(null);
    }

    @Test
    public void getResource_ResourceTypeNull_NullPointerException() throws IOException {
        PackResourcesAdapter adapter = new PackResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.getResource(null, new ResourceLocation("testing"));
    }

    @Test
    public void getResource_LocationNull_NullPointerException() throws IOException {
        PackResourcesAdapter adapter = new PackResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.getResource(PackType.CLIENT_RESOURCES, null);
    }

    @Test
    public void getResource_GetExistingClientResource_ResourceRetrieved() throws IOException {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("textures/block/sea/rock/gravel.png");
        InputStream resource = adapter.getResource(PackType.CLIENT_RESOURCES, location);

        assertEquals(location.getPath(), new String(resource.readAllBytes()));
    }

    @Test
    public void getResource_GetExistingServerResource_ResourceRetrieved() throws IOException {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");
        InputStream resource = adapter.getResource(PackType.SERVER_DATA, location);

        assertEquals(location.getPath(), new String(resource.readAllBytes()));
    }

    @Test
    public void getResource_GetNotExistingClientResource_IOException() throws IOException {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("textures/block/sea/rock/other.png");

        expectedException.expect(IOException.class);
        adapter.getResource(PackType.CLIENT_RESOURCES, location);
    }

    @Test
    public void getResource_GetNotExistingServerResource_IOException() throws IOException {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/other.json");

        expectedException.expect(IOException.class);
        adapter.getResource(PackType.SERVER_DATA, location);
    }

    @Test
    public void getResource_GetResourceDifferentResourceType_IOException() throws IOException {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");

        expectedException.expect(IOException.class);
        adapter.getResource(PackType.CLIENT_RESOURCES, location);
    }

    @Test
    public void getResource_OriginalThrowsException_ThrowsException() throws IOException {
        PackResources original = new ExceptionPackResources();
        PackResourcesAdapter adapter = new PackResourcesAdapter(original);

        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");

        expectedException.expect(RuntimeException.class);
        adapter.getResource(PackType.SERVER_DATA, location);
    }

    @Test
    public void hasResource_ResourceTypeNull_NullPointerException() {
        PackResourcesAdapter adapter = new PackResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.hasResource(null, new ResourceLocation("testing"));
    }

    @Test
    public void hasResource_LocationNull_NullPointerException() {
        PackResourcesAdapter adapter = new PackResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.hasResource(PackType.CLIENT_RESOURCES, null);
    }

    @Test
    public void hasResource_GetExistingClientResource_ResourceFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("textures/block/sea/rock/gravel.png");
        assertTrue(adapter.hasResource(PackType.CLIENT_RESOURCES, location));
    }

    @Test
    public void hasResource_GetExistingServerResource_ResourceFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");
        assertTrue(adapter.hasResource(PackType.SERVER_DATA, location));
    }

    @Test
    public void hasResource_GetNotExistingClientResource_ResourceNotFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("textures/block/sea/rock/other.png");
        assertFalse(adapter.hasResource(PackType.CLIENT_RESOURCES, location));
    }

    @Test
    public void hasResource_GetNotExistingServerResource_ResourceNotFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/other.json");
        assertFalse(adapter.hasResource(PackType.SERVER_DATA, location));
    }

    @Test
    public void hasResource_GetResourceDifferentResourceType_ResourceNotFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");
        assertFalse(adapter.hasResource(PackType.CLIENT_RESOURCES, location));
    }

    @Test
    public void hasResource_OriginalThrowsException_ThrowsException() {
        PackResources original = new ExceptionPackResources();
        PackResourcesAdapter adapter = new PackResourcesAdapter(original);

        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");

        expectedException.expect(RuntimeException.class);
        adapter.hasResource(PackType.SERVER_DATA, location);
    }

    @Test
    public void getResources_TypeNull_NullPointerException() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.getResources(null, "minecraft", "textures", (file) -> true);
    }

    @Test
    public void getResources_NamespaceNull_NullPointerException() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.getResources(PackType.CLIENT_RESOURCES, null, "textures", (file) -> true);
    }

    @Test
    public void getResources_PathStartNull_NullPointerException() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.getResources(PackType.CLIENT_RESOURCES, "minecraft", null, (file) -> true);
    }

    @Test
    public void getResources_FileFilterNull_NullPointerException() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures", null);
    }

    @Test
    public void getResources_SomeMatchAllFiltersClient_MatchingFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.getResources(PackType.CLIENT_RESOURCES, "minecraft",
                "textures", (file) -> file.getPath().endsWith(".png"));

        assertEquals(2, resources.size());
        assertTrue(resources.contains(new ResourceLocation("textures/hello.png")));
        assertTrue(resources.contains(new ResourceLocation("textures/block/sea/rock/gravel.png")));
    }

    @Test
    public void getResources_SomeMatchAllFiltersServer_MatchingFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.getResources(PackType.SERVER_DATA, "sea",
                "textures", (file) -> file.getPath().endsWith(".png"));

        assertEquals(1, resources.size());
        assertTrue(resources.contains(new ResourceLocation("sea", "textures/block/coral.png")));
    }

    @Test
    public void getResources_NoneMatchNamespace_NoneFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.getResources(PackType.CLIENT_RESOURCES, "other",
                "textures", (file) -> file.getPath().endsWith(".png"));

        assertEquals(0, resources.size());
    }

    @Test
    public void getResources_NoneMatchPathStart_NoneFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.getResources(PackType.CLIENT_RESOURCES, "minecraft",
                "other", (file) -> file.getPath().endsWith(".png"));

        assertEquals(0, resources.size());
    }

    @Test
    public void getResources_EmptyNamespace_NoExceptionIfOriginalAccepts() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.getResources(PackType.CLIENT_RESOURCES, "",
                "textures", (file) -> file.getPath().endsWith(".png"));

        assertEquals(0, resources.size());
    }

    @Test
    public void getResources_EmptyPathStart_NoExceptionIfOriginalAccepts() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.getResources(PackType.CLIENT_RESOURCES, "minecraft",
                "", (file) -> file.getPath().endsWith(".png"));

        assertEquals(2, resources.size());
        assertTrue(resources.contains(new ResourceLocation("textures/hello.png")));
        assertTrue(resources.contains(new ResourceLocation("textures/block/sea/rock/gravel.png")));
    }

    @Test
    public void getResources_NoneMatchFilter_NoneFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.getResources(PackType.CLIENT_RESOURCES, "minecraft",
                "textures", (file) -> file.getPath().endsWith(".jpg"));

        assertEquals(0, resources.size());
    }

    @Test
    public void getResources_OriginalThrowsException_ThrowsException() {
        PackResources original = new ExceptionPackResources();
        PackResourcesAdapter adapter = new PackResourcesAdapter(original);

        expectedException.expect(RuntimeException.class);
        adapter.getResources(PackType.CLIENT_RESOURCES, "minecraft",
                "textures", (file) -> file.getPath().endsWith(".png"));
    }

    @Test
    public void getNamespaces_TypeNull_NullPointerException() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.getNamespaces(null);
    }

    @Test
    public void getNamespaces_ClientType_ClientNamespaces() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        assertEquals(Set.of("minecraft", "sea", "moremcmeta"), adapter.getNamespaces(PackType.CLIENT_RESOURCES));
    }

    @Test
    public void getNamespaces_ServerType_ServerNamespaces() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        assertEquals(Set.of("minecraft", "sea"), adapter.getNamespaces(PackType.SERVER_DATA));
    }

    private PackResourcesAdapter makeAdapterWithResources() {
        Set<String> rootResources = Set.of("image.png", "info.txt", "readme.md");
        Map<PackType, Set<ResourceLocation>> regularResources = new HashMap<>();
        regularResources.put(PackType.CLIENT_RESOURCES, Set.of(new ResourceLocation("textures/hello.png"),
                new ResourceLocation("textures/block/sea/rock/gravel.png"),
                new ResourceLocation("sea", "textures/block/coral.png"),
                new ResourceLocation("lang/en/us/words.txt"),
                new ResourceLocation("moremcmeta", "config/textures/settings.json")));
        regularResources.put(PackType.SERVER_DATA, Set.of(new ResourceLocation("settings/server/network/config.json"),
                new ResourceLocation("lang/en/us/words.txt"),
                new ResourceLocation("sea", "textures/block/coral.png"),
                new ResourceLocation("textures/block/sea/rock/gravel.png")));

        PackResources original = new MockPackResources(rootResources, regularResources, "pack name");

        return new PackResourcesAdapter(original);
    }

    /**
     * Dummy implementation of {@link PackResources} that always throws a runtime exception.
     * @author soir20
     */
    private static class ExceptionPackResources implements PackResources {

        @Override
        public InputStream getRootResource(String string) {
            throw new RuntimeException("dummy getRootResource exception");
        }

        @Override
        public InputStream getResource(PackType packType, ResourceLocation resourceLocation) {
            throw new RuntimeException("dummy getResource exception");
        }

        @Override
        public Collection<ResourceLocation> getResources(PackType packType, String namespace, String pathStart,
                                                         Predicate<ResourceLocation> fileFilter) {
            throw new RuntimeException("dummy getResources exception");
        }

        @Override
        public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
            throw new RuntimeException("dummy hasResource exception");
        }

        @Override
        public Set<String> getNamespaces(PackType packType) {
            throw new RuntimeException("dummy hasNamespaces exception");
        }

        @Override
        public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
            throw new RuntimeException("dummy getMetadataSection exception");
        }

        @Override
        public String getName() {
            throw new RuntimeException("dummy getName exception");
        }

        @Override
        public void close() {
            throw new RuntimeException("dummy close exception");
        }
    }

}