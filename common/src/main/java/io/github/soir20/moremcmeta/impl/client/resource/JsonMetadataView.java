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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * {@link MetadataView} implementation with an underlying JSON format.
 * @author soir20
 */
public class JsonMetadataView implements MetadataView {
    private final Root ROOT;
    private final List<String> KEYS;
    private final int SIZE;

    /**
     * Creates a new metadata view with a JSON object as the root.
     * @param root              the root JSON object
     * @param keyComparator     comparator to order keys by index
     */
    public JsonMetadataView(JsonObject root, Comparator<? super String> keyComparator) {
        ROOT = new Root(requireNonNull(root, "Root cannot be null"));
        KEYS = root.keySet().stream().filter((key) -> !root.get(key).isJsonNull()).sorted(keyComparator).toList();
        SIZE = KEYS.size();
    }

    /**
     * Creates a new metadata view with a JSON array as the root.
     * @param root              the root JSON array
     */
    public JsonMetadataView(JsonArray root) {
        ROOT = new Root(requireNonNull(root, "Array cannot be null"));
        KEYS = IntStream.range(0, root.size()).mapToObj(String::valueOf).toList();
        SIZE = root.size();
    }

    /**
     * Gets the number of top-level keys in this view.
     * @return number of top-level keys
     */
    @Override
    public int size() {
        return SIZE;
    }

    /**
     * Gets all the top-level keys in this view.
     * @return all the top-level keys in this view
     */
    @Override
    public Iterable<String> keys() {

        // The list is created to be unmodifiable
        return KEYS;

    }

    /**
     * Checks if this view has a data value or sub-view for a top-level key with the
     * given name.
     * @param key       the key to check for
     * @return true if the view has a data value or sub-view for this key or false otherwise
     */
    @Override
    public boolean hasKey(String key) {
        requireNonNull(key, "Key cannot be null");

        int keyAsIndex = strAsIndex(key);
        return ROOT.get(
                (obj) -> obj.has(key),
                (array) -> keyAsIndex >= 0 && keyAsIndex < SIZE
        );
    }

    /**
     * Checks if this view has a data value or sub-view for a top-level key at the
     * given index (starting at 0). Returns false for any positive or negative index out of
     * bounds.
     * @param index       the index of the key to check for
     * @return true if the view has a data value or sub-view for this key or false otherwise
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    @Override
    public boolean hasKey(int index) {
        if (index < 0) {
            throw new NegativeKeyIndexException(index);
        }

        return index < SIZE;
    }

    /**
     * Retrieves the value of a string for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid string, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a string.
     *
     * Any value that is not a sub-view can be converted to a string, so this method will return a
     * string whenever {@link #hasKey(String)} returns true.
     * @param key       the key whose string value to retrieve
     * @return An {@link Optional} containing the string value or {@link Optional#empty()} if there is
     *         string value associated with the key. The string inside the {@link Optional} will never
     *         be null.
     */
    @Override
    public Optional<String> stringValue(String key) {

        // The Gson implementation can convert any primitive to a string, even if isString() is false
        return primitiveFromKey(key, (element) -> true, JsonPrimitive::getAsString);

    }

