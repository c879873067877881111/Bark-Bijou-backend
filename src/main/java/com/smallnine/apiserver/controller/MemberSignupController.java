package com.smallnine.apiserver.controller;

import com.smallnine.apiserver.dao.EmailVerificationDao;
import com.smallnine.apiserver.dao.UserDao;
import com.smallnine.apiserver.dto.*;
import com.smallnine.apiserver.entity.RefreshToken;
import com.smallnine.apiserver.entity.User;
import com.smallnine.apiserver.exception.BusinessException;
import com.smallnine.apiserver.service.MailService;
import com.smallnine.apiserver.service.OtpService;
import com.smallnine.apiserver.service.RefreshTokenService;
import com.smallnine.apiserver.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/member/signup")
@RequiredArgsConstructor
@Tag(name = "會員註冊", description = "OTP 註冊流程 API")
public class MemberSignupController {

    private final OtpService otpService;
    private final MailService mailService;
    private final UserDao userDao;
    private final EmailVerificationDao emailVerificationDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "發送 OTP 驗證碼")
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        if (userDao.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("此信箱已被註冊"));
        }
        Map<String, String> result = otpService.generateAndSend(request.getEmail());
        mailService.sendOtpEmail(request.getEmail(), result.get("otp"));

        return ResponseEntity.ok(ApiResponse.success("驗證碼已寄出，請至信箱查收",
                Map.of("secret", result.get("secret"))));
    }

    @Operation(summary = "驗證 OTP")
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            otpService.verify(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(ApiResponse.success("信箱驗證成功"));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "完成註冊")
    @PostMapping("")
    public ResponseEntity<ApiResponse<Map<String, Object>>> complete(@Valid @RequestBody SignupCompleteRequest request) {
        if (!request.getPassword().equals(request.getRepassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("兩次密碼輸入不一致"));
        }

        if (!emailVerificationDao.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("請先完成信箱驗證"));
        }

        if (userDao.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("用戶名已被使用"));
        }
        if (userDao.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("此信箱已被註冊"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealname(request.getUsername());
        user.setGender(User.Gender.male);
        user.setEmailValidated(true);

        userDao.insert(user);
        otpService.cleanup(request.getEmail());

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtUtil.getAccessTokenExpirationTime() / 1000);

        Map<String, Object> authData = new LinkedHashMap<>();
        authData.put("accessToken", accessToken);
        authData.put("refreshToken", refreshToken.getToken());
        authData.put("id", user.getId());
        authData.put("username", user.getUsername());
        authData.put("email", user.getEmail());
        authData.put("expiresAt", expiresAt.toString());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("註冊成功", authData));
    }

    @Operation(summary = "欄位即時驗證")
    @PostMapping("/validate-field")
    public ResponseEntity<ApiResponse<Void>> validateField(@Valid @RequestBody ValidateFieldRequest request) {
        switch (request.getField()) {
            case "email":
                if (userDao.existsByEmail(request.getValue())) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("此信箱已被註冊"));
                }
                break;
            case "username":
                if (userDao.existsByUsername(request.getValue())) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("用戶名已被使用"));
                }
                break;
            case "password":
                if (request.getValue().length() < 6) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("密碼長度至少6位"));
                }
                break;
            default:
                break;
        }

        return ResponseEntity.ok(ApiResponse.success());
    }
}
