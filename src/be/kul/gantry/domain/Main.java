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
        ArrayList<Item>[][] a = new ArrayList[100][200];
    }
}
