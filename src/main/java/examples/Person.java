package examples;

import annotation.JavaBean;

import java.util.List;

@JavaBean
public class Person {
    /** Properties **/
    private boolean deceased = false;

    private List<String> list;

    /** Property "name", readable/writable. */
    private String name = null;
}