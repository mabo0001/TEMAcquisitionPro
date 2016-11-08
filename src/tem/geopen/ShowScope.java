/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tem.geopen;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author wy
 */
public class ShowScope {

    public static int width = getWidth();
    public static int height = getHeight();

    public static void setScope(JFrame frame) {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension d = tool.getScreenSize();
        frame.setSize((int) d.getWidth(), (int) d.getHeight() - 30);
    }

    public static void setMidScope(JFrame frame) {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension d = tool.getScreenSize();
        frame.setSize((int) d.getWidth() * 3 / 4, (int) d.getHeight() * 2 / 3);
        setLocation(frame);
    }

    public static void setScope(JDialog frame) {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension d = tool.getScreenSize();
        frame.setSize((int) d.getWidth(), (int) (d.getHeight() - 30));
//        frame.setSize((int) d.getWidth() / 2 + 20, (int) (d.getHeight() - 30));
    }

    public static void setScope(JPanel frame) {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension d = tool.getScreenSize();
        frame.setSize((int) d.getWidth() / 2, (int) (d.getHeight() - 30));
    }

    public static void setLocation(JDialog frame) {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension d = tool.getScreenSize();
        frame.setLocation((int) (d.getWidth() - frame.getWidth()) / 2, (int) (d.getHeight() - frame.getHeight()) / 2);
    }

    public static void setLocation(JFrame frame) {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension d = tool.getScreenSize();
        frame.setLocation((int) d.getWidth() / 2 - frame.getWidth() / 2, (int) d.getHeight() / 2 - frame.getHeight() / 2);
    }

    public static int getWidth() {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension d = tool.getScreenSize();
        return (int) d.getWidth();
//        return (int) d.getWidth() / 2;
    }

    public static int getHeight() {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension d = tool.getScreenSize();
        return (int) (d.getHeight() - 30);
    }
}
