package bisq.web.util;

import java.util.function.Function;

public class Util {

    public static <T, R> R nullSafe(T t, Function<T, R> fun) {
        if (t == null) {
            return null;
        } else {
            return fun.apply(t);
        }

    }
}
