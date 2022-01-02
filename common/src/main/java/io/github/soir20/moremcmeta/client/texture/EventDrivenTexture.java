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

package io.github.soir20.moremcmeta.client.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A flexible texture "shell" for mixing {@link TextureComponent}s. Listeners in each
 * component provide texture implementation.
 *
 * No listeners are fired on the render thread. Wrap listener code with calls to
 * {@link com.mojang.blaze3d.systems.RenderSystem} if it must be executed on the
 * render thread.
 * @author soir20
 */
public class EventDrivenTexture extends AbstractTexture implements CustomTickable {
    private final Map<TextureListener.Type, List<TextureListener>> LISTENERS;
    private final TextureState CURRENT_STATE;
    private boolean registered;

    /**
     * Binds this texture or the texture it proxies to OpenGL. Fires upload listeners
     * if the texture's image has changed.
     */
    @Override
    public void bind() {

        /* We need to get the ID from the superclass, so we don't get stuck in an infinite
           loop if the texture isn't bound. */
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> GlStateManager._bindTexture(super.getId()));
        } else {
            GlStateManager._bindTexture(super.getId());
        }

        runListeners(TextureListener.Type.BIND);

        if (CURRENT_STATE.hasUpdatedSinceUpload) {
            upload();
        }
    }

    /**
     * Fires registration listeners when this texture is put into the texture manager.
     * @param resourceManager   resource manager (unused; texture resources should
     *                          already be retrieved by the time this is called)
     */
    @Override
    public void load(@Nullable ResourceManager resourceManager) {
        runListeners(TextureListener.Type.REGISTRATION);
        registered = true;
    }

    /**
     * Fires upload listeners and marks the texture as not needing an upload.
     */
    public void upload() {
        runListeners(TextureListener.Type.UPLOAD);
        CURRENT_STATE.hasUpdatedSinceUpload = false;
    }

    /**
     * Fires tick listeners when this texture is ticked.
     */
    @Override
    public void tick() {
        runListeners(TextureListener.Type.TICK);
    }

    /**
     * Fires close listeners when this texture is closed.
     */
    @Override
    public void close() {
        runListeners(TextureListener.Type.CLOSE);
    }

    /**
     * Gets the OpenGL ID of this texture. Released IDs may be reused.
     * @return the texture's OpenGL ID
     */
    @Override
    public int getId() {
        int id = super.getId();

        /* Check if this texture is currently bound. If it isn't bound, the texture is
           probably being used in RenderSystem#setShaderTexture(). That RenderSystem method
           binds the ID directly instead of this texture, preventing the title screen
           textures from animating normally. */
        if (!isCurrentlyBound() && registered) {
            bind();
        }

        return id;
    }

    /**
     * Checks if this texture is currently bound to OpenGL.
     * @return whether this texture is bound
     */
    private boolean isCurrentlyBound() {

        // The GlStateManager adds this character when it returns the active texture
        char offset = '蓀';

        return GlStateManager._getTextureId(GlStateManager._getActiveTexture() - offset) == id;
    }

    /**
     * Runs all listeners of a given type. Fills listener type
     * with an empty list if no listeners exist.
     * @param type      type of listeners to fire
     */
    private void runListeners(TextureListener.Type type) {
        LISTENERS.putIfAbsent(type, new ArrayList<>());
        LISTENERS.get(type).forEach((listener) -> listener.run(CURRENT_STATE));
    }

    /**
     * Creates an event-driven texture with listeners.
     * @param listeners     list of all listeners, which will execute
     *                      in the order given (by type)
     * @param image         initial image for this texture
     */
    private EventDrivenTexture(List<? extends TextureListener> listeners, RGBAImageFrame image) {
        super();
        LISTENERS = new EnumMap<>(TextureListener.Type.class);
        for (TextureListener listener : listeners) {
            LISTENERS.putIfAbsent(listener.getType(), new ArrayList<>());
            LISTENERS.get(listener.getType()).add(listener);
        }

        CURRENT_STATE = new TextureState(this);
        CURRENT_STATE.replaceImage(image);
    }

    /**
     * Builds an event-driven texture from components.
     */
    public static class Builder {
        private final List<TextureComponent> COMPONENTS;
        private RGBAImageFrame firstImage;

        /**
         * Creates a new event-driven texture builder.
         */
        public Builder() {
            COMPONENTS = new ArrayList<>();
        }

        /**
         * Gets the initial image set in this builder if there is one.
         * @return the initial image for the texture, or an empty
         */
        public Optional<RGBAImageFrame> getImage() {
            return Optional.ofNullable(firstImage);
        }

        /**
         * Sets the initial image for this texture. Required for building.
         * Unless it is altered by a component prior to binding, this image
         * will be available to upload listeners on the first binding.
         * @param image     initial image for this texture
         * @return this builder for chaining
         */
        public Builder setImage(RGBAImageFrame image) {
            requireNonNull(image, "Image cannot be null");
            firstImage = image;
            return this;
        }

        /**
         * Adds a component that the texture should have.
         * @param component     component to add to the texture
         * @return this builder for chaining
         */
        public Builder add(TextureComponent component) {
            requireNonNull(component, "Component cannot be null");
            COMPONENTS.add(component);
            return this;
        }

        /**
         * Builds the event-driven texture with the added components. Throws an
         * {@link IllegalStateException} if no initial image has been set.
         * @return the built event-driven texture
         */
        public EventDrivenTexture build() {
            if (firstImage == null) {
                throw new IllegalStateException("Texture must have an image set");
            }

            List<TextureListener> listeners = COMPONENTS.stream().flatMap(
                    TextureComponent::getListeners
            ).collect(Collectors.toList());

            return new EventDrivenTexture(listeners, firstImage);
        }

    }

    /**
     * A mutable object to hold an event-driven texture's current state.
     */
    public static class TextureState {
        private final EventDrivenTexture TEXTURE;
        private RGBAImageFrame image;
        private boolean hasUpdatedSinceUpload;

        /**
         * Gets the event-driven texture.
         * @return the event-driven texture
         */
        public EventDrivenTexture getTexture() {
            return TEXTURE;
        }

        /**
         * Gets the event-driven texture's current image. Automatically flags
         * the texture for uploading. (This flag will be removed if upload
         * listeners are being fired.)
         * @return the texture's current image
         */
        public RGBAImageFrame getImage() {
            markNeedsUpload();
            return image;
        }

        /**
         * Flags the texture as needing an upload.
         */
        public void markNeedsUpload() {
            hasUpdatedSinceUpload = true;
        }

        /**
         * Completely replaces the event-driven texture's current image.
         * Automatically flags the the texture for uploading. (This flag
         * will be removed if upload listeners are being fired.)
         * @param newImage      the texture's new image
         */
        public void replaceImage(RGBAImageFrame newImage) {
            requireNonNull(newImage, "New image cannot be null");
            markNeedsUpload();
            image = newImage;
        }

        /**
         * Creates a new texture state. Automatically flags the texture
         * for upload on the first binding.
         * @param texture     the event-driven texture
         */
        private TextureState(EventDrivenTexture texture) {
            TEXTURE = texture;
        }

    }

}
