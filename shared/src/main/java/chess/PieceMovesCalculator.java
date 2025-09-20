package chess;

import java.util.Collection;
import java.util.HashSet;

public class PieceMovesCalculator {

    /**
     * Calculates all possible moves for a given piece at a given position.
     *
     * @param board The current chess board.
     * @param position The position of the piece to calculate moves for.
     * @return A collection of all valid ChessMove objects.
     */
    public static Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            return new HashSet<>(); // Return empty set if no piece at position
        }

        return switch (piece.getPieceType()) {
            case BISHOP -> calculateBishopMoves(board, position);
            case ROOK -> calculateRookMoves(board, position);
            case QUEEN -> calculateQueenMoves(board, position);
            case KING -> calculateKingMoves(board, position);
            case KNIGHT -> calculateKnightMoves(board, position);
            case PAWN -> calculatePawnMoves(board, position);
        };
    }

    // --- Private Helper Methods for Each Piece Type ---

    private static Collection<ChessMove> calculateBishopMoves(ChessBoard board, ChessPosition position) {
        // Directions: diagonals
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        return calculateSlidingMoves(board, position, directions);
    }

    private static Collection<ChessMove> calculateRookMoves(ChessBoard board, ChessPosition position) {
        // Directions: horizontal and vertical
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        return calculateSlidingMoves(board, position, directions);
    }

    private static Collection<ChessMove> calculateQueenMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        // Queen moves = Bishop moves + Rook moves
        moves.addAll(calculateBishopMoves(board, position));
        moves.addAll(calculateRookMoves(board, position));
        return moves;
    }

    private static Collection<ChessMove> calculateKingMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece piece = board.getPiece(position);
        // All 8 directions, but only one step
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] dir : directions) {
            int endRow = position.getRow() + dir[0];
            int endCol = position.getColumn() + dir[1];

            // Check if the move is within the board
            if (endRow >= 1 && endRow <= 8 && endCol >= 1 && endCol <= 8) {
                ChessPosition endPosition = new ChessPosition(endRow, endCol);
                ChessPiece occupyingPiece = board.getPiece(endPosition);
                // Can move if the square is empty or has an opponent's piece
                if (occupyingPiece == null || occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(position, endPosition, null));
                }
            }
        }
        return moves;
    }

    private static Collection<ChessMove> calculateKnightMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece piece = board.getPiece(position);
        // 8 possible L-shaped moves
        int[][] offsets = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};

        for (int[] offset : offsets) {
            int endRow = position.getRow() + offset[0];
            int endCol = position.getColumn() + offset[1];

            if (endRow >= 1 && endRow <= 8 && endCol >= 1 && endCol <= 8) {
                ChessPosition endPosition = new ChessPosition(endRow, endCol);
                ChessPiece occupyingPiece = board.getPiece(endPosition);
                if (occupyingPiece == null || occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(position, endPosition, null));
                }
            }
        }
        return moves;
    }

    private static Collection<ChessMove> calculatePawnMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece piece = board.getPiece(position);
        int startRow = position.getRow();
        int startCol = position.getColumn();

        // Determine direction of movement and starting row based on color
        int direction = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int initialRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // 1. Single Step Forward
        ChessPosition oneStep = new ChessPosition(startRow + direction, startCol);
        if (board.getPiece(oneStep) == null) {
            if (oneStep.getRow() == promotionRow) {
                addPromotionMoves(moves, position, oneStep);
            } else {
                moves.add(new ChessMove(position, oneStep, null));
            }

            // 2. Double Step Forward (only from starting row)
            if (startRow == initialRow) {
                ChessPosition twoSteps = new ChessPosition(startRow + 2 * direction, startCol);
                if (board.getPiece(twoSteps) == null) {
                    moves.add(new ChessMove(position, twoSteps, null));
                }
            }
        }

        // 3. Captures (diagonal)
        int[] captureCols = {startCol - 1, startCol + 1};
        for (int captureCol : captureCols) {
            if (captureCol >= 1 && captureCol <= 8) {
                ChessPosition capturePos = new ChessPosition(startRow + direction, captureCol);
                ChessPiece target = board.getPiece(capturePos);
                if (target != null && target.getTeamColor() != piece.getTeamColor()) {
                    if (capturePos.getRow() == promotionRow) {
                        addPromotionMoves(moves, position, capturePos);
                    } else {
                        moves.add(new ChessMove(position, capturePos, null));
                    }
                }
            }
        }
        System.out.println(moves);
        return moves;

    }

    // --- Generic Helper Methods ---

    /**
     * A generic method for calculating moves for sliding pieces (Rook, Bishop, Queen).
     * It iterates in given directions until it hits another piece or the edge of the board.
     */
    private static Collection<ChessMove> calculateSlidingMoves(ChessBoard board, ChessPosition position, int[][] directions) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece piece = board.getPiece(position);

        for (int[] dir : directions) {
            int row = position.getRow();
            int col = position.getColumn();

            while (true) {
                row += dir[0];
                col += dir[1];

                if (row < 1 || row > 8 || col < 1 || col > 8) break; // Off board

                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece occupying = board.getPiece(newPos);

                if (occupying == null) {
                    moves.add(new ChessMove(position, newPos, null)); // Empty square
                } else {
                    if (occupying.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, newPos, null)); // Capture
                    }
                    break; // Stop, blocked by a piece
                }
            }
        }
        return moves;
    }

    /**
     * Helper to add all four promotion moves for a pawn.
     */
    private static void addPromotionMoves(Collection<ChessMove> moves, ChessPosition start, ChessPosition end) {
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
    }
}