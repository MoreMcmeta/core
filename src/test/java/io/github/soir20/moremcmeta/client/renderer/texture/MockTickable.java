package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;

/**
 * A tick counter that represents a tickable item.
 * @author soir20
 */
public class MockTickable implements ITickable {
    private int ticks;

    @Override
    public void tick() {
        ticks++;
    }

    public int getTicks() {
        return ticks;
    }

}
