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

import io.github.moremcmeta.moremcmeta.api.client.texture.Color;
import io.github.moremcmeta.moremcmeta.api.client.texture.PixelOutOfBoundsException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link PredefinedFrameView}.
 * @author soir20
 */
public final class PredefinedFrameViewTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NulLFrame_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new PredefinedFrameView(null);
    }

    @Test
    public void width_HasWidth_GetsWidth() {
        PredefinedFrameView view = new PredefinedFrameView(new MockCloseableImageFrame(100, 200, 1));
        assertEquals(100, view.width());
    }

    @Test
    public void height_HasHeight_GetsHeight() {
        PredefinedFrameView view = new PredefinedFrameView(new MockCloseableImageFrame(100, 200, 1));
        assertEquals(200, view.height());
    }

    @Test
    public void color_ColorOutOfBounds_PixelOutOfBoundsException() {
        PredefinedFrameView view = new PredefinedFrameView(new MockCloseableImageFrame(100, 200, 1));
        expectedException.expect(PixelOutOfBoundsException.class);
        view.color(100, 30);
    }

    @Test
    public void color_ColorInBounds_GetsColor() {
        MockCloseableImageFrame frame = new MockCloseableImageFrame(100, 200, 1);
        frame.mipmap(0).setColor(25, 30, Color.pack(100, 100, 100, 100));

        PredefinedFrameView view = new PredefinedFrameView(frame);
        assertEquals(Color.pack(100, 100, 100, 100), view.color(25, 30));
    }

}