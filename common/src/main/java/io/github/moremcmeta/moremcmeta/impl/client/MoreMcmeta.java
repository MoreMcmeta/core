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

package io.github.moremcmeta.moremcmeta.impl.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import io.github.moremcmeta.moremcmeta.api.client.ClientPlugin;
import io.github.moremcmeta.moremcmeta.api.client.ConflictingPluginsException;
import io.github.moremcmeta.moremcmeta.api.client.InvalidPluginException;
import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaMetadataParserPlugin;
import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaTexturePlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.impl.client.adapter.AtlasAdapter;
import io.github.moremcmeta.moremcmeta.impl.client.adapter.NativeImageAdapter;
import io.github.moremcmeta.moremcmeta.impl.client.adapter.PackResourcesAdapter;
import io.github.moremcmeta.moremcmeta.impl.client.adapter.RootResourcesAdapter;
import io.github.moremcmeta.moremcmeta.impl.client.adapter.TextureManagerAdapter;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureData;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureDataAssembler;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureDataReader;
import io.github.moremcmeta.moremcmeta.impl.client.mixin.TextureManagerAccessor;
import io.github.moremcmeta.moremcmeta.impl.client.resource.MetadataRegistryImpl;
import io.github.moremcmeta.moremcmeta.impl.client.resource.ModRepositorySource;
import io.github.moremcmeta.moremcmeta.impl.client.resource.OrderedResourceRepository;
import io.github.moremcmeta.moremcmeta.impl.client.resource.ResourceCollection;
import io.github.moremcmeta.moremcmeta.impl.client.resource.SpriteFrameSizeFixPack;
import io.github.moremcmeta.moremcmeta.impl.client.resource.StagedResourceReloadListener;
import io.github.moremcmeta.moremcmeta.impl.client.resource.TextureCache;
import io.github.moremcmeta.moremcmeta.impl.client.resource.TextureLoader;
import io.github.moremcmeta.moremcmeta.impl.client.texture.BaseCollection;
import io.github.moremcmeta.moremcmeta.impl.client.texture.EventDrivenTexture;
import io.github.moremcmeta.moremcmeta.impl.client.texture.SpriteFinder;
import io.github.moremcmeta.moremcmeta.impl.client.texture.TextureManagerWrapper;
import io.github.moremcmeta.moremcmeta.impl.client.texture.TexturePreparer;
import io.github.moremcmeta.moremcmeta.impl.client.texture.UploadComponent;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * An entrypoint with common elements of the startup process in both
 * the Forge and Fabric mod loaders.
 * @author soir20
 */
public abstract class MoreMcmeta {

    /**
     * Mod ID for Forge and Fabric.
     */
    public static final String MODID = "moremcmeta";

    /**
     * Registry for the latest metadata. MODDERS: This is an internal field.
     * Use {@link MetadataRegistry#INSTANCE} to access the registry.
     */
    public static final MetadataRegistryImpl METADATA_REGISTRY = new MetadataRegistryImpl();

    private static ImmutableMap<ResourceLocation, ImmutableSet<ResourceLocation>> dependencies = ImmutableMap.of();

    private final Set<String> DEFAULT_PLUGINS = Set.of(
            "moremcmeta_texture_plugin",
            "moremcmeta_animation_plugin",
            "moremcmeta_gui_plugin",
            "moremcmeta_properties_parser_plugin",
            "moremcmeta_moremcmeta_parser_plugin"
    );

    /**
     * Gets all textures that have the given texture as a base.
     * @param baseName      full path of the base texture
     * @return all textures that have the given texture as a base
     */
    public static Set<ResourceLocation> dependencies(ResourceLocation baseName) {
        requireNonNull(baseName, "Base name cannot be null");
        return dependencies.getOrDefault(baseName, ImmutableSet.of());
    }

