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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertFalse;

/**
 * Tests the default methods of the {@link AnalyzedMetadata} class.
 * @author soir20
 */
public final class AnalyzedMetadataTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void frameWidth_NotOverridden_Empty() {
        assertFalse((new AnalyzedMetadata() {}).frameWidth().isPresent());
    }

    @Test
    public void frameHeight_NotOverridden_Empty() {
        assertFalse((new AnalyzedMetadata() {}).frameHeight().isPresent());
    }

    @Test
    public void blur_NotOverridden_Empty() {
        assertFalse((new AnalyzedMetadata() {}).blur().isPresent());
    }

    @Test
    public void clamp_NotOverridden_Empty() {
        assertFalse((new AnalyzedMetadata() {}).clamp().isPresent());
    }

    @Test
    public void guiScaling_NotOverridden_Empty() {
        assertFalse((new AnalyzedMetadata() {}).guiScaling().isPresent());
    }

}