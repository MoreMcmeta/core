package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.ResourceLocation;

/**
 * A container for textures that can be added and removed. It represents what textures Minecraft is aware of.
 * @author soir20
 */
public interface ITextureManager {

    /**
     * Prepares a texture and makes Minecraft aware of it.
     * @param textureLocation   file location of texture identical to how it is used in a entity/gui/map
     * @param textureObj        the actual texture that should be used (atlas or otherwise)
     */
    void loadTexture(ResourceLocation textureLocation, Texture textureObj);

    /**
     * Deletes a texture so Minecraft is no longer aware of it. This also allows the texture to be replaced.
     * @param textureLocation   file location of texture to delete
     */
    void deleteTexture(ResourceLocation textureLocation);

}
