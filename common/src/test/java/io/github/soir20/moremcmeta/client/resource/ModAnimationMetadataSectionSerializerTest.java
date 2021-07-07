package io.github.soir20.moremcmeta.client.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
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