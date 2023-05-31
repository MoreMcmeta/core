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

package io.github.moremcmeta.moremcmeta.api.client.texture;

import net.minecraft.resources.ResourceLocation;

import static java.util.Objects.requireNonNull;

/**
 * Provides utility methods for working with sprite names ({@link ResourceLocation}s
 * without the "textures/" prefix and ".png" suffix) and texture paths (locations with
 * the full path and suffix).
 * @author soir20
 * @since 4.0.0
 */
public final class SpriteName {
    private static final String TEX_PATH_PREFIX = "textures/";
    private static final int TEX_PATH_PREFIX_LENGTH = TEX_PATH_PREFIX.length();
    private static final String TEX_PATH_SUFFIX = ".png";
    private static final int TEX_PATH_SUFFIX_LENGTH = TEX_PATH_SUFFIX.length();
    private static final int EMPTY_PATH_LENGTH = TEX_PATH_PREFIX_LENGTH + TEX_PATH_SUFFIX_LENGTH;

    /**
     * Converts a texture path to a sprite name. If the provided texture path cannot be converted
     * to a sprite name (because it does not start with "textures/" and end with ".png"), a
     * {@link ResourceLocation} equal to the provided texture path will be returned. This method
     * does not throw an exception when the parameter is an invalid texture path because we often
     * want to use the bad path to make the game show a missing texture, rather than causing
     * the game to crash from an exception.
     * @param texturePath       texture path to convert to a sprite name
     * @return sprite name for the texture at the provided path, or a location equal to the
     *         provided path if it cannot be converted
     */
    public static ResourceLocation fromTexturePath(ResourceLocation texturePath) {
        requireNonNull(texturePath, "Texture path cannot be null");

        String path = texturePath.getPath();
        if (path.startsWith(TEX_PATH_PREFIX) && path.endsWith(TEX_PATH_SUFFIX) && path.length() > EMPTY_PATH_LENGTH) {
            path = path.substring(TEX_PATH_PREFIX_LENGTH, path.length() - TEX_PATH_SUFFIX_LENGTH);
        }

        return new ResourceLocation(texturePath.getNamespace(), path);
    }

    /**
     * Converts a sprite name to a texture path.  If the provided sprite name cannot be converted
     * to a texture path (because it starts with "textures/" or ends with ".png"), a
     * {@link ResourceLocation} equal to the provided sprite name will be returned. This method
     * does not throw an exception when the parameter is an invalid sprite name because we often
     * want to use the bad path to make the game show a missing texture, rather than causing
     * the game to crash from an exception.
     * @param spriteName        sprite name to convert to a texture path
     * @return texture path for the texture at the provided sprite name, or a location equal to the
     *         provided sprite name if it cannot be converted
     */
    public static ResourceLocation toTexturePath(ResourceLocation spriteName) {
        requireNonNull(spriteName, "Sprite name cannot be null");

        String path = spriteName.getPath();
        if (!isSpriteName(spriteName)) {
            return spriteName;
        }

        return new ResourceLocation(
                spriteName.getNamespace(),
                String.format("%s%s%s", TEX_PATH_PREFIX, path, TEX_PATH_SUFFIX)
        );
    }

    /**
     * Checks if a resource location represents a valid sprite name. This method returns true when
     * the conversion in {@link SpriteName#toTexturePath(ResourceLocation)} will succeed.
     * @param location       resource location that might be a sprite name
     * @return true if the location is a valid sprite name
     */
    public static boolean isSpriteName(ResourceLocation location) {
        requireNonNull(location, "Location cannot be null");
        String path = location.getPath();
        return !path.startsWith(TEX_PATH_PREFIX) && !path.endsWith(TEX_PATH_SUFFIX) && path.length() > 0;
    }

    /**
     * Prevents this class from being constructed.
     */
    private SpriteName() {}

}
