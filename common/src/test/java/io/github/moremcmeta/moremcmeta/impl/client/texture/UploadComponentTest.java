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
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.PersistentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import io.github.moremcmeta.moremcmeta.api.math.Area;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link UploadComponent}.
 * @author soir20
 */
public final class UploadComponentTest {
    private static final ResourceLocation DUMMY_LOCATION = new ResourceLocation("dummy.png");
    private static final TexturePreparer DUMMY_PREPARER =  (id, mipmap, width, height) -> {};
    private static final BaseCollection DUMMY_BASE_COLLECTION = BaseCollection.find(
            new SpriteFinder((atlasLocation) -> (spriteLocation) -> Optional.of(
                    new MockSprite(new ResourceLocation("dummy"), Point.pack(0, 0), 1)
            )),
            DUMMY_LOCATION
    );

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullPreparer_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new UploadComponent(null, DUMMY_BASE_COLLECTION);
    }

    @Test
    public void construct_NullBaseCollection_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new UploadComponent(DUMMY_PREPARER, null);
    }

    @Test
    public void upload_FirstUpload_FrameUploadedAtMipmappedPoints() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new UploadComponent(DUMMY_PREPARER, DUMMY_BASE_COLLECTION));

        MockCloseableImageFrame frame = new MockCloseableImageFrame(1);
        builder.setPredefinedFrames(ImmutableList.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.upload(DUMMY_LOCATION);

        assertEquals(1, frame.uploadCount());
    }

    @Test
    public void upload_SecondUpload_FrameUploadedAtMipmappedPointsOnce() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new UploadComponent(DUMMY_PREPARER, DUMMY_BASE_COLLECTION));

        MockCloseableImageFrame frame = new MockCloseableImageFrame(1);
        builder.setPredefinedFrames(ImmutableList.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.upload(DUMMY_LOCATION);
        texture.upload(DUMMY_LOCATION);

        assertEquals(1, frame.uploadCount());
    }

    @Test
    public void tick_FirstTick_NotBoundAndUploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new UploadComponent(DUMMY_PREPARER, DUMMY_BASE_COLLECTION));

        MockCloseableImageFrame frame = new MockCloseableImageFrame(1);
        builder.setPredefinedFrames(ImmutableList.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame(1));
        EventDrivenTexture texture = builder.build();

        texture.tick();

        assertEquals(0, frame.uploadCount());
    }

    @Test
    public void tick_SecondTick_NotBoundAndUploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        AtomicInteger ticks = new AtomicInteger();
        builder.add(new TextureComponent<EventDrivenTexture.TextureAndFrameView>() {
            @Override
            public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame,
                               FrameGroup<? extends PersistentFrameView> predefinedFrames) {

                // Modify the current frame to ensure it is uploaded
                currentFrame.generateWith(
                        (x, y, dependencies) -> predefinedFrames.frame(ticks.incrementAndGet() % predefinedFrames.frames())
                                .color(x, y),
                        Area.of(Point.pack(0, 0))
                );

            }
        });
        builder.add(new UploadComponent(DUMMY_PREPARER, DUMMY_BASE_COLLECTION));

        int layers = 2;
        MockCloseableImageFrame frame = new MockCloseableImageFrame(layers);
        List<MockCloseableImageFrame> predefinedFrames = ImmutableList.of(frame, frame);
        builder.setPredefinedFrames(predefinedFrames);
        builder.setGeneratedFrame(new MockCloseableImageFrame(layers));
        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.tick();

        assertEquals(0, frame.uploadCount());
    }

    @Test
    public void register_AllImages_MipmapLoweredToSprite() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new UploadComponent(DUMMY_PREPARER, DUMMY_BASE_COLLECTION));

        MockCloseableImageFrame frame1 = new MockCloseableImageFrame(1);
        MockCloseableImageFrame frame2 = new MockCloseableImageFrame(1);
        MockCloseableImageFrame frame3 = new MockCloseableImageFrame(1);

        builder.setPredefinedFrames(ImmutableList.of(frame1, frame2));
        builder.setGeneratedFrame(frame3);
        EventDrivenTexture texture = builder.build();

        assertEquals(2, frame1.mipmapLevel());
        assertEquals(2, frame2.mipmapLevel());
        assertEquals(2, frame3.mipmapLevel());
        texture.load(null);
        assertEquals(1, frame1.mipmapLevel());
        assertEquals(1, frame2.mipmapLevel());
        assertEquals(1, frame3.mipmapLevel());
    }

}