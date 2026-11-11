package com.yisy.modules.doramcu.babylon5.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a Babylon 5 dialogue scene with multiple characters and lines.
 */
public class B5Dialogue {

    private String id;
    private String title;
    private String scene;
    private List<String> characters;

    @JsonProperty("dialogue")
    private List<B5DialogueLine> lines;

    public B5Dialogue() {
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

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public List<String> getCharacters() {
        return characters;
    }

    public void setCharacters(List<String> characters) {
        this.characters = characters;
    }

    public List<B5DialogueLine> getLines() {
        return lines;
    }

    public void setLines(List<B5DialogueLine> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "B5Dialogue{id='" + id + "', title='" + title + "'}";
    }
}
