package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Reads an {@link AnimatedTexture} from file data.
 * @author soir20
 */
public class AnimatedTextureReader implements ITextureReader<AnimatedTexture<NativeImageFrame>> {
    private final int MIPMAP;
    private final Logger LOGGER;

    /**
     * Creates a new reader for animated textures.
     * @param mipmap    number of mipmap levels to use
     * @param logger    logger for reading-related messages
     */
    public AnimatedTextureReader(int mipmap, Logger logger) {
        MIPMAP = mipmap;
        LOGGER = logger;
    }

    /**
     * Reads an {@link AnimatedTexture}.
     * @param inputStream           input stream with image data
     * @param texMetadata           texture metadata (blur and clamp options)
     * @param animationMetadata     animation metadata (frames, frame time, etc.)
     * @return  an animated texture based on the provided data
     */
    public AnimatedTexture<NativeImageFrame> read(InputStream inputStream, TextureMetadataSection texMetadata,
                                                  AnimationMetadataSection animationMetadata) throws IOException {
        NativeImage image = NativeImage.read(inputStream);
        LOGGER.debug("Successfully read image from input");

        NativeImage[] mipmaps = MipmapGenerator.generateMipmaps(image, MIPMAP);

        boolean blur = texMetadata.getTextureBlur();
        boolean clamp = texMetadata.getTextureClamp();

        // Frames
        FrameReader<NativeImageFrame> frameReader = new FrameReader<>((frameData ->
                new NativeImageFrame(frameData, mipmaps, blur, clamp, false)));
        List<NativeImageFrame> frames = frameReader.read(image.getWidth(), image.getHeight(), animationMetadata);
        int frameWidth = frames.get(0).getWidth();
        int frameHeight = frames.get(0).getHeight();

        // Interpolation
        List<IRGBAImage.VisibleArea> visibleAreas = getInterpolatablePoints(image, frameWidth, frameHeight);
        NativeImageFrameInterpolator interpolator = new NativeImageFrameInterpolator(mipmaps, visibleAreas,
                frameWidth, frameHeight, blur, clamp);

        // Frame management
        AnimationFrameManager<NativeImageFrame> frameManager = new AnimationFrameManager<>(
                frames,
                getFrameTimeCalculator(animationMetadata.getFrameTime()),
                interpolator
        );

        // Resource cleanup
        Runnable closeMipmaps = () -> {
            for (NativeImage mipmap : mipmaps) {
                mipmap.close();
            }

            interpolator.close();
        };

        return new AnimatedTexture<>(frameManager, frameWidth, frameHeight, MIPMAP, closeMipmaps);
    }

