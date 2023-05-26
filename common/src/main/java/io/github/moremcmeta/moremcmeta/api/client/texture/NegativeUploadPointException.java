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

package io.github.moremcmeta.moremcmeta.api.client.texture;

/**
 * Indicates that negative coordinates were used for an upload point.
 * @author soir20
 * @since 4.0.0
 */
public final class NegativeUploadPointException extends IllegalArgumentException {

    /**
     * Creates a new exception to indicate that negative coordinates were used for an upload point.
     * @param x x-coordinate of the point accessed
     * @param y y-coordinate of the point accessed
     */
    public NegativeUploadPointException(int x, int y) {
        super("Point (" + x + ", " + y + ") is negative and thus out of bounds");
    }

}
