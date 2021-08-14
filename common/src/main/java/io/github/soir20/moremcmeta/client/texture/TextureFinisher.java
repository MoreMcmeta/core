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

package io.github.soir20.moremcmeta.client.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Finishes event-driven textures with upload components. Textures should not be finished until
 * all atlas stitching has completed.
 * @author soir20
 */
public class TextureFinisher implements IFinisher<EventDrivenTexture.Builder, EventDrivenTexture> {
    private final ArrayDeque<Pair<ResourceLocation, EventDrivenTexture.Builder>> QUEUED_BUILDERS;
    private final SpriteFinder SPRITE_FINDER;
    private final ITexturePreparer PREPARER;

    /**
     * Creates a new finisher for event-driven textures.
     * @param spriteFinder      finder for atlas sprites
     * @param preparer          prepares independent textures for OpenGL when they are registered
     */
    public TextureFinisher(SpriteFinder spriteFinder, ITexturePreparer preparer) {
        QUEUED_BUILDERS = new ArrayDeque<>();
        SPRITE_FINDER = requireNonNull(spriteFinder, "Sprite finder cannot be null");
        PREPARER = requireNonNull(preparer, "Preparer cannot be null");
    }

    /**
     * Queues a texture that needs to be finished with an upload component.
     * @param location      texture location
     * @param builder       texture builder
     */
    @Override
    public void queue(ResourceLocation location, EventDrivenTexture.Builder builder) {
        requireNonNull(location, "Location cannot be null");
        requireNonNull(builder, "Texture builder cannot be null");

        QUEUED_BUILDERS.add(new Pair<>(location, builder));
    }

    /**
     * Finishes all currently-queued textures.
     * @return a map of all textures
     */
    @Override
    public Map<ResourceLocation, EventDrivenTexture> finish() {
        Map<ResourceLocation, EventDrivenTexture> builtTextures = new HashMap<>();

        while (!QUEUED_BUILDERS.isEmpty()) {
            Pair<ResourceLocation, EventDrivenTexture.Builder> pair = QUEUED_BUILDERS.remove();
            ResourceLocation location = pair.getFirst();
            EventDrivenTexture texture = finishOne(location, pair.getSecond());
            builtTextures.put(location, texture);
        }

        return builtTextures;
    }

    /**
     * Finishes one queued texture.
     * @param location      location of the texture
     * @param builder       texture builder
     * @return the finished texture
     */
    private EventDrivenTexture finishOne(ResourceLocation location, EventDrivenTexture.Builder builder) {
        Optional<ISprite> sprite = SPRITE_FINDER.findSprite(location);
        if (sprite.isPresent()) {
            builder.add(new SpriteUploadComponent(sprite.get()));
        } else {
            builder.add(new SingleUploadComponent(PREPARER));
        }

        return builder.build();
    }

}
