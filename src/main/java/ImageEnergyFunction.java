/*
 * ImageEnergyFunction.java
 *
 * Created on den 3 september 2007, 00:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */


import java.awt.*;
import java.util.Set;

/**
 *
 * @author Administrat�r
 */
public interface ImageEnergyFunction {
    public int[][] transform(Set<Point> facePoints, int[][] img);
}