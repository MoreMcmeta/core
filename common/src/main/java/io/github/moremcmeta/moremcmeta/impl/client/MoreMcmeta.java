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

package io.github.moremcmeta.moremcmeta.impl.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import io.github.moremcmeta.moremcmeta.api.client.ClientPlugin;
import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaMetadataReaderPlugin;
import io.github.moremcmeta.moremcmeta.api.client.MoreMcmetaTexturePlugin;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataReader;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.impl.client.resource.MetadataRegistryImpl;
import io.github.moremcmeta.moremcmeta.impl.client.resource.ModRepositorySource;
import io.github.moremcmeta.moremcmeta.impl.client.resource.OrderedResourceRepository;
import io.github.moremcmeta.moremcmeta.impl.client.resource.SpriteFrameSizeFixPack;
import io.github.moremcmeta.moremcmeta.impl.client.resource.StagedResourceReloadListener;
import io.github.moremcmeta.moremcmeta.impl.client.resource.TextureCache;
import io.github.moremcmeta.moremcmeta.impl.client.resource.TextureLoader;
import io.github.moremcmeta.moremcmeta.impl.client.texture.EventDrivenTexture;
import io.github.moremcmeta.moremcmeta.impl.client.texture.LazyTextureManager;
import io.github.moremcmeta.moremcmeta.impl.client.texture.SpriteFinder;
import io.github.moremcmeta.moremcmeta.impl.client.texture.TextureFinisher;
import io.github.moremcmeta.moremcmeta.impl.client.texture.TexturePreparer;
import io.github.moremcmeta.moremcmeta.impl.client.adapter.AtlasAdapter;
import io.github.moremcmeta.moremcmeta.impl.client.adapter.NativeImageAdapter;
import io.github.moremcmeta.moremcmeta.impl.client.adapter.PackResourcesAdapter;
import io.github.moremcmeta.moremcmeta.impl.client.adapter.TextureManagerAdapter;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureData;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureDataAssembler;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureDataReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
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

    private final Set<String> DEFAULT_PLUGINS = Set.of();

    /**
     * Begins the startup process, creating necessary objects and registering the
     * resource reload listener.
     * @throws ClientPlugin.InvalidPluginException if one of the plugins did not provide
     *         all required objects
     * @throws ClientPlugin.ConflictingPluginsException if two plugins are not compatible for
     *         any reason
     */
    public void start() throws ClientPlugin.InvalidPluginException,
            ClientPlugin.ConflictingPluginsException {

        Minecraft minecraft = Minecraft.getInstance();
        Logger logger = LogManager.getLogger();

        // Fetch and validate plugins
        Pair<Collection<MoreMcmetaTexturePlugin>, Collection<MoreMcmetaMetadataReaderPlugin>> plugins = dividePlugins(
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

        Collection<MoreMcmetaMetadataReaderPlugin> readerPlugins = plugins.getSecond();
        validateIndividualReaderPlugins(readerPlugins);
        readerPlugins = removeOverriddenPlugins(
                readerPlugins,
                MoreMcmetaMetadataReaderPlugin::extension,
                DEFAULT_PLUGINS,
                logger
        );
        checkItemConflict(readerPlugins, MoreMcmetaMetadataReaderPlugin::extension, "extension");

        List<ClientPlugin> allPlugins = Stream.concat(texturePlugins.stream(), readerPlugins.stream()).toList();
        checkItemConflict(allPlugins, ClientPlugin::displayName, "display name");

        // Texture manager
        SpriteFinder spriteFinder = new SpriteFinder((loc) -> new AtlasAdapter(loc, mipmapLevelGetter(logger)));
        TextureFinisher finisher = new TextureFinisher(spriteFinder, preparer());
        LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> manager = new LazyTextureManager<>(
                new TextureManagerAdapter(minecraft::getTextureManager, unregisterAction()),
                finisher
        );

        // Resource loaders
        TextureDataReader<NativeImageAdapter> reader = new TextureDataReader<>(
                texturePlugins,
                (stream, blur, clamp) -> new NativeImageAdapter(NativeImage.read(stream), 0, blur, clamp)
        );
        TextureLoader<TextureData<NativeImageAdapter>> loader = new TextureLoader<>(
                reader,
                readersByExtension(readerPlugins),
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

            ModRepositorySource source = new ModRepositorySource(() -> {
                OrderedResourceRepository repository = getResourceRepository(packRepository);

                List<String> currentPackIds = packIdGetter.get();

                cache.load(repository, Set.of("textures", "optifine"), currentPackIds);
                return new SpriteFrameSizeFixPack(cache.get(currentPackIds), repository);
            });

            addRepositorySource(packRepository, source);

            /* Even though this is not the normal way to register reload listeners in Fabric,
               registering our listener like a vanilla listener ensures it is executed
               before the TextureManager resets its textures. This is the least invasive way to
               animate preloaded title screen textures. */
            rscManager.registerReloadListener(wrapListener(new TextureResourceReloadListener(
                    manager,
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
     * Begins ticking the {@link LazyTextureManager} on a mod loader.
     * @param texManager        the manager to begin ticking
     */
    protected abstract void startTicking(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager);

    /**
     * Divides the collection of all plugins into their separate subtypes.
     * @param plugins       the plugins to divide
     * @return divided texture and metadata plugins
     */
    private Pair<Collection<MoreMcmetaTexturePlugin>, Collection<MoreMcmetaMetadataReaderPlugin>> dividePlugins(
            Collection<ClientPlugin> plugins
    ) {
        Collection<MoreMcmetaTexturePlugin> texturePlugins = new ArrayList<>();
        Collection<MoreMcmetaMetadataReaderPlugin> readerPlugins = new ArrayList<>();
        for (ClientPlugin plugin : plugins) {
            if (plugin instanceof MoreMcmetaTexturePlugin) {
                texturePlugins.add((MoreMcmetaTexturePlugin) plugin);
            }

            /* If a plugin implements both interfaces, we will throw a conflicting name exception later
               instead of ignoring the other interface. */
            if (plugin instanceof MoreMcmetaMetadataReaderPlugin) {
                readerPlugins.add((MoreMcmetaMetadataReaderPlugin) plugin);
            }

        }

        return Pair.of(texturePlugins, readerPlugins);
    }

    /**
     * Removes default plugins that are overridden by other plugins if they share an item.
     * @param plugins               main collection of plugins (with user-provided plugins)
     * @param itemRetriever         retrieves the item that will be used to override plugins
     * @param defaultPluginNames    unique names of default plugins
     * @param logger                logger to report warnings and errors
     * @return the new collection of plugins with overridden plugins removed
     * @param <P>   plugin type
     * @param <T>   item type
     */
    private <P extends ClientPlugin, T> Collection<P> removeOverriddenPlugins(
            Collection<P> plugins,
            Function<P, T> itemRetriever,
            Set<String> defaultPluginNames,
            Logger logger) {

        Map<T, Long> countBySection = plugins.stream()
                .collect(Collectors.groupingBy(
                        itemRetriever,
                        Collectors.counting()
                ));

        Set<P> results = new HashSet<>();

        plugins.forEach((plugin) -> {
            T item = itemRetriever.apply(plugin);
            String displayName = plugin.displayName();

            /* Disable default plugin if there is a user-provided plugin with the same item.
               It has already been validated that no two plugins have the same display name, so only
               default plugins will be disabled. */
            if (countBySection.get(item) > 1 && defaultPluginNames.contains(displayName)) {
                logger.info("Disabled default plugin " + plugin.displayName()
                        + " as a replacement plugin was provided");
            } else {
                results.add(plugin);
            }

        });

        return results;
    }

    /**
     * Checks all the registered texture plugins to make sure they are individually valid.
     * @param plugins   plugins to validate
     * @throws ClientPlugin.InvalidPluginException if a plugin is not valid
     */
    private void validateIndividualTexturePlugins(Collection<MoreMcmetaTexturePlugin> plugins)
            throws ClientPlugin.InvalidPluginException {

        // Validate individual plugins
        for (MoreMcmetaTexturePlugin plugin : plugins) {
            validatePluginItem(plugin.displayName(), "display name", plugin.displayName());
            validatePluginItem(plugin.sectionName(), "section name", plugin.displayName());
            validatePluginItem(plugin.parser(), "parser", plugin.displayName());
            validatePluginItem(plugin.componentProvider(), "component provider", plugin.displayName());
        }

    }

    /**
     * Checks all the reader registered plugins to make sure they are individually valid.
     * @param plugins   plugins to validate
     * @throws ClientPlugin.InvalidPluginException if a plugin is not valid
     */
    private void validateIndividualReaderPlugins(Collection<MoreMcmetaMetadataReaderPlugin> plugins)
            throws ClientPlugin.InvalidPluginException {

        // Validate individual plugins
        for (MoreMcmetaMetadataReaderPlugin plugin : plugins) {
            validatePluginItem(plugin.displayName(), "display name", plugin.displayName());

            String extension = plugin.extension();
            validatePluginItem(extension, "extension", plugin.displayName());
            if (extension.contains(".")) {
                throw new ClientPlugin.InvalidPluginException("Extension cannot contain a period (.)");
            }

            if (extension.length() == 0) {
                throw new ClientPlugin.InvalidPluginException("Extension cannot be empty");
            }

            validatePluginItem(plugin.metadataReader(), "metadata reader", plugin.displayName());
        }

    }

    /**
     * Validates that an individual item in a plugin is present (non-null).
     * @param item          item to validate
     * @param itemName      display name of the item
     * @param pluginName    display name of the plugin
     * @throws ClientPlugin.InvalidPluginException if the item is not present
     */
    private void validatePluginItem(Object item, String itemName, String pluginName)
            throws ClientPlugin.InvalidPluginException {

        if (item == null) {
            throw new ClientPlugin.InvalidPluginException("Plugin " + pluginName + " is missing " + itemName);
        }
    }

    /**
     * Checks the provided plugins and throws a {@link ClientPlugin.ConflictingPluginsException}
     * if any two have a duplicate value for the property being checked.
     * @param plugins               the plugins to check
     * @param propertyAccessor      the function to use to access the property
     * @param propertyName          display name of the property
     * @param <P> type of plugin
     * @param <T> type of the property values
     * @throws ClientPlugin.ConflictingPluginsException if two plugins have the same value returned
     *                                                            by the propertyAccessor
     */
    private <P extends ClientPlugin, T> void checkItemConflict(
            Collection<P> plugins,
            Function<P, T> propertyAccessor,
            String propertyName)
            throws ClientPlugin.ConflictingPluginsException {

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

        // Report section name and plugin display names to help diagnose the issue
        T conflictingProperty = conflictingPlugins.get().getKey();
        List<String> conflictingPluginNames = conflictingPlugins.get().getValue()
                .stream()
                .map(ClientPlugin::displayName)
                .toList();

        throw new ClientPlugin.ConflictingPluginsException("Plugins " + conflictingPluginNames
                + " have conflicting " + propertyName + ": " + conflictingProperty);
    }

    /**
     * Associates all registered {@link MetadataReader}s with their extensions.
     * @param readerPlugins     all the metadata reader plugins
     * @return a map that is a valid input to a {@link TextureLoader}
     */
    private ImmutableMap<String, MetadataReader> readersByExtension(
            Iterable<MoreMcmetaMetadataReaderPlugin> readerPlugins) {

        ImmutableMap.Builder<String, MetadataReader> readers = new ImmutableMap.Builder<>();
        for (MoreMcmetaMetadataReaderPlugin plugin : readerPlugins) {
            readers.put(
                    "." + plugin.extension(),
                    plugin.metadataReader()
            );
        }

        return readers.build();
    }

    /**
     * Gets the repository containing all the game's resources except the pack this mod adds.
     * @param packRepository the repository containing all packs
     * @return the repository with all resources
     */
    private OrderedResourceRepository getResourceRepository(PackRepository packRepository) {
        List<PackResourcesAdapter> otherPacks = new ArrayList<>(packRepository.getSelectedPacks()
                .stream()
                .filter((pack) -> !pack.getId().equals(ModRepositorySource.PACK_ID))
                .map(Pack::open)
                .map(PackResourcesAdapter::new)
                .toList());

        Collections.reverse(otherPacks);

        return new OrderedResourceRepository(PackType.CLIENT_RESOURCES, otherPacks);
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
     * @param logger            logger to report warnings or errors
     */
    private void addCompletedReloadCallback(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> manager,
                                            Logger logger) {
        Optional<LoadingOverlay> overlay = loadingOverlay(logger);
        if (overlay.isEmpty()) {
            return;
        }

        Optional<ReloadInstance> reloadInstance = reloadInstance(overlay.get(), logger);
        reloadInstance.ifPresent((instance) -> instance.done().thenRun(manager::finishQueued));
    }

    /**
     * Loads and queues textures controlled by this mod on resource reloading. Clears out old textures that
     * no longer have metadata for this mod.
     * @author soir20
     */
    private class TextureResourceReloadListener
            implements StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> {
        private final Map<ResourceLocation, EventDrivenTexture.Builder> LAST_TEXTURES_ADDED = new HashMap<>();
        private final TextureCache<TextureData<NativeImageAdapter>, List<String>> CACHE;
        private final Supplier<List<String>> PACK_ID_GETTER;
        private final LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> TEX_MANAGER;
        private final Logger LOGGER;

        /**
         * Creates a new resource reload listener.
         * @param texManager            texture manager that accepts queued textures
         * @param cache                 cache for texture data that should be loaded
         * @param packIdGetter          gets the IDs of the currently-selected packs
         * @param logger                a logger to write output
         */
        public TextureResourceReloadListener(
                LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager,
                TextureCache<TextureData<NativeImageAdapter>, List<String>> cache,
                Supplier<List<String>> packIdGetter,
                Logger logger
        ) {
            TEX_MANAGER = requireNonNull(texManager, "Texture manager cannot be null");
            CACHE = requireNonNull(cache, "Cache cannot be null");
            PACK_ID_GETTER = requireNonNull(packIdGetter, "Pack ID getter cannot be null");
            LOGGER = requireNonNull(logger, "Logger cannot be null");
        }

        /**
         * Loads textures and adds the resource pack to fix animated sprites.
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
                    (image) -> {
                        int maxMipmap = Minecraft.getInstance().options.mipmapLevels;
                        NativeImage[] mipmaps = MipmapGenerator.generateMipLevels(image.image(), maxMipmap);

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

            METADATA_REGISTRY.set(CACHE.get(PACK_ID_GETTER.get()));

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

            addCompletedReloadCallback(TEX_MANAGER, LOGGER);

            return CompletableFuture.runAsync(() -> {
                LAST_TEXTURES_ADDED.keySet().forEach(TEX_MANAGER::unregister);
                LAST_TEXTURES_ADDED.clear();
                LAST_TEXTURES_ADDED.putAll(data);

                data.forEach(TEX_MANAGER::register);
            }, applyExecutor);
        }

    }

}
