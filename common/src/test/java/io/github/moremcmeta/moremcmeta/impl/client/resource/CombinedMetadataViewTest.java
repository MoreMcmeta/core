/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.moremcmeta.moremcmeta.api.client.metadata.CombinedMetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.NegativeKeyIndexException;
import io.github.moremcmeta.moremcmeta.impl.client.io.MockMetadataView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link CombinedMetadataView}.
 * @author soir20
 */
public final class CombinedMetadataViewTest {
    private static final MockMetadataView MOCK_SUB_VIEW = new MockMetadataView(List.of("test"));
    private static final InputStream MOCK_STREAM = new ByteArrayInputStream("eight".getBytes());
    private static final ImmutableList<MetadataView> MOCK_VIEWS = ImmutableList.of(
            new MockMetadataView(ImmutableMap.of("one", 1, "two", 2.0f)),
            new MockMetadataView(
                    ImmutableMap.of(
                            "three", "3",
                            "four", true,
                            "five", 5L,
                            "six", 6.0D,
                            "seven", MOCK_SUB_VIEW,
                            "eight", MOCK_STREAM
                    )
            )
    );

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullViewCollection_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new CombinedMetadataView(null);
    }

    @Test
    public void construct_NullView_NullPointerException() {
        List<MetadataView> views = new ArrayList<>();
        views.add(new MockMetadataView(List.of("one", "two")));
        views.add(null);

        expectedException.expect(NullPointerException.class);
        new CombinedMetadataView(views);
    }

    @Test
    public void construct_ConflictingKeys_KeyMethodsUseFirstViewWithKey() {
        CombinedMetadataView view = new CombinedMetadataView(List.of(
                new MockMetadataView(ImmutableMap.of("one", 1, "two", 2.0f)),
                new MockMetadataView(ImmutableMap.of("three", "3", "one", true)),
                new MockMetadataView(ImmutableMap.of("three", 5L, "four", 6.0D)),
                new MockMetadataView(ImmutableMap.of("three", MOCK_SUB_VIEW))
        ));

        assertTrue(view.hasKey("one"));
        assertTrue(view.hasKey("two"));
        assertTrue(view.hasKey("three"));

        assertEquals(1, (int) view.integerValue("one").orElseThrow());
        assertTrue(view.booleanValue("one").isEmpty());

        assertEquals("3", view.stringValue("three").orElseThrow());
        assertTrue(view.longValue("three").isEmpty());
        assertTrue(view.subView("three").isEmpty());
    }

    @Test
    public void construct_ConflictingKeys_IndexMethodsUseFirstViewWithKey() {
        CombinedMetadataView view = new CombinedMetadataView(List.of(
                new MockMetadataView(ImmutableMap.of("one", 1, "two", 2.0f)),
                new MockMetadataView(ImmutableMap.of("three", "3", "one", true)),
                new MockMetadataView(ImmutableMap.of("three", 5L, "four", 6.0D)),
                new MockMetadataView(ImmutableMap.of("three", MOCK_SUB_VIEW))
        ));

        assertTrue(view.hasKey("one"));
        assertTrue(view.hasKey("two"));
        assertTrue(view.hasKey("three"));

        assertEquals(1, (int) view.integerValue(0).orElseThrow());
        assertTrue(view.booleanValue(0).isEmpty());

        assertEquals("3", view.stringValue(2).orElseThrow());
        assertTrue(view.longValue(2).isEmpty());
        assertTrue(view.subView(2).isEmpty());
    }

    @Test
    public void construct_NoViews_NoException() {
        CombinedMetadataView view = new CombinedMetadataView(List.of());
        assertEquals(0, view.size());
    }

    @Test
    public void size_MultipleViews_AllKeysCounted() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(8, view.size());
    }

    @Test
    public void keys_MultipleViews_IterationInCorrectOrder() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(List.of("one", "two", "three", "four", "five", "six", "seven", "eight"), Lists.newArrayList(view.keys()));
    }

    @Test
    public void hasKey_KeyInFirstView_HasKey() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertTrue(view.hasKey("one"));
        assertTrue(view.hasKey("two"));
    }

    @Test
    public void hasKey_KeyInLastView_HasKey() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertTrue(view.hasKey("three"));
        assertTrue(view.hasKey("four"));
        assertTrue(view.hasKey("five"));
        assertTrue(view.hasKey("six"));
        assertTrue(view.hasKey("seven"));
    }

    @Test
    public void hasKey_KeyNoView_DoesNotHaveKey() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertFalse(view.hasKey("nine"));
    }

    @Test
    public void hasKey_IndexInFirstView_HasKey() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertTrue(view.hasKey(0));
        assertTrue(view.hasKey(1));
    }

    @Test
    public void hasKey_IndexInLastView_HasKey() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertTrue(view.hasKey(2));
        assertTrue(view.hasKey(3));
        assertTrue(view.hasKey(4));
        assertTrue(view.hasKey(5));
        assertTrue(view.hasKey(6));
    }

    @Test
    public void hasKey_IndexInNoView_DoesNotHaveKey() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertFalse(view.hasKey(8));
    }

    @Test
    public void hasKey_IndexNegative_NegativeIndexException() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        expectedException.expect(NegativeKeyIndexException.class);
        assertFalse(view.hasKey(-1));
    }

    @Test
    public void stringValue_ItemIsRightType_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals("3", view.stringValue("three").orElseThrow());
    }

    @Test
    public void stringValue_ItemIsDifferentType_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.stringValue("four"));
    }

    @Test
    public void stringValue_KeyNotPresent_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.stringValue("nine"));
    }

    @Test
    public void stringValue_NegativeIndex_NegativeIndexException() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        expectedException.expect(NegativeKeyIndexException.class);
        view.stringValue(-1);
    }

    @Test
    public void stringValue_TooLargeIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.stringValue(10));
    }

    @Test
    public void stringValue_WrongTypeAtIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.stringValue(0));
    }

    @Test
    public void stringValue_RightTypeAtIndex_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals("3", view.stringValue(2).orElseThrow());
    }

    @Test
    public void integerValue_ItemIsRightType_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(1, (int) view.integerValue("one").orElseThrow());
    }

    @Test
    public void integerValue_ItemIsDifferentType_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.integerValue("four"));
    }

    @Test
    public void integerValue_KeyNotPresent_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.integerValue("nine"));
    }

    @Test
    public void integerValue_NegativeIndex_NegativeIndexException() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        expectedException.expect(NegativeKeyIndexException.class);
        view.integerValue(-1);
    }

    @Test
    public void integerValue_TooLargeIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.integerValue(10));
    }

    @Test
    public void integerValue_WrongTypeAtIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.integerValue(1));
    }

    @Test
    public void integerValue_RightTypeAtIndex_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(1, (int) view.integerValue(0).orElseThrow());
    }

    @Test
    public void longValue_ItemIsRightType_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(5L, (long) view.longValue("five").orElseThrow());
    }

    @Test
    public void longValue_ItemIsDifferentType_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.longValue("four"));
    }

    @Test
    public void longValue_KeyNotPresent_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.longValue("nine"));
    }

    @Test
    public void longValue_NegativeIndex_NegativeIndexException() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        expectedException.expect(NegativeKeyIndexException.class);
        view.longValue(-1);
    }

    @Test
    public void longValue_TooLargeIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.longValue(10));
    }

    @Test
    public void longValue_WrongTypeAtIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.longValue(1));
    }

    @Test
    public void longValue_RightTypeAtIndex_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(5L, (long) view.longValue(4).orElseThrow());
    }

    @Test
    public void floatValue_ItemIsRightType_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(2.0f, view.floatValue("two").orElseThrow(), 0);
    }

    @Test
    public void floatValue_ItemIsDifferentType_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.floatValue("five"));
    }

    @Test
    public void floatValue_KeyNotPresent_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.floatValue("nine"));
    }

    @Test
    public void floatValue_NegativeIndex_NegativeIndexException() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        expectedException.expect(NegativeKeyIndexException.class);
        view.floatValue(-1);
    }

    @Test
    public void floatValue_TooLargeIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.floatValue(10));
    }

    @Test
    public void floatValue_WrongTypeAtIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.floatValue(0));
    }

    @Test
    public void floatValue_RightTypeAtIndex_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(2.0f, view.floatValue(1).orElseThrow(), 0);
    }

    @Test
    public void doubleValue_ItemIsRightType_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(6.0D, view.doubleValue("six").orElseThrow(), 0);
    }

    @Test
    public void doubleValue_ItemIsDifferentType_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.doubleValue("five"));
    }

    @Test
    public void doubleValue_KeyNotPresent_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.doubleValue("nine"));
    }

    @Test
    public void doubleValue_NegativeIndex_NegativeIndexException() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        expectedException.expect(NegativeKeyIndexException.class);
        view.doubleValue(-1);
    }

    @Test
    public void doubleValue_TooLargeIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.doubleValue(10));
    }

    @Test
    public void doubleValue_WrongTypeAtIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.doubleValue(0));
    }

    @Test
    public void doubleValue_RightTypeAtIndex_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(6.0D, view.doubleValue(5).orElseThrow(), 0);
    }

    @Test
    public void booleanValue_ItemIsRightType_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(true, view.booleanValue("four").orElseThrow());
    }

    @Test
    public void booleanValue_ItemIsDifferentType_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.booleanValue("five"));
    }

    @Test
    public void booleanValue_KeyNotPresent_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.booleanValue("nine"));
    }

    @Test
    public void booleanValue_NegativeIndex_NegativeIndexException() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        expectedException.expect(NegativeKeyIndexException.class);
        view.booleanValue(-1);
    }

    @Test
    public void booleanValue_TooLargeIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.booleanValue(10));
    }

    @Test
    public void booleanValue_WrongTypeAtIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.booleanValue(1));
    }

    @Test
    public void booleanValue_RightTypeAtIndex_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(true, view.booleanValue(3).orElseThrow());
    }

    @Test
    public void subView_ItemIsRightType_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(MOCK_SUB_VIEW, view.subView("seven").orElseThrow());
    }

    @Test
    public void subView_ItemIsDifferentType_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.subView("five"));
    }

    @Test
    public void subView_KeyNotPresent_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.subView("nine"));
    }

    @Test
    public void subView_NegativeIndex_NegativeIndexException() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        expectedException.expect(NegativeKeyIndexException.class);
        view.subView(-1);
    }

    @Test
    public void subView_TooLargeIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.subView(10));
    }

    @Test
    public void subView_WrongTypeAtIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.subView(1));
    }

    @Test
    public void subView_RightTypeAtIndex_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(MOCK_SUB_VIEW, view.subView(6).orElseThrow());
    }

    @Test
    public void streamValue_ItemIsRightType_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(MOCK_STREAM, view.byteStreamValue("eight").orElseThrow());
    }

    @Test
    public void streamValue_ItemIsDifferentType_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.byteStreamValue("five"));
    }

    @Test
    public void streamValue_KeyNotPresent_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.byteStreamValue("nine"));
    }

    @Test
    public void streamValue_NegativeIndex_NegativeIndexException() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        expectedException.expect(NegativeKeyIndexException.class);
        view.byteStreamValue(-1);
    }

    @Test
    public void streamValue_TooLargeIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.byteStreamValue(10));
    }

    @Test
    public void streamValue_WrongTypeAtIndex_DoesNotHaveItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(Optional.empty(), view.byteStreamValue(1));
    }

    @Test
    public void streamValue_RightTypeAtIndex_HasItem() {
        CombinedMetadataView view = new CombinedMetadataView(MOCK_VIEWS);
        assertEquals(MOCK_STREAM, view.byteStreamValue(7).orElseThrow());
    }

}