package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of the {@link ResourceLocation}s of textures that would have been added to a real texture manager.
 * @param <R> resource type
 * @author soir20
 */
public class MockManager<R> implements IManager<R> {
    private final Map<ResourceLocation, R> TEXTURES;
    private final Map<ResourceLocation, Tickable> ANIMATED_TEXTURES;

    public MockManager() {
        TEXTURES = new HashMap<>();
        ANIMATED_TEXTURES = new HashMap<>();
    }

    @Override
    public void register(ResourceLocation textureLocation, R textureObj) {
        TEXTURES.put(textureLocation, textureObj);
        ANIMATED_TEXTURES.remove(textureLocation);
        if (textureObj instanceof Tickable) {
            ANIMATED_TEXTURES.put(textureLocation, (Tickable) textureObj);
        }
    }

    @Override
    public void unregister(ResourceLocation textureLocation) {
        TEXTURES.remove(textureLocation);
    }

    public Set<ResourceLocation> getLocations() {
        return TEXTURES.keySet();
    }

    public void tick() {
        ANIMATED_TEXTURES.values().forEach(Tickable::tick);
    }
}
