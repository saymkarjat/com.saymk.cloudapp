package com.saymk.cloud6x.minio.listener;

import com.saymk.cloud6x.minio.exception.FolderAlreadyExistException;
import com.saymk.cloud6x.minio.service.StorageService;
import com.saymk.cloud6x.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationSuccessListener {

    private final StorageService storageService;

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        UserDetails principal = (UserDetails) event.getAuthentication().getPrincipal();

        try {
            storageService.createInitialFolderForNewUser(getUserId(principal));
        } catch (FolderAlreadyExistException e) {
            log.warn("Корневая папка для пользователя уже существует");
        }
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof UserDetailsImpl details) {
            return details.getId();
        }
        throw new IllegalStateException("Cannot extract user ID from UserDetails");
    }
}
