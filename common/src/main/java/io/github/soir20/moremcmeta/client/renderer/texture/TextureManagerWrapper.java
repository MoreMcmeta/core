package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Wraps the {@link TextureManager} because it is not immediately available during mod construction.
 * @author soir20
 */
public class TextureManagerWrapper implements ITextureManager {
    private final Supplier<TextureManager> TEXTURE_MANAGER_GETTER;
    private final Map<ResourceLocation, Tickable> ANIMATED_TEXTURES;
    private final ArrayDeque<Pair<ResourceLocation, EventDrivenTexture.Builder<NativeImageFrame>>>
            QUEUED_TEXTURES;

    /**
     * Creates the TextureManagerWrapper.
     * @param texManagerGetter      getter for the texture manager. The manager may not exist during parallel
     *                              mod loading, but it will when resources are reloaded.
     */
    public TextureManagerWrapper(Supplier<TextureManager> texManagerGetter) {
        requireNonNull(texManagerGetter, "Texture manager getter cannot be null");
        TEXTURE_MANAGER_GETTER = texManagerGetter;
        ANIMATED_TEXTURES = new HashMap<>();
        QUEUED_TEXTURES = new ArrayDeque<>();
    }

    /**
     * Prepares a texture and makes Minecraft aware of it.
     * @param textureLocation   file location of texture identical to how it is used in a entity/gui/map
     * @param textureObj        the actual texture that should be used (atlas or otherwise)
     */
    public void loadTexture(ResourceLocation textureLocation, AbstractTexture textureObj) {
        requireNonNull(textureLocation, "Texture location cannot be null");
        requireNonNull(textureObj, "Texture cannot be null");

        TextureManager textureManager = TEXTURE_MANAGER_GETTER.get();
        requireNonNull(textureManager, "Supplied texture manager cannot be null");

        textureManager.register(textureLocation, textureObj);

        // Update tickables list
        ANIMATED_TEXTURES.remove(textureLocation);
        if (textureObj instanceof Tickable) {
            ANIMATED_TEXTURES.put(textureLocation, (Tickable) textureObj);
        }

    }

    /**
     * Gets a texture that is already loaded into this texture manager.
     * @param textureLocation           the location of the texture
     * @return the texture at that location or an exception if not found
     */
    @Override
    public AbstractTexture getTexture(ResourceLocation textureLocation) {
        return TEXTURE_MANAGER_GETTER.get().getTexture(textureLocation);
    }

    /**
     * Deletes a texture so Minecraft is no longer aware of it. This also allows the texture to be replaced.
     * @param textureLocation   file location of texture to delete
     */
    @Override
    public void deleteTexture(ResourceLocation textureLocation) {
        requireNonNull(textureLocation, "Texture location cannot be null");
        TEXTURE_MANAGER_GETTER.get().release(textureLocation);
    }

    /**
     * Updates all animated textures that were loaded through this manager.
     */
    @Override
    public void tick() {
        while (!QUEUED_TEXTURES.isEmpty()) {
            Pair<ResourceLocation, EventDrivenTexture.Builder<NativeImageFrame>> queueEntry =
                    QUEUED_TEXTURES.pop();
            ResourceLocation location = queueEntry.getFirst();
            loadTexture(location, queueEntry.getSecond().build());
        }

        ANIMATED_TEXTURES.values().forEach(Tickable::tick);
    }

}
