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

import com.google.common.primitives.Ints;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.soir20.moremcmeta.api.client.metadata.MetadataView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

public class JsonMetadataView implements MetadataView {
    private final Root ROOT;
    private final List<String> KEYS;
    private final int SIZE;

    public JsonMetadataView(JsonObject root, Comparator<? super String> keyComparator) {
        ROOT = new Root(requireNonNull(root, "Root cannot be null"));
        KEYS = root.keySet().stream().sorted(keyComparator).toList();
        SIZE = root.size();
    }

    public JsonMetadataView(JsonArray root) {
        ROOT = new Root(requireNonNull(root, "Array cannot be null"));
        KEYS = IntStream.range(0, root.size()).mapToObj(String::valueOf).toList();
        SIZE = root.size();
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public Iterable<String> keys() {

        // The list is created to be unmodifiable
        return KEYS;

    }

    @Override
    public boolean hasKey(String key) {
        requireNonNull(key, "Key cannot be null");

        int keyAsIndex = strAsIndex(key);
        return ROOT.get(
                (obj) -> obj.has(key),
                (array) -> keyAsIndex >= 0 && keyAsIndex < SIZE
        );
    }

    @Override
    public boolean hasKey(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }

        return index < SIZE && ROOT.get(
                (obj) -> objectElementByIndex(obj, index).isJsonPrimitive(),
                (array) -> array.get(index).isJsonPrimitive()
        );
    }

    @Override
    public Optional<String> stringValue(String key) {

        // The Gson implementation can convert any primitive to a string, even if isString() is false
        return primitiveFromKey(key, JsonPrimitive::isJsonPrimitive, JsonPrimitive::getAsString);

    }

    @Override
    public Optional<String> stringValue(int index) {

        // The Gson implementation can convert any primitive to a string, even if isString() is false
        return primitiveFromIndex(index, JsonPrimitive::isJsonPrimitive, JsonPrimitive::getAsString);

    }