    /**
     * Retrieves the value of a string for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid string, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a string.
     *
     * Any value that is not a sub-view can be converted to a string, so this method will return a
     * string whenever {@link #hasKey(int)} returns true.
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the string value or {@link Optional#empty()} if there is
     *         string value associated with the key. The string inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    @Override
    public Optional<String> stringValue(int index) {

        // The Gson implementation can convert any primitive to a string, even if isString() is false
        return primitiveFromIndex(index, (element) -> true, JsonPrimitive::getAsString);

    }

    /**
     * Retrieves the value of a signed 32-bit integer for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid signed 32-bit integer, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not an integer.
     * @param key       the key whose signed 32-bit integer value to retrieve
     * @return An {@link Optional} containing the integer value or {@link Optional#empty()} if there is
     *         integer value associated with the key. The integer inside the {@link Optional} will never
     *         be null.
     */
    @Override
    public Optional<Integer> integerValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && isSignedInteger(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsInt
        );
    }

    /**
     * Retrieves the value of a signed 32-bit integer for the key at the given index if any exists. If there is no
     * such key or the key's associated value is not a valid signed 32-bit integer, this method returns
     * {@link Optional#empty()}. {@link #hasKey(int)} can be used to determine whether the key is present or
     * the value is not an integer.
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the integer value or {@link Optional#empty()} if there is
     *         integer value associated with the key. The integer inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    @Override
    public Optional<Integer> integerValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && isSignedInteger(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsInt
        );
    }

    /**
     * Retrieves the value of a signed 64-bit long for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid signed 64-bit long, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a long.
     * @param key       the key whose signed 64-bit long value to retrieve
     * @return An {@link Optional} containing the long value or {@link Optional#empty()} if there is
     *         long value associated with the key. The long inside the {@link Optional} will never
     *         be null.
     */
    @Override
    public Optional<Long> longValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && isSignedLong(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsLong
        );
    }

    /**
     * Retrieves the value of a signed 64-bit long for the key at the given index if any exists. If there is
     * no such key or the key's associated value is not a valid signed 64-bit long, this method returns
     * {@link Optional#empty()}. {@link #hasKey(int)} can be used to determine whether the key is present
     * or the value is not a long.
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the long value or {@link Optional#empty()} if there is
     *         long value associated with the key. The long inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    @Override
    public Optional<Long> longValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && isSignedLong(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsLong
        );
    }

    /**
     * Retrieves the value of a float for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid float, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a float.
     * @param key       the key whose float value to retrieve
     * @return An {@link Optional} containing the float value or {@link Optional#empty()} if there is
     *         float value associated with the key. The float inside the {@link Optional} will never
     *         be null.
     */
    @Override
    public Optional<Float> floatValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && Float.isFinite(primitive.getAsBigDecimal().floatValue()),
                JsonPrimitive::getAsFloat
        );
    }

    /**
     * Retrieves the value of a float for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid float, this method returns {@link Optional#empty()}.
     * {@link #hasKey(int)} can be used to determine whether the key is present or the value is
     * not a float.
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the float value or {@link Optional#empty()} if there is
     *         float value associated with the key. The float inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    @Override
    public Optional<Float> floatValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && Float.isFinite(primitive.getAsBigDecimal().floatValue()),
                JsonPrimitive::getAsFloat
        );
    }

    /**
     * Retrieves the value of a double for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid double, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a double.
     * @param key       the key whose double value to retrieve
     * @return An {@link Optional} containing the double value or {@link Optional#empty()} if there is
     *         double value associated with the key. The double inside the {@link Optional} will never
     *         be null.
     */
    @Override
    public Optional<Double> doubleValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && Double.isFinite(primitive.getAsBigDecimal().doubleValue()),
                JsonPrimitive::getAsDouble
        );
    }

    /**
     * Retrieves the value of a double for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid double, this method returns {@link Optional#empty()}.
     * {@link #hasKey(int)} can be used to determine whether the key is present or the value is
     * not a double.
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the double value or {@link Optional#empty()} if there is
     *         double value associated with the key. The double inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    @Override
    public Optional<Double> doubleValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && Double.isFinite(primitive.getAsBigDecimal().doubleValue()),
                JsonPrimitive::getAsDouble
        );
    }

    /**
     * Retrieves the value of a boolean for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid boolean, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a boolean.
     *
     * A value is considered true if it is equivalent to the string "true" (case-insensitive). Otherwise,
     * the value is considered false. However, a sub-view is not considered a valid boolean.
     * @param key       the key whose boolean value to retrieve
     * @return An {@link Optional} containing the boolean value or {@link Optional#empty()} if there is
     *         boolean value associated with the key. The boolean inside the {@link Optional} will never
     *         be null.
     */
    @Override
    public Optional<Boolean> booleanValue(String key) {

        // The Gson implementation can convert any primitive to a boolean, even if isString() is false
        return primitiveFromKey(key, (element) -> true, JsonPrimitive::getAsBoolean);

    }

    /**
     * Retrieves the value of a boolean for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid boolean, this method returns {@link Optional#empty()}.
     * {@link #hasKey(int)} can be used to determine whether the key is present or the value is
     * not a boolean.
     *
     * A value is considered true if it is equivalent to the string "true" (case-insensitive). Otherwise,
     * the value is considered false. However, a sub-view is not considered a valid boolean.
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the boolean value or {@link Optional#empty()} if there is
     *         boolean value associated with the key. The boolean inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    @Override
    public Optional<Boolean> booleanValue(int index) {

        // The Gson implementation can convert any primitive to a boolean, even if isString() is false
        return primitiveFromIndex(index, (element) -> true, JsonPrimitive::getAsBoolean);

    }

    /**
     * Retrieves the sub-view for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid sub-view, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a sub-view.
     * @param key       the key whose sub-view to retrieve
     * @return An {@link Optional} containing the sub-view or {@link Optional#empty()} if there is
     *         sub-view associated with the key. The sub-view inside the {@link Optional} will never
     *         be null.
     */
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

                    // Do not use hasKey(index) here as a negative index should not raise an exception
                    if (keyAsIndex < 0 || keyAsIndex >= SIZE) {
                        return Optional.empty();
                    }

                    return convertToSubView(array.get(keyAsIndex));
                }
        );
    }

    /**
     * Retrieves a sub-view for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid sub-view, this method returns {@link Optional#empty()}.
     * {@link #hasKey(int)} can be used to determine whether the key is present or the value is
     * not a sub-view.
     * @param index       the index of the key whose sub-view value to retrieve
     * @return An {@link Optional} containing the sub-view or {@link Optional#empty()} if there is
     *         sub-view associated with the key. The sub-view inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    @Override
    public Optional<MetadataView> subView(int index) {
        if (!hasKey(index)) {
            return Optional.empty();
        }

        return ROOT.get(
                (obj) -> convertToSubView(objectElementByIndex(obj, index)),
                (array) -> convertToSubView(array.get(index))
        );
    }

    /**
     * Convert a string to an integer index.
     * @param str       the string to convert
     * @return the string as an integer or -1 if the string does not represent an integer
     */
    private int strAsIndex(String str) {

        @SuppressWarnings("UnstableApiUsage")
        int index = Optional.of(str).map(Ints::tryParse).orElse(-1);

        return index;
    }

    /**
     * Retrieves the primitive at the given key if it satisfies the requirement of the `checkFunction`.
     * @param key                   the key whose value to retrieve
     * @param checkFunction         checks whether the primitive is a value of the required type
     * @param convertFunction       converts the primitive to the value of the required type if it
     *                              passes the `checkFunction`
     * @return the value at the key if it exists, is a primitive, and satisfies the `checkFunction`
     * @param <T> the type of primitive to retrieve
     */
    private <T> Optional<T> primitiveFromKey(String key, Function<JsonPrimitive, Boolean> checkFunction,
                                             Function<JsonPrimitive, T> convertFunction) {
        requireNonNull(key, "Key cannot be null");

        if (!hasKey(key)) {
            return Optional.empty();
        }

        int keyAsIndex = strAsIndex(key);
        return ROOT.get(
                (obj) -> primitiveOfType(obj.get(key), checkFunction, convertFunction),
                (array) ->  primitiveOfType(array.get(keyAsIndex), checkFunction, convertFunction)
        );
    }

    /**
     * Retrieves the primitive at the given index if it satisfies the requirement of the `checkFunction`.
     * @param index                 the index whose value to retrieve
     * @param checkFunction         checks whether the primitive is a value of the required type
     * @param convertFunction       converts the primitive to the value of the required type if it
     *                              passes the `checkFunction`
     * @return the value at the index if it exists, is a primitive, and satisfies the `checkFunction`
     * @param <T> the type to convert the primitive to
     */
    private <T> Optional<T> primitiveFromIndex(int index, Function<JsonPrimitive, Boolean> checkFunction,
                                               Function<JsonPrimitive, T> convertFunction) {
        if (!hasKey(index)) {
            return Optional.empty();
        }

        return ROOT.get(
                (obj) -> primitiveOfType(objectElementByIndex(obj, index), checkFunction, convertFunction),
                (array) ->  primitiveOfType(array.get(index), checkFunction, convertFunction)
        );
    }

    /**
     * Converts a JSON element to a value of the desired type if it is a primitive and  passes the `checkFunction`.
     * @param element               element to convert
     * @param checkFunction         checks whether the primitive is a value of the required type
     * @param convertFunction       converts the primitive to the value of the required type if it
     *                              passes the `checkFunction`
     * @return the value if it satisfies the `checkFunction` or {@link Optional#empty()}
     * @param <T> the type to convert the primitive to
     */
    private <T> Optional<T> primitiveOfType(JsonElement element, Function<JsonPrimitive, Boolean> checkFunction,
                                            Function<JsonPrimitive, T> convertFunction) {
        if (!element.isJsonPrimitive()) {
            return Optional.empty();
        }

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!checkFunction.apply(primitive)) {
            return Optional.empty();
        }

        return Optional.of(convertFunction.apply(primitive));
    }

    /**
     * Gets an element in a JSON object by index.
     * @param obj       the object to access by index
     * @param index     the index of the key to access
     * @return the item at that key or `null` if there is no item at the given index
     */
    private JsonElement objectElementByIndex(JsonObject obj, int index) {
        return obj.get(KEYS.get(index));
    }

    /**
     * Checks if a number is a signed 32-bit integer.
     * @param num       number to check
     * @return true if the number is a signed 32-bit integer, false otherwise
     */
    private boolean isSignedInteger(BigDecimal num) {
        return num.stripTrailingZeros().scale() <= 0
                && isBetweenInclusive(num, BigDecimal.valueOf(Integer.MIN_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
    }

    /**
     * Checks if a number is a signed 64-bit long.
     * @param num       number to check
     * @return true if the number is a signed 64-bit long, false otherwise
     */
    private boolean isSignedLong(BigDecimal num) {
        return num.stripTrailingZeros().scale() <= 0
                && isBetweenInclusive(num, BigDecimal.valueOf(Long.MIN_VALUE), BigDecimal.valueOf(Long.MAX_VALUE));
    }

    /**
     * Checks if a value is in between a lower value (inclusive) and an upper value (inclusive).
     * @param value     the value to check
     * @param lower     the lower bound to compare the value to
     * @param upper     the upper bound to compare the value to
     * @return true if the value is between the lower bound and upper bound, inclusive
     * @param <T> the type of the values to compare
     */
    private <T extends Comparable<? super T>> boolean isBetweenInclusive(T value, T lower, T upper) {
        return value.compareTo(lower) >= 0 && value.compareTo(upper) <= 0;
    }

    /**
     * Converts a JSON object or a JSON array to a {@link MetadataView}.
     * @param element       the element to convert
     * @return a metadata view for the element or {@link Optional#empty()} if the element is neither
     *         an object nor an array
     */
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

    /**
     * Union of the {@link JsonObject} and {@link JsonArray} types so that they can be
     * accessed similarly.
     * @author soir20
     */
    private static class Root {
        private final JsonObject OBJECT;
        private final JsonArray ARRAY;

        /**
         * Creates a new root based on an object.
         * @param object        the object that is the root
         */
        public Root(JsonObject object) {
            OBJECT = object;
            ARRAY = null;
        }

        /**
         * Creates a new root based on an array.
         * @param array         the array that is the root
         */
        public Root(JsonArray array) {
            OBJECT = null;
            ARRAY = array;
        }

        /**
         * Applies a function to the object root or the array root, depending on which type this root is.
         * @param objectFunction        the function to apply if this root holds an object
         * @param arrayFunction         the function to apply if this root holds an array
         * @return the return value of whichever function was applies
         * @param <T> the type of value to retrieve
         */
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
