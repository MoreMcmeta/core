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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mock {@link PackResources} implementation for testing.
 * @author soir20
 */
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
    public IoSupplier<InputStream> getRootResource(String... strings) {
        for (String file : strings) {
            if (ROOT_RESOURCES.contains(file)) {
                return () -> makeDemoStream(file);
            }
        }

        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        if (REGULAR_RESOURCES.getOrDefault(packType, new HashSet<>()).contains(resourceLocation)) {
            return () -> makeDemoStream(resourceLocation.getPath());
        }

        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String pathStart, ResourceOutput resourceOutput) {

        String directoryStart = !pathStart.isEmpty() ? pathStart + "/" : "";

        // Simplified code from the SpriteFrameSizeFixPack
        REGULAR_RESOURCES.getOrDefault(packType, new HashSet<>()).stream().filter((location) -> {
            String path = location.getPath();
            boolean isRightNamespace = location.getNamespace().equals(namespace);
            boolean isRightPath = path.startsWith(directoryStart);
            return isRightNamespace && isRightPath;
        }).forEach((location) -> resourceOutput.accept(location, () -> makeDemoStream(location.getPath())));

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
    public String packId() {
        return NAME;
    }

    @Override
    public void close() {}

    private static InputStream makeDemoStream(String string) {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }

}
