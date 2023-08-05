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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link RootResourceName}.
 * @author soir20
 */
public final class RootResourceNameTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    @Test
    public void construct_NullName_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new RootResourceName(null);
    }

    @Test
    public void construct_EmptyName_IllegalArgException() {
        expectedException.expect(InvalidRootResourceNameException.class);
        new RootResourceName("");
    }

    @Test
    public void construct_InvalidName_IllegalArgException() {
        expectedException.expect(InvalidRootResourceNameException.class);
        new RootResourceName("assets/pack.png");
    }

    @SuppressWarnings({"SimplifiableAssertion", "EqualsBetweenInconvertibleTypes"})
    @Test
    public void equals_DifferentClasses_False() {
        RootResourceName name = new RootResourceName("pack.png.moremcmeta");
        assertFalse(name.equals("pack.png"));
    }

    @SuppressWarnings("SimplifiableAssertion")
    @Test
    public void equals_DifferentNames_False() {
        RootResourceName name1 = new RootResourceName("pack.png.moremcmeta");
        RootResourceName name2 = new RootResourceName("pack.jpg.moremcmeta");
        assertFalse(name1.equals(name2));
    }

    @SuppressWarnings("SimplifiableAssertion")
    @Test
    public void equals_SameName_True() {
        RootResourceName name1 = new RootResourceName("pack.png.moremcmeta");
        RootResourceName name2 = new RootResourceName("pack.png.moremcmeta");
        assertTrue(name1.equals(name2));
    }

    @Test
    public void hashCode_DifferentNames_DifferentHashCodes() {
        RootResourceName name1 = new RootResourceName("pack.png.moremcmeta");
        RootResourceName name2 = new RootResourceName("pack.jpg.moremcmeta");
        assertNotEquals(name1.hashCode(), name2.hashCode());
    }

    @Test
    public void hashCode_SameName_SameHashCode() {
        RootResourceName name1 = new RootResourceName("pack.png.moremcmeta");
        RootResourceName name2 = new RootResourceName("pack.png.moremcmeta");
        assertEquals(name1.hashCode(), name2.hashCode());
    }

    @Test
    public void toString_ValidName_IdenticalStringReturned() {
        RootResourceName name1 = new RootResourceName("pack.png.moremcmeta");
        assertEquals("pack.png.moremcmeta", name1.toString());
    }

}