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

package io.github.moremcmeta.moremcmeta.forge.impl.client.reflection;

import com.google.common.collect.ImmutableSet;
import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaMetadataParserPlugin;
import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaTexturePlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataAnalyzer;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentBuilder;
import io.github.moremcmeta.moremcmeta.forge.api.client.MoreMcmetaClientPlugin;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests the {@link AnnotatedClassLoader} with dummy annotation data.
 * @author soir20
 */
public final class AnnotatedClassLoaderTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_SupplierNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnnotatedClassLoader(
                null,
                LogManager.getLogger()
        );
    }

    @Test
    public void construct_LoggerNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnnotatedClassLoader(
                () -> Arrays.asList(createData(MockPluginOne.class), createData(MockPluginTwo.class)),
                null
        );
    }

    @Test
    public void load_AnnotationClassNull_NullPointerException() {
        AnnotatedClassLoader loader = new AnnotatedClassLoader(
                () -> Arrays.asList(createData(MockPluginOne.class), createData(MockPluginTwo.class)),
                LogManager.getLogger()
        );

        expectedException.expect(NullPointerException.class);
        loader.load(
                null,
                MoreMcmetaTexturePlugin.class
        );
    }

    @Test
    public void load_SuperClassNull_NullPointerException() {
        AnnotatedClassLoader loader = new AnnotatedClassLoader(
                () -> Arrays.asList(createData(MockPluginOne.class), createData(MockPluginTwo.class)),
                LogManager.getLogger()
        );

        expectedException.expect(NullPointerException.class);
        loader.load(
                MoreMcmetaClientPlugin.class,
                null
        );
    }

    @Test
    public void load_AllClassesMatch_AllLoaded() {
        AnnotatedClassLoader loader = new AnnotatedClassLoader(
                () -> Arrays.asList(createData(MockPluginOne.class), createData(MockPluginTwo.class)),
                LogManager.getLogger()
        );

        Collection<MoreMcmetaTexturePlugin> plugins = loader.load(
                MoreMcmetaClientPlugin.class,
                MoreMcmetaTexturePlugin.class
        );

        assertEquals(2, plugins.size());

        Set<String> pluginNames = new HashSet<>();
        for (MoreMcmetaTexturePlugin plugin : plugins) {
            pluginNames.add(plugin.id());
        }
        assertEquals(ImmutableSet.of("one", "two"), pluginNames);
    }

    @Test
    public void load_SomeClassesMatch_AllLoaded() {
        AnnotatedClassLoader loader = new AnnotatedClassLoader(
                () -> Arrays.asList(
                        createData(MockPluginTwo.class),
                        createData(MockPluginThree.class),
                        createData(MockPluginOne.class)
                ),
                LogManager.getLogger()
        );

        Collection<MoreMcmetaTexturePlugin> plugins = loader.load(
                MoreMcmetaClientPlugin.class,
                MoreMcmetaTexturePlugin.class
        );

        assertEquals(2, plugins.size());

        Set<String> pluginNames = new HashSet<>();
        for (MoreMcmetaTexturePlugin plugin : plugins) {
            pluginNames.add(plugin.id());
        }
        assertEquals(ImmutableSet.of("one", "two"), pluginNames);
    }

    @Test
    public void load_NoClassesMatch_AllLoaded() {
        AnnotatedClassLoader loader = new AnnotatedClassLoader(
                () -> Arrays.asList(createData(MockPluginThree.class), createData(MockPluginFour.class)),
                LogManager.getLogger()
        );

        Collection<MoreMcmetaTexturePlugin> plugins = loader.load(
                MoreMcmetaClientPlugin.class,
                MoreMcmetaTexturePlugin.class
        );

        assertEquals(0, plugins.size());

        Set<String> pluginNames = new HashSet<>();
        for (MoreMcmetaTexturePlugin plugin : plugins) {
            pluginNames.add(plugin.id());
        }
        assertEquals(ImmutableSet.of(), pluginNames);
    }

    @Test
    public void load_ClassHasNoDefaultConstructor_AllLoaded() {
        AnnotatedClassLoader loader = new AnnotatedClassLoader(
                () -> Arrays.asList(
                        createData(MockPluginTwo.class),
                        createData(PluginWithoutDefaultConstructor.class),
                        createData(MockPluginOne.class)
                ),
                LogManager.getLogger()
        );

        Collection<MoreMcmetaTexturePlugin> plugins = loader.load(
                MoreMcmetaClientPlugin.class,
                MoreMcmetaTexturePlugin.class
        );

        assertEquals(2, plugins.size());

        Set<String> pluginNames = new HashSet<>();
        for (MoreMcmetaTexturePlugin plugin : plugins) {
            pluginNames.add(plugin.id());
        }
        assertEquals(ImmutableSet.of("one", "two"), pluginNames);
    }

    @Test
    public void load_ClassHasLinkageError_AllLoaded() {
        AnnotatedClassLoader loader = new AnnotatedClassLoader(
                () -> Arrays.asList(
                        createData(MockPluginTwo.class),
                        createData(LinkageErrorPlugin.class),
                        createData(MockPluginOne.class)
                ),
                LogManager.getLogger()
        );

        Collection<MoreMcmetaTexturePlugin> plugins = loader.load(
                MoreMcmetaClientPlugin.class,
                MoreMcmetaTexturePlugin.class
        );

        assertEquals(2, plugins.size());

        Set<String> pluginNames = new HashSet<>();
        for (MoreMcmetaTexturePlugin plugin : plugins) {
            pluginNames.add(plugin.id());
        }
        assertEquals(ImmutableSet.of("one", "two"), pluginNames);
    }

    /**
     * A mock texture plugin.
     * @author soir20
     */
    public static final class MockPluginOne implements MoreMcmetaTexturePlugin {

        @Override
        public String id() {
            return "one";
        }

        @Override
        public String sectionName() {
            return null;
        }

        @Override
        public MetadataAnalyzer analyzer() {
            return null;
        }

        @Override
        public ComponentBuilder componentBuilder() {
            return null;
        }
    }

    /**
     * A mock texture plugin.
     * @author soir20
     */
    public static final class MockPluginTwo implements MoreMcmetaTexturePlugin {

        @Override
        public String id() {
            return "two";
        }

        @Override
        public String sectionName() {
            return null;
        }

        @Override
        public MetadataAnalyzer analyzer() {
            return null;
        }

        @Override
        public ComponentBuilder componentBuilder() {
            return null;
        }
    }

    /**
     * A mock reader plugin.
     * @author soir20
     */
    public static final class MockPluginThree implements MoreMcmetaMetadataParserPlugin {

        @Override
        public String id() {
            return "three";
        }

        @Override
        public String extension() {
            return null;
        }

        @Override
        public MetadataParser metadataParser() {
            return null;
        }
    }

    /**
     * A mock reader plugin.
     * @author soir20
     */
    public static final class MockPluginFour implements MoreMcmetaMetadataParserPlugin {

        @Override
        public String id() {
            return "four";
        }

        @Override
        public String extension() {
            return null;
        }

        @Override
        public MetadataParser metadataParser() {
            return null;
        }
    }

    /**
     * A mock plugin with no default constructor.
     * @author soir20
     */
    public static final class PluginWithoutDefaultConstructor implements MoreMcmetaTexturePlugin {
        private final String NAME;

        public PluginWithoutDefaultConstructor(String name) {
            NAME = name;
        }

        @Override
        public String id() {
            return NAME;
        }

        @Override
        public String sectionName() {
            return null;
        }

        @Override
        public MetadataAnalyzer analyzer() {
            return null;
        }

        @Override
        public ComponentBuilder componentBuilder() {
            return null;
        }
    }

    /**
     * A mock plugin that throws a {@link LinkageError} when constructed.
     * @author soir20
     */
    public static final class LinkageErrorPlugin implements MoreMcmetaTexturePlugin {

        public LinkageErrorPlugin() {
            throw new LinkageError("dummy exception");
        }

        @Override
        public String id() {
            return "bad_plugin";
        }

        @Override
        public String sectionName() {
            return null;
        }

        @Override
        public MetadataAnalyzer analyzer() {
            return null;
        }

        @Override
        public ComponentBuilder componentBuilder() {
            return null;
        }
    }

    /**
     * Creates dummy annotation data.
     * @param pluginClass       class of the plugin to be loaded
     * @return dummy annotation data for the plugin
     */
    private static ModFileScanData.AnnotationData createData(Class<?> pluginClass) {
        return new ModFileScanData.AnnotationData(
                Type.getType(MoreMcmetaClientPlugin.class),
                ElementType.TYPE,
                Type.getType(pluginClass),
                pluginClass.getName(),
                new HashMap<>()
        );
    }

}