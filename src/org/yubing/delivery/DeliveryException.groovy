package org.yubing.delivery;

/**
 * Thrown when a plugin is found to be invalid when it is loaded.
 */
public class DeliveryException extends RuntimeException {
	private String code;

    public DeliveryException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DeliveryException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
