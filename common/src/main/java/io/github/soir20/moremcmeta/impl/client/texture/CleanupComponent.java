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

package io.github.soir20.moremcmeta.impl.client.texture;

import static java.util.Objects.requireNonNull;

/**
 * Cleans up images when an {@link EventDrivenTexture} closes.
 * @author soir20
 */
public class CleanupComponent implements CoreTextureComponent {
    private final Runnable CLOSE_ACTION;

    /**
     * Creates a cleanup component for a texture.
     * @param closeAction       action to clean up all native image resources
     */
    public CleanupComponent(Runnable closeAction) {
        CLOSE_ACTION = requireNonNull(closeAction, "Close action cannot be null");
    }

    /**
     * Cleans up resources associated with the texture when it is closed.
     * @param currentFrame      view of the texture's current frame
     */
    @Override
    public void onClose(EventDrivenTexture.TextureAndFrameView currentFrame) {
        CLOSE_ACTION.run();
    }

}
