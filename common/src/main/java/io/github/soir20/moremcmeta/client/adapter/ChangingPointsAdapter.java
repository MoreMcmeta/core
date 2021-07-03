package io.github.soir20.moremcmeta.client.adapter;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.soir20.moremcmeta.client.io.ChangingPointsReader;
import io.github.soir20.moremcmeta.client.texture.IRGBAImage;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Finds the pixels that change during an animation for an {@link NativeImage}.
 * @author soir20
 */
public class ChangingPointsAdapter {
    private final ChangingPointsReader READER;

    /**
     * Creates a new changing points reader that works with {@link NativeImage}s.
     */
    public ChangingPointsAdapter() {
        READER = new ChangingPointsReader();
    }

    /**
     * Gets the pixels that will change for every mipmap.
     * @param image         the original image to analyze
     * @param frameWidth    the width of a frame
     * @param frameHeight   the height of a frame
     * @param mipmap        number of mipmap levels to use
     * @return  pixels that change for every mipmap (starting with the default image)
     */
    public List<IRGBAImage.VisibleArea> read(NativeImage image, int frameWidth, int frameHeight, int mipmap) {
        requireNonNull(image, "Image cannot be null");
        IRGBAImage wrappedImage = new NativeImageAdapter(image, mipmap);
        return READER.read(wrappedImage, frameWidth, frameHeight, mipmap);
    }

}
