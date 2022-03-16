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
import com.mojang.blaze3d.platform.NativeImage;
import io.github.soir20.moremcmeta.impl.client.adapter.ChangingPointsAdapter;
import io.github.soir20.moremcmeta.impl.client.adapter.NativeImageAdapter;
import io.github.soir20.moremcmeta.impl.client.animation.AnimationFrameManager;
import io.github.soir20.moremcmeta.impl.client.animation.WobbleFunction;
import io.github.soir20.moremcmeta.impl.client.resource.ModAnimationMetadataSection;
import io.github.soir20.moremcmeta.impl.client.texture.AnimationComponent;
import io.github.soir20.moremcmeta.impl.client.texture.CleanupComponent;
import io.github.soir20.moremcmeta.impl.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImage;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImageFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Assembles texture data into a texture builder.
 * @author soir20
 */
public class TextureDataAssembler {
    private static final int TICKS_PER_MC_DAY = 24000;
    private final ChangingPointsAdapter POINT_READER;
    private final WobbleFunction WOBBLE_FUNCTION;

    /**
     * Creates a new texture data assembler.
     */
    public TextureDataAssembler() {
        POINT_READER = new ChangingPointsAdapter();
        WOBBLE_FUNCTION = new WobbleFunction();
    }

    /**
     * Combines the texture image and metadata into a {@link EventDrivenTexture.Builder} that
     * minimally needs an upload component. The image is guaranteed to be set in the builder.
     * @param data          texture data to assemble
     * @return texture data assembled as a texture builder
     */
    public EventDrivenTexture.Builder assemble(TextureData<NativeImageAdapter> data) {
        requireNonNull(data, "Data cannot be null");

        Minecraft minecraft = Minecraft.getInstance();
        final int MAX_MIPMAP = minecraft.options.mipmapLevels;

        NativeImage original = data.getImage().getImage();

        /* Use defaults if no metadata was read.
           The metadata parser can set these to null even if there was no error. */
        AnimationMetadataSection animationMetadata = data.getMetadata(AnimationMetadataSection.class)
                .orElse(AnimationMetadataSection.EMPTY);
        ModAnimationMetadataSection modAnimationMetadata = data.getMetadata(ModAnimationMetadataSection.class)
                .orElse(ModAnimationMetadataSection.EMPTY);

        Optional<TextureMetadataSection> textureMetadata = data.getMetadata(TextureMetadataSection.class);
        boolean blur = textureMetadata.map(TextureMetadataSection::isBlur).orElse(false);
        boolean clamp = textureMetadata.map(TextureMetadataSection::isClamp).orElse(false);

        // Create frames
        List<NativeImage> mipmaps = Arrays.asList(MipmapGenerator.generateMipLevels(original, MAX_MIPMAP));
        CloseableImageFrame.SharedMipmapLevel sharedMipmapLevel = new CloseableImageFrame.SharedMipmapLevel(MAX_MIPMAP);
        ImmutableList<CloseableImageFrame> frames = getFrames(mipmaps, blur, clamp, sharedMipmapLevel, animationMetadata);

        CloseableImageFrame firstFrame = frames.get(0);
        int frameWidth = firstFrame.getWidth();
        int frameHeight = firstFrame.getHeight();

        List<CloseableImage.VisibleArea> visibleAreas = getVisibleAreas(firstFrame);

        // Frame manager
        ImmutableList<NativeImageAdapter> interpolatedMipmaps;
        AnimationFrameManager<CloseableImageFrame> frameManager;
        if (animationMetadata.isInterpolatedFrames()) {
             interpolatedMipmaps = getInterpolationMipmaps(mipmaps, frameWidth, frameHeight, blur, clamp, visibleAreas);
            CloseableImageFrame.Interpolator interpolator = new CloseableImageFrame.Interpolator(interpolatedMipmaps);

            frameManager = new AnimationFrameManager<>(frames, CloseableImageFrame::getFrameTime, interpolator);
        } else {
            interpolatedMipmaps = ImmutableList.of();
            frameManager = new AnimationFrameManager<>(frames, CloseableImageFrame::getFrameTime);
        }

        // Resource cleanup
        Runnable closeMipmaps = () -> {

            // The images are shared between all frames
            for (int level = 0; level <= firstFrame.getMipmapLevel(); level++) {
                firstFrame.getImage(level).close();
            }

            interpolatedMipmaps.forEach(NativeImageAdapter::close);
        };

        // Time retrieval
        Supplier<Optional<Long>> timeGetter = () -> {
            if (minecraft.level == null) {
                return Optional.empty();
            }

            ClientLevel level = minecraft.level;
            long time = WOBBLE_FUNCTION.calculate(level.dayTime(), level.getGameTime(), level.dimensionType().natural());
            return Optional.of(time);
        };

        // Add components
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(frameManager.getCurrentFrame())
                .add(new CleanupComponent(closeMipmaps));

        if (modAnimationMetadata.isDaytimeSynced()) {
            builder.add(new AnimationComponent(TICKS_PER_MC_DAY, timeGetter, frameManager));
        } else {
            builder.add(new AnimationComponent(frameManager));
        }

        return builder;
    }

