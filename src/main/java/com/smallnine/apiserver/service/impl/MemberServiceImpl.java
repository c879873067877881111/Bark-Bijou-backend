package com.smallnine.apiserver.service.impl;

import com.smallnine.apiserver.constants.enums.ResponseCode;
import com.smallnine.apiserver.dao.UserDao;
import com.smallnine.apiserver.entity.User;
import com.smallnine.apiserver.exception.BusinessException;
import com.smallnine.apiserver.service.FileStorageService;
import com.smallnine.apiserver.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    public String updateProfile(User user, String username, String realname, String email,
                                String birthDate, String gender, String phone, MultipartFile avatar) {
        if (username != null && !username.isBlank()) user.setUsername(username);
        if (realname != null) user.setRealname(realname);
        if (email != null && !email.isBlank()) user.setEmail(email);
        if (gender != null) {
            try { user.setGender(User.Gender.valueOf(gender)); } catch (Exception ignored) {}
        }
        if (phone != null) user.setPhone(phone);
        if (birthDate != null && !birthDate.isEmpty()) {
            user.setBirthDate(java.time.LocalDate.parse(birthDate));
        }

        if (avatar != null && !avatar.isEmpty()) {
            String storedPath = fileStorageService.store(avatar, "member_images");
            String filename = storedPath.substring(storedPath.lastIndexOf('/') + 1);
            user.setImageUrl("/member/member_images/" + filename);
        }

        userDao.updateProfile(user);
        return user.getImageUrl();
    }

    @Override
    public void changePassword(Long memberId, Long authenticatedUserId, String currentPassword, String newPassword) {
        if (!authenticatedUserId.equals(memberId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN, "無權限修改他人密碼");
        }

        User user = userDao.findById(memberId)
                .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ResponseCode.INVALID_PASSWORD, "舊密碼錯誤");
        }

        if (newPassword.length() < 6) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "密碼長度至少6位");
        }

        userDao.updatePassword(memberId, passwordEncoder.encode(newPassword));
    }
}
