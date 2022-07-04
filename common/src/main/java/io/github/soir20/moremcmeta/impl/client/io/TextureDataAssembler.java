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
import io.github.soir20.moremcmeta.api.client.texture.ColorTransform;
import io.github.soir20.moremcmeta.api.client.texture.ComponentProvider;
import io.github.soir20.moremcmeta.api.client.texture.CurrentFrameView;
import io.github.soir20.moremcmeta.api.client.texture.FrameGroup;
import io.github.soir20.moremcmeta.api.client.texture.MutableFrameView;
import io.github.soir20.moremcmeta.api.client.texture.TextureComponent;
import io.github.soir20.moremcmeta.api.math.Area;
import io.github.soir20.moremcmeta.impl.client.texture.CleanupComponent;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImage;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImageFrame;
import io.github.soir20.moremcmeta.impl.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.impl.client.texture.FrameGroupImpl;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Assembles texture data into a texture builder.
 * @param <I> image type
 * @author soir20
 */
public class TextureDataAssembler<I extends CloseableImage> {
    private final ImageAllocator ALLOCATOR;
    private final Function<? super I, ? extends List<? extends I>> MIPMAP_GENERATOR;

    /**
     * Creates a new texture assembler.
     * @param allocator         allocator for new images
     * @param mipmapGenerator   generates mipmaps from an original image, the number of which
     */
    public TextureDataAssembler(ImageAllocator allocator,
                                Function<? super I, ? extends List<? extends I>> mipmapGenerator) {
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

        // Create frames
        List<? extends I> mipmaps = MIPMAP_GENERATOR.apply(original);
        ImmutableList<CloseableImageFrame> frames = getFrames(
                mipmaps,
                frameWidth,
                frameHeight
        );
        CloseableImageFrame generatedFrame = createGeneratedFrame(
                mipmaps.size() - 1,
                frameWidth,
                frameHeight,
                blur,
                clamp
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

        for (Pair<ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            ParsedMetadata sectionData = metadata.getFirst();

            assembleComponents(
                    metadata.getSecond(),
                    frames,
                    sectionData
            ).forEach(builder::add);
        }

        return builder;
    }

    /**
     * Gets all frames from the generated mipmaps and animation metadata.
     * @param mipmaps               mipmaps of the full texture image (with all frames)
     * @param frameWidth            width of each frame in the image
     * @param frameHeight           height of each frame in the image
     * @return the frames based on the texture image in chronological order
     */
    private ImmutableList<CloseableImageFrame> getFrames(List<? extends I> mipmaps, int frameWidth,
                                                         int frameHeight) {
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

            return new CloseableImageFrame(frameData, subImageMipmaps);
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
     * @return the adapters for the interpolation images
     */
    private CloseableImageFrame createGeneratedFrame(int maxMipmap, int frameWidth,
                                                     int frameHeight, boolean blur, boolean clamp) {
        ImmutableList.Builder<CloseableImage> images = new ImmutableList.Builder<>();

        for (int level = 0; level <= maxMipmap; level++) {
            int mipmappedWidth = frameWidth >> level;
            int mipmappedHeight = frameHeight >> level;

            CloseableImage image = ALLOCATOR.allocate(mipmappedWidth, mipmappedHeight, level, blur, clamp);
            images.add(image);
        }

        return new CloseableImageFrame(
                new FrameReader.FrameData(frameWidth, frameHeight, 0, 0),
                images.build()
        );
    }

    /**
     * Creates texture components based on the given frames and provider.
     * @param provider      component provider
     * @param frames        predefined frames
     * @param metadata      metadata associated with the transform
     * @return assembled components
     */
    private Iterable<TextureComponent<CurrentFrameView>> assembleComponents(ComponentProvider provider,
                                                                            List<CloseableImageFrame> frames,
                                                                            ParsedMetadata metadata) {
        FrameGroup<MutableFrameViewImpl> mutableFrames = new FrameGroupImpl<>(frames, MutableFrameViewImpl::new);

        Iterable<TextureComponent<CurrentFrameView>> components = provider.assemble(metadata, mutableFrames);

        mutableFrames.forEach(MutableFrameViewImpl::invalidate);

        return components;
    }

    /**
     * {@link MutableFrameView} implementation for a predefined frame.
     * @author soir20
     */
    private static class MutableFrameViewImpl implements MutableFrameView {
        private final CloseableImageFrame FRAME;
        private final int INDEX;

        private boolean valid;

        /**
         * Creates a new view for a mutable predefined frame.
         * @param frame         the original frame
         * @param index         index of the frame among all frames
         */
        public MutableFrameViewImpl(CloseableImageFrame frame, int index) {
            FRAME = frame;
            INDEX = index;
            valid = true;
        }

        /**
         * Gets the width of the frame.
         * @return width of the frame
         */
        @Override
        public int width() {
            checkValid();
            return FRAME.width();
        }

        /**
         * Gets the height of the frame.
         * @return height of the frame
         */
        @Override
        public int height() {
            checkValid();
            return FRAME.height();
        }

        /**
         * Gets the index of the frame. Always exists since it
         * is a predefined frame by definition.
         * @return index of the frame
         */
        @Override
        public Optional<Integer> index() {
            checkValid();
            return Optional.of(INDEX);
        }

        /**
         * Modifies this frame with the given function over the given apply area.
         * @param transform     transformation to apply to this frame
         * @param applyArea     area to apply the transformation to
         * @param dependencies  the points whose current colors this transformation depends on
         */
        @Override
        public void transform(ColorTransform transform, Area applyArea, Area dependencies) {
            checkValid();
            FRAME.applyTransform(transform, applyArea, dependencies);
        }

        /**
         * Makes this frame view invalid for further use. After this method is called, all future
         * calls to other methods will throw an {@link IllegalFrameReference} exception. However,
         * this method is idempotent.
         */
        public void invalidate() {
            valid = false;
        }

        /**
         * Checks that this frame view is currently valid and throws an exception if not.
         * @throws IllegalFrameReference if this view is no longer valid
         */
        public void checkValid() throws IllegalFrameReference {
            if (!valid) {
                throw new IllegalFrameReference();
            }
        }

    }

}
