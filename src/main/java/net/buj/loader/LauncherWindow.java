package net.buj.loader;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class LauncherWindow extends Frame implements WindowListener {
    private final Runnable disposeFunc;

    public LauncherWindow(Runnable disposeFunc) {
        super();

        this.disposeFunc = disposeFunc;

        addWindowListener(this);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            disposeFunc.run();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        //System.exit(0);
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
