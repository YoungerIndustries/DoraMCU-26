package com.yisy.modules.doramcu.babylon5.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the six DoraMCU 2E characteristics (ST, DX, EN, IN, ED, SO)
 * for a Babylon 5 race or character.
 */
public class B5Characteristics {

    @JsonProperty("ST")
    private int ST;
    @JsonProperty("DX")
    private int DX;
    @JsonProperty("EN")
    private int EN;
    @JsonProperty("IN")
    private int IN;
    @JsonProperty("ED")
    private int ED;
    @JsonProperty("SO")
    private int SO;

    public B5Characteristics() {
    }

    public B5Characteristics(int ST, int DX, int EN, int IN, int ED, int SO) {
        this.ST = ST;
        this.DX = DX;
        this.EN = EN;
        this.IN = IN;
        this.ED = ED;
        this.SO = SO;
    }

    public int getST() {
        return ST;
    }

    public void setST(int ST) {
        this.ST = ST;
    }

    public int getDX() {
        return DX;
    }

    public void setDX(int DX) {
        this.DX = DX;
    }

    public int getEN() {
        return EN;
    }

    public void setEN(int EN) {
        this.EN = EN;
    }

    public int getIN() {
        return IN;
    }

    public void setIN(int IN) {
        this.IN = IN;
    }

    public int getED() {
        return ED;
    }

    public void setED(int ED) {
        this.ED = ED;
    }

    public int getSO() {
        return SO;
    }

    public void setSO(int SO) {
        this.SO = SO;
    }

    @Override
    public String toString() {
        return "B5Characteristics{ST=" + ST + ", DX=" + DX + ", EN=" + EN +
                ", IN=" + IN + ", ED=" + ED + ", SO=" + SO + "}";
    }
}
