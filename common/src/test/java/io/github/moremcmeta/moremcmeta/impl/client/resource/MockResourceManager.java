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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A mock resource manager that can provide fake found/missing files.
 * @author soir20
 */
@SuppressWarnings("unused")
public class MockResourceManager implements ResourceManager {
    public static final String DEFAULT_PACK_NAME = "test";
    private static final InputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[] {});

    private final List<String> FILES_TO_ADD;
    private final List<String> MISSING_FILES;
    private final boolean USE_UNIQUE_PACK_NAMES;

    private String lastPackName = DEFAULT_PACK_NAME;

    public MockResourceManager(List<String> filesToAdd, List<String> missingFiles, boolean useUniquePackNames) {
        FILES_TO_ADD = filesToAdd;
        MISSING_FILES = missingFiles;
        USE_UNIQUE_PACK_NAMES = useUniquePackNames;
    }

    @Override
    public Set<String> getNamespaces() {
        return ImmutableSet.of();
    }

    @Override
    public Resource getResource(ResourceLocation resourceLocationIn) throws IOException {
        int fileNameStart = resourceLocationIn.getPath().lastIndexOf('/') + 1;
        boolean isMissing = MISSING_FILES.contains(resourceLocationIn.getPath().substring(fileNameStart));

        if (isMissing) {
            throw new IOException();
        } else {

            // Add a dummy character to make pack names different for every resource
            if (USE_UNIQUE_PACK_NAMES) {
                lastPackName += "a";
            }

            return new MockSimpleResource(resourceLocationIn, lastPackName);
        }
    }

    @Override
    public boolean hasResource(ResourceLocation path) {
        return false;
    }

    @Override
    public List<Resource> getResources(ResourceLocation resourceLocationIn) {
        return ImmutableList.of();
    }

    @Override
    public Collection<ResourceLocation> listResources(String pathIn, Predicate<String> filter) {
        List<Pair<String, String>> namespacesAndPaths = new ArrayList<>();

        FILES_TO_ADD.forEach((fileName) -> {
            String[] namespaceAndFile = fileName.split(":", 2);

            if (namespaceAndFile.length == 1) {
                namespacesAndPaths.add(new Pair<>("minecraft", pathIn + "/" + namespaceAndFile[0]));
            } else {
                namespacesAndPaths.add(new Pair<>(namespaceAndFile[0], pathIn + "/" + namespaceAndFile[1]));
            }

        });
        namespacesAndPaths.removeIf(namespaceAndPath -> filter.negate().test(namespaceAndPath.getSecond()));

        List<ResourceLocation> locations = new ArrayList<>();
        namespacesAndPaths.forEach(namespaceAndPath -> locations.add(
                new ResourceLocation(namespaceAndPath.getFirst(), namespaceAndPath.getSecond())
        ));

        return locations;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return Stream.of();
    }

    /**
     * Acts as fake resource. This is a separate class in case we need to override metadata parsing
     * in the future.
     * @author soir20
     */
    public static class MockSimpleResource extends SimpleResource {
        public MockSimpleResource(ResourceLocation locationIn, String packName) {
            super(packName, locationIn, EMPTY_STREAM, null);
        }
    }

}
