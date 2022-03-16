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
import io.github.soir20.moremcmeta.impl.client.texture.SingleUploadComponent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Tests the {@link SingleUploadComponent}. Coverage here is somewhat low
 * because of the render system call inside the component, which can't be
 * tested. It's a small class, so the single call has a large impact on
 * test coverage.
 * @author soir20
 */
public class SingleUploadComponentTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullPreparer_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new SingleUploadComponent(null);
    }

    @Test
    public void upload_FirstUpload_FrameUploadedAtOrigin() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SingleUploadComponent((id, mipmap, width, height) -> {})).getListeners());

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.upload();

        assertEquals(1, frame.getUploadCount());
        assertEquals(new Point(0, 0), ((MockCloseableImage) frame.getImage(0)).getLastUploadPoint());
    }

    @Test
    public void upload_SecondUpload_FrameUploadedAtOriginAgain() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SingleUploadComponent((id, mipmap, width, height) -> {})).getListeners());

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.upload();
        texture.upload();

        assertEquals(2, frame.getUploadCount());
        assertEquals(new Point(0, 0), ((MockCloseableImage) frame.getImage(0)).getLastUploadPoint());
    }

    @Test
    public void upload_OriginalImage_MipmapLoweredTo0() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SingleUploadComponent((id, mipmap, width, height) -> {})).getListeners());

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        assertEquals(2, frame.getMipmapLevel());
        texture.upload();
        assertEquals(0, frame.getMipmapLevel());
    }

    @Test
    public void upload_SecondImage_MipmapLoweredTo0() {
        MockCloseableImageFrame frame2 = new MockCloseableImageFrame();
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> Stream.of(new TextureListener<>(TextureListener.Type.TICK, (state) -> state.replaceImage(frame2))));
        builder.add(() -> (new SingleUploadComponent((id, mipmap, width, height) -> {})).getListeners());

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.upload();
        texture.tick();

        assertEquals(2, frame2.getMipmapLevel());
        texture.upload();
        assertEquals(0, frame2.getMipmapLevel());
    }

}