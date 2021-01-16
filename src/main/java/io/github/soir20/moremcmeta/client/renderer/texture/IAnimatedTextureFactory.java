package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;

/**
 * Creates animated (tickable) textures. In most cases, use a reference to a texture's constructor
 * to fulfill this contract.
 * @param <T>   tickable texture type
 * @author soir20
 */
public interface IAnimatedTextureFactory<T extends Texture & ITickable> {

    /**
     * Creates an animated texture based on a file data.
     * @param location      file location of texture identical to how it is used in a entity/gui/map
     * @param width         texture width
     * @param height        texture height
     * @param metadata      animation metadata (.mcmeta information)
     * @param mipmapLevels  number of mipmap levels to use
     * @param nativeImage   native image corresponding to the texture
     * @return  animated texture that can be loaded into the
     *          {@link net.minecraft.client.renderer.texture.TextureManager}
     */
    T createAnimatedTexture(ResourceLocation location, int width, int height,
                                           AnimationMetadataSection metadata, int mipmapLevels,
                                           NativeImage nativeImage);

}
