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

package io.github.soir20.moremcmeta.api.client.metadata;

import java.util.Optional;

public interface ParsedMetadata {
    default Optional<FrameSize> frameSize() {
        return Optional.empty();
    }

    default Optional<Boolean> blur() {
        return Optional.empty();
    }

    default Optional<Boolean> clamp() {
        return Optional.empty();
    }

    default Optional<String> invalidReason() {
        return Optional.empty();
    }

    final class FrameSize {
        private final int WIDTH;
        private final int HEIGHT;

        public FrameSize(int width, int height) {
            WIDTH = width;
            HEIGHT = height;
        }

        public int width() {
            return WIDTH;
        }

        public int height() {
            return HEIGHT;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof FrameSize otherSize)) {
                return false;
            }

            return width() == otherSize.width() && height() == otherSize.height();
        }

        @Override
        public int hashCode() {
            return 31 * WIDTH + HEIGHT;
        }
    }
}
