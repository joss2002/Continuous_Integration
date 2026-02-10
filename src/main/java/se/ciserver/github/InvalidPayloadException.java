package se.ciserver.github;

/**
 * Defines an Exception specified to invalid payloads. The exception is
 * thrown when the GitHub payload can not be parsed or does not contain
 * the required fields.
 */
public class InvalidPayloadException extends Exception
{
    /**
     * Constructs a InvalidPayloadException with a given message.
     *
     * @param message The message explaining why the payload is invalid
     */
    public InvalidPayloadException(String message)
    {
        super(message);
    }

    /**
     * Constructs a InvalidPayloadException with a given message and
     * cause.
     *
     * @param message The message explaining why the payload is invalid
     * @param cause   The cause of the exception
     */
    public InvalidPayloadException(String message, Throwable cause)
    {
        super(message, cause);
    }
}