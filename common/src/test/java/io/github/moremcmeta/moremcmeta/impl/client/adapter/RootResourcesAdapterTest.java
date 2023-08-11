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
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    public void construct_OriginalNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new RootResourcesAdapter(null, "dummy-pack");
    }

    @Test
    public void construct_PackIdNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new RootResourcesAdapter(new MockPackResources(), null);
    }

    @Test
    public void find_ResourceTypeNull_NullPointerException() throws IOException {
        RootResourcesAdapter adapter = new RootResourcesAdapter(new MockPackResources(), "dummy-pack");
        expectedException.expect(NullPointerException.class);
        adapter.find(null, new ResourceLocation("testing"));
    }

    @Test
    public void find_LocationNull_NullPointerException() throws IOException {
        RootResourcesAdapter adapter = new RootResourcesAdapter(new MockPackResources(), "dummy-pack");
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

        assertEquals("root.png.moremcmeta", IOUtils.toString(resource, StandardCharsets.UTF_8));
    }

    @Test
    public void find_GetPresentRootResourceServerType_ResourceRetrieved() throws IOException {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = adapter.locateRootResource("root.png.moremcmeta");
        InputStream resource = adapter.find(PackType.SERVER_DATA, location);

        assertEquals("root.png.moremcmeta", IOUtils.toString(resource, StandardCharsets.UTF_8));
    }

    @Test
    public void contains_ResourceTypeNull_NullPointerException() {
        RootResourcesAdapter adapter = new RootResourcesAdapter(new MockPackResources(), "dummy-pack");
        expectedException.expect(NullPointerException.class);
        adapter.contains(null, new ResourceLocation("testing"));
    }

    @Test
    public void contains_LocationNull_NullPointerException() {
        RootResourcesAdapter adapter = new RootResourcesAdapter(new MockPackResources(), "dummy-pack");
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
        assertTrue(adapter.namespaces(PackType.CLIENT_RESOURCES).isEmpty());
    }

    @Test
    public void getNamespaces_ServerType_None() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        assertTrue(adapter.namespaces(PackType.SERVER_DATA).isEmpty());
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
        assertEquals(new ResourceLocation("minecraft:pack/dummy-pack/1838f274a7ef6e95946a2ff69b5d1aab67bcc566/icon"), location);
    }

    @Test
    public void locateRootResource_PackMetadata_UniqueLocationRetrieved() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = adapter.locateRootResource("pack.png.moremcmeta");
        assertEquals(new ResourceLocation("minecraft:pack/dummy-pack/1838f274a7ef6e95946a2ff69b5d1aab67bcc566/icon.moremcmeta"), location);
    }

    @Test
    public void locateRootResource_NonPackPng_UniqueLocationRetrieved() {
        RootResourcesAdapter adapter = makeAdapterWithResources();
        ResourceLocation location = adapter.locateRootResource("root.png");
        assertEquals(new ResourceLocation("minecraft:pack/dummy-pack/1838f274a7ef6e95946a2ff69b5d1aab67bcc566/root.png"), location);
    }

    private RootResourcesAdapter makeAdapterWithResources() {
        String packId = "dummy-pack";

        Set<String> rootResources = ImmutableSet.of("image.png", "info.txt", "readme.md", "root.png.moremcmeta");
        Map<PackType, Set<ResourceLocation>> regularResources = new HashMap<>();

        //noinspection deprecation
        regularResources.put(PackType.CLIENT_RESOURCES, ImmutableSet.of(new ResourceLocation("textures/hello.png"),
                new ResourceLocation("textures/block/sea/rock/gravel.png"),
                new ResourceLocation("sea", "textures/block/coral.png"),
                new ResourceLocation("lang/en/us/words.txt"),
                new ResourceLocation("moremcmeta", "config/textures/settings.json"),
                new ResourceLocation("pack/dummy-pack/" + Hashing.sha1().hashUnencodedChars(packId)
                        + "/in-pack.png.moremcmeta")));

        regularResources.put(PackType.SERVER_DATA, ImmutableSet.of(new ResourceLocation("settings/server/network/config.json"),
                new ResourceLocation("lang/en/us/words.txt"),
                new ResourceLocation("sea", "textures/block/coral.png"),
                new ResourceLocation("textures/block/sea/rock/gravel.png")));

        PackResources original = new MockPackResources(rootResources, regularResources, "pack name");

        return new RootResourcesAdapter(original, packId);
    }

}