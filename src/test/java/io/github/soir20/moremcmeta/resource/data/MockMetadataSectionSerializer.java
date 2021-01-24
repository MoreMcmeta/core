package io.github.soir20.moremcmeta.resource.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.data.IMetadataSectionSerializer;

import javax.annotation.Nullable;

@SuppressWarnings("NullableProblems")
public class MockMetadataSectionSerializer<M> implements IMetadataSectionSerializer<M> {
    private final M METADATA_RETURN_VALUE;

    public MockMetadataSectionSerializer(@Nullable M metadataReturnValue) {
        METADATA_RETURN_VALUE = metadataReturnValue;
    }

    @Override
    public String getSectionName() {
        return "test";
    }

    @Override
    public M deserialize(JsonObject json) {
        return METADATA_RETURN_VALUE;
    }
}