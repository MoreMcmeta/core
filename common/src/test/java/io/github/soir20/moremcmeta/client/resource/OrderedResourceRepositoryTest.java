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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests the {@link OrderedResourceRepository}.
 * @author soir20
 */
public class OrderedResourceRepositoryTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullPackType_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new OrderedResourceRepository(null,
                List.of(new MockResourceCollection(Set.of(new ResourceLocation("one.png")))));
    }

    @Test
    public void construct_NullCollectionList_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new OrderedResourceRepository(PackType.CLIENT_RESOURCES, null);
    }

    @Test
    public void construct_NullCollection_NullPointerException() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(null);

        expectedException.expect(NullPointerException.class);
        new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);
    }

    @Test
    public void getResourceType_ClientType_ReturnsClient() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);
        assertEquals(PackType.CLIENT_RESOURCES, repository.getResourceType());
    }

    @Test
    public void getResourceType_ServerType_ReturnsServer() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.SERVER_DATA, collections);
        assertEquals(PackType.SERVER_DATA, repository.getResourceType());
    }

    @Test
    public void getFirstCollectionWith_NullLocation_NullPointerException() throws IOException {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        expectedException.expect(NullPointerException.class);
        repository.getFirstCollectionWith(null);
    }

    @Test
    public void getFirstCollectionWith_LocationNotFound_IOException() throws IOException {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        expectedException.expect(IOException.class);
        repository.getFirstCollectionWith(new ResourceLocation("four.png"));
    }

    @Test
    public void getFirstCollectionWith_LocationInSeveralCollections_FirstCollectionFound() throws IOException {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        assertEquals(collections.get(1), repository.getFirstCollectionWith(new ResourceLocation("two.png")));
    }

    @Test
    public void getFirstCollectionWith_ResourceInFirstPack_FirstPackFound() throws IOException {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        assertEquals(collections.get(0), repository.getFirstCollectionWith(new ResourceLocation("one.png")));
    }

    @Test
    public void getFirstCollectionWith_ResourceInMiddlePack_MiddlePackFound() throws IOException {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        assertEquals(collections.get(1), repository.getFirstCollectionWith(new ResourceLocation("two.png")));
    }

    @Test
    public void getFirstCollectionWith_ResourceInLastPack_LastPackFound() throws IOException {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        assertEquals(collections.get(2), repository.getFirstCollectionWith(new ResourceLocation("three.png")));
    }

    @Test
    public void getFirstCollectionWith_ClientRepository_ServerResourcesSkipped() throws IOException {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png")),
                Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        assertEquals(collections.get(1), repository.getFirstCollectionWith(new ResourceLocation("two.png")));
    }

    @Test
    public void getFirstCollectionWith_ServerRepository_ClientResourcesSkipped() throws IOException {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png")),
                Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.SERVER_DATA, collections);

        assertEquals(collections.get(1), repository.getFirstCollectionWith(new ResourceLocation("two.png")));
    }

    @Test
    public void hasResource_NullLocation_NullPointerException() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        expectedException.expect(NullPointerException.class);
        repository.hasResource(null);
    }

    @Test
    public void hasResource_ResourceNotInAnyPack_NotFound() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        assertFalse(repository.hasResource(new ResourceLocation("four.png")));
    }

    @Test
    public void hasResource_ResourceInFirstPackClient_Found() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        assertTrue(repository.hasResource(new ResourceLocation("one.png")));
    }

    @Test
    public void hasResource_ResourceInMiddlePackClient_Found() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        assertTrue(repository.hasResource(new ResourceLocation("two.png")));
    }

    @Test
    public void hasResource_ResourceInLastPackClient_Found() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        assertTrue(repository.hasResource(new ResourceLocation("three.png")));
    }

    @Test
    public void hasResource_ResourceInFirstPackServer_Found() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.SERVER_DATA, collections);

        assertTrue(repository.hasResource(new ResourceLocation("one.png")));
    }

    @Test
    public void hasResource_ResourceInMiddlePackServer_Found() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.SERVER_DATA, collections);

        assertTrue(repository.hasResource(new ResourceLocation("two.png")));
    }

    @Test
    public void hasResource_ResourceInLastPackServer_Found() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.SERVER_DATA, collections);

        assertTrue(repository.hasResource(new ResourceLocation("three.png")));
    }

    @Test
    public void listResources_NullPathStart_NullPointerException() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        expectedException.expect(NullPointerException.class);
        repository.listResources(null, (file) -> true);
    }

    @Test
    public void listResources_NullFilter_NullPointerException() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        expectedException.expect(NullPointerException.class);
        repository.listResources("textures", null);
    }

    @Test
    public void listResources_PathStartGiven_PassedExactly() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("texture/two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("text/three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        // The mock resource collections don't add a slash, so this should match if passed exactly
        Collection<ResourceLocation> results = repository.listResources("text", (file) -> true);

        assertEquals(3, results.size());
        assertTrue(results.contains(new ResourceLocation("textures/one.png")));
        assertTrue(results.contains(new ResourceLocation("texture/two.png")));
        assertTrue(results.contains(new ResourceLocation("text/three.png")));
    }

    @Test
    public void listResources_PathStartBlank_PassedExactly() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("texture/two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("text/three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        // The mock resource collections don't add a slash, so this should match if passed exactly
        Collection<ResourceLocation> results = repository.listResources("", (file) -> true);

        assertEquals(3, results.size());
        assertTrue(results.contains(new ResourceLocation("textures/one.png")));
        assertTrue(results.contains(new ResourceLocation("texture/two.png")));
        assertTrue(results.contains(new ResourceLocation("text/three.png")));
    }

    @Test
    public void listResources_PathStartDifferent_NoMatches() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("texture/two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("text/three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        Collection<ResourceLocation> results = repository.listResources("ext", (file) -> true);
        assertEquals(0, results.size());
    }

    @Test
    public void listResources_FilterMatches_FoundMatches() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/two.jpg"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        Collection<ResourceLocation> results = repository.listResources("textures", (file) -> file.endsWith(".png"));
        assertEquals(2, results.size());
        assertTrue(results.contains(new ResourceLocation("textures/one.png")));
        assertTrue(results.contains(new ResourceLocation("textures/three.png")));
    }

    @Test
    public void listResources_FilterNoMatch_NoMatches() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/two.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        Collection<ResourceLocation> results = repository.listResources("textures", (file) -> file.endsWith(".jpg"));
        assertEquals(0, results.size());
    }

    @Test
    public void listResources_ClientRepository_FoundClientResources() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/two.jpg"))));
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("textures/three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.CLIENT_RESOURCES, collections);

        Collection<ResourceLocation> results = repository.listResources("textures", (file) -> file.endsWith(".png"));
        assertEquals(1, results.size());
        assertTrue(results.contains(new ResourceLocation("textures/one.png")));
    }

    @Test
    public void listResources_ServerRepository_FoundServerResources() {
        List<ResourceCollection> collections = new ArrayList<>();
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/one.png"))));
        collections.add(new MockResourceCollection(Set.of(new ResourceLocation("textures/two.jpg"))));
        collections.add(new MockResourceCollection(Set.of(), Set.of(new ResourceLocation("textures/three.png"))));

        OrderedResourceRepository repository = new OrderedResourceRepository(PackType.SERVER_DATA, collections);

        Collection<ResourceLocation> results = repository.listResources("textures", (file) -> file.endsWith(".png"));
        assertEquals(1, results.size());
        assertTrue(results.contains(new ResourceLocation("textures/three.png")));
    }

}