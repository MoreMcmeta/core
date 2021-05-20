package client.event;

import net.minecraft.client.renderer.texture.Tickable;

/**
 * A tick counter that represents a tickable item.
 * @author soir20
 */
public class MockTickable implements Tickable {
    private int ticks;

    @Override
    public void tick() {
        ticks++;
    }

    public int getTicks() {
        return ticks;
    }

}
