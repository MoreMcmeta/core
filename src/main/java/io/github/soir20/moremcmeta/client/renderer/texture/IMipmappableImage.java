package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.Collection;

/**
 * An image that can be mipmapped.
 * @author soir20
 */
public interface IMipmappableImage<T> {

    T getMipmap(int level);

    boolean isMipmapped();

    Collection<Integer> getMipmapLevels();

}