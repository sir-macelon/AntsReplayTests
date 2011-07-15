package com.aichallenge.ants.json;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.aichallenge.ants.Aim;
import com.aichallenge.ants.Ants;
import com.aichallenge.ants.Tile;

/**
 * Parser for JSON Ants replays.
 * <p>
 * Please note hard-coded strings, chars and integers inside.
 */
public class ReplayParser {
    private final String filename;
    
    private final List<String> input = new LinkedList<String>();
    
    private final List<Tile> waterTiles = new LinkedList<Tile>();
    
    private final List<ParsedAnt> parsedAnts = new LinkedList<ParsedAnt>();
    
    private final List<Integer> playerMapping = new LinkedList<Integer>();
    
    private Ants ants;
    
    /**
     * Creates new {@link ReplayParser} object.
     * 
     * @param filename filename with Ants replay to be parsed
     * @param player my player id in the replayed game
     */
    public ReplayParser(String filename, int player) {
        this.filename = filename;
        playerMapping.add(player);
    }
    
    /**
     * Reads the replay and parses it.
     * 
     * @throws FileNotFoundException if the named file does not exist, is a directory rather than a
     *         regular file, or for some other reason cannot be opened for reading
     * @throws JSONException if there is a syntax error in the source string or a duplicated key
     */
    public void parse() throws FileNotFoundException, JSONException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject replay = new JSONObject(tokener);
        JSONObject replayData = replay.getJSONObject("replaydata");
        parseSetup(replayData);
        JSONObject map = replayData.getJSONObject("map");
        JSONArray data = map.getJSONArray("data");
        parseMap(data);
        JSONArray ants = replayData.getJSONArray("ants");
        parseAnts(ants);
        int gameLength = replay.getInt("game_length");
        addInput(gameLength);
    }
    
    /**
     * Returns input for Ants bot.
     * <p>
     * Please note generated input is not sorted as it shouldn't matter for the bot.
     * 
     * @return input for Ants bot
     */
    public List<String> getInput() {
        return input;
    }
    
    private void parseSetup(JSONObject replayData) throws JSONException {
        int loadTime = replayData.getInt("loadtime");
        int turnTime = replayData.getInt("turntime");
        int rows = replayData.getJSONObject("map").getInt("rows");
        int cols = replayData.getJSONObject("map").getInt("cols");
        int turns = replayData.getInt("turns");
        int viewRadius2 = replayData.getInt("viewradius2");
        int attackRadius2 = replayData.getInt("attackradius2");
        int spawnRadius2 = replayData.getInt("spawnradius2");
        long playerSeed = replayData.getLong("player_seed");
        ants =
            new Ants(loadTime, turnTime, rows, cols, turns, viewRadius2, attackRadius2,
                spawnRadius2);
        input.add("turn 0");
        input.add("loadtime " + loadTime);
        // hack for unlimited turn time for bot during debugging: just comment the line below
        input.add("turntime " + turnTime);
        input.add("rows " + rows);
        input.add("cols " + cols);
        input.add("turns " + turns);
        input.add("viewradius2 " + viewRadius2);
        input.add("attackradius2 " + attackRadius2);
        input.add("spawnradius2 " + spawnRadius2);
        input.add("player_seed " + playerSeed);
        input.add("ready");
    }
    
    private void parseMap(JSONArray mapData) throws JSONException {
        for (int row = 0; row < mapData.length(); row++) {
            String rowData = mapData.getString(row);
            for (int col = 0; col < rowData.length(); col++) {
                if (rowData.charAt(col) == '%') {
                    waterTiles.add(new Tile(row, col));
                }
            }
        }
    }
    
    private void parseAnts(JSONArray ants) throws JSONException {
        for (int i = 0; i < ants.length(); i++) {
            JSONArray antArray = ants.getJSONArray(i);
            int j = 0;
            int row = antArray.getInt(j++);
            int col = antArray.getInt(j++);
            int startTurn = antArray.getInt(j++);
            int conversionTurn = -1;
            if (antArray.length() > 4) {
                // as in the replay specification, never converted food will have only 4 parameters
                conversionTurn = antArray.getInt(j++);
            }
            int endTurn = antArray.getInt(j++);
            int player = -1;
            String moves = "";
            if (antArray.length() > 4) {
                // as in the replay specification, never converted food will have only 4 parameters
                player = antArray.getInt(j++);
                moves = antArray.getString(j);
            }
            parsedAnts.add(new ParsedAnt(row, col, startTurn, conversionTurn, endTurn, player,
                moves));
        }
    }
    
    private void addInput(int gameLength) {
        for (int turn = 0; turn < gameLength; turn++) {
            input.add("turn " + (turn + 1));
            List<ParsedAnt> myAnts = getMyAnts(turn);
            addVisibleWaterTiles(turn, myAnts);
            addVisibleAntTiles(turn, myAnts);
            addVisibleFoodTiles(turn, myAnts);
            addDeadAntTiles(turn, myAnts);
            input.add("go");
            moveAnts(turn + 1);
        }
    }
    
    private List<ParsedAnt> getMyAnts(int turn) {
        List<ParsedAnt> myAnts = new LinkedList<ParsedAnt>();
        for (ParsedAnt parsedAnt : parsedAnts) {
            if (parsedAnt.getStartTurn() > turn) {
                // food hasn't been placed on the map yet
                break;
            }
            if (parsedAnt.getConversionTurn() <= turn && parsedAnt.getEndTurn() > turn
                    && parsedAnt.getPlayer() == playerMapping.get(0)) {
                // this is my alive ant
                myAnts.add(parsedAnt);
            }
        }
        return myAnts;
    }
    
    private void addVisibleWaterTiles(int turn, List<ParsedAnt> myAnts) {
        for (Iterator<Tile> i = waterTiles.iterator(); i .hasNext(); ) {
            Tile waterTile = i.next();
            // find first ant which would see this water tile
            for (ParsedAnt myAnt : myAnts) {
                if (ants.getDistance(myAnt.getTile(), waterTile) <= ants.getViewRadius2()) {
                    input.add("w " + waterTile);
                    // remove the water tile, so it won't be indicated again
                    i.remove();
                    break;
                }
            }
        }
    }
    
    private void addVisibleAntTiles(int turn, List<ParsedAnt> myAnts) {
        for (ParsedAnt parsedAnt : parsedAnts) {
            if (parsedAnt.getStartTurn() > turn) {
                // food hasn't been placed on the map yet
                break;
            }
            if (parsedAnt.getConversionTurn() > turn || parsedAnt.getEndTurn() <= turn) {
                // food hasn't been converted yet or ant died already
                continue;
            }
            // find first ant which would see this ant tile
            for (ParsedAnt myAnt : myAnts) {
                if (ants.getDistance(myAnt.getTile(), parsedAnt.getTile()) <= ants.getViewRadius2()) {
                    if (!playerMapping.contains(parsedAnt.getPlayer())) {
                        // new enemy encountered
                        playerMapping.add(parsedAnt.getPlayer());
                    }
                    input.add("a " + parsedAnt.getTile() + " " + playerMapping.indexOf(parsedAnt.getPlayer()));
                    break;
                }
            }
        }
    }
    
    private void addVisibleFoodTiles(int turn, List<ParsedAnt> myAnts) {
        for (ParsedAnt parsedAnt : parsedAnts) {
            if (parsedAnt.getStartTurn() > turn) {
                // food hasn't been placed on the map yet
                break;
            }
            if (parsedAnt.getConversionTurn() <= turn) {
                // food has been converted already
                continue;
            }
            // find first ant which would see this food tile
            for (ParsedAnt myAnt : myAnts) {
                if (ants.getDistance(myAnt.getTile(), parsedAnt.getTile()) <= ants.getViewRadius2()) {
                    input.add("f " + parsedAnt.getTile());
                    break;
                }
            }
        }
    }
    
    private void addDeadAntTiles(int turn, List<ParsedAnt> myAnts) {
        for (ParsedAnt parsedAnt : parsedAnts) {
            if (parsedAnt.getStartTurn() > turn) {
                // food hasn't been placed on the map yet
                break;
            }
            if (parsedAnt.getEndTurn() != turn) {
                // ant didn't die this turn
                continue;
            }
            // find first ant which would see this dead ant tile
            for (ParsedAnt myAnt : myAnts) {
                if (ants.getDistance(myAnt.getTile(), parsedAnt.getTile()) <= ants.getViewRadius2()
                        || playerMapping.indexOf(parsedAnt.getPlayer()) == 0) {
                    input.add("d " + parsedAnt.getTile() + " "
                        + playerMapping.indexOf(parsedAnt.getPlayer()));
                    break;
                }
            }
        }
    }
    
    private void moveAnts(int turn) {
        for (ParsedAnt parsedAnt : parsedAnts) {
            if (parsedAnt.getStartTurn() > turn) {
                // food hasn't been placed on the map yet
                break;
            }
            if (parsedAnt.getConversionTurn() >= turn || parsedAnt.getEndTurn() <= turn) {
                // food hasn't been converted yet or ant died already
                continue;
            }
            String moves = parsedAnt.getMoves();
            if (moves.isEmpty()) {
                // no more moves
                continue;
            }
            char symbol = moves.charAt(0);
            if (symbol != '-') {
                // ant did move
                Aim direction = Aim.fromSymbol(symbol);
                Tile tile = ants.getTile(parsedAnt.getTile(), direction);
                parsedAnt.setTile(tile);
            }
            parsedAnt.setMoves(moves.substring(1));
        }
    }
}
