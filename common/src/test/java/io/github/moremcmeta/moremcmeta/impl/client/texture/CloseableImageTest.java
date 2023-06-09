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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests default methods of the {@link CloseableImage}.
 * @author soir20
 */
public final class CloseableImageTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    
    @Test
    @SuppressWarnings("ConstantConditions")
    public void copyFrom_NullSource_NullPointerException() {
        CloseableImage destination = new MockCloseableImage(100, 200);

        expectedException.expect(NullPointerException.class);
        destination.copyFrom(null);
    }

    @Test
    public void copyFrom_SourceHasSameWidthSameHeight_OnlyCommonAreaModified() {
        CloseableImage destination = new MockCloseableImage(100, 200);

        CloseableImage source = new MockCloseableImage(100, 200);

        source.setColor(45, 99, 1177013896);
        source.setColor(23, 10, 721898013);

        // Outside smaller width, inside smaller height
        source.setColor(75, 57, 208915787);

        // Inside smaller width, outside smaller height
        source.setColor(23, 100, 673006803);

        destination.copyFrom(source);
        assertEquals(1177013896, destination.color(45, 99));
        assertEquals(721898013, destination.color(23, 10));

        // Outside smaller width, inside smaller height
        assertEquals(208915787, destination.color(75, 57));

        // Inside smaller width, outside smaller height
        assertEquals(673006803, destination.color(23, 100));

    }

    @Test
    public void copyFrom_SourceHasSmallerWidthSmallerHeight_OnlyCommonAreaModified() {
        CloseableImage destination = new MockCloseableImage(100, 200);

        // Outside smaller width, inside smaller height
        destination.setColor(75, 57, 208915787);

        // Inside smaller width, outside smaller height
        destination.setColor(23, 100, 673006803);
        
        CloseableImage source = new MockCloseableImage(50, 100);
        
        source.setColor(45, 99, 1177013896);
        source.setColor(23, 10, 721898013);

        destination.copyFrom(source);
        assertEquals(1177013896, destination.color(45, 99));
        assertEquals(721898013, destination.color(23, 10));

        // Outside smaller width, inside smaller height
        assertEquals(208915787, destination.color(75, 57));

        // Inside smaller width, outside smaller height
        assertEquals(673006803, destination.color(23, 100));

    }

    @Test
    public void copyFrom_SourceHasSmallerWidthLargerHeight_OnlyCommonAreaModified() {
        CloseableImage destination = new MockCloseableImage(100, 100);

        // Outside smaller width, inside smaller height
        destination.setColor(75, 57, 208915787);

        CloseableImage source = new MockCloseableImage(50, 200);
        source.setColor(45, 99, 1177013896);
        source.setColor(23, 10, 721898013);

        // Inside smaller width, outside smaller height
        source.setColor(23, 100, 673006803);

        destination.copyFrom(source);
        assertEquals(1177013896, destination.color(45, 99));
        assertEquals(721898013, destination.color(23, 10));

        assertEquals(208915787, destination.color(75, 57));
    }

    @Test
    public void copyFrom_SourceHasLargerWidthSmallerHeight_OnlyCommonAreaModified() {
        CloseableImage destination = new MockCloseableImage(50, 200);

        // Inside smaller width, outside smaller height
        destination.setColor(23, 100, 673006803);

        CloseableImage source = new MockCloseableImage(100, 100);
        source.setColor(45, 99, 1177013896);
        source.setColor(23, 10, 721898013);

        // Outside smaller width, inside smaller height
        source.setColor(75, 57, 208915787);

        destination.copyFrom(source);
        assertEquals(1177013896, destination.color(45, 99));
        assertEquals(721898013, destination.color(23, 10));

        // Inside smaller width, outside smaller height
        assertEquals(673006803, destination.color(23, 100));

    }

    @Test
    public void copyFrom_SourceHasLargerWidthLargerHeight_OnlyCommonAreaModified() {
        CloseableImage destination = new MockCloseableImage(50, 100);

        CloseableImage source = new MockCloseableImage(100, 200);
        source.setColor(45, 99, 1177013896);
        source.setColor(23, 10, 721898013);

        // Outside smaller width, inside smaller height
        source.setColor(75, 57, 208915787);

        // Inside smaller width, outside smaller height
        source.setColor(23, 100, 673006803);

        destination.copyFrom(source);
        assertEquals(1177013896, destination.color(45, 99));
        assertEquals(721898013, destination.color(23, 10));
    }

    @Test
    public void copyFrom_AfterDestinationClose_IllegalStateException() {
        CloseableImage destination = new MockCloseableImage(50, 100);

        CloseableImage source = new MockCloseableImage(100, 200);
        source.setColor(45, 99, 1177013896);
        source.setColor(23, 10, 721898013);

        // Outside smaller width, inside smaller height
        source.setColor(75, 57, 208915787);

        // Inside smaller width, outside smaller height
        source.setColor(23, 100, 673006803);

        destination.close();

        expectedException.expect(IllegalStateException.class);
        destination.copyFrom(source);
    }

    @Test
    public void copyFrom_AfterSourceClose_IllegalStateException() {
        CloseableImage destination = new MockCloseableImage(50, 100);

        CloseableImage source = new MockCloseableImage(100, 200);
        source.setColor(45, 99, 1177013896);
        source.setColor(23, 10, 721898013);

        // Outside smaller width, inside smaller height
        source.setColor(75, 57, 208915787);

        // Inside smaller width, outside smaller height
        source.setColor(23, 100, 673006803);

        source.close();

        expectedException.expect(IllegalStateException.class);
        destination.copyFrom(source);
    }

}