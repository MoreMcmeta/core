package io.github.soir20.moremcmeta.client.texture;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.client.animation.AnimationFrameManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Tests the {@link AnimationComponent}.
 * @author soir20
 */
public class AnimationComponentTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NotSyncedNullManager_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        expectedException.expect(NullPointerException.class);
        builder.add(new AnimationComponent(null));
    }

    @Test
    public void construct_SyncedNegativeTicks_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AnimationFrameManager<MockRGBAImageFrame> frameManager = makeFrameManager();
        AtomicLong currentTime = new AtomicLong(800);

        expectedException.expect(IllegalArgumentException.class);
        builder.add(new AnimationComponent(-1, () -> Optional.of(currentTime.incrementAndGet()),
                frameManager));
    }

    @Test
    public void construct_SyncedZeroTicks_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AnimationFrameManager<MockRGBAImageFrame> frameManager = makeFrameManager();
        AtomicLong currentTime = new AtomicLong(800);

        expectedException.expect(IllegalArgumentException.class);
        builder.add(new AnimationComponent(0, () -> Optional.of(currentTime.incrementAndGet()),
                frameManager));
    }

    @Test
    public void construct_SyncedNullTimeGetter_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AnimationFrameManager<MockRGBAImageFrame> frameManager = makeFrameManager();

        expectedException.expect(NullPointerException.class);
        builder.add(new AnimationComponent(800, null,
                frameManager));
    }

    @Test
    public void construct_SyncedNullManager_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AtomicLong currentTime = new AtomicLong(800);

        expectedException.expect(NullPointerException.class);
        builder.add(new AnimationComponent(800, () -> Optional.of(currentTime.incrementAndGet()),
                null));
    }

    @Test
    @SuppressWarnings("OptionalAssignedToNull")
    public void tick_SyncedTimeGetterReturnsNull_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AnimationFrameManager<MockRGBAImageFrame> frameManager = makeFrameManager();

        expectedException.expect(NullPointerException.class);
        builder.add(new AnimationComponent(375, () -> null,
                frameManager));
        builder.build().tick();
    }

    @Test
    public void tick_NotSyncedLoop_SameAnimFrame() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());
        builder.add(new AnimationComponent(makeFrameManager()));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> assertEquals(1, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));
        EventDrivenTexture texture = builder.build();

        int animationLength = 550;
        for (int tick = 0; tick < animationLength; tick++) {
            texture.tick();
        }
        texture.bind();
    }

    @Test
    public void tick_NotSyncedPartWay_CorrectAnimFrame() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());
        builder.add(new AnimationComponent(makeFrameManager()));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> assertEquals(8, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));
        EventDrivenTexture texture = builder.build();

        int ticksInAnimation = 330;
        for (int tick = 0; tick < ticksInAnimation; tick++) {
            texture.tick();
        }
        texture.bind();
    }

    @Test
    public void tick_SyncsToSameLoop_SameAnimFrame() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AnimationFrameManager<MockRGBAImageFrame> frameManager = makeFrameManager();
        AtomicLong currentTime = new AtomicLong(800);
        builder.add(new AnimationComponent(800, () -> Optional.of(currentTime.incrementAndGet()),
                frameManager));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> assertEquals(1, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));
        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.bind();
    }

    @Test
    public void tick_SyncsToSame_SameAnimFrame() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AnimationFrameManager<MockRGBAImageFrame> frameManager = makeFrameManager();
        AtomicLong currentTime = new AtomicLong(-1);
        builder.add(new AnimationComponent(800, () -> Optional.of(currentTime.incrementAndGet()),
                frameManager));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> assertEquals(1, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));
        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.bind();
    }

    @Test
    public void tick_SyncsForward_FrameAtTime() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AnimationFrameManager<MockRGBAImageFrame> frameManager = makeFrameManager();
        AtomicLong currentTime = new AtomicLong(375);
        builder.add(new AnimationComponent(800, () -> Optional.of(currentTime.incrementAndGet()),
                frameManager));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> assertEquals(9, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));
        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.bind();
    }

    @Test
    public void tick_SyncsBackward_FrameAtTime() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AnimationFrameManager<MockRGBAImageFrame> frameManager = makeFrameManager();
        AtomicLong currentTime = new AtomicLong(-375);
        builder.add(new AnimationComponent(800, () -> Optional.of(currentTime.incrementAndGet()),
                frameManager));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> assertEquals(9, ((MockRGBAImageFrame) state.getImage()).getFrameNumber()))));
        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.bind();
    }

    @Test
    public void upload_NotSynced_FrameUpdated() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());
        builder.add(new AnimationComponent(makeFrameManager()));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD, (state) ->
                assertEquals(1, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));
        EventDrivenTexture texture = builder.build();

        texture.bind();
    }

    @Test
    public void upload_StartsPartiallyInAnimNotSynced_CurrentAnimFrame() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AnimationFrameManager<MockRGBAImageFrame> frameManager = makeFrameManager();
        frameManager.tick(43);
        builder.add(new AnimationComponent(frameManager));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> assertEquals(3, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));
        EventDrivenTexture texture = builder.build();

        texture.bind();
    }

    @Test
    public void upload_Synced_FrameUpdated() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AtomicLong currentTime = new AtomicLong(0);
        builder.add(new AnimationComponent(500, () -> Optional.of(currentTime.incrementAndGet()),
                makeFrameManager()));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> assertEquals(1, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));
        EventDrivenTexture texture = builder.build();

        texture.bind();
    }

    @Test
    public void upload_StartsPartiallyInAnimSynced_CurrentAnimFrame() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        AnimationFrameManager<MockRGBAImageFrame> frameManager = makeFrameManager();
        frameManager.tick(43);
        AtomicLong currentTime = new AtomicLong(0);
        builder.add(new AnimationComponent(500, () -> Optional.of(currentTime.incrementAndGet()),
                frameManager));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> assertEquals(3, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));
        EventDrivenTexture texture = builder.build();

        texture.bind();
    }

    private AnimationFrameManager<MockRGBAImageFrame> makeFrameManager() {
        ImmutableList.Builder<MockRGBAImageFrame> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 11; frame++) {
            mockFramesBuilder.add(new MockRGBAImageFrame(frame));
        }

        ImmutableList<MockRGBAImageFrame> mockFrames = mockFramesBuilder.build();

        int frameLength = 10;
        return new AnimationFrameManager<>(mockFrames, (frame) -> frame.getFrameNumber() * frameLength);
    }

}