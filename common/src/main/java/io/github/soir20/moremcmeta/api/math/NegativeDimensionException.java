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

package io.github.soir20.moremcmeta.api.math;

/**
 * Indicates that a negative integer was used to represent a dimension/measurement.
 * @author soir20
 * @since 4.0.0
 */
public final class NegativeDimensionException extends IllegalArgumentException {

    /**
     * Creates a new exception to indicate that a negative value was used for a dimension.
     * @param dimension     the negative value used
     */
    public NegativeDimensionException(int dimension) {
        super("Illegal negative dimension: " + dimension);
    }

}
