package watersec.internship.watersec_hydrolens.common.response;

import java.time.Instant;

/**
 * Generic envelope returned by every REST endpoint so clients get a consistent
 * shape for both successes and failures.
 *
 * @param <T> the payload type ({@code null} for errors or empty responses)
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Request processed successfully");
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }
}
