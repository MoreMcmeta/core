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

package io.github.soir20.moremcmeta.fabric;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.MoreMcmeta;
import io.github.soir20.moremcmeta.client.resource.DisableVanillaSpriteAnimationPack;
import io.github.soir20.moremcmeta.client.texture.ITexturePreparer;
import io.github.soir20.moremcmeta.fabric.client.event.ResourceManagerInitializedCallback;
import io.github.soir20.moremcmeta.fabric.client.mixin.LoadingOverlayAccessor;
import io.github.soir20.moremcmeta.fabric.client.mixin.TextureManagerAccessor;
import io.github.soir20.moremcmeta.client.resource.TextureLoader;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The main mod class and entrypoint for Fabric.
 * @author soir20
 */
@SuppressWarnings("unused")
public class MoreMcmetaFabric extends MoreMcmeta implements ClientModInitializer {

    /**
     * Begins the startup process on the client.
     */
    @Override
    public void onInitializeClient() {
        start();
    }

    /**
     * Gets the OpenGL preparer for new textures on this loader.
     * @return the OpenGL preparer for this loader
     */
    public ITexturePreparer getPreparer() {
        return (glId, mipmap, width, height) -> {
            if (!RenderSystem.isOnRenderThreadOrInit()) {
                RenderSystem.recordRenderCall(() -> TextureUtil.prepareImage(glId, mipmap, width, height));
            } else {
                TextureUtil.prepareImage(glId, mipmap, width, height);
            }
        };
    }

    /**
     * Gets the action that should be executed to unregister a texture on Fabric.
     * @return the action that will unregister textures
     */
    @Override
    public BiConsumer<TextureManager, ResourceLocation> getUnregisterAction() {
        return (manager, location) -> {
            TextureManagerAccessor accessor = (TextureManagerAccessor) manager;
            accessor.getByPath().remove(location);
            manager.release(location);
        };
    }

    /**
     * Executes a callback when the vanilla resource manager is initialized in Fabric.
     * @param callback      the callback to execute
     */
    @Override
    public void onResourceManagerInitialized(Consumer<Minecraft> callback) {
        ResourceManagerInitializedCallback.EVENT.register(callback::accept);
    }

    /**
     * Creates a new reload listener that loads and queues animated textures for Fabric.
     * @param texManager        manages prebuilt textures
     * @param resourceManager   Minecraft's resource manager
     * @param loader            loads textures from resource packs
     * @param logger            a logger to write output
     * @return a reload listener that loads and queues animated textures
     */
    @Override
    public PreparableReloadListener makeListener(
            LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager,
            SimpleReloadableResourceManager resourceManager,
            TextureLoader<EventDrivenTexture.Builder> loader,
            Logger logger) {

        return new SimpleResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>>() {
            private final Map<ResourceLocation, EventDrivenTexture.Builder> LAST_TEXTURES_ADDED = new HashMap<>();

            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation("moremcmeta", "texture_reload_listener");
            }

            @Override
            public CompletableFuture<Map<ResourceLocation, EventDrivenTexture.Builder>> load(ResourceManager manager,
                                                                                             ProfilerFiller profiler,
                                                                                             Executor executor) {
                DisableVanillaSpriteAnimationPack spriteFixPack = addSpriteFixPack(resourceManager);
                return CompletableFuture.supplyAsync(() -> {
                    Map<ResourceLocation, EventDrivenTexture.Builder> textures = new HashMap<>();
                    textures.putAll(loader.load(manager, "textures"));
                    textures.putAll(loader.load(manager, "optifine"));
                    spriteFixPack.setTextures(textures);
                    return textures;
                }, executor);
            }

            @Override
            public CompletableFuture<Void> apply(Map<ResourceLocation, EventDrivenTexture.Builder> data,
                                                 ResourceManager manager, ProfilerFiller profiler, Executor executor) {
                addCompletedReloadCallback(texManager, logger);

                return CompletableFuture.runAsync(() -> {
                    LAST_TEXTURES_ADDED.keySet().forEach(texManager::unregister);
                    LAST_TEXTURES_ADDED.clear();
                    LAST_TEXTURES_ADDED.putAll(data);

                    data.forEach(texManager::register);
                }, executor);
            }
        };
    }

    @Override
    public Optional<ReloadInstance> getReloadInstance(LoadingOverlay loadingOverlay, Logger logger) {
        return Optional.of(((LoadingOverlayAccessor) loadingOverlay).getReloadInstance());
    }

    /**
     * Begins ticking the {@link LazyTextureManager} on Fabric.
     * @param texManager        the manager to begin ticking
     */
    @Override
    public void startTicking(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager) {
        ClientTickEvents.START_CLIENT_TICK.register((client) -> texManager.tick());
    }

}