    /**
     * Gets all frames from the generated mipmaps and animation metadata.
     * @param mipmaps               mipmaps of the full texture image (with all frames)
     * @param blur                  whether to blur the texture
     * @param clamp                 whether to clamp the texture
     * @param sharedMipmapLevel     mipmap level shared between all frames
     * @param animationMetadata     animation metadata for this texture
     * @return the frames based on the texture image in chronological order
     */
    private ImmutableList<CloseableImageFrame> getFrames(List<NativeImage> mipmaps, boolean blur, boolean clamp,
                                                         CloseableImageFrame.SharedMipmapLevel sharedMipmapLevel,
                                                         AnimationMetadataSection animationMetadata) {
        int mipmap = mipmaps.size() - 1;

        List<CloseableImage.VisibleArea> visibleAreas = new ArrayList<>();
        FrameReader<CloseableImageFrame> frameReader = new FrameReader<>((frameData) -> {

            /* The immutable list collector was marked as beta for a while,
               and the marking was removed in a later version. */
            @SuppressWarnings("UnstableApiUsage")
            ImmutableList<CloseableImage> wrappedMipmaps = IntStream.rangeClosed(0, mipmap).mapToObj((level) -> {
                int width = frameData.getWidth() >> level;
                int height = frameData.getHeight() >> level;

                // Finding the visible areas is slow, so cache the results
                if (visibleAreas.isEmpty()) {
                    visibleAreas.addAll(POINT_READER.read(mipmaps.get(level), width, height, mipmap));
                }

                return new NativeImageAdapter(
                        mipmaps.get(level),
                        frameData.getXOffset() >> level, frameData.getYOffset() >> level,
                        width, height,
                        level, blur, clamp, false,
                        visibleAreas.get(level)
                );
            }).collect(ImmutableList.toImmutableList());

            return new CloseableImageFrame(frameData, wrappedMipmaps, sharedMipmapLevel);
        });

        return frameReader.read(mipmaps.get(0).getWidth(), mipmaps.get(0).getHeight(), animationMetadata);
    }

    /**
     * Gets the visible areas for all frames from a frame (since they should all have the same areas).
     * @param frame        frame to retrieve the visible areas from
     * @return the visible areas ordered by increasing mipmap level, starting at 0
     */
    private List<CloseableImage.VisibleArea> getVisibleAreas(CloseableImageFrame frame) {
        return IntStream.rangeClosed(0, frame.getMipmapLevel()).mapToObj(
                (level) -> frame.getImage(level).getVisibleArea()
        ).toList();
    }

    /**
     * Creates mipmapped images for interpolation.
     * @param originals             the original mipmaps to copy from
     * @param frameWidth            the width of a single frame
     * @param frameHeight           the height of a single frame
     * @param blur                  whether the images are blurred
     * @param clamp                 whether the images are clamped
     * @param visibleAreas          visible areas in ascending order of mipmap level
     * @return the adapters for the interpolation images
     */
    private ImmutableList<NativeImageAdapter> getInterpolationMipmaps(List<NativeImage> originals, int frameWidth,
                                                                      int frameHeight, boolean blur, boolean clamp,
                                                                      List<CloseableImage.VisibleArea> visibleAreas) {
        ImmutableList.Builder<NativeImageAdapter> images = new ImmutableList.Builder<>();

        for (int level = 0; level < originals.size(); level++) {
            int mipmappedWidth = frameWidth >> level;
            int mipmappedHeight = frameHeight >> level;

            NativeImage original = originals.get(level);

            NativeImage mipmappedImage = new NativeImage(mipmappedWidth, mipmappedHeight, true);
            copyTopLeftRect(mipmappedWidth, mipmappedHeight, original, mipmappedImage);

            NativeImageAdapter adapter = new NativeImageAdapter(
                    mipmappedImage,
                    0, 0,
                    mipmappedWidth, mipmappedHeight,
                    level,
                    blur, clamp, false,
                    visibleAreas.get(level)
            );
            images.add(adapter);
        }

        return images.build();
    }

    /**
     * Copies a rectangle in the top left from one image to another.
     * @param width     width of the rectangle to copy
     * @param height    height of the rectangle to copy
     * @param from      image to copy from (unchanged)
     * @param to        image to copy to (changed)
     */
    private void copyTopLeftRect(int width, int height, NativeImage from, NativeImage to) {
        for (int xPos = 0; xPos < width; xPos++) {
            for (int yPos = 0; yPos < height; yPos++) {
                to.setPixelRGBA(xPos, yPos, from.getPixelRGBA(xPos, yPos));
            }
        }
    }

}
