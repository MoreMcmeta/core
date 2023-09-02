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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureData;
import io.github.moremcmeta.moremcmeta.impl.client.texture.MockCloseableImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link  SpriteFrameSizeFixPack}.
 * @author soir20
 */
@SuppressWarnings("resource")
public final class SpriteFrameSizeFixPackTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullTextures_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new SpriteFrameSizeFixPack(null, ImmutableMap.of());
    }

    @Test
    public void construct_NullRootResources_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        expectedException.expect(NullPointerException.class);
        new SpriteFrameSizeFixPack(textures1, null);
    }

    @Test
    public void construct_EmptyTexturesAndRootResources_NoException() {
        new SpriteFrameSizeFixPack(new HashMap<>(), ImmutableMap.of());
    }

    @Test
    public void getRootResource_NullName_NullPointerException() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        expectedException.expect(NullPointerException.class);
        pack.getRootResource(null);
    }

    @Test
    public void getRootResource_NonNullName_Null() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        assertNull(pack.getRootResource("one.png"));
    }

    @Test
    public void getRootResource_EmptyName_Null() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        assertNull(pack.getRootResource(""));
    }

    @Test
    public void getRootResource_MissingName_Null() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        Map<String, StreamSource> rootResources = new HashMap<>();
        rootResources.put("pack.png", () -> new ByteArrayInputStream("Hello, world!".getBytes(StandardCharsets.UTF_8)));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, rootResources);
        assertNull(pack.getRootResource("missing"));
    }

    @Test
    public void getRootResource_PresentName_ResourceFound() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        Map<String, StreamSource> rootResources = new HashMap<>();
        rootResources.put("pack.png", () -> new ByteArrayInputStream("Hello, world!".getBytes(StandardCharsets.UTF_8)));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, rootResources);
        assertEquals("Hello, world!", IOUtils.toString(pack.getRootResource("pack.png"), StandardCharsets.UTF_8));
    }

    @Test
    public void getResource_NullPackType_NullPointerException() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        expectedException.expect(NullPointerException.class);
        pack.getResource(null, new ResourceLocation("one.png"));
    }

    @Test
    public void getResource_NullLocation_NullPointerException() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        expectedException.expect(NullPointerException.class);
        pack.getResource(PackType.CLIENT_RESOURCES, null);
    }

    @Test
    public void getResource_ServerPackTypeClientResource_IOException() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        expectedException.expect(IOException.class);
        pack.getResource(PackType.SERVER_DATA, new ResourceLocation("one.png"));
    }

    @Test
    public void getResource_ServerPackTypeServerResource_IOException() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        expectedException.expect(IOException.class);
        pack.getResource(PackType.SERVER_DATA, new ResourceLocation("server-one.png"));
    }

    @Test
    public void getResource_KnownVanillaMetadata_CorrectFrameSize() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        InputStream resource = pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("one.png.mcmeta"));

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

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        InputStream resource = pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("one.png.mcmeta"));

        JsonObject metadata = readJson(resource);
        JsonObject animationSection = metadata.getAsJsonObject("animation");

        assertEquals(1, animationSection.get("frames").getAsJsonArray().size());
        assertEquals(0, animationSection.get("frames").getAsJsonArray().get(0).getAsInt());
    }

    @Test
    public void getResource_UnknownVanillaMetadata_IOException() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        expectedException.expect(IOException.class);
        pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("three.png.mcmeta"));
    }

    @Test
    public void getResource_KnownTextureFirstPack_IOException() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        expectedException.expect(IOException.class);
        pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("one.png"));
    }

    @Test
    public void getResource_KnownTextureMiddlePack_IOException() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("four.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        expectedException.expect(IOException.class);
        pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("four.png"));
    }

    @Test
    public void getResource_KnownTextureLastPack_FoundResource() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        expectedException.expect(IOException.class);
        pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("five.png"));
    }

    @Test
    public void getResource_UnknownTexture_IOException() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        expectedException.expect(IOException.class);
        pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("three.png"));
    }

    @Test
    public void getResource_BadResourceLocation_Null() throws IOException {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("two.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        expectedException.expect(IOException.class);
        pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("one.png") {
            @Override
            public @NotNull String getNamespace() {
                return "bad namespace";
            }
        });
    }

    @Test
    public void getResources_ServerPackType_NoResources() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertTrue(pack.getResources(PackType.SERVER_DATA, "minecraft", "textures",
                20, (fileName) -> true).isEmpty());
    }

    @Test
    public void getResources_NullPackType_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        expectedException.expect(NullPointerException.class);
        pack.getResources(null, "minecraft", "textures", 20, (fileName) -> true);
    }

    @Test
    public void getResources_NullNamespace_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        expectedException.expect(NullPointerException.class);
        pack.getResources(PackType.CLIENT_RESOURCES, null, "textures", 20, (fileName) -> true);
    }

    @Test
    public void getResources_NullPathStart_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        expectedException.expect(NullPointerException.class);
        pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", null, 20, (fileName) -> true);
    }

    @Test
    public void getResources_NullFilter_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        expectedException.expect(NullPointerException.class);
        pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures", 20, null);
    }

    @Test
    public void getResources_DepthNegative_IllegalArgException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        expectedException.expect(IllegalArgumentException.class);
        pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures", -1, (path) -> true);
    }

    @Test
    public void getResources_DepthZeroNoneMatch_NoneFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/folder/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertEquals(0, pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                0, (path) -> true).size());
    }

    @Test
    public void getResources_DepthZeroDirectlyWithinFolder_NoneFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"), new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                0, (path) -> true);
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_DepthPositiveNoneMatch_NoneFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/folder2/folder3/folder4/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/folder2/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                2, (path) -> true);
        assertTrue(results.isEmpty());
    }

    @Test
    public void getResources_DepthPositiveTreeSameDepth_MatchingFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/folder2/folder3/folder4/folder5/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/folder2/folder3/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                4, (path) -> true);
        assertTrue(results.isEmpty());
    }

    @Test
    public void getResources_DepthPositiveTreeSmallerDepth_MatchingFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/folder2/folder3/folder4/folder5/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/folder2/folder3/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                6, (path) -> true);
        assertTrue(results.isEmpty());
    }

    @Test
    public void getResources_DepthMaxInt_MatchingFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/folder2/folder3/folder4/folder5/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/folder2/folder3/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                Integer.MAX_VALUE, (path) -> true);
        assertTrue(results.isEmpty());
    }

    @Test
    public void getResources_DepthMultiFolderPathStart_DepthFromPathStart() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/folder/folder3/folder4/folder5/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/folder2/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures/folder",
                2, (path) -> true);
        assertTrue(results.isEmpty());
    }

    @Test
    public void getResources_PathStartNotAtBeginning_NotMatched() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("/textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("/textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                2, (path) -> true);
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_PathStartDifferent_NotMatched() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "other",
                2, (path) -> true);
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_EmptyPathStart_Ignored() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "",
                2, (path) -> true);
        assertTrue(results.isEmpty());
    }

    @Test
    public void getResources_PathStartSubstringOfBeginning_NotMatched() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "text",
                2, (path) -> true);
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_PathStartEndsWithSlash_NotMatched() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures/",
                2, (path) -> true);
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_PathFilteredMismatch_NotMatched() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                2, (path) -> false);
        assertTrue(results.isEmpty());
    }

    @Test
    public void getResources_PathFilteredSomeMismatch_NoneFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                2, (path) -> path.endsWith("five.png"));
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_NoTextures_NoneMatch() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                2, (path) -> true);
        assertEquals(0, results.size());
    }

    @Test
    public void getResources_AllParameters_NoneMatch() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.jpg"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/two.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("other", "textures/folder/three.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("other/folder/four.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/folder/folder2/folder3/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        Collection<ResourceLocation> results = pack.getResources(PackType.CLIENT_RESOURCES, "minecraft", "textures",
                2, (path) -> path.endsWith(".png"));
        assertTrue(results.isEmpty());
    }

    @Test
    public void hasResource_NullPackType_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        expectedException.expect(NullPointerException.class);
        pack.hasResource(null, new ResourceLocation("textures/one.png"));
    }

    @Test
    public void hasResource_NullLocation_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        expectedException.expect(NullPointerException.class);
        pack.hasResource(PackType.CLIENT_RESOURCES, null);
    }

    @Test
    public void hasResource_ServerType_NotFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertFalse(pack.hasResource(PackType.SERVER_DATA, new ResourceLocation("textures/one.png")));
    }

    @Test
    public void hasResource_HasVanillaMetadata_Found() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertTrue(pack.hasResource(PackType.CLIENT_RESOURCES, new ResourceLocation("textures/one.png.mcmeta")));
    }

    @Test
    public void hasResource_MissingVanillaMetadata_NotFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertFalse(pack.hasResource(PackType.CLIENT_RESOURCES, new ResourceLocation("textures/two.png.mcmeta")));
    }

    @Test
    public void hasResource_HasOtherResource_NotFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png.moremcmeta"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertFalse(pack.hasResource(PackType.CLIENT_RESOURCES, new ResourceLocation("textures/one.png.moremcmeta")));
    }

    @Test
    public void hasResource_MissingOtherResource_NotFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertFalse(pack.hasResource(PackType.CLIENT_RESOURCES, new ResourceLocation("textures/one.png.moremcmeta")));
    }

    @Test
    public void hasResource_HasTexture_NotFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertFalse(pack.hasResource(PackType.CLIENT_RESOURCES, new ResourceLocation("textures/one.png")));
    }

    @Test
    public void hasResource_MissingTexture_NotFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertFalse(pack.hasResource(PackType.CLIENT_RESOURCES, new ResourceLocation("textures/two.png")));
    }

    @Test
    public void hasResource_NoTextures_NotFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertFalse(pack.hasResource(PackType.CLIENT_RESOURCES, new ResourceLocation("textures/one.png")));
    }

    @Test
    public void hasResource_NotInRepo_NotFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/other.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertFalse(pack.hasResource(PackType.CLIENT_RESOURCES, new ResourceLocation("textures/other.png")));
    }

    @Test
    public void hasResource_BadResourceLocation_NotFound() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));

        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertFalse(pack.hasResource(
                PackType.CLIENT_RESOURCES,
                new ResourceLocation("textures/one.png") {
                    @Override
                    public @NotNull String getNamespace() {
                        return "bad namespace";
                    }
                }
        ));
    }

    @Test
    public void getNamespaces_NullPackType_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

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
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertEquals(ImmutableSet.of("first", "second", "third", "minecraft"), pack.getNamespaces(PackType.CLIENT_RESOURCES));
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
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertTrue(pack.getNamespaces(PackType.SERVER_DATA).isEmpty());
    }

    @Test
    public void getNamespaces_NoTextures_None() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());
        assertTrue(pack.getNamespaces(PackType.CLIENT_RESOURCES).isEmpty());
    }

    @Test
    public void getMetadataSection_NullSerializer_NullPointerException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

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
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertNull(pack.getMetadataSection(PackMetadataSection.SERIALIZER));
    }

    @Test
    public void getName_OnePack_NameNotEmpty() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        assertNotEquals(0, pack.getName().length());
    }

    @Test
    public void getName_TwoPacks_SameName() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());


        Map<ResourceLocation, TextureData<?>> textures2 = new HashMap<>();
        textures2.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures2.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack2 = new SpriteFrameSizeFixPack(textures2, ImmutableMap.of());

        assertEquals(pack.getName(), pack2.getName());
    }

    @Test
    public void close_NotYetClosed_NoException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        pack.close();
    }

    @Test
    public void close_AlreadyClosed_NoException() {
        Map<ResourceLocation, TextureData<?>> textures1 = new HashMap<>();
        textures1.put(new ResourceLocation("textures/one.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        textures1.put(new ResourceLocation("textures/five.png"),
                new TextureData<>(new TextureData.FrameSize(1, 2), false, false, new MockCloseableImage(10, 10), ImmutableList.of()));
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack(textures1, ImmutableMap.of());

        pack.close();
        pack.close();
    }

    private JsonObject readJson(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return GsonHelper.parse(reader);
        }
    }

}