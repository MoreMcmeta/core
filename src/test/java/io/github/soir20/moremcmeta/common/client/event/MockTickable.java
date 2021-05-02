package io.github.soir20.moremcmeta.common.client.event;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.ITickable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A tick counter that represents a tickable item.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
