package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.stream.Stream;

/**
 * A container for related {@link TextureListener}s.
 * @param <I>
 */
public interface ITextureComponent<I> {

    /**
     * Gets all listeners for this component.
     * @return all of this component's listeners
     */
    Stream<TextureListener<I>> getListeners();

}
