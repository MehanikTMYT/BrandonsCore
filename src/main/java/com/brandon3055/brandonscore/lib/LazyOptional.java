package com.brandon3055.brandonscore.lib;
import java.util.Optional;
// STUB(port-26.1.2): Optional удален, используем Optional
public class Optional<T> {
    public static <T> Optional<T> of(T value) { return Optional.ofNullable(value); }
    public static <T> Optional<T> empty() { return Optional.empty(); }
}
