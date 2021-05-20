package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;

/**
 * Mocks the animated texture. Doesn't do anything besides being able to be used in a texture loader.
 * @author soir20
 */
public class MockAnimatedTexture extends AbstractTexture implements Tickable {

    public MockAnimatedTexture(InputStream textureStream, InputStream metadataStream) {}

    @Override
    public void tick() {}

    @Override
    public void load(ResourceManager manager) {}
}
