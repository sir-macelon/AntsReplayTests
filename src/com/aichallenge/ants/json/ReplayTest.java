package com.aichallenge.ants.json;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.aichallenge.ants.MyBot;

/**
 * Test class for ant replays.
 */
public class ReplayTest extends TestCase {
    // make sure the path points out to an existing replay
    private static final String REPLAY_FILE = "../Ants/aichallenge/ants/game_logs/0.replay";
    
    /**
     * Tests a replay.
     */
    @Test
    public void testReplay() {
        try {
            ReplayParser parser = new ReplayParser(REPLAY_FILE, 0);
            parser.parse();
            List<String> input = parser.getInput();
            MyBot myBot = new MyBot();
            for (String line : input) {
                // add conditional break point here, i.e. line.equals("turn 123")
                myBot.processLine(line);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
