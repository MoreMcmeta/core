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

package io.github.soir20.moremcmeta;

import io.github.soir20.moremcmeta.client.adapter.AtlasAdapter;
import io.github.soir20.moremcmeta.client.adapter.NativeImageAdapter;
import io.github.soir20.moremcmeta.client.adapter.PackResourcesAdapter;
import io.github.soir20.moremcmeta.client.adapter.TextureManagerAdapter;
import io.github.soir20.moremcmeta.client.io.TextureData;
import io.github.soir20.moremcmeta.client.io.TextureDataAssembler;
import io.github.soir20.moremcmeta.client.io.TextureDataReader;
import io.github.soir20.moremcmeta.client.resource.ModRepositorySource;
import io.github.soir20.moremcmeta.client.resource.OrderedResourceRepository;
import io.github.soir20.moremcmeta.client.resource.SpriteFrameSizeFixPack;
import io.github.soir20.moremcmeta.client.resource.StagedResourceReloadListener;
import io.github.soir20.moremcmeta.client.resource.TextureCache;
import io.github.soir20.moremcmeta.client.resource.TextureLoader;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import io.github.soir20.moremcmeta.client.texture.SpriteFinder;
import io.github.soir20.moremcmeta.client.texture.TextureFinisher;
import io.github.soir20.moremcmeta.client.texture.TexturePreparer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * An entrypoint with common elements of the startup process in both
 * the Forge and Fabric mod loaders.
 * @author soir20
 */
public abstract class MoreMcmeta {

    /**
     * Begins the startup process, creating necessary objects and registering the
     * resource reload listener.
     */
    public void start() {
        Minecraft minecraft = Minecraft.getInstance();
        Logger logger = LogManager.getLogger();

        // Texture manager
        SpriteFinder spriteFinder = new SpriteFinder((loc) -> new AtlasAdapter(loc, getMipmapLevelGetter(logger)));
        TextureFinisher finisher = new TextureFinisher(spriteFinder, getPreparer());
        LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> manager = new LazyTextureManager<>(
                new TextureManagerAdapter(minecraft::getTextureManager, getUnregisterAction()),
                finisher
        );

        // Resource loaders
        TextureDataReader reader = new TextureDataReader();
        TextureLoader<TextureData<NativeImageAdapter>> loader = new TextureLoader<>(reader, logger);

        // Cache
        final TextureCache<TextureData<NativeImageAdapter>, List<String>> cache = new TextureCache<>(loader);

        // Listener registration and resource manager replacement
        onResourceManagerInitialized((client) -> {
            if (!(client.getResourceManager() instanceof SimpleReloadableResourceManager rscManager)) {
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
     * Gets the function that converts atlas sprites to their mipmap level.
     * @param logger        logger to report warnings or errors
     * @return the mipmap level getter
     */
    protected abstract ToIntFunction<TextureAtlasSprite> getMipmapLevelGetter(Logger logger);

    /**
     * Gets the OpenGL preparer for new textures on this loader.
     * @return the OpenGL preparer for this loader
     */
    protected abstract TexturePreparer getPreparer();

    /**
     * Gets the action that should be executed to unregister a texture on a specific mod loader.
     * @return the action that will unregister textures
     */
    protected abstract BiConsumer<TextureManager, ResourceLocation> getUnregisterAction();

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
    protected abstract Optional<ReloadInstance> getReloadInstance(LoadingOverlay overlay, Logger logger);

    /**
     * Begins ticking the {@link LazyTextureManager} on a mod loader.
     * @param texManager        the manager to begin ticking
     */
    protected abstract void startTicking(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager);

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
    private Optional<LoadingOverlay> getLoadingOverlay(Logger logger) {
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
        Optional<LoadingOverlay> overlay = getLoadingOverlay(logger);
        if (overlay.isEmpty()) {
            return;
        }

        Optional<ReloadInstance> reloadInstance = getReloadInstance(overlay.get(), logger);
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

            TextureDataAssembler assembler = new TextureDataAssembler();

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
