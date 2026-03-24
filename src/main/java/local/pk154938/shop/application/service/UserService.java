package local.pk154938.shop.application.service;

import local.pk154938.shop.domain.user.User;

public class UserService {
    public User login(String username, String password){
        if(username == null || username.isBlank()) return null;
        if(password == null || password.isBlank()) return null;
        return new User(username, password);
    }
}
