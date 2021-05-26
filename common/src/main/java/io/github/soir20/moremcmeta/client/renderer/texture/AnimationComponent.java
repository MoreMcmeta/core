package io.github.soir20.moremcmeta.client.renderer.texture;

import io.github.soir20.moremcmeta.client.animation.AnimationFrameManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.stream.Stream;

/**
 * Manages an animation for an {@link EventDrivenTexture}.
 */
public class AnimationComponent implements ITextureComponent<NativeImageFrame> {
    private final boolean DO_DAYTIME_SYNC;
    private final int SYNC_TICKS;
    private final AnimationFrameManager<NativeImageFrame> FRAME_MANAGER;

    private int ticks;

    /**
     * Creates an animation component that syncs to the current game time.
     * @param syncTicks         number of ticks to sync to
     * @param frameManager      frame manager for the animation
     */
    public AnimationComponent(int syncTicks, AnimationFrameManager<NativeImageFrame> frameManager) {
        DO_DAYTIME_SYNC = true;
        SYNC_TICKS = syncTicks;
        FRAME_MANAGER = frameManager;
    }

    /**
     * Creates an animation component that does not sync to the current game time.
     * @param frameManager      frame manager for the animation
     */
    public AnimationComponent(AnimationFrameManager<NativeImageFrame> frameManager) {
        DO_DAYTIME_SYNC = false;
        SYNC_TICKS = -1;
        FRAME_MANAGER = frameManager;
    }

    /**
     * Gets the animation listeners for this component.
     * @return the listeners for this component
     */
    @Override
    public Stream<TextureListener<NativeImageFrame>> getListeners() {
        TextureListener<NativeImageFrame> tickListener =
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

        TextureListener<NativeImageFrame> uploadListener =
                new TextureListener<>(TextureListener.Type.UPLOAD, (state) ->
                        state.replaceImage(FRAME_MANAGER.getCurrentFrame()));

        return Stream.of(tickListener, uploadListener);
    }

}
