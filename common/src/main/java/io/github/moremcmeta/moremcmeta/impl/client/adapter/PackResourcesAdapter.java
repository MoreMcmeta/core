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

import io.github.moremcmeta.moremcmeta.impl.client.resource.ResourceCollection;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Allows Minecraft's {@link PackResources} to implement {@link ResourceCollection}.
 * @author soir20
 */
public final class PackResourcesAdapter implements ResourceCollection {
    private final PackResources ORIGINAL;
    private final Logger LOGGER;

    /**
     * Creates an adapter for resource packs.
     * @param original      the original pack to delegate to
     * @param logger        logger to log warnings or errors
     */
    public PackResourcesAdapter(PackResources original, Logger logger) {
        ORIGINAL = requireNonNull(original, "Original pack cannot be null");
        LOGGER = requireNonNull(logger, "Logger cannot be null");
    }

    @Override
    public InputStream find(PackType resourceType, ResourceLocation location) throws IOException {
        requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(location, "Location cannot be null");
        return ORIGINAL.getResource(resourceType, location);
    }

    @Override
    public boolean contains(PackType resourceType, ResourceLocation location) {
        requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(location, "Location cannot be null");
        return ORIGINAL.hasResource(resourceType, location);
    }

    @Override
    public Collection<ResourceLocation> list(PackType resourceType, String namespace, String pathStart,
                                             Predicate<String> fileFilter) {
        requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(namespace, "Namespace cannot be null");
        requireNonNull(pathStart, "Path start cannot be null");
        requireNonNull(fileFilter, "File filter cannot be null");


        /* We should catch ResourceLocation errors to prevent bad texture names/paths from
           removing all resource packs. We can't filter invalid folder names, so we don't filter
           invalid texture names for consistency.
           NOTE: Some pack types (like FolderPack) handle bad locations before we see them. */
        try {
            return ORIGINAL.getResources(resourceType, namespace, pathStart, Integer.MAX_VALUE, fileFilter);
        } catch (ResourceLocationException error) {
            LOGGER.error("Found texture with invalid name in pack {}; cannot return any resources " +
                            "from this pack: {}", ORIGINAL.getName(), error.toString());
            return List.of();
        }

    }

    @Override
    public Set<String> namespaces(PackType resourceType) {
        requireNonNull(resourceType, "Resource type cannot be null");
        return ORIGINAL.getNamespaces(resourceType);
    }

}
