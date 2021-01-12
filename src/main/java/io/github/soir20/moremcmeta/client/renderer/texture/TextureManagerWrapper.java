package io.github.soir20.moremcmeta.client.renderer.texture;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

/**
 * Wraps the {@link TextureManager} so that it implements the {@link ITextureLoader} interface and is compatible
 * with the reload listener.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TextureManagerWrapper implements ITextureLoader {
    private final Supplier<TextureManager> TEXTURE_MANAGER_GETTER;

    /**
     * Creates the TextureManagerWrapper.
     * @param texManagerGetter      getter for the texture manager. The manager may not exist during parallel
     *                              mod loading, but it will when resources are reloaded.
     */
    public TextureManagerWrapper(Supplier<TextureManager> texManagerGetter) {
        TEXTURE_MANAGER_GETTER = texManagerGetter;
    }

    /**
     * Prepares a texture and makes Minecraft aware of it.
     * @param textureLocation   file location of texture identical to how it is used in a entity/gui/map
     * @param textureObj        the actual texture that should be used (atlas or otherwise)
     */
    @Override
    public void loadTexture(ResourceLocation textureLocation, Texture textureObj) {
        TEXTURE_MANAGER_GETTER.get().loadTexture(textureLocation, textureObj);
    }

}
