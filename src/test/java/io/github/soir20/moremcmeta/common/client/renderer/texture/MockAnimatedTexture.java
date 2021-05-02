package io.github.soir20.moremcmeta.common.client.renderer.texture;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.resources.IResourceManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.InputStream;

/**
 * Mocks the animated texture. Doesn't do anything besides being able to be used in a texture loader.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MockAnimatedTexture extends Texture implements ITickable {

    public MockAnimatedTexture(InputStream textureStream, InputStream metadataStream) {}

    @Override
    public void tick() {}

    @Override
    public void loadTexture(IResourceManager manager) {}
}
