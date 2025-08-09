package com.rustytech.moneymanager.service;

import com.rustytech.moneymanager.dtos.AuthDto;
import com.rustytech.moneymanager.dtos.ProfileDto;
import com.rustytech.moneymanager.entity.Profile;
import com.rustytech.moneymanager.exceptions.UserAlreadyExistException;
import com.rustytech.moneymanager.repository.ProfileRepository;
import com.rustytech.moneymanager.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Value("${app.activation.url}")
    private String activationUrl;

    public ProfileDto registerNewProfile(ProfileDto profileDto) {
        var existingProfile = profileRepository.findByEmail(profileDto.getEmail());
        if(existingProfile.isPresent()) {
            throw new UserAlreadyExistException("Es existiert bereits ein Profile mit dieser E-mail adresse " + profileDto.getEmail());
        }
        var newProfile = toEntity(profileDto);
        newProfile.setActivateToken(UUID.randomUUID().toString());
        newProfile.setPassword(passwordEncoder.encode(newProfile.getPassword()));
        newProfile = profileRepository.save(newProfile);
        String activateLink =activationUrl+ "/api/v1.0/activate?token=" + newProfile.getActivateToken();
        String subject = "Activate your Money Manager Account";
        String body = "Click on the following link to activate your Money Manager account: "+ activateLink;
        emailService.sendEmail(profileDto.getEmail(), subject, body);

        return toDto(newProfile);
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivateToken(activationToken).map(
                profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                }
        ).orElse(false);
    }

    public boolean isAccountActive(String email){
        return profileRepository.findByEmail(email).map(Profile::getIsActive).orElse(false);
    }

    public Profile getCurrentProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return profileRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found!")); // Typo korrigiert
    }

    public ProfileDto getPublicProfile(String email) {
        Profile currentProfile;
        if(email == null){
            currentProfile = getCurrentProfile();
        } else {
            currentProfile = profileRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }
        return ProfileDto.builder()
                .id(currentProfile.getId())
                .fullName(currentProfile.getFullName())
                .email(currentProfile.getEmail())
                .imageUrl(currentProfile.getImageUrl())
                .createdAt(currentProfile.getCreatedAt())
                .updatedAt(currentProfile.getUpdatedAt()).build();
    }

    private Profile toEntity(ProfileDto profileDto) {
        return Profile.builder()
                .id(profileDto.getId())
                .fullName(profileDto.getFullName())
                .email(profileDto.getEmail())
                .password(profileDto.getPassword())
                .imageUrl(profileDto.getImageUrl())
                .createdAt(profileDto.getCreatedAt())
                .updatedAt(profileDto.getUpdatedAt())
                .build();
    }

    private ProfileDto toDto(Profile profile) {
        return ProfileDto.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .imageUrl(profile.getImageUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt()).build();
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDto authDto) {
        try {
            // Benutzer authentifizieren
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword())
            );

            // Überprüfen ob das Konto aktiviert ist
            if (!isAccountActive(authDto.getEmail())) {
                throw new RuntimeException("Account is not activated. Please check your email.");
            }

            // Token generieren
            String token = jwtUtils.generateTokenFromUsername(authDto.getEmail());

            // FEHLER KORRIGIERT: Direkt den generierten Token verwenden
            return Map.of(
                    "token", token,  // Hier war der Fehler: authDto.getToken() -> token
                    "user", getPublicProfile(authDto.getEmail())
            );

        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password: " + e.getMessage());
        }
    }
}