package io.github.soir20.moremcmeta.client.event;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.client.io.AnimatedTextureReader;
import io.github.soir20.moremcmeta.client.renderer.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.renderer.texture.NativeImageFrame;
import io.github.soir20.moremcmeta.client.renderer.texture.TextureManagerWrapper;
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

        if (!(minecraft.getResourceManager() instanceof SimpleReloadableResourceManager)) {
            logger.error("Reload listener was not added because resource manager is not reloadable");
            return;
        }

        // Resource managers
        SimpleReloadableResourceManager rscManager =
                (SimpleReloadableResourceManager) minecraft.getResourceManager();
        TextureManagerWrapper texManager = new TextureManagerWrapper(minecraft::getTextureManager);

        // Texture ticker
        BooleanSupplier areTexturesNotUpdating = () -> true;
        new ClientTicker(ImmutableList.of(texManager), MinecraftForge.EVENT_BUS,
                TickEvent.Phase.START, areTexturesNotUpdating);

        AnimatedTextureReader texReader = new AnimatedTextureReader(logger);

        TextureReloadListener<EventDrivenTexture.Builder<NativeImageFrame>> commonListener =
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
