package io.github.soir20.moremcmeta.client.renderer.texture;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Searches atlas textures for sprites.
 * @author soir20
 */
public class SpriteFinder {
    private static final ImmutableSet<ResourceLocation> ATLAS_LOCATIONS = ImmutableSet.of(
            new ResourceLocation("textures/atlas/blocks.png"),
            new ResourceLocation("textures/atlas/signs.png"),
            new ResourceLocation("textures/atlas/banner_patterns.png"),
            new ResourceLocation("textures/atlas/shield_patterns.png"),
            new ResourceLocation("textures/atlas/chest.png"),
            new ResourceLocation("textures/atlas/beds.png"),
            new ResourceLocation("textures/atlas/particles.png"),
            new ResourceLocation("textures/atlas/paintings.png"),
            new ResourceLocation("textures/atlas/mob_effects.png")
    );

    private final Supplier<TextureManager> MANAGER_GETTER;

    /**
     * Creates a new sprite finder.
     * @param textureManagerGetter  provides a texture manager
     */
    public SpriteFinder(Supplier<TextureManager> textureManagerGetter) {
        MANAGER_GETTER = requireNonNull(textureManagerGetter, "Texture manager getter cannot be null");
    }

    /**
     * Finds an atlas associated with a texture location.
     * @param location          the location of the texture
     * @return the atlas or null if the given location is not an atlas sprite
     */
    public Optional<TextureAtlasSprite> findSprite(ResourceLocation location) {
        requireNonNull(location, "Location cannot be null");

        TextureAtlasSprite sprite = findNew(location);
        return sprite == null ? Optional.empty() : Optional.of(sprite);
    }

    /**
     * Finds an atlas associated with a texture location not yet cached.
     * @param location      the location of the texture to look for (with extension)
     * @return the atlas or null if the given location is not an atlas sprite
     */
    @Nullable
    private TextureAtlasSprite findNew(ResourceLocation location) {
        TextureManager manager = requireNonNull(MANAGER_GETTER.get(), "Null was supplied as a texture manager");

        // Atlases store sprites without their extension
        ResourceLocation pathWithoutExtension = makeSpritePath(location);

        for (ResourceLocation atlasLocation : ATLAS_LOCATIONS) {
            AbstractTexture atlas = manager.getTexture(atlasLocation);

            // We should never have to skip here, unless another mod has modified the atlases
            if (!(atlas instanceof TextureAtlas)) {
                continue;
            }

            TextureAtlas typedAtlas = (TextureAtlas) atlas;
            TextureAtlasSprite sprite = typedAtlas.getSprite(pathWithoutExtension);
            if (sprite != null && sprite.getName() != MissingTextureAtlasSprite.getLocation()) {
                return sprite;
            }
        }

        return null;
    }

    /**
     * Removes the extension and first directory from a texture location.
     * @param location      the location to remove the extension from
     * @return that location as a path to a sprite in a texture atlas
     */
    private ResourceLocation makeSpritePath(ResourceLocation location) {
        String originalPath = location.getPath();
        String cutPath = originalPath.substring(originalPath.indexOf('/') + 1,
                originalPath.lastIndexOf('.'));
        return new ResourceLocation(location.getNamespace(), cutPath);
    }

}
