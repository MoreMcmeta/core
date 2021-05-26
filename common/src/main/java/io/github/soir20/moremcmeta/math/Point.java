package io.github.soir20.moremcmeta.math;

/**
 * A pair of coordinates in a plane.
 */
public class Point {
    private final int X_POS;
    private final int Y_POS;

    /**
     * Creates a new point.
     * @param xPos      horizontal coordinate of the point
     * @param yPos      vertical coordinate of the point
     */
    public Point(int xPos, int yPos) {
        X_POS = xPos;
        Y_POS = yPos;
    }

    /**
     * Gets the horizontal coordinate of the point.
     * @return x coordinate of the point
     */
    public int getX() {
        return X_POS;
    }

    /**
     * Gets the vertical coordinate of the point.
     * @return y coordinate of the point
     */
    public int getY() {
        return Y_POS;
    }

}
