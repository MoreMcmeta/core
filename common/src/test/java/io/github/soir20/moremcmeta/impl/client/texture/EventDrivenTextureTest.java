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

package io.github.soir20.moremcmeta.impl.client.texture;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.api.client.texture.Color;
import io.github.soir20.moremcmeta.api.client.texture.ColorTransform;
import io.github.soir20.moremcmeta.api.client.texture.FrameGroup;
import io.github.soir20.moremcmeta.api.client.texture.FrameView;
import io.github.soir20.moremcmeta.api.client.texture.PersistentFrameView;
import io.github.soir20.moremcmeta.api.client.texture.TextureComponent;
import io.github.soir20.moremcmeta.api.math.Point;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link EventDrivenTexture}.
 * @author soir20
 */
public class EventDrivenTextureTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void build_EmptyPredefinedFrames_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(IllegalArgumentException.class);
        builder.setPredefinedFrames(List.of());
    }

    @Test
    public void build_NullPredefinedFrames_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(NullPointerException.class);
        builder.setPredefinedFrames(null);
    }

    @Test
    public void build_NoPredefinedFrames_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new TextureComponent<>() {});
        builder.setGeneratedFrame(new MockCloseableImageFrame());

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_SetPredefinedFramesTwice_HasLastList() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame()));
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.add(new CoreTextureComponent() {
            @Override
            public void onRegistration(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                assertEquals(2, predefinedFrames.frames());
            }
        });
        builder.build().load(null);
    }

    @Test
    public void build_NullGeneratedFrame_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(NullPointerException.class);
        builder.setPredefinedFrames(null);
    }

    @Test
    public void build_NoGeneratedFrame_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new TextureComponent<>() {});
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame()));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_SetGeneratedFrameTwice_HasLastFrame() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame();
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        builder.setGeneratedFrame(generatedFrame);

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame()));
        builder.add(new CoreTextureComponent() {
            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> new Color(0), List.of(), List.of());
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        EventDrivenTexture texture = builder.build();
        texture.upload();

        assertEquals(1, generatedFrame.uploadCount());
    }

    @Test
    public void build_NullComponent_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());

        expectedException.expect(NullPointerException.class);
        builder.add(null);
    }

    @Test
    public void build_NoListeners_NoException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        builder.build();
    }

    @Test
    public void build_PredefinedFramesWithDifferentMipLevels_IllegalArgException() {
        MockCloseableImageFrame lowerMipmapFrame = new MockCloseableImageFrame(
                ImmutableList.of(new MockCloseableImage())
        );

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(IllegalArgumentException.class);
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), lowerMipmapFrame));
    }

    @Test
    public void build_PredefinedFramesWithDifferentWidths_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(IllegalArgumentException.class);
        builder.setPredefinedFrames(List.of(
                new MockCloseableImageFrame(100, 200),
                new MockCloseableImageFrame(50, 200)
        ));
    }

    @Test
    public void build_PredefinedFramesWithDifferentHeights_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(IllegalArgumentException.class);
        builder.setPredefinedFrames(List.of(
                new MockCloseableImageFrame(50, 200),
                new MockCloseableImageFrame(50, 100)
        ));
    }

    @Test
    public void build_PredefinedFrameHasLowerMipLevelThanGenerated_IllegalStateException() {
        MockCloseableImageFrame lowerMipmapFrame = new MockCloseableImageFrame(
                ImmutableList.of(new MockCloseableImage())
        );

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(List.of(lowerMipmapFrame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFrameHasHigherMipLevelThanGenerated_IllegalStateException() {
        MockCloseableImageFrame lowerMipmapFrame = new MockCloseableImageFrame(
                ImmutableList.of(new MockCloseableImage())
        );

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame()));
        builder.setGeneratedFrame(lowerMipmapFrame);

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFrameHasLargerWidthThanGenerated_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(100, 200)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(50, 200));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFrameHasSmallerWidthThanGenerated_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(50, 200)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(100, 200));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFrameHasLargerHeightThanGenerated_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(50, 100)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(50, 200));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFrameHasSmallerHeightThanGenerated_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(50, 200)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(50, 100));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void runListeners_GetTexture_SameTexture() {

        /* Use this array to get around final restriction on lambdas
           because we have to create listener before texture is built. */
        final Object[] textureGetter = new Object[1];

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new TextureComponent<>() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                assertEquals(textureGetter[0], currentFrame.texture());
            }
        });
        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();
        textureGetter[0] = texture;

        texture.tick();
    }

    @Test
    public void runListeners_MarkForUpload_MarkedForUpload() {
        AtomicInteger timesUploaded = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.markNeedsUpload();
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                timesUploaded.incrementAndGet();
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        // We have to bind once first because the texture is always uploaded on the first bind
        texture.bind();

        texture.tick();
        texture.bind();

        assertEquals(2, timesUploaded.get());
    }

    @Test
    public void runListeners_SetImage_MarkedForUpload() {
        AtomicInteger timesUploaded = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.replaceWith(1);
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                timesUploaded.incrementAndGet();
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        // We have to bind once first because the texture is always uploaded on the first bind
        texture.bind();

        texture.tick();
        texture.bind();

        assertEquals(2, timesUploaded.get());
    }

    @Test
    public void runListeners_SetImage_ImageReplaced() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new TextureComponent<>() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.replaceWith(1);
            }

            @Override
            public void onClose(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                assertEquals(1, (int) currentFrame.index().orElseThrow());
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.close();
    }

    @Test
    public void register_FirstRegistration_RegisterFiredInOrder() {
        Integer[] expected = {1, 2, 3};
        testExpectedOrder((texture) -> texture.load(null), false, expected);
    }

    @Test
    public void register_SecondRegistration_RegisterFiredInOrder() {
        Integer[] expected = {1, 2, 3, 1, 2, 3};
        testExpectedOrder((texture) -> { texture.load(null);
            texture.load(null); }, false, expected);
    }

    @Test
    public void bind_FirstBind_UploadFiredInOrder() {
        Integer[] expected = {4, 5, 6};
        testExpectedOrder(EventDrivenTexture::bind, false, expected);
    }

    @Test
    public void bind_SecondBindUploadNotNeeded_UploadFiredInOrderOnce() {
        Integer[] expected = {4, 5, 6};
        testExpectedOrder((texture) -> {texture.bind(); texture.bind();}, false, expected);
    }

    @Test
    public void bind_SecondBindUploadNeeded_UploadFiredInOrder() {
        Integer[] expected = {4, 5, 6, 7, 8, 9, 4, 5, 6};
        testExpectedOrder((texture) -> {texture.bind(); texture.tick(); texture.bind();}, true, expected);
    }

    @Test
    public void upload_FirstUpload_UploadFiredInOrder() {
        Integer[] expected = {4, 5, 6};
        testExpectedOrder(EventDrivenTexture::upload, false, expected);
    }

    @Test
    public void upload_SecondUploadNotNeeded_UploadFiredInOrder() {

        // The upload method should run regardless of whether an upload is needed
        Integer[] expected = {4, 5, 6, 4, 5, 6};
        testExpectedOrder((texture) -> {texture.upload(); texture.upload();}, false, expected);

    }

    @Test
    public void tick_FirstTick_TickFiredInOrder() {
        Integer[] expected = {7, 8, 9};
        testExpectedOrder(EventDrivenTexture::tick, false, expected);
    }

    @Test
    public void tick_SecondTick_TickFiredInOrder() {
        Integer[] expected = {7, 8, 9, 7, 8, 9};
        testExpectedOrder((texture) -> { texture.tick(); texture.tick(); }, false, expected);
    }

    @Test
    public void close_FirstClose_CloseFiredInOrder() {
        Integer[] expected = {10, 11, 12};
        testExpectedOrder(EventDrivenTexture::close, false, expected);
    }

    @Test
    public void close_SecondClose_CloseFiredInOrder() {
        Integer[] expected = {10, 11, 12, 10, 11, 12};
        testExpectedOrder((texture) -> { texture.close(); texture.close(); }, false, expected);
    }

    @Test
    public void replace_Index0_Replaced() {
        AtomicInteger currentFrameIndex = new AtomicInteger(-1);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {

            @Override
            public void onRegistration(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.replaceWith(1);
            }

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.replaceWith(0);
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrameIndex.set(currentFrame.index().orElseThrow());
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.load(null);

        // We have to bind once first because the texture is always uploaded on the first bind
        texture.bind();
        assertEquals(1, currentFrameIndex.get());

        texture.tick();

        texture.bind();
        assertEquals(0, currentFrameIndex.get());
    }

    @Test
    public void replace_IndexNegative_FrameIndexOutOfBoundsException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.replaceWith(-1);
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(FrameView.FrameIndexOutOfBoundsException.class);
        texture.tick();
    }

    @Test
    public void replace_IndexTooLarge_FrameIndexOutOfBoundsException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.replaceWith(2);
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(FrameView.FrameIndexOutOfBoundsException.class);
        texture.tick();
    }

    @Test
    public void generate_NullTransform_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(null, List.of(new Point(0, 0)), List.of());
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NullPointerException.class);
        texture.tick();
    }

    @Test
    public void generate_NullArea_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> new Color(100, 100, 100, 100), null, List.of());
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NullPointerException.class);
        texture.tick();
    }

    @Test
    public void generate_TransformReturnsNull_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> null, List.of(new Point(0, 0)), List.of());
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(NullPointerException.class);
        texture.bind();
    }

    @Test
    public void generate_NullPointInArea_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        List<Point> area = new ArrayList<>();
        area.add(new Point(0, 0));
        area.add(null);
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> new Color(100, 100, 100, 100), area, List.of());
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(NullPointerException.class);
        texture.bind();
    }

    @Test
    public void generate_EmptyArea_FrameNotUpdated() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> new Color(100, 100, 100, 100), List.of(), List.of());
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(4, 4), new MockCloseableImageFrame(4, 4));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.bind();

        assertEquals(0, frames.get(0).uploadCount());
        assertEquals(0, frames.get(1).uploadCount());
        assertEquals(1, generatedFrame.uploadCount());

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        assertEquals(0, generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(0, generatedFrame.color(1, 0));
        assertEquals(0, generatedFrame.color(1, 1));
    }

    @Test
    public void generate_NullDependencies_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(50, 50)), null);
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NullPointerException.class);
        texture.tick();
    }

    @Test
    public void generate_NullDependencyPoint_NullPointerException() {
        List<Point> dependencies = new ArrayList<>();
        dependencies.add(null);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(50, 50)), dependencies);
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NullPointerException.class);
        texture.tick();
    }

    @Test
    public void generate_NegativeXDependencyPoint_ExceptionFromImage() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(50, 50)), List.of(new Point(-1, 50)));
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(MockCloseableImage.MockPixelOutOfBoundsException.class);
        texture.tick();
    }

    @Test
    public void generate_NegativeYDependencyPoint_ExceptionFromImage() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(50, 50)), List.of(new Point(50, -1)));
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(MockCloseableImage.MockPixelOutOfBoundsException.class);
        texture.tick();
    }

    @Test
    public void generate_TooLargeXDependencyPoint_ExceptionFromImage() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(50, 50)), List.of(new Point(100, 50)));
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(MockCloseableImage.MockPixelOutOfBoundsException.class);
        texture.tick();
    }

    @Test
    public void generate_TooLargeYDependencyPoint_ExceptionFromImage() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(50, 50)), List.of(new Point(50, 100)));
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(MockCloseableImage.MockPixelOutOfBoundsException.class);
        texture.tick();
    }

    @Test
    public void generate_NonRequestedDependencyPoint_NonDependencyRequestException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> depFunction.apply(new Point(25, 25)), List.of(new Point(50, 50)), List.of(new Point(25, 50)));
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(ColorTransform.NonDependencyRequestException.class);
        texture.tick();
    }

    @Test
    public void generate_RequestedDependencyPoint_NoException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((point, depFunction) -> depFunction.apply(new Point(25, 50)), List.of(new Point(50, 50)), List.of(new Point(25, 50)));
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame());
        frames.get(0).applyTransform((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(25, 50)), List.of());

        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();
        assertEquals(new Color(100, 100, 100, 100).combine(), frames.get(0).color(25, 50));
    }

    @Test
    public void generate_EmptyMipmap_NoException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y) -> new Color(100, 100, 100, 100),
                        List.of(new Point(1, 0), new Point(1, 1)),
                        List.of()
                );
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(2, 2), new MockCloseableImageFrame(2, 2));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(2, 2);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.bind();

        assertEquals(0, frames.get(0).uploadCount());
        assertEquals(0, frames.get(1).uploadCount());
        assertEquals(1, generatedFrame.uploadCount());

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        assertEquals(0, generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(new Color(100, 100, 100, 100).combine(), generatedFrame.color(1, 0));
        assertEquals(new Color(100, 100, 100, 100).combine(), generatedFrame.color(1, 1));
    }

    @Test
    public void generate_SingleTransformation_GeneratedFrameUpdatedOnUpload() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y) -> new Color(100, 100, 100, 100),
                        List.of(new Point(1, 0), new Point(1, 1)),
                        List.of()
                );
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(new Point(0, 0));
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(4, 4), new MockCloseableImageFrame(4, 4));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        assertEquals(0, generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(0, generatedFrame.color(1, 0));
        assertEquals(0, generatedFrame.color(1, 1));

        texture.bind();

        assertEquals(0, frames.get(0).uploadCount());
        assertEquals(0, frames.get(1).uploadCount());
        assertEquals(1, generatedFrame.uploadCount());

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        assertEquals(0, generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(new Color(100, 100, 100, 100).combine(), generatedFrame.color(1, 0));
        assertEquals(new Color(100, 100, 100, 100).combine(), generatedFrame.color(1, 1));
    }

    @Test
    public void generate_MultipleTransformations_GeneratedFrameUpdatedOnUpload() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y) -> new Color(100, 100, 100, 100),
                        List.of(new Point(1, 0), new Point(1, 1)),
                        List.of()
                );
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(new Point(0, 0));
            }
        });
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y) -> new Color(200, 200, 200, 200),
                        List.of(new Point(0, 0), new Point(1, 1)),
                        List.of()
                );
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(4, 4), new MockCloseableImageFrame(4, 4));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        assertEquals(0, generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(0, generatedFrame.color(1, 0));
        assertEquals(0, generatedFrame.color(1, 1));

        texture.bind();

        assertEquals(0, frames.get(0).uploadCount());
        assertEquals(0, frames.get(1).uploadCount());
        assertEquals(1, generatedFrame.uploadCount());

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        assertEquals(new Color(200, 200, 200, 200).combine(), generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(new Color(100, 100, 100, 100).combine(), generatedFrame.color(1, 0));
        assertEquals(new Color(200, 200, 200, 200).combine(), generatedFrame.color(1, 1));
    }

    @Test
    public void changedFrames_GenerateReplace_ReplacedFrameUploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y) -> new Color(100, 100, 100, 100),
                        List.of(new Point(1, 0), new Point(1, 1)),
                        List.of()
                );
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(new Point(0, 0));
            }
        });
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.replaceWith(1);
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(4, 4), new MockCloseableImageFrame(4, 4));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        texture.bind();

        assertEquals(0, frames.get(0).uploadCount());
        assertEquals(1, frames.get(1).uploadCount());
        assertEquals(0, generatedFrame.uploadCount());

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));
    }

    @Test
    public void changedFrames_GenerateReplaceGenerated_FrameGeneratedFromCorrectFrame() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y) -> new Color(100, 100, 100, 100),
                        List.of(new Point(1, 0), new Point(1, 1), new Point(0, 0)),
                        List.of()
                );
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(new Point(0, 0));
            }
        });
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.replaceWith(1);
            }
        });
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y) -> new Color(200, 200, 200, 200),
                        List.of(new Point(1, 0), new Point(1, 1)),
                        List.of()
                );
            }
        });


        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(4, 4), new MockCloseableImageFrame(4, 4));
        frames.get(0).applyTransform((point, depFunction) -> new Color(10, 10, 10, 10), List.of(new Point(2, 3)), List.of());
        frames.get(1).applyTransform((point, depFunction) -> new Color(50, 50, 50, 50), List.of(new Point(2, 3)), List.of());
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        assertEquals(0, generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(0, generatedFrame.color(1, 0));
        assertEquals(0, generatedFrame.color(1, 1));
        assertEquals(0, generatedFrame.color(2, 3));

        texture.bind();

        assertEquals(0, frames.get(0).uploadCount());
        assertEquals(0, frames.get(1).uploadCount());
        assertEquals(1, generatedFrame.uploadCount());

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        assertEquals(0, generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(new Color(200, 200, 200, 200).combine(), generatedFrame.color(1, 0));
        assertEquals(new Color(200, 200, 200, 200).combine(), generatedFrame.color(1, 1));
        assertEquals(new Color(50, 50, 50, 50).combine(), generatedFrame.color(2, 3));
    }

    @Test
    public void changedFrames_ReplaceGenerate_FrameGeneratedFromCorrectFrame() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(new Point(0, 0));
            }
        });
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.replaceWith(1);
            }
        });
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y) -> new Color(200, 200, 200, 200),
                        List.of(new Point(1, 0), new Point(1, 1)),
                        List.of()
                );
            }
        });


        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(4, 4), new MockCloseableImageFrame(4, 4));
        frames.get(0).applyTransform((point, depFunction) -> new Color(10, 10, 10, 10), List.of(new Point(2, 3)), List.of());
        frames.get(1).applyTransform((point, depFunction) -> new Color(50, 50, 50, 50), List.of(new Point(2, 3)), List.of());
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        assertEquals(0, generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(0, generatedFrame.color(1, 0));
        assertEquals(0, generatedFrame.color(1, 1));
        assertEquals(0, generatedFrame.color(2, 3));

        texture.bind();

        assertEquals(0, frames.get(0).uploadCount());
        assertEquals(0, frames.get(1).uploadCount());
        assertEquals(1, generatedFrame.uploadCount());

        assertEquals(0, frames.get(0).color(0, 0));
        assertEquals(0, frames.get(0).color(0, 1));
        assertEquals(0, frames.get(0).color(1, 0));
        assertEquals(0, frames.get(0).color(1, 1));

        assertEquals(0, frames.get(1).color(0, 0));
        assertEquals(0, frames.get(1).color(0, 1));
        assertEquals(0, frames.get(1).color(1, 0));
        assertEquals(0, frames.get(1).color(1, 1));

        assertEquals(0, generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(new Color(200, 200, 200, 200).combine(), generatedFrame.color(1, 0));
        assertEquals(new Color(200, 200, 200, 200).combine(), generatedFrame.color(1, 1));
        assertEquals(new Color(50, 50, 50, 50).combine(), generatedFrame.color(2, 3));
    }

    @Test
    public void replace_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view.replaceWith(1);
                currentFrame.uploadAt(new Point(0, 0));
            }
        });


        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(4, 4), new MockCloseableImageFrame(4, 4));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        texture.bind();
    }

    @Test
    public void generate_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view.generateWith((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(0, 0)), List.of());
                currentFrame.uploadAt(new Point(0, 0));
            }
        });


        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(4, 4), new MockCloseableImageFrame(4, 4));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        texture.bind();
    }

    @Test
    public void width_WhileValid_CorrectWidth() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                assertEquals(100, currentFrame.width());
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame());
        frames.get(0).applyTransform((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(20, 20)), List.of());
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();
    }

    @Test
    public void width_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view.width();
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        texture.bind();
    }

    @Test
    public void height_WhileValid_CorrectHeight() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                assertEquals(100, currentFrame.height());
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame());
        frames.get(0).applyTransform((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(20, 20)), List.of());
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();
    }

    @Test
    public void height_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view.height();
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        texture.bind();
    }

    @Test
    public void index_GeneratedFrame_EmptyIndex() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y) -> new Color(100, 100, 100, 100),
                        List.of(new Point(1, 0), new Point(1, 1)),
                        List.of()
                );
                assertEquals(Optional.empty(), currentFrame.index());
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame());
        frames.get(0).applyTransform((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(20, 20)), List.of());
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();
    }

    @Test
    public void index_PredefinedFrame_EmptyIndex() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                assertEquals(Optional.of(0), currentFrame.index());
                currentFrame.replaceWith(1);
                assertEquals(Optional.of(1), currentFrame.index());
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();
    }

    @Test
    public void index_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view.index();
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        texture.bind();
    }

    @Test
    public void texture_WhileValid_CorrectTexture() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        final EventDrivenTexture[] expectedTexture = new EventDrivenTexture[1];
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                assertEquals(expectedTexture[0], currentFrame.texture());
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame());
        frames.get(0).applyTransform((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(20, 20)), List.of());
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();
        expectedTexture[0] = texture;

        texture.tick();
    }

    @Test
    public void texture_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view.texture();
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        texture.bind();
    }

    @Test
    public void upload_NullPoint_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(null);
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame());
        frames.get(0).applyTransform((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(20, 20)), List.of());
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NullPointerException.class);
        texture.tick();
    }

    @Test
    public void upload_WhileValid_Uploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.uploadAt(new Point(2, 3));
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame());
        frames.get(0).applyTransform((point, depFunction) -> new Color(100, 100, 100, 100), List.of(new Point(20, 20)), List.of());
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();
        assertEquals(1, frames.get(0).uploadCount());
        assertEquals(new Point(2, 3), frames.get(0).lastUploadPoint());
    }

    @Test
    public void uploadAt_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view.uploadAt(new Point(0, 0));
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        texture.bind();
    }

    @Test
    public void lowerMipmapLevel_NegativeLevel_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.lowerMipmapLevel(-1);
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(IllegalArgumentException.class);
        texture.tick();
    }

    @Test
    public void lowerMipmapLevel_AboveCurrentLevel_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.lowerMipmapLevel(3);
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(IllegalArgumentException.class);
        texture.tick();
    }

    @Test
    public void lowerMipmapLevel_BelowCurrentLevel_MipmapLowered() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.lowerMipmapLevel(0);
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame());
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        assertEquals(2, frames.get(0).mipmapLevel());
        texture.tick();
        assertEquals(0, frames.get(0).mipmapLevel());
    }

    @Test
    public void lowerMipmapLevel_SameAsCurrentLevel_MipmapUnchanged() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                currentFrame.lowerMipmapLevel(2);
            }
        });

        List<MockCloseableImageFrame> frames = List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame());
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        assertEquals(2, frames.get(0).mipmapLevel());
        texture.tick();
        assertEquals(2, frames.get(0).mipmapLevel());
    }

    @Test
    public void lowerMipmapLevel_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                view.lowerMipmapLevel(0);
            }
        });

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), new MockCloseableImageFrame()));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        texture.bind();
    }

    private void testExpectedOrder(Consumer<EventDrivenTexture> action, boolean flagForUpload,
                                   Integer[] expected) {
        EventDrivenTexture.Builder texture = new EventDrivenTexture.Builder();
        texture.setPredefinedFrames(List.of(new MockCloseableImageFrame()));
        texture.setGeneratedFrame(new MockCloseableImageFrame());

        final int REG_ID_BASE = 1;
        final int UPLOAD_ID_BASE = 4;
        final int TICK_ID_BASE = 7;
        final int CLOSE_ID_BASE = 10;

        List<Integer> execOrder = new ArrayList<>();

        for (int index = 0; index < 3; index++) {
            int finalIndex = index;
            texture.add(new CoreTextureComponent() {
                @Override
                public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                    if (flagForUpload) currentFrame.markNeedsUpload();
                    execOrder.add(TICK_ID_BASE + finalIndex);
                }

                @Override
                public void onClose(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                    if (flagForUpload) currentFrame.markNeedsUpload();
                    execOrder.add(CLOSE_ID_BASE + finalIndex);
                }

                @Override
                public void onRegistration(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                    if (flagForUpload) currentFrame.markNeedsUpload();
                    execOrder.add(REG_ID_BASE + finalIndex);
                }

                @Override
                public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {
                    if (flagForUpload) currentFrame.markNeedsUpload();
                    execOrder.add(UPLOAD_ID_BASE + finalIndex);
                }
            });
        }

        action.accept(texture.build());
        assertArrayEquals(expected, execOrder.toArray(new Integer[expected.length]));
    }

}