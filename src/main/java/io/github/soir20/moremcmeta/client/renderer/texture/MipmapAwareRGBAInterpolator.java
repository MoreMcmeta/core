package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class MipmapAwareRGBAInterpolator<T extends IRGBAImage> {
    private final RGBAInterpolator<T> INTERPOLATOR;

    public MipmapAwareRGBAInterpolator(BiFunction<Integer, Integer, T> imageFactory) {
        INTERPOLATOR = new RGBAInterpolator<>(imageFactory);
    }

    public Collection<MipmapContainer<T>> interpolate(int steps, int mipmap, IMipmappableRGBAImage<T> start,
                                                      IMipmappableRGBAImage<T> end) {
        List<MipmapContainer<T>> mipmapContainers = Collections.nCopies(steps, new MipmapContainer<>());

        for (int level = 0; level < mipmap; level++) {
            Collection<T> frames = INTERPOLATOR.interpolate(steps, start.getMipmap(level), end.getMipmap(level));

            int frameIndex = 0;
            for (T frame : frames) {
                mipmapContainers.get(frameIndex).addMipmap(level, frame);
                frameIndex++;
            }
        }

        return mipmapContainers;
    }

}
