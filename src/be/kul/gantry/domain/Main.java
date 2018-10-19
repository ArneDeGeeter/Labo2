package be.kul.gantry.domain;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    static ArrayList<Item>[][] storage;
    static HashMap<Item, Coordinaat> hashMap;
    static Integer layoutX;
    static Integer layoutY;
    static Problem prob;
    static ArrayList<Integer> order = new ArrayList<>(
            Arrays.asList(0, -1, 1, -2, 2, -3, 3, -4, 4, -5, 5, -6, 6, -7, 7, -8, 8, -9, 9));
    static ArrayList<Coordinaat> offsetEdge = new ArrayList<>();
    static ArrayList<Coordinaat> offsetCenter = new ArrayList<>();
    static ArrayList<Item> output = new ArrayList<>();

    public static void main(String[] args) {
        hashMap = new HashMap<>();
        File file = new File("1_10_100_4_FALSE_65_50_50.json");
        try {
            prob = Problem.fromJson(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j + i <= 10; j++) {
                offsetEdge.add(new Coordinaat(i + 1, j + 1));
                offsetCenter.add(new Coordinaat(i - 5, j - 5));
            }
        }
        offsetEdge.sort(null);
        offsetCenter.sort(null);
        layoutX = (prob.getMaxX() + prob.getMinX()) / 10;
        layoutY = prob.getMaxY() / 10;
        storage = new ArrayList[layoutX][layoutY];
        for (int i = 0; i < storage.length; i++) {
            for (int j = 0; j < storage[0].length; j++) {
                storage[i][j] = new ArrayList<>();
            }
        }
        System.out.println(storage[0][0]);
        for (Slot s : prob.getSlots()) {
            if (s.getItem() != null) {
                try {
                    storage[(s.getCenterX() - 5) / 10][(s.getCenterY() - 5) / 10].add(s.getZ(), s.getItem());
                } catch (Exception e) {
               /* System.out.println(s);
                System.err.println(e.getMessage());*/
                }
                hashMap.put(s.getItem(), new Coordinaat(((s.getCenterX() - 5) / 10), (s.getCenterY() - 5) / 10));
            }
        }
        long curt=System.currentTimeMillis();
        System.out.println(prob.getInputJobSequence());
        System.out.println(hashMap.toString());
        for (Job job : prob.getOutputJobSequence()) {
            if (hashMap.containsKey(job.getItem())) {
                verplaats(job.getItem());
                getFromStacked(getStack(hashMap.get(job.getItem())), job.getItem());
            } else {
                while (!job.isFinished()) {
                    if (prob.getInputJobSequence().get(0).getItem().getId() == job.getItem().getId()) {
                        output.add(job.getItem());
                        job.setFinished(true);
                    } else if (prob.getOutputJobSequenceItemId().contains(prob.getInputJobSequence().get(0).getItem().getId())) {
                        moveItemCloseToExit(prob.getInputJobSequence().get(0).getItem());
                        prob.getInputJobSequence().remove(0);
                    } else {
                        moveItemCloseToEntrance(prob.getInputJobSequence().get(0).getItem());
                        prob.getInputJobSequence().remove(0);

                    }

                }
            }
        }
        System.out.println(System.currentTimeMillis()-curt);
        System.out.println(prob.getGantries());
    }

    private static void moveItemCloseToEntrance(Item item) {
        int highestValue = Integer.MIN_VALUE;
        Coordinaat highestValueCoord = new Coordinaat(-1, -1);
        for (int i = 0; i < offsetEdge.size(); i++) {
            int y = offsetEdge.get(i).getY();
            if (isValidYValue(y)) {
                ArrayList<Item> stack = storage[offsetEdge.get(i).getX()][y];
                if (stack.size() < 4) {
                    for (Item itemStack : stack) {
                        if (prob.getOutputJobSequenceItemId().contains(itemStack.getId())) {
                            if (highestValue <= prob.getOutputJobSequenceItemId().indexOf(itemStack.getId())) {
                                highestValue = prob.getOutputJobSequenceItemId().indexOf(itemStack.getId());
                                highestValueCoord.setX(offsetEdge.get(i).getX());
                                highestValueCoord.setY(y);
                            }
                        } else {
                            Coordinaat coord = new Coordinaat((offsetEdge.get(i).getX()), y);
                            moveItem(item, coord);
                            return;
                        }

                    }
                }
            }
        }
        moveItem(item, highestValueCoord);
    }


    private static void moveItemCloseToExit(Item item) {
        int highestValue = Integer.MIN_VALUE;
        Coordinaat highestValueCoord = new Coordinaat(-1, -1);
        for (int i = 0; i < offsetEdge.size(); i++) {
            int y = offsetEdge.get(i).getY();
            if (isValidYValue(y)) {
                ArrayList<Item> stack = storage[layoutX - offsetEdge.get(i).getX()][y];
                if (stack.size() < 4) {
                    for (Item itemStack : stack) {
                        if (prob.getOutputJobSequenceItemId().contains(itemStack.getId())) {
                            if (highestValue <= prob.getOutputJobSequenceItemId().indexOf(itemStack.getId())) {
                                highestValue = prob.getOutputJobSequenceItemId().indexOf(itemStack.getId());
                                highestValueCoord.setX(layoutX - offsetEdge.get(i).getX());
                                highestValueCoord.setY(y);
                            }
                        } else {
                            Coordinaat coord = new Coordinaat((layoutX - offsetEdge.get(i).getX()), y);
                            moveItem(item, coord);
                            return;
                        }

                    }
                }
            }
        }
        moveItem(item, highestValueCoord);

    }

    private static void moveItem(Item item, Coordinaat coord) {
        ArrayList<Item> stack = storage[coord.getX()][coord.getY()];
        stack.add(item);
        //TODO time+log
        hashMap.put(item, coord);
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
                verplaats(item);
                stacked.remove(stacked.size() - 1);
            }
        }
        //TODO: Verplaats naar output
        System.out.println("verplaats naar output");
        output.add(item);

    }

    public static void verplaats(Item item) {
        Coordinaat coord = hashMap.get(item);
        ArrayList<Item> stack = findNearestStack(coord, item);


    }

    private static ArrayList<Item> findNearestStack(Coordinaat coord, Item item) {
        boolean found = false;
        int highestValue = Integer.MIN_VALUE;
        Coordinaat highestValueCoord = new Coordinaat(-1, -1);

        for (int i = 0; i < offsetCenter.size(); i++) {
            int x = coord.getX() + offsetCenter.get(i).getX();
            int y = coord.getY() + offsetCenter.get(i).getY();
            if (isValidXValue(x) && isValidYValue(y)) {
                ArrayList<Item> stack = storage[x][y];
                if (stack.size() < 4) {
                    for (Item itemStack : stack) {
                        if (prob.getOutputJobSequenceItemId().contains(itemStack.getId())) {
                            if (highestValue <= prob.getOutputJobSequenceItemId().indexOf(itemStack.getId())) {
                                highestValue = prob.getOutputJobSequenceItemId().indexOf(itemStack.getId());
                                highestValueCoord.setX(x);
                                highestValueCoord.setY(y);
                            }
                        } else {
                            Coordinaat newcoord = new Coordinaat(x, y);
                            moveItem(item, newcoord);
                            return stack;
                        }

                    }
                }

            }

        }
        moveItem(item, highestValueCoord);
        return storage[highestValueCoord.getX()][highestValueCoord.getY()];

    }


    private static boolean isValidYValue(int i) {
        return i >= 0 && i < 10;
    }

    private static boolean isValidXValue(int i) {
        return i >= 0 && i < 100;
    }
}