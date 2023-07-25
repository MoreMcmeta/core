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

/**
 * Signals that the metadata provided to the reader is invalid for some reason. It may be
 * in an invalid format or have an incompatible combination of properties.
 * @author soir20
 * @since 4.0.0
 */
public final class InvalidMetadataException extends Exception {
    private final boolean SILENCED;

    /**
     * Creates a new exception with a detail message.
     * @param reason        the reason the metadata is invalid
     */
    public InvalidMetadataException(String reason) {
        this(reason, false);
    }

    /**
     * Creates a new exception with a detail message.
     * @param reason        the reason the metadata is invalid
     * @param cause         the exception that caused this exception to be thrown
     */
    public InvalidMetadataException(String reason, Throwable cause) {
        this(reason, cause, false);
    }

    /**
     * Creates a new exception with a detail message.
     * @param reason        the reason the metadata is invalid
     * @param silenced      whether the exception should be silenced in logs
     */
    public InvalidMetadataException(String reason, boolean silenced) {
        super(reason);
        SILENCED = silenced;
    }

    /**
     * Creates a new exception with a detail message.
     * @param reason        the reason the metadata is invalid
     * @param cause         the exception that caused this exception to be thrown
     * @param silenced      whether the exception should be silenced in logs
     */
    public InvalidMetadataException(String reason, Throwable cause, boolean silenced) {
        super(reason, cause);
        SILENCED = silenced;
    }

    /**
     * Checks whether this exception should be silenced in logs.
     * @return whether the exception should be silenced in logs
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean silenced() {
        return SILENCED;
    }

}
