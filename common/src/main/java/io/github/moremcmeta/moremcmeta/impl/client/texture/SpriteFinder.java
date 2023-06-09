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

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Searches atlas textures for sprites.
 * @author soir20
 */
public final class SpriteFinder {
    private static final ImmutableSet<ResourceLocation> ATLAS_LOCATIONS = ImmutableSet.of(
            new ResourceLocation("textures/atlas/blocks.png"),
            new ResourceLocation("textures/atlas/signs.png"),
            new ResourceLocation("textures/atlas/banner_patterns.png"),
            new ResourceLocation("textures/atlas/shield_patterns.png"),
            new ResourceLocation("textures/atlas/chest.png"),
            new ResourceLocation("textures/atlas/beds.png"),
            new ResourceLocation("textures/atlas/particles.png"),
            new ResourceLocation("textures/atlas/paintings.png"),
            new ResourceLocation("textures/atlas/mob_effects.png")
    );

    private final Function<ResourceLocation, ? extends Atlas> ATLAS_GETTER;

    /**
     * Creates a new sprite finder.
     * @param atlasGetter  provides an atlas from a location
     */
    public SpriteFinder(Function<ResourceLocation, ? extends Atlas> atlasGetter) {
        ATLAS_GETTER = requireNonNull(atlasGetter, "Atlas getter cannot be null");
    }

    /**
     * Finds an atlas associated with a texture location.
     * @param location          the location of the texture
     * @return an {@link Optional} containing the atlas sprite
     */
    public Optional<Sprite> findSprite(ResourceLocation location) {
        requireNonNull(location, "Location cannot be null");

        return findNew(location);
    }

    /**
     * Finds an atlas associated with a texture location not yet cached.
     * @param location      the location of the texture to look for (with extension)
     * @return an {@link Optional} containing the atlas sprite
     */
    private Optional<Sprite> findNew(ResourceLocation location) {
        for (ResourceLocation atlasLocation : ATLAS_LOCATIONS) {
            Atlas atlas = ATLAS_GETTER.apply(atlasLocation);
            requireNonNull(atlas, "Atlas getter cannot supply null");

            Optional<Sprite> sprite = atlas.sprite(location);
            if (sprite.isPresent()) {
                return sprite;
            }

        }

        return Optional.empty();
    }

}
