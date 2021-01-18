package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.HashMap;
import java.util.Set;

public class MipmapContainer<T> {
    private final HashMap<Integer, T> mipmaps;

    public MipmapContainer() {
        mipmaps = new HashMap<>();
    }

    public void addMipmap(int level, T image) {
        mipmaps.put(level, image);
    }

    public T getMipmap(int level) {
        return mipmaps.get(level);
    }

    public Set<Integer> getMipmapLevels() {
        return mipmaps.keySet();
    }

}
