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

import io.github.soir20.moremcmeta.api.math.Point;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link SpriteUploadComponent}.
 * @author soir20
 */
public class SpriteUploadComponentTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullSprite_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new SpriteUploadComponent(null);
    }

    @Test
    public void upload_FirstUpload_FrameUploadedAtMipmappedPoints() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new SpriteUploadComponent(new MockSprite(new Point(2, 3))));

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.upload();

        assertEquals(1, frame.uploadCount());
        assertEquals(new Point(2, 3), frame.mipmap(0).lastUploadPoint());
        assertEquals(new Point(1, 1), frame.mipmap(1).lastUploadPoint());
        assertEquals(new Point(0, 0), frame.mipmap(2).lastUploadPoint());
    }

    @Test
    public void upload_SecondUpload_FrameUploadedAtMipmappedPoints() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new SpriteUploadComponent(new MockSprite(new Point(2, 3))));

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.upload();
        texture.upload();

        assertEquals(2, frame.uploadCount());
        assertEquals(new Point(2, 3), frame.mipmap(0).lastUploadPoint());
        assertEquals(new Point(1, 1), frame.mipmap(1).lastUploadPoint());
        assertEquals(new Point(0, 0), frame.mipmap(2).lastUploadPoint());
    }

    @Test
    public void tick_FirstTick_BoundAndUploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        MockSprite sprite = new MockSprite(new Point(2, 3));
        builder.add(new SpriteUploadComponent(sprite));

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();

        assertEquals(1, sprite.bindCount());
        assertEquals(1, frame.uploadCount());
        assertEquals(new Point(2, 3), frame.mipmap(0).lastUploadPoint());
        assertEquals(new Point(1, 1), frame.mipmap(1).lastUploadPoint());
        assertEquals(new Point(0, 0), frame.mipmap(2).lastUploadPoint());
    }

    @Test
    public void tick_SecondTick_BoundAndUploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        MockSprite sprite = new MockSprite(new Point(2, 3));
        builder.add(new SpriteUploadComponent(sprite));

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.tick();

        assertEquals(2, sprite.bindCount());
        assertEquals(2, frame.uploadCount());
        assertEquals(new Point(2, 3), frame.mipmap(0).lastUploadPoint());
        assertEquals(new Point(1, 1), frame.mipmap(1).lastUploadPoint());
        assertEquals(new Point(0, 0), frame.mipmap(2).lastUploadPoint());
    }

    @Test
    public void register_AllImages_MipmapLoweredToSprite() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        MockSprite sprite = new MockSprite(1);
        builder.add(new SpriteUploadComponent(sprite));

        MockCloseableImageFrame frame1 = new MockCloseableImageFrame();
        MockCloseableImageFrame frame2 = new MockCloseableImageFrame();
        MockCloseableImageFrame frame3 = new MockCloseableImageFrame();

        builder.setPredefinedFrames(List.of(frame1, frame2));
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