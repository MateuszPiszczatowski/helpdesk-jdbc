package local.pk154938.shop.application.service;

import local.pk154938.shop.application.repository.UserRepository;
import local.pk154938.shop.domain.user.User;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public Optional<User> login(String username){
        return userRepository.findByUsername(username);
    }
    public void removeUser(String username) {
        userRepository.delete(username);
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
