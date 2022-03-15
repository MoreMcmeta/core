/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.soir20.moremcmeta.client.animation;

import io.github.soir20.moremcmeta.client.texture.CloseableImage;
import io.github.soir20.moremcmeta.client.texture.MockCloseableImage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link CloseableImageInterpolator}. Use an online random number generator for
 * RGBA component values in new tests.
 * @author soir20
 */
public class CloseableImageInterpolatorTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_ImageGetterNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new CloseableImageInterpolator(null);
    }

    @Test
    public void interpolate_StartFrameNull_NullPointerException() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea.Builder builder = new CloseableImage.VisibleArea.Builder();
        builder.addPixel(1, 2);
        builder.addPixel(4, 0);
        builder.addPixel(2, 3);
        builder.addPixel(0, 4);
        CloseableImage.VisibleArea area = builder.build();

        int[][] endPixels = new int[width][height];
        endPixels[1][2] = toBinary(25, 181, 119, 37);
        endPixels[4][0] = toBinary(106, 126, 174, 11);
        endPixels[2][3] = toBinary(0, 238, 24, 122);
        endPixels[0][4] = toBinary(93, 209, 60, 223);
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) ->
                new MockCloseableImage(new int[w][h], area));

        expectedException.expect(NullPointerException.class);
        interpolator.interpolate(10, 5, null, end);
    }

    @Test
    public void interpolate_EndFrameNull_NullPointerException() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea.Builder builder = new CloseableImage.VisibleArea.Builder();
        builder.addPixel(1, 2);
        builder.addPixel(4, 0);
        builder.addPixel(2, 3);
        builder.addPixel(0, 4);
        CloseableImage.VisibleArea area = builder.build();

        int[][] startPixels = new int[width][height];
        startPixels[1][2] = toBinary(184, 143, 65, 197);
        startPixels[4][0] = toBinary(41, 248, 80, 100);
        startPixels[2][3] = toBinary(19, 159, 70, 226);
        startPixels[0][4] = toBinary(216, 101, 41, 195);
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) ->
                new MockCloseableImage(new int[w][h], area));

        expectedException.expect(NullPointerException.class);
        interpolator.interpolate(10, 5, start, null);
    }

    @Test
    public void interpolate_InterpolatedFrameNull_NullPointerException() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea.Builder builder = new CloseableImage.VisibleArea.Builder();
        builder.addPixel(1, 2);
        builder.addPixel(4, 0);
        builder.addPixel(2, 3);
        builder.addPixel(0, 4);
        CloseableImage.VisibleArea area = builder.build();

        int[][] startPixels = new int[width][height];
        startPixels[1][2] = toBinary(184, 143, 65, 197);
        startPixels[4][0] = toBinary(41, 248, 80, 100);
        startPixels[2][3] = toBinary(19, 159, 70, 226);
        startPixels[0][4] = toBinary(216, 101, 41, 195);
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[width][height];
        endPixels[1][2] = toBinary(25, 181, 119, 37);
        endPixels[4][0] = toBinary(106, 126, 174, 11);
        endPixels[2][3] = toBinary(0, 238, 24, 122);
        endPixels[0][4] = toBinary(93, 209, 60, 223);
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) -> null);

        expectedException.expect(NullPointerException.class);
        interpolator.interpolate(10, 5, start, end);
    }

    @Test
    public void interpolate_StepLessThanOne_IllegalArgException() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea area = (new CloseableImage.VisibleArea.Builder()).build();

        int[][] startPixels = new int[width][height];
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[width][height];
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        int[][] interpolatedPixels = new int[width][height];
        MockCloseableImage interpolated = new MockCloseableImage(interpolatedPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) -> interpolated);

        expectedException.expect(IllegalArgumentException.class);
        interpolator.interpolate(10, 0, start, end);
    }

    @Test
    public void interpolate_StepEqualsSteps_IllegalArgException() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea area = (new CloseableImage.VisibleArea.Builder()).build();

        int[][] startPixels = new int[width][height];
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[width][height];
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        int[][] interpolatedPixels = new int[width][height];
        MockCloseableImage interpolated = new MockCloseableImage(interpolatedPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) -> interpolated);

        expectedException.expect(IllegalArgumentException.class);
        interpolator.interpolate(10, 10, start, end);
    }

    @Test
    public void interpolate_StepGreaterThanSteps_IllegalArgException() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea area = (new CloseableImage.VisibleArea.Builder()).build();

        int[][] startPixels = new int[width][height];
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[width][height];
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        int[][] interpolatedPixels = new int[width][height];
        MockCloseableImage interpolated = new MockCloseableImage(interpolatedPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) -> interpolated);

        expectedException.expect(IllegalArgumentException.class);
        interpolator.interpolate(10, 11, start, end);
    }

    @Test
    public void interpolate_SameStartAndEnd_IdenticalOutput() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea.Builder builder = new CloseableImage.VisibleArea.Builder();
        builder.addPixel(1, 2);
        builder.addPixel(4, 0);
        builder.addPixel(2, 3);
        builder.addPixel(0, 4);
        CloseableImage.VisibleArea area = builder.build();

        int firstColor = toBinary(25, 50, 250, 255);
        int secondColor = toBinary(25, 250, 50, 150);
        int thirdColor = toBinary(250, 25, 50, 60);
        int fourthColor = toBinary(25, 50, 30, 200);

        int[][] startPixels = new int[width][height];
        startPixels[1][2] = firstColor;
        startPixels[4][0] = secondColor;
        startPixels[2][3] = thirdColor;
        startPixels[0][4] = fourthColor;
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[width][height];
        endPixels[1][2] = firstColor;
        endPixels[4][0] = secondColor;
        endPixels[2][3] = thirdColor;
        endPixels[0][4] = fourthColor;
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) ->
                new MockCloseableImage(new int[w][h], area));

        CloseableImage output = interpolator.interpolate(10, 1, start, end);

        assertEquals(firstColor, output.getPixel(1, 2));
        assertEquals(secondColor, output.getPixel(4, 0));
        assertEquals(thirdColor, output.getPixel(2, 3));
        assertEquals(fourthColor, output.getPixel(0, 4));
        assertEquals(width, output.getWidth());
        assertEquals(height, output.getHeight());
    }

    @Test
    public void interpolate_AllTransparent_OutputTransparent() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea.Builder builder = new CloseableImage.VisibleArea.Builder();
        builder.addPixel(1, 2);
        builder.addPixel(4, 0);
        builder.addPixel(2, 3);
        builder.addPixel(0, 4);
        CloseableImage.VisibleArea area = builder.build();

        int[][] startPixels = new int[width][height];
        startPixels[1][2] = toBinary(25, 50, 250, 0);
        startPixels[4][0] = toBinary(25, 250, 50, 0);
        startPixels[2][3] = toBinary(250, 25, 50, 0);
        startPixels[0][4] = toBinary(25, 50, 30, 0);
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[width][height];
        endPixels[1][2] = toBinary(54, 78, 243, 0);
        endPixels[4][0] = toBinary(250, 25, 50, 0);
        endPixels[2][3] = toBinary(25, 250, 50, 0);
        endPixels[0][4] = toBinary(150, 50, 100, 0);
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) ->
                new MockCloseableImage(new int[w][h], area));

        CloseableImage output = interpolator.interpolate(10, 5, start, end);

        assertEquals(toBinary(39, 64, 246, 0), output.getPixel(1, 2));
        assertEquals(toBinary(137, 137, 50, 0), output.getPixel(4, 0));
        assertEquals(toBinary(137, 137, 50, 0), output.getPixel(2, 3));
        assertEquals(toBinary(87, 50, 65, 0), output.getPixel(0, 4));
        assertEquals(width, output.getWidth());
        assertEquals(height, output.getHeight());
    }

    @Test
    public void interpolate_MixedColorsNearStart_CorrectlyAveraged() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea.Builder builder = new CloseableImage.VisibleArea.Builder();
        builder.addPixel(1, 2);
        builder.addPixel(4, 0);
        builder.addPixel(2, 3);
        builder.addPixel(0, 4);
        CloseableImage.VisibleArea area = builder.build();

        int[][] startPixels = new int[width][height];
        startPixels[1][2] = toBinary(184, 143, 65, 197);
        startPixels[4][0] = toBinary(41, 248, 80, 100);
        startPixels[2][3] = toBinary(19, 159, 70, 226);
        startPixels[0][4] = toBinary(216, 101, 41, 195);
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[width][height];
        endPixels[1][2] = toBinary(25, 181, 119, 37);
        endPixels[4][0] = toBinary(106, 126, 174, 11);
        endPixels[2][3] = toBinary(0, 238, 24, 122);
        endPixels[0][4] = toBinary(93, 209, 60, 223);
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) ->
                new MockCloseableImage(new int[w][h], area));

        CloseableImage output = interpolator.interpolate(10, 2, start, end);

        assertEquals(toBinary(152, 150, 75, 197), output.getPixel(1, 2));
        assertEquals(toBinary(54, 223, 98, 100), output.getPixel(4, 0));
        assertEquals(toBinary(15, 174, 60, 226), output.getPixel(2, 3));
        assertEquals(toBinary(191, 122, 44, 195), output.getPixel(0, 4));
        assertEquals(width, output.getWidth());
        assertEquals(height, output.getHeight());
    }

    @Test
    public void interpolate_MixedColorsEven_CorrectlyAveraged() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea.Builder builder = new CloseableImage.VisibleArea.Builder();
        builder.addPixel(1, 2);
        builder.addPixel(4, 0);
        builder.addPixel(2, 3);
        builder.addPixel(0, 4);
        CloseableImage.VisibleArea area = builder.build();

        int[][] startPixels = new int[width][height];
        startPixels[1][2] = toBinary(184, 143, 65, 197);
        startPixels[4][0] = toBinary(41, 248, 80, 100);
        startPixels[2][3] = toBinary(19, 159, 70, 226);
        startPixels[0][4] = toBinary(216, 101, 41, 195);
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[width][height];
        endPixels[1][2] = toBinary(25, 181, 119, 37);
        endPixels[4][0] = toBinary(106, 126, 174, 11);
        endPixels[2][3] = toBinary(0, 238, 24, 122);
        endPixels[0][4] = toBinary(93, 209, 60, 223);
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) ->
                new MockCloseableImage(new int[w][h], area));

        CloseableImage output = interpolator.interpolate(10, 5, start, end);

        assertEquals(toBinary(104, 162, 92, 197), output.getPixel(1, 2));
        assertEquals(toBinary(73, 187, 127, 100), output.getPixel(4, 0));
        assertEquals(toBinary(9, 198, 47, 226), output.getPixel(2, 3));
        assertEquals(toBinary(154, 155, 50, 195), output.getPixel(0, 4));
        assertEquals(width, output.getWidth());
        assertEquals(height, output.getHeight());
    }

    @Test
    public void interpolate_MixedColorsNearEnd_CorrectlyAveraged() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea.Builder builder = new CloseableImage.VisibleArea.Builder();
        builder.addPixel(1, 2);
        builder.addPixel(4, 0);
        builder.addPixel(2, 3);
        builder.addPixel(0, 4);
        CloseableImage.VisibleArea area = builder.build();

        int[][] startPixels = new int[width][height];
        startPixels[1][2] = toBinary(184, 143, 65, 197);
        startPixels[4][0] = toBinary(41, 248, 80, 100);
        startPixels[2][3] = toBinary(19, 159, 70, 226);
        startPixels[0][4] = toBinary(216, 101, 41, 195);
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[width][height];
        endPixels[1][2] = toBinary(25, 181, 119, 37);
        endPixels[4][0] = toBinary(106, 126, 174, 11);
        endPixels[2][3] = toBinary(0, 238, 24, 122);
        endPixels[0][4] = toBinary(93, 209, 60, 223);
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) ->
                new MockCloseableImage(new int[w][h], area));

        CloseableImage output = interpolator.interpolate(10, 8, start, end);

        assertEquals(toBinary(56, 173, 108, 197), output.getPixel(1, 2));
        assertEquals(toBinary(93, 150, 155, 100), output.getPixel(4, 0));
        assertEquals(toBinary(3, 222, 33, 226), output.getPixel(2, 3));
        assertEquals(toBinary(117, 187, 56, 195), output.getPixel(0, 4));
        assertEquals(width, output.getWidth());
        assertEquals(height, output.getHeight());
    }

    @Test
    public void interpolate_DifferentDimensions_DimensionsExpand() {
        CloseableImage.VisibleArea.Builder builder = new CloseableImage.VisibleArea.Builder();
        builder.addPixel(1, 2);
        builder.addPixel(4, 0);
        builder.addPixel(2, 3);
        builder.addPixel(0, 6);
        builder.addPixel(9, 3);
        CloseableImage.VisibleArea area = builder.build();

        int[][] startPixels = new int[5][8];
        startPixels[1][2] = toBinary(184, 143, 65, 197);
        startPixels[4][0] = toBinary(41, 248, 80, 100);
        startPixels[2][3] = toBinary(19, 159, 70, 226);
        startPixels[0][6] = toBinary(216, 101, 41, 195);
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[10][4];
        endPixels[1][2] = toBinary(25, 181, 119, 37);
        endPixels[4][0] = toBinary(106, 126, 174, 11);
        endPixels[2][3] = toBinary(0, 238, 24, 122);
        endPixels[9][3] = toBinary(93, 209, 60, 223);
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) ->
                new MockCloseableImage(new int[w][h], area));

        CloseableImage output = interpolator.interpolate(10, 5, start, end);

        assertEquals(toBinary(104, 162, 92, 197), output.getPixel(1, 2));
        assertEquals(toBinary(73, 187, 127, 100), output.getPixel(4, 0));
        assertEquals(toBinary(9, 198, 47, 226), output.getPixel(2, 3));
        assertEquals(toBinary(108, 50, 20, 195), output.getPixel(0, 6));
        assertEquals(toBinary(46, 104, 30, 0), output.getPixel(9, 3));
        assertEquals(10, output.getWidth());
        assertEquals(8, output.getHeight());
    }

    @Test
    public void interpolate_ColorsOutOfVisibleArea_OutsideColorsIgnored() {
        int width = 5;
        int height = 5;

        CloseableImage.VisibleArea.Builder builder = new CloseableImage.VisibleArea.Builder();
        builder.addPixel(1, 2);
        builder.addPixel(4, 0);
        builder.addPixel(2, 3);
        builder.addPixel(0, 4);
        CloseableImage.VisibleArea area = builder.build();

        int[][] startPixels = new int[width][height];
        startPixels[1][2] = toBinary(184, 143, 65, 197);
        startPixels[4][0] = toBinary(41, 248, 80, 100);
        startPixels[2][3] = toBinary(19, 159, 70, 226);
        startPixels[0][2] = toBinary(216, 101, 41, 195);
        MockCloseableImage start = new MockCloseableImage(startPixels, area);

        int[][] endPixels = new int[width][height];
        endPixels[1][2] = toBinary(25, 181, 119, 37);
        endPixels[4][0] = toBinary(106, 126, 174, 11);
        endPixels[2][3] = toBinary(0, 238, 24, 122);
        endPixels[3][1] = toBinary(93, 209, 60, 223);
        MockCloseableImage end = new MockCloseableImage(endPixels, area);

        CloseableImageInterpolator interpolator = new CloseableImageInterpolator((w, h) ->
                new MockCloseableImage(new int[w][h], area));

        CloseableImage output = interpolator.interpolate(10, 5, start, end);

        assertEquals(toBinary(104, 162, 92, 197), output.getPixel(1, 2));
        assertEquals(toBinary(73, 187, 127, 100), output.getPixel(4, 0));
        assertEquals(toBinary(9, 198, 47, 226), output.getPixel(2, 3));
        assertEquals(toBinary(0, 0, 0, 0), output.getPixel(0, 4));
        assertEquals(toBinary(0, 0, 0, 0), output.getPixel(0, 2));
        assertEquals(toBinary(0, 0, 0, 0), output.getPixel(3, 1));
        assertEquals(width, output.getWidth());
        assertEquals(height, output.getHeight());
    }

    private int toBinary(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

}