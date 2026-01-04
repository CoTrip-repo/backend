package com.ssafy.cotrip.security.service;

import com.ssafy.cotrip.api.user.repository.UserMapper;
import com.ssafy.cotrip.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userMapper.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("회원을 찾을 수 없습니다.");
        }

        return new CustomUserDetails(user);
    }
}
