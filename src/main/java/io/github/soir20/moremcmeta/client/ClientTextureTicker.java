package io.github.soir20.moremcmeta.client;

import com.google.common.collect.ImmutableCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Updates textures at the start of each client tick when the player is not in a world.
 * @param <T>   the type of texture to update
 */
public class ClientTextureTicker<T extends Texture & ITickable> implements IClientTicker {
    private final ImmutableCollection<T> TEXTURES;
    private boolean isTicking;

    /**
     * Creates and starts a ticker that will update the given textures.
     * @param textures  the textures to update on each tick
     */
    public ClientTextureTicker(ImmutableCollection<T> textures) {
        TEXTURES = textures;
        isTicking = false;

        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Updates all the textures associated with this ticker.
     * @param event     tick event on the client side
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        /* The texture manager will update textures when the world exists, so we only need
           to update on the main menu. */
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().world == null) {
            TEXTURES.forEach(ITickable::tick);
        }

    }

    /**
     * Stops ticking the current textures. Does nothing if ticking has already stopped.
     */
    @Override
    public void stopTicking() {
        if (isTicking) {
            MinecraftForge.EVENT_BUS.unregister(this);
            isTicking = false;
        }
    }

}
