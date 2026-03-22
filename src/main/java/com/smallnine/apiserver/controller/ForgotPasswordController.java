package com.smallnine.apiserver.controller;

import com.smallnine.apiserver.dto.ApiResponse;
import com.smallnine.apiserver.dto.ResetPasswordByLinkRequest;
import com.smallnine.apiserver.dto.ResetPasswordByOtpRequest;
import com.smallnine.apiserver.dto.SendOtpRequest;
import com.smallnine.apiserver.service.ForgotPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "忘記密碼", description = "忘記密碼 OTP 流程 API")
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @Operation(summary = "發送重設密碼 OTP")
    @PostMapping("/api/member/requestotp")
    public ResponseEntity<ApiResponse<Map<String, String>>> requestOtp(@Valid @RequestBody SendOtpRequest request) {
        Map<String, String> result = forgotPasswordService.sendResetOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("驗證碼已寄出，請至信箱查收", result));
    }

    @Operation(summary = "重設密碼 (透過 OTP)")
    @PostMapping("/api/member/resetpassword")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordByOtpRequest request) {
        forgotPasswordService.resetPasswordByOtp(request.getSecret(), request.getOtpToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("密碼重設成功"));
    }

    @Operation(summary = "重設密碼 (透過連結)")
    @PostMapping("/api/member/resetpassword/bylink")
    public ResponseEntity<ApiResponse<Void>> resetPasswordByLink(@Valid @RequestBody ResetPasswordByLinkRequest request) {
        forgotPasswordService.resetPasswordByLink(request.getSecret(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("密碼重設成功"));
    }
}
