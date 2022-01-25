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

package io.github.soir20.moremcmeta.client.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Tests the {@link ModRepositorySource}.
 * @author soir20
 */
public class ModRepositorySourceTest {
    private static final Pack.PackConstructor MOCK_CONSTRUCTOR =
            (id, title, required, packSupplier, packMetadataSection, position, packSource) ->
                    new Pack(id, title, required, packSupplier, packMetadataSection, PackType.CLIENT_RESOURCES,
                            position, packSource);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullPackGetter_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new ModRepositorySource(null);
    }

    @Test
    public void loadPacks_NullConsumer_NullPointerException() {
        ModRepositorySource repositorySource = new ModRepositorySource(MockPackResources::new);

        expectedException.expect(NullPointerException.class);
        repositorySource.loadPacks(null, MOCK_CONSTRUCTOR);
    }

    @Test
    public void loadPacks_NullPackConstructor_NullPointerExceptionPackNotSupplied() {
        ModRepositorySource repositorySource = new ModRepositorySource(MockPackResources::new);

        AtomicBoolean wasSet = new AtomicBoolean(false);

        try {
            expectedException.expect(NullPointerException.class);
            repositorySource.loadPacks((pack) -> wasSet.set(true), null);
        } finally {
            assertFalse(wasSet.get());
        }
    }

    @Test
    public void loadPacks_ValidParameters_OnePackGiven() {
        ModRepositorySource repositorySource = new ModRepositorySource(MockPackResources::new);

        AtomicInteger packsConsumed = new AtomicInteger();
        repositorySource.loadPacks((pack) -> packsConsumed.getAndIncrement(), MOCK_CONSTRUCTOR);

        assertEquals(1, packsConsumed.get());
    }

    @Test
    public void loadPacks_ValidParameters_PackWithRightIdGiven() {
        ModRepositorySource repositorySource = new ModRepositorySource(MockPackResources::new);

        AtomicReference<String> packId = new AtomicReference<>("");

        repositorySource.loadPacks((pack) -> packId.set(pack.getId()), MOCK_CONSTRUCTOR);

        assertEquals(ModRepositorySource.getPackId(), packId.get());
    }

    /**
     * Mock {@link PackResources} implementation for testing.
     * @author soir20
     */
    private static class MockPackResources implements PackResources {

        @Override
        public InputStream getRootResource(String string) {
            return new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public InputStream getResource(PackType packType, ResourceLocation resourceLocation) {
            return new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int i,
                                                         Predicate<String> predicate) {
            return Set.of();
        }

        @Override
        public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
            return false;
        }

        @Override
        public Set<String> getNamespaces(PackType packType) {
            return Set.of();
        }

        @Override
        public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
            return null;
        }

        @Override
        public String getName() {
            return "name";
        }

        @Override
        public void close() {}
    }

}