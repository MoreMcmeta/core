/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import io.github.moremcmeta.moremcmeta.api.math.NegativeDimensionException;
import org.jetbrains.annotations.ApiStatus;

/**
 * A GUI scaling type supported by vanilla Minecraft.
 * @author soir20
 * @since 4.4.0
 */
@ApiStatus.NonExtendable
public interface GuiScaling {

    /**
     * Name of the scaling type as would be found in an .mcmeta file.
     * @return name of the scaling type
     */
    String name();

    /**
     * GUI scaling type that stretches the sprite across the space.
     * @author soir20
     */
    final class Stretch implements GuiScaling {
        @Override
        public boolean equals(Object other) {
            return other instanceof Stretch;
        }

        @Override
        public String name() {
            return "stretch";
        }
    }

    /**
     * GUI scaling type that tiles the sprite across the space.
     * @author soir20
     */
    final class Tile implements GuiScaling {
        @Override
        public boolean equals(Object other) {
            return other instanceof Tile;
        }

        @Override
        public String name() {
            return "tile";
        }
    }

    /**
     * GUI scaling type that splits the sprite into 4 corner slices, 4 edge slices, and 1 center slice.
     * The center slice will be tiled across the space.
     * @author soir20
     */
    final class NineSlice implements GuiScaling {
        private final int LEFT;
        private final int TOP;
        private final int RIGHT;
        private final int BOTTOM;

        /**
         * Creates a new nine slice GUI setting.
         * @param left      left border size
         * @param right     right border size
         * @param top       top border size
         * @param bottom    bottom border size
         */
        public NineSlice(int left, int right, int top, int bottom) {
            if (left < 0) {
                throw new NegativeDimensionException(left);
            }

            if (right < 0) {
                throw new NegativeDimensionException(right);
            }

            if (top < 0) {
                throw new NegativeDimensionException(top);
            }

            if (bottom < 0) {
                throw new NegativeDimensionException(bottom);
            }

            LEFT = left;
            RIGHT = right;
            TOP = top;
            BOTTOM = bottom;
        }

        /**
         * Gets the left border size.
         * @return left border size
         */
        public int left() {
            return LEFT;
        }

        /**
         * Gets the right border size.
         * @return right border size
         */
        public int right() {
            return RIGHT;
        }

        /**
         * Gets the top border size.
         * @return top border size
         */
        public int top() {
            return TOP;
        }

        /**
         * Gets the bottom border size.
         * @return bottom border size
         */
        public int bottom() {
            return BOTTOM;
        }

        /**
         * Checks whether the border sizes leave room for a center slice, based on the given frame size.
         * @param frameWidth        width of the frame
         * @param frameHeight       height of the frame
         * @return whether the borders leave enough room for a center slice
         */
        public boolean hasCenterSlice(int frameWidth, int frameHeight) {
            if (frameWidth < 0) {
                throw new NegativeDimensionException(frameWidth);
            }

            if (frameHeight < 0) {
                throw new NegativeDimensionException(frameHeight);
            }

            return left() + right() < frameWidth && top() + bottom() < frameHeight;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof NineSlice)) {
                return false;
            }

            NineSlice otherSlice = (NineSlice) other;
            return left() == otherSlice.left()
                    && right() == otherSlice.right()
                    && top() == otherSlice.top()
                    && bottom() == otherSlice.bottom();
        }

        @Override
        public String name() {
            return "nine_slice";
        }
    }

}
