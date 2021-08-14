/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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

import io.github.soir20.moremcmeta.client.resource.MockResourceManager;
import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

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
    private static final ResourceManager MOCK_RESOURCE_MANAGER = new MockResourceManager(Collections.emptyList(),
            Collections.emptyList(), false);

    @Test
    public void construct_NullPreparer_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new SingleUploadComponent(null);
    }

    @Test
    public void register_FirstRegistration_TexturePrepared() {
        boolean[] wasPrepared = {false};

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (
                new SingleUploadComponent((id, mipmap, width, height) -> wasPrepared[0] = true)
        ).getListeners());

        MockRGBAImageFrame frame = new MockRGBAImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.load(MOCK_RESOURCE_MANAGER);

        assertTrue(wasPrepared[0]);
    }

    @Test
    public void upload_FirstUpload_FrameUploadedAtOrigin() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SingleUploadComponent((id, mipmap, width, height) -> {})).getListeners());

        MockRGBAImageFrame frame = new MockRGBAImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.upload();

        assertEquals(1, frame.getUploadCount());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(2)).getLastUploadPoint());
    }

    @Test
    public void upload_SecondUpload_FrameUploadedAtOriginAgain() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SingleUploadComponent((id, mipmap, width, height) -> {})).getListeners());

        MockRGBAImageFrame frame = new MockRGBAImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.upload();
        texture.upload();

        assertEquals(2, frame.getUploadCount());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(2)).getLastUploadPoint());
    }

}