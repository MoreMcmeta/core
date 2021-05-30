package io.github.soir20.moremcmeta.client.renderer.texture;

import io.github.soir20.moremcmeta.client.animation.AnimationFrameManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Manages an animation for an {@link EventDrivenTexture}.
 * @param <I> image type
 * @author soir20
 */
public class AnimationComponent<I> implements ITextureComponent<I> {
    private final boolean DO_DAYTIME_SYNC;
    private final int SYNC_TICKS;
    private final AnimationFrameManager<I> FRAME_MANAGER;

    private int ticks;

    /**
     * Creates an animation component that syncs to the current game time.
     * @param syncTicks         number of ticks to sync to
     * @param frameManager      frame manager for the animation
     */
    public AnimationComponent(int syncTicks, AnimationFrameManager<I> frameManager) {
        DO_DAYTIME_SYNC = true;
        SYNC_TICKS = syncTicks;
        FRAME_MANAGER = requireNonNull(frameManager, "Frame manager cannot be null");
    }

    /**
     * Creates an animation component that does not sync to the current game time.
     * @param frameManager      frame manager for the animation
     */
    public AnimationComponent(AnimationFrameManager<I> frameManager) {
        DO_DAYTIME_SYNC = false;
        SYNC_TICKS = -1;
        FRAME_MANAGER = requireNonNull(frameManager, "Frame manager cannot be null");
    }

    /**
     * Gets the animation listeners for this component.
     * @return the listeners for this component
     */
    @Override
    public Stream<TextureListener<I>> getListeners() {
        TextureListener<I> tickListener =
                new TextureListener<>(TextureListener.Type.TICK, (state) -> {
                    ClientLevel currentWorld = Minecraft.getInstance().level;

                    if (DO_DAYTIME_SYNC && currentWorld != null) {
                        int ticksToAdd = (int) (currentWorld.getDayTime() - ticks) % SYNC_TICKS + SYNC_TICKS;
                        ticksToAdd %= SYNC_TICKS;

                        ticks += ticksToAdd;
                        FRAME_MANAGER.tick(ticksToAdd);
                    } else {
                        ticks++;
                        FRAME_MANAGER.tick();
                    }

                    state.markNeedsUpload();
                });

        TextureListener<I> uploadListener =
                new TextureListener<>(TextureListener.Type.UPLOAD, (state) ->
                        state.replaceImage(FRAME_MANAGER.getCurrentFrame()));

        return Stream.of(tickListener, uploadListener);
    }

}
