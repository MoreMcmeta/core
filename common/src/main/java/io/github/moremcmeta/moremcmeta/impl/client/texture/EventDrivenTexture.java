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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moremcmeta.moremcmeta.api.client.texture.Color;
import io.github.moremcmeta.moremcmeta.api.client.texture.ColorTransform;
import io.github.moremcmeta.moremcmeta.api.client.texture.CurrentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.PersistentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import io.github.moremcmeta.moremcmeta.api.client.texture.UploadableFrameView;
import io.github.moremcmeta.moremcmeta.api.math.Area;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

/**
 * A flexible texture "shell" for mixing {@link CoreTextureComponent}s. Listeners in each
 * component provide texture implementation.
 *
 * No listeners are fired on the render thread. Wrap listener code with calls to
 * {@link com.mojang.blaze3d.systems.RenderSystem} if it must be executed on the
 * render thread.
 * @author soir20
 */
public class EventDrivenTexture extends AbstractTexture implements CustomTickable {
    private final List<CoreTextureComponent> COMPONENTS;
    private final TextureState CURRENT_STATE;
    private boolean registered;
    private boolean uploading;

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

        if (!uploading) {
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

        // Set the first frame as current by default, unless overwritten by the registration listeners
        CURRENT_STATE.replaceWith(0);

        runListeners((component, view) -> component.onRegistration(view, CURRENT_STATE.predefinedFrames()));
        registered = true;
        bind();
    }

    /**
     * Fires upload listeners and marks the texture as not needing an upload.
     */
    public void upload() {
        uploading = true;

        if (CURRENT_STATE.hasUpdatedSinceUpload) {
            CURRENT_STATE.hasUpdatedSinceUpload = false;

            runListeners(((textureComponent, textureAndFrameView) -> {
                bind();
                textureComponent.onUpload(textureAndFrameView);
            }));
        }

        uploading = false;
    }

    /**
     * Fires tick listeners when this texture is ticked.
     */
    @Override
    public void tick() {
        runListeners((component, view) -> component.onTick(view, CURRENT_STATE.predefinedFrames()));
    }

