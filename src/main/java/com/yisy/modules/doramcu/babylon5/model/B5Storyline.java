package com.yisy.modules.doramcu.babylon5.model;

import java.util.List;

/**
 * Represents a Babylon 5 storyline with chapters and key characters.
 */
public class B5Storyline {

    private String id;
    private String title;
    private String description;
    private List<Integer> seasons;
    private List<String> keyCharacters;
    private List<B5StorylineChapter> chapters;

    public B5Storyline() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<Integer> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<Integer> seasons) {
        this.seasons = seasons;
    }

    public List<String> getKeyCharacters() {
        return keyCharacters;
    }

    public void setKeyCharacters(List<String> keyCharacters) {
        this.keyCharacters = keyCharacters;
    }

    public List<B5StorylineChapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<B5StorylineChapter> chapters) {
        this.chapters = chapters;
    }

    @Override
    public String toString() {
        return "B5Storyline{id='" + id + "', title='" + title + "'}";
    }
}
