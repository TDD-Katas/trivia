package com.adaptionsoft.games.trivia.runner;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by julianghionoiu on 19/11/2015.
 */
public class Players extends ArrayList<String> {

    public Players(String... names) {
        super(Arrays.asList(names));
    }
}
