package be.kul.gantry.domain;

import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
    /* static ArrayList<Integer> order = new ArrayList<>(
             Arrays.asList(0, -1, 1, -2, 2, -3, 3, -4, 4, -5, 5, -6, 6, -7, 7, -8, 8, -9, 9));*/
    static ArrayList<Coordinaat> offsetEdge = new ArrayList<>();
    static ArrayList<Coordinaat> offsetCenter = new ArrayList<>();
    static ArrayList<Item> output = new ArrayList<>();
    static ArrayList<String> outputLog = new ArrayList<>();
    public static double timer = 0;

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
        long curt = System.currentTimeMillis();
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
            if (s.getItem() != null && s.getType() == Slot.SlotType.STORAGE) {

                storage[(s.getCenterX() - 5) / 10][(s.getCenterY() - 5) / 10].add(s.getZ(), s.getItem());
                hashMap.put(s.getItem(), new Coordinaat(((s.getCenterX() - 5) / 10), (s.getCenterY() - 5) / 10));
            } else if (s.getType() == Slot.SlotType.INPUT) {
                prob.setInputSlot(s);
            } else if (s.getType() == Slot.SlotType.OUTPUT) {
                prob.setOutputSlot(s);
            }
        }
        System.out.println(prob.getInputJobSequence());
        System.out.println(hashMap.toString());
        // outputLog.add(prob.getGantries().get(0).toLog());
        for (Job job : prob.getOutputJobSequence()) {
            if (hashMap.containsKey(job.getItem())) {
                getFromStacked(getStack(hashMap.get(job.getItem())), job.getItem());
                moveItemToOutput(job.getItem());
            } else {
                while (!job.isFinished()) {
                    if (prob.getInputJobSequence().get(0).getItem().getId() == job.getItem().getId()) {
                        hashMap.put(prob.getInputJobSequence().get(0).getItem(), prob.getInputSlotCoordinaat());

                        moveItemToOutput(job.getItem());
                        output.add(job.getItem());
                        prob.getInputJobSequence().remove(0);

                        job.setFinished(true);
                    } else if (prob.getOutputJobSequenceItemId().contains(prob.getInputJobSequence().get(0).getItem().getId())) {
                        hashMap.put(prob.getInputJobSequence().get(0).getItem(), prob.getInputSlotCoordinaat());
                        moveItemCloseToExit(prob.getInputJobSequence().get(0).getItem());
                        prob.getInputJobSequence().remove(0);
                    } else {
                        hashMap.put(prob.getInputJobSequence().get(0).getItem(), prob.getInputSlotCoordinaat());

                        moveItemCloseToEntrance(prob.getInputJobSequence().get(0).getItem());
                        prob.getInputJobSequence().remove(0);

                    }

                }
            }
        }
        outputLog.add(prob.getGantries().get(0).toLog());

        System.out.println(System.currentTimeMillis() - curt);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));
            for (String s : outputLog) {
                bw.write(s + "\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis() - curt);

        System.out.println(output);
    }

    private static void moveItemToOutput(Item item) {
        moveItem(item, prob.getOutputSlotCoordinaat());
        System.out.println(item);
        output.add(item);

    }

    private static void moveItemCloseToEntrance(Item item) {
        int highestValue = Integer.MIN_VALUE;
        Coordinaat highestValueCoord = new Coordinaat(-1, -1);
        for (int i = 0; i < offsetEdge.size(); i++) {
            int x = offsetEdge.get(i).getX();
            int y = offsetEdge.get(i).getY();
            if (isValidYValue(y)) {
                ArrayList<Item> stack = storage[x][y];
                if (stack.size() < 4) {
                    boolean containsOutputItems = false;
                    int highestValueInStack = Integer.MIN_VALUE;

                    for (int j = stack.size() - 1; j >= 0; j--) {
                        if (prob.getOutputJobSequenceItemId().contains(stack.get(j).getId())) {
                            containsOutputItems = true;
                            if (highestValueInStack <= prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId())) {
                                highestValueInStack = prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId());

                            }
                        }
                    }
                    if (containsOutputItems) {
                        if (highestValue < highestValueInStack) {
                            highestValue = highestValueInStack;
                            highestValueCoord.setX(x);
                            highestValueCoord.setY(y);
                        }
                    } else {
                        Coordinaat coord = new Coordinaat(x, y);

                        moveItem(item, coord);
                        return;
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
            int x = layoutX - offsetEdge.get(i).getX();
            if (isValidYValue(y)) {
                ArrayList<Item> stack = storage[x][y];

                if (stack.size() < 4) {
                    boolean containsOutputItems = false;
                    int highestValueInStack = Integer.MIN_VALUE;

                    for (int j = stack.size() - 1; j >= 0; j--) {
                        if (prob.getOutputJobSequenceItemId().contains(stack.get(j).getId())) {
                            containsOutputItems = true;
                            if (highestValueInStack <= prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId())) {
                                highestValueInStack = prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId());

                            }
                        }
                    }
                    if (containsOutputItems) {
                        if (highestValue < highestValueInStack) {
                            highestValue = highestValueInStack;
                            highestValueCoord.setX(x);
                            highestValueCoord.setY(y);
                        }
                    } else {
                        Coordinaat coord = new Coordinaat(x, y);

                        moveItem(item, coord);
                        return;
                    }
                }
            }
        }
        moveItem(item, highestValueCoord);

    }

    private static void moveItem(Item item, Coordinaat coord) {
        Coordinaat c = null;
        System.out.println(item);
        System.out.println(hashMap.containsKey(item));
        if (hashMap.containsKey(item)) {
            c = hashMap.get(item);
            if (isValidXValue(c.getX()) && isValidYValue(c.getY())) {
                storage[c.getX()][c.getY()].remove(item);
            }

        }
        outputLog.add(prob.getGantries().get(0).toLog());
        timer = timer + prob.getGantries().get(0).moveGantry(c);
        outputLog.add(prob.getGantries().get(0).toLog());
        timer = timer + prob.getPickupPlaceDuration();
        prob.getGantries().get(0).setItem(item);
        outputLog.add(prob.getGantries().get(0).toLog());

        if (isValidXValue(coord.getX()) && isValidYValue(coord.getY())) {
            ArrayList<Item> stack = storage[coord.getX()][coord.getY()];
            stack.add(item);
        }
        timer = timer + prob.getGantries().get(0).moveGantry(coord);
        outputLog.add(prob.getGantries().get(0).toLog());
        timer = timer + prob.getPickupPlaceDuration();
        prob.getGantries().get(0).setItem(null);
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
                verplaats(stacked.get(stacked.size() - 1));
                // stacked.remove(stacked.size() - 1);
            }
        }
        //TODO: Verplaats naar output
        // System.out.println("verplaats naar output");
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

        for (int i = 1; i < offsetCenter.size(); i++) {
            int x = coord.getX() + offsetCenter.get(i).getX();
            int y = coord.getY() + offsetCenter.get(i).getY();
            if (isValidXValue(x) && isValidYValue(y)) {
                ArrayList<Item> stack = storage[x][y];
                if (stack.size() < 4) {
                    //TODO change order for loop, then change contains method
                    boolean containsOutputItems = false;
                    int highestValueInStack = Integer.MIN_VALUE;

                    for (int j = stack.size() - 1; j >= 0; j--) {
                        if (prob.getOutputJobSequenceItemId().contains(stack.get(j).getId())) {
                            containsOutputItems = true;
                            if (highestValueInStack <= prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId())) {
                                highestValueInStack = prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId());

                            }
                        }
                    }
                    if (containsOutputItems) {
                        if (highestValue < highestValueInStack) {
                            highestValue = highestValueInStack;
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
        System.out.println();
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