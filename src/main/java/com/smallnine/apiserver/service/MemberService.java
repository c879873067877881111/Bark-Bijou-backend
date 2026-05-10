package com.smallnine.apiserver.service;

import com.smallnine.apiserver.dto.GoogleProfile;
import com.smallnine.apiserver.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface MemberService {

    /**
     * 更新會員資料，回傳更新後的頭像 URL（若有更新）
     */
    String updateProfile(User user, String username, String realname, String email,
                         String birthDate, String gender, String phone, MultipartFile avatar);

    /**
     * 修改密碼
     */
    void changePassword(Long memberId, Long authenticatedUserId, String currentPassword, String newPassword);

    /**
     * 依 Google profile 找出或建立使用者；已存在但未綁定 google_uid 者自動連結。
     *
     * 並行情境：兩個 thread 同時對同一個 sub 跑到 INSERT，第二個會撞 google_uid
     * UNIQUE constraint。本方法會拋 {@link com.smallnine.apiserver.exception.ConcurrentOAuthRegistrationException}，
     * 由呼叫端用 {@link #findByGoogleUid(String)} 在新 transaction 裡 re-fetch。
     */
    User findOrCreateGoogleUser(GoogleProfile profile);

    /**
     * 在獨立 (readOnly) transaction 裡用 google_uid 查使用者。
     * 主要用途是 {@link #findOrCreateGoogleUser(GoogleProfile)} 撞到 race condition
     * 時的恢復查詢；當作一般 lookup 也安全。
     */
    Optional<User> findByGoogleUid(String googleUid);
}
