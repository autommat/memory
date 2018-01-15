package project.client;

import javafx.scene.control.Button;

import java.util.List;
import java.util.TimerTask;

public class ColorButton extends TimerTask {
    static int current = 0;
    static boolean on = true;
    String[] seqFromServer;
    private List<Button> buttons;
    private Runnable toRunOnFinish;

    ColorButton(List<Button> buttons, String[] seqFromServer) {
        this.seqFromServer = seqFromServer;
        this.buttons = buttons;
    }

    @Override
    public void run() {
        if (current >= seqFromServer.length) {
            current = 0;
            cancel();
            if (toRunOnFinish != null) {
                toRunOnFinish.run();
            }
        } else {
            if (on) {
                buttons.get(Integer.parseInt(seqFromServer[current])).getStyleClass().add("distin");
                on = !on;
            } else {
                buttons.get(Integer.parseInt(seqFromServer[current++])).getStyleClass().remove("distin");
                on = !on;
            }
        }
    }

    public void onFinish(Runnable toRunOnFinish) {
        this.toRunOnFinish = toRunOnFinish;
    }

}