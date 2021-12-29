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

package io.github.soir20.moremcmeta.forge;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.MoreMcmeta;
import io.github.soir20.moremcmeta.client.texture.ITexturePreparer;
import io.github.soir20.moremcmeta.forge.client.event.ClientTicker;
import io.github.soir20.moremcmeta.client.resource.TextureLoader;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The main mod class and entrypoint for Forge.
 * @author soir20
 */
@Mod(MoreMcmetaForge.MODID)
public final class MoreMcmetaForge extends MoreMcmeta {
    public static final String MODID = "moremcmeta";

    /**
     * Registers this class as an event listener. This must be left as the default
     * constructor to be recognized by Forge as the mod entrypoint.
     */
    public MoreMcmetaForge() {

        /* Make sure the mod being absent on the other network side does not
           cause the client to display the server as incompatible. */
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                ()-> new IExtensionPoint.DisplayTest(
                        () -> FMLNetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isServer)-> true
                )
        );

        start();
    }

    /**
     * Gets the OpenGL preparer for new textures on this loader.
     * @return the OpenGL preparer for this loader
     */
    public ITexturePreparer getPreparer() {
        return (glId, mipmap, width, height) -> {
            if (!RenderSystem.isOnRenderThreadOrInit()) {
                RenderSystem.recordRenderCall(() -> TextureUtil.m_85287_(glId, mipmap, width, height));
            } else {
                TextureUtil.m_85287_(glId, mipmap, width, height);
            }
        };
    }

    /**
     * Gets the action that should be executed to unregister a texture on Forge.
     * @return the action that will unregister textures
     */
    @Override
    public BiConsumer<TextureManager, ResourceLocation> getUnregisterAction() {

        // Forge already fixes the bug that prevents texture removal
        return TextureManager::release;

    }

    /**
     * Executes a callback when the vanilla resource manager is initialized in Forge.
     * @param callback      the callback to execute
     */
    @Override
    public void onResourceManagerInitialized(Consumer<Minecraft> callback) {
        callback.accept(Minecraft.getInstance());
    }

    @Override
    public Optional<ReloadInstance> getReloadInstance(LoadingOverlay overlay, Logger logger) {
        try {
            return Optional.ofNullable(
                    ObfuscationReflectionHelper.getPrivateValue(LoadingOverlay.class, overlay, "f_96164_")
            );
        } catch (ObfuscationReflectionHelper.UnableToAccessFieldException err) {
            logger.error("Unable to access LoadingOverlay's reload instance field. " +
                    "Animated atlas sprites will be squished!");
        } catch (ObfuscationReflectionHelper.UnableToFindFieldException err) {
            logger.error("Unable to find LoadingOverlay's reload instance field. " +
                    "Animated atlas sprites will be squished!");
        }

        return Optional.empty();
    }

    /**
     * Creates a new reload listener that loads and queues animated textures for Forge.
     * @param texManager        manages prebuilt textures
     * @param resourceManager   Minecraft's resource manager
     * @param loader            loads textures from resource packs
     * @param logger            a logger to write output
     * @return a reload listener that loads and queues animated textures
     */
    @Override
    public ResourceManagerReloadListener makeListener(
            LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager,
            SimpleReloadableResourceManager resourceManager,
            TextureLoader<EventDrivenTexture.Builder> loader, Logger logger) {

        return new ResourceManagerReloadListener() {
            private final Map<ResourceLocation, EventDrivenTexture.Builder> LAST_TEXTURES_ADDED = new HashMap<>();

            @Override
            public void onResourceManagerReload(@NotNull ResourceManager manager) {
                Map<ResourceLocation, EventDrivenTexture.Builder> textures = new HashMap<>();
                textures.putAll(loader.load(manager, "textures"));
                textures.putAll(loader.load(manager, "optifine"));

                LAST_TEXTURES_ADDED.keySet().forEach(texManager::unregister);
                LAST_TEXTURES_ADDED.clear();
                LAST_TEXTURES_ADDED.putAll(textures);

                textures.forEach(texManager::register);
            }
        };

    }

    /**
     * Begins ticking the {@link LazyTextureManager} on Forge.
     * @param texManager        the manager to begin ticking
     */
    @Override
    public void startTicking(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager) {
        new ClientTicker(ImmutableList.of(texManager), MinecraftForge.EVENT_BUS, TickEvent.Phase.START, () -> true);
    }

}
