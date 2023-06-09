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

package io.github.moremcmeta.moremcmeta.api.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the messages inside the {@link InvalidPluginException} and
 * {@link ConflictingPluginsException}.
 * @author soir20
 */
public final class PluginExceptionTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void incompletePluginException_Thrown_MessageSameAsReason() {
        expectedException.expect(InvalidPluginException.class);
        expectedException.expectMessage("Dummy message");
        throw new InvalidPluginException("Dummy message");
    }

    @Test
    public void conflictingPluginsException_Thrown_MessageSameAsReason() {
        expectedException.expect(ConflictingPluginsException.class);
        expectedException.expectMessage("Dummy message");
        throw new ConflictingPluginsException("Dummy message");
    }

}