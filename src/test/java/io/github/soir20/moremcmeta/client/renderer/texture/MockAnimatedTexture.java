package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.resources.IResourceManager;

public class MockAnimatedTexture extends Texture implements ITickable {
    @Override
    public void tick() {}

    @Override
    public void loadTexture(IResourceManager manager) {}
}
