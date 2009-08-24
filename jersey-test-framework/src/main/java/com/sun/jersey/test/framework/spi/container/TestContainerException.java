package com.sun.jersey.test.framework.spi.container;

/**
 * Thrown when a test container-specific error occurs.
 * 
 * @author Paul.Sandoz@Sun.COM
 */
public class TestContainerException extends RuntimeException {
    /**
     * Construct a new instance with no message.
     */
    public TestContainerException() {
        super();
    }

    /**
     * Construct a new instance with a message.
     *
     * @param message the message
     */
    public TestContainerException(String message) {
        super(message);
    }

    /**
     * Construct a new instance with a message and a cause.
     *
     * @param message the message.
     * @param cause the Throwable that caused the exception to be thrown.
     */
    public TestContainerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a new instance with a cause.
     * 
     * @param cause the Throwable that caused the exception to be thrown.
     */
    public TestContainerException(Throwable cause) {
        super(cause);
    }
}
