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
import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureData;
import io.github.moremcmeta.moremcmeta.impl.client.texture.MockCloseableImage;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link MetadataRegistryImpl}.
 * @author soir20
 */
public final class MetadataRegistryImplTest {
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
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        expectedException.expect(NullPointerException.class);
        registry.metadataFromPath(null, new ResourceLocation("block.png"));
    }

    @Test
    public void getFromPath_NullLocation_NullPointerException() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        expectedException.expect(NullPointerException.class);
        registry.metadataFromPath("plugin", null);
    }

    @Test
    public void getFromPath_SpriteName_NothingFound() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertFalse(registry.metadataFromPath("plugin", new ResourceLocation("block")).isPresent());
    }

    @Test
    public void getFromPath_DifferentPluginHasMetadata_NothingFound() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertFalse(registry.metadataFromPath("other", new ResourceLocation("block.png")).isPresent());
    }

    @Test
    public void getFromPath_SamePluginHasMetadata_MetadataFound() {
        AnalyzedMetadata expected = new AnalyzedMetadata() {};

        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                expected,
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertEquals(
                expected,
                registry.metadataFromPath("plugin", new ResourceLocation("textures/block.png")).orElseThrow()
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
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        expectedException.expect(NullPointerException.class);
        registry.metadataFromSpriteName(null, new ResourceLocation("block.png"));
    }

    @Test
    public void getFromSpriteName_NullLocation_NullPointerException() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        expectedException.expect(NullPointerException.class);
        registry.metadataFromSpriteName("plugin", null);
    }

    @Test
    public void getFromSpriteName_FullTextureLocation_NothingFound() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertFalse(registry.metadataFromSpriteName("plugin", new ResourceLocation("textures/block.png")).isPresent());
    }

    @Test
    public void getFromSpriteName_DifferentPluginHasMetadata_NothingFound() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertFalse(registry.metadataFromSpriteName("other", new ResourceLocation("block")).isPresent());
    }

    @Test
    public void getFromSpriteName_SamePluginHasMetadata_MetadataFound() {
        AnalyzedMetadata expected = new AnalyzedMetadata() {};

        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                expected,
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        assertEquals(
                expected,
                registry.metadataFromSpriteName("plugin", new ResourceLocation("block")).orElseThrow()
        );
    }

    @Test
    public void getByPlugin_NullPluginName_NullPointerException() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        expectedException.expect(NullPointerException.class);
        registry.metadataByPlugin(null);
    }

    @Test
    public void getByPlugin_PluginHasNoMetadata_AllMetadataFound() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        Map<ResourceLocation, AnalyzedMetadata> metadata = registry.metadataByPlugin("plugin2");
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void getByPlugin_PluginHasMetadata_AllMetadataFound() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                ),
                new ResourceLocation("textures/block2.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
                                (metadata, frames) -> new TextureComponent<>() {}
                        ))
                )
        ));

        Map<ResourceLocation, AnalyzedMetadata> metadata = registry.metadataByPlugin("plugin");
        assertTrue(metadata.containsKey(new ResourceLocation("textures/block.png")));
        assertTrue(metadata.containsKey(new ResourceLocation("textures/block2.png")));
        assertEquals(2, metadata.size());
    }

    @Test
    public void set_NullMap_NullPointerException() {
        MetadataRegistryImpl registry = new MetadataRegistryImpl();

        expectedException.expect(NullPointerException.class);
        registry.set(null);
    }

    @Test
    public void set_SamePluginHasTwoMetadataObjects_IllegalArgException() {
        AnalyzedMetadata expected = new AnalyzedMetadata() {};

        MetadataRegistryImpl registry = new MetadataRegistryImpl();

        expectedException.expect(IllegalArgumentException.class);
        registry.set(ImmutableMap.of(
                new ResourceLocation("textures/block.png"),
                new TextureData<>(
                        new TextureData.FrameSize(30, 40),
                        false, false,
                        Optional.empty(),
                        new MockCloseableImage(100, 100),
                        ImmutableList.of(Triple.of(
                                "plugin",
                                new AnalyzedMetadata() {},
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