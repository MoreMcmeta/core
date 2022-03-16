/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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

package io.github.soir20.moremcmeta.impl.client.adapter;

import io.github.soir20.moremcmeta.impl.client.texture.Manager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Wraps a {@link TextureManager} so it is compatible with the {@link Manager}
 * interface and because it is not immediately available during mod construction.
 * This class also retrieves the texture manager and throws errors
 * if it is used too early (and the manager is null).
 * @author soir20
 */
public class TextureManagerAdapter implements Manager<AbstractTexture> {
    private final Supplier<? extends TextureManager> MANAGER_GETTER;
    private final BiConsumer<TextureManager, ResourceLocation> UNREGISTER_ACTION;

    /**
     * Creates a new wrapper for the texture manager.
     * @param managerGetter         retrieves the texture manager, which should not be null
     * @param unregisterAction      unregisters a texture from the manager on a specific mod loader
     */
    public TextureManagerAdapter(Supplier<? extends TextureManager> managerGetter,
                                 BiConsumer<TextureManager, ResourceLocation> unregisterAction) {
        MANAGER_GETTER = requireNonNull(managerGetter, "Manager getter cannot be null");
        UNREGISTER_ACTION = requireNonNull(unregisterAction, "Unregister action cannot be null");
    }

    /**
     * Registers a texture to the texture manager
     * @param location      file location of resource identical to how it is used in a entity/gui/map
     * @param texture       the texture to register
     */
    @Override
    public void register(ResourceLocation location, AbstractTexture texture) {
        requireNonNull(location, "Location cannot be null");
        requireNonNull(texture, "Texture cannot be null");
        getManager().register(location, texture);
    }

    /**
     * Removes a texture from the manager using the provided unregister action.
     * @param location   file location of resource to delete
     */
    @Override
    public void unregister(ResourceLocation location) {
        requireNonNull(location, "Location cannot be null");
        UNREGISTER_ACTION.accept(getManager(), location);
    }

    /**
     * Updates all textures in the texture manager.
     */
    @Override
    public void tick() {
        getManager().tick();
    }

    /**
     * Retrieves the texture manager from the getter. Throws a null pointer exception
     * if the texture manager is being used too early and does not exist yet.
     * @return Minecraft's texture manager
     */
    private TextureManager getManager() {
        return requireNonNull(MANAGER_GETTER.get(), "Supplied manager cannot be null");
    }

}
