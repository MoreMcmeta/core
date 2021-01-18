package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.Collection;

/**
 * A mipmapped image with an RGB color scheme.
 * Color format: AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB in binary, stored as an integer (32 bits total)
 * @author soir20
 */
public interface IMipmappableRGBAImage<T extends IRGBAImage> {

    T getMipmap(int level);

    boolean isMipmapped();

    Collection<Integer> getMipmapLevels();

}