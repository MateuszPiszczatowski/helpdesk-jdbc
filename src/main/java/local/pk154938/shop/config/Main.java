package local.pk154938.shop.config;
import local.pk154938.shop.application.service.UserService;
import local.pk154938.shop.application.session.Session;
import local.pk154938.shop.ui.menu.MainMenu;

public class Main {
    public static void main(String[] args) {
        Session session = new Session();
        UserService userService = new UserService();
        MainMenu menu = new MainMenu(userService, session);
        menu.show();
    }
}