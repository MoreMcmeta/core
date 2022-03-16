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

package io.github.soir20.moremcmeta.impl.client.io;

import io.github.soir20.moremcmeta.impl.client.texture.CloseableImage;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Container to hold frame size, image, and metadata information.
 * @param <I> texture image type
 * @author soir20
 */
public class TextureData<I extends CloseableImage> {
    private final int FRAME_WIDTH;
    private final int FRAME_HEIGHT;
    private final I IMAGE;
    private final Map<Class<?>, Object> METADATA;

    /**
     * Creates a new texture data container.
     * @param frameWidth        width of a frame in the animation, or the width of the image if not animated
     * @param frameHeight       height of a frame in the animation, or the height of the image if not animated
     * @param image             texture image
     */
    public TextureData(int frameWidth, int frameHeight, I image) {
        if (frameWidth < 0) {
            throw new IllegalArgumentException("Frame width cannot be negative");
        }

        if (frameWidth > image.getWidth()) {
            throw new IllegalArgumentException("Frame width cannot be larger than image width");
        }

        if (frameHeight < 0) {
            throw new IllegalArgumentException("Frame height cannot be negative");
        }

        if (frameHeight > image.getHeight()) {
            throw new IllegalArgumentException("Frame height cannot be larger than image height");
        }

        FRAME_WIDTH = frameWidth;
        FRAME_HEIGHT = frameHeight;
        IMAGE = requireNonNull(image, "Image cannot be null");
        METADATA = new HashMap<>();
    }

    /**
     * Gets the width of an animation frame in the texture, or the width of the image if not animated.
     * @return frame width
     */
    public int getFrameWidth() {
        return FRAME_WIDTH;
    }

    /**
     * Gets the height of an animation frame in the texture, or the height of the image if not animated.
     * @return frame height
     */
    public int getFrameHeight() {
        return FRAME_HEIGHT;
    }

    /**
     * Gets the image for this texture, which includes all non-generated frames.
     * @return the image for this texture
     */
    public I getImage() {
        return IMAGE;
    }

    /**
     * Add metadata associated with the texture. If metadata already exists for a given class, that metadata
     * is replaced.
     * @param sectionClass      class of the metadata section
     * @param section           an instance of the metadata section. If null, this method is a no-op,
     *                          and any existing metadata remains.
     * @param <T> type of metadata
     */
    public <T> void addMetadataSection(Class<T> sectionClass, @Nullable T section) {
        requireNonNull(sectionClass, "Section class cannot be null");

        if (section != null) {
            METADATA.put(sectionClass, section);
        }
    }

    /**
     * Gets the metadata associated with the given metadata class, if any.
     * @param sectionClass      class of metadata section
     * @param <T> type of metadata section
     * @return the associated metadata section, if present
     */
    public <T> Optional<T> getMetadata(Class<T> sectionClass) {
        requireNonNull(sectionClass, "Section class cannot be null");
        return Optional.ofNullable(sectionClass.cast(METADATA.get(sectionClass)));
    }

}
