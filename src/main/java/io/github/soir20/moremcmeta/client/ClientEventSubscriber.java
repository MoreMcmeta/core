package io.github.soir20.moremcmeta.client;

import io.github.soir20.moremcmeta.MoreMcmeta;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;


@EventBusSubscriber(modid = MoreMcmeta.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ClientEventSubscriber {

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        if (event.getMap().getTextureLocation() == PlayerContainer.LOCATION_BLOCKS_TEXTURE) {
            event.addSprite(new ResourceLocation("minecraft:entity/bat"));
        }
    }

}