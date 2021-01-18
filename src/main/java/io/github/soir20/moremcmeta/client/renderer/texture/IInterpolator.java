package io.github.soir20.moremcmeta.client.renderer.texture;

public interface IInterpolator<T> {

    T interpolate(int steps, int step, T start, T end);

}
