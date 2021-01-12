package io.github.soir20.moremcmeta.client.renderer.texture;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TextureManagerWrapper implements ITextureLoader {
    private final Supplier<TextureManager> TEXTURE_MANAGER_GETTER;

    public TextureManagerWrapper(Supplier<TextureManager> texManagerGetter) {
        TEXTURE_MANAGER_GETTER = texManagerGetter;
    }

    @Override
    public void loadTexture(ResourceLocation textureLocation, Texture textureObj) {
        TEXTURE_MANAGER_GETTER.get().loadTexture(textureLocation, textureObj);
    }
}
