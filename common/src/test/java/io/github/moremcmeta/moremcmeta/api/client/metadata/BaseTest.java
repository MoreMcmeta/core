/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import io.github.moremcmeta.moremcmeta.api.client.texture.NegativeUploadPointException;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link Base}.
 * @author soir20
 */
public class BaseTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullLocation_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new Base(null, Point.pack(0, 0));
    }

    @Test
    public void construct_NegativeUploadX_NegativeUploadPointException() {
        expectedException.expect(NegativeUploadPointException.class);
        new Base(new ResourceLocation("dummy.png"), Point.pack(-1, 0));
    }

    @Test
    public void construct_NegativeUploadY_NegativeUploadPointException() {
        expectedException.expect(NegativeUploadPointException.class);
        new Base(new ResourceLocation("dummy.png"), Point.pack(0, -1));
    }

    @Test
    public void construct_ZeroUploadXY_NoException() {
        Base base = new Base(new ResourceLocation("dummy.png"), Point.pack(0, 0));
        assertEquals(0, Point.x(base.uploadPoint()));
        assertEquals(0, Point.y(base.uploadPoint()));
    }

    @Test
    public void construct_PositiveUploadX_NoException() {
        Base base = new Base(new ResourceLocation("dummy.png"), Point.pack(2, 0));
        assertEquals(2, Point.x(base.uploadPoint()));
        assertEquals(0, Point.y(base.uploadPoint()));
    }

    @Test
    public void construct_PositiveUploadY_NoException() {
        Base base = new Base(new ResourceLocation("dummy.png"), Point.pack(0, 5));
        assertEquals(0, Point.x(base.uploadPoint()));
        assertEquals(5, Point.y(base.uploadPoint()));
    }

    @Test
    public void construct_PositiveUploadXY_NoException() {
        Base base = new Base(new ResourceLocation("dummy.png"), Point.pack(2, 5));
        assertEquals(2, Point.x(base.uploadPoint()));
        assertEquals(5, Point.y(base.uploadPoint()));
    }

}