    /**
     * Fires close listeners when this texture is closed.
     */
    @Override
    public void close() {
        runListeners((component, view) -> component.onClose(view, CURRENT_STATE.predefinedFrames()));
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
     * Runs all listeners (a method for every component), each with its own temporary view.
     * @param method      executes the necessary method from the component
     */
    private void runListeners(
            BiConsumer<CoreTextureComponent, TextureAndFrameView> method) {
        for (int layer = 0; layer < COMPONENTS.size(); layer++) {
            TextureAndFrameView view = new TextureAndFrameView(CURRENT_STATE, layer);
            method.accept(COMPONENTS.get(layer), view);
            view.invalidate();
        }
    }

    /**
     * Creates an event-driven texture with listeners.
     * @param components                components that listen to texture events
     * @param predefinedFrames          frames already existing in the original image
     * @param generatedFrame            initial image for this texture
     */
    private EventDrivenTexture(
            List<CoreTextureComponent> components,
            List<? extends CloseableImageFrame> predefinedFrames,
            CloseableImageFrame generatedFrame) {
        super();
        COMPONENTS = components;
        CURRENT_STATE = new TextureState(this, predefinedFrames, generatedFrame);
    }

    /**
     * Builds an event-driven texture from components.
     * @author soir20
     */
    public static class Builder {
        private final List<CoreTextureComponent> COMPONENTS;
        private List<? extends CloseableImageFrame> predefinedFrames;
        private CloseableImageFrame generatedFrame;

        /**
         * Creates a new event-driven texture builder.
         */
        public Builder() {
            COMPONENTS = new ArrayList<>();
        }

        /**
         * Sets the predefined frames already existing in the source image for this texture.
         * @param frames        list of predefined frames. Must not be empty. Must all have the
         *                      same mipmap level and size. The first frame will be used as the
         *                      initial  frame for the texture. While the pixels in the frames
         *                      will not be modified, the frame's mipmap level may be altered.
         * @return this builder for chaining
         */
        public Builder setPredefinedFrames(List<? extends CloseableImageFrame> frames) {
            requireNonNull(frames, "Predefined frames cannot be null");
            if (frames.size() == 0) {
                throw new IllegalArgumentException("Predefined frames cannot be empty");
            }

            if (frames.stream().mapToInt(CloseableImageFrame::mipmapLevel).distinct().count() > 1) {
                throw new IllegalArgumentException("All predefined frames must have the same mipmap level");
            }

            if (frames.stream().mapToInt(CloseableImageFrame::width).distinct().count() > 1) {
                throw new IllegalArgumentException("All predefined frames must have the same width");
            }

            if (frames.stream().mapToInt(CloseableImageFrame::height).distinct().count() > 1) {
                throw new IllegalArgumentException("All predefined frames must have the same height");
            }

            if (frames.stream().mapToInt(CloseableImageFrame::layers).distinct().count() > 1) {
                throw new IllegalArgumentException("All predefined frames must have the same number of layers");
            }

            predefinedFrames = frames;
            return this;
        }

        /**
         * Sets the frame to use as the generated frame.
         * @param frame         frame to store generated images. Pixels will be overwritten
         *                      and the frame's mipmap level may be lowered.
         * @return this builder for chaining
         */
        public Builder setGeneratedFrame(CloseableImageFrame frame) {
            requireNonNull(frame, "Generated frame cannot be null");
            generatedFrame = frame;
            return this;
        }

        /**
         * Adds a component that the texture should have.
         * @param component     component to add to the texture
         * @return this builder for chaining
         */
        public Builder add(TextureComponent<? super TextureAndFrameView, ? super TextureAndFrameView> component) {
            requireNonNull(component, "Component cannot be null");
            add(new CoreTextureComponent() {
                @Override
                public void onTick(TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                    component.onTick(currentFrame, predefinedFrames);
                }

                @Override
                public void onUpload(TextureAndFrameView currentFrame) {
                    component.onUpload(currentFrame);
                }

                @Override
                public void onClose(TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                    component.onClose(currentFrame, predefinedFrames);
                }
            });
            return this;
        }

        /**
         * Adds a component that the texture should have.
         * @param component     component to add to the texture
         * @return this builder for chaining
         */
        public Builder add(CoreTextureComponent component) {
            requireNonNull(component, "Component cannot be null");
            COMPONENTS.add(component);
            return this;
        }

        /**
         * Builds the event-driven texture with the added components. Throws an
         * {@link IllegalStateException} if the predefined frames or generated
         * frame is not present ot the predefined frames and the generated frame
         * do not have the same mipmap level and size when this method is called.
         * @return the built event-driven texture
         */
        public EventDrivenTexture build() {
            if (predefinedFrames == null) {
                throw new IllegalStateException("Texture must have predefined frames");
            }

            if (generatedFrame == null) {
                throw new IllegalStateException("Texture must have a generated frame");
            }

            if (predefinedFrames.get(0).mipmapLevel() != generatedFrame.mipmapLevel()) {
                throw new IllegalStateException("Predefined frames and generated frame must have same mipmap level");
            }

            if (predefinedFrames.get(0).width() != generatedFrame.width()) {
                throw new IllegalStateException("Predefined frames and generated frame must have same width");
            }

            if (predefinedFrames.get(0).height() != generatedFrame.height()) {
                throw new IllegalStateException("Predefined frames and generated frame must have same height");
            }

            int components = COMPONENTS.size();

            int predefinedLayers = predefinedFrames.get(0).layers();
            if (components > 0 && predefinedLayers != components) {
                throw new IllegalStateException(String.format(
                        "Predefined frames have %s layers but there are %s components; must be the same",
                        predefinedLayers,
                        components
                ));
            }

            int generatedLayers = generatedFrame.layers();
            if (components > 0 && generatedLayers != components) {
                throw new IllegalStateException(String.format(
                        "Generated frame %s layers but there are %s components; must be the same",
                        generatedLayers,
                        components
                ));
            }

            return new EventDrivenTexture(COMPONENTS, predefinedFrames, generatedFrame);
        }

    }

    /**
     * Provides a view of the current state of the texture and the current frame.
     * @author soir20
     */
    public static class TextureAndFrameView implements CurrentFrameView, UploadableFrameView {
        private final TextureState STATE;
        private final int LAYER;
        private boolean valid;

        /**
         * Applies the provided transformation to the current frame to generate
         * a new frame, which will become the current frame.
         * @param transform     the transformation to apply to the current frame
         * @param applyArea     area to apply the transformation to
         * @param dependencies  the points whose current colors this transformation depends on
         */
        @Override
        public void generateWith(ColorTransform transform, Area applyArea, Area dependencies) {
            checkValid();
            STATE.generateWith(transform, applyArea, dependencies, LAYER);
        }

        /**
         * Replace the current frame with one of the predefined frames.
         * @param index     the index of the predefined frame to make
         *                  the current frame
         */
        @Override
        public void replaceWith(int index) {
            checkValid();
            STATE.replaceWith(index);
        }

        /**
         * Gets the width of a frame. All frames have the same width.
         * @return the width of a frame
         */
        @Override
        public int width() {
            checkValid();
            return STATE.width();
        }

        /**
         * Gets the height of a frame. All frames have the same height.
         * @return the height of a frame
         */
        @Override
        public int height() {
            checkValid();
            return STATE.height();
        }

        /**
         * Gets the index of the current frame if it is a predefined frame.
         * Otherwise, the frame is a generated frame, so empty is returned.
         * @return the index of the predefined frame or empty if generated
         */
        @Override
        public Optional<Integer> index() {
            checkValid();
            return STATE.index();
        }

        /**
         * Gets the event-driven texture.
         * @return the event-driven texture
         */
        public EventDrivenTexture texture() {
            checkValid();
            return STATE.texture();
        }

        /**
         * Uploads the current frame at the given point. This should only
         * be called when the correct texture (usually this texture) is
         * bound in OpenGL.
         * @param x   x-coordinate of the point to upload the frame at
         * @param y   y-coordinate of the point to upload the frame at
         */
        public void upload(int x, int y) {
            checkValid();

            if (x < 0 || y < 0) {
                throw new NegativeUploadPointException(x, y);
            }

            STATE.uploadAt(new Point(x, y));
        }

        /**
         * Lowers the mipmap level of all predefined and generated frames
         * for this texture. The new mipmap level must be less than or
         * equal to the current mipmap level of the frames.
         * @param newMipmapLevel        new mipmap level of the frames
         */
        public void lowerMipmapLevel(int newMipmapLevel) {
            checkValid();
            STATE.lowerMipmapLevel(newMipmapLevel);
        }

        /**
         * Flags the texture as needing an upload.
         */
        public void markNeedsUpload() {
            checkValid();
            STATE.markNeedsUpload();
        }

        /**
         * Creates an ephemeral wrapper for a texture state.
         * @param state     texture state to wrap
         * @param layer     layer that will be modified when transforms are applied
         */
        private TextureAndFrameView(TextureState state, int layer) {
            STATE = state;
            LAYER = layer;
            valid = true;
        }

        /**
         * Checks that this frame view is currently valid and throws an exception if not.
         * @throws IllegalFrameReference if this view is no longer valid
         */
        private void checkValid() throws IllegalFrameReference {
            if (!valid) {
                throw new IllegalFrameReference();
            }
        }

        /**
         * Makes this frame view invalid for further use. After this method is called, all future
         * calls to other methods will throw an {@link IllegalFrameReference} exception. However,
         * this method is idempotent.
         */
        private void invalidate() {
            valid = false;
        }

    }

    /**
     * A mutable object to hold an event-driven texture's current state.
     * @author soir20
     */
    private static class TextureState {
        private final EventDrivenTexture TEXTURE;
        private final List<? extends CloseableImageFrame> PREDEFINED_FRAMES;
        private final FrameGroup<PersistentFrameView> PREDEFINED_FRAME_GROUP;
        private final CloseableImageFrame GENERATED_FRAME;
        private final Queue<QueuedTransform> TRANSFORMS;
        private Integer currentFrameIndex;
        private Integer indexToCopyToGenerated;
        private boolean hasUpdatedSinceUpload;

        /**
         * Applies the provided transformation to the current frame to generate
         * a new frame, which will become the current frame.
         * @param transform     the transformation to apply to the current frame
         * @param applyArea     area to apply the transformation to
         * @param dependencies  the points whose current colors this transformation depends on
         * @param layer         layer to apply the transform to
         */
        public void generateWith(ColorTransform transform, Area applyArea, Area dependencies, int layer) {
            requireNonNull(transform, "Frame transform cannot be null");
            requireNonNull(applyArea, "Apply area cannot be null");
            requireNonNull(dependencies, "Dependencies cannot be null");

            markNeedsUpload();
            currentFrameIndex = null;
            TRANSFORMS.add(new QueuedTransform(transform, applyArea, dependencies, layer));
        }

        /**
         * Replace the current frame with one of the predefined frames.
         * @param index     the index of the predefined frame to make
         *                  the current frame
         */
        public void replaceWith(int index) {
            if (index < 0 || index >= PREDEFINED_FRAMES.size()) {
                throw new FrameView.FrameIndexOutOfBoundsException(index);
            }

            // If we are setting the current frame to itself, we don't need to upload again
            if (currentFrameIndex != null && index == currentFrameIndex) {
                return;
            }

            markNeedsUpload();
            currentFrameIndex = index;
            indexToCopyToGenerated = index;
            TRANSFORMS.clear();
        }

        /**
         * Gets the color of the given pixel in the current frame.
         * @param x     x-coordinate of the pixel (from the top left)
         * @param y     y-coordinate of the pixel (from the top left)
         * @return the color of the pixel at the given coordinate
         */
        public Color color(int x, int y) {
            return new Color(currentFrame().color(x, y));
        }

        /**
         * Gets the width of a frame. All frames have the same width.
         * @return the width of a frame
         */
        public int width() {
            return PREDEFINED_FRAMES.get(0).width();
        }

        /**
         * Gets the height of a frame. All frames have the same height.
         * @return the height of a frame
         */
        public int height() {
            return PREDEFINED_FRAMES.get(0).height();
        }

        /**
         * Gets the index of the current frame if it is a predefined frame.
         * Otherwise, the frame is a generated frame, so empty is returned.
         * @return the index of the predefined frame or empty if generated
         */
        public Optional<Integer> index() {
            return Optional.ofNullable(currentFrameIndex);
        }

        /**
         * Gets the event-driven texture.
         * @return the event-driven texture
         */
        public EventDrivenTexture texture() {
            return TEXTURE;
        }

        /**
         * Uploads the current frame at the given point. This should only
         * be called when the correct texture (usually this texture) is
         * bound in OpenGL.
         * @param uploadPoint   point to upload the frame at
         */
        public void uploadAt(Point uploadPoint) {
            requireNonNull(uploadPoint, "Upload point cannot be null");
            if (currentFrame() == GENERATED_FRAME) {
                updateGeneratedFrame();
            }
            currentFrame().uploadAt(uploadPoint);
        }

        /**
         * Lowers the mipmap level of all predefined and generated frames
         * for this texture. The new mipmap level must be less than or
         * equal to the current mipmap level of the frames.
         * @param newMipmapLevel        new mipmap level of the frames
         */
        public void lowerMipmapLevel(int newMipmapLevel) {
            for (CloseableImageFrame predefined_frame : PREDEFINED_FRAMES) {
                predefined_frame.lowerMipmapLevel(newMipmapLevel);
            }

            GENERATED_FRAME.lowerMipmapLevel(newMipmapLevel);
        }

        /**
         * Gets the group of persistent views of the predefined frames.
         * @return the group of predefined frames
         */
        public FrameGroup<PersistentFrameView> predefinedFrames() {
            return PREDEFINED_FRAME_GROUP;
        }

        /**
         * Creates a new texture state. Automatically flags the texture
         * for upload on the first binding.
         * @param texture               the event-driven texture
         * @param predefinedFrames      frames already existing in the image
         * @param generatedFrame        generated frame that holds images generated from the predefined frames
         */
        private TextureState(EventDrivenTexture texture, List<? extends CloseableImageFrame> predefinedFrames,
                             CloseableImageFrame generatedFrame) {
            TEXTURE = texture;
            PREDEFINED_FRAMES = predefinedFrames;
            PREDEFINED_FRAME_GROUP = new FrameGroupImpl<>(predefinedFrames, PredefinedFrameView::new);
            GENERATED_FRAME = generatedFrame;
            TRANSFORMS = new ArrayDeque<>();
            replaceWith(0);
        }

        /**
         * Flags the texture as needing an upload.
         */
        private void markNeedsUpload() {
            hasUpdatedSinceUpload = true;
        }

        /**
         * Gets the event-driven texture's current image.
         * @return the texture's current image
         */
        private CloseableImageFrame currentFrame() {
            return currentFrameIndex == null ? GENERATED_FRAME : PREDEFINED_FRAMES.get(currentFrameIndex);
        }

        /**
         * Applies all transformations to the generated frame.
         */
        private void updateGeneratedFrame() {
            if (indexToCopyToGenerated != null) {
                CloseableImageFrame copyFrame = PREDEFINED_FRAMES.get(indexToCopyToGenerated);
                GENERATED_FRAME.copyFrom(copyFrame);

                indexToCopyToGenerated = null;
            }

            while (!TRANSFORMS.isEmpty()) {
                QueuedTransform transform = TRANSFORMS.remove();
                GENERATED_FRAME.applyTransform(
                        transform.TRANSFORM,
                        transform.APPLY_AREA,
                        transform.DEPENDENCIES,
                        transform.LAYER
                );
            }
        }

    }

    /**
     * Wrapper class for queued transforms.
     * @author soir20
     */
    private static class QueuedTransform {
        private final ColorTransform TRANSFORM;
        private final Area APPLY_AREA;
        private final Area DEPENDENCIES;
        private final int LAYER;

        /**
         * Creates a new wrapper for a queued transform.
         * @param transform         the transform
         * @param applyArea         apply area of the transform
         * @param dependencies      dependencies of the transform
         * @param layer             layer to apply the transform to
         */
        public QueuedTransform(ColorTransform transform, Area applyArea, Area dependencies, int layer) {
            TRANSFORM = transform;
            APPLY_AREA = applyArea;
            DEPENDENCIES = dependencies;
            LAYER = layer;
        }

    }

}
