/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import com.google.common.collect.ImmutableList;
import io.github.moremcmeta.moremcmeta.api.client.texture.Color;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.IllegalFrameReferenceException;
import io.github.moremcmeta.moremcmeta.api.client.texture.NegativeUploadPointException;
import io.github.moremcmeta.moremcmeta.api.client.texture.PersistentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.PixelOutOfBoundsException;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import io.github.moremcmeta.moremcmeta.api.math.Area;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link EventDrivenTexture}.
 * @author soir20
 */
public final class EventDrivenTextureTest {
    private static final ResourceLocation DUMMY_BASE_LOCATION = new ResourceLocation("dummy.png");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void build_EmptyPredefinedFrames_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(IllegalArgumentException.class);
        builder.setPredefinedFrames(ImmutableList.of());
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
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_SetPredefinedFramesTwice_HasLastList() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1)));
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.add(new CoreTextureComponent() {
            @Override
            public void onRegistration(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
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
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1)));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_SetGeneratedFrameTwice_HasLastFrame() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(1);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        builder.setGeneratedFrame(generatedFrame);

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1)));
        builder.add(new CoreTextureComponent() {
            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                currentFrame.generateWith((x, y, depFunction) -> 0, Area.of());
                currentFrame.upload(0, 0, 0);
            }
        });

        EventDrivenTexture texture = builder.build();
        texture.upload(new ResourceLocation("dummy.png"));

        assertEquals(1, generatedFrame.uploadCount());
    }

    @Test
    public void build_NullComponent_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));

        expectedException.expect(NullPointerException.class);
        builder.add(null);
    }

    @Test
    public void build_NoListeners_NoException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        builder.build();
    }

    @Test
    public void build_PredefinedFramesWithDifferentMipLevels_IllegalArgException() {
        MockCloseableImageFrame lowerMipmapFrame = new MockCloseableImageFrame(
                ImmutableList.of(new MockCloseableImage()),
                1
        );

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(IllegalArgumentException.class);
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), lowerMipmapFrame));
    }

    @Test
    public void build_PredefinedFramesWithDifferentWidths_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(IllegalArgumentException.class);
        builder.setPredefinedFrames(ImmutableList.of(
                new MockCloseableImageFrame(100, 200, 1),
                new MockCloseableImageFrame(50, 200, 1)
        ));
    }

    @Test
    public void build_PredefinedFramesWithDifferentHeights_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(IllegalArgumentException.class);
        builder.setPredefinedFrames(ImmutableList.of(
                new MockCloseableImageFrame(50, 200, 1),
                new MockCloseableImageFrame(50, 100, 1)
        ));
    }

    @Test
    public void build_PredefinedFramesWithDifferentLayers_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(IllegalArgumentException.class);
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(2)));
    }

    @Test
    public void build_PredefinedFrameHasLowerMipLevelThanGenerated_IllegalStateException() {
        MockCloseableImageFrame lowerMipmapFrame = new MockCloseableImageFrame(
                ImmutableList.of(new MockCloseableImage()),
                1
        );

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(lowerMipmapFrame));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFrameHasHigherMipLevelThanGenerated_IllegalStateException() {
        MockCloseableImageFrame lowerMipmapFrame = new MockCloseableImageFrame(
                ImmutableList.of(new MockCloseableImage()),
                1
        );

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(lowerMipmapFrame);

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFrameHasLargerWidthThanGenerated_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(100, 200, 1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(50, 200, 1));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFrameHasSmallerWidthThanGenerated_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(50, 200, 1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(100, 200, 1));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFrameHasLargerHeightThanGenerated_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(50, 100, 1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(50, 200, 1));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFrameHasSmallerHeightThanGenerated_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(50, 200, 1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(50, 100, 1));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFramesHaveLessLayersThanComponents_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(50, 100, 1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(50, 100, 3));
        builder.add(new CoreTextureComponent() {});
        builder.add(new CoreTextureComponent() {});
        builder.add(new CoreTextureComponent() {});

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_PredefinedFramesHaveMoreLayersThanComponents_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(50, 100, 4)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(50, 100, 3));
        builder.add(new CoreTextureComponent() {});
        builder.add(new CoreTextureComponent() {});
        builder.add(new CoreTextureComponent() {});

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_GeneratedFramesHaveLessLayersThanComponents_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(50, 100, 3)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(50, 100, 1));
        builder.add(new CoreTextureComponent() {});
        builder.add(new CoreTextureComponent() {});
        builder.add(new CoreTextureComponent() {});

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_GeneratedFramesHaveMoreLayersThanComponents_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(50, 100, 3)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(50, 100, 4));
        builder.add(new CoreTextureComponent() {});
        builder.add(new CoreTextureComponent() {});
        builder.add(new CoreTextureComponent() {});

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
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                assertEquals(textureGetter[0], currentFrame.texture());
            }
        });
        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
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
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.markNeedsUpload();
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                timesUploaded.incrementAndGet();
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        // We have to bind once first because the texture is always uploaded on the first bind
        texture.upload(DUMMY_BASE_LOCATION);

        texture.tick();
        texture.upload(DUMMY_BASE_LOCATION);

        assertEquals(2, timesUploaded.get());
    }

    @Test
    public void runListeners_SetImage_MarkedForUpload() {
        AtomicInteger timesUploaded = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y, dependencies) -> predefinedFrames.frame(1)
                                .color(x, y),
                        Area.of(Point.pack(0, 0))
                );
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                timesUploaded.incrementAndGet();
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        // We have to bind once first because the texture is always uploaded on the first bind
        texture.upload(DUMMY_BASE_LOCATION);

        texture.tick();
        texture.upload(DUMMY_BASE_LOCATION);

        assertEquals(2, timesUploaded.get());
    }

    @Test
    public void register_FirstRegistration_RegisterFiredInOrder() {
        Integer[] expected = {1, 2, 3};
        testExpectedOrder((texture) -> texture.load(null), false, expected);
    }

    @Test
    public void register_SecondRegistration_RegisterFiredInOrderTwice() {
        Integer[] expected = {1, 2, 3, 1, 2, 3};
        testExpectedOrder((texture) -> { texture.load(null);
            texture.load(null); }, false, expected);
    }

    @Test
    public void bind_FirstBind_NoneFired() {
        Integer[] expected = {};
        testExpectedOrder(EventDrivenTexture::bind, false, expected);
    }

    @Test
    public void bind_SecondUploadNotNeeded_UploadFiredInOrderOnce() {
        Integer[] expected = {13, 14, 15, 4, 5, 6, 13, 14, 15};
        testExpectedOrder((texture) -> {texture.upload(DUMMY_BASE_LOCATION); texture.upload(DUMMY_BASE_LOCATION);}, false, expected);
    }

    @Test
    public void bind_SecondUploadNeeded_UploadFiredInOrder() {
        Integer[] expected = {13, 14, 15, 4, 5, 6, 7, 8, 9, 13, 14, 15, 4, 5, 6};
        testExpectedOrder((texture) -> {texture.upload(DUMMY_BASE_LOCATION); texture.tick(); texture.upload(DUMMY_BASE_LOCATION);}, true, expected);
    }

    @Test
    public void upload_FirstUpload_UploadFiredInOrder() {
        Integer[] expected = {13, 14, 15, 4, 5, 6};
        testExpectedOrder((texture) -> texture.upload(new ResourceLocation("dummy.png")), false, expected);
    }

    @Test
    public void upload_SecondUploadNotNeeded_FirstUploadOnly() {
        Integer[] expected = {13, 14, 15, 4, 5, 6, 13, 14, 15};
        ResourceLocation dummyBase = new ResourceLocation("dummy.png");
        testExpectedOrder((texture) -> {texture.upload(dummyBase); texture.upload(dummyBase);}, false, expected);

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
    public void generate_NullTransform_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(null, Area.of(Point.pack(0, 0)));
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NullPointerException.class);
        texture.tick();
    }

    @Test
    public void generate_NullArea_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((x, y, depFunction) -> Color.pack(100, 100, 100, 100), null);
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NullPointerException.class);
        texture.tick();
    }

    @Test
    public void generate_EmptyArea_FrameNotUpdated() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of());
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                currentFrame.upload(0, 0, 0);
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(4, 4, 1), new MockCloseableImageFrame(4, 4, 1));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4, 1);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.upload(DUMMY_BASE_LOCATION);

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
    public void generate_NegativeXDependencyPoint_ExceptionFromImage() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((x, y, depFunction) -> depFunction.color(-1, 50), Area.of(Point.pack(50, 50)));
                currentFrame.upload(0, 0, 0);
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(PixelOutOfBoundsException.class);
        texture.tick();
    }

    @Test
    public void generate_NegativeYDependencyPoint_ExceptionFromImage() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((x, y, depFunction) -> depFunction.color(50, -1), Area.of(Point.pack(50, 50)));
                currentFrame.upload(0, 0, 0);
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(PixelOutOfBoundsException.class);
        texture.tick();
    }

    @Test
    public void generate_TooLargeXDependencyPoint_ExceptionFromImage() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((x, y, depFunction) -> depFunction.color(100, 50), Area.of(Point.pack(50, 50)));
                currentFrame.upload(0, 0, 0);
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(PixelOutOfBoundsException.class);
        texture.tick();
    }

    @Test
    public void generate_TooLargeYDependencyPoint_ExceptionFromImage() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((x, y, depFunction) -> depFunction.color(50, 100), Area.of(Point.pack(50, 50)));
                currentFrame.upload(0, 0, 0);
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(PixelOutOfBoundsException.class);
        texture.tick();
    }

    @Test
    public void generate_ValidDependencyPoint_NoException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith((x, y, depFunction) -> depFunction.color(25, 50), Area.of(Point.pack(50, 50)));
                currentFrame.upload(0, 0, 0);
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1));
        frames.get(0).applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(25, 50)), 0);

        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.tick();
        assertEquals(Color.pack(100, 100, 100, 100), frames.get(0).color(25, 50));
    }

    @Test
    public void generate_EmptyMipmap_NoException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y, depFunction) -> Color.pack(100, 100, 100, 100),
                        Area.of(Point.pack(1, 0), Point.pack(1, 1))
                );
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                currentFrame.upload(0, 0, 0);
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(2, 2, 1), new MockCloseableImageFrame(2, 2, 1));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(2, 2, 1);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.upload(DUMMY_BASE_LOCATION);

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
        assertEquals(Color.pack(100, 100, 100, 100), generatedFrame.color(1, 0));
        assertEquals(Color.pack(100, 100, 100, 100), generatedFrame.color(1, 1));
    }

    @Test
    public void generate_SingleTransformation_GeneratedFrameUpdatedImmediately() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y, depFunction) -> Color.pack(100, 100, 100, 100),
                        Area.of(Point.pack(1, 0), Point.pack(1, 1))
                );
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                currentFrame.upload(0, 0, 0);
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(4, 4, 1), new MockCloseableImageFrame(4, 4, 1));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4, 1);
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
        assertEquals(Color.pack(100, 100, 100, 100), generatedFrame.color(1, 0));
        assertEquals(Color.pack(100, 100, 100, 100), generatedFrame.color(1, 1));
    }

    @Test
    public void generate_MultipleTransformations_GeneratedFrameUpdatedImmediately() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y, depFunction) -> Color.pack(100, 100, 100, 100),
                        Area.of(Point.pack(1, 0), Point.pack(1, 1))
                );
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                currentFrame.upload(0, 0, 0);
            }
        });
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.generateWith(
                        (x, y, depFunction) -> Color.pack(200, 200, 200, 200),
                        Area.of(Point.pack(0, 0), Point.pack(1, 1))
                );
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(4, 4, 2), new MockCloseableImageFrame(4, 4, 2));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4, 2);
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

        assertEquals(Color.pack(200, 200, 200, 200), generatedFrame.color(0, 0));
        assertEquals(0, generatedFrame.color(0, 1));
        assertEquals(Color.pack(100, 100, 100, 100), generatedFrame.color(1, 0));
        assertEquals(Color.pack(200, 200, 200, 200), generatedFrame.color(1, 1));
    }

    @Test
    public void generate_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                view.generateWith((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(0, 0)));
                currentFrame.upload(0, 0, 0);
            }
        });


        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(4, 4, 1), new MockCloseableImageFrame(4, 4, 1));
        builder.setPredefinedFrames(frames);

        MockCloseableImageFrame generatedFrame = new MockCloseableImageFrame(4, 4, 1);
        builder.setGeneratedFrame(generatedFrame);

        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(IllegalFrameReferenceException.class);
        texture.upload(DUMMY_BASE_LOCATION);
    }

    @Test
    public void width_WhileValid_CorrectWidth() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                assertEquals(100, currentFrame.width());
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1));
        frames.get(0).applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(20, 20)), 0);
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.tick();
    }

    @Test
    public void width_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                view.width();
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(IllegalFrameReferenceException.class);
        texture.upload(DUMMY_BASE_LOCATION);
    }

    @Test
    public void height_WhileValid_CorrectHeight() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                assertEquals(100, currentFrame.height());
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1));
        frames.get(0).applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(20, 20)), 0);
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.tick();
    }

    @Test
    public void height_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                view.height();
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(IllegalFrameReferenceException.class);
        texture.upload(DUMMY_BASE_LOCATION);
    }

    @Test
    public void texture_WhileValid_CorrectTexture() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        final EventDrivenTexture[] expectedTexture = new EventDrivenTexture[1];
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                assertEquals(expectedTexture[0], currentFrame.texture());
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1));
        frames.get(0).applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(20, 20)), 0);
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
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
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                view.texture();
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(IllegalFrameReferenceException.class);
        texture.upload(DUMMY_BASE_LOCATION);
    }

    @Test
    public void upload_NullBase_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1));
        frames.get(0).applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(20, 20)), 0);
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NullPointerException.class);
        texture.upload(null);
    }

    @Test
    public void upload_NegativeXUploadPoint_NegativeUploadPointException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.upload(-1, 0, 0);
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1));
        frames.get(0).applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(20, 20)), 0);
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NegativeUploadPointException.class);
        texture.tick();
    }

    @Test
    public void upload_NegativeYUploadPoint_NegativeUploadPointException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.upload(0, -1, 0);
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1));
        frames.get(0).applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(20, 20)), 0);
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NegativeUploadPointException.class);
        texture.tick();
    }

    @Test
    public void upload_WhileValid_Uploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.upload(2, 3, 0);
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1));
        frames.get(0).applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(20, 20)), 0);
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.tick();
        assertEquals(1, frames.get(0).uploadCount());
        assertEquals((Long) Point.pack(2, 3), frames.get(0).lastUploadPoint());
    }

    @Test
    public void upload_AfterInvalidated_IllegalFrameReferenceException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            private EventDrivenTexture.TextureAndFrameView view;

            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                view.upload(0, 0, 0);
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(IllegalFrameReferenceException.class);
        texture.upload(DUMMY_BASE_LOCATION);
    }

    @Test
    public void lowerMipmapLevel_NegativeLevel_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.lowerMipmapLevel(-1);
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(IllegalArgumentException.class);
        texture.tick();
    }

    @Test
    public void lowerMipmapLevel_AboveCurrentLevel_IllegalArgException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.lowerMipmapLevel(3);
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        expectedException.expect(IllegalArgumentException.class);
        texture.tick();
    }

    @Test
    public void lowerMipmapLevel_BelowCurrentLevel_MipmapLowered() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new CoreTextureComponent() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.lowerMipmapLevel(0);
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1));
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
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
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                currentFrame.lowerMipmapLevel(2);
            }
        });

        List<MockCloseableImageFrame> frames = ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1));
        builder.setPredefinedFrames(frames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
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
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                view = currentFrame;
            }

            @Override
            public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                view.lowerMipmapLevel(0);
            }
        });

        builder.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.tick();

        expectedException.expect(IllegalFrameReferenceException.class);
        texture.upload(DUMMY_BASE_LOCATION);
    }

    private void testExpectedOrder(Consumer<EventDrivenTexture> action, boolean flagForUpload,
                                   Integer[] expected) {
        EventDrivenTexture.Builder texture = new EventDrivenTexture.Builder();
        texture.setPredefinedFrames(ImmutableList.of(new MockCloseableImageFrame(3)));
        texture.setGeneratedFrame(new MockCloseableImageFrame(3));

        final int REG_ID_BASE = 1;
        final int UPLOAD_ID_BASE = 4;
        final int TICK_ID_BASE = 7;
        final int CLOSE_ID_BASE = 10;
        final int TICK_2_ID_BASE = 13;

        List<Integer> execOrder = new ArrayList<>();

        for (int index = 0; index < 3; index++) {
            int finalIndex = index;
            texture.add(new CoreTextureComponent() {
                @Override
                public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                    if (flagForUpload) currentFrame.markNeedsUpload();
                    execOrder.add(TICK_ID_BASE + finalIndex);
                }

                @Override
                public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames, int ticks) {
                    if (flagForUpload) currentFrame.markNeedsUpload();
                    execOrder.add(TICK_2_ID_BASE + finalIndex);
                }

                @Override
                public void onClose(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                    if (flagForUpload) currentFrame.markNeedsUpload();
                    execOrder.add(CLOSE_ID_BASE + finalIndex);
                }

                @Override
                public void onRegistration(EventDrivenTexture.TextureAndFrameView currentFrame, FrameGroup<? extends PersistentFrameView> predefinedFrames) {
                    if (flagForUpload) currentFrame.markNeedsUpload();
                    execOrder.add(REG_ID_BASE + finalIndex);
                }

                @Override
                public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
                    if (flagForUpload) currentFrame.markNeedsUpload();
                    execOrder.add(UPLOAD_ID_BASE + finalIndex);
                }
            });
        }

        action.accept(texture.build());
        assertArrayEquals(expected, execOrder.toArray(new Integer[expected.length]));
    }

}