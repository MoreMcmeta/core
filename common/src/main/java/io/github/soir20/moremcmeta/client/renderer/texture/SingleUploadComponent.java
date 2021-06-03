package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.math.Point;

import java.util.stream.Stream;

/**
 * Manages uploading a texture that is not associated with an atlas sprite.
 * @author soir20
 */
public class SingleUploadComponent implements ITextureComponent<NativeImageFrame> {

    /**
     * Gets the listeners for this component.
     * @return all of the listeners for this component
     */
    @Override
    public Stream<TextureListener<NativeImageFrame>> getListeners() {
        TextureListener<NativeImageFrame> bindListener = new TextureListener<>(TextureListener.Type.BIND,
                (state) -> {
                    NativeImageFrame image = state.getImage();

                    if (!RenderSystem.isOnRenderThreadOrInit()) {
                        RenderSystem.recordRenderCall(() ->
                                TextureUtil.prepareImage(state.getTexture().getId(), image.getMipmapLevel(),
                                        image.getWidth(), image.getHeight()));
                    } else {
                        TextureUtil.prepareImage(state.getTexture().getId(), image.getMipmapLevel(),
                                image.getWidth(), image.getHeight());
                    }
                });

        Point uploadPoint = new Point(0, 0);
        TextureListener<NativeImageFrame> uploadListener = new TextureListener<>(TextureListener.Type.UPLOAD,
                (state) -> {

                    if (!RenderSystem.isOnRenderThreadOrInit()) {
                        RenderSystem.recordRenderCall(() -> state.getImage().uploadAt(uploadPoint));
                    } else {
                        state.getImage().uploadAt(uploadPoint);
                    }
                });

        return Stream.of(bindListener, uploadListener);
    }

}