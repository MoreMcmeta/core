package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An animated texture that an update on tick.
 * @author soir20
 */
@ParametersAreNonnullByDefault
public class AnimatedTexture extends Texture implements ITickable {
    private final TextureAtlasSprite SPRITE;

    /**
     * Creates a new AnimatedTexture.
     * @param location      file location of texture identical to how it is used in a entity/gui/map
     * @param width         texture width
     * @param height        texture height
     * @param metadata      animation metadata (.mcmeta information)
     * @param mipmapLevels  number of mipmap levels to use
     * @param nativeImage   native image corresponding to this texture
     */
    public AnimatedTexture(ResourceLocation location, int width, int height, AnimationMetadataSection metadata,
                           int mipmapLevels, NativeImage nativeImage) {
        TextureAtlasSprite.Info spriteInfo = new TextureAtlasSprite.Info(location, width, height, metadata);
        SPRITE = new SingleTextureSprite(SingleTextureSprite.EMPTY_ATLAS, spriteInfo, mipmapLevels,
                width, height, 0, 0, nativeImage);
    }

    /**
     * Uploads this texture to OpenGL on the appropriate thread.
     * @param manager   resource manager
     */
    @Override
    public void loadTexture(IResourceManager manager) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(this::loadImage);
        } else {
            loadImage();
        }
    }

    /**
     * Uploads this image to OpenGL immediately.
     */
    private void loadImage() {
        TextureUtil.prepareImage(getGlTextureId(), 0, SPRITE.getWidth(), SPRITE.getHeight());
        SPRITE.uploadMipmaps();
    }

    /**
     * Updates this texture's animation on the appropriate thread each tick.
     */
    @Override
    public void tick() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::updateAnimation);
        } else {
            updateAnimation();
        }
    }

    /**
     * Updates this texture's animation immediately.
     */
    private void updateAnimation() {
        bindTexture();
        SPRITE.updateAnimation();
    }

    /**
     * KEEP THIS CLASS PRIVATE. Unfortunately, TextureAtlasSprite's constructor is protected, and we can't
     * extract its animation code without copying large swaths of it. We need this class to get access to the
     * constructor without reflection.
     * @author soir20
     */
    private static class SingleTextureSprite extends TextureAtlasSprite {
        private static final AtlasTexture EMPTY_ATLAS = new AtlasTexture(
                new ResourceLocation("moremcmeta", "textures/atlas/empty.png")
        );

        /**
         * Creates a new sprite, which should not be connected to an atlas.
         * @param atlasTextureIn    pass {@link #EMPTY_ATLAS}
         * @param spriteInfoIn      sprite information section
         * @param mipmapLevelsIn    number of mipmap levels to use
         * @param atlasWidthIn      pass sprite width
         * @param atlasHeightIn     pass sprite height
         * @param xIn               pass 0 (no other textures to offset this sprite)
         * @param yIn               pass 0 (no other textures to offset this sprite)
         * @param imageIn           native image data associated with this sprite
         */
        public SingleTextureSprite(AtlasTexture atlasTextureIn, Info spriteInfoIn, int mipmapLevelsIn,
                                   int atlasWidthIn, int atlasHeightIn, int xIn, int yIn, NativeImage imageIn) {
            super(atlasTextureIn, spriteInfoIn, mipmapLevelsIn, atlasWidthIn, atlasHeightIn, xIn, yIn, imageIn);
        }
    }

}
