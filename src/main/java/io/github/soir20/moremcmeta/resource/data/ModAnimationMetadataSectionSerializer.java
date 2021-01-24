package io.github.soir20.moremcmeta.resource.data;

import com.google.gson.JsonObject;
import io.github.soir20.moremcmeta.MoreMcmeta;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.data.IMetadataSectionSerializer;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Uses a custom section name to read animation metadata. This distinguishes textures meant for the mod to
 * animate from textures already animated by default or by other mods.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModAnimationMetadataSectionSerializer implements IMetadataSectionSerializer<AnimationMetadataSection> {
    private final IMetadataSectionSerializer<AnimationMetadataSection> SERIALIZER;

    /**
     * Creates a new serializer for mod-specific animation metadata.
     * @param serializer    a serializer for regular animation metadata
     */
    public ModAnimationMetadataSectionSerializer(IMetadataSectionSerializer<AnimationMetadataSection> serializer) {
        SERIALIZER = serializer;
    }

    /**
     * Gets the section name of this metadata (i.e. the top level JSON key).
     * @return  the metadata's section name
     */
    @Override
    public String getSectionName() {
        return MoreMcmeta.MODID;
    }

    /**
     * Reads metadata values from raw JSON.
     * @param json  raw JSON from the metadata (.mcmeta) file
     * @return  animation parameters
     */
    @Override
    public AnimationMetadataSection deserialize(JsonObject json) {
        return SERIALIZER.deserialize(json);
    }

}
