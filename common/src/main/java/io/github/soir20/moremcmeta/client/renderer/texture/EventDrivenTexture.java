package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A flexible texture "shell" for mixing {@link ITextureComponent}s. Listeners in each
 * component provide texture implementation.
 *
 * No listeners are fired on the render thread. Wrap listener code with calls to
 * {@link RenderSystem} if it must be executed on the render thread.
 * @param <I> image type
 * @author soir20
 */
public class EventDrivenTexture<I> extends AbstractTexture implements CustomTickable {
    private final Map<TextureListener.Type, List<TextureListener<I>>> LISTENERS;
    private final TextureState<I> CURRENT_STATE;

    /**
     * Binds this texture or the texture it proxies to OpenGL. Fires upload listeners
     * if the texture's image has changed.
     */
    @Override
    public void bind() {
        super.bind();
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
     * Runs all listeners of a given type. Fills listener type
     * with an empty list if no listeners exist.
     * @param type      type of listeners to fire
     */
    private void runListeners(TextureListener.Type type) {
        LISTENERS.putIfAbsent(type, new ArrayList<>());
        LISTENERS.get(type).forEach((listener -> listener.run(CURRENT_STATE)));
    }

    /**
     * Creates an event-driven texture with listeners.
     * @param listeners     list of all listeners, which will execute
     *                      in the order given (by type)
     * @param image         initial image for this texture
     */
    private EventDrivenTexture(List<TextureListener<I>> listeners, I image) {
        super();
        LISTENERS = new EnumMap<>(TextureListener.Type.class);
        for (TextureListener<I> listener : listeners) {
            LISTENERS.putIfAbsent(listener.getType(), new ArrayList<>());
            LISTENERS.get(listener.getType()).add(listener);
        }

        CURRENT_STATE = new TextureState<>(this);
        CURRENT_STATE.replaceImage(image);
    }

    /**
     * Builds an event-driven texture from components.
     * @param <I>   image type
     */
    public static class Builder<I> {
        private final List<ITextureComponent<I>> COMPONENTS;
        private I firstImage;

        /**
         * Creates a new event-driven texture builder.
         */
        public Builder() {
            COMPONENTS = new ArrayList<>();
        }

        /**
         * Sets the initial image for this texture. Required for building.
         * Unless it is altered by a component prior to binding, this image
         * will be available to upload listeners on the first binding.
         * @param image     initial image for this texture
         * @return this builder for chaining
         */
        public Builder<I> setImage(I image) {
            requireNonNull(image, "Image cannot be null");
            firstImage = image;
            return this;
        }

        /**
         * Adds a component that the texture should have.
         * @param component     component to add to the texture
         * @return this builder for chaining
         */
        public Builder<I> add(ITextureComponent<I> component) {
            requireNonNull(component, "Component cannot be null");
            COMPONENTS.add(component);
            return this;
        }

        /**
         * Builds the event-driven texture with the added components. Throws an
         * {@link IllegalStateException} if no initial image has been set.
         * @return the built event-driven texture
         */
        public EventDrivenTexture<I> build() {
            if (firstImage == null) {
                throw new IllegalStateException("Texture must have an image set");
            }

            List<TextureListener<I>> listeners = COMPONENTS.stream().flatMap(
                    ITextureComponent::getListeners
            ).collect(Collectors.toList());

            return new EventDrivenTexture<>(listeners, firstImage);
        }

    }

    /**
     * A mutable object to hold an event-driven texture's current state.
     * @param <I> image type
     */
    public static class TextureState<I> {
        private final EventDrivenTexture<I> TEXTURE;
        private I image;
        private boolean hasUpdatedSinceUpload;

        /**
         * Gets the event-driven texture.
         * @return the event-driven texture
         */
        public EventDrivenTexture<I> getTexture() {
            return TEXTURE;
        }

        /**
         * Gets the event-driven texture's current image. Automatically flags
         * the texture for uploading. (This flag will be removed if upload
         * listeners are being fired.)
         * @return the texture's current image
         */
        public I getImage() {
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
        public void replaceImage(I newImage) {
            requireNonNull(newImage, "New image cannot be null");
            markNeedsUpload();
            image = newImage;
        }

        /**
         * Creates a new texture state. Automatically flags the texture
         * for upload on the first binding.
         * @param texture     the event-driven texture
         */
        private TextureState(EventDrivenTexture<I> texture) {
            TEXTURE = texture;
        }

    }

}
