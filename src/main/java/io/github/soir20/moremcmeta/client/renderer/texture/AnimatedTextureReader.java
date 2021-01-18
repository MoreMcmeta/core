package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AnimatedTextureReader<T> implements IAnimatedTextureReader<AnimatedTexture> {
    private final int MIPMAP;

    public AnimatedTextureReader(int mipmap) {
        MIPMAP = mipmap;
    }

    @Override
    public AnimatedTexture readAnimatedTexture(NativeImage image, AnimationMetadataSection metadata) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        if (imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException("Image must not be empty");
        }

        Pair<Integer, Integer> frameSize = metadata.getSpriteSize(imageWidth, imageHeight);
        int frameWidth = frameSize.getFirst();
        int frameHeight = frameSize.getSecond();

        MipmapContainer<NativeImageRGBAWrapper> mipmappedImage =
                createMipmappedImage(image, frameWidth, frameHeight);

        int numFramesX = imageWidth / frameWidth;
        int numFramesY = imageHeight / frameHeight;

        List<NativeImageFrame> frames;
        if (metadata.getFrameCount() > 0) {
            frames = getPredefinedFrames(mipmappedImage, metadata, frameWidth, frameHeight, numFramesX);
        } else {
            frames = findFrames(mipmappedImage, frameWidth, frameHeight, numFramesX, numFramesY);
        }

        Function<NativeImageFrame, Integer> frameTimeCalculator =
                getFrameTimeCalculator(metadata.getFrameTime());
        IInterpolator<NativeImageFrame> interpolator = getFrameInterpolator(frameWidth, frameHeight);

        AnimationFrameManager<NativeImageFrame> frameManager =
                new AnimationFrameManager<>(frames, frameTimeCalculator, interpolator);

        return new AnimatedTexture(frameManager, frameWidth, frameHeight, MIPMAP);
    }

    private MipmapContainer<NativeImageRGBAWrapper> createMipmappedImage(NativeImage image,
                                                                         int frameWidth, int frameHeight) {
        NativeImage[] mipmaps = generateMipmaps(image, frameWidth, frameHeight);
        MipmapContainer<NativeImageRGBAWrapper> mipmappedImage = new MipmapContainer<>();

        for (int level = 0; level < mipmaps.length; level++) {
            NativeImageRGBAWrapper wrappedMipmap = new NativeImageRGBAWrapper(mipmaps[level]);
            mipmappedImage.addMipmap(level, wrappedMipmap);
        }

        return mipmappedImage;
    }

    private NativeImage[] generateMipmaps(NativeImage image, int frameWidth, int frameHeight) {
        try {
            return MipmapGenerator.generateMipmaps(image, MIPMAP);
        } catch (Throwable throwable) {
            CrashReport report = CrashReport.makeCrashReport(throwable,
                    "Generating mipmaps for texture");
            CrashReportCategory category = report.makeCategory("Animated texture mipmapping");

            category.addDetail("Image size", image.getWidth() + " x " + image.getHeight());
            category.addDetail("Frame size", frameWidth + " x " + frameHeight);
            category.addDetail("Mipmap levels", MIPMAP);

            throw new ReportedException(report);
        }
    }

    private Pair<Integer, Integer> frameIndexToPosition(int index, int numFramesX) {
        int xPos = index % numFramesX;
        int yPos = index / numFramesX;
        return new Pair<>(xPos, yPos);
    }

    private List<NativeImageFrame> getPredefinedFrames(MipmapContainer<NativeImageRGBAWrapper> mipmaps,
                                                       AnimationMetadataSection metadata, int frameWidth,
                                                       int frameHeight, int numFramesX) {
        List<NativeImageFrame> frames = new ArrayList<>();

        for (int frame = 0; frame < metadata.getFrameCount(); frame++) {
            int index = metadata.getFrameIndex(frame);
            int time = metadata.getFrameTimeSingle(frame);

            Pair<Integer, Integer> framePos = frameIndexToPosition(index, numFramesX);
            int xOffset = framePos.getFirst() * frameWidth;
            int yOffset = framePos.getSecond() * frameHeight;

            SubImage<NativeImageRGBAWrapper> frameImage = new SubImage<>(mipmaps,
                    xOffset, yOffset, frameWidth, frameHeight, false, false, false);

            frames.add(new NativeImageFrame(frameImage, time));
        }

        return frames;
    }

    private List<NativeImageFrame> findFrames(MipmapContainer<NativeImageRGBAWrapper> mipmaps, int frameWidth,
                                              int frameHeight, int numFramesX, int numFramesY) {
        List<NativeImageFrame> frames = new ArrayList<>();

        for (int row = 0; row < numFramesY; row++) {
            for (int column = 0; column < numFramesX; column++) {
                SubImage<NativeImageRGBAWrapper> frameImage = new SubImage<>(mipmaps,
                        column * frameWidth, row * frameHeight, frameWidth, frameHeight,
                        false, false, false);

                frames.add(new NativeImageFrame(frameImage,  -1));
            }
        }

        return frames;
    }

    private Function<NativeImageFrame, Integer> getFrameTimeCalculator(int metadataFrameTime) {
        return (frame) -> {
            int singleFrameTime = frame.getFrameTime();
            return singleFrameTime == -1 ? metadataFrameTime : singleFrameTime;
        };
    }

    private IInterpolator<NativeImageFrame> getFrameInterpolator(int frameWidth, int frameHeight) {
        NativeImage interpolationHolder = new NativeImage(frameWidth, frameHeight, false);
        NativeImageRGBAWrapper wrappedHolder = new NativeImageRGBAWrapper(interpolationHolder);

        RGBAInterpolator<NativeImageRGBAWrapper> interpolator =
                new RGBAInterpolator<>((width, height) -> wrappedHolder);

        return (int steps, int step, NativeImageFrame start, NativeImageFrame end) -> {
            MipmapContainer<NativeImageRGBAWrapper> mipmaps = new MipmapContainer<>();

            for (int level = 0; level < MIPMAP; level++) {
                NativeImageRGBAWrapper startImage = start.getImage().getMipmap(level);
                NativeImageRGBAWrapper endImage = end.getImage().getMipmap(level);

                NativeImageRGBAWrapper interpolated =
                        interpolator.interpolate(steps, step, startImage, endImage);

                mipmaps.addMipmap(level, interpolated);
            }

            SubImage<NativeImageRGBAWrapper> subImage = new SubImage<>(mipmaps, 0, 0,
                    frameWidth, frameHeight, false, false, false);
            return new NativeImageFrame(subImage, 1);
        };
    }

}