    /**
     * Begins the startup process, creating necessary objects and registering the
     * resource reload listener.
     * @throws InvalidPluginException if one of the plugins did not provide
     *         all required objects
     * @throws ConflictingPluginsException if two plugins are not compatible for
     *         any reason
     */
    public void start() throws InvalidPluginException,
            ConflictingPluginsException {

        Minecraft minecraft = Minecraft.getInstance();
        Logger logger = LogManager.getLogger();

        // Fetch and validate plugins
        Pair<Collection<MoreMcmetaTexturePlugin>, Collection<MoreMcmetaMetadataParserPlugin>> plugins = dividePlugins(
                fetchTexturePlugins(logger)
        );

        Collection<MoreMcmetaTexturePlugin> texturePlugins = plugins.getFirst();
        validateIndividualTexturePlugins(texturePlugins);
        texturePlugins = removeOverriddenPlugins(
                texturePlugins,
                MoreMcmetaTexturePlugin::sectionName,
                DEFAULT_PLUGINS,
                logger
        );
        checkItemConflict(texturePlugins, MoreMcmetaTexturePlugin::sectionName, "section name");

        Collection<MoreMcmetaMetadataParserPlugin> parserPlugins = plugins.getSecond();
        validateIndividualParserPlugins(parserPlugins);
        parserPlugins = removeOverriddenPlugins(
                parserPlugins,
                MoreMcmetaMetadataParserPlugin::extension,
                DEFAULT_PLUGINS,
                logger
        );
        checkItemConflict(parserPlugins, MoreMcmetaMetadataParserPlugin::extension, "extension");

        List<ClientPlugin> allPlugins = Stream.concat(texturePlugins.stream(), parserPlugins.stream()).toList();
        checkItemConflict(allPlugins, ClientPlugin::id, "id");

        logPluginList(allPlugins, logger);

        // Texture manager
        TextureManagerWrapper<EventDrivenTexture> manager =
                new TextureManagerWrapper<>(
                        new TextureManagerAdapter(minecraft::getTextureManager, unregisterAction())
                );

        // Resource loaders
        TextureDataReader<NativeImageAdapter> reader = new TextureDataReader<>(
                texturePlugins,
                (stream) -> new NativeImageAdapter(NativeImage.read(stream), 0, false, false),
                (image, blur, clamp) -> new NativeImageAdapter(image.image(), image.mipmapLevel(), blur, clamp)
        );
        TextureLoader<TextureData<NativeImageAdapter>> loader = new TextureLoader<>(
                reader,
                parsersByExtension(parserPlugins),
                logger
        );

        // Cache
        final TextureCache<TextureData<NativeImageAdapter>, List<String>> cache = new TextureCache<>(loader);

        // Listener registration and add resource pack
        onResourceManagerInitialized((client) -> {
            if (!(client.getResourceManager() instanceof ReloadableResourceManager rscManager)) {
                logger.error("Reload listener was not added because resource manager is not reloadable");
                return;
            }

            PackRepository packRepository = client.getResourcePackRepository();
            Supplier<List<String>> packIdGetter = () -> packRepository.getSelectedPacks().stream()
                    .map(Pack::getId)
                    .toList();

            int packVersion = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
            ModRepositorySource source = new ModRepositorySource(
                    new Pack.ResourcesSupplier() {
                        @Override
                        public @NotNull PackResources openPrimary(String packId) {
                            OrderedResourceRepository repository = makeResourceRepository(packRepository);

                            List<String> currentPackIds = packIdGetter.get();

                            cache.load(repository, currentPackIds, "textures", "optifine");
                            METADATA_REGISTRY.set(cache.get(currentPackIds));

                            ResourceLocation packIcon = new ResourceLocation(MODID, "pack.png");

                            return new SpriteFrameSizeFixPack(
                                    cache.get(currentPackIds),
                                    ImmutableMap.of(
                                            "pack.png",
                                            () -> repository.firstCollectionWith(packIcon)
                                                    .collection()
                                                    .find(PackType.CLIENT_RESOURCES, packIcon),
                                            "pack.mcmeta",
                                            () -> makePackMetadataStream(packVersion, ModRepositorySource.DESCRIPTION)
                                    )
                            );
                        }

                        @Override
                        public @NotNull PackResources openFull(String packId, Pack.Info info) {
                            return openPrimary(packId);
                        }
                    }
            );

            addRepositorySource(packRepository, source);

            /* Even though this is not the normal way to register reload listeners in Fabric,
               registering our listener like a vanilla listener ensures it is executed
               before the TextureManager resets its textures. This is the least invasive way to
               animate preloaded title screen textures. */
            rscManager.registerReloadListener(wrapListener(new TextureResourceReloadListener(
                    manager,
                    preparer(),
                    cache,
                    packIdGetter,
                    logger
            )));
            logger.debug("Added texture reload listener");

        });

        // Enable animation by ticking the manager
        startTicking(manager);

    }

