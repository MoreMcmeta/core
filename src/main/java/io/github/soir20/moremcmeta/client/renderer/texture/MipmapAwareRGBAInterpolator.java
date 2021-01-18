package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.function.BiFunction;
import java.util.function.Function;

public class MipmapAwareRGBAInterpolator<T extends IRGBAImage, E extends IMipmappableRGBAImage<T>> {
    private final Function<MipmapContainer<T>, E> CONTAINER_GETTER;
    private final Function<Integer, BiFunction<Integer, Integer, T>> IMAGE_GETTER;

    public MipmapAwareRGBAInterpolator(Function<MipmapContainer<T>, E> containerGetter,
            Function<Integer, BiFunction<Integer, Integer, T>> mipmappableImageGetter) {
        CONTAINER_GETTER = containerGetter;
        IMAGE_GETTER = mipmappableImageGetter;
    }

    public E interpolate(int steps, int step, int mipmap, E start, E end) {
        MipmapContainer<T> mipmaps = new MipmapContainer<>();

        for (int level = 0; level < mipmap; level++) {
            RGBAInterpolator<T> interpolator = new RGBAInterpolator<>(IMAGE_GETTER.apply(mipmap));
            T mipmappedFrame = interpolator.interpolate(steps, step,
                    start.getMipmap(level), end.getMipmap(level));

            mipmaps.addMipmap(level, mipmappedFrame);
        }

        return CONTAINER_GETTER.apply(mipmaps);
    }

}
