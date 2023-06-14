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

package io.github.moremcmeta.moremcmeta.impl.client.adapter;

import io.github.moremcmeta.moremcmeta.impl.client.resource.MockPackResources;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void find_ResourceTypeNull_NullPointerException() throws IOException {
        PackResourcesAdapter adapter = new PackResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.find(null, new ResourceLocation("testing"));
    }

    @Test
    public void find_LocationNull_NullPointerException() throws IOException {
        PackResourcesAdapter adapter = new PackResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.find(PackType.CLIENT_RESOURCES, null);
    }

    @Test
    public void find_GetExistingClientResource_ResourceRetrieved() throws IOException {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("textures/block/sea/rock/gravel.png");
        InputStream resource = adapter.find(PackType.CLIENT_RESOURCES, location);

        assertEquals(location.getPath(), new String(resource.readAllBytes()));
    }

    @Test
    public void find_GetExistingServerResource_ResourceRetrieved() throws IOException {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");
        InputStream resource = adapter.find(PackType.SERVER_DATA, location);

        assertEquals(location.getPath(), new String(resource.readAllBytes()));
    }

    @Test
    public void find_GetNotExistingClientResource_IOException() throws IOException {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("textures/block/sea/rock/other.png");

        expectedException.expect(IOException.class);
        adapter.find(PackType.CLIENT_RESOURCES, location);
    }

    @Test
    public void find_GetNotExistingServerResource_IOException() throws IOException {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/other.json");

        expectedException.expect(IOException.class);
        adapter.find(PackType.SERVER_DATA, location);
    }

    @Test
    public void find_GetResourceDifferentResourceType_IOException() throws IOException {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");

        expectedException.expect(IOException.class);
        adapter.find(PackType.CLIENT_RESOURCES, location);
    }

    @Test
    public void find_OriginalThrowsException_ThrowsException() throws IOException {
        PackResources original = new ExceptionPackResources();
        PackResourcesAdapter adapter = new PackResourcesAdapter(original);

        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");

        expectedException.expect(RuntimeException.class);
        adapter.find(PackType.SERVER_DATA, location);
    }

    @Test
    public void contains_ResourceTypeNull_NullPointerException() {
        PackResourcesAdapter adapter = new PackResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.contains(null, new ResourceLocation("testing"));
    }

    @Test
    public void contains_LocationNull_NullPointerException() {
        PackResourcesAdapter adapter = new PackResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.contains(PackType.CLIENT_RESOURCES, null);
    }

    @Test
    public void contains_GetExistingClientResource_ResourceFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("textures/block/sea/rock/gravel.png");
        assertTrue(adapter.contains(PackType.CLIENT_RESOURCES, location));
    }

    @Test
    public void contains_GetExistingServerResource_ResourceFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");
        assertTrue(adapter.contains(PackType.SERVER_DATA, location));
    }

    @Test
    public void contains_GetNotExistingClientResource_ResourceNotFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("textures/block/sea/rock/other.png");
        assertFalse(adapter.contains(PackType.CLIENT_RESOURCES, location));
    }

    @Test
    public void contains_GetNotExistingServerResource_ResourceNotFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/other.json");
        assertFalse(adapter.contains(PackType.SERVER_DATA, location));
    }

    @Test
    public void contains_GetResourceDifferentResourceType_ResourceNotFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");
        assertFalse(adapter.contains(PackType.CLIENT_RESOURCES, location));
    }

    @Test
    public void contains_OriginalThrowsException_ThrowsException() {
        PackResources original = new ExceptionPackResources();
        PackResourcesAdapter adapter = new PackResourcesAdapter(original);

        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");

        expectedException.expect(RuntimeException.class);
        adapter.contains(PackType.SERVER_DATA, location);
    }

    @Test
    public void list_TypeNull_NullPointerException() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.list(null, "minecraft", "textures", (file) -> true);
    }

    @Test
    public void list_NamespaceNull_NullPointerException() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.list(PackType.CLIENT_RESOURCES, null, "textures", (file) -> true);
    }

    @Test
    public void list_PathStartNull_NullPointerException() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.list(PackType.CLIENT_RESOURCES, "minecraft", null, (file) -> true);
    }

    @Test
    public void list_FileFilterNull_NullPointerException() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.list(PackType.CLIENT_RESOURCES, "minecraft", "textures", null);
    }

    @Test
    public void list_SomeMatchAllFiltersClient_MatchingFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.list(PackType.CLIENT_RESOURCES, "minecraft",
                "textures", (file) -> file.endsWith(".png"));

        assertEquals(2, resources.size());
        assertTrue(resources.contains(new ResourceLocation("textures/hello.png")));
        assertTrue(resources.contains(new ResourceLocation("textures/block/sea/rock/gravel.png")));
    }

    @Test
    public void list_SomeMatchAllFiltersServer_MatchingFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.list(PackType.SERVER_DATA, "sea",
                "textures", (file) -> file.endsWith(".png"));

        assertEquals(1, resources.size());
        assertTrue(resources.contains(new ResourceLocation("sea", "textures/block/coral.png")));
    }

    @Test
    public void list_NoneMatchNamespace_NoneFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.list(PackType.CLIENT_RESOURCES, "other",
                "textures", (file) -> file.endsWith(".png"));

        assertEquals(0, resources.size());
    }

    @Test
    public void list_NoneMatchPathStart_NoneFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.list(PackType.CLIENT_RESOURCES, "minecraft",
                "other", (file) -> file.endsWith(".png"));

        assertEquals(0, resources.size());
    }

    @Test
    public void list_EmptyNamespace_NoExceptionIfOriginalAccepts() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.list(PackType.CLIENT_RESOURCES, "",
                "textures", (file) -> file.endsWith(".png"));

        assertEquals(0, resources.size());
    }

    @Test
    public void list_EmptyPathStart_NoExceptionIfOriginalAccepts() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.list(PackType.CLIENT_RESOURCES, "minecraft",
                "", (file) -> file.endsWith(".png"));

        assertEquals(2, resources.size());
        assertTrue(resources.contains(new ResourceLocation("textures/hello.png")));
        assertTrue(resources.contains(new ResourceLocation("textures/block/sea/rock/gravel.png")));
    }

    @Test
    public void list_NoneMatchFilter_NoneFound() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.list(PackType.CLIENT_RESOURCES, "minecraft",
                "textures", (file) -> file.endsWith(".jpg"));

        assertEquals(0, resources.size());
    }

    @Test
    public void list_OriginalThrowsException_ThrowsException() {
        PackResources original = new ExceptionPackResources();
        PackResourcesAdapter adapter = new PackResourcesAdapter(original);

        expectedException.expect(RuntimeException.class);
        adapter.list(PackType.CLIENT_RESOURCES, "minecraft",
                "textures", (file) -> file.endsWith(".png"));
    }

    @Test
    public void getNamespaces_TypeNull_NullPointerException() {
        PackResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.namespaces(null);
    }

    @Test
    public void getNamespaces_ClientType_ClientNamespaces() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        assertEquals(Set.of("minecraft", "sea", "moremcmeta"), adapter.namespaces(PackType.CLIENT_RESOURCES));
    }

    @Test
    public void getNamespaces_ServerType_ServerNamespaces() {
        PackResourcesAdapter adapter = makeAdapterWithResources();
        assertEquals(Set.of("minecraft", "sea"), adapter.namespaces(PackType.SERVER_DATA));
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
    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    private static class ExceptionPackResources implements PackResources {

        @Override
        public IoSupplier<InputStream> getRootResource(String... strings) {
            throw new RuntimeException("dummy getRootResource exception");
        }

        @Override
        public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
            throw new RuntimeException("dummy getResource exception");
        }

        @Override
        public void listResources(PackType packType, String namespace, String pathStart,
                                  ResourceOutput resourceOutput) {
            throw new RuntimeException("dummy getResources exception");
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
        public String packId() {
            throw new RuntimeException("dummy getName exception");
        }

        @Override
        public void close() {
            throw new RuntimeException("dummy close exception");
        }
    }

}