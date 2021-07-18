/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.soir20.moremcmeta.client.adapter.ChangingPointsAdapter;
import io.github.soir20.moremcmeta.client.animation.WobbleFunction;
import io.github.soir20.moremcmeta.client.resource.ModAnimationMetadataSection;
import io.github.soir20.moremcmeta.client.texture.AnimationComponent;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.IRGBAImage;
import io.github.soir20.moremcmeta.client.texture.CleanupComponent;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import io.github.soir20.moremcmeta.client.texture.RGBAImageFrame;
import io.github.soir20.moremcmeta.client.adapter.NativeImageAdapter;
import io.github.soir20.moremcmeta.client.animation.AnimationFrameManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleResource;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Reads an {@link EventDrivenTexture} from file data. It is reusable for all
 * animated textures with the same mipmap level. It leaves textures in an pre-built
 * state to allow for the {@link LazyTextureManager}
 * to add components related to texture registration and binding.
 * @author soir20
 */
public class AnimatedTextureReader implements ITextureReader<EventDrivenTexture.Builder> {
    private static final int TICKS_PER_MC_DAY = 24000;
    private final Logger LOGGER;
    private final ChangingPointsAdapter POINT_READER;
    private final WobbleFunction WOBBLE_FUNCTION;

    /**
     * Creates a new reader for animated textures.
     * @param logger        logger for reading-related messages
     */
    public AnimatedTextureReader(Logger logger) {
        LOGGER = requireNonNull(logger, "Logger cannot be null");
        POINT_READER = new ChangingPointsAdapter();
        WOBBLE_FUNCTION = new WobbleFunction();
    }

    /**
     * Reads an {@link EventDrivenTexture}.
     * @param textureStream           input stream with image data
     * @param metadataStream          input stream with texture and animation properties
     * @return  an animated texture based on the provided data
     * @throws IOException  failure reading from either input stream
     */
    public EventDrivenTexture.Builder read(InputStream textureStream, InputStream metadataStream) throws IOException,
            JsonParseException, IllegalArgumentException {

        requireNonNull(textureStream, "Texture input stream cannot be null");
        requireNonNull(metadataStream, "Metadata input stream cannot be null");

        Minecraft minecraft = Minecraft.getInstance();
        final int MIPMAP = minecraft.options.mipmapLevels;

        NativeImage image = NativeImage.read(textureStream);
        LOGGER.debug("Successfully read image from input");

        List<NativeImage> mipmaps = new ArrayList<>(Arrays.asList(MipmapGenerator.generateMipLevels(image, MIPMAP)));

        /* The SimpleResource class would normally handle metadata parsing when we originally
           got the resource. However, the ResourceManager only looks for .mcmeta metadata, and its
           nested structure and an unordered (stream) accessor for resource packs cannot be
           easily overridden. However, we can create a dummy resource to parse the metadata. */
        SimpleResource metadataParser = new SimpleResource("dummy", new ResourceLocation(""),
                textureStream, metadataStream);

        AnimationMetadataSection animationMetadata =
                metadataParser.getMetadata(AnimationMetadataSection.SERIALIZER);
        ModAnimationMetadataSection modAnimationMetadata =
                metadataParser.getMetadata(ModAnimationMetadataSection.SERIALIZER);
        TextureMetadataSection textureMetadata =
                metadataParser.getMetadata(TextureMetadataSection.SERIALIZER);

        /* Use defaults if no metadata was read.
           The metadata parser can set these to null even if there was no error. */
        if (animationMetadata == null) {
            animationMetadata = AnimationMetadataSection.EMPTY;
        }

        if (modAnimationMetadata == null) {
            modAnimationMetadata = ModAnimationMetadataSection.EMPTY;
        }

        if (textureMetadata == null) {
            textureMetadata = new TextureMetadataSection(false, false);
        }

        boolean blur = textureMetadata.isBlur();
        boolean clamp = textureMetadata.isClamp();

        // Frames
        List<IRGBAImage.VisibleArea> visibleAreas = new ArrayList<>();
        FrameReader<RGBAImageFrame> frameReader = new FrameReader<>(frameData -> {

            /* The immutable list collector was marked as beta for a while,
               and the marking was removed in a later version. */
            @SuppressWarnings("UnstableApiUsage")
            ImmutableList<IRGBAImage> wrappedMipmaps = IntStream.range(0, mipmaps.size()).mapToObj((level) -> {
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
                                                                      List<IRGBAImage.VisibleArea> visibleAreas) {
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
