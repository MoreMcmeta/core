package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.data.AnimationMetadataSection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AnimatedTextureReader<T extends IRGBAImage & IUploadableMipmap, E extends IAnimationFrame<T>>
        implements IAnimatedTextureReader<AnimatedTexture<E>, T> {
    private final int MIPMAP;
    private final BiFunction<Integer, Integer, T> IMAGE_FACTORY;
    private final BiFunction<SubImage<T>, Integer, E> FRAME_FACTORY;

    public AnimatedTextureReader(int mipmap, BiFunction<Integer, Integer, T> imageFactory,
                                 BiFunction<SubImage<T>, Integer, E> frameFactory) {
        MIPMAP = mipmap;
        IMAGE_FACTORY = imageFactory;
        FRAME_FACTORY = frameFactory;
    }

    @Override
    public AnimatedTexture<E> readAnimatedTexture(MipmapContainer<T> mipmappedImage,
                                               AnimationMetadataSection metadata) {
        int imageWidth = mipmappedImage.getMipmap(0).getWidth();
        int imageHeight = mipmappedImage.getMipmap(0).getHeight();

        if (imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException("Image must not be empty");
        }

        Pair<Integer, Integer> frameSize = metadata.getSpriteSize(imageWidth, imageHeight);
        int frameWidth = frameSize.getFirst();
        int frameHeight = frameSize.getSecond();

        int numFramesX = imageWidth / frameWidth;
        int numFramesY = imageHeight / frameHeight;

        List<E> frames;
        if (metadata.getFrameCount() > 0) {
            frames = getPredefinedFrames(mipmappedImage, metadata, frameWidth, frameHeight, numFramesX);
        } else {
            frames = findFrames(mipmappedImage, frameWidth, frameHeight, numFramesX, numFramesY);
        }

        Function<E, Integer> frameTimeCalculator =
                getFrameTimeCalculator(metadata.getFrameTime());
        IInterpolator<E> interpolator = getFrameInterpolator(frameWidth, frameHeight);

        AnimationFrameManager<E> frameManager =
                new AnimationFrameManager<>(frames, frameTimeCalculator, interpolator);

        return new AnimatedTexture<>(frameManager, frameWidth, frameHeight, MIPMAP);
    }

    private Pair<Integer, Integer> frameIndexToPosition(int index, int numFramesX) {
        int xPos = index % numFramesX;
        int yPos = index / numFramesX;
        return new Pair<>(xPos, yPos);
    }

    private List<E> getPredefinedFrames(MipmapContainer<T> mipmaps, AnimationMetadataSection metadata,
                                        int frameWidth, int frameHeight, int numFramesX) {
        List<E> frames = new ArrayList<>();

        for (int frame = 0; frame < metadata.getFrameCount(); frame++) {
            int index = metadata.getFrameIndex(frame);
            int time = metadata.getFrameTimeSingle(frame);

            Pair<Integer, Integer> framePos = frameIndexToPosition(index, numFramesX);
            int xOffset = framePos.getFirst() * frameWidth;
            int yOffset = framePos.getSecond() * frameHeight;

            SubImage<T> frameImage = new SubImage<>(mipmaps, xOffset, yOffset, frameWidth, frameHeight,
                    false, false, false);

            frames.add(FRAME_FACTORY.apply(frameImage, time));
        }

        return frames;
    }

    private List<E> findFrames(MipmapContainer<T> mipmaps, int frameWidth, int frameHeight,
                               int numFramesX, int numFramesY) {
        List<E> frames = new ArrayList<>();

        for (int row = 0; row < numFramesY; row++) {
            for (int column = 0; column < numFramesX; column++) {
                SubImage<T> frameImage = new SubImage<>(mipmaps,
                        column * frameWidth, row * frameHeight, frameWidth, frameHeight,
                        false, false, false);

                frames.add(FRAME_FACTORY.apply(frameImage,  -1));
            }
        }

        return frames;
    }

    private Function<E, Integer> getFrameTimeCalculator(int metadataFrameTime) {
        return (frame) -> {
            int singleFrameTime = frame.getFrameTime();
            return singleFrameTime == -1 ? metadataFrameTime : singleFrameTime;
        };
    }

    private IInterpolator<E> getFrameInterpolator(int frameWidth, int frameHeight) {
        RGBAInterpolator<T> interpolator = new RGBAInterpolator<>(IMAGE_FACTORY);

        return (int steps, int step, E start, E end) -> {
            MipmapContainer<T> mipmaps = new MipmapContainer<>();

            for (int level = 0; level < MIPMAP; level++) {
                T startImage = start.getImage().getMipmap(level);
                T endImage = end.getImage().getMipmap(level);

                T interpolated = interpolator.interpolate(steps, step, startImage, endImage);

                mipmaps.addMipmap(level, interpolated);
            }

            SubImage<T> subImage = new SubImage<>(mipmaps, 0, 0, frameWidth, frameHeight,
                    false, false, false);
            return FRAME_FACTORY.apply(subImage, 1);
        };
    }

}
