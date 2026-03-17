package com.pareto.pactum_challenge.service;

import com.pareto.pactum_challenge.entity.User;
import com.pareto.pactum_challenge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User createGuest() {
        User user = new User();
        user.setName("Guest " + new Random().nextInt(10000));
        return userRepository.save(user);
    }
}
