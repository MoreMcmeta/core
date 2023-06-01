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

import com.google.common.collect.ImmutableMap;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.NegativeKeyIndexException;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mock implementation of {@link MetadataView} that contains no data.
 * @author soir20
 */
public class MockMetadataView implements MetadataView {
    private final Map<String, Object> SECTION_TO_VALUE;

    public MockMetadataView(List<String> sections) {
        SECTION_TO_VALUE = new LinkedHashMap<>();
        for (String section : sections) {
            SECTION_TO_VALUE.put(section, new MockMetadataView(ImmutableMap.of("dummy", 1)));
        }
    }

    public MockMetadataView(Map<String, Object> sectionToValue) {
        SECTION_TO_VALUE = sectionToValue;
    }

    @Override
    public int size() {
        return SECTION_TO_VALUE.size();
    }

    @Override
    public Iterable<String> keys() {
        return SECTION_TO_VALUE.keySet();
    }

    @Override
    public boolean hasKey(String key) {
        return SECTION_TO_VALUE.containsKey(key);
    }

    @Override
    public boolean hasKey(int index) {
        checkNegativeIndex(index);
        return SECTION_TO_VALUE.size() > index;
    }

    @Override
    public Optional<String> stringValue(String key) {
        return valueIfPresent(key, String.class);
    }

    @Override
    public Optional<String> stringValue(int index) {
        checkNegativeIndex(index);
        return valueIfPresent(index, String.class);
    }

    @Override
    public Optional<Integer> integerValue(String key) {
        return valueIfPresent(key, Integer.class);
    }

    @Override
    public Optional<Integer> integerValue(int index) {
        checkNegativeIndex(index);
        return valueIfPresent(index, Integer.class);
    }

    @Override
    public Optional<Long> longValue(String key) {
        return valueIfPresent(key, Long.class);
    }

    @Override
    public Optional<Long> longValue(int index) {
        checkNegativeIndex(index);
        return valueIfPresent(index, Long.class);
    }

    @Override
    public Optional<Float> floatValue(String key) {
        return valueIfPresent(key, Float.class);
    }

    @Override
    public Optional<Float> floatValue(int index) {
        checkNegativeIndex(index);
        return valueIfPresent(index, Float.class);
    }

    @Override
    public Optional<Double> doubleValue(String key) {
        return valueIfPresent(key, Double.class);
    }

    @Override
    public Optional<Double> doubleValue(int index) {
        checkNegativeIndex(index);
        return valueIfPresent(index, Double.class);
    }

    @Override
    public Optional<Boolean> booleanValue(String key) {
        return valueIfPresent(key, Boolean.class);
    }

    @Override
    public Optional<Boolean> booleanValue(int index) {
        checkNegativeIndex(index);
        return valueIfPresent(index, Boolean.class);
    }

    @Override
    public Optional<InputStream> byteStreamValue(String key) {
        return valueIfPresent(key, InputStream.class);
    }

    @Override
    public Optional<InputStream> byteStreamValue(int index) {
        checkNegativeIndex(index);
        return valueIfPresent(index, InputStream.class);
    }

    @Override
    public Optional<MetadataView> subView(String key) {
        return valueIfPresent(key, MetadataView.class);
    }

    @Override
    public Optional<MetadataView> subView(int index) {
        checkNegativeIndex(index);
        return valueIfPresent(index, MetadataView.class);
    }

    private <T> Optional<T> valueIfPresent(String key, Class<T> expectedClass) {
        if (!SECTION_TO_VALUE.containsKey(key)) {
            return Optional.empty();
        }

        Object value = SECTION_TO_VALUE.get(key);
        return expectedClass.isInstance(value) ? Optional.of(expectedClass.cast(value)) : Optional.empty();
    }

    private <T> Optional<T> valueIfPresent(int index, Class<T> expectedClass) {
        if (index >= SECTION_TO_VALUE.size()) {
            return Optional.empty();
        }

        Object value = SECTION_TO_VALUE.values().toArray()[index];
        return expectedClass.isInstance(value) ? Optional.of(expectedClass.cast(value)) : Optional.empty();
    }

    private void checkNegativeIndex(int index) {
        if (index < 0) {
            throw new NegativeKeyIndexException(index);
        }
    }
}
