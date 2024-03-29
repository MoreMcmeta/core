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

package io.github.moremcmeta.moremcmeta.impl.client.io;

import com.google.common.collect.ImmutableList;
import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.metadata.GuiScaling;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentBuilder;
import io.github.moremcmeta.moremcmeta.api.math.NegativeDimensionException;
import io.github.moremcmeta.moremcmeta.impl.client.texture.CloseableImage;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Container to hold frame size, image, and metadata information.
 * @param <I> texture image type
 * @author soir20
 */
public final class TextureData<I extends CloseableImage> {
    private final FrameSize FRAME_SIZE;
    private final boolean BLUR;
    private final boolean CLAMP;
    private final Optional<GuiScaling> GUI_SCALING;
    private final I IMAGE;
    private final List<Triple<String, AnalyzedMetadata, ComponentBuilder>> ANALYZED_SECTIONS;

    /**
     * Creates a new texture data container.
     * @param frameSize         size of a frame in the image
     * @param blur              whether to blur the image
     * @param clamp             whether to clamp the image
     * @param guiScaling        GUI scaling settings for the texture
     * @param image             texture image
     * @param analyzedSections  analyzed metadata and component builders that will
     *                          process the metadata
     */
    public TextureData(FrameSize frameSize, boolean blur, boolean clamp, Optional<GuiScaling> guiScaling,
                       I image, List<Triple<String, AnalyzedMetadata, ComponentBuilder>> analyzedSections) {
        if (frameSize.width() > image.width()) {
            throw new IllegalArgumentException("Frame width cannot be larger than image width");
        }

        if (frameSize.height() > image.height()) {
            throw new IllegalArgumentException("Frame height cannot be larger than image height");
        }

        FRAME_SIZE = requireNonNull(frameSize, "Frame size cannot be null");
        BLUR = blur;
        CLAMP = clamp;
        GUI_SCALING = requireNonNull(guiScaling, "GUI scaling cannot be null");
        IMAGE = requireNonNull(image, "Image cannot be null");
        ANALYZED_SECTIONS = requireNonNull(ImmutableList.copyOf(analyzedSections), "Analyzed sections cannot be null");
    }

    /**
     * Gets the size of a frame in the image.
     * @return the size of a frame in the image
     */
    public FrameSize frameSize() {
        return FRAME_SIZE;
    }

    /**
     * Gets whether the texture should be clamped.
     * @return whether the texture should be clamped
     */
    public boolean clamp() {
        return CLAMP;
    }

    /**
     * Gets whether the texture should be blurred.
     * @return whether the texture should be blurred
     */
    public boolean blur() {
        return BLUR;
    }

    /**
     * Gets the GUI scaling setting for the texture, if any.
     * @return GUI scaling setting for the texture, if any
     */
    public Optional<GuiScaling> guiScaling() {
        return GUI_SCALING;
    }

    /**
     * Gets the image for this texture, which includes all non-generated frames.
     * @return the image for this texture
     */
    public I image() {
        return IMAGE;
    }

    /**
     * Gets analyzed metadata and its associated plugin names and component builders that
     * will process the metadata.
     * @return analyzed metadata sections and associated component builder
     */
    public List<Triple<String, AnalyzedMetadata, ComponentBuilder>> analyzedMetadata() {
        return ANALYZED_SECTIONS;
    }

    /**
     * Holds the frame width and height as a single object.
     * @author soir20
     */
    public static final class FrameSize {
        private final int WIDTH;
        private final int HEIGHT;

        /**
         * Creates a new object representing a frame size.
         * @param width     width of a frame
         * @param height    height of a frame
         * @throws NegativeDimensionException if the width or the height is negative
         */
        public FrameSize(int width, int height) {
            if (width < 0) {
                throw new NegativeDimensionException(width);
            }

            if (height < 0) {
                throw new NegativeDimensionException(height);
            }

            WIDTH = width;
            HEIGHT = height;
        }

        /**
         * Gets the width of a frame.
         * @return the width of a frame
         */
        public int width() {
            return WIDTH;
        }

        /**
         * Gets the height of a frame.
         * @return the height of a frame
         */
        public int height() {
            return HEIGHT;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof FrameSize otherSize)) {
                return false;
            }

            return width() == otherSize.width() && height() == otherSize.height();
        }

        @Override
        public int hashCode() {
            return 31 * WIDTH + HEIGHT;
        }

    }

}
