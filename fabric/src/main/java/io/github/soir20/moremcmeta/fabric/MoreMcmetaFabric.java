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

package io.github.soir20.moremcmeta.fabric;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.MoreMcmeta;
import io.github.soir20.moremcmeta.client.resource.StagedResourceReloadListener;
import io.github.soir20.moremcmeta.client.texture.TexturePreparer;
import io.github.soir20.moremcmeta.fabric.client.adapter.SimpleReloadListenerAdapter;
import io.github.soir20.moremcmeta.fabric.client.event.ResourceManagerInitializedCallback;
import io.github.soir20.moremcmeta.fabric.client.mixin.LoadingOverlayAccessor;
import io.github.soir20.moremcmeta.fabric.client.mixin.PackRepositoryAccessor;
import io.github.soir20.moremcmeta.fabric.client.mixin.SpriteAccessor;
import io.github.soir20.moremcmeta.fabric.client.mixin.TextureManagerAccessor;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.apache.logging.log4j.Logger;

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
public class MoreMcmetaFabric extends MoreMcmeta implements ClientModInitializer {

    /**
     * Begins the startup process on the client.
     */
    @Override
    public void onInitializeClient() {
        start();
    }

    /**
     * Gets the function that converts atlas sprites to their mipmap level.
     * @return the mipmap level getter
     */
    protected ToIntFunction<TextureAtlasSprite> getMipmapLevelGetter(Logger logger) {
        return (sprite) -> ((SpriteAccessor) sprite).getMainImage().length - 1;
    }

    /**
     * Gets the OpenGL preparer for new textures on this loader.
     * @return the OpenGL preparer for this loader
     */
    protected TexturePreparer getPreparer() {
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
    protected BiConsumer<TextureManager, ResourceLocation> getUnregisterAction() {
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
    protected void onResourceManagerInitialized(Consumer<Minecraft> callback) {
        ResourceManagerInitializedCallback.EVENT.register(callback::accept);
    }

    /**
     * Adds a repository source to Minecraft's {@link PackRepository}.
     * @param packRepository        the repository to add a source to
     * @param repositorySource      the source to add
     */
    @Override
    protected void addRepositorySource(PackRepository packRepository, RepositorySource repositorySource) {
        PackRepositoryAccessor accessor = (PackRepositoryAccessor) packRepository;

        // The vanilla list of sources is immutable, so we need to create a new set.
        ImmutableSet.Builder<RepositorySource> sources = new ImmutableSet.Builder<>();
        sources.addAll(accessor.getSources());
        sources.add(repositorySource);

        // Restore immutability
        accessor.setSources(sources.build());

    }

    /**
     * Wraps the given resource reload listener in any Fabric-specific interfaces, if necessary.
     * @param original      the original resource reload listener to wrap
     * @return the wrapped resource reload listener
     */
    @Override
    protected StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> wrapListener(
            StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> original
    ) {
        return new SimpleReloadListenerAdapter<>(original,
                new ResourceLocation("moremcmeta", "texture_reload_listener"));
    }

    /**
     * Gets the current reload instance.
     * @param overlay    the overlay containing the instance
     * @param logger     a logger to write output
     * @return the current reload instance
     */
    @Override
    protected Optional<ReloadInstance> getReloadInstance(LoadingOverlay overlay, Logger logger) {
        return Optional.of(((LoadingOverlayAccessor) overlay).getReloadInstance());
    }

    /**
     * Begins ticking the {@link LazyTextureManager} on Fabric.
     * @param texManager        the manager to begin ticking
     */
    @Override
    protected void startTicking(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager) {
        ClientTickEvents.START_CLIENT_TICK.register((client) -> texManager.tick());
    }

}
