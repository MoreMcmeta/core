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

/**
 * Interpolates between two images.
 * @param <I>   type of image
 * @author soir20
 */
public interface Interpolator<I> {

    /**
     * Creates an image between two other images at a certain step.
     * @param steps     total number of steps to interpolate
     * @param step      current step of the interpolation (between 1 and steps - 1)
     * @param start     image to start interpolation from
     * @param end       image to end interpolation at
     * @return  the interpolated image at the given step
     */
    I interpolate(int steps, int step, I start, I end);

}
