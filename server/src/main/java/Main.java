import chess.*;
import dataaccess.DataAccessException;

import static dataaccess.DatabaseManager.createDatabase;

public class Main {
    public static void main(String[] args) throws DataAccessException {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
        createDatabase();
        System.out.println("DB created");
    }
}