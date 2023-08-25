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

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import io.github.moremcmeta.moremcmeta.impl.client.resource.MockPackResources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
 * Tests the {@link RootResourcesAdapter}.
 * @author soir20
 */
public final class RootResourcesAdapterTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void locateForPackScreen_NullLocation_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        RootResourcesAdapter.locateForPackScreen(null);
    }

    @Test
    public void locateForPackScreen_NonRootLocation_SameLocationReturned() {
        ResourceLocation location = new ResourceLocation("test.png");
        assertEquals(location, RootResourcesAdapter.locateForPackScreen(location));
    }

    @Test
    public void locateForPackScreen_NonRootLocation_UsesMinecraftNamespace() {
        ResourceLocation location = new ResourceLocation(RootResourcesAdapter.ROOT_NAMESPACE, "test.png");
        assertEquals(new ResourceLocation("minecraft", "test.png"), RootResourcesAdapter.locateForPackScreen(location));
    }

    @Test
    public void construct_OriginalNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new RootResourcesAdapter(null);
    }

    @Test
    public void find_ResourceTypeNull_NullPointerException() throws IOException {
        RootResourcesAdapter adapter = new RootResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.find(null, new ResourceLocation("testing"));
    }

    @Test
    public void find_LocationNull_NullPointerException() throws IOException {
        RootResourcesAdapter adapter = new RootResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.find(PackType.CLIENT_RESOURCES, null);
    }

    @Test
    public void find_GetNonRootClientResource_IOException() throws IOException {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("textures/block/sea/rock/gravel.png");

        expectedException.expect(IOException.class);
        adapter.find(PackType.CLIENT_RESOURCES, location);
    }

    @Test
    public void find_GetNonRootServerResource_IOException() throws IOException {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");

        expectedException.expect(IOException.class);
        adapter.find(PackType.SERVER_DATA, location);
    }

    @Test
    public void find_GetPresentRootResourceClientType_ResourceRetrieved() throws IOException {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = adapter.locateRootResource("root.png.moremcmeta");
        InputStream resource = adapter.find(PackType.CLIENT_RESOURCES, location);

        assertEquals("root.png.moremcmeta", new String(resource.readAllBytes()));
    }

    @Test
    public void find_GetPresentRootResourceServerType_ResourceRetrieved() throws IOException {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = adapter.locateRootResource("root.png.moremcmeta");
        InputStream resource = adapter.find(PackType.SERVER_DATA, location);

        assertEquals("root.png.moremcmeta", new String(resource.readAllBytes()));
    }

    @Test
    public void contains_ResourceTypeNull_NullPointerException() {
        RootResourcesAdapter adapter = new RootResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.contains(null, new ResourceLocation("testing"));
    }

    @Test
    public void contains_LocationNull_NullPointerException() {
        RootResourcesAdapter adapter = new RootResourcesAdapter(new MockPackResources());
        expectedException.expect(NullPointerException.class);
        adapter.contains(PackType.CLIENT_RESOURCES, null);
    }

    @Test
    public void contains_GetNonRootClientResource_ResourceNotFound() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("textures/block/sea/rock/gravel.png");
        assertFalse(adapter.contains(PackType.CLIENT_RESOURCES, location));
    }

    @Test
    public void contains_GetNonRootServerResource_ResourceNotFound() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = new ResourceLocation("settings/server/network/config.json");
        assertFalse(adapter.contains(PackType.SERVER_DATA, location));
    }

    @Test
    public void contains_GetPresentRootResourceClientType_ResourceFound() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = adapter.locateRootResource("root.png.moremcmeta");
        assertTrue(adapter.contains(PackType.CLIENT_RESOURCES, location));
    }

    @Test
    public void contains_GetPresentRootResourceServerType_ResourceFound() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = adapter.locateRootResource("root.png.moremcmeta");
        assertTrue(adapter.contains(PackType.CLIENT_RESOURCES, location));
    }

    @Test
    public void list_TypeNull_NullPointerException() {
        RootResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.list(null, "minecraft", "textures", (file) -> true);
    }

    @Test
    public void list_NamespaceNull_NullPointerException() {
        RootResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.list(PackType.CLIENT_RESOURCES, null, "textures", (file) -> true);
    }

    @Test
    public void list_PathStartNull_NullPointerException() {
        RootResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.list(PackType.CLIENT_RESOURCES, "minecraft", null, (file) -> true);
    }

    @Test
    public void list_FileFilterNull_NullPointerException() {
        RootResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.list(PackType.CLIENT_RESOURCES, "minecraft", "textures", null);
    }

    @Test
    public void list_AllRootResources_NoneFound() {
        RootResourcesAdapter adapter = makeAdapterWithResources();

        Collection<ResourceLocation> resources = adapter.list(PackType.CLIENT_RESOURCES,
                "minecraft", "pack", (file) -> true);

        assertTrue(resources.isEmpty());
    }

    @Test
    public void getNamespaces_TypeNull_NullPointerException() {
        RootResourcesAdapter adapter = makeAdapterWithResources();

        expectedException.expect(NullPointerException.class);
        adapter.namespaces(null);
    }

    @Test
    public void getNamespaces_ClientType_None() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        assertEquals(ImmutableSet.of(RootResourcesAdapter.ROOT_NAMESPACE), adapter.namespaces(PackType.CLIENT_RESOURCES));
    }

    @Test
    public void getNamespaces_ServerType_Minecraft() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        assertEquals(ImmutableSet.of(RootResourcesAdapter.ROOT_NAMESPACE), adapter.namespaces(PackType.SERVER_DATA));
    }

    @Test
    public void locateRootResource_Null_NullPointerException() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        expectedException.expect(NullPointerException.class);
        adapter.locateRootResource(null);
    }

    @Test
    public void locateRootResource_PackPng_UniqueLocationRetrieved() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = adapter.locateRootResource("pack.png");
        assertEquals(new ResourceLocation(RootResourcesAdapter.ROOT_NAMESPACE, "pack/pack_name/400583302ac4dbbb6707031620374c9a45991149/icon"), location);
    }

    @Test
    public void locateRootResource_PackMetadata_UniqueLocationRetrieved() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = adapter.locateRootResource("pack.png.moremcmeta");
        assertEquals(new ResourceLocation(RootResourcesAdapter.ROOT_NAMESPACE, "pack/pack_name/400583302ac4dbbb6707031620374c9a45991149/icon.moremcmeta"), location);
    }

    @Test
    public void locateRootResource_NonPackPng_UniqueLocationRetrieved() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = adapter.locateRootResource("root.png");
        assertEquals(new ResourceLocation(RootResourcesAdapter.ROOT_NAMESPACE, "pack/pack_name/400583302ac4dbbb6707031620374c9a45991149/root.png"), location);
    }

    private RootResourcesAdapter makeAdapterWithResources() {
        Set<String> rootResources = Set.of("image.png", "info.txt", "readme.md", "root.png.moremcmeta");
        Map<PackType, Set<ResourceLocation>> regularResources = new HashMap<>();

        //noinspection deprecation
        regularResources.put(PackType.CLIENT_RESOURCES, Set.of(new ResourceLocation("textures/hello.png"),
                new ResourceLocation("textures/block/sea/rock/gravel.png"),
                new ResourceLocation("sea", "textures/block/coral.png"),
                new ResourceLocation("lang/en/us/words.txt"),
                new ResourceLocation("moremcmeta", "config/textures/settings.json"),
                new ResourceLocation("pack/pack_name/" + Hashing.sha1().hashUnencodedChars("pack name")
                        + "/in-pack.png.moremcmeta")));

        regularResources.put(PackType.SERVER_DATA, Set.of(new ResourceLocation("settings/server/network/config.json"),
                new ResourceLocation("lang/en/us/words.txt"),
                new ResourceLocation("sea", "textures/block/coral.png"),
                new ResourceLocation("textures/block/sea/rock/gravel.png")));

        PackResources original = new MockPackResources(rootResources, regularResources, "pack name");

        return new RootResourcesAdapter(original);
    }

}