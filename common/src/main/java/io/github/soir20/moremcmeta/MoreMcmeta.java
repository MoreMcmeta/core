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
import io.github.soir20.moremcmeta.client.resource.SizeSwappingResourceManager;
import io.github.soir20.moremcmeta.client.resource.TextureLoader;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import io.github.soir20.moremcmeta.client.texture.SpriteFinder;
import io.github.soir20.moremcmeta.client.texture.TextureFinisher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        TextureFinisher finisher = new TextureFinisher(spriteFinder);
        LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> manager = new LazyTextureManager<>(
                new TextureManagerAdapter(minecraft::getTextureManager, getUnregisterAction()),
                finisher
        );

        // Resource loaders
        AnimatedTextureReader reader = new AnimatedTextureReader(logger);
        TextureLoader<EventDrivenTexture.Builder> loader = new TextureLoader<>(reader, logger);

        // Listener registration and resource manager replacement
        onResourceManagerInitialized((client) -> {
            if (!(client.getResourceManager() instanceof SimpleReloadableResourceManager)) {
                logger.error("Reload listener was not added because resource manager is not reloadable");
                return;
            }

            SimpleReloadableResourceManager rscManager = (SimpleReloadableResourceManager) client.getResourceManager();

            /* Even though this is not the normal way to register reload listeners in Fabric,
               registering our listener like a vanilla listener ensures it is executed
               before the TextureManager resets its textures. This is the least invasive way to
               animate preloaded title screen textures. */
            rscManager.registerReloadListener(makeListener(manager, loader, logger));
            logger.debug("Added texture reload listener");

            replaceResourceManager(client, new SizeSwappingResourceManager(rscManager, manager::finishQueued), logger);
        });

        // Enable animation by ticking the manager
        startTicking(manager);

    }

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
     * @param texManager    manages prebuilt textures
     * @param loader        loads textures from resource packs
     * @param logger        a logger to write output
     * @return a reload listener that loads and queues animated textures
     */
    public abstract PreparableReloadListener makeListener(
            LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager,
            TextureLoader<EventDrivenTexture.Builder> loader, Logger logger
    );

    /**
     * Replaces the {@link net.minecraft.server.packs.resources.SimpleReloadableResourceManager}
     * with the mod's custom one in a mod loader.
     * @param client        the Minecraft client
     * @param manager       the manager that should be made Minecraft's resource manager
     * @param logger        a logger to write output
     */
    public abstract void replaceResourceManager(Minecraft client, SizeSwappingResourceManager manager, Logger logger);

    /**
     * Begins ticking the {@link LazyTextureManager} on a mod loader.
     * @param texManager        the manager to begin ticking
     */
    public abstract void startTicking(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager);

}
