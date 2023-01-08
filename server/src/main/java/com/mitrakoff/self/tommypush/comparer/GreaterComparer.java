package com.mitrakoff.self.tommypush.comparer;

public class GreaterComparer implements Comparer {
    public boolean compare(double x, double y) {return x >= y;}
    public String toString() {return "≥";}
}
