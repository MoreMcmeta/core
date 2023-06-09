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

import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaTexturePlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentBuilder;
import io.github.moremcmeta.moremcmeta.impl.client.texture.CloseableImage;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Reads minimum texture data from byte streams. The {@link TextureDataAssembler} takes
 * this data and puts it together.
 * @param <I> image type
 * @author soir20
 */
public final class TextureDataReader<I extends CloseableImage> implements TextureReader<TextureData<I>> {
    private final Map<String, MoreMcmetaTexturePlugin> SECTION_TO_PLUGIN;
    private final ImageReader<? extends I> IMAGE_READER;
    private final BlurClampApplier<? super I, ? extends I> BLUR_CLAMP_APPLIER;

    /**
     * Creates a new reader that is aware of the given plugins, if any.
     * @param plugins           plugins that the reader should use to analyze texture data
     * @param imageReader       reads the image from the {@link InputStream} of texture data
     * @param blurClampApplier  applies blur and clamp to an image
     */
    public TextureDataReader(Iterable<? extends MoreMcmetaTexturePlugin> plugins,
                             ImageReader<? extends I> imageReader,
                             BlurClampApplier<? super I, ? extends I> blurClampApplier) {
        requireNonNull(plugins, "Plugins cannot be null");
        SECTION_TO_PLUGIN = new HashMap<>();
        plugins.forEach((plugin) -> SECTION_TO_PLUGIN.put(plugin.sectionName(), plugin));

        IMAGE_READER = requireNonNull(imageReader, "Image reader cannot be null");
        BLUR_CLAMP_APPLIER = requireNonNull(blurClampApplier, "Blur-clamp applier cannot be null");
    }

    @Override
    public TextureData<I> read(InputStream textureStream, MetadataView metadata)
            throws IOException, InvalidMetadataException {

        requireNonNull(textureStream, "Texture stream cannot be null");
        requireNonNull(metadata, "Metadata cannot be null");

        I image = IMAGE_READER.read(textureStream);
        requireNonNull(image, "Image read cannot be null. Throw an IOException instead.");

        List<Triple<String, AnalyzedMetadata, ComponentBuilder>> analyzedSections = new ArrayList<>();
        Optional<Integer> frameWidthOptional = Optional.empty();
        Optional<Integer> frameHeightOptional = Optional.empty();
        Optional<Boolean> blurOptional = Optional.empty();
        Optional<Boolean> clampOptional = Optional.empty();

        for (String section : metadata.keys()) {
            MoreMcmetaTexturePlugin plugin = SECTION_TO_PLUGIN.get(section);
            Optional<MetadataView> sectionView = metadata.subView(section);
            if (plugin == null || sectionView.isEmpty()) {
                continue;
            }

            AnalyzedMetadata sectionData;
            try {
                sectionData = plugin.analyzer().analyze(sectionView.get(), image.width(), image.height());
            } catch (InvalidMetadataException err) {
                throw new InvalidMetadataException(String.format("%s marked metadata as invalid: %s",
                        plugin.id(), err.getMessage()), err);
            }

            requireNonNull(sectionData, "Plugin " + plugin.id() + " returned null for analyzed metadata");
            analyzedSections.add(Triple.of(plugin.id(), sectionData, plugin.componentBuilder()));

            frameWidthOptional = unwrapIfCompatible(frameWidthOptional, sectionData.frameWidth(), "frame width");
            frameHeightOptional = unwrapIfCompatible(frameHeightOptional, sectionData.frameHeight(), "frame width");
            blurOptional = unwrapIfCompatible(blurOptional, sectionData.blur(), "blur");
            clampOptional = unwrapIfCompatible(clampOptional, sectionData.clamp(), "clamp");
        }

        boolean blur = blurOptional.orElse(false);
        boolean clamp = clampOptional.orElse(false);
        image = BLUR_CLAMP_APPLIER.apply(image, blur, clamp);
        requireNonNull(image, "Blurred and clamped image cannot be null");

        int frameWidth = frameWidthOptional.orElse(image.width());
        int frameHeight = frameHeightOptional.orElse(image.height());

        // Check for frame size too large
        if (frameWidth > image.width() || frameHeight > image.height()) {
            throw new InvalidMetadataException(String.format(
                    "%sx%s larger than %sx%s image",
                    frameWidth, frameHeight,
                    image.width(), image.height()
            ));
        }

        if (frameWidth <= 0) {
            throw new InvalidMetadataException("Frame width cannot be zero or negative: " + frameWidth);
        }
        if (frameHeight <= 0) {
            throw new InvalidMetadataException("Frame height cannot be zero or negative: " + frameHeight);
        }

        TextureData.FrameSize frameSize = new TextureData.FrameSize(frameWidth, frameHeight);

        return new TextureData<>(
                frameSize,
                blur,
                clamp,
                image,
                analyzedSections
        );
    }

    /**
     * <p>Compares the current value and a possible new value to check if
     * they are compatible. The two values are not compatible if and
     * only if they are not both empty, and they contain different
     * items according to {@link Object#equals(Object)}.</p>
     *
     * <p>If the two values are not compatible, an exception is thrown.
     * Otherwise, the new value is returned if it is present.</p>
     * @param currentVal    current value
     * @param newVal        new value that may be returned
     * @param propName      property name describing what is contained in the values
     * @param <T> type contained by the {@link Optional}s
     * @return the new value if both values are compatible
     * @throws InvalidMetadataException if the values are not compatible
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T> Optional<T> unwrapIfCompatible(Optional<T> currentVal, Optional<T> newVal, String propName)
            throws InvalidMetadataException {
        if (currentVal.isPresent() && newVal.isPresent() && !currentVal.get().equals(newVal.get())) {
            throw new InvalidMetadataException(String.format("%s was given conflicting values by two plugins: %s %s",
                    propName, currentVal.get(), newVal.get()));
        }

        if (newVal.isPresent()) {
            return newVal;
        }

        return currentVal;
    }

}
