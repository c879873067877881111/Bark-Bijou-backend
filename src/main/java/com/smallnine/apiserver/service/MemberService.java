package com.smallnine.apiserver.service;

import com.smallnine.apiserver.entity.User;
import org.springframework.web.multipart.MultipartFile;

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
}
