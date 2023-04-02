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

package io.github.moremcmeta.moremcmeta.api.client.texture;

import io.github.moremcmeta.moremcmeta.api.math.NegativeDimensionException;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Tests the {@link TextureHandle}.
 * @author soir20
 */
public class TextureHandleTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void find_NullPath_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        TextureHandle.find(null);
    }

    @Test
    public void find_ModNotLoaded_NothingReturned() {
        TextureHandle.find(new ResourceLocation("bat.png"));
    }

    @Test
    public void construct_NullBindFunction_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureHandle(null, 10, 20, 30, 40);
    }

    @Test
    public void construct_NegativeMinX_NegativeCoordinateException() {
        expectedException.expect(NegativeUploadPointException.class);
        new TextureHandle(() -> {}, -1, 20, 30, 40);
    }

    @Test
    public void construct_NegativeMinY_NegativeCoordinateException() {
        expectedException.expect(NegativeUploadPointException.class);
        new TextureHandle(() -> {}, 10, -1, 30, 40);
    }

    @Test
    public void construct_NegativeWidth_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new TextureHandle(() -> {}, 10, 20, -1, 40);
    }

    @Test
    public void construct_NegativeHeight_NegativeDimensionException() {
        expectedException.expect(NegativeDimensionException.class);
        new TextureHandle(() -> {}, 10, 20, 30, -1);
    }

    @Test
    public void construct_ZeroMinX_NoException() {
        TextureHandle handle = new TextureHandle(() -> {}, 0, 20, 30, 40);
        assertEquals(0, handle.minX());
    }

    @Test
    public void construct_ZeroMinY_NoException() {
        TextureHandle handle = new TextureHandle(() -> {}, 10, 0, 30, 40);
        assertEquals(0, handle.minY());
    }

    @Test
    public void construct_ZeroWidth_NoException() {
        TextureHandle handle = new TextureHandle(() -> {}, 10, 20, 0, 40);
        assertEquals(0, handle.width());
    }

    @Test
    public void construct_ZeroHeight_NoException() {
        TextureHandle handle = new TextureHandle(() -> {}, 10, 20, 30, 0);
        assertEquals(0, handle.height());
    }

    @Test
    public void construct_PositiveMinX_NoException() {
        TextureHandle handle = new TextureHandle(() -> {}, 10, 20, 30, 40);
        assertEquals(10, handle.minX());
    }

    @Test
    public void construct_PositiveMinY_NoException() {
        TextureHandle handle = new TextureHandle(() -> {}, 10, 20, 30, 40);
        assertEquals(20, handle.minY());
    }

    @Test
    public void construct_PositiveWidth_NoException() {
        TextureHandle handle = new TextureHandle(() -> {}, 10, 20, 30, 40);
        assertEquals(30, handle.width());
    }

    @Test
    public void construct_PositiveHeight_NoException() {
        TextureHandle handle = new TextureHandle(() -> {}, 10, 20, 30, 40);
        assertEquals(40, handle.height());
    }

    @Test
    public void construct_ValidBind_BindExecuted() {
        AtomicBoolean ran = new AtomicBoolean();
        TextureHandle handle = new TextureHandle(() -> ran.set(true), 10, 20, 30, 40);
        handle.bind();
        assertTrue(ran.get());
    }

}