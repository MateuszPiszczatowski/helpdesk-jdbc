package local.pk154938.helpdesk.application.repository;

import local.pk154938.helpdesk.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    void save(User user);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void delete(UUID id);
}
