package com.yisy.modules.doramcu.fullthrust.model;

/**
 * Model class for board obstacles (asteroids, etc.).
 */
public class BoardObstacle {

    private Long id;
    private String name;
    private int positionQ;
    private int positionR;

    public BoardObstacle() {}

    public BoardObstacle(String name, int positionQ, int positionR) {
        this.name = name;
        this.positionQ = positionQ;
        this.positionR = positionR;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPositionQ() { return positionQ; }
    public void setPositionQ(int positionQ) { this.positionQ = positionQ; }
    public int getPositionR() { return positionR; }
    public void setPositionR(int positionR) { this.positionR = positionR; }
}
