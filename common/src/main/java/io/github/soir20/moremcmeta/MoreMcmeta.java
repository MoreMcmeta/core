/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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
import io.github.soir20.moremcmeta.client.adapter.TextureManagerAdapter;
import io.github.soir20.moremcmeta.client.io.AnimatedTextureReader;
import io.github.soir20.moremcmeta.client.resource.SpriteFrameSizeFixPack;
import io.github.soir20.moremcmeta.client.resource.StagedResourceReloadListener;
import io.github.soir20.moremcmeta.client.resource.TextureLoader;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.TexturePreparer;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import io.github.soir20.moremcmeta.client.texture.SpriteFinder;
import io.github.soir20.moremcmeta.client.texture.TextureFinisher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
        SpriteFinder spriteFinder = new SpriteFinder(AtlasAdapter::new);
        TextureFinisher finisher = new TextureFinisher(spriteFinder, getPreparer());
        LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> manager = new LazyTextureManager<>(
                new TextureManagerAdapter(minecraft::getTextureManager, getUnregisterAction()),
                finisher
        );

        // Resource loaders
        AnimatedTextureReader reader = new AnimatedTextureReader(logger);
        TextureLoader<EventDrivenTexture.Builder> loader = new TextureLoader<>(reader, logger);

        // Listener registration and resource manager replacement
        onResourceManagerInitialized((client) -> {
            if (!(client.getResourceManager() instanceof SimpleReloadableResourceManager rscManager)) {
                logger.error("Reload listener was not added because resource manager is not reloadable");
                return;
            }

            /* Even though this is not the normal way to register reload listeners in Fabric,
               registering our listener like a vanilla listener ensures it is executed
               before the TextureManager resets its textures. This is the least invasive way to
               animate preloaded title screen textures. */
            rscManager.registerReloadListener(wrapListener(makeListener(manager, rscManager, loader, logger)));
            logger.debug("Added texture reload listener");
        });

        // Enable animation by ticking the manager
        startTicking(manager);

    }

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
     * Creates a new reload listener that loads and queues animated textures for a mod loader.
     * @param texManager        manages prebuilt textures
     * @param loader            loads textures from resource packs
     * @param resourceManager   Minecraft's resource manager
     * @param logger            a logger to write output
     * @return reload listener that loads and queues animated textures
     */
    private StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> makeListener(
            LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager,
            SimpleReloadableResourceManager resourceManager,
            TextureLoader<EventDrivenTexture.Builder> loader,
            Logger logger
    ) {
        return new TextureResourceReloadListener(texManager, resourceManager, loader, logger);
    }

    /**
     * Adds the mod-animated sprite resource pack to the resource manager.
     * @param resourceManager       the resource manager to add the pack to
     * @return the added pack
     */
    private SpriteFrameSizeFixPack addSpriteFixPack(SimpleReloadableResourceManager resourceManager) {
        SpriteFrameSizeFixPack pack = new SpriteFrameSizeFixPack();
        resourceManager.add(pack);
        return pack;
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
        private final LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> TEX_MANAGER;
        private final SimpleReloadableResourceManager RESOURCE_MANAGER;
        private final TextureLoader<EventDrivenTexture.Builder> LOADER;
        private final Logger LOGGER;

        /**
         * Creates a new resource reload listener.
         * @param texManager            texture manager that accepts queued textures
         * @param resourceManager       Minecraft's resource manager (as a {@link SimpleReloadableResourceManager})
         * @param loader                texture loader
         * @param logger                a logger to write output
         */
        public TextureResourceReloadListener(
                LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager,
                SimpleReloadableResourceManager resourceManager,
                TextureLoader<EventDrivenTexture.Builder> loader,
                Logger logger
        ) {
            TEX_MANAGER = requireNonNull(texManager, "Texture manager cannot be null");
            RESOURCE_MANAGER = requireNonNull(resourceManager, "Resource manager cannot be null");
            LOADER = requireNonNull(loader, "Texture loader cannot be null");
            LOGGER = requireNonNull(logger, "Logger cannot be null");
        }

        /**
         * Loads textures and adds the resource pack to fix animated sprites.
         * @param manager       Minecraft's resource manager
         * @param profiler      load stage profiler
         * @param executor      asynchronously executes load stage tasks
         * @return task returning loaded texture builders by location
         */
        @Override
        public CompletableFuture<Map<ResourceLocation, EventDrivenTexture.Builder>> load(ResourceManager manager,
                                                                                         ProfilerFiller profiler,
                                                                                         Executor executor) {
            SpriteFrameSizeFixPack spriteFixPack = addSpriteFixPack(RESOURCE_MANAGER);
            return CompletableFuture.supplyAsync(() -> {
                Map<ResourceLocation, EventDrivenTexture.Builder> textures = new HashMap<>();
                textures.putAll(LOADER.load(manager, "textures"));
                textures.putAll(LOADER.load(manager, "optifine"));
                spriteFixPack.setTextures(textures);
                return textures;
            }, executor);
        }

        /**
         * Clears old textures, if any, and registers new ones.
         * @param data          texture builders by location that were just loaded
         * @param manager       Minecraft's resource manager
         * @param profiler      apply stage profiler
         * @param executor      asynchronously executes apply stage tasks
         * @return task with no return data
         */
        @Override
        public CompletableFuture<Void> apply(Map<ResourceLocation, EventDrivenTexture.Builder> data,
                                             ResourceManager manager, ProfilerFiller profiler, Executor executor) {
            addCompletedReloadCallback(TEX_MANAGER, LOGGER);

            return CompletableFuture.runAsync(() -> {
                LAST_TEXTURES_ADDED.keySet().forEach(TEX_MANAGER::unregister);
                LAST_TEXTURES_ADDED.clear();
                LAST_TEXTURES_ADDED.putAll(data);

                data.forEach(TEX_MANAGER::register);
            }, executor);
        }

    }

}