    /**
     * Gets all loaded MoreMcmeta plugins from other mods.
     * @param logger    logger to report errors
     * @return all loaded plugins
     */
    protected abstract Collection<ClientPlugin> fetchTexturePlugins(Logger logger);

    /**
     * Gets the function that converts atlas sprites to their mipmap level.
     * @param logger        logger to report warnings or errors
     * @return the mipmap level getter
     */
    protected abstract ToIntFunction<TextureAtlasSprite> mipmapLevelGetter(Logger logger);

    /**
     * Gets the OpenGL preparer for new textures on this loader.
     * @return the OpenGL preparer for this loader
     */
    protected abstract TexturePreparer preparer();

    /**
     * Gets the action that should be executed to unregister a texture on a specific mod loader.
     * @return the action that will unregister textures
     */
    protected abstract BiConsumer<TextureManager, ResourceLocation> unregisterAction();

    /**
     * Executes a callback when the vanilla resource manager is initialized in a mod loader.
     * @param callback      the callback to execute
     */
    protected abstract void onResourceManagerInitialized(Consumer<Minecraft> callback);

    /**
     * Adds a repository source to Minecraft's {@link PackRepository}.
     * @param packRepository        the repository to add a source to
     * @param repositorySource      the source to add
     */
    protected abstract void addRepositorySource(PackRepository packRepository, RepositorySource repositorySource);

    /**
     * Wraps the given resource reload listener in any mod loader-specific interfaces, if necessary.
     * @param original      the original resource reload listener to wrap
     * @return the wrapped resource reload listener
     */
    protected abstract StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> wrapListener(
            StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> original
    );

    /**
     * Gets the current reload instance.
     * @param overlay    the overlay containing the instance
     * @param logger     a logger to write output
     * @return the current reload instance
     */
    protected abstract Optional<ReloadInstance> reloadInstance(LoadingOverlay overlay, Logger logger);

    /**
     * Begins ticking the {@link TextureManagerWrapper} on a mod loader.
     * @param texManager        the manager to begin ticking
     */
    protected abstract void startTicking(TextureManagerWrapper<EventDrivenTexture> texManager);

    /**
     * Divides the collection of all plugins into their separate subtypes.
     * @param plugins       the plugins to divide
     * @return divided texture and metadata plugins
     */
    private Pair<Collection<MoreMcmetaTexturePlugin>, Collection<MoreMcmetaMetadataParserPlugin>> dividePlugins(
            Collection<ClientPlugin> plugins
    ) {
        Collection<MoreMcmetaTexturePlugin> texturePlugins = new ArrayList<>();
        Collection<MoreMcmetaMetadataParserPlugin> parserPlugins = new ArrayList<>();
        for (ClientPlugin plugin : plugins) {
            if (plugin instanceof MoreMcmetaTexturePlugin) {
                texturePlugins.add((MoreMcmetaTexturePlugin) plugin);
            }

            /* If a plugin implements both interfaces, we will throw a conflicting name exception later
               instead of ignoring the other interface. */
            if (plugin instanceof MoreMcmetaMetadataParserPlugin) {
                parserPlugins.add((MoreMcmetaMetadataParserPlugin) plugin);
            }

        }

        return Pair.of(texturePlugins, parserPlugins);
    }

