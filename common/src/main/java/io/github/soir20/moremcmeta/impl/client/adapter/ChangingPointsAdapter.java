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

package io.github.soir20.moremcmeta.impl.client.adapter;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.soir20.moremcmeta.impl.client.io.ChangingPointsReader;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImage;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Finds the pixels that change during an animation for an {@link NativeImage}.
 * @author soir20
 */
public class ChangingPointsAdapter {
    private final ChangingPointsReader READER;

    /**
     * Creates a new changing points reader that works with {@link NativeImage}s.
     */
    public ChangingPointsAdapter() {
        READER = new ChangingPointsReader();
    }

    /**
     * Gets the pixels that will change for every mipmap.
     * @param image         the original image to analyze
     * @param frameWidth    the width of a frame
     * @param frameHeight   the height of a frame
     * @param mipmap        number of mipmap levels to use
     * @return  pixels that change for every mipmap (starting with the default image)
     */
    public List<CloseableImage.VisibleArea> read(NativeImage image, int frameWidth, int frameHeight, int mipmap) {
        requireNonNull(image, "Image cannot be null");
        CloseableImage wrappedImage = new NativeImageAdapter(image, mipmap);
        return READER.read(wrappedImage, frameWidth, frameHeight, mipmap);
    }

}
