package io.github.soir20.moremcmeta.client.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.math.Point;

import java.util.stream.Stream;

/**
 * Manages uploading a texture that is not associated with an atlas sprite.
 * @author soir20
 */
public class SingleUploadComponent implements ITextureComponent {

    /**
     * Gets the listeners for this component.
     * @return all of the listeners for this component
     */
    @Override
    public Stream<TextureListener> getListeners() {
        TextureListener registrationListener = new TextureListener(TextureListener.Type.REGISTRATION,
                (state) -> {
                    RGBAImageFrame image = state.getImage();

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
        TextureListener uploadListener = new TextureListener(
                TextureListener.Type.UPLOAD,
                (state) -> state.getImage().uploadAt(uploadPoint)
        );

        return Stream.of(registrationListener, uploadListener);
    }

}
