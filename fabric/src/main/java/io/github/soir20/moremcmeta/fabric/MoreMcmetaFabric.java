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
import io.github.soir20.moremcmeta.client.resource.StagedResourceReloadListener;
import io.github.soir20.moremcmeta.client.texture.ITexturePreparer;
import io.github.soir20.moremcmeta.fabric.client.adapter.SimpleReloadListenerAdapter;
import io.github.soir20.moremcmeta.fabric.client.event.ResourceManagerInitializedCallback;
import io.github.soir20.moremcmeta.fabric.client.mixin.LoadingOverlayAccessor;
import io.github.soir20.moremcmeta.fabric.client.mixin.TextureManagerAccessor;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
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

    @Override
    public StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> wrapListener(
            StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> original
    ) {
        return new SimpleReloadListenerAdapter<>(original,
                new ResourceLocation("moremcmeta", "texture_reload_listener"));
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
