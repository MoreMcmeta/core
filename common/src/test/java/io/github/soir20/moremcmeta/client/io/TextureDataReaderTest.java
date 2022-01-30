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

package io.github.soir20.moremcmeta.client.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Tests the {@link TextureDataReader}. This class uses some Minecraft IO functions, and it
 * is planned to completely revamp how this class works in the future. Its code is mostly creating
 * other things that are tested. Therefore, the tests for it are less thorough.
 * @author soir20
 */
public class TextureDataReaderTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void read_NullTextureStream_NullPointerException() throws IOException {
        TextureDataReader reader = new TextureDataReader();
        expectedException.expect(NullPointerException.class);
        reader.read(null, new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void read_NullMetadataStream_NullPointerException() throws IOException {
        TextureDataReader reader = new TextureDataReader();
        expectedException.expect(NullPointerException.class);
        reader.read(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), null);
    }

}