    @Override
    public Optional<Integer> integerValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && isSignedInteger(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsInt
        );
    }

    @Override
    public Optional<Integer> integerValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && isSignedInteger(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsInt
        );
    }

    @Override
    public Optional<Integer> unsignedIntegerValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && isUnsignedInteger(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsInt
        );
    }

    @Override
    public Optional<Integer> unsignedIntegerValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && isUnsignedInteger(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsInt
        );
    }

    @Override
    public Optional<Long> longValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && isSignedLong(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsLong
        );
    }

    @Override
    public Optional<Long> longValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && isSignedLong(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsLong
        );
    }

    @Override
    public Optional<Long> unsignedLongValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && isUnsignedLong(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsLong
        );
    }

    @Override
    public Optional<Long> unsignedLongValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && isUnsignedLong(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsLong
        );
    }

    @Override
    public Optional<Float> floatValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && Float.isFinite(primitive.getAsBigDecimal().floatValue()),
                JsonPrimitive::getAsFloat
        );
    }

    @Override
    public Optional<Float> floatValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && Float.isFinite(primitive.getAsBigDecimal().floatValue()),
                JsonPrimitive::getAsFloat
        );
    }

    @Override
    public Optional<Double> doubleValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && Double.isFinite(primitive.getAsBigDecimal().doubleValue()),
                JsonPrimitive::getAsDouble
        );
    }

    @Override
    public Optional<Double> doubleValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && Double.isFinite(primitive.getAsBigDecimal().doubleValue()),
                JsonPrimitive::getAsDouble
        );
    }

    @Override
    public Optional<Boolean> booleanValue(String key) {

        // The Gson implementation can convert any primitive to a boolean, even if isString() is false
        return primitiveFromKey(key, JsonPrimitive::isJsonPrimitive, JsonPrimitive::getAsBoolean);

    }

    @Override
    public Optional<Boolean> booleanValue(int index) {

        // The Gson implementation can convert any primitive to a boolean, even if isString() is false
        return primitiveFromIndex(index, JsonPrimitive::isJsonPrimitive, JsonPrimitive::getAsBoolean);

    }

    @Override
    public Optional<MetadataView> subView(String key) {
        return ROOT.get(
                (obj) -> {
                    if (!obj.has(key)) {
                        return Optional.empty();
                    }

                    return convertToSubView(obj.get(key));
                }, (array) -> {
                    int keyAsIndex = strAsIndex(key);
                    if (keyAsIndex < 0 || keyAsIndex >= SIZE) {
                        return Optional.empty();
                    }

                    return convertToSubView(array.get(keyAsIndex));
                }
        );
    }

    @Override
    public Optional<MetadataView> subView(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }

        if (index >= SIZE) {
            return Optional.empty();
        }

        return ROOT.get(
                (obj) -> convertToSubView(objectElementByIndex(obj, index)),
                (array) -> convertToSubView(array.get(index))
        );
    }

    private int strAsIndex(String str) {

        @SuppressWarnings("UnstableApiUsage")
        int index = Optional.of(str).map(Ints::tryParse).orElse(-1);

        return index;
    }

    private <T> Optional<T> primitiveFromKey(String key, Function<JsonPrimitive, Boolean> checkFunction,
                                             Function<JsonPrimitive, T> convertFunction) {
        requireNonNull(key, "Key cannot be null");

        if (!hasKey(key)) {
            return Optional.empty();
        }

        int keyAsIndex = strAsIndex(key);
        return ROOT.get(
                (obj) -> primitiveOfType(obj.get(key).getAsJsonPrimitive(), checkFunction, convertFunction),
                (array) ->  primitiveOfType(array.get(keyAsIndex).getAsJsonPrimitive(), checkFunction, convertFunction)
        );
    }

    private <T> Optional<T> primitiveFromIndex(int index, Function<JsonPrimitive, Boolean> checkFunction,
                                               Function<JsonPrimitive, T> convertFunction) {
        if (!hasKey(index)) {
            return Optional.empty();
        }

        return ROOT.get(
                (obj) -> {
                    JsonPrimitive primitive = objectElementByIndex(obj, index).getAsJsonPrimitive();
                    return primitiveOfType(primitive, checkFunction, convertFunction);
                },
                (array) ->  primitiveOfType(array.get(index).getAsJsonPrimitive(), checkFunction, convertFunction)
        );
    }

    private <T> Optional<T> primitiveOfType(JsonPrimitive primitive, Function<JsonPrimitive, Boolean> checkFunction,
                                            Function<JsonPrimitive, T> convertFunction) {
        if (!checkFunction.apply(primitive)) {
            return Optional.empty();
        }

        return Optional.of(convertFunction.apply(primitive));
    }

    private JsonElement objectElementByIndex(JsonObject obj, int index) {
        return obj.get(KEYS.get(index));
    }

    private boolean isSignedInteger(BigDecimal num) {
        return num.stripTrailingZeros().scale() <= 0
                && isBetweenInclusive(num, BigDecimal.valueOf(Integer.MIN_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
    }

    private boolean isUnsignedInteger(BigDecimal num) {
        return num.stripTrailingZeros().scale() <= 0
                && isBetweenInclusive(num, BigDecimal.ZERO, BigDecimal.valueOf((1L << 32) - 1));
    }

    private boolean isSignedLong(BigDecimal num) {
        return num.stripTrailingZeros().scale() <= 0
                && isBetweenInclusive(num, BigDecimal.valueOf(Long.MIN_VALUE), BigDecimal.valueOf(Long.MAX_VALUE));
    }

    private boolean isUnsignedLong(BigDecimal num) {
        return num.stripTrailingZeros().scale() <= 0
                && isBetweenInclusive(num.toBigInteger(), BigInteger.ZERO,
                BigInteger.ONE.shiftRight(64).subtract(BigInteger.ONE));
    }

    private <T extends Comparable<? super T>> boolean isBetweenInclusive(T value, T lower, T upper) {
        return value.compareTo(lower) >= 0 && value.compareTo(upper) <= 0;
    }

    private Optional<MetadataView> convertToSubView(JsonElement element) {
        if (element.isJsonObject()) {

            // We only want to use a special comparator for the root of the JSON tree
            return Optional.of(new JsonMetadataView(element.getAsJsonObject(), String::compareTo));

        }

        if (element.isJsonArray()) {
            return Optional.of(new JsonMetadataView(element.getAsJsonArray()));
        }

        return Optional.empty();
    }

    private static class Root {
        private final JsonObject OBJECT;
        private final JsonArray ARRAY;

        public Root(JsonObject object) {
            OBJECT = object;
            ARRAY = null;
        }

        public Root(JsonArray array) {
            OBJECT = null;
            ARRAY = array;
        }

        public <T> T get(Function<JsonObject, T> objectFunction, Function<JsonArray, T> arrayFunction) {
            if (OBJECT != null) {
                return objectFunction.apply(OBJECT);
            }

            if (ARRAY != null) {
                return arrayFunction.apply(ARRAY);
            }

            throw new IllegalStateException("Either object or array must be present");
        }
    }
}
