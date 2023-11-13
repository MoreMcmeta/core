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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link ModRepositorySource}.
 * @author soir20
 */
@SuppressWarnings("ConstantConditions")
public final class ModRepositorySourceTest {
    private static final Pack.ResourcesSupplier MOCK_RESOURCES_SUPPLIER = new Pack.ResourcesSupplier() {
        @Override
        public @NotNull PackResources openPrimary(String packId) {
            return new MockPackResources();
        }

        @Override
        public @NotNull PackResources openFull(String packId, Pack.Info info) {
            return openPrimary(packId);
        }
    };

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
    }

    @Test
    public void construct_NullPackGetter_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new ModRepositorySource(null);
    }

    @Test
    public void loadPacks_NullConsumer_NullPointerException() {
        ModRepositorySource repositorySource = new ModRepositorySource(MOCK_RESOURCES_SUPPLIER);

        expectedException.expect(NullPointerException.class);
        repositorySource.loadPacks(null);
    }

    @Test
    public void loadPacks_ValidParameters_OnePackGiven() {
        ModRepositorySource repositorySource = new ModRepositorySource(MOCK_RESOURCES_SUPPLIER);

        AtomicInteger packsConsumed = new AtomicInteger();
        repositorySource.loadPacks((pack) -> packsConsumed.getAndIncrement());

        assertEquals(1, packsConsumed.get());
    }

    @Test
    public void loadPacks_ValidParameters_PackWithRightIdGiven() {
        ModRepositorySource repositorySource = new ModRepositorySource(MOCK_RESOURCES_SUPPLIER);

        AtomicReference<String> packId = new AtomicReference<>("");

        repositorySource.loadPacks((pack) -> packId.set(pack.getId()));

        assertEquals(ModRepositorySource.PACK_ID, packId.get());
    }

    @Test
    public void loadPacks_ValidParameters_PackWithRightFormatVersionGiven() {
        ModRepositorySource repositorySource = new ModRepositorySource(MOCK_RESOURCES_SUPPLIER);

        AtomicReference<PackCompatibility> packCompatibility = new AtomicReference<>(null);

        repositorySource.loadPacks((pack) -> packCompatibility.set(pack.getCompatibility()));

        assertEquals(PackCompatibility.COMPATIBLE, packCompatibility.get());
    }

}