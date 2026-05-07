package com.minidouban.auth;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Pattern(regexp = "^[A-Za-z0-9_]{3,32}$")
        String username,
        @Size(min = 6)
        String password
) {
}
