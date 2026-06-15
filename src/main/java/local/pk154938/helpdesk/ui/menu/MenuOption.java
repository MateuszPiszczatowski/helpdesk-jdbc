package local.pk154938.helpdesk.ui.menu;

import local.pk154938.helpdesk.application.auth.Operation;

public class MenuOption {
    private final String label;
    private final Runnable action;

    public MenuOption(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    public String getLabel() { return label; }
    public Runnable getAction() { return action; }
}
