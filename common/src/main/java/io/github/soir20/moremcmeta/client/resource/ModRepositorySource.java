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

import net.minecraft.WorldVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Supplies the {@link Pack} (and thus {@link PackResources}) added by this mod.
 * @author soir20
 */
public class ModRepositorySource implements RepositorySource {

    /**
     * The unique identifier for the mod's resource pack.
     */
    public static final String PACK_ID = "__moremcmeta-internal__";
    private final Pack.ResourcesSupplier PACK_GETTER;
    private final WorldVersion CURRENT_VERSION;

    /**
     * Creates a new resource pack repository.
     * @param packGetter        creates the resource pack added by this mod on each reload
     * @param currentVersion    current game version
     */
    public ModRepositorySource(Pack.ResourcesSupplier packGetter, WorldVersion currentVersion) {
        PACK_GETTER = requireNonNull(packGetter, "Pack getter cannot be null");
        CURRENT_VERSION = requireNonNull(currentVersion, "Current version cannot be null");
    }

    /**
     * Loads the pack added by this mod.
     * @param consumer          consumer that accepts the pack
     */
    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        requireNonNull(consumer, "Pack consumer cannot be null");

        int packVersion = CURRENT_VERSION.getPackVersion(PackType.CLIENT_RESOURCES);
        Pack pack = Pack.create(
                PACK_ID,
                Component.literal("MoreMcmeta Internal"),
                true,
                PACK_GETTER,
                new Pack.Info(
                        Component.literal("Used by the MoreMcmeta mod. Cannot be moved."),
                        packVersion, FeatureFlagSet.of()
                ),
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,
                true,
                PackSource.BUILT_IN
        );

        consumer.accept(pack);
    }

}
