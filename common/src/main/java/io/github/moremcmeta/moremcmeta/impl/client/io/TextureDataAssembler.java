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
import io.github.moremcmeta.moremcmeta.api.client.texture.ColorTransform;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentBuilder;
import io.github.moremcmeta.moremcmeta.api.client.texture.CurrentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.IllegalFrameReferenceException;
import io.github.moremcmeta.moremcmeta.api.client.texture.MutableFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import io.github.moremcmeta.moremcmeta.api.math.Area;
import io.github.moremcmeta.moremcmeta.impl.client.texture.CleanupComponent;
import io.github.moremcmeta.moremcmeta.impl.client.texture.CloseableImage;
import io.github.moremcmeta.moremcmeta.impl.client.texture.CloseableImageFrame;
import io.github.moremcmeta.moremcmeta.impl.client.texture.EventDrivenTexture;
import io.github.moremcmeta.moremcmeta.impl.client.texture.FrameGroupImpl;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Assembles texture data into a texture builder.
 * @param <I> image type
 * @author soir20
 */
public final class TextureDataAssembler<I extends CloseableImage> {

    /** An upload component is added later, so an additional layer is needed. */
    public final static int EXTERNAL_DEFAULT_COMPONENTS = 1;

    private final static int INTERNAL_DEFAULT_COMPONENTS = 1;

    private final ImageAllocator ALLOCATOR;
    private final BiFunction<? super I, Integer, ? extends List<? extends I>> MIPMAP_GENERATOR;

    /**
     * Creates a new texture assembler.
     * @param allocator         allocator for new images
     * @param mipmapGenerator   generates mipmaps from an original image, the number of which
     */
    public TextureDataAssembler(ImageAllocator allocator,
                                BiFunction<? super I, Integer, ? extends List<? extends I>> mipmapGenerator) {
        ALLOCATOR = requireNonNull(allocator, "Allocator cannot be null");
        MIPMAP_GENERATOR = requireNonNull(mipmapGenerator, "Mipmap generator cannot be null");
    }

    /**
     * Combines the texture image and metadata into a {@link EventDrivenTexture.Builder} that
     * minimally needs an upload component.
     * @param data          texture data to assemble
     * @return texture data assembled as a texture builder
     */
    public EventDrivenTexture.Builder assemble(TextureData<? extends I> data) {
        requireNonNull(data, "Data cannot be null");

        I original = data.image();
        int frameWidth = data.frameSize().width();
        int frameHeight = data.frameSize().height();
        boolean blur = data.blur();
        boolean clamp = data.clamp();

        int maxMipmapX = Mth.log2(frameWidth);
        int maxMipmapY = Mth.log2(frameHeight);

        // Create frames
        int layers = data.analyzedMetadata().size() + EXTERNAL_DEFAULT_COMPONENTS + INTERNAL_DEFAULT_COMPONENTS;
        List<? extends I> mipmaps = MIPMAP_GENERATOR.apply(original, Math.min(maxMipmapX, maxMipmapY));
        ImmutableList<CloseableImageFrame> frames = readFrames(
                mipmaps,
                frameWidth,
                frameHeight,
                layers
        );
        CloseableImageFrame generatedFrame = createGeneratedFrame(
                mipmaps.size() - 1,
                frameWidth,
                frameHeight,
                blur,
                clamp,
                layers
        );

        // Resource cleanup
        Runnable closeMipmaps = () -> {
            frames.forEach(CloseableImageFrame::close);
            generatedFrame.close();
        };

        // Add components
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(frames)
                .setGeneratedFrame(generatedFrame)
                .add(new CleanupComponent(closeMipmaps));

        for (int index = 0; index < data.analyzedMetadata().size(); index++) {
            Triple<String, AnalyzedMetadata, ComponentBuilder> metadata = data.analyzedMetadata().get(index);
            AnalyzedMetadata sectionData = metadata.getMiddle();

            builder.add(
                    buildComponent(
                            metadata.getRight(),
                            frames,
                            sectionData,
                            index
                    )
            );
        }

        return builder;
    }

