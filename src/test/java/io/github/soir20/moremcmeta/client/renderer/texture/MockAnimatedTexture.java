package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResourceManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.InputStream;

@ParametersAreNonnullByDefault
public class MockAnimatedTexture extends Texture implements ITickable {

    public MockAnimatedTexture(InputStream stream, TextureMetadataSection texMetadata,
                               AnimationMetadataSection animationMetadata) {}

    @Override
    public void tick() {}

    @Override
    public void loadTexture(IResourceManager manager) {}
}
