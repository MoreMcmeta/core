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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.soir20.moremcmeta.impl.client.resource.ModAnimationMetadataSection;
import io.github.soir20.moremcmeta.impl.client.resource.ModAnimationMetadataSectionSerializer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link ModAnimationMetadataSectionSerializer}.
 * @author soir20
 */
public class ModAnimationMetadataSectionSerializerTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getSectionName_HasName_SameAsMCSection() {
        assertEquals("animation", (new ModAnimationMetadataSectionSerializer()).getMetadataSectionName());
    }

    @Test
    public void fromJson_NullJson_NullPointerException() {
        ModAnimationMetadataSectionSerializer serializer = new ModAnimationMetadataSectionSerializer();
        expectedException.expect(NullPointerException.class);
        serializer.fromJson(null);
    }

    @Test
    public void fromJson_MissingProperty_NotSyncedSection() {
        ModAnimationMetadataSectionSerializer serializer = new ModAnimationMetadataSectionSerializer();
        ModAnimationMetadataSection section = serializer.fromJson(new JsonObject());
        assertFalse(section.isDaytimeSynced());
    }

    @Test
    public void fromJson_SyncedIsTruthyPrimitive_NotSyncedSection() {
        ModAnimationMetadataSectionSerializer serializer = new ModAnimationMetadataSectionSerializer();
        JsonObject container = new JsonObject();
        container.addProperty("daytimeSync", 5);
        ModAnimationMetadataSection section = serializer.fromJson(container);
        assertFalse(section.isDaytimeSynced());
    }

    @Test
    public void fromJson_SyncedIsFalsyPrimitive_NotSyncedSection() {
        ModAnimationMetadataSectionSerializer serializer = new ModAnimationMetadataSectionSerializer();
        JsonObject container = new JsonObject();
        container.addProperty("daytimeSync", 0);
        ModAnimationMetadataSection section = serializer.fromJson(container);
        assertFalse(section.isDaytimeSynced());
    }

    @Test
    public void fromJson_SyncedIsArray_NotSyncedSection() {
        ModAnimationMetadataSectionSerializer serializer = new ModAnimationMetadataSectionSerializer();
        JsonObject container = new JsonObject();
        container.add("daytimeTime", new JsonArray());
        ModAnimationMetadataSection section = serializer.fromJson(container);
        assertFalse(section.isDaytimeSynced());
    }

    @Test
    public void fromJson_SyncedIsObject_NotSyncedSection() {
        ModAnimationMetadataSectionSerializer serializer = new ModAnimationMetadataSectionSerializer();
        JsonObject container = new JsonObject();
        container.add("daytimeTime", new JsonObject());
        ModAnimationMetadataSection section = serializer.fromJson(container);
        assertFalse(section.isDaytimeSynced());
    }

    @Test
    public void fromJson_IsSynced_SyncedSection() {
        ModAnimationMetadataSectionSerializer serializer = new ModAnimationMetadataSectionSerializer();
        JsonObject container = new JsonObject();
        container.addProperty("daytimeSync", true);
        ModAnimationMetadataSection section = serializer.fromJson(container);
        assertTrue(section.isDaytimeSynced());
    }

    @Test
    public void fromJson_IsNotSynced_NotSyncedSection() {
        ModAnimationMetadataSectionSerializer serializer = new ModAnimationMetadataSectionSerializer();
        JsonObject container = new JsonObject();
        container.addProperty("daytimeSync", false);
        ModAnimationMetadataSection section = serializer.fromJson(container);
        assertFalse(section.isDaytimeSynced());
    }

}