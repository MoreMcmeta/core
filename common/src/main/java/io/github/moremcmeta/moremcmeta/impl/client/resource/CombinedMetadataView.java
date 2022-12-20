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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A {@link MetadataView} that is the aggregate of several other views.
 * @author soir20
 */
public class CombinedMetadataView implements MetadataView {
    private final Map<String, MetadataView> KEY_TO_VIEW;
    private final NavigableMap<Integer, MetadataView> INDEX_TO_VIEW;

    /**
     * Creates a new combined metadata view.
     * @param metadataViews     metadata views to combine
     */
    public CombinedMetadataView(Collection<? extends MetadataView> metadataViews) {
        requireNonNull(metadataViews, "Collection of metadata views cannot be null");

        // The LinkedHashMap will keep the keys in insertion order
        KEY_TO_VIEW = new LinkedHashMap<>();
        INDEX_TO_VIEW = new TreeMap<>();

        int keysProcessed = 0;
        for (MetadataView view : metadataViews) {
            requireNonNull(view, "Metadata view cannot be null");
            INDEX_TO_VIEW.put(keysProcessed, view);

            for (String key : view.keys()) {
                if (KEY_TO_VIEW.put(key, view) != null) {
                    throw new IllegalArgumentException(
                            "Two metadata files for the same texture have conflicting keys: " + key
                    );
                }
                keysProcessed++;
            }
        }
    }

    @Override
    public int size() {
        return KEY_TO_VIEW.size();
    }

    @Override
    public Iterable<String> keys() {
        return KEY_TO_VIEW.keySet();
    }

    @Override
    public boolean hasKey(String key) {
        return KEY_TO_VIEW.containsKey(key);
    }

    @Override
    public boolean hasKey(int index) {
        return value(index, MetadataView::hasKey);
    }

    @Override
    public Optional<String> stringValue(String key) {
        return value(key, (view) -> view.stringValue(key), Optional.empty());
    }

    @Override
    public Optional<String> stringValue(int index) {
        return value(index, MetadataView::stringValue);
    }

    @Override
    public Optional<Integer> integerValue(String key) {
        return value(key, (view) -> view.integerValue(key), Optional.empty());
    }

    @Override
    public Optional<Integer> integerValue(int index) {
        return value(index, MetadataView::integerValue);
    }

    @Override
    public Optional<Long> longValue(String key) {
        return value(key, (view) -> view.longValue(key), Optional.empty());
    }

    @Override
    public Optional<Long> longValue(int index) {
        return value(index, MetadataView::longValue);
    }

    @Override
    public Optional<Float> floatValue(String key) {
        return value(key, (view) -> view.floatValue(key), Optional.empty());
    }

    @Override
    public Optional<Float> floatValue(int index) {
        return value(index, MetadataView::floatValue);
    }

    @Override
    public Optional<Double> doubleValue(String key) {
        return value(key, (view) -> view.doubleValue(key), Optional.empty());
    }

    @Override
    public Optional<Double> doubleValue(int index) {
        return value(index, MetadataView::doubleValue);
    }

    @Override
    public Optional<Boolean> booleanValue(String key) {
        return value(key, (view) -> view.booleanValue(key), Optional.empty());
    }

    @Override
    public Optional<Boolean> booleanValue(int index) {
        return value(index, MetadataView::booleanValue);
    }

    @Override
    public Optional<MetadataView> subView(String key) {
        return value(key, (view) -> view.subView(key), Optional.empty());
    }

    @Override
    public Optional<MetadataView> subView(int index) {
        return value(index, MetadataView::subView);
    }

    /**
     * Retrieves a value with the given key.
     * @param key           key of the value to retrieve
     * @param valueGetter   function to get value using the key
     * @param defaultValue  default value if the value is not found
     * @return value with the provided key
     * @param <T> type of value to retrieve
     */
    private <T> T value(String key, Function<MetadataView, T> valueGetter, T defaultValue) {
        MetadataView view = KEY_TO_VIEW.get(key);
        if (view == null) {
            return defaultValue;
        }

        return valueGetter.apply(view);
    }

    /**
     * Retrieves a value at the given index.
     * @param index         index of the value to retrieve
     * @param valueGetter   function to get value using the index
     * @return value at the provided index
     * @param <T> type of value to retrieve
     */
    private <T> T value(int index, BiFunction<MetadataView, Integer, T> valueGetter) {
        if (index < 0) {
            throw new NegativeKeyIndexException(index);
        }

        Map.Entry<Integer, MetadataView> startIndexAndView = INDEX_TO_VIEW.floorEntry(index);
        return valueGetter.apply(startIndexAndView.getValue(), index - startIndexAndView.getKey());
    }

}
