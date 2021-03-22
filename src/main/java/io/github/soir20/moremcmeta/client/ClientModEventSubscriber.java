package io.github.soir20.moremcmeta.client;

import com.google.common.collect.ImmutableCollection;
import io.github.soir20.moremcmeta.MoreMcmeta;
import io.github.soir20.moremcmeta.client.renderer.texture.AnimatedTexture;
import io.github.soir20.moremcmeta.client.renderer.texture.AnimatedTextureReader;
import io.github.soir20.moremcmeta.client.renderer.texture.NativeImageFrame;
import io.github.soir20.moremcmeta.client.renderer.texture.TextureManagerWrapper;
import io.github.soir20.moremcmeta.resource.TextureReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

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

        if (!(minecraft.getResourceManager() instanceof IReloadableResourceManager)) {
            logger.error("Reload listener was not added because resource manager is not reloadable");
        }

        // Resource managers
        IReloadableResourceManager rscManager = (IReloadableResourceManager) minecraft.getResourceManager();
        TextureManagerWrapper texManager = new TextureManagerWrapper(minecraft::getTextureManager);

        // Texture tickers
        BooleanSupplier areTexturesNotUpdating = () -> minecraft.world == null;
        Function<ImmutableCollection<AnimatedTexture<NativeImageFrame>>, ClientTicker> tickerFactory;
        tickerFactory = (textures) -> new ClientTicker(textures, MinecraftForge.EVENT_BUS,
                TickEvent.Phase.START, areTexturesNotUpdating);

        AnimatedTextureReader texReader = new AnimatedTextureReader(0, logger);

        rscManager.addReloadListener(new TextureReloadListener<>(texReader, texManager, tickerFactory, logger));
        logger.debug("Added texture reload listener");
    }

}
