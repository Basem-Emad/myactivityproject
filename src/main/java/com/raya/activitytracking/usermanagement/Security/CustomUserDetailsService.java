package com.raya.activitytracking.usermanagement.Security;

import com.raya.activitytracking.usermanagement.entity.User_;
import com.raya.activitytracking.usermanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class CustomUserDetailsService implements UserDetailsService {

    public final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws
            UsernameNotFoundException { User_ user = userRepository.findByUserName(username)
            .orElseThrow(() ->
                    new UsernameNotFoundException(
                            "User not found with userName: " + username
                    )
            );
        return new CustomUserDetails(user);
    }
}
