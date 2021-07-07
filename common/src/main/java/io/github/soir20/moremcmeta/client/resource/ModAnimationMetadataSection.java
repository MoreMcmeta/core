/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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

package io.github.soir20.moremcmeta.client.resource;

import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

/**
 * Holds animation metadata that is added by MoreMcmeta and not in the vanilla
 * {@link net.minecraft.client.resources.metadata.animation.AnimationMetadataSection}.
 * @author soir20
 */
public class ModAnimationMetadataSection {
    public static final MetadataSectionSerializer<ModAnimationMetadataSection> SERIALIZER =
            new ModAnimationMetadataSectionSerializer();
    public static final ModAnimationMetadataSection EMPTY = new ModAnimationMetadataSection(false);

    private final boolean DAYTIME_SYNC;

    /**
     * Creates a new metadata holder.
     * @param daytimeSync       whether the animation should sync to the game time
     */
    public ModAnimationMetadataSection(boolean daytimeSync) {
        DAYTIME_SYNC = daytimeSync;
    }

    /**
     * Gets whether the animation should sync to the game time.
     * @return whether the animation should sync to the game time
     */
    public boolean isDaytimeSynced() {
        return DAYTIME_SYNC;
    }

}
