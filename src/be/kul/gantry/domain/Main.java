package be.kul.gantry.domain;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    static ArrayList<Item>[][] storage;

    public static void main(String[] args) {
        HashMap<Item, Coordinaat> hashMap = new HashMap<>();
        File file = new File("1_10_100_4_FALSE_65_50_50.json");
        Problem prob = null;
        try {
            prob = Problem.fromJson(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        storage = new ArrayList[(prob.getMaxX() + prob.getMinX()) / 10][prob.getMaxY() / 10];
        for (int i = 0; i < storage.length; i++) {
            for (int j = 0; j < storage[0].length; j++) {
                storage[i][j] = new ArrayList<>();
            }
        }
        for (Slot s : prob.getSlots()) {
            try {
                storage[(s.getCenterX() - 5) / 10][(s.getCenterY() - 5) / 10].add(s.getZ(), s.getItem());
            } catch (Exception e) {
               /* System.out.println(s);
                System.err.println(e.getMessage());*/
            }
            if (s.getItem() != null) {
                hashMap.put(s.getItem(), new Coordinaat(((s.getCenterX() - 5) / 10), (s.getCenterY() - 5) / 10));
            }
        }

        for (Job job : prob.getOutputJobSequence()) {
            if (hashMap.containsKey(job.getItem())) {

            }
        }
        System.out.println(prob.getInputJobSequence());
        for (Job job : prob.getOutputJobSequence()) {

            if (hashMap.containsKey(job.getItem())) {
                System.out.println(job);
                getFromStacked(getStack(hashMap.get(job.getItem())), job.getItem());
            } else {
                //  System.out.println(job);
                while (!job.isFinished()) {
                    if (prob.getInputJobSequence().get(0).getItem().getId() == job.getItem().getId()) {
                        job.isFinished();
                        System.out.println(job);
                        job.setFinished(true);
                    } else {
                        hashMap.put(prob.getInputJobSequence().get(0).getItem(), new Coordinaat(0, 0));
                        prob.getInputJobSequence().remove(0);
                        System.err.println(prob.getInputJobSequence().size());
                    }
                }
            }
        }
        System.out.println(prob.getGantries());
    }

    public static ArrayList<Item> getStack(Coordinaat coordinaat) {
        int x;
        int y;
        x = coordinaat.getX();
        y = coordinaat.getY();
        return storage[x][y];


    }

    public static void getFromStacked(ArrayList<Item> stacked, Item item) {
        boolean found = false;
        while (!found) {
            if (stacked.get(stacked.size() - 1).getId() == item.getId()) {
                found = true;
            } else {
                //TODO: Verplaats
                System.out.println("verplaats: ");
                stacked.remove(stacked.size() - 1);
            }
        }
        //TODO: Verplaats naar output
        System.out.println("verplaats naar output");

    }
}