    /**
     * Removes default plugins that are overridden by other plugins if they share an item.
     * @param plugins               main collection of plugins (with user-provided plugins)
     * @param itemRetriever         retrieves the item that will be used to override plugins
     * @param defaultPluginIds      unique IDs of default plugins
     * @param logger                logger to report warnings and errors
     * @return the new collection of plugins with overridden plugins removed
     * @param <P>   plugin type
     * @param <T>   item type
     */
    private <P extends ClientPlugin, T> Collection<P> removeOverriddenPlugins(
            Collection<P> plugins,
            Function<P, T> itemRetriever,
            Set<String> defaultPluginIds,
            Logger logger) {

        Map<T, Long> countBySection = plugins.stream()
                .collect(Collectors.groupingBy(
                        itemRetriever,
                        Collectors.counting()
                ));

        Set<P> results = new HashSet<>();

        plugins.forEach((plugin) -> {
            T item = itemRetriever.apply(plugin);
            String id = plugin.id();

            /* Disable default plugin if there is a user-provided plugin with the same item.
               It has already been validated that no two plugins have the same ID, so only
               default plugins will be disabled. */
            if (countBySection.get(item) > 1 && defaultPluginIds.contains(id)) {
                logger.info("Disabled default plugin {} as a replacement plugin was provided", plugin.id());
            } else {
                results.add(plugin);
            }

        });

        return results;
    }

    /**
     * Checks all the registered texture plugins to make sure they are individually valid.
     * @param plugins   plugins to validate
     * @throws InvalidPluginException if a plugin is not valid
     */
    private void validateIndividualTexturePlugins(Collection<MoreMcmetaTexturePlugin> plugins)
            throws InvalidPluginException {

        // Validate individual plugins
        for (MoreMcmetaTexturePlugin plugin : plugins) {
            requirePluginItem(plugin.id(), "id", plugin.id());
            validateId(plugin.id());
            requirePluginItem(plugin.sectionName(), "section name", plugin.id());
            requirePluginItem(plugin.analyzer(), "analyzer", plugin.id());
            requirePluginItem(plugin.componentBuilder(), "component builder", plugin.id());
        }

    }

    /**
     * Checks all the analyzer registered plugins to make sure they are individually valid.
     * @param plugins   plugins to validate
     * @throws InvalidPluginException if a plugin is not valid
     */
    private void validateIndividualParserPlugins(Collection<MoreMcmetaMetadataParserPlugin> plugins)
            throws InvalidPluginException {

        // Validate individual plugins
        for (MoreMcmetaMetadataParserPlugin plugin : plugins) {
            requirePluginItem(plugin.id(), "id", plugin.id());
            validateId(plugin.id());

            String extension = plugin.extension();
            requirePluginItem(extension, "extension", plugin.id());
            String regex = "[a-z0-9_-]+";
            if (!extension.matches(regex)) {
                throw new InvalidPluginException("Extension must match " + regex);
            }
            if (extension.equals("mcmeta")) {
                throw new InvalidPluginException("Implementing .mcmeta parser extensions is not allowed " +
                        "due to the way Minecraft implements resource packs");
            }

            if (extension.isEmpty()) {
                throw new InvalidPluginException("Extension cannot be empty");
            }

            requirePluginItem(plugin.metadataParser(), "metadata analyzer", plugin.id());
        }

    }

    /**
     * Validates that an individual item in a plugin is present (non-null).
     * @param item          item to validate
     * @param itemName      display name of the item
     * @param pluginId      ID of the plugin
     * @throws InvalidPluginException if the item is not present
     */
    private void requirePluginItem(Object item, String itemName, String pluginId) throws InvalidPluginException {
        if (item == null) {
            throw new InvalidPluginException("Plugin " + pluginId + " is missing " + itemName);
        }
    }

    /**
     * Validates that an individual item in a plugin is present (non-null).
     * @param id            unique ID of the plugin
     * @throws InvalidPluginException if the item is not present
     */
    private void validateId(String id) throws InvalidPluginException {
        String regex = "[a-z0-9_.-]+";
        if (!id.matches(regex)) {
            throw new InvalidPluginException("Plugin ID must match " + regex + ", but was: " + id);
        }
    }

