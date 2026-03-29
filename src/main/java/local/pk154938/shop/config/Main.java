package local.pk154938.shop.config;
import local.pk154938.shop.application.repository.UserRepository;
import local.pk154938.shop.application.service.UserService;
import local.pk154938.shop.application.session.Session;
import local.pk154938.shop.domain.user.Role;
import local.pk154938.shop.domain.user.User;
import local.pk154938.shop.infrastructure.persistence.InMemoryUserRepository;
import local.pk154938.shop.ui.menu.MainMenu;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Session session = new Session();
        UserRepository userRepository = new InMemoryUserRepository();
        UserService userService = new UserService(userRepository);
        User user = new User("Admin", "Admin", Set.of(Role.ADMIN));
        userRepository.save(user);
        MainMenu menu = new MainMenu(userService, session);
        menu.show();
    }
}