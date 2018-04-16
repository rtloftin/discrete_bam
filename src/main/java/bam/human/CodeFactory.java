package bam.human;

import java.util.Optional;
import java.util.UUID;

public interface CodeFactory {

    static CodeFactory uuid() {
        return () -> Optional.of(UUID.randomUUID().toString());
    }

    static CodeFactory dummy(String code) {
        return () -> Optional.of(code);
    }

    Optional<String> nextCode();
}