    /**
     * Checks the provided plugins and throws a {@link ConflictingPluginsException}
     * if any two have a duplicate value for the property being checked.
     * @param plugins               the plugins to check
     * @param propertyAccessor      the function to use to access the property
     * @param propertyName          display name of the property
     * @param <P> type of plugin
     * @param <T> type of the property values
     * @throws ConflictingPluginsException if two plugins have the same value returned
     *                                                            by the propertyAccessor
     */
    private <P extends ClientPlugin, T> void checkItemConflict(
            Collection<P> plugins,
            Function<P, T> propertyAccessor,
            String propertyName)
            throws ConflictingPluginsException {

        Map<T, List<P>> pluginsByProperty = plugins
                .stream()
                .collect(Collectors.groupingBy(propertyAccessor));
        Optional<Map.Entry<T, List<P>>> conflictingPlugins = pluginsByProperty
                .entrySet()
                .stream()
                .filter((entry) -> entry.getValue().size() > 1)
                .findFirst();

        if (conflictingPlugins.isEmpty()) {
            return;
        }

        // Report section name and plugin IDs to help diagnose the issue
        T conflictingProperty = conflictingPlugins.get().getKey();
        List<String> conflictingIds = conflictingPlugins.get().getValue()
                .stream()
                .map(ClientPlugin::id)
                .toList();

        throw new ConflictingPluginsException("Plugins " + conflictingIds + " have conflicting " + propertyName
                + ": " + conflictingProperty);
    }

    /**
     * Associates all registered {@link MetadataParser}s with their extensions.
     * @param parserPlugins     all the metadata analyzer plugins
     * @return a map that is a valid input to a {@link TextureLoader}
     */
    private ImmutableMap<String, MetadataParser> parsersByExtension(
            Iterable<MoreMcmetaMetadataParserPlugin> parserPlugins) {

        ImmutableMap.Builder<String, MetadataParser> analyzers = new ImmutableMap.Builder<>();
        for (MoreMcmetaMetadataParserPlugin plugin : parserPlugins) {
            analyzers.put(
                    "." + plugin.extension(),
                    plugin.metadataParser()
            );
        }

        return analyzers.build();
    }

    /**
     * Writes a list of all plugins to the log.
     * @param plugins       list of loaded plugins
     * @param logger        logger to use to log the list
     */
    private void logPluginList(Collection<ClientPlugin> plugins, Logger logger) {
        String pluginList = plugins.stream()
                .map((plugin) -> "\n\t- " + plugin.id())
                .collect(Collectors.joining());

        logger.info(String.format("Loading %s MoreMcmeta plugins:", plugins.size()) + pluginList);
    }

    /**
     * Gets the repository containing all the game's resources except the pack this mod adds.
     * @param packRepository        the repository containing all packs
     * @return the repository with all resources
     */
    private OrderedResourceRepository makeResourceRepository(PackRepository packRepository) {
        List<ResourceCollection> packs = new ArrayList<>(packRepository.getSelectedPacks()
                .stream()
                .filter((pack) -> !pack.getId().equals(ModRepositorySource.PACK_ID))
                .map(Pack::open)
                .map(PackResourcesAdapter::new)
                .toList());

        Collections.reverse(packs);

        Collection<String> selectedIds = packRepository.getSelectedIds();
        packRepository.getAvailablePacks().stream()
                .filter((pack) -> !selectedIds.contains(pack.getId()))
                .map(Pack::open)
                .map(RootResourcesAdapter::new)
                .forEach(packs::add);

        return new OrderedResourceRepository(PackType.CLIENT_RESOURCES, packs);
    }

