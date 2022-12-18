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

package io.github.moremcmeta.moremcmeta.impl.client.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link FrameReader} with predefined and not-defined frames.
 * @author soir20
 */
public class FrameReaderTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void findFrames_FrameFactoryNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new FrameReader<>(null);
    }

    @Test
    public void findFrames_CreatedFrameNull_NullPointerException() {
        FrameReader<MockFrame> frameReader = new FrameReader<>((data) -> null);
        int frameWidth = 100;
        int frameHeight = 100;

        expectedException.expect(NullPointerException.class);
        frameReader.read(frameWidth * 5, frameHeight, 10, 10);
    }

    @Test
    public void getDefinedFrames_CreatedFrameNull_NullPointerException() {
        FrameReader<MockFrame> frameReader = new FrameReader<>((data) -> null);
        int frameWidth = 100;
        int frameHeight = 100;

        expectedException.expect(NullPointerException.class);
        frameReader.read(frameWidth * 5, frameHeight, frameWidth, frameHeight);
    }

    @Test
    public void findFrames_EmptyImageWidth_IllegalArgException() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(0, 100, 10, 10);
    }

    @Test
    public void findFrames_EmptyImageHeight_IllegalArgException() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 0, 10, 10);
    }

    @Test
    public void findFrames_BothImageDimensionsEmpty_IllegalArgException() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(0, 0, 10, 10);
    }

    @Test
    public void findFrames_DefinedWidthNotMultipleOfSize_FindsFrames() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);
        int frameWidth = 30;
        int frameHeight = 35;

        List<MockFrame> frames = frameReader.read(100, 70, frameWidth, frameHeight);
        assertEquals(6, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).width());
            assertEquals(frameHeight, frames.get(frameIndex).height());
            assertEquals(frameWidth * (frameIndex % 3), frames.get(frameIndex).xOffset());
            assertEquals(frameHeight * (frameIndex / 3), frames.get(frameIndex).yOffset());
        }
    }

    @Test
    public void findFrames_DefinedHeightNotMultipleOfSize_ReadsFramesInBounds() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);
        int frameWidth = 35;
        int frameHeight = 30;

        List<MockFrame> frames = frameReader.read(70, 100, frameWidth, frameHeight);
        assertEquals(6, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).width());
            assertEquals(frameHeight, frames.get(frameIndex).height());
            assertEquals(frameWidth * (frameIndex % 2), frames.get(frameIndex).xOffset());
            assertEquals(frameHeight * (frameIndex / 2), frames.get(frameIndex).yOffset());
        }
    }

    @Test
    public void findFrames_ZeroFrameWidth_IllegalArgException() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 100, 0, 10);
    }

    @Test
    public void findFrames_ZeroFrameHeight_IllegalArgException() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 100, 10, 0);
    }

    @Test
    public void findFrames_ZeroFrameBothDimensions_IllegalArgException() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 100, 0, 0);
    }

    @Test
    public void findFrames_SquareImageDefinedDimensions_SingleFrame() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);

        List<MockFrame> frames = frameReader.read(100, 100, 100, 100);
        assertEquals(1, frames.size());
        assertEquals(100, frames.get(0).width());
        assertEquals(100, frames.get(0).height());
        assertEquals(0, frames.get(0).xOffset());
        assertEquals(0, frames.get(0).yOffset());
    }

    @Test
    public void findFrames_LongerWidthDefinedDimensions_MultipleSquareFrames() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        List<MockFrame> frames = frameReader.read(frameWidth * 5, frameHeight, frameWidth, frameHeight);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).width());
            assertEquals(frameHeight, frames.get(frameIndex).height());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).xOffset());
            assertEquals(0, frames.get(frameIndex).yOffset());
        }
    }

    @Test
    public void findFrames_LongerHeightDefinedDimensions_MultipleSquareFrames() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        List<MockFrame> frames = frameReader.read(frameWidth, frameHeight * 5, frameWidth, frameHeight);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).width());
            assertEquals(frameHeight, frames.get(frameIndex).height());
            assertEquals(0, frames.get(frameIndex).xOffset());
            assertEquals(frameHeight * frameIndex, frames.get(frameIndex).yOffset());
        }
    }

    @Test
    public void findFrames_LandscapeRectangularDefinedDimensions_MultipleRectangularFrames() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);
        int frameWidth = 100;
        int frameHeight = 20;

        List<MockFrame> frames = frameReader.read(frameWidth * 5, frameHeight, frameWidth, frameHeight);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).width());
            assertEquals(frameHeight, frames.get(frameIndex).height());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).xOffset());
            assertEquals(0, frames.get(frameIndex).yOffset());
        }
    }

    @Test
    public void findFrames_PortraitRectangularDefinedDimensions_MultipleRectangularFrames() {
        FrameReader<MockFrame> frameReader = new FrameReader<>(MockFrame::new);
        int frameWidth = 20;
        int frameHeight = 100;

        List<MockFrame> frames = frameReader.read(frameWidth * 5, frameHeight, frameWidth, frameHeight);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).width());
            assertEquals(frameHeight, frames.get(frameIndex).height());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).xOffset());
            assertEquals(0, frames.get(frameIndex).yOffset());
        }
    }

    /**
     * Mocks a frame. Essentially a wrapper for frame data that can be validated.
     * @author soir20
     */
    private static class MockFrame {
        private final int WIDTH;
        private final int HEIGHT;
        private final int X_OFFSET;
        private final int Y_OFFSET;

        public MockFrame(FrameReader.FrameData frameData) {
            WIDTH = frameData.width();
            HEIGHT = frameData.height();
            X_OFFSET = frameData.xOffset();
            Y_OFFSET = frameData.yOffset();
        }

        public int width() {
            return WIDTH;
        }

        public int height() {
            return HEIGHT;
        }

        public int xOffset() {
            return X_OFFSET;
        }

        public int yOffset() {
            return Y_OFFSET;
        }

    }

}