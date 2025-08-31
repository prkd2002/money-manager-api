package com.rustytech.moneymanager.controller;

import com.rustytech.moneymanager.dtos.AuthDto;
import com.rustytech.moneymanager.dtos.ProfileDto;
import com.rustytech.moneymanager.exceptions.UserAlreadyExistException;
import com.rustytech.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDto> registryProfile(@RequestBody ProfileDto profileDto){
        try{
            var newProfile = profileService.registerNewProfile(profileDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newProfile);

        } catch (UserAlreadyExistException e) {
            throw new UserAlreadyExistException(e.getMessage());
        }
    }

    @GetMapping("activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token){
        boolean isActive = profileService.activateProfile(token);
        if(isActive){
            return ResponseEntity.ok("Profile activated sucessfully !");
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation token not found or already used");
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody AuthDto authDto){
        try{
            if(!profileService.isAccountActive(authDto.getEmail())){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Account is not active. Please activate your account first"));
            }
            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDto);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch(Exception e){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));

        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDto> getProfile(){
        ProfileDto profileDto = profileService.getPublicProfile(null);
        return ResponseEntity.status(HttpStatus.OK).body(profileDto);
    }



}
