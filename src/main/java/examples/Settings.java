package examples;

import annotation.JavaBean;

@JavaBean
public class Settings {
    public static final int MAX_ITEMS = 10;
    private transient String cache;
    private final String id = "x";
    private String theme;
}