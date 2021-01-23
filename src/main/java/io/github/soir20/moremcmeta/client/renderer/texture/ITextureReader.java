package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads an animated texture.
 * @param <T>   the type of texture to create
 * @author soir20
 */
public interface ITextureReader<T extends Texture & ITickable> {

    /**
     * Reads an animated texture from file data.
     * @param stream                input stream of image data
     * @param texMetadata           texture metadata (blur and clamp options)
     * @param animationMetadata     animation metadata (frames, time info, etc.)
     * @return  an animated texture
     * @throws IOException  failure reading from the input stream
     */
    T read(InputStream stream, TextureMetadataSection texMetadata, AnimationMetadataSection animationMetadata)
            throws IOException;

}
