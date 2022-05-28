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
 * Tests the {@link SingleUploadComponent}. Coverage here is somewhat low
 * because of the render system call inside the component, which can't be
 * tested. It's a small class, so the single call has a large impact on
 * test coverage.
 * @author soir20
 */
@SuppressWarnings("resource")
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
        builder.add(new SingleUploadComponent((id, mipmap, width, height) -> {}));

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.upload();

        assertEquals(1, frame.uploadCount());
        assertEquals(new Point(0, 0), frame.mipmap(0).lastUploadPoint());
    }

    @Test
    public void upload_SecondUpload_FrameUploadedAtOriginAgain() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(new SingleUploadComponent((id, mipmap, width, height) -> {}));

        MockCloseableImageFrame frame = new MockCloseableImageFrame();
        builder.setPredefinedFrames(List.of(frame));
        builder.setGeneratedFrame(new MockCloseableImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.upload();
        texture.upload();

        assertEquals(2, frame.uploadCount());
        assertEquals(new Point(0, 0), frame.mipmap(0).lastUploadPoint());
    }

}