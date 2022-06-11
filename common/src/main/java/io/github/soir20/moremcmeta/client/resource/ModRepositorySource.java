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

package io.github.soir20.moremcmeta.client.resource;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Supplies the {@link Pack} (and thus {@link PackResources}) added by this mod.
 * @author soir20
 */
public class ModRepositorySource implements RepositorySource {
    private final Supplier<PackResources> PACK_GETTER;

    /**
     * Creates a new resource pack repository.
     * @param packGetter        creates the resource pack added by this mod on each reload
     */
    public ModRepositorySource(Supplier<PackResources> packGetter) {
        PACK_GETTER = requireNonNull(packGetter, "Pack getter cannot be null");
    }

    /**
     * Loads the pack added by this mod.
     * @param consumer          consumer that accepts the pack
     * @param packConstructor   constructor to create a pack with default settings
     */
    @Override
    public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor packConstructor) {
        requireNonNull(consumer, "Pack consumer cannot be null");
        requireNonNull(packConstructor, "Pack constructor cannot be null");

        Pack pack = new Pack(
                getPackId(),
                true,
                PACK_GETTER,
                Component.literal("MoreMcmeta Internal"),
                Component.literal("Used by the MoreMcmeta mod. Cannot be moved."),
                PackCompatibility.COMPATIBLE,
                Pack.Position.TOP,
                true,
                PackSource.BUILT_IN
        );

        consumer.accept(pack);
    }

    /**
     * Gets the ID used as the unique identifier for the mod's resource pack/
     * @return the mod's resource pack ID
     */
    public static String getPackId() {
        return "__moremcmeta-internal__";
    }

}
