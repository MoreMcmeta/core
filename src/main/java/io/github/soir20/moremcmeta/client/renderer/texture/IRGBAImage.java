package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An image with an RGB color scheme.
 * Color format: AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB in binary, stored as an integer (32 bits total)
 * @author soir20
 */
public interface IRGBAImage {

    /**
     * Gets the color of a pixel in this image.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return  the color of the given pixel
     */
    int getPixel(int x, int y);

    /**
     * Sets the color of a pixel in this image.
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @param color     new color of the pixel
     */
    void setPixel(int x, int y, int color);

    /**
     * Gets the width of this image.
     * @return  the width of this image
     */
    int getWidth();

    /**
     * Gets the height of this image.
     * @return  the height of this image
     */
    int getHeight();

    VisibleArea getVisibleArea();

    class VisibleArea implements Iterable<Pair<Integer, Integer>> {
        private final Set<VisibleRow> VISIBLE_ROWS;

        @Override
        @Nonnull
        public Iterator<Pair<Integer, Integer>> iterator() {
            return new VisiblePointIterator(VISIBLE_ROWS);
        }

        public static class Builder {
            private final Map<Integer, Set<Integer>> ROWS;

            public Builder() {
                ROWS = new HashMap<>();
            }

            public void addPixel(int x, int y) {
                if (!ROWS.containsKey(y)) {
                    ROWS.put(y, new HashSet<>());
                }

                ROWS.get(y).add(x);
            }

            public VisibleArea build() {
                Set<VisibleRow> visibleRows = new HashSet<>();

                for (Map.Entry<Integer, Set<Integer>> entry : ROWS.entrySet()) {
                    List<Integer> xPoints = entry.getValue().stream().sorted().collect(Collectors.toList());
                    int numPoints = xPoints.size();

                    int startIndex = 0;
                    for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
                        int nextIndex = pointIndex + 1;

                        boolean isLastPoint = pointIndex == numPoints - 1;
                        boolean isRowEnd = isLastPoint || xPoints.get(nextIndex) - xPoints.get(pointIndex) > 1;

                        if (isRowEnd) {
                            int width = pointIndex - startIndex + 1;
                            visibleRows.add(new VisibleRow(xPoints.get(startIndex), entry.getKey(), width));

                            startIndex = nextIndex;
                        }
                    }
                }

                return new VisibleArea(visibleRows);
            }

        }

        private VisibleArea(Set<VisibleRow> rows) {
            VISIBLE_ROWS = rows;
        }

        private static class VisibleRow {
            private final int X;
            private final int Y;
            private final int WIDTH;

            public VisibleRow(int x, int y, int width) {
                X = x;
                Y = y;
                WIDTH = width;
            }

            @Override
            public int hashCode() {
                return Objects.hash(X, Y, WIDTH);
            }

            @Override
            public boolean equals(Object other) {
                if (!(other instanceof VisibleRow)) {
                    return false;
                }

                VisibleRow otherRow = (VisibleRow) other;
                return X == otherRow.X && Y == otherRow.Y && WIDTH == ((VisibleRow) other).WIDTH;
            }

        }

        private static class VisiblePointIterator implements Iterator<Pair<Integer, Integer>> {
            private final Iterator<VisibleRow> rowIterator;
            private VisibleRow currentRow;
            private int pixelCount;

            public VisiblePointIterator(Set<VisibleRow> rows) {
                rowIterator = rows.iterator();
            }

            @Override
            public boolean hasNext() {
                return rowIterator.hasNext() || (currentRow != null && pixelCount < currentRow.WIDTH);
            }

            @Override
            public Pair<Integer, Integer> next() {
                if (currentRow == null || pixelCount == currentRow.WIDTH) {
                    currentRow = rowIterator.next();
                    pixelCount = 0;
                }

                pixelCount++;
                return new Pair<>(currentRow.X + pixelCount - 1, currentRow.Y);
            }
        }

    }

}
