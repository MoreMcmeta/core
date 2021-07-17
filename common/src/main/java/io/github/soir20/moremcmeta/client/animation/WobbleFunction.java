/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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

package io.github.soir20.moremcmeta.client.animation;

import net.minecraft.util.Mth;

import java.util.Random;

/**
 * Calculates transitions daytime-synced animations to the current time.
 * @author soir20
 */
public class WobbleFunction {
    private static final long TICKS_PER_MC_DAY = 24000;
    private final Random RANDOM_GENERATOR;

    private double lastPercent;
    private double lastDelta;
    private long lastUpdateTick;

    /**
     * Creates a function with the default seed.
     */
    public WobbleFunction() {
        RANDOM_GENERATOR = new Random();
    }

    /**
     * Creates a function with a particular seed.
     * @param seed      the seed to use for random values
     */
    public WobbleFunction(long seed) {
        RANDOM_GENERATOR = new Random(seed);
    }

    /**
     * Calculates the tick that an animation should sync to. Note that this is *not* a pure function.
     * The calculation is identical for all textures at a particular game time tick.
     * @param dayTime               the current game time in ticks. Does *not* have to be
     *                              between 0 and 24,000.
     * @param gameTime              the current game time in ticks
     * @param isDimensionNatural    whether the dimension has normal time (like the Overworld
     *                              but not the Nether)
     * @return the tick (out of 24,000) that a daytime-synced animation should be at for its next frame
     */
    public long calculate(long dayTime, long gameTime, boolean isDimensionNatural) {
        if (gameTime != lastUpdateTick) {
            lastUpdateTick = gameTime;

            double timePercent = Math.floorMod(dayTime, TICKS_PER_MC_DAY) / (double) TICKS_PER_MC_DAY;
            if (!isDimensionNatural) {
                timePercent = RANDOM_GENERATOR.nextDouble();
            }

            wobble(timePercent);
        }

        return Math.round(lastPercent * TICKS_PER_MC_DAY);
    }

    /**
     * Calculates a percent of the Minecraft day that the next frame should be at.
     * {@link #lastPercent} is updated with the result. The percentages become
     * successively closer to the desired time over multiple calculations.
     * @param timePercent       the current percent or fraction of the day that has based,
     *                          starting when day time == 0. Must be between 0 and 1.
     */
    private void wobble(double timePercent) {
        double delta = timePercent - lastPercent;
        delta = Mth.positiveModulo(delta + 0.5, 1) - 0.5;
        lastDelta += delta * 0.1;
        lastDelta *= 0.9;

        lastPercent = Mth.positiveModulo(lastPercent + lastDelta, 1);
    }

}
