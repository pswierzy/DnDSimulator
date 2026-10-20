package ai.movement;

import engine.state.Combatant;
import engine.state.GameState;
import engine.state.MovementTrait;
import engine.state.Vector2d;

import java.util.*;

public class AStarPathfinder implements IPathfinder {

    private static class Node implements Comparable<Node> {
        final Vector2d position;
        final Node parent;
        final int gCost;
        final int hCost;

        public Node(Vector2d position, Node parent, int gCost, int hCost) {
            this.position = position;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
        }

        public int getFCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(Node o) {
            int fCompare = Integer.compare(this.getFCost(), o.getFCost());
            if (fCompare == 0) {
                return Integer.compare(this.gCost, o.gCost);
            }
            return fCompare;
        }
    }

    @Override
    public List<Vector2d> findPath(Combatant entity, Vector2d destination, GameState state) {
        Vector2d startPos = entity.getPosition();

        if (startPos.equals(destination)) {
            return new ArrayList<>();
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Vector2d> closedSet = new HashSet<>();
        Map<Vector2d, Node> allNodes = new HashMap<>();

        Node startNode = new Node(startPos, null, 0, startPos.distance(destination) * 10);
        openSet.add(startNode);
        allNodes.put(startPos, startNode);

        int[][] directions = {
                {0, 1}, {1, 0}, {0, -1}, {-1, 0},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        while (!openSet.isEmpty()) {
            Node curr = openSet.poll();
            closedSet.add(curr.position);

            if (curr.position.equals(destination)) {
                return retracePath(startNode, curr);
            }

            for (int[] dir : directions) {
                Vector2d neighborPos = new Vector2d(curr.position.getX() + dir[0], curr.position.getY() + dir[1]);

                if (closedSet.contains(neighborPos)) continue;

                if (!isWalkable(entity, neighborPos, state)) continue;

                int moveCost = (Math.abs(dir[0]) + Math.abs(dir[1]) == 2 ? 15 : 10);
                int gCost = curr.gCost + moveCost;

                Node neighborNode = allNodes.get(neighborPos);

                if (neighborNode == null || gCost < neighborNode.gCost) {
                    int hCost = neighborPos.distance(destination) * 10;
                    Node nextNode = new Node(neighborPos, curr, gCost, hCost);

                    if (neighborNode != null) {
                        openSet.remove(neighborNode);
                    }

                    openSet.add(nextNode);
                    allNodes.put(neighborPos, nextNode);
                }
            }
        }
        return new ArrayList<>();
    }

    private List<Vector2d> retracePath(Node startNode, Node endNode) {
        List<Vector2d> path = new ArrayList<>();
        Node current = endNode;

        while (current != startNode) {
            path.add(current.position);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public boolean isWalkable(Combatant entity, Vector2d place, GameState state) {
        for (int dx = 0; dx < entity.getSize(); dx++) {
            for (int dy = 0; dy < entity.getSize(); dy++) {
                Vector2d checkPos = new Vector2d(place.getX() + dx, place.getY() + dy);

                if (!state.getBattleMap().isWithinBounds(checkPos)) return false;
                if (!entity.hasTrait(MovementTrait.INCORPOREAL)) {
                    if (state.getBattleMap().isBlocked(checkPos)) return false;
                }
                if (!entity.hasTrait(MovementTrait.FLYING)) {
                    if (state.getBattleMap().isChasm(checkPos)) return false;
                }

                for (Combatant other : state.getAllCombatants()) {
                    if (other == entity || other.getHp() <= 0) continue;
                    if (other.getOccupiedTiles().contains(checkPos)) return false;
                }
            }
        }
        return true;
    }
}
