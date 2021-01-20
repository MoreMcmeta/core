package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class AnimatedTextureReader {
    private final int MIPMAP;
    private final Logger LOGGER;

    public AnimatedTextureReader(int mipmap, Logger logger) {
        MIPMAP = mipmap;
        LOGGER = logger;
    }

    public AnimatedTexture<NativeImageFrame> read(InputStream inputStream, AnimationMetadataSection metadata) {
        NativeImage image;
        try {
            image = NativeImage.read(inputStream);
        } catch (IOException e) {
            LOGGER.error("Unable to read animated image from input stream");
            return null;
        }

        NativeImage[] mipmaps = MipmapGenerator.generateMipmaps(image, MIPMAP);

        FrameReader<NativeImageFrame> frameReader = new FrameReader<>((frameData ->
                new NativeImageFrame(frameData, mipmaps, false, false, false)));

        List<NativeImageFrame> frames = frameReader.read(image.getWidth(), image.getHeight(), metadata);
        int frameWidth = frames.get(0).getImageWrapper(0).getWidth();
        int frameHeight = frames.get(0).getImageWrapper(0).getHeight();

        AnimationFrameManager<NativeImageFrame> frameManager = new AnimationFrameManager<>(frames,
                getFrameTimeCalculator(metadata.getFrameTime()), getFrameInterpolator(frameWidth, frameHeight));

        return new AnimatedTexture<>(frameManager, frameWidth, frameHeight, MIPMAP);
    }

    private Function<NativeImageFrame, Integer> getFrameTimeCalculator(int metadataFrameTime) {
        return (frame) -> {
            int singleFrameTime = frame.getFrameTime();
            return singleFrameTime == -1 ? metadataFrameTime : singleFrameTime;
        };
    }

    private IInterpolator<NativeImageFrame> getFrameInterpolator(int frameWidth, int frameHeight) {

        // Directly convert mipmapped widths to their associated image
        HashMap<Integer, NativeImage> widthsToImage = new HashMap<>();
        for (int level = 0; level <= MIPMAP; level++) {
            int mipmappedWidth = frameWidth >> level;
            int mipmappedHeight = frameHeight >> level;
            widthsToImage.put(mipmappedWidth, new NativeImage(mipmappedWidth, mipmappedHeight, false));
        }

        RGBAInterpolator<NativeImageRGBAWrapper> interpolator = new RGBAInterpolator<>((width, height) ->
                new NativeImageRGBAWrapper(widthsToImage.get(width), 0, 0, width, height));

        return (int steps, int step, NativeImageFrame start, NativeImageFrame end) -> {
            NativeImage[] mipmaps = new NativeImage[MIPMAP + 1];

            for (int level = 0; level <= MIPMAP; level++) {
                NativeImageRGBAWrapper startImage = start.getImageWrapper(level);
                NativeImageRGBAWrapper endImage = end.getImageWrapper(level);

                NativeImageRGBAWrapper interpolated = interpolator.interpolate(steps, step, startImage, endImage);

                mipmaps[level] = interpolated.getImage();
            }

            FrameReader.FrameData data = new FrameReader.FrameData(mipmaps[0].getWidth(), mipmaps[0].getHeight(),
                    0, 0, 1);
            return new NativeImageFrame(data, mipmaps, false, false, false);
        };
    }

}
