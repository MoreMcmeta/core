package io.github.soir20.moremcmeta.resource.data;

import com.google.gson.JsonObject;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.AnimationMetadataSectionSerializer;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSectionSerializer;
import net.minecraft.resources.data.IMetadataSectionSerializer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedMetadataSectionSerializer implements IMetadataSectionSerializer<CombinedMetadataSection> {
    private final TextureMetadataSectionSerializer textureMetadataSerializer;
    private final AnimationMetadataSectionSerializer animationMetadataSerializer;

    public CombinedMetadataSectionSerializer() {
        textureMetadataSerializer = new TextureMetadataSectionSerializer();
        animationMetadataSerializer = new AnimationMetadataSectionSerializer();
    }

    @Override
    public String getSectionName() {
        return "combined";
    }

    @Override
    public CombinedMetadataSection deserialize(JsonObject json) {
        TextureMetadataSection textureMetadataSection = textureMetadataSerializer.deserialize(json);
        AnimationMetadataSection animationMetadataSection = animationMetadataSerializer.deserialize(json);

        return new CombinedMetadataSection(textureMetadataSection, animationMetadataSection);
    }
}
