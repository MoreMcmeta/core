package io.github.soir20.moremcmeta.client.event;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.client.io.AnimatedTextureReader;
import io.github.soir20.moremcmeta.client.renderer.texture.TextureManagerWrapper;
import io.github.soir20.moremcmeta.client.resource.TextureReloadListener;
import io.github.soir20.moremcmeta.MoreMcmeta;
import io.github.soir20.moremcmeta.client.resource.SelectiveReloadListenerWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BooleanSupplier;

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

        if (!(minecraft.getResourceManager() instanceof ReloadableResourceManager)) {
            logger.error("Reload listener was not added because resource manager is not reloadable");
            return;
        }

        // Resource managers
        ReloadableResourceManager rscManager = (ReloadableResourceManager) minecraft.getResourceManager();
        TextureManagerWrapper texManager = new TextureManagerWrapper(minecraft::getTextureManager);

        // Texture ticker
        BooleanSupplier areTexturesNotUpdating = () -> minecraft.level == null;
        new ClientTicker(ImmutableList.of(texManager), MinecraftForge.EVENT_BUS,
                TickEvent.Phase.START, areTexturesNotUpdating);

        AnimatedTextureReader texReader = new AnimatedTextureReader(0, logger);

        TextureReloadListener commonListener = new TextureReloadListener(texReader, texManager, logger);
        SelectiveReloadListenerWrapper wrapper = new SelectiveReloadListenerWrapper(
                VanillaResourceType.TEXTURES,
                commonListener
        );
        rscManager.registerReloadListener(wrapper);
        logger.debug("Added texture reload listener");
    }

}
