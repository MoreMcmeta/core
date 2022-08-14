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

package io.github.soir20.moremcmeta.forge.impl.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.api.client.MoreMcmetaMetadataReaderPlugin;
import io.github.soir20.moremcmeta.api.client.MoreMcmetaTexturePlugin;
import io.github.soir20.moremcmeta.forge.api.client.MoreMcmetaClientPluginRegisterEvent;
import io.github.soir20.moremcmeta.forge.impl.client.event.ClientTicker;
import io.github.soir20.moremcmeta.impl.client.MoreMcmeta;
import io.github.soir20.moremcmeta.impl.client.resource.StagedResourceReloadListener;
import io.github.soir20.moremcmeta.impl.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.impl.client.texture.LazyTextureManager;
import io.github.soir20.moremcmeta.impl.client.texture.TexturePreparer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import static io.github.soir20.moremcmeta.impl.client.MoreMcmeta.MODID;

/**
 * The main mod class and entrypoint for Forge.
 * @author soir20
 */
@Mod(MODID)
public final class MoreMcmetaForge extends MoreMcmeta {

    /**
     * Serves as mod entrypoint on Forge and tells the server to ignore this mod.
     */
    public MoreMcmetaForge() {

        /* Make sure the mod being absent on the other network side does not
           cause the client to display the server as incompatible. */
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                ()-> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isServer)-> true
                )
        );

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> this::start);
    }

    /**
     * Gets all loaded MoreMcmeta plugins from other mods.
     * @param logger    logger to report errors
     * @return all loaded plugins
     */
    @Override
    protected Pair<Collection<MoreMcmetaTexturePlugin>, Collection<MoreMcmetaMetadataReaderPlugin>>
            fetchTexturePlugins(Logger logger) {

        List<MoreMcmetaTexturePlugin> texturePlugins = new ArrayList<>();
        List<MoreMcmetaMetadataReaderPlugin> readerPlugins = new ArrayList<>();
        FMLJavaModLoadingContext.get().getModEventBus().post(
                new MoreMcmetaClientPluginRegisterEvent(texturePlugins, readerPlugins)
        );
        return Pair.of(texturePlugins, readerPlugins);
    }

    /**
     * Gets the function that converts atlas sprites to their mipmap level.
     * @return the mipmap level getter
     */
    protected ToIntFunction<TextureAtlasSprite> mipmapLevelGetter(Logger logger) {
        return (sprite) -> {
            try {
                NativeImage[] mipmaps = ObfuscationReflectionHelper.getPrivateValue(TextureAtlasSprite.class,
                        sprite, "f_118342_");

                if (mipmaps != null) {
                    return mipmaps.length - 1;
                }

                logger.error("Unable to retrieve mipmaps for TextureAtlasSprite " + sprite.getName()
                        + ". Defaulting to mipmap level 0 for this sprite.");
            } catch (ObfuscationReflectionHelper.UnableToAccessFieldException err) {
                logger.error("Unable to access TextureAtlasSprite's mipmap field. " +
                        "Defaulting to mipmap level 0 for this sprite.");
            } catch (ObfuscationReflectionHelper.UnableToFindFieldException err) {
                logger.error("Unable to find TextureAtlasSprite's mipmap field. " +
                        "Defaulting to mipmap level 0 for this sprite.");
            }

            return 0;
        };
    }

    /**
     * Gets the OpenGL preparer for new textures on this loader.
     * @return the OpenGL preparer for this loader
     */
    protected TexturePreparer preparer() {
        return TextureUtil::prepareImage;
    }

    /**
     * Gets the action that should be executed to unregister a texture on Forge.
     * @return the action that will unregister textures
     */
    @Override
    protected BiConsumer<TextureManager, ResourceLocation> unregisterAction() {

        // Forge already fixes the bug that prevents texture removal
        return TextureManager::release;

    }

    /**
     * Executes a callback when the vanilla resource manager is initialized in Forge.
     * @param callback      the callback to execute
     */
    @Override
    protected void onResourceManagerInitialized(Consumer<Minecraft> callback) {
        callback.accept(Minecraft.getInstance());
    }

    /**
     * Adds a repository source to Minecraft's {@link PackRepository}.
     * @param packRepository        the repository to add a source to
     * @param repositorySource      the source to add
     */
    @Override
    protected void addRepositorySource(PackRepository packRepository, RepositorySource repositorySource) {
        packRepository.addPackFinder(repositorySource);
    }

    /**
     * Wraps the given resource reload listener in any Forge-specific interfaces, if necessary.
     * @param original      the original resource reload listener to wrap
     * @return the wrapped resource reload listener
     */
    @Override
    protected StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> wrapListener(
            StagedResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>> original
    ) {
        return original;
    }

    /**
     * Gets the current reload instance.
     * @param overlay    the overlay containing the instance
     * @param logger     a logger to write output
     * @return the current reload instance
     */
    @Override
    protected Optional<ReloadInstance> reloadInstance(LoadingOverlay overlay, Logger logger) {
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
     * Begins ticking the {@link LazyTextureManager} on Forge.
     * @param texManager        the manager to begin ticking
     */
    @Override
    protected void startTicking(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager) {
        new ClientTicker(ImmutableList.of(texManager), MinecraftForge.EVENT_BUS, TickEvent.Phase.START, () -> true);
    }

}
