package de.ws1718.ismla.JapaneseHelper.client;

import java.util.ArrayList;

import de.ws1718.ismla.JapaneseHelper.server.GreetingServiceImpl;
public class GlossesTest {
    public static void main(String[] args) {
        GreetingServiceImpl gs = new GreetingServiceImpl();
        ArrayList<String> results1 = gs.processGlossesTester("1) a land, a large place 2) a country in general, a region 3) a country as in a nation, a state 4) the office of emperor, the crown; affair s of state 5) a province of ancient Japan 6) the national  government in ancient Japan; the national capital in ancient Japan 7) one's birthplace");
        ArrayList<String> results2 = gs.processGlossesTester("1) ? 2) to sleep 3) to leave");
        ArrayList<String> results3 = gs.processGlossesTester("1) ? 2) ? 3) ? 4) loss 5) barrel");

        for (String g : results1) {
            System.out.println(g);
        }

        // Shouldn't print the ? mark
        for (String g : results2) {
            System.out.println(g);
        }

        for (String g : results3) {
            System.out.println(g);
        }
    }

}
