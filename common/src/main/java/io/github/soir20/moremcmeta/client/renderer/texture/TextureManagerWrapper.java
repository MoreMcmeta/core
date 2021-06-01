package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Wraps the {@link TextureManager} because it is not immediately available during mod construction.
 * @author soir20
 */
public class TextureManagerWrapper implements IManager<EventDrivenTexture.Builder<NativeImageFrame>> {
    private final Supplier<TextureManager> TEXTURE_MANAGER_GETTER;
    private final Map<ResourceLocation, CustomTickable> ANIMATED_TEXTURES;
    private final TextureFinisher FINISHER;

    /**
     * Creates the TextureManagerWrapper.
     * @param texManagerGetter      getter for the texture manager. The manager may not exist during parallel
     *                              mod loading, but it will when resources are reloaded.
     */
    public TextureManagerWrapper(Supplier<TextureManager> texManagerGetter) {
        requireNonNull(texManagerGetter, "Texture manager getter cannot be null");
        TEXTURE_MANAGER_GETTER = texManagerGetter;
        ANIMATED_TEXTURES = new HashMap<>();
        FINISHER = new TextureFinisher();
    }

    /**
     * Finishes building a texture and makes Minecraft aware of it.
     * @param textureLocation   file location of texture identical to how it is used in a entity/gui/map
     * @param builder           the actual texture that should be used (atlas or otherwise)
     */
    @Override
    public void register(ResourceLocation textureLocation,
                         EventDrivenTexture.Builder<NativeImageFrame> builder) {
        requireNonNull(textureLocation, "Texture location cannot be null");
        requireNonNull(builder, "Texture builder cannot be null");

        /* Clear any existing texture immediately to prevent PreloadedTextures
           from re-adding themselves. The texture manager will reload before the
           EventDrivenTextures are added, causing a race condition with the
           registration CompletableFuture inside PreloadedTexture's reset method. */
        TextureManager textureManager = TEXTURE_MANAGER_GETTER.get();
        requireNonNull(textureManager, "Supplied texture manager cannot be null");
        textureManager.release(textureLocation);

        FINISHER.queue(textureLocation, builder);
    }

    /**
     * Finishes all queued textures by adding them to Minecraft's texture manager.
     */
    public void finishQueued() {
        TextureManager textureManager = TEXTURE_MANAGER_GETTER.get();
        requireNonNull(textureManager, "Supplied texture manager cannot be null");

        Map<ResourceLocation, EventDrivenTexture<NativeImageFrame>> textures = FINISHER.finish();

        textures.forEach((location, texture) -> {
            textureManager.register(location, texture);
            ANIMATED_TEXTURES.put(location, texture);
        });
    }

    /**
     * Deletes a texture so Minecraft is no longer aware of it. This also allows the texture to be replaced.
     * @param textureLocation   file location of texture to delete
     */
    @Override
    public void unregister(ResourceLocation textureLocation) {
        requireNonNull(textureLocation, "Texture location cannot be null");

        TextureManager textureManager = TEXTURE_MANAGER_GETTER.get();
        requireNonNull(textureManager, "Supplied texture manager cannot be null");

        textureManager.release(textureLocation);
        ANIMATED_TEXTURES.remove(textureLocation);
    }

    /**
     * Updates all animated textures that were loaded through this manager.
     */
    @Override
    public void tick() {
        ANIMATED_TEXTURES.values().forEach(CustomTickable::tick);
    }

}
