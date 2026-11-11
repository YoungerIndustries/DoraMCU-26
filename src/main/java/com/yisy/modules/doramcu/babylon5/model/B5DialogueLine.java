package com.yisy.modules.doramcu.babylon5.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single line in a Babylon 5 dialogue scene.
 */
public class B5DialogueLine {

    @JsonProperty("speaker")
    private String character;

    @JsonProperty("line")
    private String text;

    public B5DialogueLine() {
    }

    public B5DialogueLine(String character, String text) {
        this.character = character;
        this.text = text;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "B5DialogueLine{character='" + character + "', text='" + text + "'}";
    }
}
