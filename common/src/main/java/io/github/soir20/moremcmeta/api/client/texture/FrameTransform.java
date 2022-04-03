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

package io.github.soir20.moremcmeta.api.client.texture;

import io.github.soir20.moremcmeta.api.math.Point;

import static java.util.Objects.requireNonNull;

public final class FrameTransform {
    private final ColorTransform TRANSFORM;
    private final Iterable<Point> APPLY_AREA;

    public FrameTransform(ColorTransform transform, Iterable<Point> applyArea) {
        TRANSFORM = requireNonNull(transform, "Transform cannot be null");
        APPLY_AREA = requireNonNull(applyArea, "Apply area cannot be null");
    }

    public ColorTransform transform() {
        return TRANSFORM;
    }

    public Iterable<Point> applyArea() {
        return APPLY_AREA;
    }

}