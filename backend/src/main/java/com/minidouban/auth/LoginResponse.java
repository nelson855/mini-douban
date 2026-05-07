package com.minidouban.auth;

import com.minidouban.user.UserResponse;

public record LoginResponse(String token, UserResponse user) {
}
