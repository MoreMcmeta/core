package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.NativeImage;

import java.io.IOException;
import java.io.InputStream;

public interface INativeImageFactory {

    NativeImage createNativeImage(InputStream inputStreamIn) throws IOException;

}
