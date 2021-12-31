package io.github.soir20.moremcmeta.client.texture;

/**
 * Prepares a texture in OpenGL. Textures must already have an initialized ID, so
 * an {@link net.minecraft.client.renderer.texture.AbstractTexture} should be created
 * first.
 * @author soir20
 */
@FunctionalInterface
public interface TexturePreparer {

    /**
     * Prepares a new texture in OpenGL.
     * @param glId          the unique ID of the new texture in OpenGL
     * @param mipmapLevel   the mipmap level of the texture
     * @param width         the width of the texture (no mipmap)
     * @param height        the height of the texture (no mipmap)
     */
    void prepare(int glId, int mipmapLevel, int width, int height);

}
