package io.github.soir20.moremcmeta.client.renderer.texture;

import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * A fake {@link TextureAtlasSprite}.
 * @author soir20
 */
public class MockAtlasSprite implements ISprite {
    private final ResourceLocation NAME;

    public MockAtlasSprite(ResourceLocation name) {
        NAME = name;
    }

    @Override
    public void bind() {}

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public Point getUploadPoint() {
        return new Point(1, 2);
    }

}
