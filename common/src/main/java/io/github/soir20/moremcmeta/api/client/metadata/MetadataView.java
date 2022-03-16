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

package io.github.soir20.moremcmeta.api.client.metadata;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public interface MetadataView {

    int size();

    boolean hasValue(String key);

    boolean hasValue(int index);

    Optional<String> stringValue(String key);

    Optional<String> stringValue(int index);

    OptionalInt integerValue(String key);

    OptionalInt integerValue(int index);

    OptionalLong longValue(String key);

    OptionalLong longValue(int index);

    Optional<Float> floatValue(String key);

    Optional<Float> floatValue(int index);

    OptionalDouble doubleValue(String key);

    OptionalDouble doubleValue(int index);

    Optional<Boolean> booleanValue(String key);

    Optional<Boolean> booleanValue(int index);

    Optional<MetadataView> subView(String key);

    Optional<MetadataView> subView(int index);
}
