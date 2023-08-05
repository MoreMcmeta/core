package io.github.moremcmeta.moremcmeta.api.client.metadata;

/**
 * Indicates that an invalid {@link RootResourceName} was attempted to be created.
 * @author soir20
 * @since 4.2.0
 */
public final class InvalidRootResourceNameException extends IllegalArgumentException {

    /**
     * Creates a new exception with a detail message.
     * @param reason        the reason the name is invalid
     */
    InvalidRootResourceNameException(String reason) {
        super(reason);
    }

}
