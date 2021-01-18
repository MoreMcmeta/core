package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.AnimationMetadataSection;

/**
 * Creates animated (tickable) textures.
 * @param <T>   tickable texture type
 * @author soir20
 */
public interface IAnimatedTextureReader<T extends Texture & ITickable> {

    /**
     * Creates an animated texture based on file data
     * @param image     the image to read from
     * @param metadata  image animation metadata
     * @return animated texture that can be loaded into the
     *         {@link net.minecraft.client.renderer.texture.TextureManager}
     */
    T readAnimatedTexture(NativeImage image, AnimationMetadataSection metadata);

}
