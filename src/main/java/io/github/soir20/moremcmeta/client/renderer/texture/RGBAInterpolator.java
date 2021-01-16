package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public class RGBAInterpolator<T extends IRGBAImage> {
    private final BiFunction<Integer, Integer, T> IMAGE_FACTORY;

    public RGBAInterpolator(BiFunction<Integer, Integer, T> imageFactory) {
        IMAGE_FACTORY = imageFactory;
    }

    public Collection<T> interpolate(int steps, T start, T end) {
        List<T> output = new ArrayList<>();

        // Don't start at 0 because that would generate the start image
        for (int step = 1; step < steps; step++) {
            double ratio = (1.0 - step) / (double) steps;
            output.add(mixImage(ratio, start, end));
        }

        return output;
    }

    private T mixImage(double ratio, T start, T end) {
        int maxWidth = Math.max(start.getWidth(), end.getWidth());
        int maxHeight = Math.max(start.getHeight(), end.getHeight());
        T output = IMAGE_FACTORY.apply(maxWidth, maxHeight);

        for (int column = 0; column < maxHeight; column++) {
            for (int row = 0; row < maxWidth; row++) {
                int startColor= start.getPixel(row, column);
                int endColor = end.getPixel(row, column);
                int mixedColor = mixPixel(ratio, startColor, endColor);

                output.setPixel(row, column, mixedColor);
            }
        }

        return output;
    }

    private int mixPixel(double ratio, int startColor, int endColor) {
        return (int) (ratio * startColor + (1.0 - ratio) * endColor);
    }
}
