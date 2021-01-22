package io.github.soir20.moremcmeta.resource.data;

import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;

public class CombinedMetadataSection {
    private final TextureMetadataSection TEXTURE_METADATA;
    private final AnimationMetadataSection ANIMATION_METADATA;

    public CombinedMetadataSection(TextureMetadataSection textureMetadata, AnimationMetadataSection animationMetadata) {
        TEXTURE_METADATA = textureMetadata;
        ANIMATION_METADATA = animationMetadata;
    }

    public TextureMetadataSection getTextureMetadata() {
        return TEXTURE_METADATA;
    }

    public AnimationMetadataSection getAnimationMetadata() {
        return ANIMATION_METADATA;
    }

}