    /**
     * Creates a metadata stream for the internal pack metadata.
     * @param version       pack version
     * @param description   pack description
     * @return stream for the internal pack metadata
     */
    private InputStream makePackMetadataStream(int version, String description) {
        return new ByteArrayInputStream(
                String.format("{ \"pack\": { \"pack_format\": %s, \"description\": \"%s\"} }", version, description)
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Gets the current loading overlay if there is one. Returns empty if the current
     * overlay is not a loading overlay.
     * @param logger    a logger to write output
     * @return the loading overlay if there is one
     */
    private Optional<LoadingOverlay> loadingOverlay(Logger logger) {
        Overlay currentOverlay = Minecraft.getInstance().getOverlay();

        if (!(currentOverlay instanceof LoadingOverlay)) {
            logger.error("Loading overlay expected. Textures will not be finished!");
            return Optional.empty();
        }

        return Optional.of((LoadingOverlay) currentOverlay);
    }

    /**
     * Adds a callback for any necessary post-reload work.
     * @param manager           texture manager with unfinished work
     * @param preparer          prepares textures for OpenGL
     * @param textures          most recent textures that have been loaded
     * @param logger            logger to report warnings or errors
     */
    private void addCompletedReloadCallback(TextureManagerWrapper<EventDrivenTexture> manager,
                                            TexturePreparer preparer,
                                            Map<ResourceLocation, EventDrivenTexture.Builder> textures,
                                            Logger logger) {
        Optional<LoadingOverlay> overlay = loadingOverlay(logger);
        if (overlay.isEmpty()) {
            return;
        }

        Map<ResourceLocation, ImmutableSet.Builder<ResourceLocation>> dependencies = new HashMap<>();

        Optional<ReloadInstance> reloadInstance = reloadInstance(overlay.get(), logger);
        reloadInstance.ifPresent((instance) -> instance.done().thenRun(() -> {
            TextureManagerAccessor textureManager = (TextureManagerAccessor) Minecraft.getInstance().getTextureManager();
            SpriteFinder spriteFinder = new SpriteFinder(
                    (loc) -> new AtlasAdapter(loc, mipmapLevelGetter(logger)),
                    textureManager.moremcmeta_byPath().entrySet().stream()
                            .filter((entry) -> entry.getValue() instanceof TextureAtlas)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toSet())
            );

            textures.forEach((location, builder) -> {
                BaseCollection allBases = BaseCollection.find(spriteFinder, location);

                allBases.baseNames().forEach((base) ->
                        dependencies.computeIfAbsent(base, (loc) -> new ImmutableSet.Builder<>())
                                .add(location)
                );

                UploadComponent uploadComponent = new UploadComponent(
                        preparer,
                        allBases
                );
                builder.add(uploadComponent);
                manager.register(location, builder.build());

            });

            MoreMcmeta.dependencies = ImmutableMap.copyOf(Maps.transformValues(dependencies, ImmutableSet.Builder::build));
            AtlasAdapter.clearNameMappings();
        }));
    }

    /**
     * Loads and queues textures controlled by this mod on resource reloading. Clears out old textures that
     * no longer have metadata for this mod.
     * @author soir20
     */
    @MethodsReturnNonnullByDefault
    private class TextureResourceReloadListener
            implements StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> {
        private final Map<ResourceLocation, EventDrivenTexture.Builder> LAST_TEXTURES_ADDED;
        private final TextureManagerWrapper<EventDrivenTexture> TEX_MANAGER;
        private final TexturePreparer PREPARER;
        private final TextureCache<TextureData<NativeImageAdapter>, List<String>> CACHE;
        private final Supplier<List<String>> PACK_ID_GETTER;
        private final Logger LOGGER;

        /**
         * Creates a new resource reload listener.
         * @param texManager            texture manager that accepts queued textures
         * @param preparer              prepares textures for OpenGL
         * @param cache                 cache for texture data that should be loaded
         * @param packIdGetter          gets the IDs of the currently-selected packs
         * @param logger                a logger to write output
         */
        public TextureResourceReloadListener(
                TextureManagerWrapper<EventDrivenTexture> texManager,
                TexturePreparer preparer,
                TextureCache<TextureData<NativeImageAdapter>, List<String>> cache,
                Supplier<List<String>> packIdGetter,
                Logger logger
        ) {
            LAST_TEXTURES_ADDED = new HashMap<>();
            TEX_MANAGER = requireNonNull(texManager, "Texture manager cannot be null");
            PREPARER = requireNonNull(preparer, "Preparer cannot be null");
            CACHE = requireNonNull(cache, "Cache cannot be null");
            PACK_ID_GETTER = requireNonNull(packIdGetter, "Pack ID getter cannot be null");
            LOGGER = requireNonNull(logger, "Logger cannot be null");
        }

        /**
         * Loads textures and adds the resource pack to fix MoreMcmeta-controlled sprites.
         * @param manager           Minecraft's resource manager
         * @param loadProfiler      load stage profiler
         * @param loadExecutor          asynchronously executes load stage tasks
         * @return task returning loaded texture builders by location
         */
        @Override
        public CompletableFuture<Map<ResourceLocation, EventDrivenTexture.Builder>> load(ResourceManager manager,
                                                                                         ProfilerFiller loadProfiler,
                                                                                         Executor loadExecutor) {
            requireNonNull(manager, "Resource manager cannot be null");
            requireNonNull(loadProfiler, "Profiler cannot be null");
            requireNonNull(loadExecutor, "Executor cannot be null");

            TextureDataAssembler<NativeImageAdapter> assembler = new TextureDataAssembler<>(
                    (int width, int height, int mipmapLevel, boolean blur, boolean clamp) -> {
                        NativeImage image = new NativeImage(width, height, true);
                        return new NativeImageAdapter(image, mipmapLevel, blur, clamp);
                    },
                    (image, mipmap) -> {
                        int maxMipmapSettings = Minecraft.getInstance().options.mipmapLevels().get();
                        int maxMipmap = Math.min(maxMipmapSettings, mipmap);
                        NativeImage[] mipmaps = MipmapGenerator.generateMipLevels(
                                new NativeImage[]{ image.image() },
                                maxMipmap
                        );

                        List<NativeImageAdapter> wrappedMipmaps = new ArrayList<>();
                        for (int level = 0; level < mipmaps.length; level++) {
                            wrappedMipmaps.add(new NativeImageAdapter(
                                    mipmaps[level],
                                    level,
                                    image.blur(), image.clamp()
                            ));
                        }

                        return wrappedMipmaps;
                    }
            );

            return CompletableFuture.supplyAsync(() -> CACHE.get(PACK_ID_GETTER.get()).entrySet()
                    .stream().parallel()
                    .collect(
                            Collectors.toMap(Map.Entry::getKey, (entry) -> assembler.assemble(entry.getValue()))
                    ), loadExecutor);
        }

        /**
         * Clears old textures, if any, and registers new ones.
         * @param data          texture builders by location that were just loaded
         * @param manager       Minecraft's resource manager
         * @param applyProfiler      apply stage profiler
         * @param applyExecutor      asynchronously executes apply stage tasks
         * @return task with no return data
         */
        @Override
        public CompletableFuture<Void> apply(Map<ResourceLocation, EventDrivenTexture.Builder> data,
                                             ResourceManager manager, ProfilerFiller applyProfiler,
                                             Executor applyExecutor) {
            requireNonNull(data, "Data cannot be null");
            requireNonNull(manager, "Resource manager cannot be null");
            requireNonNull(applyProfiler, "Profiler cannot be null");
            requireNonNull(applyExecutor, "Executor cannot be null");

            addCompletedReloadCallback(TEX_MANAGER, PREPARER, LAST_TEXTURES_ADDED, LOGGER);

            return CompletableFuture.runAsync(() -> {
                OrderedResourceRepository repository = makeResourceRepository(Minecraft.getInstance().getResourcePackRepository());

                // Clear the cache to avoid using closed resources if there is a reload with the same packs
                CACHE.load(repository, ImmutableList.of());

                LAST_TEXTURES_ADDED.keySet().forEach(TEX_MANAGER::unregister);
                LAST_TEXTURES_ADDED.clear();
                LAST_TEXTURES_ADDED.putAll(data.entrySet().stream().collect(Collectors.toMap(
                        (entry) -> RootResourcesAdapter.locateForPackScreen(entry.getKey()),
                        Map.Entry::getValue
                )));

                /* Clear any existing texture immediately to prevent PreloadedTextures
                   from re-adding themselves. The texture manager will reload before the
                   EventDrivenTextures are added, causing a race condition with the
                   registration CompletableFuture inside PreloadedTexture's reset method. */
                LAST_TEXTURES_ADDED.keySet().forEach(TEX_MANAGER::unregister);

            }, applyExecutor);
        }

    }

}
