package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of the {@link ResourceLocation}s of textures that would have been added to a real texture manager.
 * @author soir20
 */
public class MockTextureManager implements ITextureManager {
    private final List<ResourceLocation> LOCATIONS;

    public MockTextureManager() {
        LOCATIONS = new ArrayList<>();
    }

    @Override
    public void loadTexture(ResourceLocation textureLocation, Texture textureObj) {
        LOCATIONS.add(textureLocation);
    }

    @Override
    public void deleteTexture(ResourceLocation textureLocation) {
        LOCATIONS.remove(textureLocation);
    }

    public List<ResourceLocation> getLocations() {
        return new ArrayList<>(LOCATIONS);
    }
}
