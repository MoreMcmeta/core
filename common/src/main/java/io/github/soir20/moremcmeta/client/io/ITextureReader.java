package io.github.soir20.moremcmeta.client.io;

import net.minecraft.client.renderer.texture.AbstractTexture;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads an animated texture.
 * @author soir20
 */
public interface ITextureReader {

    /**
     * Reads an animated texture from file data.
     * @param textureStream     input stream of image data
     * @param metadataStream    input stream of texture metadata (JSON)
     * @return getter for retrieving an animated texture after all resources are loaded
     * @throws IOException  failure reading from either input stream
     */
    AbstractTexture read(InputStream textureStream, InputStream metadataStream) throws IOException;

}
