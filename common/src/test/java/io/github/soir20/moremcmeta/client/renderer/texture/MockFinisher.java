package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A fake {@link IFinisher} that turns integers to textures.
 * @author soir20
 */
public class MockFinisher implements IFinisher<Integer, MockAnimatedTexture> {
    private final Map<ResourceLocation, Integer> ITEMS;

    public MockFinisher() {
        ITEMS = new HashMap<>();
    }

    @Override
    public void queue(ResourceLocation location, Integer input) {
        ITEMS.put(location, input);
    }

    @Override
    public Map<ResourceLocation, MockAnimatedTexture> finish() {
        return ITEMS.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, entry -> new MockAnimatedTexture())
        );
    }

}
