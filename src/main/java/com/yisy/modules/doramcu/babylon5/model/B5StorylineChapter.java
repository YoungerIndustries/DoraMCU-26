package com.yisy.modules.doramcu.babylon5.model;

import java.util.List;

/**
 * Represents a chapter in a Babylon 5 storyline, with season number, title, and key events.
 */
public class B5StorylineChapter {

    private int season;
    private String title;
    private String description;
    private List<String> keyEvents;

    public B5StorylineChapter() {
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getKeyEvents() {
        return keyEvents;
    }

    public void setKeyEvents(List<String> keyEvents) {
        this.keyEvents = keyEvents;
    }

    @Override
    public String toString() {
        return "B5StorylineChapter{season=" + season + ", title='" + title + "'}";
    }
}
