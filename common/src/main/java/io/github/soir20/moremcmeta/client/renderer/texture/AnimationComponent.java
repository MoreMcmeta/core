package io.github.soir20.moremcmeta.client.renderer.texture;

import io.github.soir20.moremcmeta.client.animation.AnimationFrameManager;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Manages an animation for an {@link EventDrivenTexture}.
 * @param <I> image type
 * @author soir20
 */
public class AnimationComponent<I> implements ITextureComponent<I> {
    private final int SYNC_TICKS;
    private final Supplier<Optional<Long>> TIME_GETTER;
    private final AnimationFrameManager<I> FRAME_MANAGER;

    private int ticks;

    /**
     * Creates an animation component that syncs to the current game time.
     * @param syncTicks         number of ticks to sync to
     * @param timeGetter        gets the client's current time if it has a time
     * @param frameManager      frame manager for the animation
     */
    public AnimationComponent(int syncTicks, Supplier<Optional<Long>> timeGetter,
                              AnimationFrameManager<I> frameManager) {
        SYNC_TICKS = syncTicks;
        TIME_GETTER = timeGetter;
        FRAME_MANAGER = requireNonNull(frameManager, "Frame manager cannot be null");
    }

    /**
     * Creates an animation component that does not sync to the current game time.
     * @param frameManager      frame manager for the animation
     */
    public AnimationComponent(AnimationFrameManager<I> frameManager) {
        SYNC_TICKS = -1;
        TIME_GETTER = Optional::empty;
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
                    Optional<Long> timeOptional = TIME_GETTER.get();

                    if (timeOptional.isPresent()) {
                        long currentTime = timeOptional.get();
                        int ticksToAdd = (int) ((currentTime - ticks) % SYNC_TICKS) + SYNC_TICKS;
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
