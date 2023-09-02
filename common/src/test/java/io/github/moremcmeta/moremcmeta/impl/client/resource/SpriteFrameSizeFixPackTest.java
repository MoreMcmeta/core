/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureData;
import io.github.moremcmeta.moremcmeta.impl.client.texture.MockCloseableImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link  SpriteFrameSizeFixPack}.
 * @author soir20
 */
@SuppressWarnings({"resource", "DataFlowIssue"})
public final class SpriteFrameSizeFixPackTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullTextures_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new SpriteFrameSizeFixPack(null);
    }

    @Test
    public void construct_EmptyTextures_NoException() {
        new SpriteFrameSizeFixPack(new HashMap<>());
    }

    @Test
    public void getRootResource_NullArray_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        expectedException.expect(NullPointerException.class);
        pack.getRootResource((String[]) null);
    }

    @Test
    public void getRootResource_NullName_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        assertNull(pack.getRootResource((String) null));
    }

    @Test
    public void getRootResource_NonNullName_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        assertNull(pack.getRootResource("one.png"));
    }

    @Test
    public void getRootResource_EmptyName_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        assertNull(pack.getRootResource(""));
    }

    @Test
    public void getResource_NullPackType_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        expectedException.expect(NullPointerException.class);
        pack.getResource(null, new ResourceLocation("one.png"));
    }

    @Test
    public void getResource_NullLocation_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        expectedException.expect(NullPointerException.class);
        pack.getResource(PackType.CLIENT_RESOURCES, null);
    }

    @Test
    public void getResource_ServerPackTypeClientResource_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        assertNull(pack.getResource(PackType.SERVER_DATA, new ResourceLocation("one.png")));
    }

    @Test
    public void getResource_ServerPackTypeServerResource_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        assertNull(pack.getResource(PackType.SERVER_DATA, new ResourceLocation("server-one.png")));
    }

    @Test
    public void getResource_KnownVanillaMetadata_CorrectFrameSize() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        InputStream resource = pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("one.png.mcmeta")).get();

        JsonObject metadata = readJson(resource);
        JsonObject animationSection = metadata.getAsJsonObject("animation");

        assertEquals(1, animationSection.get("width").getAsInt());
        assertEquals(2, animationSection.get("height").getAsInt());
    }

    @Test
    public void getResource_KnownVanillaMetadata_HasSingleFrame() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        InputStream resource = pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("one.png.mcmeta")).get();

        JsonObject metadata = readJson(resource);
        JsonObject animationSection = metadata.getAsJsonObject("animation");

        assertEquals(1, animationSection.get("frames").getAsJsonArray().size());
        assertEquals(0, animationSection.get("frames").getAsJsonArray().get(0).getAsInt());
    }

    @Test
    public void getResource_UnknownVanillaMetadata_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        assertNull(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("three.png.mcmeta")));
    }

    @Test
    public void getResource_KnownTextureFirstPack_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        assertNull(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("one.png")));
    }

    @Test
    public void getResource_KnownTextureMiddlePack_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("four.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        assertNull(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("four.png")));
    }

    @Test
    public void getResource_KnownTextureLastPack_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        assertNull(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("five.png")));
    }

    @Test
    public void getResource_UnknownTexture_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        assertNull(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("three.png")));
    }

    @Test
    public void getResource_BadResourceLocation_Null() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        IoSupplier<InputStream> resource = pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("one.png") {
            @Override
            public @NotNull String getNamespace() {
                return "bad namespace";
            }
        });

        assertNull(resource);
    }

    @Test
    public void getResources_ServerPackType_NoResources() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        assertEquals(0, getResources(pack, PackType.SERVER_DATA, "minecraft", "textures").size());
    }

    @Test
    public void getResources_NullPackType_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        expectedException.expect(NullPointerException.class);
        getResources(pack, null, "minecraft", "textures");
    }

    @Test
    public void getResources_NullNamespace_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        expectedException.expect(NullPointerException.class);
        getResources(pack, PackType.CLIENT_RESOURCES, null, "textures");
    }

    @Test
    public void getResources_NullPathStart_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        expectedException.expect(NullPointerException.class);
        getResources(pack, PackType.CLIENT_RESOURCES, "minecraft", null);
    }

    @Test
    public void getResources_NullResourceOutput_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        expectedException.expect(NullPointerException.class);
        pack.listResources(PackType.CLIENT_RESOURCES, "minecraft", "textures", null);
    }

    @Test
    public void getResources_NamespaceMismatch_NoMismatchedResources() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        Collection<ResourceLocation> results = getResources(pack, PackType.CLIENT_RESOURCES, "other", "textures");
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_PathStartNotAtBeginning_NotMatched() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("/textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("/textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        Collection<ResourceLocation> results = getResources(pack, PackType.CLIENT_RESOURCES, "minecraft", "textures");
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_PathStartDifferent_NotMatched() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        Collection<ResourceLocation> results = getResources(pack, PackType.CLIENT_RESOURCES, "minecraft", "other");
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_EmptyPathStart_Ignored() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        Collection<ResourceLocation> results = getResources(pack, PackType.CLIENT_RESOURCES, "minecraft", "");
        assertEquals(2, results.size());
        assertTrue(results.contains(new ResourceLocation("textures/one.png.mcmeta")));
        assertTrue(results.contains(new ResourceLocation("textures/five.png.mcmeta")));
    }

    @Test
    public void getResources_PathStartSubstringOfBeginning_NotMatched() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        Collection<ResourceLocation> results = getResources(pack, PackType.CLIENT_RESOURCES, "minecraft", "text");
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_PathStartEndsWithSlash_NotMatched() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        Collection<ResourceLocation> results = getResources(pack, PackType.CLIENT_RESOURCES, "minecraft", "textures/");
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_NoTextures_NoneMatch() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        Collection<ResourceLocation> results = getResources(pack, PackType.CLIENT_RESOURCES, "minecraft", "textures");
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_AllParameters_SomeMatch() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.jpg"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/two.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("other", "textures/folder/three.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("other/folder/four.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/folder2/folder3/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        Collection<ResourceLocation> results = getResources(pack, PackType.CLIENT_RESOURCES, "minecraft", "textures");
        assertEquals(2, results.size());
        assertTrue(results.contains(new ResourceLocation("textures/folder/two.png.mcmeta")));
        assertTrue(results.contains(new ResourceLocation("textures/folder/folder2/folder3/five.png.mcmeta")));
    }

    @Test
    public void getResources_BadResourceLocation_BadLocationSkipped() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.jpg"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(
                new ResourceLocation("textures/folder/two.png") {
                    @Override
                    public @NotNull String getNamespace() {
                        return "bad namespace";
                    }
                },
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("other", "textures/folder/three.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("other/folder/four.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/folder2/folder3/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        Collection<ResourceLocation> results = getResources(pack, PackType.CLIENT_RESOURCES, "minecraft", "textures");
        assertEquals(1, results.size());
        assertTrue(results.contains(new ResourceLocation("textures/folder/folder2/folder3/five.png.mcmeta")));
    }

    @Test
    public void getNamespaces_NullPackType_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        expectedException.expect(NullPointerException.class);
        pack.getNamespaces(null);
    }

    @Test
    public void getNamespaces_ClientType_UniqueNamespaces() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("first", "textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("second", "textures/two.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("third", "textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        assertEquals(Set.of("first", "second", "third", "minecraft"), pack.getNamespaces(PackType.CLIENT_RESOURCES));
    }

    @Test
    public void getNamespaces_ServerType_None() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("first", "textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("second", "textures/two.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("third", "textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        assertEquals(0, pack.getNamespaces(PackType.SERVER_DATA).size());
    }

    @Test
    public void getNamespaces_NoTextures_None() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);
        assertEquals(0, pack.getNamespaces(PackType.CLIENT_RESOURCES).size());
    }

    @Test
    public void getMetadataSection_NullSerializer_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        expectedException.expect(NullPointerException.class);
        pack.getMetadataSection(null);
    }

    @Test
    public void getMetadataSection_ProvidedSerializer_NullMetadata() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        assertNull(pack.getMetadataSection(PackMetadataSection.TYPE));
    }

    @Test
    public void getName_OnePack_NameNotEmpty() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        assertNotEquals(0, pack.packId().length());
    }

    @Test
    public void getName_TwoPacks_SameName() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);


        Map<ResourceLocation, TextureData<?>> textures2 = new HashMap<>();
        textures2.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures2.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack2 = new SpriteFrameSizeFixPack(textures2);

        assertEquals(pack.packId(), pack2.packId());
    }

    @Test
    public void close_NotYetClosed_NoException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        pack.close();
    }

    @Test
    public void close_AlreadyClosed_NoException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1);

        pack.close();
        pack.close();
    }

    private JsonObject readJson(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return GsonHelper.parse(reader);
        }
    }

    private Collection<ResourceLocation> getResources(SpriteFrameSizeFixPack pack, PackType packType,
                                                      String namespace, String pathStart) {
        Set<ResourceLocation> locations = new HashSet<>();

        PackResources.ResourceOutput resourceOutput = (location, supplier) -> locations.add(location);
        pack.listResources(packType, namespace, pathStart, resourceOutput);

        return locations;
    }

}