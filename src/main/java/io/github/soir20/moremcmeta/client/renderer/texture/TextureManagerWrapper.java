package io.github.soir20.moremcmeta.client.renderer.texture;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TextureManagerWrapper implements ITextureLoader {
    private final TextureManager TEXTURE_MANAGER;

    public TextureManagerWrapper(TextureManager texManager) {
        TEXTURE_MANAGER = texManager;
    }

    @Override
    public void loadTexture(ResourceLocation textureLocation, Texture textureObj) {
        TEXTURE_MANAGER.loadTexture(textureLocation, textureObj);
    }
}
