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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import io.github.moremcmeta.moremcmeta.impl.client.texture.Atlas;
import io.github.moremcmeta.moremcmeta.impl.client.texture.Sprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link TextureAtlas} to be a {@link Atlas}.
 * @author soir20
 */
public final class AtlasAdapter implements Atlas {
    private static final Map<ResourceLocation, Map<ResourceLocation, Set<ResourceLocation>>> SPRITE_NAME_MAPPINGS = new ConcurrentHashMap<>();
    private final ResourceLocation ATLAS_LOCATION;
    private final TextureAtlas ATLAS;
    private final ToIntFunction<TextureAtlasSprite> MIPMAP_LEVEL_GETTER;

    /**
     * Adds a name mapping from the full path of a source texture to a sprite ID.
     * @param atlas         full path of the atlas
     * @param fullPath      full path of the source texture
     * @param spriteName    ID of the sprite
     */
    public static void addNameMapping(ResourceLocation atlas, ResourceLocation fullPath, ResourceLocation spriteName) {
        requireNonNull(atlas, "Atlas location cannot be null");
        requireNonNull(fullPath, "Full path cannot be null");
        requireNonNull(spriteName, "Sprite name cannot be null");
        SPRITE_NAME_MAPPINGS.computeIfAbsent(atlas, (key) -> new ConcurrentHashMap<>())
                .computeIfAbsent(fullPath, (key) -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(spriteName);
    }

    /**
     * Removes sprite IDs matching the given predicate.
     * @param atlas         full path of the atlas
     * @param filter        predicate that returns true if the sprite ID should be removed
     */
    public static void removeNameMappings(ResourceLocation atlas, Predicate<ResourceLocation> filter) {
        requireNonNull(atlas, "Atlas location cannot be null");
        requireNonNull(filter, "Filter cannot be null");
        SPRITE_NAME_MAPPINGS.computeIfAbsent(atlas, (key) -> new ConcurrentHashMap<>())
                .forEach((fullPath, spriteNames) -> spriteNames.removeIf(filter));
    }

    /**
     * Removes name mappings for all atlases.
     */
    public static void clearNameMappings() {
        SPRITE_NAME_MAPPINGS.clear();
    }

    /**
     * Creates a new adapter for an atlas at the given location. If no texture exists at the
     * location or the texture there is not an atlas, this adapter will simply act as an
     * empty atlas and provide no sprites.
     * @param location              the location to look for an atlas
     * @param mipmapLevelGetter     gets the mipmap level of this atlas from a sprite
     */
    public AtlasAdapter(ResourceLocation location, ToIntFunction<TextureAtlasSprite> mipmapLevelGetter) {
        ATLAS_LOCATION = requireNonNull(location, "Location cannot be null");
        MIPMAP_LEVEL_GETTER = requireNonNull(mipmapLevelGetter, "Mipmap level getter cannot be null");

        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
        if (texture instanceof TextureAtlas) {
            ATLAS = (TextureAtlas) texture;
        } else {
            ATLAS = null;
        }
    }

    @Override
    public List<Sprite> sprite(ResourceLocation location) {
        requireNonNull(location, "Sprite location cannot be null");

        if (ATLAS == null) {
            return ImmutableList.of();
        }

        return SPRITE_NAME_MAPPINGS.getOrDefault(ATLAS_LOCATION, ImmutableMap.of())
                .getOrDefault(location, ImmutableSet.of())
                .stream()
                .flatMap((spriteName) -> {
                    TextureAtlasSprite sprite = ATLAS.getSprite(spriteName);

                    if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
                        return Stream.of();
                    }

                    return Stream.of(
                            (Sprite) new SpriteAdapter(
                                    sprite,
                                    MIPMAP_LEVEL_GETTER.applyAsInt(sprite),
                                    0,
                                    0,
                                    sprite.contents().width(),
                                    sprite.contents().height()
                            )
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Adapts a {@link TextureAtlasSprite} to be a {@link Sprite}.
     * @author soir20
     */
    private static class SpriteAdapter implements Sprite {
        private final TextureAtlasSprite SPRITE;
        private final long UPLOAD_POINT;
        private final int MIPMAP_LEVEL;
        private final int X_OFFSET_LEFT;
        private final int Y_OFFSET_LEFT;
        private final int X_OFFSET_RIGHT;
        private final int Y_OFFSET_RIGHT;

        /**
         * Adapts the given sprite to be a {@link Sprite}.
         * @param sprite        the sprite to adapt
         * @param mipmapLevel   the number of mipmaps for this sprite
         * @param subAreaX      x-coordinate of the top-left corner of the sub-area to upload
         * @param subAreaY      y-coordinate of the top-left corner of the sub-area to upload
         * @param subAreaWidth  width the sub-area to upload
         * @param subAreaHeight height the sub-area to upload
         */
        public SpriteAdapter(TextureAtlasSprite sprite, int mipmapLevel, int subAreaX, int subAreaY,
                             int subAreaWidth, int subAreaHeight) {
            SPRITE = sprite;
            UPLOAD_POINT = findUploadPoint();

            if (mipmapLevel < 0) {
                throw new IllegalArgumentException("Sprite cannot have negative mipmaps");
            }
            MIPMAP_LEVEL = mipmapLevel;

            X_OFFSET_LEFT = subAreaX;
            Y_OFFSET_LEFT = subAreaY;
            X_OFFSET_RIGHT = sprite.contents().width() - subAreaX - subAreaWidth;
            Y_OFFSET_RIGHT = sprite.contents().height() - subAreaY - subAreaHeight;

            if (X_OFFSET_RIGHT < 0 || Y_OFFSET_RIGHT < 0) {
                throw new IllegalArgumentException("Sub area is outside sprite bounds");
            }
        }

        @Override
        public ResourceLocation atlas() {
            return SPRITE.atlasLocation();
        }

        @Override
        public long uploadPoint() {
            return UPLOAD_POINT;
        }

        @Override
        public int mipmapLevel() {
            return MIPMAP_LEVEL;
        }

        @Override
        public int xOffsetLeft() {
            return X_OFFSET_LEFT;
        }

        @Override
        public int yOffsetLeft() {
            return Y_OFFSET_LEFT;
        }

        @Override
        public int xOffsetRight() {
            return X_OFFSET_RIGHT;
        }

        @Override
        public int yOffsetRight() {
            return Y_OFFSET_RIGHT;
        }

        /**
         * Gets a sprite's x and y-coordinates of its top left corner in its texture atlas.
         * @return the x and y-coordinates of the sprite's top left corner
         */
        private long findUploadPoint() {
            return Point.pack(SPRITE.getX(), SPRITE.getY());
        }

    }
}
