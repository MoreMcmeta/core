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

import com.mojang.blaze3d.platform.NativeImage;
import io.github.soir20.moremcmeta.client.adapter.NativeImageAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link NativeImageAdapter >} as much as possible without
 * instantiating a {@link com.mojang.blaze3d.platform.NativeImage}, which
 * uses the render system and will throw errors in test code.
 * @author soir20
 */
public class NativeImageAdapterTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void constructFull_NullImage_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new NativeImageAdapter(null, 0, 0, 100, 100, 2,
                false, false, false, (new RGBAImage.VisibleArea.Builder()).build(),
                new NativeImageAdapter.ClosedStatus());
    }

    @Test
    public void constructReduced_NullImage_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new NativeImageAdapter(null, 0, new NativeImageAdapter.ClosedStatus());
    }

    @Test
    public void constructFull_NullStatus_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new NativeImageAdapter(new NativeImage(10, 10, true), 0, 0, 100, 100, 2,
                false, false, false, (new RGBAImage.VisibleArea.Builder()).build(),
                null);
    }

    @Test
    public void constructReduced_NullStatus_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new NativeImageAdapter(new NativeImage(10, 10, true), 0, null);
    }

}