package io.github.soir20.moremcmeta.client.renderer.texture;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Wraps the {@link TextureManager} because it is not immediately available during mod construction.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TextureManagerWrapper implements ITextureManager {
    private final Supplier<TextureManager> TEXTURE_MANAGER_GETTER;
    private final Map<ResourceLocation, ITickable> ANIMATED_TEXTURES;

    /**
     * Creates the TextureManagerWrapper.
     * @param texManagerGetter      getter for the texture manager. The manager may not exist during parallel
     *                              mod loading, but it will when resources are reloaded.
     */
    public TextureManagerWrapper(Supplier<TextureManager> texManagerGetter) {
        requireNonNull(texManagerGetter, "Texture manager getter cannot be null");
        TEXTURE_MANAGER_GETTER = texManagerGetter;
        ANIMATED_TEXTURES = new HashMap<>();
    }

    /**
     * Prepares a texture and makes Minecraft aware of it.
     * @param textureLocation   file location of texture identical to how it is used in a entity/gui/map
     * @param textureObj        the actual texture that should be used (atlas or otherwise)
     */
    public void loadTexture(ResourceLocation textureLocation, Texture textureObj) {
        requireNonNull(textureLocation, "Texture location cannot be null");
        requireNonNull(textureObj, "Texture cannot be null");

        TextureManager textureManager = TEXTURE_MANAGER_GETTER.get();
        requireNonNull(textureManager, "Supplied texture manager cannot be null");

        textureManager.loadTexture(textureLocation, textureObj);

        // Update tickables list
        ANIMATED_TEXTURES.remove(textureLocation);
        if (textureObj instanceof ITickable) {
            ANIMATED_TEXTURES.put(textureLocation, (ITickable) textureObj);
        }

    }

    /**
     * Deletes a texture so Minecraft is no longer aware of it. This also allows the texture to be replaced.
     * @param textureLocation   file location of texture to delete
     */
    @Override
    public void deleteTexture(ResourceLocation textureLocation) {
        requireNonNull(textureLocation, "Texture location cannot be null");
        TEXTURE_MANAGER_GETTER.get().deleteTexture(textureLocation);
    }

    /**
     * Updates all animated textures that were loaded through this manager.
     */
    @Override
    public void tick() {
        ANIMATED_TEXTURES.values().forEach(ITickable::tick);
    }

}
