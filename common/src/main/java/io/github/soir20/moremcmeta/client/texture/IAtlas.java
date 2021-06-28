package io.github.soir20.moremcmeta.client.texture;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * An adapter for {@link net.minecraft.client.renderer.texture.TextureAtlas}
 * because it is difficult to instantiate in tests.
 * @author soir20
 */
@FunctionalInterface
public interface IAtlas {

    /**
     * Gets a sprite from this atlas if it is present.
     * @param location      the location of the sprite
     * @return the sprite at the given location if present
     */
    Optional<ISprite> getSprite(ResourceLocation location);

}
