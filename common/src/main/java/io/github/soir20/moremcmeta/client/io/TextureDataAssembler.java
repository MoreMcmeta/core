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

package io.github.soir20.moremcmeta.client.io;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.soir20.moremcmeta.client.adapter.ChangingPointsAdapter;
import io.github.soir20.moremcmeta.client.adapter.NativeImageAdapter;
import io.github.soir20.moremcmeta.client.animation.AnimationFrameManager;
import io.github.soir20.moremcmeta.client.animation.WobbleFunction;
import io.github.soir20.moremcmeta.client.resource.ModAnimationMetadataSection;
import io.github.soir20.moremcmeta.client.texture.AnimationComponent;
import io.github.soir20.moremcmeta.client.texture.CleanupComponent;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.RGBAImage;
import io.github.soir20.moremcmeta.client.texture.RGBAImageFrame;
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
import java.util.stream.Collectors;
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
        final int MIPMAP = minecraft.options.mipmapLevels;

        NativeImage image = data.getImage().getImage();
        List<NativeImage> mipmaps = new ArrayList<>(Arrays.asList(MipmapGenerator.generateMipLevels(image, MIPMAP)));

        /* Use defaults if no metadata was read.
           The metadata parser can set these to null even if there was no error. */
        AnimationMetadataSection animationMetadata = data.getMetadata(AnimationMetadataSection.class)
                .orElse(AnimationMetadataSection.EMPTY);
        ModAnimationMetadataSection modAnimationMetadata = data.getMetadata(ModAnimationMetadataSection.class)
                .orElse(ModAnimationMetadataSection.EMPTY);

        Optional<TextureMetadataSection> textureMetadata = data.getMetadata(TextureMetadataSection.class);
        boolean blur = textureMetadata.map(TextureMetadataSection::isBlur).orElse(false);
        boolean clamp = textureMetadata.map(TextureMetadataSection::isClamp).orElse(false);

        // Frames
        List<RGBAImage.VisibleArea> visibleAreas = new ArrayList<>();
        FrameReader<RGBAImageFrame> frameReader = new FrameReader<>(frameData -> {

            /* The immutable list collector was marked as beta for a while,
               and the marking was removed in a later version. */
            @SuppressWarnings("UnstableApiUsage")
            ImmutableList<RGBAImage> wrappedMipmaps = IntStream.range(0, mipmaps.size()).mapToObj((level) -> {
                int width = frameData.getWidth() >> level;
                int height = frameData.getHeight() >> level;

                // Finding the visible areas is slow, so we want to cache the results
                if (visibleAreas.isEmpty()) {
                    visibleAreas.addAll(POINT_READER.read(mipmaps.get(level), width, height, MIPMAP));
                }

                return new NativeImageAdapter(
                        mipmaps.get(level),
                        frameData.getXOffset() >> level, frameData.getYOffset() >> level,
                        width, height,
                        level, blur, clamp, false,
                        visibleAreas.get(level)
                );
            }).collect(ImmutableList.toImmutableList());

            return new RGBAImageFrame(frameData, wrappedMipmaps);
        });

        ImmutableList<RGBAImageFrame> frames = frameReader.read(image.getWidth(), image.getHeight(), animationMetadata);
        RGBAImageFrame firstFrame = frames.get(0);
        int frameWidth = firstFrame.getWidth();
        int frameHeight = firstFrame.getHeight();

        // Frame management
        AnimationFrameManager<RGBAImageFrame> frameManager;
        if (animationMetadata.isInterpolatedFrames()) {
            ImmutableList<NativeImageAdapter> interpolatedMipmaps = getInterpolationMipmaps(
                    mipmaps, frameWidth, frameHeight, blur, clamp, visibleAreas
            );
            mipmaps.addAll(interpolatedMipmaps.stream().map(NativeImageAdapter::getImage).collect(Collectors.toList()));
            RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(interpolatedMipmaps);

            frameManager = new AnimationFrameManager<>(frames, RGBAImageFrame::getFrameTime, interpolator);
        } else {
            frameManager = new AnimationFrameManager<>(frames, RGBAImageFrame::getFrameTime);
        }

        // Resource cleanup
        Runnable closeMipmaps = () -> mipmaps.forEach(NativeImage::close);

        // Time retrieval
        Supplier<Optional<Long>> timeGetter = () -> {
            if (minecraft.level == null) {
                return Optional.empty();
            }

            ClientLevel level = minecraft.level;
            long time = WOBBLE_FUNCTION.calculate(level.dayTime(), level.getGameTime(), level.dimensionType().natural());
            return Optional.of(time);
        };

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
     * Creates mipmapped images for interpolation.
     * @param originals         the original mipmaps to copy from
     * @param frameWidth        the width of a single frame
     * @param frameHeight       the height of a single frame
     * @param blur              whether the images are blurred
     * @param clamp             whether the images are clamped
     * @param visibleAreas      visible areas in ascending order of mipmap level
     * @return the adapters for the interpolation images
     */
    private ImmutableList<NativeImageAdapter> getInterpolationMipmaps(List<NativeImage> originals, int frameWidth,
                                                                      int frameHeight, boolean blur, boolean clamp,
                                                                      List<RGBAImage.VisibleArea> visibleAreas) {
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
