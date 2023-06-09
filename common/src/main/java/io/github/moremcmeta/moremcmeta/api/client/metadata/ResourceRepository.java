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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * <p>A collection of ordered {@link Pack}s. Resources in packs that are "higher" are applied over
 * those in "lower" packs.</p>
 *
 * <p>This interface is analogous to a collection of Minecraft resource packs. However, MoreMcmeta does
 * not provide direct access to resource packs to prevent operations that should not be performed and
 * allow for any necessary internal modifications to the packs.</p>
 * @author soir20
 * @since 4.0.0
 */
public interface ResourceRepository {

    /**
     * Gets the highest pack that has a given resource.
     * @param location      location of the resource
     * @return highest pack that has a given resource, if any
     */
    Optional<Pack> highestPackWith(ResourceLocation location);

    /**
     * Searches for resources that exist in any currently-applied resource pack.
     * @param fileFilter    returns true for resource location paths that should be included in the results
     * @return all resources that match the provided filter
     */
    Set<? extends ResourceLocation> list(Predicate<String> fileFilter);

    /**
     * A pack that contains resources as streams of bytes.
     * @author soir20
     * @since 4.0.0
     */
    interface Pack {

        /**
         * Gets a resource from this pack, if it is present.
         * @param location      location of the resource to retrieve
         * @return resource as a byte stream, if present
         */
        Optional<InputStream> resource(ResourceLocation location);

    }

}
