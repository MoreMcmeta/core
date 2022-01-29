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

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.forge.client.event.ClientTicker;
import io.github.soir20.moremcmeta.client.resource.SizeSwappingResourceManager;
import io.github.soir20.moremcmeta.client.resource.TextureLoader;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The main mod class and entrypoint for Forge.
 * @author soir20
 */
@Mod(MoreMcmetaForge.MODID)
public final class MoreMcmetaForge extends MoreMcmeta {
    public static final String MODID = "moremcmeta";
    private FMLConstructModEvent lastEvent;

    /**
     * Registers this class as an event listener. This must be left as the default
     * constructor to be recognized by Forge as the mod entrypoint.
     */
    public MoreMcmetaForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FMLJavaModLoadingContext.get().getModEventBus().register(this);
        }

        /* Make sure the mod being absent on the other network side does not
           cause the client to display the server as incompatible. */
        ModLoadingContext.get().registerExtensionPoint(
                ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(
                        () -> FMLNetworkConstants.IGNORESERVERONLY,
                        (a, b) -> true
                )
        );

    }

    /**
     * The actual/effective entrypoint for the mod. Begins the startup process on the client
     * when the mod is constructed.
     * @param event     the mod construction event
     */
    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onPreInit(final FMLConstructModEvent event) {
        lastEvent = event;
        start();
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

    /**
     * Creates a new reload listener that loads and queues animated textures for Forge.
     * @param texManager    manages prebuilt textures
     * @param loader        loads textures from resource packs
     * @param logger        a logger to write output
     * @return a reload listener that loads and queues animated textures
     */
    @Override
    public PreparableReloadListener makeListener(
            LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager,
            TextureLoader<EventDrivenTexture.Builder> loader, Logger logger) {

        return new ISelectiveResourceReloadListener() {
            private final Map<ResourceLocation, EventDrivenTexture.Builder> LAST_TEXTURES_ADDED = new HashMap<>();

            @Override
            public void onResourceManagerReload(@NotNull ResourceManager manager,
                                                @NotNull Predicate<IResourceType> predicate) {

                if (predicate.test(VanillaResourceType.TEXTURES)) {
                    Map<ResourceLocation, EventDrivenTexture.Builder> textures = new HashMap<>();
                    textures.putAll(loader.load(manager, "textures"));
                    textures.putAll(loader.load(manager, "optifine"));

                    LAST_TEXTURES_ADDED.keySet().forEach(texManager::unregister);
                    LAST_TEXTURES_ADDED.clear();
                    LAST_TEXTURES_ADDED.putAll(textures);

                    textures.forEach(texManager::register);
                }
            }
        };
    }

    /**
     * Replaces the {@link net.minecraft.server.packs.resources.SimpleReloadableResourceManager}
     * with the mod's custom one in Forge.
     * @param client        the Minecraft client
     * @param manager       the manager that should be made Minecraft's resource manager
     * @param logger        a logger to write output
     */
    @Override
    public void replaceResourceManager(Minecraft client, SizeSwappingResourceManager manager, Logger logger) {

        /* enqueueWork() will run after all mods have added their listeners but before Minecraft
           adds any of its own listeners. This allows us to wrap the resource manager and intercept
           how metadata is retrieved during atlas stitching. It prevents animated sprites
           from having all of their frames stitched onto the atlas when no .mcmeta file is present.
           Checks inside the wrapper prevent any difference in how textures are loaded for
           non-MoreMcmeta textures.

           We have to use reflection to replace the resource manager here because the only other ways
           to intercept metadata retrieval would be:
           1. add a custom resource pack (requires reflection and breaks when users change pack order)
           2. replace AnimationMetadataSection.EMPTY (requires reflection and affects non-MoreMcmeta textures)
           3. Mixin/ASM/bytecode manipulation (obviously more prone to compatibility issues)

           The resource manager wrapper mostly calls the original, so it should be the solution
           most compatible with other mods. */
        lastEvent.enqueueWork(() -> {
            try {
                ObfuscationReflectionHelper.setPrivateValue(
                        Minecraft.class, client, manager, "field_110451_am"
                );
            } catch (ObfuscationReflectionHelper.UnableToAccessFieldException err) {
                logger.error("Unable to access Minecraft's resource manager field. " +
                        "Animated atlas sprites will be squished!");
            } catch (ObfuscationReflectionHelper.UnableToFindFieldException err) {
                logger.error("Unable to find Minecraft's resource manager field. " +
                        "Animated atlas sprites will be squished!");
            }
        });

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
