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

package io.github.soir20.moremcmeta.impl.client.resource;

import com.google.gson.JsonObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link JsonMetadataView}.
 * @author soir20
 */
public class JsonMetadataViewTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullObject_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new JsonMetadataView(null, String::compareTo);
    }

    @Test
    public void construct_NullComparator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new JsonMetadataView(new JsonObject(), null);
    }

    @Test
    public void construct_NullArray_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new JsonMetadataView(null);
    }

}