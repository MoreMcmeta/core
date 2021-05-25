package io.github.soir20.moremcmeta.client.renderer.texture;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Searches atlas textures for sprites.
 */
public class SpriteFinder {
    private static final ImmutableSet<ResourceLocation> ATLAS_LOCATIONS = ImmutableSet.of();

    private final ITextureManager TEXTURE_MANAGER;
    private final Map<ResourceLocation, TextureAtlasSprite> CACHE;

    /**
     * Creates a new atlas searcher.
     * @param texManager    texture manager to get atlases from
     */
    public SpriteFinder(ITextureManager texManager) {
        TEXTURE_MANAGER = texManager;
        CACHE = new HashMap<>();
    }

    /**
     * Finds an atlas associated with a texture location.
     * @param location          the location of the texture
     * @return the atlas or null if the given location is not an atlas sprite
     */
    @Nullable
    public TextureAtlasSprite findSprite(ResourceLocation location) {
        if (!CACHE.containsKey(location)) {
            CACHE.put(location, findNew(location));
        }

        return CACHE.get(location);
    }

    /**
     * Finds an atlas associated with a texture location not yet cached.
     * @param location      the location of the texture to look for (with extension)
     * @return the atlas or null if the given location is not an atlas sprite
     */
    @Nullable
    private TextureAtlasSprite findNew(ResourceLocation location) {

        // Atlases store sprites without their extension
        ResourceLocation pathWithoutExtension = removeExtension(location);

        for (ResourceLocation atlasLocation : ATLAS_LOCATIONS) {
            AbstractTexture atlas = TEXTURE_MANAGER.getTexture(atlasLocation);

            // We should never have to skip here, unless another mod has modified the atlases
            if (!(atlas instanceof TextureAtlas)) {
                continue;
            }

            TextureAtlas typedAtlas = (TextureAtlas) atlas;
            TextureAtlasSprite sprite = (typedAtlas).getSprite(pathWithoutExtension);
            if (sprite.getName() != MissingTextureAtlasSprite.getLocation()) {
                return sprite;
            }
        }

        return null;
    }

    /**
     * Removes the extension from a texture location.
     * @param location      the location to remove the extension from
     * @return that location without its extension
     */
    private ResourceLocation removeExtension(ResourceLocation location) {
        String originalPath = location.getPath();
        String cutPath = originalPath.substring(0, originalPath.lastIndexOf('.'));
        return new ResourceLocation(location.getPath(), cutPath);
    }

}
