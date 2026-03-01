package com.example.jobPortal.security;

import com.example.jobPortal.entity.AuthProvider;
import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.Role;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.CandidateProfileRepository;
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

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        return new CustomOAuth2User(oAuth2User);
    }

    public User processOAuthUser(CustomOAuth2User customOAuth2User) {
        String email = customOAuth2User.getEmail();
        return userRepository.findByEmail(email)
                .map(existing -> {
                    existing.setAuthProvider(AuthProvider.GOOGLE);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User user1 = new User();
                    user1.setAuthProvider(AuthProvider.GOOGLE);
                    user1.setPassword(null);
                    user1.setEmail(email);
                    user1.setRole(Role.CANDIDATE);
                    user1 = userRepository.save(user1);

                    // Auto-create candidate profile for new OAuth users
                    CandidateProfile profile = new CandidateProfile();
                    profile.setUser(user1);
                    profile.setFullName(customOAuth2User.getAttribute("name") != null
                            ? customOAuth2User.getAttribute("name")
                            : "");
                    candidateProfileRepository.save(profile);

                    return user1;
                });
    }
}
