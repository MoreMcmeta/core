package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.ResourceLocation;

public interface ITextureLoader {

    void loadTexture(ResourceLocation textureLocation, Texture textureObj);

}
