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
import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.api.client.metadata.ParsedMetadata;
import io.github.soir20.moremcmeta.api.client.texture.ComponentProvider;
import io.github.soir20.moremcmeta.impl.client.adapter.NativeImageAdapter;
import io.github.soir20.moremcmeta.impl.client.texture.CleanupComponent;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImage;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImageFrame;
import io.github.soir20.moremcmeta.impl.client.texture.EventDrivenTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MipmapGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Assembles texture data into a texture builder.
 * @author soir20
 */
public class TextureDataAssembler {

    /**
     * Combines the texture image and metadata into a {@link EventDrivenTexture.Builder} that
     * minimally needs an upload component.
     * @param data          texture data to assemble
     * @return texture data assembled as a texture builder
     */
    public EventDrivenTexture.Builder assemble(TextureData<NativeImageAdapter> data) {
        requireNonNull(data, "Data cannot be null");

        Minecraft minecraft = Minecraft.getInstance();
        final int MAX_MIPMAP = minecraft.options.mipmapLevels;

        NativeImage original = data.image().getImage();
        int frameWidth = data.frameSize().width();
        int frameHeight = data.frameSize().height();
        boolean blur = data.blur();
        boolean clamp = data.clamp();

        // Create frames
        List<NativeImage> mipmaps = Arrays.asList(MipmapGenerator.generateMipLevels(original, MAX_MIPMAP));
        ImmutableList<CloseableImageFrame> frames = getFrames(
                mipmaps, frameWidth, frameHeight, blur, clamp
        );
        CloseableImageFrame generatedFrame = createGeneratedFrame(
                mipmaps, frameWidth, frameHeight, blur, clamp
        );

        // Resource cleanup
        CloseableImageFrame firstFrame = frames.get(0);
        Runnable closeMipmaps = () -> {

            // The images are shared between all frames
            for (int level = 0; level <= firstFrame.getMipmapLevel(); level++) {
                firstFrame.getImage(level).close();
            }

            for (int level = 0; level <= generatedFrame.getMipmapLevel(); level++) {
                generatedFrame.getImage(level).close();
            }
        };

        // Add components
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(frames).setGeneratedFrame(generatedFrame).add(new CleanupComponent(closeMipmaps));
        for (Pair<ParsedMetadata, ComponentProvider> metadata : data.parsedMetadata()) {
            ParsedMetadata sectionData = metadata.getFirst();
            ComponentProvider componentProvider = metadata.getSecond();
            componentProvider.assemble(sectionData, data.frameSize(), data.blur(), data.clamp()).forEach(builder::add);
        }

        return builder;
    }

    /**
     * Gets all frames from the generated mipmaps and animation metadata.
     * @param mipmaps               mipmaps of the full texture image (with all frames)
     * @param frameWidth            width of each frame in the image
     * @param frameHeight           height of each frame in the image
     * @param blur                  whether to blur the texture
     * @param clamp                 whether to clamp the texture
     * @return the frames based on the texture image in chronological order
     */
    private ImmutableList<CloseableImageFrame> getFrames(List<NativeImage> mipmaps, int frameWidth, int frameHeight,
                                                         boolean blur, boolean clamp) {
        int mipmap = mipmaps.size() - 1;

        FrameReader<CloseableImageFrame> frameReader = new FrameReader<>((frameData) -> {

            /* The immutable list collector was marked as beta for a while,
               and the marking was removed in a later version. */
            ImmutableList<CloseableImage> wrappedMipmaps = IntStream.rangeClosed(0, mipmap).mapToObj((level) -> {
                int width = frameData.getWidth() >> level;
                int height = frameData.getHeight() >> level;

                return new NativeImageAdapter(
                        mipmaps.get(level),
                        frameData.getXOffset() >> level, frameData.getYOffset() >> level,
                        width, height,
                        level, blur, clamp, false
                );
            }).collect(ImmutableList.toImmutableList());

            return new CloseableImageFrame(frameData, wrappedMipmaps);
        });

        return frameReader.read(mipmaps.get(0).getWidth(), mipmaps.get(0).getHeight(), frameWidth, frameHeight);
    }

    /**
     * Creates a frame that will hold generated frames.
     * @param originals             the original mipmaps to copy from
     * @param frameWidth            the width of a single frame
     * @param frameHeight           the height of a single frame
     * @param blur                  whether the images are blurred
     * @param clamp                 whether the images are clamped
     * @return the adapters for the interpolation images
     */
    private CloseableImageFrame createGeneratedFrame(List<NativeImage> originals, int frameWidth,
                                                     int frameHeight, boolean blur, boolean clamp) {
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
                    blur, clamp, false
            );
            images.add(adapter);
        }

        return new CloseableImageFrame(
                new FrameReader.FrameData(frameWidth, frameHeight, 0, 0),
                images.build()
        );
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
