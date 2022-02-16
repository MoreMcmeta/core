/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        requireNonNull(jsonObject, "JSON object cannot be null");
        boolean isSynced = GsonHelper.getAsBoolean(jsonObject, "daytimeSync", false);
        return new ModAnimationMetadataSection(isSynced);
    }

}
