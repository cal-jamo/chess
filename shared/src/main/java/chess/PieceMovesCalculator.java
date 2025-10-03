package chess;

import java.util.Collection;
import java.util.HashSet;

public class PieceMovesCalculator {

    public static Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            return new HashSet<>();
        }

        return switch (piece.getPieceType()) {
            case KING -> KingMovesCalculator(board, position);
            case QUEEN -> QueenMovesCalculator(board, position);
            case BISHOP -> BishopMovesCalculator(board, position);
            case KNIGHT -> KnightMovesCalculator(board, position);
            case ROOK -> RookMovesCalculator(board, position);
            case PAWN -> PawnMovesCalculator(board, position);
        };
    }

    private static Collection<ChessMove> PawnMovesCalculator(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves =new HashSet<>();
        ChessPiece piece = board.getPiece(position);

        int direction = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int initialRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

        int startRow = position.getRow();
        int startCol = position.getColumn();

        ChessPosition oneStep = new ChessPosition(startRow + direction, startCol);
        if (isWithinBoard(oneStep)) {
            ChessPiece occupyingPiece = board.getPiece(oneStep);
            if (occupyingPiece == null) {
                if (oneStep.getRow() == promotionRow) {
                    addPromotionalMoves(position, oneStep, moves);
                } else {
                    moves.add(new ChessMove(position, oneStep, null));
                }

                if (startRow == initialRow) {
                    ChessPosition twoStep = new ChessPosition(startRow + 2 * direction, startCol);
                    ChessPiece secondOccupyingPiece = board.getPiece(twoStep);
                    if (secondOccupyingPiece == null) {
                        moves.add(new ChessMove(position, twoStep, null));
                    }
                }
            }
        }

        int[] captureCols = {startCol + 1, startCol -1};
        for (int col : captureCols) {
            ChessPosition capturePos = new ChessPosition(startRow + direction, col);
            if (isWithinBoard(capturePos)) {
                ChessPiece occupyingPiece = board.getPiece(capturePos);
                if (occupyingPiece != null && occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                    if (capturePos.getRow() == promotionRow) {
                        addPromotionalMoves(position, capturePos, moves);
                    } else {
                        moves.add(new ChessMove(position, capturePos, null));
                    }
                }
            }
        }

        return moves;
    }

    private static void addPromotionalMoves(ChessPosition position, ChessPosition endPosition, Collection<ChessMove> moves) {
        moves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.KNIGHT));
        moves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.BISHOP));
    }

    private static Collection<ChessMove> KnightMovesCalculator(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece piece = board.getPiece(position);
        int [][] direction = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] dir : direction) {
            int endRow = position.getRow() + dir[0];
            int endCol = position.getColumn() + dir[1];
            ChessPosition endPosition = new ChessPosition(endRow, endCol);
            if (isWithinBoard(endPosition)) {
                ChessPiece occupyingPiece = board.getPiece(endPosition);
                if (occupyingPiece == null || occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(position, endPosition, null));
                }
            }
        }
        return moves;
    }

    private static Collection<ChessMove> KingMovesCalculator(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece piece = board.getPiece(position);
        int [][] direction = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] dir : direction) {
            int endRow = position.getRow() + dir[0];
            int endCol = position.getColumn() + dir[1];
            ChessPosition endPosition = new ChessPosition(endRow, endCol);
            if (isWithinBoard(endPosition)) {
                ChessPiece occupyingPiece = board.getPiece(endPosition);
                if (occupyingPiece == null || occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(position, endPosition, null));
                }
            }
        }
        return moves;
    }

    private static Collection<ChessMove> QueenMovesCalculator(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        moves.addAll(BishopMovesCalculator(board, position));
        moves.addAll(RookMovesCalculator(board, position));
        return moves;
    }

    private static Collection<ChessMove> RookMovesCalculator(ChessBoard board, ChessPosition position) {
        int[][] direction = {{1,0},{-1,0},{0,1},{0,-1}};
        return calculateSlidingMoves(board, position, direction);
    }

    private static Collection<ChessMove> BishopMovesCalculator(ChessBoard board, ChessPosition position) {
        int[][] direction = {{1,1},{1,-1},{-1,1},{-1,-1}};
        return calculateSlidingMoves(board, position, direction);
    }

    private static Collection<ChessMove> calculateSlidingMoves(ChessBoard board, ChessPosition position, int[][] direction) {
        Collection<ChessMove> moves =new HashSet<>();
        ChessPiece piece = board.getPiece(position);
        for (int[] dir: direction) {
            int row = position.getRow();
            int col = position.getColumn();
            while (true) {
                row += dir[0];
                col += dir[1];
                ChessPosition endPosition = new ChessPosition(row, col);
                if (!isWithinBoard(endPosition)) break;
                ChessPiece occupyingPiece = board.getPiece(endPosition);
                if (occupyingPiece == null) {
                    moves.add(new ChessMove(position, endPosition, null));
                } else {
                    if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, endPosition, null));
                    } break;
                }
            }
        }

        return moves;
    }

    private static boolean isWithinBoard(ChessPosition endPosition) {
        return endPosition.getRow() >= 1 && endPosition.getRow() <= 8 &&
                endPosition.getColumn() >= 1 && endPosition.getColumn() <= 8;
    }
}
