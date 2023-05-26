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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import io.github.moremcmeta.moremcmeta.api.client.metadata.Base;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A collection of bases for a particular texture.
 * @author soir20
 */
public class BaseCollection {
    private final Map<ResourceLocation, Collection<MipmappedBase>> BASES;
    private final int MAX_MIPMAP;

    /**
     * Finds all bases associated with the given texture. Duplicates (where both the base
     * and the mipmap level are the same) are combined.
     * @param spriteFinder      checks if a texture is a sprite stitched onto an atlas
     * @param knownBases        all already-known bases as defined in metadata
     * @param textureLocation   location of the texture whose bases to retrieve
     * @return all bases associated with the texture at the given location
     */
    public static BaseCollection find(SpriteFinder spriteFinder, Collection<Base> knownBases,
                                      ResourceLocation textureLocation) {
        requireNonNull(spriteFinder, "Sprite finder cannot be null");
        requireNonNull(knownBases, "Known bases cannot be null");
        requireNonNull(textureLocation, "Texture location cannot be null");

        Map<ResourceLocation, Collection<MipmappedBase>> newBases = new HashMap<>();

        // Always add the provided texture as its own base
        knownBases = new ArrayList<>(knownBases);
        knownBases.add(new Base(textureLocation, EventDrivenTexture.SELF_UPLOAD_POINT));

        for (Base base : knownBases) {
            baseSet(newBases, base.baseLocation()).add(makeNonSpriteBase(base));

            findSpriteBase(spriteFinder, base).ifPresent(
                    (pair) -> baseSet(newBases, pair.getFirst().atlas()).add(pair.getSecond())
            );
        }

        return new BaseCollection(newBases);
    }

    /**
     * Retrieves a set of bases at the given key of the map, creating an empty set
     * if it does not exist.
     * @param newBases          map of base texture locations to specific bases
     * @param baseLocation      key of the set to retrieve
     * @return a newly-created or existing set of bases at the given key
     */
    private static Collection<MipmappedBase> baseSet(
            Map<ResourceLocation, Collection<MipmappedBase>> newBases,
            ResourceLocation baseLocation
    ) {
        return newBases.computeIfAbsent(baseLocation, (location) -> new HashSet<>());
    }

    /**
     * Creates a base for a non-sprite texture.
     * @param base      base to convert to a base with mipmap
     * @return base with mipmap for a non-sprite texture
     */
    private static MipmappedBase makeNonSpriteBase(Base base) {
        return new MipmappedBase(
                base.uploadPoint(),

                // No way to determine mipmap level for an arbitrary texture, so assume it is the default mipmap level
                EventDrivenTexture.SELF_MIPMAP_LEVEL

        );
    }

    /**
     * Finds a {@link MipmappedBase} if the given {@link Base} is associated with a sprite.
     * @param spriteFinder      finds sprites stitched to atlases
     * @param base              base to find
     * @return if the base is associated with a sprite, the sprite and the {@link MipmappedBase}
     */
    private static Optional<Pair<Sprite, MipmappedBase>> findSpriteBase(SpriteFinder spriteFinder, Base base) {
        ResourceLocation baseLocation = base.baseLocation();

        Optional<Sprite> spriteOptional = spriteFinder.findSprite(baseLocation);
        if (spriteOptional.isEmpty()) {
            return Optional.empty();
        }

        Sprite sprite = spriteOptional.get();
        long uploadPointInSprite = Point.pack(
                Point.x(base.uploadPoint()) + Point.x(sprite.uploadPoint()),
                Point.y(base.uploadPoint()) + Point.y(sprite.uploadPoint())
        );

        return Optional.of(
                Pair.of(
                        sprite,
                        new MipmappedBase(
                                uploadPointInSprite,
                                sprite.mipmapLevel()
                        )
                )
        );
    }

    /**
     * Gets the full paths of all unique bases stored in the collection.
     * @return names of all unique bases stored in the collection
     */
    public Collection<ResourceLocation> baseNames() {
        return BASES.keySet();
    }

    /**
     * Retrieves all bases within a particular base texture.
     * @param baseName      full path of the base texture
     * @return all bases associated with the texture, if any
     */
    public Collection<MipmappedBase> baseData(ResourceLocation baseName) {
        requireNonNull(baseName, "Base name cannot be null");
        return BASES.getOrDefault(baseName, ImmutableSet.of());
    }

    /**
     * Gets the maximum mipmap level that the texture needs to support to upload to any of its bases.
     * @return maximum mipmap level of all bases
     */
    public int maxMipmap() {
        return MAX_MIPMAP;
    }

    /**
     * Creates a new base collection.
     * @param bases     bases to put in the collection by base name
     */
    private BaseCollection(Map<ResourceLocation, Collection<MipmappedBase>> bases) {
        Map<ResourceLocation, Collection<MipmappedBase>> basesCopy = new HashMap<>(bases);
        basesCopy.replaceAll((location, baseData) -> ImmutableList.copyOf(baseData));
        BASES = ImmutableMap.copyOf(basesCopy);
        MAX_MIPMAP = BASES.values().stream()
                .flatMap(Collection::stream)
                .map(MipmappedBase::mipmap)
                .max(Integer::compareTo)
                .orElse(0);
    }

    /**
     * Represents a single base within a larger base texture.
     * @author soir20
     */
    public static final class MipmappedBase {
        private final long UPLOAD_POINT;
        private final int MIPMAP;

        /**
         * Gets the coordinate in the base texture at which the top-left corner of the dependency
         * will be uploaded.
         * @return coordinate in the base texture at which the top-left corner of the dependency
         *         will be uploaded.
         */
        public long uploadPoint() {
            return UPLOAD_POINT;
        }

        /**
         * Gets the mipmap level of this base.
         * @return mipmap level of this base
         */
        public int mipmap() {
            return MIPMAP;
        }

        @Override
        public int hashCode() {
            return Objects.hash(UPLOAD_POINT, MIPMAP);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof MipmappedBase otherBase)) {
                return false;
            }

            return UPLOAD_POINT == otherBase.UPLOAD_POINT && MIPMAP == otherBase.MIPMAP;
        }

        /**
         * Creates a new base.
         * @param uploadPoint       coordinate in the base texture at which the top-left corner of the dependency
         *                          will be uploaded.
         * @param mipmap            mipmap level of the base
         */
        private MipmappedBase(long uploadPoint, int mipmap) {
            UPLOAD_POINT = uploadPoint;
            MIPMAP = mipmap;
        }

    }

}
