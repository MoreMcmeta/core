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

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * <p>A {@link MetadataView} that is the aggregate of several other views. This class is useful when
 * implementing {@link MetadataParser#combine(ResourceLocation, Map)}.</p>
 *
 * <p>This view's keys are in the same order as the views provided; they are not alphabetized or otherwise
 * reordered. If a key is present in multiple views, the value of that key in the first view it appears in
 * is used. The other values for that key are omitted.</p>
 *
 * <p>For example, say there are three views provided with the following keys: (A, C, B), (D, E), (E, F, A).
 * The resulting view would have keys (A, C, B, D, E, F), with the value of A from the first view and the
 * value of E from the second view.</p>
 * @author soir20
 * @since 4.0.0
 */
public final class CombinedMetadataView implements MetadataView {
    private final Map<String, MetadataView> KEY_TO_VIEW;
    private final List<Pair<String, MetadataView>> INDEX_TO_VIEW;

    /**
     * Creates a new combined metadata view.
     * @param metadataViews     metadata views to combine
     */
    public CombinedMetadataView(Collection<? extends MetadataView> metadataViews) {
        requireNonNull(metadataViews, "Collection of metadata views cannot be null");

        // The LinkedHashMap will keep the keys in insertion order
        KEY_TO_VIEW = new LinkedHashMap<>();
        INDEX_TO_VIEW = new ArrayList<>();

        for (MetadataView view : metadataViews) {
            requireNonNull(view, "Metadata view cannot be null");

            for (String key : view.keys()) {
                if (KEY_TO_VIEW.putIfAbsent(key, view) == null) {
                    INDEX_TO_VIEW.add(Pair.of(key, view));
                }
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
        return value(index, MetadataView::hasKey, false);
    }

    @Override
    public Optional<String> stringValue(String key) {
        return value(key, (view) -> view.stringValue(key), Optional.empty());
    }

    @Override
    public Optional<String> stringValue(int index) {
        return value(index, MetadataView::stringValue, Optional.empty());
    }

    @Override
    public Optional<Integer> integerValue(String key) {
        return value(key, (view) -> view.integerValue(key), Optional.empty());
    }

    @Override
    public Optional<Integer> integerValue(int index) {
        return value(index, MetadataView::integerValue, Optional.empty());
    }

    @Override
    public Optional<Long> longValue(String key) {
        return value(key, (view) -> view.longValue(key), Optional.empty());
    }

    @Override
    public Optional<Long> longValue(int index) {
        return value(index, MetadataView::longValue, Optional.empty());
    }

    @Override
    public Optional<Float> floatValue(String key) {
        return value(key, (view) -> view.floatValue(key), Optional.empty());
    }

    @Override
    public Optional<Float> floatValue(int index) {
        return value(index, MetadataView::floatValue, Optional.empty());
    }

    @Override
    public Optional<Double> doubleValue(String key) {
        return value(key, (view) -> view.doubleValue(key), Optional.empty());
    }

    @Override
    public Optional<Double> doubleValue(int index) {
        return value(index, MetadataView::doubleValue, Optional.empty());
    }

    @Override
    public Optional<Boolean> booleanValue(String key) {
        return value(key, (view) -> view.booleanValue(key), Optional.empty());
    }

    @Override
    public Optional<Boolean> booleanValue(int index) {
        return value(index, MetadataView::booleanValue, Optional.empty());
    }

    @Override
    public Optional<InputStream> byteStreamValue(String key) {
        return value(key, (view) -> view.byteStreamValue(key), Optional.empty());
    }

    @Override
    public Optional<InputStream> byteStreamValue(int index) {
        return value(index, MetadataView::byteStreamValue, Optional.empty());
    }

    @Override
    public Optional<MetadataView> subView(String key) {
        return value(key, (view) -> view.subView(key), Optional.empty());
    }

    @Override
    public Optional<MetadataView> subView(int index) {
        return value(index, MetadataView::subView, Optional.empty());
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
     * @param valueGetter   function to get value using the key at the given index
     * @param defaultValue  default value if the value is not found
     * @return value at the provided index
     * @param <T> type of value to retrieve
     */
    private <T> T value(int index, BiFunction<MetadataView, String, T> valueGetter, T defaultValue) {
        if (index < 0) {
            throw new NegativeKeyIndexException(index);
        }

        if (index >= size()) {
            return defaultValue;
        }

        Pair<String, MetadataView> keyAndView = INDEX_TO_VIEW.get(index);
        return valueGetter.apply(keyAndView.getSecond(), keyAndView.getFirst());
    }

}
