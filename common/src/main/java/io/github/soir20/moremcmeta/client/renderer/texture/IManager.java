package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;

/**
 * A container for resources that can be added and removed. It represents what textures Minecraft is aware of.
 * @author soir20
 */
public interface IManager<R> extends Tickable {

    /**
     * Prepares a texture and makes Minecraft aware of it.
     * @param location      file location of resource identical to how it is used in a entity/gui/map
     * @param resource      the actual resource that should be used
     */
    void register(ResourceLocation location, R resource);

    /**
     * Unregisters a resource so Minecraft is no longer aware of it.
     * This also allows the resource to be replaced.
     * @param location   file location of resource to delete
     */
    void unregister(ResourceLocation location);

    /**
     * Updates all animated resources that were loaded through this manager.
     */
    void tick();

}
