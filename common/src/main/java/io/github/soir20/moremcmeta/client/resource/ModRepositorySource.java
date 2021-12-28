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

import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ModRepositorySource implements RepositorySource {
    private final Map<ResourceLocation, EventDrivenTexture.Builder> TEXTURES;

    public ModRepositorySource() {
        TEXTURES = new HashMap<>();
    }

    public void setTextures(Map<ResourceLocation, EventDrivenTexture.Builder> newTextures) {
        reload();
        TEXTURES.putAll(newTextures);
    }

    public void reload() {
        TEXTURES.clear();
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor packConstructor) {
        Pack pack = new Pack(
                "__moremcmeta-internal__",
                true,
                () -> new DisableVanillaSpriteAnimationPack(TEXTURES),
                new TextComponent("MoreMcmeta Internal"),
                new TextComponent("Used by the MoreMcmeta mod. Cannot be moved."),
                PackCompatibility.COMPATIBLE,
                Pack.Position.TOP,
                true,
                PackSource.BUILT_IN
        );

        consumer.accept(pack);
    }
}
