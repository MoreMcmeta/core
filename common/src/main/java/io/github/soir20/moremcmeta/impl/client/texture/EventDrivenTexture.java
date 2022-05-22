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

package io.github.soir20.moremcmeta.impl.client.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.api.client.texture.ColorTransform;
import io.github.soir20.moremcmeta.api.client.texture.CurrentFrameView;
import io.github.soir20.moremcmeta.api.client.texture.TextureListener;
import io.github.soir20.moremcmeta.api.math.Point;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A flexible texture "shell" for mixing {@link GenericTextureComponent}s. Listeners in each
 * component provide texture implementation.
 *
 * No listeners are fired on the render thread. Wrap listener code with calls to
 * {@link com.mojang.blaze3d.systems.RenderSystem} if it must be executed on the
 * render thread.
 * @author soir20
 */
public class EventDrivenTexture extends AbstractTexture implements CustomTickable {
    private final Map<TextureListener.Type, List<TextureListener<? super TextureAndFrameView>>> LISTENERS;
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

        LISTENERS.get(type).forEach((listener) -> {
            TextureAndFrameView view = new TextureAndFrameView(CURRENT_STATE);
            listener.run(view);
            view.invalidate();
        });
    }

    /**
     * Creates an event-driven texture with listeners.
     * @param listeners                 list of all listeners, which will execute in the order given (by type)
     * @param predefinedFrames          frames already existing in the original image
     * @param generatedFrame            initial image for this texture
     */
    private EventDrivenTexture(List<? extends TextureListener<? super TextureAndFrameView>> listeners,
                               List<CloseableImageFrame> predefinedFrames, CloseableImageFrame generatedFrame) {
        super();
        LISTENERS = new EnumMap<>(TextureListener.Type.class);
        for (TextureListener<? super TextureAndFrameView> listener : listeners) {
            LISTENERS.putIfAbsent(listener.getType(), new ArrayList<>());
            LISTENERS.get(listener.getType()).add(listener);
        }

        CURRENT_STATE = new TextureState(this, predefinedFrames, generatedFrame);
    }

    /**
     * Builds an event-driven texture from components.
     * @author soir20
     */
    public static class Builder {
        private final List<GenericTextureComponent<? super TextureAndFrameView>> COMPONENTS;
        private List<CloseableImageFrame> predefinedFrames;
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
        public Builder setPredefinedFrames(List<CloseableImageFrame> frames) {
            requireNonNull(frames, "Predefined frames cannot be null");
            if (frames.size() == 0) {
                throw new IllegalArgumentException("Predefined frames cannot be empty");
            }

            if (frames.stream().mapToInt(CloseableImageFrame::getMipmapLevel).distinct().count() > 1) {
                throw new IllegalArgumentException("All predefined frames must have the same mipmap level");
            }

            if (frames.stream().mapToInt(CloseableImageFrame::getWidth).distinct().count() > 1) {
                throw new IllegalArgumentException("All predefined frames must have the same width");
            }

            if (frames.stream().mapToInt(CloseableImageFrame::getHeight).distinct().count() > 1) {
                throw new IllegalArgumentException("All predefined frames must have the same height");
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
        public Builder add(GenericTextureComponent<? super TextureAndFrameView> component) {
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

            if (predefinedFrames.get(0).getMipmapLevel() != generatedFrame.getMipmapLevel()) {
                throw new IllegalStateException("Predefined frames and generated frame must have same mipmap level");
            }

            if (predefinedFrames.get(0).getWidth() != generatedFrame.getWidth()) {
                throw new IllegalStateException("Predefined frames and generated frame must have same width");
            }

            if (predefinedFrames.get(0).getHeight() != generatedFrame.getHeight()) {
                throw new IllegalStateException("Predefined frames and generated frame must have same height");
            }

            List<TextureListener<? super TextureAndFrameView>> listeners = COMPONENTS.stream().flatMap(
                    GenericTextureComponent::getListeners
            ).collect(Collectors.toList());

            return new EventDrivenTexture(listeners, predefinedFrames, generatedFrame);
        }

    }

    /**
     * Provides a view of the current state of the texture and the current frame.
     * @author soir20
     */
    public static class TextureAndFrameView implements CurrentFrameView {
        private final TextureState STATE;
        private boolean valid;

        /**
         * Applies the provided transformation to the current frame to generate
         * a new frame, which will become the current frame.
         * @param transform     the transformation to apply to the current frame
         * @param applyArea     area to apply the transformation to
         */
        @Override
        public void generateWith(ColorTransform transform, Iterable<Point> applyArea) {
            checkValid();
            STATE.generateWith(transform, applyArea);
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
         * Gets the number of predefined frames this texture has. A texture
         * always has at least one predefined frame.
         * @return the number of predefined frames for this texture
         */
        @Override
        public int predefinedFrames() {
            checkValid();
            return STATE.predefinedFrames();
        }

        /**
         * Gets the event-driven texture.
         * @return the event-driven texture
         */
        public EventDrivenTexture getTexture() {
            checkValid();
            return STATE.getTexture();
        }

        /**
         * Uploads the current frame at the given point. This should only
         * be called when the correct texture (usually this texture) is
         * bound in OpenGL.
         * @param uploadPoint   point to upload the frame at
         */
        public void uploadAt(Point uploadPoint) {
            checkValid();
            STATE.uploadAt(uploadPoint);
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
         */
        private TextureAndFrameView(TextureState state) {
            STATE = state;
            valid = true;
        }

        /**
         * Checks that this wrapper is still valid and throws an exception if not.
         */
        private void checkValid() {
            if (!valid) {
                throw new IllegalFrameReference();
            }
        }

        /**
         * Makes this wrapper no longer usable.
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
        private final List<CloseableImageFrame> PREDEFINED_FRAMES;
        private final CloseableImageFrame GENERATED_FRAME;
        private final Queue<Pair<ColorTransform, Iterable<Point>>> TRANSFORMS;
        private Integer currentFrameIndex;
        private Integer indexToCopyToGenerated;
        private boolean hasUpdatedSinceUpload;

        /**
         * Applies the provided transformation to the current frame to generate
         * a new frame, which will become the current frame.
         * @param transform     the transformation to apply to the current frame
         * @param applyArea     area to apply the transformation to
         */
        public void generateWith(ColorTransform transform, Iterable<Point> applyArea) {
            requireNonNull(transform, "Frame transform cannot be null");

            markNeedsUpload();
            currentFrameIndex = null;
            TRANSFORMS.add(Pair.of(transform, applyArea));
        }

        /**
         * Replace the current frame with one of the predefined frames.
         * @param index     the index of the predefined frame to make
         *                  the current frame
         */
        public void replaceWith(int index) {
            if (index < 0 || index >= PREDEFINED_FRAMES.size()) {
                throw new IllegalArgumentException("Tried to replace with negative or non-existent frame index");
            }

            // If we are setting the current frame to itself, we don't need to upload again
            if (index == currentFrameIndex) {
                return;
            }

            markNeedsUpload();
            currentFrameIndex = index;
            indexToCopyToGenerated = index;
            TRANSFORMS.clear();
        }

        /**
         * Gets the width of a frame. All frames have the same width.
         * @return the width of a frame
         */
        public int width() {
            return PREDEFINED_FRAMES.get(0).getWidth();
        }

        /**
         * Gets the height of a frame. All frames have the same height.
         * @return the height of a frame
         */
        public int height() {
            return PREDEFINED_FRAMES.get(0).getHeight();
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
         * Gets the number of predefined frames this texture has. A texture
         * always has at least one predefined frame.
         * @return the number of predefined frames for this texture
         */
        public int predefinedFrames() {
            return PREDEFINED_FRAMES.size();
        }

        /**
         * Gets the event-driven texture.
         * @return the event-driven texture
         */
        public EventDrivenTexture getTexture() {
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
            if (getCurrentFrame() == GENERATED_FRAME) {
                updateGeneratedFrame();
            }
            getCurrentFrame().uploadAt(uploadPoint);
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
         * Flags the texture as needing an upload.
         */
        public void markNeedsUpload() {
            hasUpdatedSinceUpload = true;
        }

        /**
         * Creates a new texture state. Automatically flags the texture
         * for upload on the first binding.
         * @param texture               the event-driven texture
         * @param predefinedFrames      frames already existing in the image
         * @param generatedFrame        generated frame that holds images generated from the predefined frames
         */
        private TextureState(EventDrivenTexture texture, List<CloseableImageFrame> predefinedFrames,
                             CloseableImageFrame generatedFrame) {
            TEXTURE = texture;
            PREDEFINED_FRAMES = predefinedFrames;
            GENERATED_FRAME = generatedFrame;
            TRANSFORMS = new ArrayDeque<>();
            replaceWith(0);
        }

        /**
         * Gets the event-driven texture's current image.
         * @return the texture's current image
         */
        private CloseableImageFrame getCurrentFrame() {
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
                Pair<ColorTransform, Iterable<Point>> transform = TRANSFORMS.remove();
                GENERATED_FRAME.applyTransform(transform.getFirst(), transform.getSecond());
            }
        }

    }

}
