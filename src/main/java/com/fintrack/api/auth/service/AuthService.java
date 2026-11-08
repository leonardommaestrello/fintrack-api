package com.fintrack.api.auth.service;

import com.fintrack.api.auth.dto.LoginRequest;
import com.fintrack.api.auth.dto.LoginResponse;
import com.fintrack.api.auth.dto.RegisterRequest;
import com.fintrack.api.auth.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

}