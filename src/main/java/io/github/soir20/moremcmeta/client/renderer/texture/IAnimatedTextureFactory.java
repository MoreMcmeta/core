package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;

public interface IAnimatedTextureFactory<T extends Texture & ITickable> {

    T createAnimatedTexture(ResourceLocation location, int width, int height,
                                           AnimationMetadataSection metadata, int mipmapLevels,
                                           NativeImage nativeImage);

}
