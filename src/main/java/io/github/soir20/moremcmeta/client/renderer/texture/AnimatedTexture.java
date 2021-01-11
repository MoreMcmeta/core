package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AnimatedTexture extends Texture implements IAnimatedTexture {
    private final TextureAtlasSprite SPRITE;

    public AnimatedTexture(ResourceLocation location, int width, int height, AnimationMetadataSection metadata,
                           int mipmapLevels, NativeImage nativeImage) {
        TextureAtlasSprite.Info spriteInfo = new TextureAtlasSprite.Info(location, width, height, metadata);
        SPRITE = new SingleTextureSprite(SingleTextureSprite.EMPTY_ATLAS, spriteInfo, mipmapLevels,
                width, height, 0, 0, nativeImage);
    }

    @Override
    public void loadTexture(IResourceManager manager) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(this::loadImage);
        } else {
            loadImage();
        }
    }

    private void loadImage() {
        TextureUtil.prepareImage(getGlTextureId(), 0, SPRITE.getWidth(), SPRITE.getHeight());
        SPRITE.uploadMipmaps();
    }

    @Override
    public void tick() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::updateAnimation);
        } else {
            updateAnimation();
        }
    }

    private void updateAnimation() {
        bindTexture();
        SPRITE.updateAnimation();
    }

    /* KEEP THIS CLASS PRIVATE. Unfortunately, TextureAtlasSprite's constructor is protected, and we can't
    extract its animation code without copying large swaths of it. We need this class to get access to the
    constructor without reflection. */
    private static class SingleTextureSprite extends TextureAtlasSprite {
        private static final AtlasTexture EMPTY_ATLAS = new AtlasTexture(
                new ResourceLocation("moremcmeta", "textures/atlas/empty.png")
        );

        public SingleTextureSprite(AtlasTexture atlasTextureIn, Info spriteInfoIn, int mipmapLevelsIn,
                                   int atlasWidthIn, int atlasHeightIn, int xIn, int yIn, NativeImage imageIn) {
            super(atlasTextureIn, spriteInfoIn, mipmapLevelsIn, atlasWidthIn, atlasHeightIn, xIn, yIn, imageIn);
        }
    }
}
