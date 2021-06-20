package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A fake {@link IFinisher} that turns items to textures.
 * @param <I> input type
 * @author soir20
 */
public class MockFinisher<I> implements IFinisher<I, MockAnimatedTexture> {
    private final Map<ResourceLocation, I> ITEMS;

    public MockFinisher() {
        ITEMS = new HashMap<>();
    }

    @Override
    public void queue(ResourceLocation location, I input) {
        ITEMS.put(location, input);
    }

    @Override
    public Map<ResourceLocation, MockAnimatedTexture> finish() {
        return ITEMS.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, entry -> new MockAnimatedTexture())
        );
    }

}
