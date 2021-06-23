package io.github.soir20.moremcmeta.client.renderer.texture;

/**
 * Mocks an {@link IRGBAImage}. Keeps track of set pixel colors.
 * @author soir20
 */
public class MockRGBAImage implements IRGBAImage {
    private final int[][] PIXELS;
    private final int WIDTH;
    private final int HEIGHT;
    private final VisibleArea VISIBLE_AREA;

    public MockRGBAImage() {
        PIXELS = new int[1][1];
        WIDTH = 1;
        HEIGHT = 1;
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
    public void upload(int uploadX, int uploadY) {}

}
