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

package io.github.soir20.moremcmeta.impl.client.adapter;

import io.github.soir20.moremcmeta.impl.client.adapter.AtlasAdapter;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link AtlasAdapter}.
 * @author soir20
 */
public class AtlasAdapterTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullLocation_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AtlasAdapter(null, (sprite) -> 1);
    }

    @Test
    public void construct_NullGetter_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AtlasAdapter(new ResourceLocation("textures/atlas/blocks.png"), null);
    }

}