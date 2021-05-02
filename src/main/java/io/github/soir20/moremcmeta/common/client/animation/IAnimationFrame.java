package io.github.soir20.moremcmeta.common.client.animation;

import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A single frame in an animation.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IAnimationFrame {

    /**
     * Gets the length (time) of this frame.
     * @return  the length of this frame in ticks
     */
    int getFrameTime();

    /**
     * Uploads this frame at a given position in the active texture.
     * @param x     x-coordinate to upload the upper-left corner of this frame at
     * @param y     y-coordinate to upload the upper-left corner of this frame at
     */
    void uploadAt(int x, int y);

}
