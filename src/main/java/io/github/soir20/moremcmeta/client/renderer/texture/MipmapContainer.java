package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.HashMap;
import java.util.Set;

public class MipmapContainer<I> {
    private final HashMap<Integer, I> mipmaps;

    public MipmapContainer() {
        mipmaps = new HashMap<>();
    }

    public void addMipmap(int level, I image) {
        mipmaps.put(level, image);
    }

    public I getMipmap(int level) {
        return mipmaps.get(level);
    }

    public Set<Integer> getMipmapLevels() {
        return mipmaps.keySet();
    }

}
