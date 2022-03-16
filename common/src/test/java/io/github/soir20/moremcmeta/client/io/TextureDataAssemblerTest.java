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

import io.github.soir20.moremcmeta.impl.client.io.TextureDataAssembler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link TextureDataAssembler}. This class uses some Minecraft IO functions, and it
 * is planned to completely revamp how this class works in the future. Its code is mostly creating
 * other things that are tested. Therefore, the tests for it are less thorough.
 * @author soir20
 */
public class TextureDataAssemblerTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void assemble_NullData_NullPointerException() {
        TextureDataAssembler assembler = new TextureDataAssembler();
        expectedException.expect(NullPointerException.class);
        assembler.assemble(null);
    }

}