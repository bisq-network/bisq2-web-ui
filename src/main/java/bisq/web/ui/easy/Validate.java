package bisq.web.ui.easy;

import java.util.Optional;
import java.util.function.Consumer;

public class Validate {
    public static class ValidationException extends RuntimeException {
        public ValidationException() {
        }

        public ValidationException(String message) {
            super(message);
        }

        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ValidationException(Throwable cause) {
            super(cause);
        }
    }

    public static Optional<ValidationException> thisCode(Consumer<Validate> validator) {
        try {
            validator.accept(new Validate());
            return Optional.empty();
        } catch (ValidationException ex) {
            return Optional.of(ex);
        }
    }

    public Validate() {

    }

    public void that(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

}
