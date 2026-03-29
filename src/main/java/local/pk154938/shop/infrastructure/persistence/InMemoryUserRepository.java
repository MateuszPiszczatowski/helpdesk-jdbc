package local.pk154938.shop.infrastructure.persistence;

import local.pk154938.shop.application.repository.UserRepository;
import local.pk154938.shop.domain.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    private final List<User> users = new ArrayList<>();

    @Override
    public void save(User user) {
        if(findByUsername(user.getUsername()).isPresent())
            throw new IllegalStateException("BŁĄD: Użytkownik "+user.getUsername()+" już istnieje!");
        users.add(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(users);
    }

    @Override
    public void delete(String username) {
        users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
    }
}
