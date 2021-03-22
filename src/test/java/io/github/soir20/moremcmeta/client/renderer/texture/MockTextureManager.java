package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of the {@link ResourceLocation}s of textures that would have been added to a real texture manager.
 * @author soir20
 */
public class MockTextureManager implements ITextureManager {
    private final Map<ResourceLocation, Texture> TEXTURES;

    public MockTextureManager() {
        TEXTURES = new HashMap<>();
    }

    @Override
    public void loadTexture(ResourceLocation textureLocation, Texture textureObj) {
        TEXTURES.put(textureLocation, textureObj);
    }

    @Override
    public void deleteTexture(ResourceLocation textureLocation) {
        TEXTURES.remove(textureLocation);
    }

    public Texture getTexture(ResourceLocation textureLocation) {
        return TEXTURES.get(textureLocation);
    }

    public Set<ResourceLocation> getLocations() {
        return TEXTURES.keySet();
    }
}
