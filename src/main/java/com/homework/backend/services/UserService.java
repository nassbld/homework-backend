package com.homework.backend.services;

import com.homework.backend.dto.ProfileUpdateRequest;
import com.homework.backend.models.Role;
import com.homework.backend.models.User;
import com.homework.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User updateUserProfile(Long userId, ProfileUpdateRequest request) {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));

        userToUpdate.setFirstName(request.firstName());
        userToUpdate.setLastName(request.lastName());

        if (Role.TEACHER.equals(userToUpdate.getRole())) {
            userToUpdate.setBio(request.bio());
        }

        return userRepository.save(userToUpdate);
    }
}
