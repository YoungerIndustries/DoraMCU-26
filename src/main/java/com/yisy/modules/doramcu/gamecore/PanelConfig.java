package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Panel configuration for a UI profile.
 * Defines the four panel titles, state bar items, and action categories.
 */
public class PanelConfig {
    /** Title for the info panel (left side) */
    private final String infoPanelTitle;
    /** Title for the prompt/narrative panel (center-left) */
    private final String promptPanelTitle;
    /** Title for the responses/actions panel (center-right) */
    private final String responsesPanelTitle;
    /** Title for the actions/log panel (right side) */
    private final String actionsPanelTitle;
    /** State bar items displayed in the game state bar */
    private final List<String> stateBarItems;
    /** Action categories available in the action panel */
    private final List<String> actionCategories;

    public PanelConfig(String infoPanelTitle, String promptPanelTitle, String responsesPanelTitle,
                       String actionsPanelTitle, List<String> stateBarItems, List<String> actionCategories) {
        this.infoPanelTitle = infoPanelTitle;
        this.promptPanelTitle = promptPanelTitle;
        this.responsesPanelTitle = responsesPanelTitle;
        this.actionsPanelTitle = actionsPanelTitle;
        this.stateBarItems = stateBarItems;
        this.actionCategories = actionCategories;
    }

    public String getInfoPanelTitle() {
        return infoPanelTitle;
    }

    public String getPromptPanelTitle() {
        return promptPanelTitle;
    }

    public String getResponsesPanelTitle() {
        return responsesPanelTitle;
    }

    public String getActionsPanelTitle() {
        return actionsPanelTitle;
    }

    public List<String> getStateBarItems() {
        return stateBarItems;
    }

    public List<String> getActionCategories() {
        return actionCategories;
    }
}
