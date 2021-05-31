package io.github.soir20.moremcmeta.client.renderer.texture;

/**
 * A custom copy of Minecraft's {@link net.minecraft.client.renderer.texture.Tickable} interface
 * to prevent textures from being ticked by Minecraft's texture manager. Minecraft's texture
 * manager does not remove a texture from the tickable list when that texture is removed,
 * causing OpenGL errors. (Forge patches this bug, but we need to work around it to be
 * multi-platform.)
 */
public interface CustomTickable {

    /**
     * Updates this item on tick.
     */
    void tick();

}
