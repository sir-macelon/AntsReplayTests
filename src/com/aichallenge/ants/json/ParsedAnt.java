package com.aichallenge.ants.json;

import com.aichallenge.ants.Tile;

/**
 * Represents ant movement parsed from replay.
 */
public class ParsedAnt {
    private Tile tile;
    
    private final int startTurn;
    
    private final int conversionTurn;
    
    private final int endTurn;
    
    private final int player;
    
    private String moves;
    
    /**
     * Creates new {@link ParsedAnt} object.
     * 
     * @param row row index
     * @param col column index
     * @param startTurn start turn
     * @param conversionTurn conversion turn
     * @param endTurn end turn
     * @param player player identifier
     * @param moves list of moves
     */
    public ParsedAnt(int row, int col, int startTurn, int conversionTurn, int endTurn, int player,
            String moves) {
        tile = new Tile(row, col);
        this.startTurn = startTurn;
        this.conversionTurn = conversionTurn;
        this.endTurn = endTurn;
        this.player = player;
        this.moves = moves;
    }
    
    /**
     * Returns tile.
     * 
     * @return tile
     */
    public Tile getTile() {
        return tile;
    }

    /**
     * Sets tile.
     * 
     * @param tile tile to be set
     */
    public void setTile(Tile tile) {
        this.tile = tile;
    }

    /**
     * Returns start turn.
     * 
     * @return start turn
     */
    public int getStartTurn() {
        return startTurn;
    }

    /**
     * Returns conversion turn.
     * 
     * @return conversion turn
     */
    public int getConversionTurn() {
        return conversionTurn;
    }

    /**
     * Returns end turn.
     * 
     * @return end turn
     */
    public int getEndTurn() {
        return endTurn;
    }

    /**
     * Returns player.
     * 
     * @return player
     */
    public int getPlayer() {
        return player;
    }

    /**
     * Returns moves.
     * 
     * @return moves
     */
    public String getMoves() {
        return moves;
    }

    /**
     * Sets moves.
     * 
     * @param moves moves to be set
     */
    public void setMoves(String moves) {
        this.moves = moves;
    }
}
