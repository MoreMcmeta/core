package io.github.soir20.moremcmeta.client.texture;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Function;

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

    private final Function<ResourceLocation, IAtlas> ATLAS_GETTER;

    /**
     * Creates a new sprite finder.
     * @param atlasGetter  provides an atlas from a location
     */
    public SpriteFinder(Function<ResourceLocation, IAtlas> atlasGetter) {
        ATLAS_GETTER = requireNonNull(atlasGetter, "Atlas getter cannot be null");
    }

    /**
     * Finds an atlas associated with a texture location.
     * @param location          the location of the texture
     * @return an {@link Optional} containing the atlas sprite
     */
    public Optional<ISprite> findSprite(ResourceLocation location) {
        requireNonNull(location, "Location cannot be null");

        return findNew(location);
    }

    /**
     * Finds an atlas associated with a texture location not yet cached.
     * @param location      the location of the texture to look for (with extension)
     * @return an {@link Optional} containing the atlas sprite
     */
    private Optional<ISprite> findNew(ResourceLocation location) {

        // Atlases store sprites without their extension
        ResourceLocation pathWithoutExtension = makeSpritePath(location);

        for (ResourceLocation atlasLocation : ATLAS_LOCATIONS) {
            IAtlas atlas = ATLAS_GETTER.apply(atlasLocation);
            requireNonNull(atlas, "Atlas getter cannot supply null");

            Optional<ISprite> sprite = atlas.getSprite(pathWithoutExtension);
            if (sprite.isPresent() && sprite.get().getName() == MissingTextureAtlasSprite.getLocation()) {
                return Optional.empty();
            } else if (sprite.isPresent()) {
                return sprite;
            }

        }

        return Optional.empty();
    }

    /**
     * Removes the extension and first directory from a texture location.
     * @param location      the location to remove the extension from
     * @return that location as a path to a sprite in a texture atlas
     */
    private ResourceLocation makeSpritePath(ResourceLocation location) {
        String originalPath = location.getPath();
        String cutPath = originalPath.substring(originalPath.indexOf('/') + 1);

        int extensionIndex = cutPath.lastIndexOf('.');
        if (extensionIndex >= 0) {
            cutPath = cutPath.substring(0, extensionIndex);
        }

        return new ResourceLocation(location.getNamespace(), cutPath);
    }

}
