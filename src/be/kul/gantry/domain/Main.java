package be.kul.gantry.domain;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
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
        ArrayList<Item>[][] a = new ArrayList[(prob.getMaxX() + prob.getMinX()) / 10][prob.getMaxY() / 10];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                a[i][j] = new ArrayList<>();
            }
        }
        for (Slot s : prob.getSlots()) {
            try {
                a[(s.getCenterX() - 5) / 10][(s.getCenterY() - 5) / 10].add(s.getZ(), s.getItem());
            } catch (Exception e) {
               /* System.out.println(s);
                System.err.println(e.getMessage());*/
            }
            if (s.getItem() != null) {
                hashMap.put(s.getItem(), new Coordinaat(((s.getCenterX() - 5) / 10), (s.getCenterY() - 5) / 10));
            }
        }

        for (Job job: prob.getOutputJobSequence()) {
            if(hashMap.containsKey(job.getItem())) {

            }
        }
        System.out.println(prob.getInputJobSequence());
        for (Job job : prob.getOutputJobSequence()) {

            if (hashMap.containsKey(job.getItem())) {
                System.out.println(job);
            } else {
              //  System.out.println(job);
                while(!job.isFinished()){
                    if(prob.getInputJobSequence().get(0).getItem().getId()==job.getItem().getId()){
                        job.isFinished();
                        System.out.println(job);
                        job.setFinished(true);
                    }else{
                        hashMap.put(prob.getInputJobSequence().get(0).getItem(),new Coordinaat(0,0));
                        prob.getInputJobSequence().remove(0);
                        System.err.println(prob.getInputJobSequence().size());
                    }
                }
            }
        }
        System.out.println(prob.getGantries());
    }
    public void getFromStacked(ArrayList<Item> stacked){
        if(stacked.size()>1){
            for(int i=stacked.size()-1;i>0;i--){
                verplaats(stacked.get(i));
                stacked.remove(i);
            }
        }

    }
}
