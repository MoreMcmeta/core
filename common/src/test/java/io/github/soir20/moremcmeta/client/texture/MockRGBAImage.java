package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.math.Point;

/**
 * Mocks an {@link IRGBAImage}. Keeps track of set pixel colors.
 * @author soir20
 */
public class MockRGBAImage implements IRGBAImage {
    public static final int DEFAULT_DIMENSION = 100;

    private final int[][] PIXELS;
    private final int WIDTH;
    private final int HEIGHT;
    private final VisibleArea VISIBLE_AREA;
    private Point uploadPoint;

    public MockRGBAImage() {
        PIXELS = new int[DEFAULT_DIMENSION][DEFAULT_DIMENSION];
        WIDTH = DEFAULT_DIMENSION;
        HEIGHT = DEFAULT_DIMENSION;
        VISIBLE_AREA = (new VisibleArea.Builder()).build();
    }

    public MockRGBAImage(int width, int height) {
        PIXELS = new int[width][height];
        WIDTH = width;
        HEIGHT = height;
        VISIBLE_AREA = (new VisibleArea.Builder()).build();
    }

    public MockRGBAImage(int[][] pixels, VisibleArea visibleArea) {
        PIXELS = pixels;
        WIDTH = pixels.length;
        HEIGHT = pixels[0].length;
        VISIBLE_AREA = visibleArea;
    }

    @Override
    public int getPixel(int x, int y) {
        return PIXELS[x][y];
    }

    @Override
    public void setPixel(int x, int y, int color) {
        PIXELS[x][y] = color;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public VisibleArea getVisibleArea() {
        return VISIBLE_AREA;
    }

    @Override
    public void upload(int uploadX, int uploadY) {
        uploadPoint = new Point(uploadX, uploadY);
    }

    public Point getLastUploadPoint() {
        return uploadPoint;
    }

}
