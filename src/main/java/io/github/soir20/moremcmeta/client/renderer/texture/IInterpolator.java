package io.github.soir20.moremcmeta.client.renderer.texture;

import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interpolates between two images.
 * @param <I>   type of image
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IInterpolator<I> {

    /**
     * Creates an image between two other images at a certain step.
     * @param steps     total number of steps to interpolate
     * @param step      current step of the interpolation (between 1 and steps - 1)
     * @param start     image to start interpolation from
     * @param end       image to end interpolation at
     * @return  the interpolated image at the given step
     */
    I interpolate(int steps, int step, I start, I end);

}
