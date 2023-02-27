package net.buj.loader;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class LauncherWindow extends Frame implements WindowListener {
    public interface DisposeFn {
        void run(LauncherWindow win);
    }
    private DisposeFn disposeFunc;

    public LauncherWindow(DisposeFn disposeFunc) {
        super();

        this.disposeFunc = disposeFunc;

        addWindowListener(this);
    }

    public void setOnDispose(DisposeFn newFunc) {
        disposeFunc = newFunc;
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            disposeFunc.run(this);
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
