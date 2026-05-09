package com.nihit.craft_connect.config;

import com.nihit.craft_connect.repository.UserRepository;
import com.nihit.craft_connect.userdetail.UserUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Searching for user with email: " + username);

        return userRepo.findByEmail(username)
                .map(UserUserDetail::new)
                .orElseThrow(() -> {
                    System.out.println("User not found for email: " + username);
                    return new UsernameNotFoundException("No user found");
                });
    }
}
