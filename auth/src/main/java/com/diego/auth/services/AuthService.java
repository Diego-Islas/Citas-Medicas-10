package com.diego.auth.services;

import com.diego.auth.dto.LoginRequest;
import com.diego.auth.dto.TokenResponse;

public interface AuthService {

    TokenResponse autenticar(LoginRequest request) throws Exception;
}

