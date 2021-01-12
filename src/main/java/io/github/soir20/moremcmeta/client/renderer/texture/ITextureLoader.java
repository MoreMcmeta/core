package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.ResourceLocation;

/**
 * Stipulates the minimum {@link net.minecraft.client.renderer.texture.TextureManager} methods that
 * {@link io.github.soir20.moremcmeta.resource.TextureReloadListener} uses to allow for unit testing.
 * @author soir20
 */
public interface ITextureLoader {

    /**
     * Prepares a texture and makes Minecraft aware of it.
     * @param textureLocation   file location of texture identical to how it is used in a entity/gui/map
     * @param textureObj        the actual texture that should be used (atlas or otherwise)
     */
    void loadTexture(ResourceLocation textureLocation, Texture textureObj);

}
