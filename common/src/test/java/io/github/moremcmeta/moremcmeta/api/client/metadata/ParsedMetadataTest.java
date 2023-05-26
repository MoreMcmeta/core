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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the default methods of the {@link ParsedMetadata} class and {@link .ParsedMetadata.FrameSize}.
 * @author soir20
 */
public class ParsedMetadataTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void frameWidth_NotOverridden_Empty() {
        assertFalse((new ParsedMetadata() {}).frameWidth().isPresent());
    }

    @Test
    public void frameHeight_NotOverridden_Empty() {
        assertFalse((new ParsedMetadata() {}).frameHeight().isPresent());
    }

    @Test
    public void blur_NotOverridden_Empty() {
        assertFalse((new ParsedMetadata() {}).blur().isPresent());
    }

    @Test
    public void clamp_NotOverridden_Empty() {
        assertFalse((new ParsedMetadata() {}).clamp().isPresent());
    }

    @Test
    public void bases_NotOverridden_Empty() {
        assertTrue((new ParsedMetadata() {}).bases().isEmpty());
    }

}