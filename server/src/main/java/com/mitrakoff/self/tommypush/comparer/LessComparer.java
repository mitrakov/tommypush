package com.mitrakoff.self.tommypush.comparer;

@Deprecated
public class LessComparer implements Comparer {
    public boolean compare(double x, double y) {return x <= y;}
    public String toString() {return "≤";}
}
