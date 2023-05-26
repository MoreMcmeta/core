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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import com.google.common.collect.ImmutableSet;
import io.github.moremcmeta.moremcmeta.api.client.metadata.Base;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link BaseCollection}.
 * @author soir20
 */
public class BaseCollectionTest {
    private static final ResourceLocation NO_SPRITE_LOCATION_1 = new ResourceLocation("no_sprite1.png");
    private static final ResourceLocation NO_SPRITE_LOCATION_2 = new ResourceLocation("no_sprite2.png");
    private static final long NO_SPRITE_UPLOAD_POINT_2 = Point.pack(7, 3);
    private static final ResourceLocation SPRITE_LOCATION_1 = new ResourceLocation("sprite1.png");
    private static final ResourceLocation SPRITE_ATLAS_1 = new ResourceLocation("textures/atlas/blocks.png");
    private static final long SPRITE_UPLOAD_POINT_1 = Point.pack(20, 10);
    private static final int SPRITE_MIPMAP_1 = 3;
    private static final ResourceLocation SPRITE_LOCATION_2 = new ResourceLocation("sprite2.png");
    private static final ResourceLocation SPRITE_ATLAS_2 = new ResourceLocation("textures/atlas/paintings.png");
    private static final long SPRITE_UPLOAD_POINT_2 = Point.pack(2, 5);
    private static final int SPRITE_MIPMAP_2 = 0;
    private static final SpriteFinder SPRITE_FINDER = new SpriteFinder((atlasLocation) -> (spriteLocation) -> {
        if (atlasLocation.equals(SPRITE_ATLAS_1) && spriteLocation.equals(SPRITE_LOCATION_1)) {
            return Optional.of(new MockSprite(
                    SPRITE_LOCATION_1,
                    SPRITE_UPLOAD_POINT_1,
                    SPRITE_MIPMAP_1,
                    SPRITE_ATLAS_1
            ));
        }

        if (atlasLocation.equals(SPRITE_ATLAS_2) && spriteLocation.equals(SPRITE_LOCATION_2)) {
            return Optional.of(new MockSprite(
                    SPRITE_LOCATION_2,
                    SPRITE_UPLOAD_POINT_2,
                    SPRITE_MIPMAP_2,
                    SPRITE_ATLAS_2
            ));
        }

        return Optional.empty();
    });

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void find_NullFinder_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        BaseCollection.find(null, ImmutableSet.of(), NO_SPRITE_LOCATION_1);
    }

    @Test
    public void find_NullBases_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        BaseCollection.find(SPRITE_FINDER, null, NO_SPRITE_LOCATION_1);
    }

    @Test
    public void find_NullLocation_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        BaseCollection.find(SPRITE_FINDER, ImmutableSet.of(), null);
    }

    @Test
    public void baseData_NullLocation_NullPointerException() {
        BaseCollection result = BaseCollection.find(SPRITE_FINDER, ImmutableSet.of(), NO_SPRITE_LOCATION_1);
        expectedException.expect(NullPointerException.class);
        result.baseData(null);
    }

    @Test
    public void baseData_NoBasesAtLocation_EmptyCollection() {
        BaseCollection result = BaseCollection.find(SPRITE_FINDER, ImmutableSet.of(), NO_SPRITE_LOCATION_1);
        assertTrue(result.baseData(NO_SPRITE_LOCATION_2).isEmpty());
    }

    @Test
    public void equals_EqualsNull_False() {
        BaseCollection result = BaseCollection.find(SPRITE_FINDER, ImmutableSet.of(), NO_SPRITE_LOCATION_1);

        assertEquals(ImmutableSet.of(NO_SPRITE_LOCATION_1), new HashSet<>(result.baseNames()));

        List<BaseCollection.MipmappedBase> bases = new ArrayList<>(result.baseData(NO_SPRITE_LOCATION_1));
        assertNotEquals(null, bases.get(0));
    }

    @Test
    public void equals_EqualsNonBaseObject_False() {
        BaseCollection result = BaseCollection.find(SPRITE_FINDER, ImmutableSet.of(), NO_SPRITE_LOCATION_1);

        assertEquals(ImmutableSet.of(NO_SPRITE_LOCATION_1), new HashSet<>(result.baseNames()));

        List<BaseCollection.MipmappedBase> bases = new ArrayList<>(result.baseData(NO_SPRITE_LOCATION_1));
        //noinspection SimplifiableAssertion,EqualsBetweenInconvertibleTypes
        assertFalse(bases.get(0).equals("test"));
    }

    @Test
    public void find_EmptyBasesNoSprite_OnlyFindsSelf() {
        BaseCollection result = BaseCollection.find(SPRITE_FINDER, ImmutableSet.of(), NO_SPRITE_LOCATION_1);

        assertEquals(ImmutableSet.of(NO_SPRITE_LOCATION_1), new HashSet<>(result.baseNames()));

        List<BaseCollection.MipmappedBase> bases = new ArrayList<>(result.baseData(NO_SPRITE_LOCATION_1));
        assertEquals(1, bases.size());
        assertEquals(EventDrivenTexture.SELF_UPLOAD_POINT, bases.get(0).uploadPoint());
        assertEquals(EventDrivenTexture.SELF_MIPMAP_LEVEL, bases.get(0).mipmap());
    }

    @Test
    public void find_EmptyBasesWithSprite_FindsSelfAndSprite() {
        BaseCollection result = BaseCollection.find(SPRITE_FINDER, ImmutableSet.of(), SPRITE_LOCATION_1);

        assertEquals(ImmutableSet.of(SPRITE_LOCATION_1, SPRITE_ATLAS_1), new HashSet<>(result.baseNames()));

        List<BaseCollection.MipmappedBase> self = new ArrayList<>(result.baseData(SPRITE_LOCATION_1));
        assertEquals(1, self.size());
        assertEquals(EventDrivenTexture.SELF_UPLOAD_POINT, self.get(0).uploadPoint());
        assertEquals(EventDrivenTexture.SELF_MIPMAP_LEVEL, self.get(0).mipmap());

        List<BaseCollection.MipmappedBase> sprite1 = new ArrayList<>(result.baseData(SPRITE_ATLAS_1));
        assertEquals(1, sprite1.size());
        assertEquals(SPRITE_UPLOAD_POINT_1, sprite1.get(0).uploadPoint());
        assertEquals(SPRITE_MIPMAP_1, sprite1.get(0).mipmap());
    }

    @Test
    public void find_NonEmptyBasesNoSprite_FindsSelfAndGiven() {
        BaseCollection result = BaseCollection.find(
                SPRITE_FINDER,
                ImmutableSet.of(new Base(NO_SPRITE_LOCATION_2, NO_SPRITE_UPLOAD_POINT_2)),
                NO_SPRITE_LOCATION_1
        );

        assertEquals(ImmutableSet.of(NO_SPRITE_LOCATION_1, NO_SPRITE_LOCATION_2), new HashSet<>(result.baseNames()));

        List<BaseCollection.MipmappedBase> bases1 = new ArrayList<>(result.baseData(NO_SPRITE_LOCATION_1));
        assertEquals(1, bases1.size());
        assertEquals(EventDrivenTexture.SELF_UPLOAD_POINT, bases1.get(0).uploadPoint());
        assertEquals(EventDrivenTexture.SELF_MIPMAP_LEVEL, bases1.get(0).mipmap());

        List<BaseCollection.MipmappedBase> bases2 = new ArrayList<>(result.baseData(NO_SPRITE_LOCATION_2));
        assertEquals(1, bases2.size());
        assertEquals(NO_SPRITE_UPLOAD_POINT_2, bases2.get(0).uploadPoint());
        assertEquals(0, bases2.get(0).mipmap());
    }

    @Test
    public void find_NonEmptyBasesWithSprite_FindsSelfAndGivenAndSprite() {
        BaseCollection result = BaseCollection.find(
                SPRITE_FINDER,
                ImmutableSet.of(new Base(NO_SPRITE_LOCATION_2, NO_SPRITE_UPLOAD_POINT_2)),
                SPRITE_LOCATION_1
        );

        assertEquals(
                ImmutableSet.of(SPRITE_LOCATION_1, SPRITE_ATLAS_1, NO_SPRITE_LOCATION_2),
                new HashSet<>(result.baseNames())
        );

        List<BaseCollection.MipmappedBase> self = new ArrayList<>(result.baseData(SPRITE_LOCATION_1));
        assertEquals(1, self.size());
        assertEquals(EventDrivenTexture.SELF_UPLOAD_POINT, self.get(0).uploadPoint());
        assertEquals(EventDrivenTexture.SELF_MIPMAP_LEVEL, self.get(0).mipmap());

        List<BaseCollection.MipmappedBase> sprite1 = new ArrayList<>(result.baseData(SPRITE_ATLAS_1));
        assertEquals(1, sprite1.size());
        assertEquals(SPRITE_UPLOAD_POINT_1, sprite1.get(0).uploadPoint());
        assertEquals(SPRITE_MIPMAP_1, sprite1.get(0).mipmap());

        List<BaseCollection.MipmappedBase> bases2 = new ArrayList<>(result.baseData(NO_SPRITE_LOCATION_2));
        assertEquals(1, bases2.size());
        assertEquals(NO_SPRITE_UPLOAD_POINT_2, bases2.get(0).uploadPoint());
        assertEquals(0, bases2.get(0).mipmap());
    }

    @Test
    public void find_NonEmptyBasesWithGivenSprite_FindsSelfAndGivenAndSprite() {
        BaseCollection result = BaseCollection.find(
                SPRITE_FINDER,
                ImmutableSet.of(new Base(SPRITE_LOCATION_2, Point.pack(8, 6))),
                SPRITE_LOCATION_1
        );

        assertEquals(
                ImmutableSet.of(SPRITE_LOCATION_1, SPRITE_ATLAS_1, SPRITE_LOCATION_2, SPRITE_ATLAS_2),
                new HashSet<>(result.baseNames())
        );

        List<BaseCollection.MipmappedBase> self = new ArrayList<>(result.baseData(SPRITE_LOCATION_1));
        assertEquals(1, self.size());
        assertEquals(EventDrivenTexture.SELF_UPLOAD_POINT, self.get(0).uploadPoint());
        assertEquals(EventDrivenTexture.SELF_MIPMAP_LEVEL, self.get(0).mipmap());

        List<BaseCollection.MipmappedBase> sprite1 = new ArrayList<>(result.baseData(SPRITE_ATLAS_1));
        assertEquals(1, sprite1.size());
        assertEquals(SPRITE_UPLOAD_POINT_1, sprite1.get(0).uploadPoint());
        assertEquals(SPRITE_MIPMAP_1, sprite1.get(0).mipmap());

        List<BaseCollection.MipmappedBase> bases2 = new ArrayList<>(result.baseData(SPRITE_LOCATION_2));
        assertEquals(1, bases2.size());
        assertEquals(Point.pack(8, 6), bases2.get(0).uploadPoint());
        assertEquals(0, bases2.get(0).mipmap());

        List<BaseCollection.MipmappedBase> basesSprite2 = new ArrayList<>(result.baseData(SPRITE_ATLAS_2));
        assertEquals(1, basesSprite2.size());
        assertEquals(Point.pack(10, 11), basesSprite2.get(0).uploadPoint());
        assertEquals(SPRITE_MIPMAP_2, basesSprite2.get(0).mipmap());
    }

    @Test
    public void find_GivenMultipleLocationsInSameTexture_FindsSelfAndGiven() {
        BaseCollection result = BaseCollection.find(
                SPRITE_FINDER,
                ImmutableSet.of(
                        new Base(NO_SPRITE_LOCATION_2, NO_SPRITE_UPLOAD_POINT_2),
                        new Base(NO_SPRITE_LOCATION_2, Point.pack(7, 14))
                ),
                NO_SPRITE_LOCATION_1
        );

        assertEquals(ImmutableSet.of(NO_SPRITE_LOCATION_1, NO_SPRITE_LOCATION_2), new HashSet<>(result.baseNames()));

        List<BaseCollection.MipmappedBase> bases1 = new ArrayList<>(result.baseData(NO_SPRITE_LOCATION_1));
        assertEquals(1, bases1.size());
        assertEquals(EventDrivenTexture.SELF_UPLOAD_POINT, bases1.get(0).uploadPoint());
        assertEquals(EventDrivenTexture.SELF_MIPMAP_LEVEL, bases1.get(0).mipmap());

        List<BaseCollection.MipmappedBase> bases2 = new ArrayList<>(result.baseData(NO_SPRITE_LOCATION_2));
        assertEquals(2, bases2.size());
        assertEquals(
                ImmutableSet.of(NO_SPRITE_UPLOAD_POINT_2, Point.pack(7, 14)),
                bases2.stream().map(BaseCollection.MipmappedBase::uploadPoint).collect(Collectors.toSet())
        );
        assertEquals(
                ImmutableSet.of(0),
                bases2.stream().map(BaseCollection.MipmappedBase::mipmap).collect(Collectors.toSet())
        );
    }

    @Test
    public void find_GivenDuplicate_DuplicatesCombined() {
        BaseCollection result = BaseCollection.find(
                SPRITE_FINDER,
                ImmutableSet.of(
                        new Base(SPRITE_LOCATION_1, EventDrivenTexture.SELF_UPLOAD_POINT),
                        new Base(SPRITE_ATLAS_1, SPRITE_UPLOAD_POINT_1)
                ),
                SPRITE_LOCATION_1
        );

        assertEquals(ImmutableSet.of(SPRITE_LOCATION_1, SPRITE_ATLAS_1), new HashSet<>(result.baseNames()));

        List<BaseCollection.MipmappedBase> self = new ArrayList<>(result.baseData(SPRITE_LOCATION_1));
        assertEquals(1, self.size());
        assertEquals(EventDrivenTexture.SELF_UPLOAD_POINT, self.get(0).uploadPoint());
        assertEquals(EventDrivenTexture.SELF_MIPMAP_LEVEL, self.get(0).mipmap());

        List<BaseCollection.MipmappedBase> sprite1 = new ArrayList<>(result.baseData(SPRITE_ATLAS_1));
        assertEquals(2, sprite1.size());
        assertEquals(
                ImmutableSet.of(SPRITE_UPLOAD_POINT_1),
                sprite1.stream().map(BaseCollection.MipmappedBase::uploadPoint).collect(Collectors.toSet())
        );
        assertEquals(
                ImmutableSet.of(SPRITE_MIPMAP_1, 0),
                sprite1.stream().map(BaseCollection.MipmappedBase::mipmap).collect(Collectors.toSet())
        );
    }

    @Test
    public void find_NonZeroMaxMipmap_MaxMipmapCorrect() {
        BaseCollection result = BaseCollection.find(
                SPRITE_FINDER,
                ImmutableSet.of(new Base(SPRITE_LOCATION_2, Point.pack(8, 6))),
                SPRITE_LOCATION_1
        );

        assertEquals(
                ImmutableSet.of(SPRITE_LOCATION_1, SPRITE_ATLAS_1, SPRITE_LOCATION_2, SPRITE_ATLAS_2),
                new HashSet<>(result.baseNames())
        );
        assertEquals(SPRITE_MIPMAP_1, result.maxMipmap());
    }

    @Test
    public void find_ZeroMaxMipmap_MaxMipmapCorrect() {
        BaseCollection result = BaseCollection.find(
                SPRITE_FINDER,
                ImmutableSet.of(new Base(NO_SPRITE_LOCATION_2, NO_SPRITE_UPLOAD_POINT_2)),
                NO_SPRITE_LOCATION_1
        );

        assertEquals(ImmutableSet.of(NO_SPRITE_LOCATION_1, NO_SPRITE_LOCATION_2), new HashSet<>(result.baseNames()));
        assertEquals(0, result.maxMipmap());
    }

}