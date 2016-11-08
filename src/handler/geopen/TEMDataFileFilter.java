package handler.geopen;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author uuwy
 */
public class TEMDataFileFilter extends JFileChooser {

    public TEMDataFileFilter(int i) {
        super();
        setAcceptAllFileFilterUsed(false);
        addFilter();
    }

    public TEMDataFileFilter(String currentDirectoryPath) {
        super(currentDirectoryPath);
        setAcceptAllFileFilterUsed(false);
        addFilter();
    }

    public String getSuf() {
        FileFilter fileFilter = this.getFileFilter();
        String desc = fileFilter.getDescription();
        String[] suffarr = desc.split(" ");
        String suf = suffarr[0].equals("所有图形文件") ? "" : ".TM";
        return suf;
    }

    public void addFilter() {
        this.addChoosableFileFilter(new MyFileFilter(new String[]{".TM"}, "TEM文件 (*.TM)"));
//        if (i == 0) {
////            this.addChoosableFileFilter(new MyFileFilter(new String[]{".C"}, "文件 (*.C)"));
////            this.addChoosableFileFilter(new MyFileFilter(new String[]{".AM"}, "AMT文件 (*.AM)"));
////            this.addChoosableFileFilter(new MyFileFilter(new String[]{".TM"}, "TEM文件 (*.TM)"));
////            this.addChoosableFileFilter(new MyFileFilter(new String[]{".CR"}, "SIP/CR文件 (*.CR)"));
////            this.addChoosableFileFilter(new MyFileFilter(new String[]{".IP"}, "TIP文件 (*.IP)"));
//        }
//        else if (i == 1) {
//            this.addChoosableFileFilter(new MyFileFilter(new String[]{".INI"}, "INI配置文件 (*.INI)"));
//        } else if (i == 2) {
//            this.addChoosableFileFilter(new MyFileFilter(new String[]{""}, "所有文件 (*.*)"));
//        }
    }

    class MyFileFilter extends FileFilter {

        String[] suffarr;
        String description;

        public MyFileFilter() {
            super();
        }

        public MyFileFilter(String[] suffarr, String description) {
            super();
            this.suffarr = suffarr;
            this.description = description;
        }

        public boolean accept(File f) {
            for (String s : suffarr) {
                if (f.getName().toUpperCase().endsWith(s)) {
                    return true;
                }
            }
            return f.isDirectory();
        }

        public String getDescription() {
            return this.description;
        }
    }
}
