package io.github.soir20.moremcmeta.client.renderer.texture;

import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.resources.ResourceLocation;

/**
 * An adapter for {@link net.minecraft.client.renderer.texture.TextureAtlasSprite}
 * to provide a cleaner interface and make it easier to instantiate in test code.
 */
public interface ISprite {

    /**
     * Binds the sprite (and thus its atlas) to OpenGL.
     */
    void bind();

    /**
     * Gets the name of this sprite (without an extension).
     * @return the sprite's name
     */
    ResourceLocation getName();

    /**
     * Gets the position of the sprite's top-left corner on its atlas.
     * @return the sprite's upload point
     */
    Point getUploadPoint();

}
