package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.NativeImage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Creates a factory that can read an image input stream into a {@link NativeImage}. In most cases, use
 * {@link NativeImage#read(InputStream)} to fulfill this contract.}
 * @author soir20
 */
public interface INativeImageFactory {

    /**
     * Reads a {@link NativeImage} based on an image input stream.
     * @param inputStreamIn     image input stream
     * @return  {@link NativeImage} based on the input stream
     * @throws IOException  an error while reading the input stream
     */
    NativeImage createNativeImage(InputStream inputStreamIn) throws IOException;

}