    /**
     * Gets the pixels that will change for every mipmap.
     * @param image         the original image to analyze
     * @param frameWidth    the width of a frame
     * @param frameHeight   the height of a frame
     * @return  pixels that change for every mipmap (starting with the default image)
     */
    private List<IRGBAImage.VisibleArea> getInterpolatablePoints(NativeImage image, int frameWidth, int frameHeight) {
        List<IRGBAImage.VisibleArea> visibleAreas = new ArrayList<>();

        // Find points in original image
        IRGBAImage.VisibleArea.Builder noMipmapBuilder = new IRGBAImage.VisibleArea.Builder();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int frameX = x % frameWidth;
                int frameY = y % frameHeight;

                // We want to detect a point that changes in any frame
                if (image.getPixelRGBA(x, y) != image.getPixelRGBA(frameX, frameY)) {
                    noMipmapBuilder.addPixel(frameX, frameY);
                }

            }
        }
        visibleAreas.add(noMipmapBuilder.build());

        // Point coordinates will be different for all mipmap levels
        for (int level = 1; level <= MIPMAP; level++) {
            IRGBAImage.VisibleArea.Builder mipmapBuilder = new IRGBAImage.VisibleArea.Builder();

            for (Pair<Integer, Integer> point : visibleAreas.get(0)) {
                mipmapBuilder.addPixel(point.getFirst() >> level, point.getSecond() >> level);
            }

            visibleAreas.add(mipmapBuilder.build());
        }

        return visibleAreas;
    }

    /**
     * Calculates the time for a frame based on a default time.
     * @param metadataFrameTime     the default frame time
     * @return  a function that calculates the time for a given frame
     */
    private Function<NativeImageFrame, Integer> getFrameTimeCalculator(int metadataFrameTime) {
        return (frame) -> {
            int singleFrameTime = frame.getFrameTime();
            return singleFrameTime == -1 ? metadataFrameTime : singleFrameTime;
        };
    }

    /**
     * Interpolates between {@link NativeImageFrame}s.
     * @author soir20
     */
    private class NativeImageFrameInterpolator implements IInterpolator<NativeImageFrame>, AutoCloseable {
        private final int FRAME_WIDTH;
        private final int FRAME_HEIGHT;
        private final boolean BLUR;
        private final boolean CLAMP;
        private final List<IRGBAImage.VisibleArea> VISIBLE_AREAS;
        private final RGBAInterpolator<NativeImageRGBAWrapper> INTERPOLATOR;
        private final NativeImage[] MIPMAPS;

        /**
         * Creates a new interpolator.
         * @param originalMipmaps   the original mipmaps to base interpolations off of (starting at the original)
         * @param visibleAreas      the visible areas for each mipmap (starting at the original's area)
         * @param frameWidth        the width of a frame in the original image
         * @param frameHeight       the height of a frame in the original image
         * @param blur              whether to blur the output
         * @param clamp             whether to clamp the output
         */
        public NativeImageFrameInterpolator(NativeImage[] originalMipmaps,
                                            List<IRGBAImage.VisibleArea> visibleAreas,
                                            int frameWidth, int frameHeight, boolean blur, boolean clamp) {
            FRAME_WIDTH = frameWidth;
            FRAME_HEIGHT = frameHeight;
            BLUR = blur;
            CLAMP = clamp;
            MIPMAPS = new NativeImage[originalMipmaps.length];

            // Directly convert mipmapped widths to their associated image
            HashMap<Integer, Pair<NativeImage, IRGBAImage.VisibleArea>> widthsToImage = new HashMap<>();
            for (int level = 0; level <= MIPMAP; level++) {
                int mipmappedWidth = FRAME_WIDTH >> level;
                int mipmappedHeight = FRAME_HEIGHT >> level;

                NativeImage mipmappedImage = new NativeImage(mipmappedWidth, mipmappedHeight, true);
                mipmappedImage.copyImageData(originalMipmaps[MIPMAP]);
                MIPMAPS[level] = mipmappedImage;

                widthsToImage.put(mipmappedWidth, new Pair<>(mipmappedImage, visibleAreas.get(level)));
            }

            VISIBLE_AREAS = visibleAreas;

            INTERPOLATOR = new RGBAInterpolator<>((width, height) -> {
                Pair<NativeImage, IRGBAImage.VisibleArea> imageAndPoints = widthsToImage.get(width);

                return new NativeImageRGBAWrapper(imageAndPoints.getFirst(), 0, 0, width, height,
                        imageAndPoints.getSecond());
            });
        }

        /**
         * Interpolates between a starting frame and an ending frame.
         * @param steps     total steps between the start and end frame
         * @param step      current step of the interpolation (between 1 and steps - 1)
         * @param start     the frame to start interpolation from
         * @param end       the frame to end interpolation at
         * @return  the interpolated frame at the given step
         */
        @Override
        public NativeImageFrame interpolate(int steps, int step, NativeImageFrame start, NativeImageFrame end) {
            NativeImage[] mipmaps = new NativeImage[MIPMAP + 1];

            for (int level = 0; level <= MIPMAP; level++) {
                NativeImageRGBAWrapper startImage = new NativeImageRGBAWrapper(
                        start.getImage(level),
                        start.getXOffset() >> level,
                        start.getYOffset() >> level,
                        FRAME_WIDTH >> level,
                        FRAME_HEIGHT >> level,
                        VISIBLE_AREAS.get(level)
                );
                NativeImageRGBAWrapper endImage = new NativeImageRGBAWrapper(
                        end.getImage(level),
                        end.getXOffset() >> level,
                        end.getYOffset() >> level,
                        FRAME_WIDTH >> level,
                        FRAME_HEIGHT >> level,
                        VISIBLE_AREAS.get(level)
                );

                NativeImageRGBAWrapper interpolated = INTERPOLATOR.interpolate(steps, step, startImage, endImage);

                mipmaps[level] = interpolated.getImage();
            }

            FrameReader.FrameData data = new FrameReader.FrameData(mipmaps[0].getWidth(), mipmaps[0].getHeight(),
                    0, 0, 1);
            return new NativeImageFrame(data, mipmaps, BLUR, CLAMP, false);
        }

        /**
         * Closes all mipmapped images where interpolated frames are uploaded.
         */
        @Override
        public void close() {
            for (NativeImage mipmap : MIPMAPS) {
                mipmap.close();
            }
        }
    }

}
