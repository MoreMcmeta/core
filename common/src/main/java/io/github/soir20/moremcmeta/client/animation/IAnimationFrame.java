package io.github.soir20.moremcmeta.client.animation;

import io.github.soir20.moremcmeta.math.Point;

/**
 * A single frame in an animation.
 * @author soir20
 */
public interface IAnimationFrame {

    /**
     * Gets the length (time) of this frame.
     * @return  the length of this frame in ticks
     */
    int getFrameTime();

    /**
     * Uploads this frame at a given position in the active texture.
     * @param point     point to upload the top-left corner of this frame at
     */
    void uploadAt(Point point);

}
