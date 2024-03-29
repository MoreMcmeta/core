/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
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

import io.github.moremcmeta.moremcmeta.api.client.texture.ColorTransform;
import io.github.moremcmeta.moremcmeta.api.client.texture.CurrentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.IllegalFrameReferenceException;
import io.github.moremcmeta.moremcmeta.api.client.texture.NegativeUploadPointException;
import io.github.moremcmeta.moremcmeta.api.client.texture.PersistentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import io.github.moremcmeta.moremcmeta.api.math.Area;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

/**
 * <p>A flexible texture "shell" for mixing {@link CoreTextureComponent}s. Listeners in each
 * component provide texture implementation.</p>
 *
 * <p>No listeners are fired on the render thread. Wrap listener code with calls to
 * {@link com.mojang.blaze3d.systems.RenderSystem} if it must be executed on the
 * render thread.</p>
 * @author soir20
 */
public final class EventDrivenTexture extends AbstractTexture implements CustomTickable {

    /**
     * The coordinate at which textures should upload to themselves.
     */
    public static final long SELF_UPLOAD_POINT = Point.pack(0, 0);

    /**
     * The mipmap level of the texture uploading to themselves.
     */
    public static final int SELF_MIPMAP_LEVEL = 0;

    private final List<CoreTextureComponent> COMPONENTS;
    private final TextureState CURRENT_STATE;
    private int ticks;

    @Override
    public void setFilter(boolean blur, boolean clamp) {

        // Prevent blur and clamp settings in the NativeImageAdapter from being overridden by TextureStateShard
        this.bind();

    }

    @Override
    public void load(@Nullable ResourceManager resourceManager) {
        runListeners((component, view) -> component.onRegistration(view, CURRENT_STATE.predefinedFrames()));
    }

    @Override
    public void tick() {
        runListeners((component, view) -> component.onTick(view, CURRENT_STATE.predefinedFrames()));
        ticks = Math.max(0, ticks + 1);
    }

    @Override
    public void close() {
        runListeners((component, view) -> component.onClose(view, CURRENT_STATE.predefinedFrames()));
    }

    /**
     * Fires upload listeners and marks the texture as not needing an upload.
     * @param base      base texture that frames will be uploaded to
     */
    public void upload(ResourceLocation base) {
        requireNonNull(base, "Base cannot be null");

        if (ticks > 0) {
            runListeners((component, view) -> component.onTick(view, CURRENT_STATE.predefinedFrames(), ticks));
            ticks = 0;
        }

        if (!CURRENT_STATE.BASES_UPLOADED_SINCE_UPDATE.contains(base)) {
            CURRENT_STATE.BASES_UPLOADED_SINCE_UPDATE.add(base);

            runListeners((textureComponent, textureAndFrameView) -> textureComponent.onUpload(textureAndFrameView, base));
        }
    }

