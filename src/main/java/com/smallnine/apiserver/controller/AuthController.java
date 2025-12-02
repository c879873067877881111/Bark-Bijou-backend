package com.smallnine.apiserver.controller;

import com.smallnine.apiserver.dto.*;
import com.smallnine.apiserver.exception.TokenRefreshException;
import com.smallnine.apiserver.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "用戶認證", description = "用戶登入、註冊和認證相關 API")
@Slf4j
public class AuthController {
    
    private final AuthServiceImpl authService;
    
    @Operation(summary = "用戶註冊", description = "註冊新用戶帳號")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "註冊成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "請求參數無效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "用戶名或信箱已存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserResponse user = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("註冊成功", user));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("已存在")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("註冊失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("註冊失敗"));
        }
    }
    
    @Operation(summary = "用戶登入", description = "用戶登入並獲取存取令牌")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登入成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "用戶名或密碼錯誤"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "帳號已被停用"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登入成功", authResponse));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException |
                 org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("停用")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("登入失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("登入失敗"));
        }
    }
    
    @Operation(summary = "獲取當前用戶訊息", description = "根據存取令牌獲取當前登入用戶的詳細訊息")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取用戶訊息"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "用戶不存在")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("未授權訪問"));
            }
            
            UserResponse user = authService.getCurrentUser(userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("用戶不存在"));
        } catch (Exception e) {
            log.error("獲取當前用戶訊息失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取用戶訊息失敗"));
        }
    }
    
    @Operation(summary = "更新存取令牌", description = "使用更新令牌重新獲取存取令牌")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "刷新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "更新令牌無效或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "更新令牌不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse authResponse = authService.refreshToken(request);
            return ResponseEntity.ok(ApiResponse.success("存取令牌更新成功", authResponse));
        } catch (TokenRefreshException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("更新令牌已過期,請重新登入"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("更新存取令牌失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新存取令牌失敗"));
        }
    }
    
    @Operation(summary = "用戶登出", description = "撤銷更新令牌並登出用戶")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登出成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("登出成功"));
        } catch (Exception e) {
            log.error("登出失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("登出失敗"));
        }
    }
}