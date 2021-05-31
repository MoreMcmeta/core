package client.event;

import io.github.soir20.moremcmeta.client.renderer.texture.CustomTickable;

/**
 * A tick counter that represents a tickable item.
 * @author soir20
 */
public class MockTickable implements CustomTickable {
    private int ticks;

    @Override
    public void tick() {
        ticks++;
    }

    public int getTicks() {
        return ticks;
    }

}
