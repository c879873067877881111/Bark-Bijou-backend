package com.smallnine.apiserver.service;

import java.util.Map;

public interface ForgotPasswordService {

    /**
     * 發送重設密碼 OTP
     * @return 包含 "secret" key 的 Map
     */
    Map<String, String> sendResetOtp(String email);

    /**
     * 透過 OTP 重設密碼
     */
    void resetPasswordByOtp(String secret, String otpToken, String newPassword);

    /**
     * 透過連結重設密碼
     */
    void resetPasswordByLink(String secret, String newPassword);
}