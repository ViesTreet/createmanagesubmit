package com.vt.createmanagesubmit.config;

import org.apache.poi.sl.usermodel.ShapeType;
import java.awt.Color;

public class ShapeConfig {
    
    private ShapeType shapeType;
    private Color color;
    
    public ShapeConfig() {
    }

    public ShapeConfig(ShapeType shapeType, Color color) {
        this.shapeType = shapeType;
        this.color = color;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    
    
}
