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
import io.github.soir20.moremcmeta.client.resource.DisableVanillaSpriteAnimationPack;
import io.github.soir20.moremcmeta.client.resource.TextureLoader;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.ITexturePreparer;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import io.github.soir20.moremcmeta.client.texture.SpriteFinder;
import io.github.soir20.moremcmeta.client.texture.TextureFinisher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
            rscManager.registerReloadListener(makeListener(manager, rscManager, loader, logger));
            logger.debug("Added texture reload listener");
        });

        // Enable animation by ticking the manager
        startTicking(manager);

    }

    /**
     * Adds the mod-animated sprite resource pack to the resource manager.
     * @param resourceManager       the resource manager to add the pack to
     * @return the added pack
     */
    public DisableVanillaSpriteAnimationPack addSpriteFixPack(SimpleReloadableResourceManager resourceManager) {
        DisableVanillaSpriteAnimationPack pack = new DisableVanillaSpriteAnimationPack();
        resourceManager.add(pack);
        return pack;
    }

    /**
     * Gets the current loading overlay if there is one. Returns empty if the current
     * overlay is not a loading overlay.
     * @param logger    a logger to write output
     * @return the loading overlay if there is one
     */
    public Optional<LoadingOverlay> getLoadingOverlay(Logger logger) {
        Overlay currentOverlay = Minecraft.getInstance().getOverlay();

        if (!(currentOverlay instanceof LoadingOverlay)) {
            logger.error("Loading overlay expected; textures will not be finished!");
            return Optional.empty();
        }

        return Optional.of((LoadingOverlay) currentOverlay);
    }

    /**
     * Adds a callback for any necessary post-reload work.
     * @param manager           texture manager with unfinished work
     */
    public void addCompletedReloadCallback(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> manager,
                                           Logger logger) {
        Optional<LoadingOverlay> overlay = getLoadingOverlay(logger);
        if (overlay.isEmpty()) {
            return;
        }

        Optional<ReloadInstance> reloadInstance = getReloadInstance(overlay.get(), logger);
        reloadInstance.ifPresent((instance) -> instance.done().thenRun(manager::finishQueued));
    }

    /**
     * Gets the OpenGL preparer for new textures on this loader.
     * @return the OpenGL preparer for this loader
     */
    public abstract ITexturePreparer getPreparer();

    /**
     * Gets the action that should be executed to unregister a texture on a specific mod loader.
     * @return the action that will unregister textures
     */
    public abstract BiConsumer<TextureManager, ResourceLocation> getUnregisterAction();

    /**
     * Executes a callback when the vanilla resource manager is initialized in a mod loader.
     * @param callback      the callback to execute
     */
    public abstract void onResourceManagerInitialized(Consumer<Minecraft> callback);

    /**
     * Creates a new reload listener that loads and queues animated textures for a mod loader.
     * @param texManager        manages prebuilt textures
     * @param loader            loads textures from resource packs
     * @param resourceManager   Minecraft's resource manager
     * @param logger            a logger to write output
     * @return a reload listener that loads and queues animated textures
     */
    public abstract PreparableReloadListener makeListener(
            LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager,
            SimpleReloadableResourceManager resourceManager,
            TextureLoader<EventDrivenTexture.Builder> loader,
            Logger logger
    );

    /**
     * Gets the current reload instance.
     * @param overlay    the overlay containing the instance
     * @param logger     a logger to write output
     * @return the current reload instance
     */
    public abstract Optional<ReloadInstance> getReloadInstance(LoadingOverlay overlay, Logger logger);

    /**
     * Begins ticking the {@link LazyTextureManager} on a mod loader.
     * @param texManager        the manager to begin ticking
     */
    public abstract void startTicking(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager);

}
