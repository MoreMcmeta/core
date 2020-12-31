package io.github.soir20.moremcmeta.client;

import io.github.soir20.moremcmeta.MoreMcmeta;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;


@EventBusSubscriber(modid = MoreMcmeta.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ClientEventSubscriber {

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        if (event.getMap().getTextureLocation() == MoreMcmeta.ATLAS_LOCATION) {
            ResourceLocation spriteLocation = new ResourceLocation("minecraft:entity/bat");
            event.addSprite(spriteLocation);
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            textureManager.loadTexture(new ResourceLocation("textures/entity/bat.png"), event.getMap());
        }
    }

}