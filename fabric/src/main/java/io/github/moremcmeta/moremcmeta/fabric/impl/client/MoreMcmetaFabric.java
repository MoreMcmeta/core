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

package io.github.moremcmeta.moremcmeta.fabric.impl.client;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.TextureUtil;
import io.github.moremcmeta.moremcmeta.api.client.ClientPlugin;
import io.github.moremcmeta.moremcmeta.fabric.impl.client.adapter.SimpleReloadListenerAdapter;
import io.github.moremcmeta.moremcmeta.fabric.impl.client.event.ResourceManagerInitializedCallback;
import io.github.moremcmeta.moremcmeta.fabric.impl.client.mixin.LoadingOverlayAccessor;
import io.github.moremcmeta.moremcmeta.fabric.impl.client.mixin.PackRepositoryAccessor;
import io.github.moremcmeta.moremcmeta.fabric.impl.client.mixin.SpriteAccessor;
import io.github.moremcmeta.moremcmeta.impl.client.MoreMcmeta;
import io.github.moremcmeta.moremcmeta.impl.client.mixin.TextureManagerAccessor;
import io.github.moremcmeta.moremcmeta.impl.client.resource.StagedResourceReloadListener;
import io.github.moremcmeta.moremcmeta.impl.client.texture.EventDrivenTexture;
import io.github.moremcmeta.moremcmeta.impl.client.texture.TextureManagerWrapper;
import io.github.moremcmeta.moremcmeta.impl.client.texture.TexturePreparer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * The main mod class and entrypoint for Fabric.
 * @author soir20
 */
@SuppressWarnings("unused")
public final class MoreMcmetaFabric extends MoreMcmeta implements ClientModInitializer {
    private static final String PLUGIN_ENTRYPOINT = MODID + "-client";

    @Override
    public void onInitializeClient() {
        start();
    }

    @Override
    protected Collection<ClientPlugin> fetchTexturePlugins(Logger logger) {
        return listPlugins(ClientPlugin.class, logger);
    }

    /**
     * Gets the function that converts atlas sprites to their mipmap level.
     * @return the mipmap level getter
     */
    protected ToIntFunction<TextureAtlasSprite> mipmapLevelGetter(Logger logger) {
        return (sprite) -> ((SpriteAccessor) sprite.contents()).moremcmeta_mainImage().length - 1;
    }

    /**
     * Gets the OpenGL preparer for new textures on this loader.
     * @return the OpenGL preparer for this loader
     */
    protected TexturePreparer preparer() {
        return TextureUtil::prepareImage;
    }

    @Override
    protected BiConsumer<TextureManager, ResourceLocation> unregisterAction() {
        return (manager, location) -> {
            TextureManagerAccessor accessor = (TextureManagerAccessor) manager;
            accessor.moremcmeta_byPath().remove(location);
            manager.release(location);
        };
    }

    @Override
    protected void onResourceManagerInitialized(Consumer<Minecraft> callback) {
        ResourceManagerInitializedCallback.EVENT.register(callback::accept);
    }

    @Override
    protected void addRepositorySource(PackRepository packRepository, RepositorySource repositorySource) {
        PackRepositoryAccessor accessor = (PackRepositoryAccessor) packRepository;

        // The vanilla list of sources is immutable, so we need to create a new set.
        ImmutableSet.Builder<RepositorySource> sources = new ImmutableSet.Builder<>();
        sources.addAll(accessor.moremcmeta_sources());
        sources.add(repositorySource);

        // Restore immutability
        accessor.moremcmeta_setSources(sources.build());

    }

    @Override
    protected StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> wrapListener(
            StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> original
    ) {
        return new SimpleReloadListenerAdapter<>(original,
                new ResourceLocation("moremcmeta", "texture_reload_listener"));
    }

    @Override
    protected Optional<ReloadInstance> reloadInstance(LoadingOverlay overlay, Logger logger) {
        return Optional.of(((LoadingOverlayAccessor) overlay).moremcmeta_reloadInstance());
    }

    @Override
    protected void startTicking(TextureManagerWrapper<EventDrivenTexture> texManager) {
        ClientTickEvents.START_CLIENT_TICK.register((client) -> texManager.tick());
    }

    /**
     * Retrieves all plugins with the given class.
     * @param pluginClass       the class of the plugin
     * @param logger            logger to record warnings or errors
     * @return the list of all plugins added as an entrypoint
     * @param <T> type of plugin
     */
    @SuppressWarnings("SameParameterValue")
    private <T> List<T> listPlugins(Class<T> pluginClass, Logger logger) {
        List<T> plugins = new ArrayList<>();
        FabricLoader.getInstance()
                .getEntrypointContainers(PLUGIN_ENTRYPOINT, pluginClass)
                .forEach((entrypoint) -> {
                    try {
                        plugins.add(entrypoint.getEntrypoint());
                    } catch (Throwable err) {
                        logger.error("Mod {} provided broken plugin to {}: {}",
                                entrypoint.getProvider().getMetadata().getId(), MODID, err);
                    }
                });
        return plugins;
    }

}
