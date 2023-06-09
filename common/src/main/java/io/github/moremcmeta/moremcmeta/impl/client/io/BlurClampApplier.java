/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2023 soir20
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

package io.github.moremcmeta.moremcmeta.impl.client.io;

/**
 * Applies blur and clamp to an image.
 * @param <I>   input image type
 * @param <O>   output image type
 * @author soir20
 */
public interface BlurClampApplier<I, O> {

    /**
     * Applies blur and clamp to an image. Implementations may return a new image or the same (modified) image.
     * @param image     image to apply blur and clamp to
     * @param blur      whether to blur the image
     * @param clamp     whether to clamp the image
     * @return a new image or the modified image
     */
    O apply(I image, boolean blur, boolean clamp);

}
