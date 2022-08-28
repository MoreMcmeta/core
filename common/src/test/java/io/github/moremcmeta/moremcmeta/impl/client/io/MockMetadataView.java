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

package io.github.moremcmeta.moremcmeta.impl.client.io;

import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;

import java.util.List;
import java.util.Optional;

/**
 * Mock implementation of {@link MetadataView} that contains no data.
 * @author soir20
 */
public class MockMetadataView implements MetadataView {
    private final List<String> SECTIONS;

    public MockMetadataView(List<String> sections) {
        SECTIONS = sections;
    }

    @Override
    public int size() {
        return SECTIONS.size();
    }

    @Override
    public Iterable<String> keys() {
        return SECTIONS;
    }

    @Override
    public boolean hasKey(String key) {
        return false;
    }

    @Override
    public boolean hasKey(int index) {
        return false;
    }

    @Override
    public Optional<String> stringValue(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<String> stringValue(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> integerValue(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> integerValue(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<Long> longValue(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<Long> longValue(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<Float> floatValue(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<Float> floatValue(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<Double> doubleValue(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<Double> doubleValue(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> booleanValue(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> booleanValue(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<MetadataView> subView(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<MetadataView> subView(int index) {
        return Optional.empty();
    }
}
