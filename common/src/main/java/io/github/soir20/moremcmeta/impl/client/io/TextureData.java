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

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.api.client.metadata.ParsedMetadata;
import io.github.soir20.moremcmeta.api.client.texture.ComponentProvider;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImage;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Container to hold frame size, image, and metadata information.
 * @param <I> texture image type
 * @author soir20
 */
public class TextureData<I extends CloseableImage> {
    private final ParsedMetadata.FrameSize FRAME_SIZE;
    private final boolean BLUR;
    private final boolean CLAMP;
    private final I IMAGE;
    private final List<Pair<ParsedMetadata, ComponentProvider>> PARSED_SECTIONS;

    /**
     * Creates a new texture data container.
     * @param frameSize         size of a frame in the image
     * @param blur              whether to blur the image
     * @param clamp             whether to clamp the image
     * @param image             texture image
     * @param parsedSections    parsed metadata and component providers that will
     *                          process the metadata
     */
    public TextureData(ParsedMetadata.FrameSize frameSize, boolean blur, boolean clamp, I image,
                       List<Pair<ParsedMetadata, ComponentProvider>> parsedSections) {
        if (frameSize.width() > image.getWidth()) {
            throw new IllegalArgumentException("Frame width cannot be larger than image width");
        }

        if (frameSize.height() > image.getHeight()) {
            throw new IllegalArgumentException("Frame height cannot be larger than image height");
        }

        FRAME_SIZE = requireNonNull(frameSize, "Frame size cannot be null");
        BLUR = blur;
        CLAMP = clamp;
        IMAGE = requireNonNull(image, "Image cannot be null");
        PARSED_SECTIONS = requireNonNull(ImmutableList.copyOf(parsedSections), "Parsed sections cannot be null");
    }

    /**
     * Gets the size of a frame in the image.
     * @return the size of a frame in the image
     */
    public ParsedMetadata.FrameSize frameSize() {
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
     * Gets the image for this texture, which includes all non-generated frames.
     * @return the image for this texture
     */
    public I image() {
        return IMAGE;
    }

    /**
     * Gets parsed metadata and component providers that will process the metadata.
     * @return parsed metadata sections and associated component providers
     */
    public Iterable<Pair<ParsedMetadata, ComponentProvider>> parsedMetadata() {
        return PARSED_SECTIONS;
    }

}
