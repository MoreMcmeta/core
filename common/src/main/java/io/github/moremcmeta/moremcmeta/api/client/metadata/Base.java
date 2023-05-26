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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import io.github.moremcmeta.moremcmeta.api.client.texture.NegativeUploadPointException;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import net.minecraft.resources.ResourceLocation;

import static java.util.Objects.requireNonNull;

/**
 * <p>Represents a "base" texture to which another texture is uploaded. For example, some file formats
 * may support creating an individual animation that is uploaded to a larger texture. The larger
 * texture would be the base.</p>
 *
 * <p>MoreMcmeta automatically adds bases to upload frames to the texture with the same name and
 * any sprites with the same name. For example, if the texture is <i>creeper.png</i>, there is no need
 * to define <i>creeper.png</i> as a base in the metadata. Including <i>creeper.png</i> should have no
 * impact on the visual result, but it may impact performance.</p>
 * @author soir20
 * @since 4.0.0
 */
public final class Base {
    private final ResourceLocation BASE_LOCATION;
    private final long UPLOAD_POINT;

    /**
     * Creates a new base.
     * @param baseTextureLocation       full path to the base texture (not a sprite name)
     * @param uploadPoint               coordinate in the base texture at which the top-left corner
     *                                  of the dependency will be uploaded
     */
    public Base(ResourceLocation baseTextureLocation, long uploadPoint) {
        BASE_LOCATION = requireNonNull(baseTextureLocation, "Base texture location cannot be null");
        UPLOAD_POINT = uploadPoint;

        int uploadX = Point.x(uploadPoint);
        int uploadY = Point.y(uploadPoint);
        if (uploadX < 0 || uploadY < 0) {
            throw new NegativeUploadPointException(uploadX, uploadY);
        }
    }

    /**
     * Gets the path of the base texture.
     * @return path of the base texture
     */
    public ResourceLocation baseLocation() {
        return BASE_LOCATION;
    }

    /**
     * Gets the coordinate in the base texture at which the top-left corner of the dependency
     * will be uploaded.
     * @return coordinate in the base texture at which the top-left corner of the dependency
     *         will be uploaded.
     */
    public long uploadPoint() {
        return UPLOAD_POINT;
    }

}
