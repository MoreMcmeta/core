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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import com.google.common.collect.ImmutableMap;
import io.github.moremcmeta.moremcmeta.impl.client.texture.MockCloseableImage;
import io.github.moremcmeta.moremcmeta.api.client.metadata.ParsedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureData;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the {@link MetadataRegistryImpl}.
 * @author soir20
 */
public class MetadataRegistryImplTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getFromPath_NullPluginName_NullPointerException() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                new ParsedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        expectedException.expect(NullPointerException.class);
        registry.getFromPath(null, new ResourceLocation("block.png"));
    }

    @Test
    public void getFromPath_NullLocation_NullPointerException() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                new ParsedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        expectedException.expect(NullPointerException.class);
        registry.getFromPath("plugin", null);
    }

    @Test
    public void getFromPath_SpriteName_NothingFound() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                new ParsedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertFalse(registry.getFromPath("plugin", new ResourceLocation("block")).isPresent());
    }

    @Test
    public void getFromPath_DifferentPluginHasMetadata_NothingFound() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                new ParsedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertFalse(registry.getFromPath("other", new ResourceLocation("block.png")).isPresent());
    }

    @Test
    public void getFromPath_SamePluginHasMetadata_MetadataFound() {
        ParsedMetadata expected = new ParsedMetadata() {};

        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                expected,
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertEquals(
                expected,
                registry.getFromPath("plugin", new ResourceLocation("textures/block.png")).orElseThrow()
        );
    }

    @Test
    public void getFromSpriteName_NullPluginName_NullPointerException() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                new ParsedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        expectedException.expect(NullPointerException.class);
        registry.getFromSpriteName(null, new ResourceLocation("block.png"));
    }

    @Test
    public void getFromSpriteName_NullLocation_NullPointerException() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                new ParsedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        expectedException.expect(NullPointerException.class);
        registry.getFromSpriteName("plugin", null);
    }

    @Test
    public void getFromSpriteName_FullTextureLocation_NothingFound() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                new ParsedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertFalse(registry.getFromSpriteName("plugin", new ResourceLocation("textures/block.png")).isPresent());
    }

    @Test
    public void getFromSpriteName_DifferentPluginHasMetadata_NothingFound() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                new ParsedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertFalse(registry.getFromSpriteName("other", new ResourceLocation("block")).isPresent());
    }

    @Test
    public void getFromSpriteName_SamePluginHasMetadata_MetadataFound() {
        ParsedMetadata expected = new ParsedMetadata() {};

        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                expected,
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertEquals(
                expected,
                registry.getFromSpriteName("plugin", new ResourceLocation("block")).orElseThrow()
        );
    }

    @Test
    public void set_NullMap_NullPointerException() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();

        expectedException.expect(NullPointerException.class);
        registry.set(null);
    }

    @Test
    public void set_SamePluginHasTwoMetadataObjects_IllegalArgException() {
        ParsedMetadata expected = new ParsedMetadata() {};

        MetadataRegistryImpl registry = new MetadataRegistryImpl();

        expectedException.expect(IllegalArgumentException.class);
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        new MockCloseableImage(100, 100),
                        List.of(Triple.of(
                                "plugin",
                                new ParsedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {
                                }
                        ), Triple.of(
                                "plugin",
                                expected,
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));
    }

}