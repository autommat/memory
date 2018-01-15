package project.server;

public class Player {
    private int portNumber;
    private String name;
    private boolean fail = false;
    private boolean ready = false;
    private int score = 0;

    public int getPortNumber() {
        return portNumber;
    }

    public Player(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void fail() {
        fail = true;
    }

    public boolean isFail() {
        return fail;
    }

    public boolean isReady() {
        if (fail) {
            return true;
        }
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setScore(int score) {
        if (!fail) {
            this.score = score;
        }
    }

    public int getScore() {
        return score;
    }
}
