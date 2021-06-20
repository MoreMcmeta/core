package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Finishes event-driven textures with upload components. Textures should not be finished until
 * all atlas stitching has completed.
 * @author soir20
 */
public class TextureFinisher
        implements IFinisher<EventDrivenTexture.Builder<NativeImageFrame>, EventDrivenTexture<NativeImageFrame>> {
    private final ArrayDeque<Pair<ResourceLocation, EventDrivenTexture.Builder<NativeImageFrame>>> QUEUED_BUILDERS;
    private final SpriteFinder SPRITE_FINDER;

    /**
     * Creates a new finisher for event-driven textures.
     * @param spriteFinder      finder for atlas sprites
     */
    public TextureFinisher(SpriteFinder spriteFinder) {
        QUEUED_BUILDERS = new ArrayDeque<>();
        SPRITE_FINDER = requireNonNull(spriteFinder);
    }

    /**
     * Queues a texture that needs to be finished with an upload component.
     * @param location      texture location
     * @param builder       texture builder
     */
    @Override
    public void queue(ResourceLocation location, EventDrivenTexture.Builder<NativeImageFrame> builder) {
        requireNonNull(location, "Location cannot be null");
        requireNonNull(builder, "Texture builder cannot be null");

        QUEUED_BUILDERS.add(new Pair<>(location, builder));
    }

    /**
     * Finishes all currently-queued textures.
     * @return a map of all textures
     */
    @Override
    public Map<ResourceLocation, EventDrivenTexture<NativeImageFrame>> finish() {
        Map<ResourceLocation, EventDrivenTexture<NativeImageFrame>> builtTextures = new HashMap<>();

        while (!QUEUED_BUILDERS.isEmpty()) {
            Pair<ResourceLocation, EventDrivenTexture.Builder<NativeImageFrame>> pair = QUEUED_BUILDERS.remove();
            ResourceLocation location = pair.getFirst();
            EventDrivenTexture<NativeImageFrame> texture = finishOne(location, pair.getSecond());
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
    private EventDrivenTexture<NativeImageFrame> finishOne(ResourceLocation location,
                                                           EventDrivenTexture.Builder<NativeImageFrame> builder) {
        Optional<TextureAtlasSprite> sprite = SPRITE_FINDER.findSprite(location);
        if (sprite.isPresent()) {
            builder.add(new SpriteUploadComponent(sprite.get()));
        } else {
            builder.add(new SingleUploadComponent());
        }

        return builder.build();
    }

}
