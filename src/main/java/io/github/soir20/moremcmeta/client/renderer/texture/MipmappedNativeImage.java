package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.NativeImage;

import java.util.HashMap;
import java.util.Set;

public class MipmappedNativeImage {
    private final HashMap<Integer, NativeImage> mipmaps;

    public MipmappedNativeImage() {
        mipmaps = new HashMap<>();
    }

    public void addMipmap(int level, NativeImage image) {
        mipmaps.put(level, image);
    }

    public NativeImage getMipmap(int level) {
        return mipmaps.get(level);
    }

    public Set<Integer> getMipmapLevels() {
        return mipmaps.keySet();
    }

}
