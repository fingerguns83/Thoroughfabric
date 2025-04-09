package net.fg83.thoroughfabric;

import java.util.List;

public class TFConfig {
    // Step Counts
    public int grassReps = 20;
    public int coarseDirtReps = 50;
    public boolean pathsWear = true;
    public int pathReps = 1000;

    // Mob modifiers
    public int camelMulti = 3;
    public int donkeyMulti = 2;
    public int horseMulti = 3;
    public int llamaMulti = 2;
    public int muleMulti = 2;
    public int pigMulti = 1;
    public int striderMulti = 1;

    // Area Restriction
    public boolean useAreaRestrictions = false;
    public List<String> areaIds = List.of("desirepathable");

}
