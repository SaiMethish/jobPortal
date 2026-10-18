package com.example.jobPortal.security;

import com.example.jobPortal.entity.AuthProvider;
import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.RecruiterProfile;
import com.example.jobPortal.entity.Role;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.RecruiterProfileRepository;
import com.example.jobPortal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        return new CustomOAuth2User(oAuth2User);
    }

    /**
     * Finds or creates a User for the given OAuth identity.
     * For NEW users, the desiredRole is applied and the matching profile is created.
     * For EXISTING users, the account is unchanged (their role is preserved).
     */
    public User processOAuthUser(CustomOAuth2User customOAuth2User, Role desiredRole) {
        String email = customOAuth2User.getEmail();
        return userRepository.findByEmail(email)
                .map(existing -> {
                    // Existing user — just mark them as Google-authenticated, preserve their role
                    existing.setAuthProvider(AuthProvider.GOOGLE);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    if (desiredRole == null) {
                        throw new RuntimeException("Account not found. Please register first.");
                    }

                    // New user — use the role they selected on the login page
                    User user1 = new User();
                    user1.setAuthProvider(AuthProvider.GOOGLE);
                    user1.setPassword(null);
                    user1.setEmail(email);
                    user1.setRole(desiredRole);
                    user1 = userRepository.save(user1);

                    String fullName = customOAuth2User.getAttribute("name") != null
                            ? customOAuth2User.getAttribute("name")
                            : "";

                    if (desiredRole == Role.RECRUITER) {
                        // Auto-create recruiter profile
                        RecruiterProfile profile = new RecruiterProfile();
                        profile.setUser(user1);
                        recruiterProfileRepository.save(profile);
                    } else {
                        // Auto-create candidate profile (default)
                        CandidateProfile profile = new CandidateProfile();
                        profile.setUser(user1);
                        profile.setFullName(fullName);
                        candidateProfileRepository.save(profile);
                    }

                    return user1;
                });
    }
}
