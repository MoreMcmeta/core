/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Mock {@link PackResources} implementation for testing.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MockPackResources implements PackResources {
    private final Set<String> ROOT_RESOURCES;
    private final Map<PackType, Set<ResourceLocation>> REGULAR_RESOURCES;
    private final String NAME;

    public MockPackResources() {
        ROOT_RESOURCES = new HashSet<>();
        REGULAR_RESOURCES = new HashMap<>();
        NAME = "name";
    }

    public MockPackResources(Set<String> rootResources, Map<PackType, Set<ResourceLocation>> regularResources,
                             String name) {
        ROOT_RESOURCES = rootResources;
        REGULAR_RESOURCES = regularResources;
        NAME = name;
    }

    @Override
    public InputStream getRootResource(String string) throws IOException {
        if (ROOT_RESOURCES.contains(string)) {
            return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        }

        throw new IOException("Root resource not found in dummy pack: " + string);
    }

    @Override
    public InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException {
        if (REGULAR_RESOURCES.getOrDefault(packType, new HashSet<>()).contains(resourceLocation)) {
            return new ByteArrayInputStream(resourceLocation.getPath().getBytes(StandardCharsets.UTF_8));
        }

        throw new IOException("Resource not found in dummy pack: " + resourceLocation);
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String namespace, String pathStart,
                                                     int maxDepth, Predicate<String> pathFilter) {

        String directoryStart = pathStart.length() > 0 ? pathStart + "/" : "";

        // Simplified code from the SpriteFrameSizeFixPack
        return REGULAR_RESOURCES.getOrDefault(packType, new HashSet<>()).stream().filter((location) -> {
            String path = location.getPath();
            boolean isRightNamespace = location.getNamespace().equals(namespace);
            boolean isRightPath = path.startsWith(directoryStart) && pathFilter.test(path);
            boolean isRightDepth = isRightPath &&
                    StringUtils.countMatches(path.substring(directoryStart.length()), '/') <= maxDepth - 1;

            return isRightNamespace && isRightDepth;
        }).collect(Collectors.toList());

    }

    @Override
    public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
        return REGULAR_RESOURCES.getOrDefault(packType, new HashSet<>()).contains(resourceLocation);
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return REGULAR_RESOURCES.getOrDefault(packType, new HashSet<>()).stream()
                .map(ResourceLocation::getNamespace).collect(Collectors.toSet());
    }

    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void close() {}

}
