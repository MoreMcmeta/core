package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.client.animation.AnimationFrameManager;
import io.github.soir20.moremcmeta.client.animation.IAnimationFrame;
import io.github.soir20.moremcmeta.client.io.AnimatedTextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.server.packs.resources.ResourceManager;

import static java.util.Objects.requireNonNull;

/**
 * An animated texture that updates on each tick.
 * @param <F>   animation frame type
 * @author soir20
 */
public abstract class AnimatedTexture<F extends IAnimationFrame> extends AbstractTexture
        implements Tickable, AutoCloseable {
    private final AnimatedTextureData<F> DATA;

    private int ticks;

    public AnimatedTexture(AnimatedTextureData<F> data) {
        DATA = requireNonNull(data, "Data must not be null");
    }

    /**
     * Uploads this texture to OpenGL on the rendering thread.
     * @param manager   resource manager
     */
    @Override
    public void load(ResourceManager manager) {
        requireNonNull(manager, "Resource manager cannot be null");

        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(this::loadImage);
        } else {
            loadImage();
        }
    }

    /**
     * Uploads this image to OpenGL immediately.
     */
    protected abstract void loadImage();

    @Override
    public void tick() {
        ClientLevel currentWorld = Minecraft.getInstance().level;

        int syncTicks = DATA.getSynchronizedTicks();
        AnimationFrameManager<F> frameManager = DATA.getFrameManager();
        if (DATA.isTimeSynchronized() && currentWorld != null) {
            int ticksToAdd = (int) ((currentWorld.getDayTime() - ticks) % syncTicks + syncTicks) % syncTicks;
            ticks += ticksToAdd;
            frameManager.tick(ticksToAdd);
        } else {
            ticks++;
            frameManager.tick();
        }
    }

    /**
     * Closes all resources that this texture uses.
     */
    public void close() {
        getData().getCloseAction().run();
    }

    /**
     * Gets the data for the animated texture.
     * @return the texture's data
     */
    protected AnimatedTextureData<F> getData() {
        return DATA;
    }

}
