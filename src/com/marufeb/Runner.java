package com.marufeb;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public class Runner {
    public static void main(String[] args) throws FileNotFoundException {
//        final File lines = new File(parseFile("WMRlines.csv").getFile());
        final File lines = new File("/Users/fabiomaruca/Documents/GitHub/West Midlands Railway (WMR) Network Route Finder/resources/WMRlines.csv");
//        final File accesses = new File(parseFile("WMRstationsWithStepFreeAccess.csv").getFile());
        final File accesses = new File("/Users/fabiomaruca/Documents/GitHub/West Midlands Railway (WMR) Network Route Finder/out/production/West Midlands Railway (WMR) Network Route Finder/WMRstationsWithStepFreeAccess.csv");
        final ControllerImpl controller = new ControllerImpl(lines, accesses);
//        System.out.println(controller.testGraph());
        new TUI(controller);
    }

    public static URL parseFile(String name) {
        return Runner.class.getResource("/"+name);
    }
}
