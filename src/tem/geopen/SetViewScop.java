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
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 *
 * @author Administrator
 */
public class SetViewScop {

    /**
     * 设置滚动窗口的大小
     *
     * @param scroll
     * @param panel
     * @param width
     * @param height
     */
    public static void setViewport(JScrollPane scroll, JPanel panel, int width, int height) {
        // 新建一个JViewport
        JViewport viewport = new JViewport();
        // 设置viewport的大小
        panel.setPreferredSize(new Dimension(width + 50, height + 50));
        // 设置viewport
        viewport.setView(panel);
        scroll.setViewport(viewport);
    }

    public static void setLocation(JDialog frame) {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension d = tool.getScreenSize();
        frame.setLocation((int) (d.getWidth() - frame.getWidth()) / 2, (int) (d.getHeight() - frame.getHeight()) / 2);
    }

    public static void setLocation(JFrame frame) {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension d = tool.getScreenSize();
        frame.setLocation((int) (d.getWidth() - frame.getWidth()) / 2, (int) (d.getHeight() - frame.getHeight()) / 2);
    }
}
