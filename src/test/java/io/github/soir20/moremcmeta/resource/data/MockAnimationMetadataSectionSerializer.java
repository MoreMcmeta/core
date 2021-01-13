package io.github.soir20.moremcmeta.resource.data;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.data.IMetadataSectionSerializer;

import javax.annotation.Nullable;

@SuppressWarnings("NullableProblems")
public class MockAnimationMetadataSectionSerializer implements
        IMetadataSectionSerializer<AnimationMetadataSection> {
    private final AnimationMetadataSection METADATA_RETURN_VALUE;

    public MockAnimationMetadataSectionSerializer(@Nullable AnimationMetadataSection metadataReturnValue) {
        METADATA_RETURN_VALUE = metadataReturnValue;
    }

    @Override
    public String getSectionName() {
        return "test";
    }

    @Override
    public AnimationMetadataSection deserialize(JsonObject json) {
        return METADATA_RETURN_VALUE;
    }
}