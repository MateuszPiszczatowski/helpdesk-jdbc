package local.pk154938.helpdesk.application.session;

import local.pk154938.helpdesk.domain.user.User;

public class Session {
    private User currentUser;

    public void login(User user){
        currentUser = user;
    }

    public void logout(){
        currentUser = null;
    }

    public User getCurrentUser(){
        return currentUser;
    }

    public boolean isLoggedIn(){
        return currentUser != null;
    }
}
