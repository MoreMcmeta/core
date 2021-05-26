package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.math.Point;

import java.util.stream.Stream;

/**
 * A component with registration and upload listeners for textures that are
 * not part of an atlas.
 */
public class SingleTextureComponent implements ITextureComponent<NativeImageFrame> {

    /**
     * Gets the listeners for this component.
     * @return all of the listeners for this component
     */
    @Override
    public Stream<TextureListener<NativeImageFrame>> getListeners() {
        TextureListener<NativeImageFrame> registrationListener =
                new TextureListener<>(TextureListener.Type.REGISTRATION, (state) -> {
                    NativeImageFrame image = state.getImage();

                    if (!RenderSystem.isOnRenderThreadOrInit()) {
                        RenderSystem.recordRenderCall(() ->
                                TextureUtil.prepareImage(state.getTextureId(), image.getMipmapLevel(),
                                image.getWidth(), image.getHeight()));
                    } else {
                        TextureUtil.prepareImage(state.getTextureId(), image.getMipmapLevel(),
                                image.getWidth(), image.getHeight());
                    }

                });

        TextureListener<NativeImageFrame> uploadListener = new TextureListener<>(TextureListener.Type.UPLOAD,
                (state) -> state.getImage().uploadAt(new Point(0, 0)));

        return Stream.of(registrationListener, uploadListener);
    }

}
