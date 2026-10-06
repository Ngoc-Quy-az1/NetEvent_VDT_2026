package com.example.notification.common.query;

import java.util.Objects;
import java.util.regex.Pattern;

/** A validated SQL identifier. Values must never be represented by this type. */
public final class SqlIdentifier {
    private static final Pattern VALID = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)?");
    private final String value;

    private SqlIdentifier(String value) { this.value = value; }

    public static SqlIdentifier of(String value) {
        Objects.requireNonNull(value, "identifier");
        if (!VALID.matcher(value).matches()) throw new IllegalArgumentException("Invalid SQL identifier");
        return new SqlIdentifier(value);
    }

    @Override public String toString() { return value; }
}
