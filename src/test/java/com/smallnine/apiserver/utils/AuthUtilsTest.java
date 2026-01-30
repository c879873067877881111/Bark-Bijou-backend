package com.smallnine.apiserver.utils;

import com.smallnine.apiserver.constants.enums.ResponseCode;
import com.smallnine.apiserver.entity.User;
import com.smallnine.apiserver.exception.BusinessException;
import com.smallnine.apiserver.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthUtilsTest {

    @Test
    void getAuthenticatedUser_withValidUserPrincipal_returnsUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        UserPrincipal principal = new UserPrincipal(user);

        User result = AuthUtils.getAuthenticatedUser(principal);

        assertThat(result).isSameAs(user);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getAuthenticatedUser_withNull_throwsUnauthorized() {
        assertThatThrownBy(() -> AuthUtils.getAuthenticatedUser(null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResponseCode.UNAUTHORIZED.getCode());
                });
    }

    @Test
    void getAuthenticatedUser_withNonUserPrincipal_throwsUnauthorized() {
        UserDetails nonUserPrincipal = org.springframework.security.core.userdetails.User
                .withUsername("test")
                .password("pass")
                .roles("USER")
                .build();

        assertThatThrownBy(() -> AuthUtils.getAuthenticatedUser(nonUserPrincipal))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResponseCode.UNAUTHORIZED.getCode());
                });
    }
}
