package project.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private final List<Player> playerList = Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, String> sequences = new ConcurrentHashMap<>();
    private Random rand = new Random();
    private long timeoutSeconds;

    public Room(long timeoutSeconds, int maxPlayersInRoom) {
        this.maxPlayersInRoom = maxPlayersInRoom;
        this.timeoutSeconds = timeoutSeconds;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getMaxPlayersInRoom() {
        return maxPlayersInRoom;
    }

    private int maxPlayersInRoom;

    public void addPlayer(int port) {
        playerList.add(new Player(port));
    }

    public int howManyPlayersNow() {
        return playerList.size();
    }

    public boolean isFull() {
        return howManyPlayersNow() == maxPlayersInRoom;
    }

    public String getSeq(int round) {
        synchronized (sequences) {
            String seq = sequences.get(round);
            if (seq == null) {
                seq = newSeq(round);
                sequences.put(round, seq);
            }
            return seq;
        }
    }

    private String newSeq(int howMany) {
        StringBuilder sb = new StringBuilder();
        sb.append(rand.nextInt(25));
        for (int i = 1; i < howMany; i++) {
            sb.append(";").append(rand.nextInt(25));
        }
        return sb.toString();
    }

    public boolean verifySeq(int round, String seq) {
        return sequences.get(round).equals(seq);
    }

    public boolean areNamesChosen() {
        synchronized (playerList) {
            boolean result = true;
            for (Player p : playerList) {
                result &= (p.getName() != null);
            }
            return result;
        }
    }

    public void addName(int port, String name) {
        for (Player p : playerList) {
            if (p.getPortNumber() == port) {
                p.setName(name);
                return;
            }
        }
    }

    public void fail(int port) {
        for (Player p : playerList) {
            if (p.getPortNumber() == port) {
                System.out.println("FAIL:" + p.getName());
                p.fail();
                return;
            }
        }
    }

    public boolean isFail(int port) {
        for (Player p : playerList) {
            if (p.getPortNumber() == port) {
                return p.isFail();
            }
        }
        return true;
    }

    public void setReady(int port, boolean ready) {
        for (Player p : playerList) {
            if (p.getPortNumber() == port) {
                p.setReady(ready);
            }
        }
    }

    public boolean isEveryoneReady() {
        for (Player p : playerList) {
            if (!p.isReady()) return false;
        }
        return true;
    }

    public void setScore(int port, int i) {
        for (Player p : playerList) {
            if (p.getPortNumber() == port) {
                p.setScore(i);
            }
        }
    }

    public Map<String, Integer> getScores() {
        Map<String, Integer> toReturn = new HashMap<>();
        for (Player p : playerList) {
            toReturn.put(p.getName(), p.getScore());
        }
        return toReturn;
    }

    public boolean isFinished() {
        System.out.println("isFin");
        for (Player p : playerList) {
            System.out.println(p.getName() + p.isFail());
            if (!p.isFail()) {
                return false;
            }
        }
        return true;
    }
}
