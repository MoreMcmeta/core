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

package io.github.soir20.moremcmeta.client.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.logging.log4j.LogManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Tests the {@link AnimatedTextureReader}.
 * @author soir20
 */
public class AnimatedTextureReaderTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullLogger_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimatedTextureReader(null);
    }

    @Test
    public void read_NullTextureStream_NullPointerException() throws IOException {
        AnimatedTextureReader reader = new AnimatedTextureReader(LogManager.getLogger());
        expectedException.expect(NullPointerException.class);
        reader.read(null, new ByteArrayInputStream("".getBytes()));
    }

    @Test
    public void read_NullMetadataStream_NullPointerException() throws IOException {
        AnimatedTextureReader reader = new AnimatedTextureReader(LogManager.getLogger());
        expectedException.expect(NullPointerException.class);
        reader.read(new ByteArrayInputStream("".getBytes()), null);
    }

}