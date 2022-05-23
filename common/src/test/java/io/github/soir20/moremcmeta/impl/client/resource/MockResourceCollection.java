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

package io.github.soir20.moremcmeta.impl.client.resource;

import io.github.soir20.moremcmeta.impl.client.resource.ResourceCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Mock class implementing {@link ResourceCollection}. Uses the {@link ResourceLocation} of the resource
 * as the resource's input stream.
 * @author soir20
 */
public class MockResourceCollection implements ResourceCollection {
    private final Set<ResourceLocation> CLIENT_RESOURCES;
    private final Set<ResourceLocation> SERVER_RESOURCES;

    public MockResourceCollection(Set<ResourceLocation> clientResources) {
        CLIENT_RESOURCES = clientResources;
        SERVER_RESOURCES = Set.of();
    }

    public MockResourceCollection(Set<ResourceLocation> clientResources,
                                  Set<ResourceLocation> serverResources) {
        CLIENT_RESOURCES = clientResources;
        SERVER_RESOURCES = serverResources;
    }

    @Override
    public InputStream getResource(PackType resourceType, ResourceLocation location) throws IOException {
        if (hasResource(resourceType, location)) {
            return new ByteArrayInputStream(location.toString().getBytes(StandardCharsets.UTF_8));
        }

        throw new IOException("Mock collection doesn't have resource: " + location.toString());
    }

    @Override
    public boolean hasResource(PackType resourceType, ResourceLocation location) {
        boolean hasClientResource = resourceType == PackType.CLIENT_RESOURCES
                && CLIENT_RESOURCES.contains(location);
        boolean hasServerResource = resourceType == PackType.SERVER_DATA
                && SERVER_RESOURCES.contains(location);

        return hasClientResource || hasServerResource;
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType resourceType, String namespace, String pathStart,
                                                     Predicate<String> fileFilter) {
        Collection<ResourceLocation> locationsOfType = resourceType == PackType.CLIENT_RESOURCES ?
                CLIENT_RESOURCES : SERVER_RESOURCES;
        return locationsOfType.stream()
                .filter((location) -> location.getNamespace().equals(namespace))
                .filter((location) -> location.getPath().startsWith(pathStart))
                .filter((location) -> fileFilter.test(location.getPath()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getNamespaces(PackType resourceType) {
        if (resourceType == PackType.CLIENT_RESOURCES) {
            return CLIENT_RESOURCES.stream().map(ResourceLocation::getNamespace).collect(Collectors.toSet());
        }

        return SERVER_RESOURCES.stream().map(ResourceLocation::getNamespace).collect(Collectors.toSet());
    }
}
