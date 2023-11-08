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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Searches atlas textures for sprites.
 * @author soir20
 */
public final class SpriteFinder {
    private final Function<ResourceLocation, ? extends Atlas> ATLAS_GETTER;
    private final Set<ResourceLocation> ATLAS_LOCATIONS;

    /**
     * Creates a new sprite finder.
     * @param atlasGetter       provides an atlas from a location
     * @param atlasLocations    locations of all texture atlases
     */
    public SpriteFinder(Function<ResourceLocation, ? extends Atlas> atlasGetter, Set<ResourceLocation> atlasLocations) {
        ATLAS_GETTER = requireNonNull(atlasGetter, "Atlas getter cannot be null");
        ATLAS_LOCATIONS = ImmutableSet.copyOf(requireNonNull(atlasLocations, "Atlas locations cannot be null"));
    }

    /**
     * Finds an atlas associated with a texture location.
     * @param location          the location of the texture
     * @return an {@link Optional} containing the atlas sprite
     */
    public List<Sprite> findSprites(ResourceLocation location) {
        requireNonNull(location, "Location cannot be null");
        return findNew(location);
    }

    /**
     * Finds an atlas associated with a texture location not yet cached.
     * @param location      the location of the texture to look for (with extension)
     * @return an {@link Optional} containing the atlas sprite
     */
    private List<Sprite> findNew(ResourceLocation location) {
        List<Sprite> results = new ArrayList<>();

        for (ResourceLocation atlasLocation : ATLAS_LOCATIONS) {
            Atlas atlas = ATLAS_GETTER.apply(atlasLocation);
            requireNonNull(atlas, "Atlas getter cannot supply null");
            atlas.sprite(location).ifPresent(results::add);
        }

        return results;
    }

}
