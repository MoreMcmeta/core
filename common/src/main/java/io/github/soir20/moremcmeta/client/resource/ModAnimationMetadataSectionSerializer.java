package io.github.soir20.moremcmeta.client.resource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

import static java.util.Objects.requireNonNull;

/**
 * Parses additional animation metadata that the vanilla serializer does not handle.
 * Produces {@link ModAnimationMetadataSection}s.
 * @author soir20
 */
public class ModAnimationMetadataSectionSerializer implements MetadataSectionSerializer<ModAnimationMetadataSection> {

    /**
     * Gets the section name that contains the metadata this serializer parses.
     * @return this serializer's section name
     */
    @Override
    public String getMetadataSectionName() {
        return "animation";
    }

    /**
     * Gets a mod animation metadata section from JSON.
     * @param jsonObject    the object to parse. It should be the contents of the section
     *                      named this serializer's section name, not including the section
     *                      name. If the daytime sync property is not present, it defaults
     *                      to false.
     * @return a mod animation metadata section from the provided JSON
     * @throws com.google.gson.JsonParseException   if there is an JSON parse error
     * @throws IllegalArgumentException             if an invalid parameter is provided
     *                                              even if the JSON is valid
     */
    @Override
    public ModAnimationMetadataSection fromJson(JsonObject jsonObject) throws JsonParseException,
            IllegalArgumentException {
        requireNonNull(jsonObject);
        boolean isSynced = GsonHelper.getAsBoolean(jsonObject, "daytimeSync", false);
        return new ModAnimationMetadataSection(isSynced);
    }

}
