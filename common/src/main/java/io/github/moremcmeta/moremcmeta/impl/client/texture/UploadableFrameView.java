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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import io.github.moremcmeta.moremcmeta.api.client.texture.FrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.IllegalFrameReferenceException;
import io.github.moremcmeta.moremcmeta.api.client.texture.NegativeUploadPointException;

/**
 * A {@link FrameView} that represents a frame that can be uploaded.
 * @author soir20
 * @since 4.0.0
 */
public interface UploadableFrameView extends FrameView {

    /**
     * Uploads the current frame to the texture currently bound in OpenGL.
     * @param x         x-coordinate of the point to upload to
     * @param y         y-coordinate of the point to upload to
     * @param mipmap    number of mipmaps to upload (the mipmap level of the base texture)
     * @throws IllegalFrameReferenceException if this view is no longer valid
     * @throws NegativeUploadPointException if the provided upload point is negative. The upload point may
     *                                      still be positive and out of bounds even if no exception is
     *                                      thrown.
     */
    void upload(int x, int y, int mipmap);

}
