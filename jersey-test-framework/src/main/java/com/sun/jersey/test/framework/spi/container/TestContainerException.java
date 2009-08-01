package com.sun.jersey.test.framework.spi.container;

/**
 *
 * @author paulsandoz
 */
public class TestContainerException extends RuntimeException {
    /**
     * Construct a new instance with the supplied message
     */
    public TestContainerException() {
        super();
    }

    /**
     * Construct a new instance with the supplied message
     * @param message the message
     */
    public TestContainerException(String message) {
        super(message);
    }

    /**
     * Construct a new instance with the supplied message and cause
     * @param message the message
     * @param cause the Throwable that caused the exception to be thrown
     */
    public TestContainerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a new instance with the supplied cause
     * @param cause the Throwable that caused the exception to be thrown
     */
    public TestContainerException(Throwable cause) {
        super(cause);
    }
}
