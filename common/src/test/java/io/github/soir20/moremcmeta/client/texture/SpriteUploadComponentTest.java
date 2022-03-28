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

package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.api.client.texture.TextureListener;
import io.github.soir20.moremcmeta.api.math.Point;
import io.github.soir20.moremcmeta.impl.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.impl.client.texture.SpriteUploadComponent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

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
        builder.add(() -> (new SpriteUploadComponent(new MockSprite(new Point(2, 3)))).getListeners());

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.upload();

        assertEquals(1, frame.getUploadCount());
        assertEquals(new Point(2, 3), ((MockCloseableImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(1, 1), ((MockCloseableImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockCloseableImage) frame.getImage(2)).getLastUploadPoint());
    }

    @Test
    public void upload_SecondUpload_FrameUploadedAtMipmappedPoints() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SpriteUploadComponent(new MockSprite(new Point(2, 3)))).getListeners());

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.upload();
        texture.upload();

        assertEquals(2, frame.getUploadCount());
        assertEquals(new Point(2, 3), ((MockCloseableImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(1, 1), ((MockCloseableImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockCloseableImage) frame.getImage(2)).getLastUploadPoint());
    }

    @Test
    public void tick_FirstTick_BoundAndUploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        MockSprite sprite = new MockSprite(new Point(2, 3));
        builder.add(() -> (new SpriteUploadComponent(sprite)).getListeners());

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();

        assertEquals(1, sprite.getBindCount());
        assertEquals(1, frame.getUploadCount());
        assertEquals(new Point(2, 3), ((MockCloseableImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(1, 1), ((MockCloseableImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockCloseableImage) frame.getImage(2)).getLastUploadPoint());
    }

    @Test
    public void tick_SecondTick_BoundAndUploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        MockSprite sprite = new MockSprite(new Point(2, 3));
        builder.add(() -> (new SpriteUploadComponent(sprite)).getListeners());

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.tick();

        assertEquals(2, sprite.getBindCount());
        assertEquals(2, frame.getUploadCount());
        assertEquals(new Point(2, 3), ((MockCloseableImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(1, 1), ((MockCloseableImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockCloseableImage) frame.getImage(2)).getLastUploadPoint());
    }

    @Test
    public void upload_OriginalImage_MipmapLoweredToSprite() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        MockSprite sprite = new MockSprite(1);
        builder.add(() -> (new SpriteUploadComponent(sprite)).getListeners());

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        assertEquals(2, frame.getMipmapLevel());
        texture.upload();
        assertEquals(1, frame.getMipmapLevel());
    }

    @Test
    public void upload_SecondImage_MipmapLoweredToSprite() {
        MockCloseableImageFrame frame2 = new MockCloseableImageFrame();
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        MockSprite sprite = new MockSprite(1);
        builder.add(() -> Stream.of(new TextureListener<>(TextureListener.Type.TICK, (state) -> state.replaceWith(1))));
        builder.add(() -> (new SpriteUploadComponent(sprite)).getListeners());

        builder.setPredefinedFrames(List.of(new MockCloseableImageFrame(), frame2));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.upload();
        texture.tick();

        // The frame automatically uploads on tick
        assertEquals(1, frame2.getMipmapLevel());

    }

}