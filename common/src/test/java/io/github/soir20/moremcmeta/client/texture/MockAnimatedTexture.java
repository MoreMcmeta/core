package io.github.soir20.moremcmeta.client.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * A fake tickable texture.
 * @author soir20
 */
public class MockAnimatedTexture extends AbstractTexture implements CustomTickable {
    private int ticks;

    @Override
    public void tick() {
        ticks++;
    }

    public int getTicks() {
        return ticks;
    }

    @Override
    public void load(ResourceManager resourceManager) {}

}