    /**
     * Runs all listeners (a method for every component), each with its own temporary view.
     * @param method      executes the necessary method from the component
     */
    private void runListeners(BiConsumer<CoreTextureComponent, TextureAndFrameView> method) {
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
    public static final class Builder {
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
        public Builder add(TextureComponent<? super TextureAndFrameView> component) {
            requireNonNull(component, "Component cannot be null");
            add(new CoreTextureComponent() {
                @Override
                public void onTick(TextureAndFrameView currentFrame,
                                   FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                    component.onTick(currentFrame, predefinedFrames);
                }

                @Override
                public void onTick(TextureAndFrameView currentFrame,
                                   FrameGroup<? extends PersistentFrameView> predefinedFrames, int ticks) {
                    component.onTick(currentFrame, predefinedFrames, ticks);
                }

                @Override
                public void onClose(TextureAndFrameView currentFrame,
                                    FrameGroup<? extends PersistentFrameView> predefinedFrames) {
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
    public static final class TextureAndFrameView implements CurrentFrameView, UploadableFrameView {
        private final TextureState STATE;
        private final int LAYER;
        private boolean valid;

        @Override
        public void generateWith(ColorTransform transform, Area applyArea) {
            checkValid();
            STATE.generateWith(transform, applyArea, LAYER);
        }

        @Override
        public int width() {
            checkValid();
            return STATE.width();
        }

        @Override
        public int height() {
            checkValid();
            return STATE.height();
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
         * @param x             x-coordinate of the point to upload the frame at
         * @param y             y-coordinate of the point to upload the frame at
         * @param mipmap        number of mipmaps to upload (the mipmap level of the base texture)
         * @param subAreaX      x-coordinate of the top-left corner of the sub-area to upload
         * @param subAreaY      y-coordinate of the top-left corner of the sub-area to upload
         * @param subAreaWidth  width the sub-area to upload
         * @param subAreaHeight height the sub-area to upload
         */
        public void upload(int x, int y, int mipmap, int subAreaX, int subAreaY, int subAreaWidth, int subAreaHeight) {
            checkValid();

            if (x < 0 || y < 0) {
                throw new NegativeUploadPointException(x, y);
            }

            STATE.uploadAt(x, y, mipmap, subAreaX, subAreaY, subAreaWidth, subAreaHeight);
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
        @VisibleForTesting
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
         * @throws IllegalFrameReferenceException if this view is no longer valid
         */
        private void checkValid() throws IllegalFrameReferenceException {
            if (!valid) {
                throw new IllegalFrameReferenceException();
            }
        }

        /**
         * Makes this frame view invalid for further use. After this method is called, all future
         * calls to other methods will throw an {@link IllegalFrameReferenceException} exception. However,
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
        private final Set<ResourceLocation> BASES_UPLOADED_SINCE_UPDATE;
        private Integer currentFrameIndex;

        /**
         * Applies the provided transformation to the current frame to generate
         * a new frame, which will become the current frame.
         * @param transform     the transformation to apply to the current frame
         * @param applyArea     area to apply the transformation to
         * @param layer         layer to apply the transform to
         */
        public void generateWith(ColorTransform transform, Area applyArea, int layer) {
            requireNonNull(transform, "Frame transform cannot be null");
            requireNonNull(applyArea, "Apply area cannot be null");

            markNeedsUpload();
            currentFrameIndex = null;

            // We may wish to delay updates later if the transforms list is optimized, but update immediately for now
            GENERATED_FRAME.applyTransform(transform, applyArea, layer);

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
         * @param x             x-coordinate of the point to upload the frame at
         * @param y             y-coordinate of the point to upload the frame at
         * @param mipmap        number of mipmaps to upload (the mipmap level of the base texture)
         * @param subAreaX      x-coordinate of the top-left corner of the sub-area to upload
         * @param subAreaY      y-coordinate of the top-left corner of the sub-area to upload
         * @param subAreaWidth  width the sub-area to upload
         * @param subAreaHeight height the sub-area to upload
         */
        public void uploadAt(int x, int y, int mipmap, int subAreaX, int subAreaY, int subAreaWidth, int subAreaHeight) {
            currentFrame().uploadAt(x, y, mipmap, subAreaX, subAreaY, subAreaWidth, subAreaHeight);
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
            PREDEFINED_FRAME_GROUP = new FrameGroupImpl<>(predefinedFrames, (frame, index) -> new PredefinedFrameView(frame));
            GENERATED_FRAME = generatedFrame;
            BASES_UPLOADED_SINCE_UPDATE = new HashSet<>();
            replaceWith(0);
        }

        /**
         * Replace the current frame with one of the predefined frames.
         * @param index     the index of the predefined frame to make
         *                  the current frame
         */
        private void replaceWith(@SuppressWarnings("SameParameterValue") int index) {

            // If we are setting the current frame to itself, we don't need to upload again
            if (currentFrameIndex != null && index == currentFrameIndex) {
                return;
            }

            markNeedsUpload();
            currentFrameIndex = index;
        }

        /**
         * Flags the texture as needing an upload.
         */
        private void markNeedsUpload() {
            BASES_UPLOADED_SINCE_UPDATE.clear();
        }

        /**
         * Gets the event-driven texture's current image.
         * @return the texture's current image
         */
        private CloseableImageFrame currentFrame() {
            return currentFrameIndex == null ? GENERATED_FRAME : PREDEFINED_FRAMES.get(currentFrameIndex);
        }

    }

}
