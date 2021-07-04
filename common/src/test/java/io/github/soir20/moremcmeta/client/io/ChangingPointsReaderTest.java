package io.github.soir20.moremcmeta.client.io;

import com.google.common.collect.ImmutableSet;
import io.github.soir20.moremcmeta.client.texture.IRGBAImage;
import io.github.soir20.moremcmeta.client.texture.MockRGBAImage;
import io.github.soir20.moremcmeta.math.Point;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Tests the {@link ChangingPointsReader}.
 * @author soir20
 */
public class ChangingPointsReaderTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void read_NullImage_NullPointerException() {
        ChangingPointsReader reader = new ChangingPointsReader();
        expectedException.expect(NullPointerException.class);
        reader.read(null, 5, 5, 3);
    }

    @Test
    public void read_EmptyWidth_IllegalArgException() {
        ChangingPointsReader reader = new ChangingPointsReader();
        expectedException.expect(IllegalArgumentException.class);
        reader.read(new MockRGBAImage(20, 20), 0, 5, 3);
    }

    @Test
    public void read_EmptyHeight_IllegalArgException() {
        ChangingPointsReader reader = new ChangingPointsReader();
        expectedException.expect(IllegalArgumentException.class);
        reader.read(new MockRGBAImage(20, 20), 5, 0, 3);
    }

    @Test
    public void read_NegativeMipmapLevel_IllegalArgException() {
        ChangingPointsReader reader = new ChangingPointsReader();
        expectedException.expect(IllegalArgumentException.class);
        reader.read(new MockRGBAImage(20, 20), 5, 5, -1);
    }

    @Test
    public void read_ChangingPointsInFrameMiddle_PointsDetected() {
        int[][] image = new int[10][10];
        image[7][8] = toBinary(206, 0, 27, 131);
        image[2][1] = toBinary(240, 200, 185, 147);
        image[2][6] = toBinary(201, 40, 58, 223);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(2, 3), new Point(2, 1));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_IdenticalFrames_NoPointsDetected() {
        int[][] image = new int[10][10];
        int demoColor = toBinary(206, 0, 27, 131);
        for (int column = 0; column < 10; column++) {
            Arrays.fill(image[column], demoColor);
        }

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of();
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_TransparentPointsDifferentRGBComponents_TransparentPointExcluded() {
        int[][] image = new int[10][10];
        image[7][8] = toBinary(206, 0, 27, 131);
        image[7][3] = toBinary(94, 29, 199, 0);
        image[2][1] = toBinary(240, 200, 185, 0);
        image[2][6] = toBinary(201, 40, 58, 0);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(2, 3));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_SameRGBComponentsDifferentAlpha_PointDetected() {
        int[][] image = new int[10][10];
        image[7][8] = toBinary(206, 0, 27, 131);
        image[7][3] = toBinary(206, 0, 27, 240);
        image[2][1] = toBinary(240, 200, 185, 0);
        image[2][6] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(2, 1), new Point(2, 3));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_PointsOnLeftEdge_PointsDetected() {
        int[][] image = new int[10][10];
        image[5][8] = toBinary(206, 0, 27, 131);
        image[0][6] = toBinary(206, 0, 27, 240);
        image[0][1] = toBinary(240, 200, 185, 0);
        image[5][2] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(0, 3), new Point(0, 1),
                new Point(0, 2));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_PointsOnRightEdge_PointsDetected() {
        int[][] image = new int[10][10];
        image[9][8] = toBinary(206, 0, 27, 131);
        image[4][6] = toBinary(206, 0, 27, 240);
        image[4][1] = toBinary(240, 200, 185, 0);
        image[9][2] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(4, 3), new Point(4, 1),
                new Point(4, 2));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_PointsOnTopEdge_PointsDetected() {
        int[][] image = new int[10][10];
        image[8][5] = toBinary(206, 0, 27, 131);
        image[6][0] = toBinary(206, 0, 27, 240);
        image[1][0] = toBinary(240, 200, 185, 0);
        image[2][5] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(3, 0), new Point(1, 0),
                new Point(2, 0));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_PointsOnBottomEdge_PointsDetected() {
        int[][] image = new int[10][10];
        image[8][9] = toBinary(206, 0, 27, 131);
        image[6][4] = toBinary(206, 0, 27, 240);
        image[1][4] = toBinary(240, 200, 185, 0);
        image[2][9] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(3, 4), new Point(1, 4),
                new Point(2, 4));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_PointsOnTopLeftCorner_PointsDetected() {
        int[][] image = new int[10][10];
        image[0][0] = toBinary(206, 0, 27, 131);
        image[0][5] = toBinary(206, 0, 27, 240);
        image[5][0] = toBinary(240, 200, 185, 0);
        image[5][5] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(0, 0));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_PointsOnBottomLeftCorner_PointsDetected() {
        int[][] image = new int[10][10];
        image[0][4] = toBinary(206, 0, 27, 131);
        image[0][9] = toBinary(206, 0, 27, 240);
        image[5][4] = toBinary(240, 200, 185, 0);
        image[5][9] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(0, 4));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_PointsOnTopRightCorner_PointsDetected() {
        int[][] image = new int[10][10];
        image[4][0] = toBinary(206, 0, 27, 131);
        image[4][5] = toBinary(206, 0, 27, 240);
        image[9][0] = toBinary(240, 200, 185, 0);
        image[9][5] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(4, 0));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_PointsOnBottomRightCorner_PointsDetected() {
        int[][] image = new int[10][10];
        image[4][4] = toBinary(206, 0, 27, 131);
        image[4][9] = toBinary(206, 0, 27, 240);
        image[9][4] = toBinary(240, 200, 185, 0);
        image[9][9] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(4, 4));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_ImageTooWide_PointsOutsideIgnored() {
        int[][] image = new int[14][10];
        image[2][3] = toBinary(206, 0, 27, 131);
        image[1][9] = toBinary(206, 0, 27, 240);
        image[12][5] = toBinary(240, 200, 185, 0);
        image[10][1] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(2, 3), new Point(1, 4));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_ImageTooHigh_PointsOutsideIgnored() {
        int[][] image = new int[10][14];
        image[2][3] = toBinary(206, 0, 27, 131);
        image[1][9] = toBinary(206, 0, 27, 240);
        image[5][12] = toBinary(240, 200, 185, 0);
        image[1][10] = toBinary(201, 40, 58, 17);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 2);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(2, 3), new Point(1, 4));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_NoMipmaps_OneAreaGenerated() {
        int[][] image = new int[10][10];
        image[7][8] = toBinary(206, 0, 27, 131);
        image[2][1] = toBinary(240, 200, 185, 147);
        image[2][6] = toBinary(201, 40, 58, 223);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 0);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(2, 3), new Point(2, 1));
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_EmptyImage_EmptyAreasGenerated() {
        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(0, 0), 5, 5, 3);
        Set<Point> expectedPoints = ImmutableSet.of();
        assertTrue(containsOnlyPoints(areas, expectedPoints));
    }

    @Test
    public void read_EmptyMipmap_EmptyAreaGeneratedForMipmap() {
        int[][] image = new int[10][10];
        image[7][8] = toBinary(206, 0, 27, 131);
        image[2][1] = toBinary(240, 200, 185, 147);
        image[2][6] = toBinary(201, 40, 58, 223);

        ChangingPointsReader reader = new ChangingPointsReader();
        List<IRGBAImage.VisibleArea> areas = reader.read(new MockRGBAImage(image), 5, 5, 3);
        Set<Point> expectedPoints = ImmutableSet.of(new Point(2, 3), new Point(2, 1));
        assertTrue(containsOnlyPoints(areas.get(0), expectedPoints));
        assertTrue(containsOnlyPoints(areas.get(1), mipmapPoints(expectedPoints, 1)));
        assertTrue(containsOnlyPoints(areas.get(2), mipmapPoints(expectedPoints, 2)));
        assertTrue(containsOnlyPoints(areas.get(3), ImmutableSet.of()));
    }

    private boolean containsOnlyPoints(List<IRGBAImage.VisibleArea> areas, Set<Point> points) {
        return IntStream.range(0, areas.size()).allMatch(
                (level) -> containsOnlyPoints(areas.get(level), mipmapPoints(points, level))
        );
    }

    private Set<Point> mipmapPoints(Set<Point> points, int level) {
        return points.stream().map(
                (point) -> new Point(point.getX() >> level, point.getY() >> level)
        ).collect(Collectors.toSet());
    }

    private boolean containsOnlyPoints(IRGBAImage.VisibleArea area, Set<Point> points) {
        Set<Point> foundPoints = new HashSet<>();
        for (Point foundPoint : area) {
            foundPoints.add(foundPoint);
        }

        return points.equals(foundPoints);
    }

    private int toBinary(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

}