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

package io.github.soir20.moremcmeta.client.event;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.client.adapter.AtlasAdapter;
import io.github.soir20.moremcmeta.client.io.AnimatedTextureReader;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.SpriteFinder;
import io.github.soir20.moremcmeta.client.texture.TextureFinisher;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import io.github.soir20.moremcmeta.client.resource.SizeSwappingResourceManager;
import io.github.soir20.moremcmeta.client.resource.TextureReloadListener;
import io.github.soir20.moremcmeta.MoreMcmeta;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles client-relevant events on the mod event bus.
 * @author soir20
 */
@Mod.EventBusSubscriber(modid = MoreMcmeta.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@SuppressWarnings("unused")
public class ClientModEventSubscriber {

    /**
     * Adds the texture reload listener before resources are loaded for the first time.
     * @param event     the mod construction event
     */
    @SubscribeEvent
    public static void onPreInit(final FMLConstructModEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        Logger logger = LogManager.getLogger();

        if (!(minecraft.getResourceManager() instanceof SimpleReloadableResourceManager)) {
            logger.error("Reload listener was not added because resource manager is not reloadable");
            return;
        }

        // Resource managers
        SimpleReloadableResourceManager rscManager = (SimpleReloadableResourceManager) minecraft.getResourceManager();

        SpriteFinder spriteFinder = new SpriteFinder(AtlasAdapter::new);
        TextureFinisher finisher = new TextureFinisher(spriteFinder);
        LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager =
                new LazyTextureManager<>(minecraft::getTextureManager, finisher);

        // Texture ticker
        new ClientTicker(ImmutableList.of(texManager), MinecraftForge.EVENT_BUS, TickEvent.Phase.START, () -> true);

        // Resource loaders
        AnimatedTextureReader texReader = new AnimatedTextureReader(logger);
        TextureReloadListener<EventDrivenTexture.Builder> commonListener =
                new TextureReloadListener<>(texReader, texManager, logger);

        // Use Forge's selective variant of the reload listener
        rscManager.registerReloadListener((ISelectiveResourceReloadListener) (manager, predicate) -> {
            if (predicate.test(VanillaResourceType.TEXTURES)) {
                commonListener.onResourceManagerReload(manager);
            }
        });

        logger.debug("Added texture reload listener");

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
        event.enqueueWork(() -> {
            try {
                ObfuscationReflectionHelper.setPrivateValue(
                        Minecraft.class, minecraft,
                        new SizeSwappingResourceManager(rscManager, texManager::finishQueued),
                        "field_110451_am"
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

}
