package com.comp3260;

public class AESInterface {
    public static void main(String[] args) {
        // Check input for file name
        if (args.length < 1) {
            System.out.println("Error: No input file provided. Please run again with a input param.");
        } else {
            // Call non static interface
            Interface intFace = new Interface();
            intFace.run(args);
        }
    }
}