    /**
     * Gets all frames from the generated mipmaps and animation metadata.
     * @param mipmaps               mipmaps of the full texture image (with all frames)
     * @param frameWidth            width of each frame in the image
     * @param frameHeight           height of each frame in the image
     * @param layers                number of layers in the image
     * @return the frames based on the texture image in chronological order
     */
    private ImmutableList<CloseableImageFrame> readFrames(List<? extends I> mipmaps, int frameWidth,
                                                          int frameHeight, int layers) {
        int mipmap = mipmaps.size() - 1;

        FrameReader<CloseableImageFrame> frameReader = new FrameReader<>((frameData) -> {

            /* The immutable list collector was marked as beta for a while,
               and the marking was removed in a later version. */
            ImmutableList<CloseableImage> subImageMipmaps = IntStream.rangeClosed(0, mipmap).mapToObj(
                    (level) -> mipmaps.get(level).subImage(
                            frameData.xOffset() >> level,
                            frameData.yOffset() >> level,
                            frameData.width() >> level,
                            frameData.height() >> level
                    )
            ).collect(ImmutableList.toImmutableList());

            return new CloseableImageFrame(frameData, subImageMipmaps, layers);
        });

        return frameReader.read(mipmaps.get(0).width(), mipmaps.get(0).height(), frameWidth, frameHeight);
    }

    /**
     * Creates a frame that will hold generated frames.
     * @param maxMipmap             maximum mipmap level of the original image
     * @param frameWidth            the width of a single frame
     * @param frameHeight           the height of a single frame
     * @param blur                  whether the images are blurred
     * @param clamp                 whether the images are clamped
     * @param layers                number of layers in the image
     * @return the adapters for the interpolation images
     */
    private CloseableImageFrame createGeneratedFrame(int maxMipmap, int frameWidth,
                                                     int frameHeight, boolean blur, boolean clamp,
                                                     int layers) {
        ImmutableList.Builder<CloseableImage> images = new ImmutableList.Builder<>();

        for (int level = 0; level <= maxMipmap; level++) {
            int mipmappedWidth = frameWidth >> level;
            int mipmappedHeight = frameHeight >> level;

            CloseableImage image = ALLOCATOR.allocate(mipmappedWidth, mipmappedHeight, level, blur, clamp);
            images.add(image);
        }

        return new CloseableImageFrame(
                new FrameReader.FrameData(frameWidth, frameHeight, 0, 0),
                images.build(),
                layers
        );
    }

    /**
     * Creates a texture component based on the given frames and builder.
     * @param builder       component builder
     * @param frames        predefined frames
     * @param metadata      metadata associated with the transform
     * @param layer         index of layer to apply transformations to
     * @return assembled component
     */
    private TextureComponent<? super CurrentFrameView> buildComponent(ComponentBuilder builder,
                                                                      List<CloseableImageFrame> frames,
                                                                      AnalyzedMetadata metadata, int layer) {
        FrameGroup<MutableFrameViewImpl> mutableFrames = new FrameGroupImpl<>(
                frames,
                (frame, index) -> new MutableFrameViewImpl(frame, layer)
        );

        TextureComponent<? super CurrentFrameView> component = builder.build(
                metadata,
                mutableFrames
        );

        mutableFrames.forEach(MutableFrameViewImpl::invalidate);

        return component;
    }

    /**
     * {@link MutableFrameView} implementation for a predefined frame.
     * @author soir20
     */
    private static class MutableFrameViewImpl implements MutableFrameView {
        private final CloseableImageFrame FRAME;
        private final int LAYER;
        private boolean valid;

        /**
         * Creates a new view for a mutable predefined frame.
         * @param frame         the original frame
         * @param layer         layer in the original frame to apply transformations to
         */
        public MutableFrameViewImpl(CloseableImageFrame frame, int layer) {
            FRAME = frame;
            LAYER = layer;
            valid = true;
        }

        @Override
        public int width() {
            checkValid();
            return FRAME.width();
        }

        @Override
        public int height() {
            checkValid();
            return FRAME.height();
        }

        @Override
        public void transform(ColorTransform transform, Area applyArea) {
            checkValid();
            FRAME.applyTransform(transform, applyArea, LAYER);
        }

        /**
         * Makes this frame view invalid for further use. After this method is called, all future
         * calls to other methods will throw an {@link IllegalFrameReferenceException} exception. However,
         * this method is idempotent.
         */
        public void invalidate() {
            valid = false;
        }

        /**
         * Checks that this frame view is currently valid and throws an exception if not.
         * @throws IllegalFrameReferenceException if this view is no longer valid
         */
        private void checkValid() throws IllegalFrameReferenceException {
            if (!valid) {
                throw new IllegalFrameReferenceException();
            }
        }

    }

}
