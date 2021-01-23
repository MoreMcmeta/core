package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;

import java.io.InputStream;

public interface ITextureReader<T extends Texture & ITickable> {

    T read(InputStream stream, TextureMetadataSection texMetadata, AnimationMetadataSection animationMetadata);

}
