/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package handler.geopen;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * 此类属于一种工具,用来改变JTable的颜色
 *
 * @author caohuaqiang
 * @version 1.10
 * @since JDK1.5
 */
public class SetTableRowColor {

    /**
     * 此方法是一静态方法是将接收到的JTAble按照奇偶行分别设置成表色和银蓝色
     *
     * @param table JTable
     */
    public static void makeFace(JTable table) {

        try {
            DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable table,
                        Object value, boolean isSelected, boolean hasFocus,
                        int row, int column) {
                    if (row % 2 == 0) {
                        setBackground(Color.white); // 设置奇数行底色
                    } else if (row % 2 == 1) {
//                        setBackground(Color.LIGHT_GRAY); // 设置偶数行底色
                        setBackground(new Color(230, 240, 240)); // 设置偶数行底色
//                        setBackground(new Color(245, 245, 245)); // 设置偶数行底色
                    }
                    return super.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);
                }
            };
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumn(table.getColumnName(i)).setCellRenderer(tcr);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
