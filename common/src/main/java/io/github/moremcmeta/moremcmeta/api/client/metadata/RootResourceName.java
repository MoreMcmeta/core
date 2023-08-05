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

import static java.util.Objects.requireNonNull;

/**
 * The name of a resource at the root of a resource pack with its metadata extension.
 * @author soir20
 * @since 4.2.0
 */
public final class RootResourceName {
    private final String NAME;

    /**
     * Creates a full root resource name.
     * @param name          name of the resource with its extension
     * @throws InvalidRootResourceNameException if the name does not match [a-z0-9_.-]+
     */
    public RootResourceName(String name) {
        NAME = requireNonNull(name, "Root metadata name cannot be null");

        String regex = "[a-z0-9_.-]+";
        if (!name.matches(regex)) {
            throw new InvalidRootResourceNameException("Root metadata name must match " + regex + ", but was: " + name);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RootResourceName otherName)) {
            return false;
        }

        return NAME.equals(otherName.NAME);
    }

    @Override
    public int hashCode() {
        return NAME.hashCode();
    }

    @Override
    public String toString() {
        return NAME;
    }

}
