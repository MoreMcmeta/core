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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link JsonMetadataView}.
 * @author soir20
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public final class JsonMetadataViewTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullObject_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new JsonMetadataView(null, String::compareTo);
    }

    @Test
    public void construct_NullComparator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new JsonMetadataView(new JsonObject(), null);
    }

    @Test
    public void construct_NullArray_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new JsonMetadataView(null);
    }

    @Test
    public void sizeObject_Empty_0() {
        JsonMetadataView view = new JsonMetadataView(new JsonObject(), String::compareTo);
        assertEquals(0, view.size());
    }

    @Test
    public void sizeObject_SingleLevelKeys_NumberOfKeys() {
        JsonObject root = new JsonObject();
        root.add("hello", new JsonPrimitive(10));
        root.add("world", new JsonPrimitive(true));
        root.add("test", new JsonPrimitive("good morning"));
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertEquals(3, view.size());
    }

    @Test
    public void sizeObject_NullValue_NullExcluded() {
        JsonObject root = new JsonObject();
        root.add("hello", new JsonPrimitive(10));
        root.add("world", new JsonPrimitive(true));
        root.add("test", new JsonPrimitive("good morning"));
        root.add("testing", null);
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertEquals(3, view.size());
    }

    @Test
    public void sizeObject_MultiLevelKeys_NumberOfTopLevelKeys() {
        JsonObject root = new JsonObject();
        root.add("hello", new JsonPrimitive(10));
        root.add("world", new JsonPrimitive(true));
        root.add("test", new JsonPrimitive("good morning"));
        root.add("testing", new JsonObject());
        root.get("testing").getAsJsonObject().add("metadata", new JsonPrimitive(20));
        root.get("testing").getAsJsonObject().add("view", new JsonPrimitive(false));
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertEquals(4, view.size());
    }

    @Test
    public void sizeArray_Empty_0() {
        JsonMetadataView view = new JsonMetadataView(new JsonArray());
        assertEquals(0, view.size());
    }

    @Test
    public void sizeArray_SingleLevelKeys_NumberOfKeys() {
        JsonArray root = new JsonArray();
        root.add(10);
        root.add(true);
        root.add("good morning");
        JsonMetadataView view = new JsonMetadataView(root);
        assertEquals(3, view.size());
    }

    @Test
    public void sizeArray_NullValue_NullExcluded() {
        JsonArray root = new JsonArray();
        root.add(10);
        root.add(true);
        root.add("good morning");
        root.add((JsonObject) null);
        JsonMetadataView view = new JsonMetadataView(root);
        assertEquals(3, view.size());
    }

    @Test
    public void sizeArray_MultiLevelKeys_NumberOfTopLevelKeys() {
        JsonArray root = new JsonArray();
        root.add(10);
        root.add(true);
        root.add("good morning");
        root.add(new JsonObject());
        root.get(3).getAsJsonObject().add("metadata", new JsonPrimitive(20));
        root.get(3).getAsJsonObject().add("view", new JsonPrimitive(false));
        JsonMetadataView view = new JsonMetadataView(root);
        assertEquals(4, view.size());
    }

    @Test
    public void keysObject_Empty_NoKeys() {
        JsonMetadataView view = new JsonMetadataView(new JsonObject(), String::compareTo);
        assertEquals(ImmutableList.of(), collectKeys(view.keys()));
    }

    @Test
    public void keysObject_SingleLevelKeys_AllKeys() {
        JsonObject root = new JsonObject();
        root.add("hello", new JsonPrimitive(10));
        root.add("world", new JsonPrimitive(true));
        root.add("test", new JsonPrimitive("good morning"));
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertEquals(ImmutableList.of("hello", "test", "world"), collectKeys(view.keys()));
    }

    @Test
    public void keysObject_MultiLevelKeys_TopLevelKeys() {
        JsonObject root = new JsonObject();
        root.add("hello", new JsonPrimitive(10));
        root.add("world", new JsonPrimitive(true));
        root.add("test", new JsonPrimitive("good morning"));
        root.add("testing", new JsonObject());
        root.get("testing").getAsJsonObject().add("metadata", new JsonPrimitive(20));
        root.get("testing").getAsJsonObject().add("view", new JsonPrimitive(false));
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertEquals(ImmutableList.of("hello", "test", "testing", "world"), collectKeys(view.keys()));
    }

    @Test
    public void keysArray_Empty_NoKeys() {
        JsonMetadataView view = new JsonMetadataView(new JsonArray());
        assertEquals(ImmutableList.of(), collectKeys(view.keys()));
    }

    @Test
    public void keysArray_SingleLevelKeys_AllKeys() {
        JsonArray root = new JsonArray();
        root.add(10);
        root.add(true);
        root.add("good morning");
        JsonMetadataView view = new JsonMetadataView(root);
        assertEquals(ImmutableList.of("0", "1", "2"), collectKeys(view.keys()));
    }

    @Test
    public void keysArray_MultiLevelKeys_TopLevelKeys() {
        JsonArray root = new JsonArray();
        root.add(10);
        root.add(true);
        root.add("good morning");
        root.add(new JsonObject());
        root.get(3).getAsJsonObject().add("metadata", new JsonPrimitive(20));
        root.get(3).getAsJsonObject().add("view", new JsonPrimitive(false));
        JsonMetadataView view = new JsonMetadataView(root);
        assertEquals(ImmutableList.of("0", "1", "2", "3"), collectKeys(view.keys()));
    }

    @Test
    public void hasKeyStringObject_KeyNotPresent_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.hasKey("not present"));
    }

    @Test
    public void hasKeyStringObject_KeyAtNextLevel_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.hasKey("object val1"));
    }

    @Test
    public void hasKeyStringObject_NullVal_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.hasKey("null val0"));
    }

    @Test
    public void hasKeyStringObject_StringVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("string val0"));
    }

    @Test
    public void hasKeyStringObject_PosIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("pos int val0"));
    }

    @Test
    public void hasKeyStringObject_NegIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("neg int val0"));
    }

    @Test
    public void hasKeyStringObject_PosLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("pos long val0"));
    }

    @Test
    public void hasKeyStringObject_NegLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("neg long val0"));
    }

    @Test
    public void hasKeyStringObject_PosBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("pos int >64-bits val0"));
    }

    @Test
    public void hasKeyStringObject_NegBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("neg int >64-bits val0"));
    }

    @Test
    public void hasKeyStringObject_PosFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("pos float val0"));
    }

    @Test
    public void hasKeyStringObject_NegFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("neg float val0"));
    }

    @Test
    public void hasKeyStringObject_PosDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("pos double val0"));
    }

    @Test
    public void hasKeyStringObject_NegDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("neg double val0"));
    }

    @Test
    public void hasKeyStringObject_TrueVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("true val0"));
    }

    @Test
    public void hasKeyStringObject_FalseVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("false val0"));
    }

    @Test
    public void hasKeyStringObject_ObjectVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("object val0"));
    }

    @Test
    public void hasKeyStringObject_ArrayVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("array val0"));
    }

    @Test
    public void hasKeyStringArray_KeyNegative_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.hasKey("-1"));
    }

    @Test
    public void hasKeyStringArray_KeyNotPresent_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.hasKey("not present"));
    }

    @Test
    public void hasKeyStringArray_KeyAtNextLevel_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.hasKey("object val1"));
    }

    @Test
    public void hasKeyStringArray_ValidIfNullNotFiltered_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.hasKey("15"));
    }

    @Test
    public void hasKeyStringArray_StringVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("0"));
    }

    @Test
    public void hasKeyStringArray_PosIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("1"));
    }

    @Test
    public void hasKeyStringArray_NegIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("2"));
    }

    @Test
    public void hasKeyStringArray_PosLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("3"));
    }

    @Test
    public void hasKeyStringArray_NegLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("4"));
    }

    @Test
    public void hasKeyStringArray_PosBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("5"));
    }

    @Test
    public void hasKeyStringArray_NegBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("6"));
    }

    @Test
    public void hasKeyStringArray_PosFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("7"));
    }

    @Test
    public void hasKeyStringArray_NegFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("8"));
    }

    @Test
    public void hasKeyStringArray_PosDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("9"));
    }

    @Test
    public void hasKeyStringArray_NegDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("10"));
    }

    @Test
    public void hasKeyStringArray_TrueVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("11"));
    }

    @Test
    public void hasKeyStringArray_FalseVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("12"));
    }

    @Test
    public void hasKeyStringArray_ObjectVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("13"));
    }

    @Test
    public void hasKeyStringArray_ArrayVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("14"));
    }

    @Test
    public void hasKeyIndexObject_NegativeIndex_NegativeKeyIndexException() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        expectedException.expect(NegativeKeyIndexException.class);
        view.hasKey(-1);
    }

    @Test
    public void hasKeyIndexObject_StringVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(0));
    }

    @Test
    public void hasKeyIndexObject_PosIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(1));
    }

    @Test
    public void hasKeyIndexObject_NegIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(2));
    }

    @Test
    public void hasKeyIndexObject_PosLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(3));
    }

    @Test
    public void hasKeyIndexObject_NegLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(4));
    }

    @Test
    public void hasKeyIndexObject_PosBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(5));
    }

    @Test
    public void hasKeyIndexObject_NegBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(6));
    }

    @Test
    public void hasKeyIndexObject_PosFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(7));
    }

    @Test
    public void hasKeyIndexObject_NegFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(8));
    }

    @Test
    public void hasKeyIndexObject_PosDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(9));
    }

    @Test
    public void hasKeyIndexObject_NegDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(10));
    }

    @Test
    public void hasKeyIndexObject_TrueVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(11));
    }

    @Test
    public void hasKeyIndexObject_FalseVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(12));
    }

    @Test
    public void hasKeyIndexObject_ObjectVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(13));
    }

    @Test
    public void hasKeyIndexObject_ArrayVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey(14));
    }

    @Test
    public void hasKeyIndexArray_PositiveIndexNotPresent_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.hasKey(view.size()));
    }

    @Test
    public void hasKeyIndexArray_NegativeIndex_NegativeKeyIndexException() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        expectedException.expect(NegativeKeyIndexException.class);
        view.hasKey(-1);
    }

    @Test
    public void hasKeyIndexArray_ValidIfNullNotFiltered_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.hasKey(15));
    }

    @Test
    public void hasKeyIndexArray_StringVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(0));
    }

    @Test
    public void hasKeyIndexArray_PosIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(1));
    }

    @Test
    public void hasKeyIndexArray_NegIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(2));
    }

    @Test
    public void hasKeyIndexArray_PosLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(3));
    }

    @Test
    public void hasKeyIndexArray_NegLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(4));
    }

    @Test
    public void hasKeyIndexArray_PosBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(5));
    }

    @Test
    public void hasKeyIndexArray_NegBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(6));
    }

    @Test
    public void hasKeyIndexArray_PosFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(7));
    }

    @Test
    public void hasKeyIndexArray_NegFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(8));
    }

    @Test
    public void hasKeyIndexArray_PosDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(9));
    }

    @Test
    public void hasKeyIndexArray_NegDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(10));
    }

    @Test
    public void hasKeyIndexArray_TrueVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(11));
    }

    @Test
    public void hasKeyIndexArray_FalseVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(12));
    }

    @Test
    public void hasKeyIndexArray_ObjectVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(13));
    }

    @Test
    public void hasKeyIndexArray_ArrayVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey(14));
    }

    @Test
    public void stringValueStringObject_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue("not present").isPresent());
    }

    @Test
    public void stringValueStringObject_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue("object val1").isPresent());
    }

    @Test
    public void stringValueStringObject_NullVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue("null val0").isPresent());
    }

    @Test
    public void stringValueStringObject_StringVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("hello world", view.stringValue("string val0").get());
    }

    @Test
    public void stringValueStringObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Integer.MAX_VALUE), view.stringValue("pos int val0").get());
    }

    @Test
    public void stringValueStringObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Integer.MIN_VALUE), view.stringValue("neg int val0").get());
    }

    @Test
    public void stringValueStringObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Long.MAX_VALUE), view.stringValue("pos long val0").get());
    }

    @Test
    public void stringValueStringObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Long.MIN_VALUE), view.stringValue("neg long val0").get());
    }

    @Test
    public void stringValueStringObject_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("9223372036854775808", view.stringValue("pos int >64-bits val0").get());
    }

    @Test
    public void stringValueStringObject_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("-9223372036854775809", view.stringValue("neg int >64-bits val0").get());
    }

    @Test
    public void stringValueStringObject_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Float.MAX_VALUE), view.stringValue("pos float val0").get());
    }

    @Test
    public void stringValueStringObject_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(-Float.MAX_VALUE), view.stringValue("neg float val0").get());
    }

    @Test
    public void stringValueStringObject_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Double.MAX_VALUE), view.stringValue("pos double val0").get());
    }

    @Test
    public void stringValueStringObject_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(-Double.MAX_VALUE), view.stringValue("neg double val0").get());
    }

    @Test
    public void stringValueStringObject_TrueVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("true", view.stringValue("true val0").get());
    }

    @Test
    public void stringValueStringObject_FalseVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("false", view.stringValue("false val0").get());
    }

    @Test
    public void stringValueStringObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue("object val0").isPresent());
    }

    @Test
    public void stringValueStringObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue("array val0").isPresent());
    }

    @Test
    public void stringValueStringArray_KeyNegative_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("-1").isPresent());
    }

    @Test
    public void stringValueStringArray_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("not present").isPresent());
    }

    @Test
    public void stringValueStringArray_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("object val1").isPresent());
    }

    @Test
    public void stringValueStringArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("15").isPresent());
    }

    @Test
    public void stringValueStringArray_StringVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("hello world", view.stringValue("0").get());
    }

    @Test
    public void stringValueStringArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Integer.MAX_VALUE), view.stringValue("1").get());
    }

    @Test
    public void stringValueStringArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Integer.MIN_VALUE), view.stringValue("2").get());
    }

    @Test
    public void stringValueStringArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Long.MAX_VALUE), view.stringValue("3").get());
    }

    @Test
    public void stringValueStringArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Long.MIN_VALUE), view.stringValue("4").get());
    }

    @Test
    public void stringValueStringArray_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("9223372036854775808", view.stringValue("5").get());
    }

    @Test
    public void stringValueStringArray_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("-9223372036854775809", view.stringValue("6").get());
    }

    @Test
    public void stringValueStringArray_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Float.MAX_VALUE), view.stringValue("7").get());
    }

    @Test
    public void stringValueStringArray_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(-Float.MAX_VALUE), view.stringValue("8").get());
    }

    @Test
    public void stringValueStringArray_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Double.MAX_VALUE), view.stringValue("9").get());
    }

    @Test
    public void stringValueStringArray_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(-Double.MAX_VALUE), view.stringValue("10").get());
    }

    @Test
    public void stringValueStringArray_TrueVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("true", view.stringValue("11").get());
    }

    @Test
    public void stringValueStringArray_FalseVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("false", view.stringValue("12").get());
    }

    @Test
    public void stringValueStringArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("13").isPresent());
    }

    @Test
    public void stringValueStringArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("14").isPresent());
    }

    @Test
    public void stringValueIndexObject_StringVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("hello world", view.stringValue(13).get());
    }

    @Test
    public void stringValueIndexObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Integer.MAX_VALUE), view.stringValue(11).get());
    }

    @Test
    public void stringValueIndexObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Integer.MIN_VALUE), view.stringValue(5).get());
    }

    @Test
    public void stringValueIndexObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Long.MAX_VALUE), view.stringValue(12).get());
    }

    @Test
    public void stringValueIndexObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Long.MIN_VALUE), view.stringValue(6).get());
    }

    @Test
    public void stringValueIndexObject_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("9223372036854775808", view.stringValue(10).get());
    }

    @Test
    public void stringValueIndexObject_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("-9223372036854775809", view.stringValue(4).get());
    }

    @Test
    public void stringValueIndexObject_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Float.MAX_VALUE), view.stringValue(9).get());
    }

    @Test
    public void stringValueIndexObject_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(-Float.MAX_VALUE), view.stringValue(3).get());
    }

    @Test
    public void stringValueIndexObject_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Double.MAX_VALUE), view.stringValue(8).get());
    }

    @Test
    public void stringValueIndexObject_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(-Double.MAX_VALUE), view.stringValue(2).get());
    }

    @Test
    public void stringValueIndexObject_TrueVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("true", view.stringValue(14).get());
    }

    @Test
    public void stringValueIndexObject_FalseVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("false", view.stringValue(1).get());
    }

    @Test
    public void stringValueIndexObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue(7).isPresent());
    }

    @Test
    public void stringValueIndexObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue(0).isPresent());
    }

    @Test
    public void stringValueIndexArray_KeyNegative_NegativeKeyIndexException() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        expectedException.expect(NegativeKeyIndexException.class);
        view.stringValue(-1);
    }

    @Test
    public void stringValueIndexArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue(15).isPresent());
    }

    @Test
    public void stringValueIndexArray_StringVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("hello world", view.stringValue(0).get());
    }

    @Test
    public void stringValueIndexArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Integer.MAX_VALUE), view.stringValue(1).get());
    }

    @Test
    public void stringValueIndexArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Integer.MIN_VALUE), view.stringValue(2).get());
    }

    @Test
    public void stringValueIndexArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Long.MAX_VALUE), view.stringValue(3).get());
    }

    @Test
    public void stringValueIndexArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Long.MIN_VALUE), view.stringValue(4).get());
    }

    @Test
    public void stringValueIndexArray_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("9223372036854775808", view.stringValue(5).get());
    }

    @Test
    public void stringValueIndexArray_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("-9223372036854775809", view.stringValue(6).get());
    }

    @Test
    public void stringValueIndexArray_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Float.MAX_VALUE), view.stringValue(7).get());
    }

    @Test
    public void stringValueIndexArray_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(-Float.MAX_VALUE), view.stringValue(8).get());
    }

    @Test
    public void stringValueIndexArray_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Double.MAX_VALUE), view.stringValue(9).get());
    }

    @Test
    public void stringValueIndexArray_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(-Double.MAX_VALUE), view.stringValue(10).get());
    }

    @Test
    public void stringValueIndexArray_TrueVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("true", view.stringValue(11).get());
    }

    @Test
    public void stringValueIndexArray_FalseVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("false", view.stringValue(12).get());
    }

    @Test
    public void stringValueIndexArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue(13).isPresent());
    }

    @Test
    public void stringValueIndexArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue(14).isPresent());
    }

    @Test
    public void integerValueStringObject_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("not present").isPresent());
    }

    @Test
    public void integerValueStringObject_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("object val1").isPresent());
    }

    @Test
    public void integerValueStringObject_NullVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("null val0").isPresent());
    }

    @Test
    public void integerValueStringObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("string val0").isPresent());
    }

    @Test
    public void integerValueStringObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MAX_VALUE, (int) view.integerValue("pos int val0").get());
    }

    @Test
    public void integerValueStringObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MIN_VALUE, (int) view.integerValue("neg int val0").get());
    }

    @Test
    public void integerValueStringObject_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("pos long val0").isPresent());
    }

    @Test
    public void integerValueStringObject_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("neg long val0").isPresent());
    }

    @Test
    public void integerValueStringObject_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("pos int >64-bits val0").isPresent());
    }

    @Test
    public void integerValueStringObject_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("neg int >64-bits val0").isPresent());
    }

    @Test
    public void integerValueStringObject_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("pos float val0").isPresent());
    }

    @Test
    public void integerValueStringObject_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("neg float val0").isPresent());
    }

    @Test
    public void integerValueStringObject_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("pos double val0").isPresent());
    }

    @Test
    public void integerValueStringObject_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("neg double val0").isPresent());
    }

    @Test
    public void integerValueStringObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("true val0").isPresent());
    }

    @Test
    public void integerValueStringObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("false val0").isPresent());
    }

    @Test
    public void integerValueStringObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("object val0").isPresent());
    }

    @Test
    public void integerValueStringObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue("array val0").isPresent());
    }

    @Test
    public void integerValueStringArray_KeyNegative_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("-1").isPresent());
    }

    @Test
    public void integerValueStringArray_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("not present").isPresent());
    }

    @Test
    public void integerValueStringArray_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("object val1").isPresent());
    }

    @Test
    public void integerValueStringArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("15").isPresent());
    }

    @Test
    public void integerValueStringArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("0").isPresent());
    }

    @Test
    public void integerValueStringArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MAX_VALUE, (int) view.integerValue("1").get());
    }

    @Test
    public void integerValueStringArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MIN_VALUE, (int) view.integerValue("2").get());
    }

    @Test
    public void integerValueStringArray_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("3").isPresent());
    }

    @Test
    public void integerValueStringArray_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("4").isPresent());
    }

    @Test
    public void integerValueStringArray_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("5").isPresent());
    }

    @Test
    public void integerValueStringArray_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("6").isPresent());
    }

    @Test
    public void integerValueStringArray_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("7").isPresent());
    }

    @Test
    public void integerValueStringArray_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("8").isPresent());
    }

    @Test
    public void integerValueStringArray_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("9").isPresent());
    }

    @Test
    public void integerValueStringArray_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("10").isPresent());
    }

    @Test
    public void integerValueStringArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("11").isPresent());
    }

    @Test
    public void integerValueStringArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("12").isPresent());
    }

    @Test
    public void integerValueStringArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("13").isPresent());
    }

    @Test
    public void integerValueStringArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue("14").isPresent());
    }

    @Test
    public void integerValueIndexObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(13).isPresent());
    }

    @Test
    public void integerValueIndexObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MAX_VALUE, (int) view.integerValue(11).get());
    }

    @Test
    public void integerValueIndexObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MIN_VALUE, (int) view.integerValue(5).get());
    }

    @Test
    public void integerValueIndexObject_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(12).isPresent());
    }

    @Test
    public void integerValueIndexObject_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(6).isPresent());
    }

    @Test
    public void integerValueIndexObject_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(10).isPresent());
    }

    @Test
    public void integerValueIndexObject_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(4).isPresent());
    }

    @Test
    public void integerValueIndexObject_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(9).isPresent());
    }

    @Test
    public void integerValueIndexObject_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(3).isPresent());
    }

    @Test
    public void integerValueIndexObject_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(8).isPresent());
    }

    @Test
    public void integerValueIndexObject_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(2).isPresent());
    }

    @Test
    public void integerValueIndexObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(14).isPresent());
    }

    @Test
    public void integerValueIndexObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(1).isPresent());
    }

    @Test
    public void integerValueIndexObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(7).isPresent());
    }

    @Test
    public void integerValueIndexObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.integerValue(0).isPresent());
    }

    @Test
    public void integerValueIndexArray_KeyNegative_NegativeKeyIndexException() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        expectedException.expect(NegativeKeyIndexException.class);
        view.integerValue(-1);
    }

    @Test
    public void integerValueIndexArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(15).isPresent());
    }

    @Test
    public void integerValueIndexArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(0).isPresent());
    }

    @Test
    public void integerValueIndexArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MAX_VALUE, (int) view.integerValue(1).get());
    }

    @Test
    public void integerValueIndexArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MIN_VALUE, (int) view.integerValue(2).get());
    }

    @Test
    public void integerValueIndexArray_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(3).isPresent());
    }

    @Test
    public void integerValueIndexArray_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(4).isPresent());
    }

    @Test
    public void integerValueIndexArray_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(5).isPresent());
    }

    @Test
    public void integerValueIndexArray_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(6).isPresent());
    }

    @Test
    public void integerValueIndexArray_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(7).isPresent());
    }

    @Test
    public void integerValueIndexArray_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(8).isPresent());
    }

    @Test
    public void integerValueIndexArray_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(9).isPresent());
    }

    @Test
    public void integerValueIndexArray_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(10).isPresent());
    }

    @Test
    public void integerValueIndexArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(11).isPresent());
    }

    @Test
    public void integerValueIndexArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(12).isPresent());
    }

    @Test
    public void integerValueIndexArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(13).isPresent());
    }

    @Test
    public void integerValueIndexArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.integerValue(14).isPresent());
    }

    @Test
    public void longValueStringObject_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("not present").isPresent());
    }

    @Test
    public void longValueStringObject_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("object val1").isPresent());
    }

    @Test
    public void longValueStringObject_NullVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("null val0").isPresent());
    }

    @Test
    public void longValueStringObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("string val0").isPresent());
    }

    @Test
    public void longValueStringObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MAX_VALUE, (long) view.longValue("pos int val0").get());
    }

    @Test
    public void longValueStringObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MIN_VALUE, (long) view.longValue("neg int val0").get());
    }

    @Test
    public void longValueStringObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Long.MAX_VALUE, (long) view.longValue("pos long val0").get());
    }

    @Test
    public void longValueStringObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Long.MIN_VALUE, (long) view.longValue("neg long val0").get());
    }

    @Test
    public void longValueStringObject_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("pos int >64-bits val0").isPresent());
    }

    @Test
    public void longValueStringObject_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("neg int >64-bits val0").isPresent());
    }

    @Test
    public void longValueStringObject_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("pos float val0").isPresent());
    }

    @Test
    public void longValueStringObject_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("neg float val0").isPresent());
    }

    @Test
    public void longValueStringObject_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("pos double val0").isPresent());
    }

    @Test
    public void longValueStringObject_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("neg double val0").isPresent());
    }

    @Test
    public void longValueStringObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("true val0").isPresent());
    }

    @Test
    public void longValueStringObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("false val0").isPresent());
    }

    @Test
    public void longValueStringObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("object val0").isPresent());
    }

    @Test
    public void longValueStringObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue("array val0").isPresent());
    }

    @Test
    public void longValueStringArray_KeyNegative_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("-1").isPresent());
    }

    @Test
    public void longValueStringArray_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("not present").isPresent());
    }

    @Test
    public void longValueStringArray_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("object val1").isPresent());
    }

    @Test
    public void longValueStringArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("15").isPresent());
    }

    @Test
    public void longValueStringArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("0").isPresent());
    }

    @Test
    public void longValueStringArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MAX_VALUE, (long) view.longValue("1").get());
    }

    @Test
    public void longValueStringArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MIN_VALUE, (long) view.longValue("2").get());
    }

    @Test
    public void longValueStringArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Long.MAX_VALUE, (long) view.longValue("3").get());
    }

    @Test
    public void longValueStringArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Long.MIN_VALUE, (long) view.longValue("4").get());
    }

    @Test
    public void longValueStringArray_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("5").isPresent());
    }

    @Test
    public void longValueStringArray_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("6").isPresent());
    }

    @Test
    public void longValueStringArray_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("7").isPresent());
    }

    @Test
    public void longValueStringArray_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("8").isPresent());
    }

    @Test
    public void longValueStringArray_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("9").isPresent());
    }

    @Test
    public void longValueStringArray_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("10").isPresent());
    }

    @Test
    public void longValueStringArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("11").isPresent());
    }

    @Test
    public void longValueStringArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("12").isPresent());
    }

    @Test
    public void longValueStringArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("13").isPresent());
    }

    @Test
    public void longValueStringArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue("14").isPresent());
    }

    @Test
    public void longValueIndexObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(13).isPresent());
    }

    @Test
    public void longValueIndexObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MAX_VALUE, (long) view.longValue(11).get());
    }

    @Test
    public void longValueIndexObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MIN_VALUE, (long) view.longValue(5).get());
    }

    @Test
    public void longValueIndexObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Long.MAX_VALUE, (long) view.longValue(12).get());
    }

    @Test
    public void longValueIndexObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Long.MIN_VALUE, (long) view.longValue(6).get());
    }

    @Test
    public void longValueIndexObject_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(10).isPresent());
    }

    @Test
    public void longValueIndexObject_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(4).isPresent());
    }

    @Test
    public void longValueIndexObject_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(9).isPresent());
    }

    @Test
    public void longValueIndexObject_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(3).isPresent());
    }

    @Test
    public void longValueIndexObject_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(8).isPresent());
    }

    @Test
    public void longValueIndexObject_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(2).isPresent());
    }

    @Test
    public void longValueIndexObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(14).isPresent());
    }

    @Test
    public void longValueIndexObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(1).isPresent());
    }

    @Test
    public void longValueIndexObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(7).isPresent());
    }

    @Test
    public void longValueIndexObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.longValue(0).isPresent());
    }

    @Test
    public void longValueIndexArray_KeyNegative_NegativeKeyIndexException() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        expectedException.expect(NegativeKeyIndexException.class);
        view.longValue(-1);
    }

    @Test
    public void longValueIndexArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(15).isPresent());
    }

    @Test
    public void longValueIndexArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(0).isPresent());
    }

    @Test
    public void longValueIndexArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MAX_VALUE, (long) view.longValue(1).get());
    }

    @Test
    public void longValueIndexArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MIN_VALUE, (long) view.longValue(2).get());
    }

    @Test
    public void longValueIndexArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Long.MAX_VALUE, (long) view.longValue(3).get());
    }

    @Test
    public void longValueIndexArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Long.MIN_VALUE, (long) view.longValue(4).get());
    }

    @Test
    public void longValueIndexArray_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(5).isPresent());
    }

    @Test
    public void longValueIndexArray_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(6).isPresent());
    }

    @Test
    public void longValueIndexArray_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(7).isPresent());
    }

    @Test
    public void longValueIndexArray_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(8).isPresent());
    }

    @Test
    public void longValueIndexArray_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(9).isPresent());
    }

    @Test
    public void longValueIndexArray_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(10).isPresent());
    }

    @Test
    public void longValueIndexArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(11).isPresent());
    }

    @Test
    public void longValueIndexArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(12).isPresent());
    }

    @Test
    public void longValueIndexArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(13).isPresent());
    }

    @Test
    public void longValueIndexArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.longValue(14).isPresent());
    }

    @Test
    public void floatValueStringObject_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue("not present").isPresent());
    }

    @Test
    public void floatValueStringObject_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue("object val1").isPresent());
    }

    @Test
    public void floatValueStringObject_NullVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue("null val0").isPresent());
    }

    @Test
    public void floatValueStringObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue("string val0").isPresent());
    }

    @Test
    public void floatValueStringObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals((float) Integer.MAX_VALUE, view.floatValue("pos int val0").get(), 0.000001);
    }

    @Test
    public void floatValueStringObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals((float) Integer.MIN_VALUE, view.floatValue("neg int val0").get(), 0.000001);
    }

    @Test
    public void floatValueStringObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals((float) Long.MAX_VALUE, view.floatValue("pos long val0").get(), 0.000001);
    }

    @Test
    public void floatValueStringObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals((float) Long.MIN_VALUE, view.floatValue("neg long val0").get(), 0.000001);
    }

    @Test
    public void floatValueStringObject_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(9223372036854775808f, view.floatValue("pos int >64-bits val0").get(), 0.000001);
    }

    @Test
    public void floatValueStringObject_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(-9223372036854775809f, view.floatValue("neg int >64-bits val0").get(), 0.000001);
    }

    @Test
    public void floatValueStringObject_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Float.MAX_VALUE, view.floatValue("pos float val0").get(), 0.000001);
    }

    @Test
    public void floatValueStringObject_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(-Float.MAX_VALUE, view.floatValue("neg float val0").get(), 0.000001);
    }

    @Test
    public void floatValueStringObject_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue("pos double val0").isPresent());
    }

    @Test
    public void floatValueStringObject_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue("neg double val0").isPresent());
    }

    @Test
    public void floatValueStringObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue("true val0").isPresent());
    }

    @Test
    public void floatValueStringObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue("false val0").isPresent());
    }

    @Test
    public void floatValueStringObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue("object val0").isPresent());
    }

    @Test
    public void floatValueStringObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue("array val0").isPresent());
    }

    @Test
    public void floatValueStringArray_KeyNegative_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("-1").isPresent());
    }

    @Test
    public void floatValueStringArray_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("not present").isPresent());
    }

    @Test
    public void floatValueStringArray_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("object val1").isPresent());
    }

    @Test
    public void floatValueStringArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("15").isPresent());
    }

    @Test
    public void floatValueStringArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("0").isPresent());
    }

    @Test
    public void floatValueStringArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals((float) Integer.MAX_VALUE, view.floatValue("1").get(), 0.000001);
    }

    @Test
    public void floatValueStringArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals((float) Integer.MIN_VALUE, view.floatValue("2").get(), 0.000001);
    }

    @Test
    public void floatValueStringArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals((float) Long.MAX_VALUE, view.floatValue("3").get(), 0.000001);
    }

    @Test
    public void floatValueStringArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals((float) Long.MIN_VALUE, view.floatValue("4").get(), 0.000001);
    }

    @Test
    public void floatValueStringArray_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(9223372036854775808f, view.floatValue("5").get(), 0.000001);
    }

    @Test
    public void floatValueStringArray_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(-9223372036854775809f, view.floatValue("6").get(), 0.000001);
    }

    @Test
    public void floatValueStringArray_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Float.MAX_VALUE, view.floatValue("7").get(), 0.000001);
    }

    @Test
    public void floatValueStringArray_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(-Float.MAX_VALUE, view.floatValue("8").get(), 0.000001);
    }

    @Test
    public void floatValueStringArray_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("9").isPresent());
    }

    @Test
    public void floatValueStringArray_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("10").isPresent());
    }

    @Test
    public void floatValueStringArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("11").isPresent());
    }

    @Test
    public void floatValueStringArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("12").isPresent());
    }

    @Test
    public void floatValueStringArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("13").isPresent());
    }

    @Test
    public void floatValueStringArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue("14").isPresent());
    }

    @Test
    public void floatValueIndexObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue(13).isPresent());
    }

    @Test
    public void floatValueIndexObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals((float) Integer.MAX_VALUE, view.floatValue(11).get(), 0.000001);
    }

    @Test
    public void floatValueIndexObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals((float) Integer.MIN_VALUE, view.floatValue(5).get(), 0.000001);
    }

    @Test
    public void floatValueIndexObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals((float) Long.MAX_VALUE, view.floatValue(12).get(), 0.000001);
    }

    @Test
    public void floatValueIndexObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals((float) Long.MIN_VALUE, view.floatValue(6).get(), 0.000001);
    }

    @Test
    public void floatValueIndexObject_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(9223372036854775808f, view.floatValue(10).get(), 0.000001);
    }

    @Test
    public void floatValueIndexObject_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(-9223372036854775809f, view.floatValue(4).get(), 0.000001);
    }

    @Test
    public void floatValueIndexObject_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Float.MAX_VALUE, view.floatValue(9).get(), 0.000001);
    }

    @Test
    public void floatValueIndexObject_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(-Float.MAX_VALUE, view.floatValue(3).get(), 0.000001);
    }

    @Test
    public void floatValueIndexObject_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue(8).isPresent());
    }

    @Test
    public void floatValueIndexObject_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue(2).isPresent());
    }

    @Test
    public void floatValueIndexObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue(14).isPresent());
    }

    @Test
    public void floatValueIndexObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue(1).isPresent());
    }

    @Test
    public void floatValueIndexObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue(7).isPresent());
    }

    @Test
    public void floatValueIndexObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.floatValue(0).isPresent());
    }

    @Test
    public void floatValueIndexArray_KeyNegative_NegativeKeyIndexException() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        expectedException.expect(NegativeKeyIndexException.class);
        view.floatValue(-1);
    }

    @Test
    public void floatValueIndexArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue(15).isPresent());
    }

    @Test
    public void floatValueIndexArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue(0).isPresent());
    }

    @Test
    public void floatValueIndexArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals((float) Integer.MAX_VALUE, view.floatValue(1).get(), 0.000001);
    }

    @Test
    public void floatValueIndexArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals((float) Integer.MIN_VALUE, view.floatValue(2).get(), 0.000001);
    }

    @Test
    public void floatValueIndexArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals((float) Long.MAX_VALUE, view.floatValue(3).get(), 0.000001);
    }

    @Test
    public void floatValueIndexArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals((float) Long.MIN_VALUE, view.floatValue(4).get(), 0.000001);
    }

    @Test
    public void floatValueIndexArray_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(9223372036854775808f, view.floatValue(5).get(), 0.000001);
    }

    @Test
    public void floatValueIndexArray_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(-9223372036854775809f, view.floatValue(6).get(), 0.000001);
    }

    @Test
    public void floatValueIndexArray_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Float.MAX_VALUE, view.floatValue(7).get(), 0.000001);
    }

    @Test
    public void floatValueIndexArray_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(-Float.MAX_VALUE, view.floatValue(8).get(), 0.000001);
    }

    @Test
    public void floatValueIndexArray_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue(9).isPresent());
    }

    @Test
    public void floatValueIndexArray_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue(10).isPresent());
    }

    @Test
    public void floatValueIndexArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue(11).isPresent());
    }

    @Test
    public void floatValueIndexArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue(12).isPresent());
    }

    @Test
    public void floatValueIndexArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue(13).isPresent());
    }

    @Test
    public void floatValueIndexArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.floatValue(14).isPresent());
    }

    @Test
    public void doubleValueStringObject_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue("not present").isPresent());
    }

    @Test
    public void doubleValueStringObject_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue("object val1").isPresent());
    }

    @Test
    public void doubleValueStringObject_NullVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue("null val0").isPresent());
    }

    @Test
    public void doubleValueStringObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue("string val0").isPresent());
    }

    @Test
    public void doubleValueStringObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MAX_VALUE, view.doubleValue("pos int val0").get(), 0.000001);
    }

    @Test
    public void doubleValueStringObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MIN_VALUE, view.doubleValue("neg int val0").get(), 0.000001);
    }

    @Test
    public void doubleValueStringObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Long.MAX_VALUE, view.doubleValue("pos long val0").get(), 0.000001);
    }

    @Test
    public void doubleValueStringObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Long.MIN_VALUE, view.doubleValue("neg long val0").get(), 0.000001);
    }

    @Test
    public void doubleValueStringObject_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(9223372036854775808d, view.doubleValue("pos int >64-bits val0").get(), 0.000001);
    }

    @Test
    public void doubleValueStringObject_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(-9223372036854775809d, view.doubleValue("neg int >64-bits val0").get(), 0.000001);
    }

    @Test
    public void doubleValueStringObject_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Float.MAX_VALUE, view.doubleValue("pos float val0").get(), 0.000001);
    }

    @Test
    public void doubleValueStringObject_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(-Float.MAX_VALUE, view.doubleValue("neg float val0").get(), 0.000001);
    }

    @Test
    public void doubleValueStringObject_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Double.MAX_VALUE, view.doubleValue("pos double val0").get(), 0.000001);
    }

    @Test
    public void doubleValueStringObject_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(-Double.MAX_VALUE, view.doubleValue("neg double val0").get(), 0.000001);
    }

    @Test
    public void doubleValueStringObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue("true val0").isPresent());
    }

    @Test
    public void doubleValueStringObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue("false val0").isPresent());
    }

    @Test
    public void doubleValueStringObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue("object val0").isPresent());
    }

    @Test
    public void doubleValueStringObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue("array val0").isPresent());
    }

    @Test
    public void doubleValueStringArray_KeyNegative_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue("-1").isPresent());
    }

    @Test
    public void doubleValueStringArray_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue("not present").isPresent());
    }

    @Test
    public void doubleValueStringArray_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue("object val1").isPresent());
    }

    @Test
    public void doubleValueStringArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue("15").isPresent());
    }

    @Test
    public void doubleValueStringArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue("0").isPresent());
    }

    @Test
    public void doubleValueStringArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MAX_VALUE, view.doubleValue("1").get(), 0.000001);
    }

    @Test
    public void doubleValueStringArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MIN_VALUE, view.doubleValue("2").get(), 0.000001);
    }

    @Test
    public void doubleValueStringArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Long.MAX_VALUE, view.doubleValue("3").get(), 0.000001);
    }

    @Test
    public void doubleValueStringArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Long.MIN_VALUE, view.doubleValue("4").get(), 0.000001);
    }

    @Test
    public void doubleValueStringArray_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(9223372036854775808d, view.doubleValue("5").get(), 0.000001);
    }

    @Test
    public void doubleValueStringArray_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(-9223372036854775809d, view.doubleValue("6").get(), 0.000001);
    }

    @Test
    public void doubleValueStringArray_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Float.MAX_VALUE, view.doubleValue("7").get(), 0.000001);
    }

    @Test
    public void doubleValueStringArray_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(-Float.MAX_VALUE, view.doubleValue("8").get(), 0.000001);
    }

    @Test
    public void doubleValueStringArray_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Double.MAX_VALUE, view.doubleValue("9").get(), 0.000001);
    }

    @Test
    public void doubleValueStringArray_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(-Double.MAX_VALUE, view.doubleValue("10").get(), 0.000001);
    }

    @Test
    public void doubleValueStringArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue("11").isPresent());
    }

    @Test
    public void doubleValueStringArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue("12").isPresent());
    }

    @Test
    public void doubleValueStringArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue("13").isPresent());
    }

    @Test
    public void doubleValueStringArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue("14").isPresent());
    }

    @Test
    public void doubleValueIndexObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue(13).isPresent());
    }

    @Test
    public void doubleValueIndexObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MAX_VALUE, view.doubleValue(11).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Integer.MIN_VALUE, view.doubleValue(5).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Long.MAX_VALUE, view.doubleValue(12).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Long.MIN_VALUE, view.doubleValue(6).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexObject_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(9223372036854775808d, view.doubleValue(10).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexObject_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(-9223372036854775809d, view.doubleValue(4).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexObject_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Float.MAX_VALUE, view.doubleValue(9).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexObject_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(-Float.MAX_VALUE, view.doubleValue(3).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexObject_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(Double.MAX_VALUE, view.doubleValue(8).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexObject_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(-Double.MAX_VALUE, view.doubleValue(2).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue(14).isPresent());
    }

    @Test
    public void doubleValueIndexObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue(1).isPresent());
    }

    @Test
    public void doubleValueIndexObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue(7).isPresent());
    }

    @Test
    public void doubleValueIndexObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.doubleValue(0).isPresent());
    }

    @Test
    public void doubleValueIndexArray_KeyNegative_NegativeKeyIndexException() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        expectedException.expect(NegativeKeyIndexException.class);
        view.doubleValue(-1);
    }

    @Test
    public void doubleValueIndexArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue(15).isPresent());
    }

    @Test
    public void doubleValueIndexArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue(0).isPresent());
    }

    @Test
    public void doubleValueIndexArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MAX_VALUE, view.doubleValue(1).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Integer.MIN_VALUE, view.doubleValue(2).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Long.MAX_VALUE, view.doubleValue(3).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Long.MIN_VALUE, view.doubleValue(4).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexArray_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(9223372036854775808d, view.doubleValue(5).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexArray_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(-9223372036854775809d, view.doubleValue(6).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexArray_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Float.MAX_VALUE, view.doubleValue(7).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexArray_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(-Float.MAX_VALUE, view.doubleValue(8).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexArray_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(Double.MAX_VALUE, view.doubleValue(9).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexArray_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(-Double.MAX_VALUE, view.doubleValue(10).get(), 0.000001);
    }

    @Test
    public void doubleValueIndexArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue(11).isPresent());
    }

    @Test
    public void doubleValueIndexArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue(12).isPresent());
    }

    @Test
    public void doubleValueIndexArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue(13).isPresent());
    }

    @Test
    public void doubleValueIndexArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.doubleValue(14).isPresent());
    }

    @Test
    public void booleanValueStringObject_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("not present").isPresent());
    }

    @Test
    public void booleanValueStringObject_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("object val1").isPresent());
    }

    @Test
    public void booleanValueStringObject_NullVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("null val0").isPresent());
    }

    @Test
    public void booleanValueStringObject_StringVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("string val0").get());
    }

    @Test
    public void booleanValueStringObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("pos int val0").get());
    }

    @Test
    public void booleanValueStringObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("neg int val0").get());
    }

    @Test
    public void booleanValueStringObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("pos long val0").get());
    }

    @Test
    public void booleanValueStringObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("neg long val0").get());
    }

    @Test
    public void booleanValueStringObject_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("pos int >64-bits val0").get());
    }

    @Test
    public void booleanValueStringObject_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("neg int >64-bits val0").get());
    }

    @Test
    public void booleanValueStringObject_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("pos float val0").get());
    }

    @Test
    public void booleanValueStringObject_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("neg float val0").get());
    }

    @Test
    public void booleanValueStringObject_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("pos double val0").get());
    }

    @Test
    public void booleanValueStringObject_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("neg double val0").get());
    }

    @Test
    public void booleanValueStringObject_TrueVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.booleanValue("true val0").get());
    }

    @Test
    public void booleanValueStringObject_FalseVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("false val0").get());
    }

    @Test
    public void booleanValueStringObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("object val0").isPresent());
    }

    @Test
    public void booleanValueStringObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue("array val0").isPresent());
    }

    @Test
    public void booleanValueStringArray_KeyNegative_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("-1").isPresent());
    }

    @Test
    public void booleanValueStringArray_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("not present").isPresent());
    }

    @Test
    public void booleanValueStringArray_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("object val1").isPresent());
    }

    @Test
    public void booleanValueStringArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("15").isPresent());
    }

    @Test
    public void booleanValueStringArray_StringVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("0").get());
    }

    @Test
    public void booleanValueStringArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("1").get());
    }

    @Test
    public void booleanValueStringArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("2").get());
    }

    @Test
    public void booleanValueStringArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("3").get());
    }

    @Test
    public void booleanValueStringArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("4").get());
    }

    @Test
    public void booleanValueStringArray_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("5").get());
    }

    @Test
    public void booleanValueStringArray_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("6").get());
    }

    @Test
    public void booleanValueStringArray_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("7").get());
    }

    @Test
    public void booleanValueStringArray_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("8").get());
    }

    @Test
    public void booleanValueStringArray_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("9").get());
    }

    @Test
    public void booleanValueStringArray_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("10").get());
    }

    @Test
    public void booleanValueStringArray_TrueVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.booleanValue("11").get());
    }

    @Test
    public void booleanValueStringArray_FalseVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("12").get());
    }

    @Test
    public void booleanValueStringArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("13").isPresent());
    }

    @Test
    public void booleanValueStringArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue("14").isPresent());
    }

    @Test
    public void booleanValueIndexObject_StringVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(13).get());
    }

    @Test
    public void booleanValueIndexObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(11).get());
    }

    @Test
    public void booleanValueIndexObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(5).get());
    }

    @Test
    public void booleanValueIndexObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(12).get());
    }

    @Test
    public void booleanValueIndexObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(6).get());
    }

    @Test
    public void booleanValueIndexObject_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(10).get());
    }

    @Test
    public void booleanValueIndexObject_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(4).get());
    }

    @Test
    public void booleanValueIndexObject_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(9).get());
    }

    @Test
    public void booleanValueIndexObject_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(3).get());
    }

    @Test
    public void booleanValueIndexObject_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(8).get());
    }

    @Test
    public void booleanValueIndexObject_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(2).get());
    }

    @Test
    public void booleanValueIndexObject_TrueVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.booleanValue(14).get());
    }

    @Test
    public void booleanValueIndexObject_FalseVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(1).get());
    }

    @Test
    public void booleanValueIndexObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(7).isPresent());
    }

    @Test
    public void booleanValueIndexObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.booleanValue(0).isPresent());
    }

    @Test
    public void booleanValueIndexArray_KeyNegative_NegativeKeyIndexException() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        expectedException.expect(NegativeKeyIndexException.class);
        view.booleanValue(-1);
    }

    @Test
    public void booleanValueIndexArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(15).isPresent());
    }

    @Test
    public void booleanValueIndexArray_StringVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(0).get());
    }

    @Test
    public void booleanValueIndexArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(1).get());
    }

    @Test
    public void booleanValueIndexArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(2).get());
    }

    @Test
    public void booleanValueIndexArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(3).get());
    }

    @Test
    public void booleanValueIndexArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(4).get());
    }

    @Test
    public void booleanValueIndexArray_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(5).get());
    }

    @Test
    public void booleanValueIndexArray_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(6).get());
    }

    @Test
    public void booleanValueIndexArray_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(7).get());
    }

    @Test
    public void booleanValueIndexArray_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(8).get());
    }

    @Test
    public void booleanValueIndexArray_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(9).get());
    }

    @Test
    public void booleanValueIndexArray_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(10).get());
    }

    @Test
    public void booleanValueIndexArray_TrueVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.booleanValue(11).get());
    }

    @Test
    public void booleanValueIndexArray_FalseVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(12).get());
    }

    @Test
    public void booleanValueIndexArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(13).isPresent());
    }

    @Test
    public void booleanValueIndexArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.booleanValue(14).isPresent());
    }

    @Test
    public void booleanValueStringObject_TrueString_CaseInsensitive() {
        JsonObject root = new JsonObject();
        root.add("lowercase", new JsonPrimitive("true"));
        root.add("uppercase", new JsonPrimitive("TRUE"));
        root.add("mixed case", new JsonPrimitive("TrUe"));
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertTrue(view.booleanValue("lowercase").get());
        assertTrue(view.booleanValue("uppercase").get());
        assertTrue(view.booleanValue("mixed case").get());
    }

    @Test
    public void booleanValueStringArray_TrueString_CaseInsensitive() {
        JsonArray root = new JsonArray();
        root.add(new JsonPrimitive("true"));
        root.add(new JsonPrimitive("TRUE"));
        root.add(new JsonPrimitive("TrUe"));
        JsonMetadataView view = new JsonMetadataView(root);
        assertTrue(view.booleanValue("0").get());
        assertTrue(view.booleanValue("1").get());
        assertTrue(view.booleanValue("2").get());
    }

    @Test
    public void booleanValueIndexObject_TrueString_CaseInsensitive() {
        JsonObject root = new JsonObject();
        root.add("lowercase", new JsonPrimitive("true"));
        root.add("uppercase", new JsonPrimitive("TRUE"));
        root.add("mixed case", new JsonPrimitive("TrUe"));
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertTrue(view.booleanValue(0).get());
        assertTrue(view.booleanValue(2).get());
        assertTrue(view.booleanValue(1).get());
    }

    @Test
    public void booleanValueIndexArray_TrueString_CaseInsensitive() {
        JsonArray root = new JsonArray();
        root.add(new JsonPrimitive("true"));
        root.add(new JsonPrimitive("TRUE"));
        root.add(new JsonPrimitive("TrUe"));
        JsonMetadataView view = new JsonMetadataView(root);
        assertTrue(view.booleanValue(0).get());
        assertTrue(view.booleanValue(1).get());
        assertTrue(view.booleanValue(2).get());
    }
    @Test
    public void subViewStringObject_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("not present").isPresent());
    }

    @Test
    public void subViewStringObject_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("object val1").isPresent());
    }

    @Test
    public void subViewStringObject_NullVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("null val0").isPresent());
    }

    @Test
    public void subViewStringObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("string val0").isPresent());
    }

    @Test
    public void subViewStringObject_PosIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("pos int val0").isPresent());
    }

    @Test
    public void subViewStringObject_NegIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("neg int val0").isPresent());
    }

    @Test
    public void subViewStringObject_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("pos long val0").isPresent());
    }

    @Test
    public void subViewStringObject_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("neg long val0").isPresent());
    }

    @Test
    public void subViewStringObject_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("pos int >64-bits val0").isPresent());
    }

    @Test
    public void subViewStringObject_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("neg int >64-bits val0").isPresent());
    }

    @Test
    public void subViewStringObject_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("pos float val0").isPresent());
    }

    @Test
    public void subViewStringObject_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("neg float val0").isPresent());
    }

    @Test
    public void subViewStringObject_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("pos double val0").isPresent());
    }

    @Test
    public void subViewStringObject_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("neg double val0").isPresent());
    }

    @Test
    public void subViewStringObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("true val0").isPresent());
    }

    @Test
    public void subViewStringObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView("false val0").isPresent());
    }

    @Test
    public void subViewStringObject_ObjectVal_ViewFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(
                ImmutableSet.of("string val1", "pos int val1", "neg int val1", "pos long val1", "neg long val1",
                        "pos int >64-bits val1", "neg int >64-bits val1", "pos float val1", "neg float val1",
                        "pos double val1", "neg double val1", "true val1", "false val1", "object val1", "array val1"),
                new HashSet<>(collectKeys(view.subView("object val0").get().keys()))
        );
    }

    @Test
    public void subViewStringObject_ArrayVal_ViewFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(
                IntStream.rangeClosed(0, 14).mapToObj(String::valueOf).collect(Collectors.toSet()),
                new HashSet<>(collectKeys(view.subView("array val0").get().keys()))
        );
    }

    @Test
    public void subViewStringArray_KeyNegative_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("-1").isPresent());
    }

    @Test
    public void subViewStringArray_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("not present").isPresent());
    }

    @Test
    public void subViewStringArray_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("object val1").isPresent());
    }

    @Test
    public void subViewStringArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("15").isPresent());
    }

    @Test
    public void subViewStringArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("0").isPresent());
    }

    @Test
    public void subViewStringArray_PosIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("1").isPresent());
    }

    @Test
    public void subViewStringArray_NegIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("2").isPresent());
    }

    @Test
    public void subViewStringArray_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("3").isPresent());
    }

    @Test
    public void subViewStringArray_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("4").isPresent());
    }

    @Test
    public void subViewStringArray_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("5").isPresent());
    }

    @Test
    public void subViewStringArray_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("6").isPresent());
    }

    @Test
    public void subViewStringArray_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("7").isPresent());
    }

    @Test
    public void subViewStringArray_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("8").isPresent());
    }

    @Test
    public void subViewStringArray_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("9").isPresent());
    }

    @Test
    public void subViewStringArray_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("10").isPresent());
    }

    @Test
    public void subViewStringArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("11").isPresent());
    }

    @Test
    public void subViewStringArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView("12").isPresent());
    }

    @Test
    public void subViewStringArray_ObjectVal_ViewFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(
                ImmutableSet.of("string val1", "pos int val1", "neg int val1", "pos long val1", "neg long val1",
                        "pos int >64-bits val1", "neg int >64-bits val1", "pos float val1", "neg float val1",
                        "pos double val1", "neg double val1", "true val1", "false val1", "object val1", "array val1"),
                new HashSet<>(collectKeys(view.subView("13").get().keys()))
        );
    }

    @Test
    public void subViewStringArray_ArrayVal_ViewFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(
                IntStream.rangeClosed(0, 14).mapToObj(String::valueOf).collect(Collectors.toSet()),
                new HashSet<>(collectKeys(view.subView("14").get().keys()))
        );
    }

    @Test
    public void subViewIndexObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(13).isPresent());
    }

    @Test
    public void subViewIndexObject_PosIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(11).isPresent());
    }

    @Test
    public void subViewIndexObject_NegIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(5).isPresent());
    }

    @Test
    public void subViewIndexObject_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(12).isPresent());
    }

    @Test
    public void subViewIndexObject_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(6).isPresent());
    }

    @Test
    public void subViewIndexObject_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(10).isPresent());
    }

    @Test
    public void subViewIndexObject_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(4).isPresent());
    }

    @Test
    public void subViewIndexObject_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(9).isPresent());
    }

    @Test
    public void subViewIndexObject_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(3).isPresent());
    }

    @Test
    public void subViewIndexObject_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(8).isPresent());
    }

    @Test
    public void subViewIndexObject_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(2).isPresent());
    }

    @Test
    public void subViewIndexObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(14).isPresent());
    }

    @Test
    public void subViewIndexObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.subView(1).isPresent());
    }

    @Test
    public void subViewIndexObject_ObjectVal_ViewFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(
                ImmutableSet.of("string val1", "pos int val1", "neg int val1", "pos long val1", "neg long val1",
                        "pos int >64-bits val1", "neg int >64-bits val1", "pos float val1", "neg float val1",
                        "pos double val1", "neg double val1", "true val1", "false val1", "object val1", "array val1"),
                new HashSet<>(collectKeys(view.subView(7).get().keys()))
        );
    }

    @Test
    public void subViewIndexObject_ArrayVal_ViewFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(
                IntStream.rangeClosed(0, 14).mapToObj(String::valueOf).collect(Collectors.toSet()),
                new HashSet<>(collectKeys(view.subView(0).get().keys()))
        );
    }

    @Test
    public void subViewIndexArray_KeyNegative_NegativeKeyIndexException() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        expectedException.expect(NegativeKeyIndexException.class);
        view.subView(-1);
    }

    @Test
    public void subViewIndexArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(15).isPresent());
    }

    @Test
    public void subViewIndexArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(0).isPresent());
    }

    @Test
    public void subViewIndexArray_PosIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(1).isPresent());
    }

    @Test
    public void subViewIndexArray_NegIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(2).isPresent());
    }

    @Test
    public void subViewIndexArray_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(3).isPresent());
    }

    @Test
    public void subViewIndexArray_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(4).isPresent());
    }

    @Test
    public void subViewIndexArray_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(5).isPresent());
    }

    @Test
    public void subViewIndexArray_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(6).isPresent());
    }

    @Test
    public void subViewIndexArray_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(7).isPresent());
    }

    @Test
    public void subViewIndexArray_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(8).isPresent());
    }

    @Test
    public void subViewIndexArray_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(9).isPresent());
    }

    @Test
    public void subViewIndexArray_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(10).isPresent());
    }

    @Test
    public void subViewIndexArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(11).isPresent());
    }

    @Test
    public void subViewIndexArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.subView(12).isPresent());
    }

    @Test
    public void subViewIndexArray_ObjectVal_ViewFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(
                ImmutableSet.of("string val1", "pos int val1", "neg int val1", "pos long val1", "neg long val1",
                        "pos int >64-bits val1", "neg int >64-bits val1", "pos float val1", "neg float val1",
                        "pos double val1", "neg double val1", "true val1", "false val1", "object val1", "array val1"),
                new HashSet<>(collectKeys(view.subView(13).get().keys()))
        );
    }

    @Test
    public void subViewIndexArray_ArrayVal_ViewFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(
                IntStream.rangeClosed(0, 14).mapToObj(String::valueOf).collect(Collectors.toSet()),
                new HashSet<>(collectKeys(view.subView(14).get().keys()))
        );
    }

    @Test
    public void streamValueStringObject_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("not present").isPresent());
    }

    @Test
    public void streamValueStringObject_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("object val1").isPresent());
    }

    @Test
    public void streamValueStringObject_NullVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("null val0").isPresent());
    }

    @Test
    public void streamValueStringObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("string val0").isPresent());
    }

    @Test
    public void streamValueStringObject_PosIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("pos int val0").isPresent());
    }

    @Test
    public void streamValueStringObject_NegIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("neg int val0").isPresent());
    }

    @Test
    public void streamValueStringObject_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("pos long val0").isPresent());
    }

    @Test
    public void streamValueStringObject_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("neg long val0").isPresent());
    }

    @Test
    public void streamValueStringObject_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("pos int >64-bits val0").isPresent());
    }

    @Test
    public void streamValueStringObject_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("neg int >64-bits val0").isPresent());
    }

    @Test
    public void streamValueStringObject_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("pos float val0").isPresent());
    }

    @Test
    public void streamValueStringObject_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("neg float val0").isPresent());
    }

    @Test
    public void streamValueStringObject_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("pos double val0").isPresent());
    }

    @Test
    public void streamValueStringObject_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("neg double val0").isPresent());
    }

    @Test
    public void streamValueStringObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("true val0").isPresent());
    }

    @Test
    public void streamValueStringObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("false val0").isPresent());
    }

    @Test
    public void streamValueStringObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("object val0").isPresent());
    }

    @Test
    public void streamValueStringObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue("array val0").isPresent());
    }

    @Test
    public void streamValueStringArray_KeyNegative_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("-1").isPresent());
    }

    @Test
    public void streamValueStringArray_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("not present").isPresent());
    }

    @Test
    public void streamValueStringArray_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("object val1").isPresent());
    }

    @Test
    public void streamValueStringArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("15").isPresent());
    }

    @Test
    public void streamValueStringArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("0").isPresent());
    }

    @Test
    public void streamValueStringArray_PosIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("1").isPresent());
    }

    @Test
    public void streamValueStringArray_NegIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("2").isPresent());
    }

    @Test
    public void streamValueStringArray_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("3").isPresent());
    }

    @Test
    public void streamValueStringArray_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("4").isPresent());
    }

    @Test
    public void streamValueStringArray_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("5").isPresent());
    }

    @Test
    public void streamValueStringArray_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("6").isPresent());
    }

    @Test
    public void streamValueStringArray_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("7").isPresent());
    }

    @Test
    public void streamValueStringArray_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("8").isPresent());
    }

    @Test
    public void streamValueStringArray_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("9").isPresent());
    }

    @Test
    public void streamValueStringArray_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("10").isPresent());
    }

    @Test
    public void streamValueStringArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("11").isPresent());
    }

    @Test
    public void streamValueStringArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("12").isPresent());
    }

    @Test
    public void streamValueStringArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("13").isPresent());
    }

    @Test
    public void streamValueStringArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue("14").isPresent());
    }

    @Test
    public void streamValueIndexObject_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(13).isPresent());
    }

    @Test
    public void streamValueIndexObject_PosIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(11).isPresent());
    }

    @Test
    public void streamValueIndexObject_NegIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(5).isPresent());
    }

    @Test
    public void streamValueIndexObject_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(12).isPresent());
    }

    @Test
    public void streamValueIndexObject_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(6).isPresent());
    }

    @Test
    public void streamValueIndexObject_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(10).isPresent());
    }

    @Test
    public void streamValueIndexObject_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(4).isPresent());
    }

    @Test
    public void streamValueIndexObject_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(9).isPresent());
    }

    @Test
    public void streamValueIndexObject_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(3).isPresent());
    }

    @Test
    public void streamValueIndexObject_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(8).isPresent());
    }

    @Test
    public void streamValueIndexObject_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(2).isPresent());
    }

    @Test
    public void streamValueIndexObject_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(14).isPresent());
    }

    @Test
    public void streamValueIndexObject_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(1).isPresent());
    }

    @Test
    public void streamValueIndexObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(7).isPresent());
    }

    @Test
    public void streamValueIndexObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.byteStreamValue(0).isPresent());
    }

    @Test
    public void streamValueIndexArray_KeyNegative_NegativeKeyIndexException() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        expectedException.expect(NegativeKeyIndexException.class);
        view.byteStreamValue(-1);
    }

    @Test
    public void streamValueIndexArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(15).isPresent());
    }

    @Test
    public void streamValueIndexArray_StringVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(0).isPresent());
    }

    @Test
    public void streamValueIndexArray_PosIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(1).isPresent());
    }

    @Test
    public void streamValueIndexArray_NegIntVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(2).isPresent());
    }

    @Test
    public void streamValueIndexArray_PosLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(3).isPresent());
    }

    @Test
    public void streamValueIndexArray_NegLongVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(4).isPresent());
    }

    @Test
    public void streamValueIndexArray_PosBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(5).isPresent());
    }

    @Test
    public void streamValueIndexArray_NegBeyond64BitsVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(6).isPresent());
    }

    @Test
    public void streamValueIndexArray_PosFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(7).isPresent());
    }

    @Test
    public void streamValueIndexArray_NegFloatVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(8).isPresent());
    }

    @Test
    public void streamValueIndexArray_PosDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(9).isPresent());
    }

    @Test
    public void streamValueIndexArray_NegDoubleVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(10).isPresent());
    }

    @Test
    public void streamValueIndexArray_TrueVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(11).isPresent());
    }

    @Test
    public void streamValueIndexArray_FalseVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(12).isPresent());
    }

    @Test
    public void streamValueIndexArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(13).isPresent());
    }

    @Test
    public void streamValueIndexArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.byteStreamValue(14).isPresent());
    }

    private JsonObject makeDemoObject() {
        JsonObject root = new JsonObject();
        addAllTypeVals(root, 0);
        addAllTypeVals(root.getAsJsonObject("object val0"), 1);
        addAllTypeVals(root.getAsJsonArray("array val0"));
        return root;
    }

    private JsonArray makeDemoArray() {
        JsonArray root = new JsonArray();
        addAllTypeVals(root);
        addAllTypeVals(root.get(14).getAsJsonObject(), 1);
        addAllTypeVals(root.get(15).getAsJsonArray());
        return root;
    }

    private void addAllTypeVals(JsonObject root, int level) {
        root.add("null val" + level, null);
        root.add("string val" + level, new JsonPrimitive("hello world"));
        root.add("pos int val" + level, new JsonPrimitive(Integer.MAX_VALUE));
        root.add("neg int val" + level, new JsonPrimitive(Integer.MIN_VALUE));
        root.add("pos long val" + level, new JsonPrimitive(Long.MAX_VALUE));
        root.add("neg long val" + level, new JsonPrimitive(Long.MIN_VALUE));
        root.add("pos int >64-bits val" + level, new JsonPrimitive(new BigInteger("9223372036854775808")));
        root.add("neg int >64-bits val" + level, new JsonPrimitive(new BigInteger("-9223372036854775809")));
        root.add("pos float val" + level, new JsonPrimitive(Float.MAX_VALUE));
        root.add("neg float val" + level, new JsonPrimitive(-Float.MAX_VALUE));
        root.add("pos double val" + level, new JsonPrimitive(Double.MAX_VALUE));
        root.add("neg double val" + level, new JsonPrimitive(-Double.MAX_VALUE));
        root.add("true val" + level, new JsonPrimitive(true));
        root.add("false val" + level, new JsonPrimitive(false));
        root.add("object val" + level, new JsonObject());
        root.add("array val" + level, new JsonArray());
    }

    private void addAllTypeVals(JsonArray root) {
        root.add((JsonObject) null);
        root.add(new JsonPrimitive("hello world"));
        root.add(new JsonPrimitive(Integer.MAX_VALUE));
        root.add(new JsonPrimitive(Integer.MIN_VALUE));
        root.add(new JsonPrimitive(Long.MAX_VALUE));
        root.add(new JsonPrimitive(Long.MIN_VALUE));
        root.add(new JsonPrimitive(new BigInteger("9223372036854775808")));
        root.add(new JsonPrimitive(new BigInteger("-9223372036854775809")));
        root.add(new JsonPrimitive(Float.MAX_VALUE));
        root.add(new JsonPrimitive(-Float.MAX_VALUE));
        root.add(new JsonPrimitive(Double.MAX_VALUE));
        root.add(new JsonPrimitive(-Double.MAX_VALUE));
        root.add(new JsonPrimitive(true));
        root.add(new JsonPrimitive(false));
        root.add(new JsonObject());
        root.add(new JsonArray());
    }

    private List<String> collectKeys(Iterable<String> keys) {
        List<String> keyList = new ArrayList<>();
        for (String key : keys) {
            keyList.add(key);
        }
        return keyList;
    }

}