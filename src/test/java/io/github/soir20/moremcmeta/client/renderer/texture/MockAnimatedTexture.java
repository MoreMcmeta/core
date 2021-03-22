package io.github.soir20.moremcmeta.client.renderer.texture;

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
public class MockAnimatedTexture extends Texture implements ITickable {
    private int ticks;

    public MockAnimatedTexture(InputStream textureStream, InputStream metadataStream) {
        ticks = 0;
    }

    @Override
    public void tick() {
        ticks++;
    }

    public int getTicks() {
        return ticks;
    }

    @Override
    public void loadTexture(IResourceManager manager) {}
}
