package com.sportsync.user;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public User getUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }
}
