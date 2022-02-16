/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.client.animation.AnimationFrameManager;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Manages an animation for an {@link EventDrivenTexture}.
 * @author soir20
 */
public class AnimationComponent implements TextureComponent {
    private final int SYNC_TICKS;
    private final Supplier<Optional<Long>> TIME_GETTER;
    private final AnimationFrameManager<? extends RGBAImageFrame> FRAME_MANAGER;

    private int ticks;

    /**
     * Creates an animation component that syncs to the current game time.
     * @param syncTicks         number of ticks to sync to
     * @param timeGetter        gets the client's current time if it has a time
     * @param frameManager      frame manager for the animation
     */
    public AnimationComponent(int syncTicks, Supplier<Optional<Long>> timeGetter,
                              AnimationFrameManager<? extends RGBAImageFrame> frameManager) {
        if (syncTicks <= 0) {
            throw new IllegalArgumentException("Sync ticks cannot be zero or negative");
        }

        SYNC_TICKS = syncTicks;
        TIME_GETTER = requireNonNull(timeGetter, "Time getter cannot be null");
        FRAME_MANAGER = requireNonNull(frameManager, "Frame manager cannot be null");
    }

    /**
     * Creates an animation component that does not sync to the current game time.
     * @param frameManager      frame manager for the animation
     */
    public AnimationComponent(AnimationFrameManager<? extends RGBAImageFrame> frameManager) {
        SYNC_TICKS = -1;
        TIME_GETTER = Optional::empty;
        FRAME_MANAGER = requireNonNull(frameManager, "Frame manager cannot be null");
    }

    /**
     * Gets the animation listeners for this component.
     * @return the listeners for this component
     */
    @Override
    public Stream<TextureListener> getListeners() {
        TextureListener tickListener =
                new TextureListener(TextureListener.Type.TICK, (state) -> {
                    Optional<Long> timeOptional = TIME_GETTER.get();
                    requireNonNull(timeOptional, "Timer getter cannot supply null");

                    if (timeOptional.isPresent()) {
                        long currentTime = timeOptional.get();
                        int ticksToAdd = (int) Math.floorMod(currentTime - ticks, SYNC_TICKS);

                        ticks += ticksToAdd;
                        FRAME_MANAGER.tick(ticksToAdd);
                    } else {
                        ticks++;
                        FRAME_MANAGER.tick();
                    }

                    state.markNeedsUpload();
                });

        TextureListener uploadListener =
                new TextureListener(TextureListener.Type.UPLOAD, (state) ->
                        state.replaceImage(FRAME_MANAGER.getCurrentFrame()));

        return Stream.of(tickListener, uploadListener);
    }

}
