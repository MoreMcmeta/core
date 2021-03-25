package io.github.soir20.moremcmeta.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the {@link FrameReader} with predefined and not-defined frames.
 * @author soir20
 */
public class FrameReaderTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final AnimationMetadataSection EMPTY_ANIM_DATA = new AnimationMetadataSection(ImmutableList.of(),
            -1, -1, 1, false);

    @Test
    @SuppressWarnings("ConstantConditions")
    public void findFrames_FrameFactoryNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new FrameReader<>(null);
    }

    @Test
    public void findFrames_CreatedFrameNull_NullPointerException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>((data) -> null);
        int frameWidth = 100;
        int frameHeight = 100;

        expectedException.expect(NullPointerException.class);
        frameReader.read(frameWidth * 5, frameHeight, EMPTY_ANIM_DATA);
    }

    @Test
    public void getDefinedFrames_CreatedFrameNull_NullPointerException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>((data) -> null);
        int frameWidth = 100;
        int frameHeight = 100;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, 1));
        predefinedFrames.add(new AnimationFrame(1, 2));
        predefinedFrames.add(new AnimationFrame(2, 3));
        predefinedFrames.add(new AnimationFrame(3, 4));
        predefinedFrames.add(new AnimationFrame(4, 5));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, 1, false);

        expectedException.expect(NullPointerException.class);
        frameReader.read(frameWidth * 5, frameHeight, metadata);
    }

    @Test
    public void findFrames_EmptyWidth_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(0, 100, EMPTY_ANIM_DATA);
    }

    @Test
    public void findFrames_EmptyHeight_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 0, EMPTY_ANIM_DATA);
    }

    @Test
    public void findFrames_BothDimensionsEmpty_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(0, 0, EMPTY_ANIM_DATA);
    }

    @Test
    public void findFrames_AssumedWidthNotMultipleOfSize_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 70, EMPTY_ANIM_DATA);
    }

    @Test
    public void findFrames_AssumedHeightNotMultipleOfSize_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(70, 100, EMPTY_ANIM_DATA);
    }

    @Test
    public void findFrames_DefinedWidthNotMultipleOfSize_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 70, new AnimationMetadataSection(ImmutableList.of(),
                30, 35, 1, false));
    }

    @Test
    public void findFrames_DefinedHeightNotMultipleOfSize_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(70, 100, new AnimationMetadataSection(ImmutableList.of(),
                35, 30, 1, false));
    }

    @Test
    public void findFrames_ZeroFrameWidth_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 100, new AnimationMetadataSection(ImmutableList.of(),
                0, 10, 1, false));
    }

    @Test
    public void findFrames_ZeroFrameHeight_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 100, new AnimationMetadataSection(ImmutableList.of(),
                10, 0, 1, false));
    }

    @Test
    public void findFrames_ZeroFrameBothDimensions_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 100, new AnimationMetadataSection(ImmutableList.of(),
                0, 0, 1, false));
    }

    @Test
    public void findFrames_SquareImageAssumedDimensions_SingleFrame() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        List<MockAnimationFrame> frames = frameReader.read(100, 100, EMPTY_ANIM_DATA);
        assertEquals(1, frames.size());
        assertEquals(100, frames.get(0).getWidth());
        assertEquals(100, frames.get(0).getHeight());
        assertEquals(0, frames.get(0).getXOffset());
        assertEquals(0, frames.get(0).getYOffset());
    }

    @Test
    public void findFrames_SquareImageDefinedDimensions_SingleFrame() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);

        List<MockAnimationFrame> frames = frameReader.read(100, 100,
                new AnimationMetadataSection(ImmutableList.of(), 100, 100, 1, false));
        assertEquals(1, frames.size());
        assertEquals(100, frames.get(0).getWidth());
        assertEquals(100, frames.get(0).getHeight());
        assertEquals(0, frames.get(0).getXOffset());
        assertEquals(0, frames.get(0).getYOffset());
    }

    @Test
    public void findFrames_LongerWidthAssumedDimensions_MultipleSquareFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, EMPTY_ANIM_DATA);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void findFrames_LongerHeightAssumedDimensions_MultipleSquareFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        List<MockAnimationFrame> frames = frameReader.read(frameWidth, frameHeight * 5, EMPTY_ANIM_DATA);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(0, frames.get(frameIndex).getXOffset());
            assertEquals(frameHeight * frameIndex, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void findFrames_LongerWidthDefinedDimensions_MultipleSquareFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight,
                new AnimationMetadataSection(ImmutableList.of(), frameWidth, frameHeight, 1, false));
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void findFrames_LongerHeightDefinedDimensions_MultipleSquareFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        List<MockAnimationFrame> frames = frameReader.read(frameWidth, frameHeight * 5,
                new AnimationMetadataSection(ImmutableList.of(), frameWidth, frameHeight, 1, false));
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(0, frames.get(frameIndex).getXOffset());
            assertEquals(frameHeight * frameIndex, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void findFrames_LandscapeRectangularDefinedDimensions_MultipleRectangularFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 20;

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight,
                new AnimationMetadataSection(ImmutableList.of(), frameWidth, frameHeight, 1, false));
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void findFrames_PortraitRectangularDefinedDimensions_MultipleRectangularFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 20;
        int frameHeight = 100;

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight,
                new AnimationMetadataSection(ImmutableList.of(), frameWidth, frameHeight, 1, false));
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void findFrames_WithInterpolation_NoInterpolatedFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight,
                new AnimationMetadataSection(ImmutableList.of(), frameWidth, frameHeight, 1, true));
        assertEquals(5, frames.size());
    }

    @Test
    public void findFrames_TimeDefined_FrameTimesDefault() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight,
                new AnimationMetadataSection(ImmutableList.of(), frameWidth, frameHeight, 1, false));
        for (MockAnimationFrame frame : frames) {
            assertEquals(1, frame.getFrameTime());
        }
    }

    @Test
    public void findFrames_TimeNotDefined_FrameTimesEmpty() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight,
                new AnimationMetadataSection(ImmutableList.of(), frameWidth, frameHeight, FrameReader.FrameData.EMPTY_TIME, false));
        for (MockAnimationFrame frame : frames) {
            assertEquals(FrameReader.FrameData.EMPTY_TIME, frame.getFrameTime());
        }
    }

    @Test
    public void getDefinedFrames_EmptyWidth_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, -1, -1, 1, false);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(0, 100, metadata);
    }

    @Test
    public void getDefinedFrames_EmptyHeight_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, -1, -1, 1, false);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 0, metadata);
    }

    @Test
    public void getDefinedFrames_BothDimensionsEmpty_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, -1, -1, 1, false);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(0, 0, metadata);
    }

    @Test
    public void getDefinedFrames_AssumedWidthNotMultipleOfSize_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, -1, -1, 1, false);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 70, metadata);
    }

    @Test
    public void getDefinedFrames_AssumedHeightNotMultipleOfSize_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, -1, -1, 1, false);

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(70, 100, metadata);
    }

    @Test
    public void getDefinedFrames_DefinedWidthNotMultipleOfSize_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 70, new AnimationMetadataSection(predefinedFrames,
                30, 35, 1, false));
    }

    @Test
    public void getDefinedFrames_DefinedHeightNotMultipleOfSize_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(70, 100, new AnimationMetadataSection(predefinedFrames,
                35, 30, 1, false));
    }

    @Test
    public void getDefinedFrames_ZeroFrameWidth_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 100, new AnimationMetadataSection(predefinedFrames,
                0, 10, 1, false));
    }

    @Test
    public void getDefinedFrames_ZeroFrameHeight_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 100, new AnimationMetadataSection(predefinedFrames,
                10, 0, 1, false));
    }

    @Test
    public void getDefinedFrames_ZeroFrameBothDimensions_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(100, 100, new AnimationMetadataSection(predefinedFrames,
                0, 0, 1, false));
    }

    @Test
    public void getDefinedFrames_IndexJustOutOfImageBounds_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(5, FrameReader.FrameData.EMPTY_TIME));

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(500, 100, new AnimationMetadataSection(predefinedFrames,
                100, 100, 1, false));
    }

    @Test
    public void getDefinedFrames_IndexFarOutOfImageBounds_IllegalArgException() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(17, FrameReader.FrameData.EMPTY_TIME));

        expectedException.expect(IllegalArgumentException.class);
        frameReader.read(500, 100, new AnimationMetadataSection(predefinedFrames,
                100, 100, 1, false));
    }

    @Test
    public void getDefinedFrames_SquareImageAssumedDimensions_SingleFrame() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, -1, -1, 1, false);

        List<MockAnimationFrame> frames = frameReader.read(100, 100, metadata);
        assertEquals(1, frames.size());
        assertEquals(100, frames.get(0).getWidth());
        assertEquals(100, frames.get(0).getHeight());
        assertEquals(0, frames.get(0).getXOffset());
        assertEquals(0, frames.get(0).getYOffset());
    }

    @Test
    public void getDefinedFrames_SquareImageDefinedDimensions_SingleFrame() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, 100, 100, 1, false);

        List<MockAnimationFrame> frames = frameReader.read(100, 100, metadata);
        assertEquals(1, frames.size());
        assertEquals(100, frames.get(0).getWidth());
        assertEquals(100, frames.get(0).getHeight());
        assertEquals(0, frames.get(0).getXOffset());
        assertEquals(0, frames.get(0).getYOffset());
    }

    @Test
    public void getDefinedFrames_LongerWidthAssumedDimensions_MultipleSquareFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, 1, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void getDefinedFrames_LongerHeightAssumedDimensions_MultipleSquareFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, 1, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth, frameHeight * 5, metadata);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(0, frames.get(frameIndex).getXOffset());
            assertEquals(frameHeight * frameIndex, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void getDefinedFrames_LongerWidthDefinedDimensions_MultipleSquareFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, 1, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void getDefinedFrames_LongerHeightDefinedDimensions_MultipleSquareFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, 1, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth, frameHeight * 5, metadata);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(0, frames.get(frameIndex).getXOffset());
            assertEquals(frameHeight * frameIndex, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void getDefinedFrames_LandscapeRectangularDefinedDimensions_MultipleRectangularFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 20;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, 1, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void getDefinedFrames_PortraitRectangularDefinedDimensions_MultipleRectangularFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 20;
        int frameHeight = 100;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, 1, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * frameIndex, frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void getDefinedFrames_WithInterpolation_NoInterpolatedFrames() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, 1, true);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        assertEquals(5, frames.size());
    }

    @Test
    public void getDefinedFrames_TimeDefined_FrameTimesDefault() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, 1, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        for (MockAnimationFrame frame : frames) {
            assertEquals(1, frame.getFrameTime());
        }
    }

    @Test
    public void getDefinedFrames_IndividualTimeDefined_FrameTimesAsDefined() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, 1));
        predefinedFrames.add(new AnimationFrame(1, 2));
        predefinedFrames.add(new AnimationFrame(2, 3));
        predefinedFrames.add(new AnimationFrame(3, 4));
        predefinedFrames.add(new AnimationFrame(4, 5));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, 1, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameIndex + 1, frames.get(frameIndex).getFrameTime());
        }
    }

    @Test
    public void getDefinedFrames_TimeNotDefined_FrameTimesEmpty() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        predefinedFrames.add(new AnimationFrame(0, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(1, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(2, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(3, FrameReader.FrameData.EMPTY_TIME));
        predefinedFrames.add(new AnimationFrame(4, FrameReader.FrameData.EMPTY_TIME));

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, FrameReader.FrameData.EMPTY_TIME, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        for (MockAnimationFrame frame : frames) {
            assertEquals(FrameReader.FrameData.EMPTY_TIME, frame.getFrameTime());
        }
    }

    @Test
    public void getDefinedFrames_FramesOutOfOrder_FramesInSameOrder() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        int[] indexOrder = {3, 2, 0, 4, 1};
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        for (int index : indexOrder) {
            predefinedFrames.add(new AnimationFrame(index, FrameReader.FrameData.EMPTY_TIME));
        }

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, FrameReader.FrameData.EMPTY_TIME, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * indexOrder[frameIndex], frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void getDefinedFrames_FramesDuplicated_DuplicatesIncluded() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        int[] indexOrder = {0, 0, 1, 1, 2};
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        for (int index : indexOrder) {
            predefinedFrames.add(new AnimationFrame(index, FrameReader.FrameData.EMPTY_TIME));
        }

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, FrameReader.FrameData.EMPTY_TIME, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        assertEquals(5, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * indexOrder[frameIndex], frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }

    @Test
    public void getDefinedFrames_FramesSkipped_SkippedFramesNotIncluded() {
        FrameReader<MockAnimationFrame> frameReader = new FrameReader<>(MockAnimationFrame::new);
        int frameWidth = 100;
        int frameHeight = 100;

        int[] indexOrder = {0, 2, 4};
        List<AnimationFrame> predefinedFrames = new ArrayList<>();
        for (int index : indexOrder) {
            predefinedFrames.add(new AnimationFrame(index, FrameReader.FrameData.EMPTY_TIME));
        }

        AnimationMetadataSection metadata = new AnimationMetadataSection(predefinedFrames, frameWidth, frameHeight, FrameReader.FrameData.EMPTY_TIME, false);

        List<MockAnimationFrame> frames = frameReader.read(frameWidth * 5, frameHeight, metadata);
        assertEquals(3, frames.size());
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            assertEquals(frameWidth, frames.get(frameIndex).getWidth());
            assertEquals(frameHeight, frames.get(frameIndex).getHeight());
            assertEquals(frameWidth * indexOrder[frameIndex], frames.get(frameIndex).getXOffset());
            assertEquals(0, frames.get(frameIndex).getYOffset());
        }
    }
}