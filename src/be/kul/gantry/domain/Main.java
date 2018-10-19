package be.kul.gantry.domain;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        File file = new File("1_10_100_4_FALSE_65_50_50.json");
        Problem prob = null;
        try {
            prob = Problem.fromJson(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(prob.toString());
        ArrayList<Item>[][] a = new ArrayList[(prob.getMaxX() + prob.getMinX()) / 10][prob.getMaxY() / 10];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                a[i][j] = new ArrayList<>();
            }
        }
        System.out.println(prob.getSlots());
        for (Slot s : prob.getSlots()) {
            try {
                a[(s.getCenterX() - 5) / 10][(s.getCenterY() - 5) / 10].add(s.getZ(), s.getItem());
            } catch (Exception e) {
                System.out.println(s);
                System.err.println(e.getMessage());
            }
        }
        /*for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                System.out.print(a[i][j]+"\t\t");
            }
            System.out.println();
        }*/

        System.out.println("jkfldjko");
        System.out.println(prob.getInputJobSequence());
        System.out.println(prob.getGantries());
    }
}
