/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 增加了多次采集数据处理 自动将文件分割成多个文件
 */
package handler.geopen;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JOptionPane;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import tem.geopen.TEMAcquisitionProMain;

/**
 *
 * @author Administrator
 */
public class TEMData {

    private TEMAcquisitionProMain frame;
    private double dividend = -1;//被除数 根据基频大小 25-16 12.5-8....
    private int fund50 = 300;
    private int fund60 = 250;

    public TEMData(TEMAcquisitionProMain frame) {
        this.frame = frame;
    }

    /**
     * 读取CS_CR_AMT数据 含有多次采集
     *
     * @param file
     */
    public void readTEM(File file) {
//        String path = makeFileDir(file);//建立文件夹
        RandomAccessFile raf = null;
        int channel = 0;
        try {
            raf = new RandomAccessFile(file, "rw");
            //整体读取数据
            int fileLength = (int) file.length();
            byte[] buffer = new byte[fileLength];
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            dis.read(buffer);
            dis.close();
            Parameters.repeat = 1;//设置采集次数
//            for (int k = 0; k < Parameters.repeat; k++) {
//                if (k * 2048 > raf.length()) {
//                    break;
//                }
//                raf.seek(0);
//                raf.seek(k * 2048);
//                channel = extractHeadInfor(raf, k * 2048);//抽取数据头数据
//                int unitChannelPoints = 0;
//                if (Parameters.restrainFrequency == 0) {
//                    unitChannelPoints = Parameters.fund * fund50;
//                } else {
//                    unitChannelPoints = Parameters.fund * fund60;
//                }
//                double rate = 1;
//                if (dividend == 3.125 || dividend == 3.75) {//3.125 3.75主频
//                    rate = 15000;
//                } else if (dividend == 25 || dividend == 12.5 || dividend == 6.25) {
//                    rate = 30000;
//                } else if (dividend == 250 || dividend == 50 || dividend == 100) {
//                    if (dividend == 250) {//区别低速25Hz
//                        dividend = 25;
//                    }
//                    rate = 500000;//500K
//                }
////                String station = file.getName().substring(0, 5);
////                String title1 = "时间：" + TEMAcquisitionProMain.dateFormat.getText() + "  " + TEMAcquisitionProMain.timeFormat.getText();
////                String path2 = path;
////                String path2 = path + "\\" + station + TEMAcquisitionProMain.dateFormat.getText().replace(".", "") ;
////                        + TEMAcquisitionProMain.timeFormat.getText().replace(":", "") + ".TM";
////                File file1 = new File(path2);
////                FileOutputStream fos = new FileOutputStream(file1);
////                DataOutputStream dos = new DataOutputStream(fos);
////                if (k - 1 < 0) {
////                    dos.write(Arrays.copyOfRange(buffer, 0, 2048));
////                } else {
////                    dos.write(Arrays.copyOfRange(buffer, (k - 1) * 2048, k * 2048));
////                }
////                dos.close();
//            }
            //显示文件
            for (int k = 0; k < Parameters.repeat; k++) {
                if (k * 2048 > raf.length()) {
                    break;
                }
                raf.seek(0);
                raf.seek(k * 2048);
                channel = extractHeadInfor(raf, k * 2048);//抽取数据头数据
                XYSeries[] xyseries = new XYSeries[channel];
                int unitChannelPoints = 0;
                if (Parameters.restrainFrequency == 0) {
                    unitChannelPoints = Parameters.fund * fund50;
                } else {
                    unitChannelPoints = Parameters.fund * fund60;
                }
                double rate = 1;
                if (dividend == 3.125 || dividend == 3.75) {//3.125 3.75主频
                    rate = 15000;
                } else if (dividend == 25 || dividend == 12.5 || dividend == 6.25) {
                    rate = 30000;
                } else if (dividend == 250 || dividend == 50 || dividend == 100) {
                    if (dividend == 250) {//区别低速25Hz
                        dividend = 25;
                        unitChannelPoints = 5000;
                    } else if (dividend == 50) {
                        unitChannelPoints = 2500;
                    } else if (dividend == 100) {
                        unitChannelPoints = 1250;
                    }
                    rate = 500000;//500K
                }
                TEMAcquisitionProMain.fundamentalSpinner.setValue(dividend);
                String title1 = "时间：" + TEMAcquisitionProMain.dateFormat.getText() + "  " + TEMAcquisitionProMain.timeFormat.getText();
                String title2 = "经度：" + TEMAcquisitionProMain.latitudeFormat.getText();
                String title3 = "纬度：" + TEMAcquisitionProMain.longitudeFormat.getText();
                for (int i = 0; i < channel; i++) {
                    xyseries[i] = new XYSeries("时间/电压");
                }
                System.out.println(unitChannelPoints + "总点数" + channel);
                for (int m = 0; m < unitChannelPoints; m++) {
                    for (int j = 0; j < channel; j++) {
                        int value = raf.readInt();
                        xyseries[j].add(m * 1000.0 / rate, value);
//                    frame.xyseries[j].add(m * 1000.0 / rate, Math.abs(raf.readInt()));
                    }
                }
                String channelName = "";
                //添加图表
                for (int i = 0; i < channel; i++) {
                    channelName = "第" + (k + 1) + "次采集 ，第" + (i + 1) + "道数据,  " + title1 + " ,  " + title2 + " , " + title3 + ",  通道定义：";
                    frame.showingPanel.add(frame.temc.createDemoPanel(channelName + Parameters.channelsEH[0], "Time (ms)", "Voltage (v) ", "TEM", new XYSeriesCollection(xyseries[i])));
                }
            }
            raf.close();
            frame.showingPanel.updateUI();
            //设定可见
            frame.paraOutPanel.setVisible(true);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "文件读取错误，请检查源文件正确性！");
        }
    }

    /**
     * 读取CS_CR_AMT数据
     *
     * @param file
     */
    public void readTEM1(File file) {

//        try {
//            findTEMHead(file);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(TEMData.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(TEMData.class.getName()).log(Level.SEVERE, null, ex);
//        }
        String path = makeFileDir(file);//建立文件夹
        RandomAccessFile raf = null;
        int channel = 0;
        try {
            raf = new RandomAccessFile(file, "rw");
            //分割文件
            int fileLength = (int) file.length();
            byte[] buffer = new byte[fileLength];
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            dis.read(buffer);
            dis.close();
            for (int k = 0; k < Parameters.repeat; k++) {
                if (k * 2048 > raf.length()) {
                    break;
                }
                raf.seek(0);
                raf.seek(k * 2048);
                channel = extractHeadInfor(raf, k * 2048);//抽取数据头数据
                int unitChannelPoints = 0;
                if (Parameters.restrainFrequency == 0) {
                    unitChannelPoints = Parameters.fund * fund50;
                } else {
                    unitChannelPoints = Parameters.fund * fund60;
                }
                double rate = 1;
                if (dividend == 3.125 || dividend == 3.75) {//3.125 3.75主频
                    rate = 15000;
                } else {
                    rate = 30000;
                }
                String station = file.getName().substring(0, 5);
//                String title1 = "时间：" + TEMAcquisitionProMain.dateFormat.getText() + "  " + TEMAcquisitionProMain.timeFormat.getText();
                String path2 = path + "\\" + station + TEMAcquisitionProMain.dateFormat.getText().replace(".", "") + TEMAcquisitionProMain.timeFormat.getText().replace(":", "") + ".TM";
                File file1 = new File(path2);
                FileOutputStream fos = new FileOutputStream(file1);
                DataOutputStream dos = new DataOutputStream(fos);
                if (k - 1 < 0) {
                    dos.write(Arrays.copyOfRange(buffer, 0, 2048));
                } else {
                    dos.write(Arrays.copyOfRange(buffer, (k - 1) * 2048, k * 2048));
                }
                dos.close();
            }
            //显示文件
            for (int k = 0; k < Parameters.repeat; k++) {
                if (k * 2048 > raf.length()) {
                    break;
                }
                raf.seek(0);
                raf.seek(k * 2048);
                channel = extractHeadInfor(raf, k * 2048);//抽取数据头数据
//                System.out.println(raf.getFilePointer());
//                System.out.println(raf.getFilePointer() + "***");
//                frame.xyseries = new XYSeries[Parameters.repeat][channel];
                XYSeries[] xyseries = new XYSeries[channel];
                int unitChannelPoints = 0;
                if (Parameters.restrainFrequency == 0) {
                    unitChannelPoints = Parameters.fund * fund50;
                } else {
                    unitChannelPoints = Parameters.fund * fund60;
                }
                double rate = 1;
                if (dividend == 3.125 || dividend == 3.75) {//3.125 3.75主频
                    rate = 15000;
                } else {
                    rate = 30000;
                }
                String title1 = "时间：" + TEMAcquisitionProMain.dateFormat.getText() + "  " + TEMAcquisitionProMain.timeFormat.getText();
                String title2 = "经度：" + TEMAcquisitionProMain.latitudeFormat.getText();
                String title3 = "纬度：" + TEMAcquisitionProMain.longitudeFormat.getText();
                for (int i = 0; i < channel; i++) {
                    xyseries[i] = new XYSeries("时间/电压");
                }
                for (int m = 0; m < unitChannelPoints; m++) {
                    for (int j = 0; j < channel; j++) {
                        int value = raf.readInt();
                        xyseries[j].add(m * 1000.0 / rate, value);
//                    frame.xyseries[j].add(m * 1000.0 / rate, Math.abs(raf.readInt()));
                    }
                }
                String channelName = "";
                //添加图表
                for (int i = 0; i < channel; i++) {
                    channelName = "第" + (k + 1) + "次采集 ，第" + (i + 1) + "道数据,  " + title1 + " ,  " + title2 + " , " + title3 + ",  通道定义：";
                    frame.showingPanel.add(frame.temc.createDemoPanel(channelName + Parameters.channelsEH[0], "Time (ms)", "Voltage (v) ", "TEM", new XYSeriesCollection(xyseries[i])));
                }
            }
            raf.close();
            frame.showingPanel.updateUI();
            //设定可见
            frame.paraOutPanel.setVisible(true);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "文件读取错误，请检查源文件正确性！");
        }
    }

    public String makeFileDir(File file) {
        File dir = new File(file.getName().split("[.]")[0]);
        dir.mkdir();
        String path = dir.getAbsolutePath().concat("\\").concat(file.getName());
        return path;
    }

    public void findTEMHead(File file) throws FileNotFoundException, IOException {
        int fileLength = (int) file.length();
        byte[] buffer = new byte[fileLength];
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        dis.read(buffer);
        ArrayList<Integer> list = new ArrayList<Integer>();
        //45 4d EM
        int i1 = 0;
        int i2 = 0;
        byte v1 = 69;
        byte v2 = 77;
        System.out.println(new String(new byte[]{69}));
        System.out.println(new String(new byte[]{77}));
        for (int i = 0; i < buffer.length; i += 16) {
            i1 = java.util.Arrays.binarySearch(buffer, i + 1, i + 2, (byte) 69);
            i2 = java.util.Arrays.binarySearch(buffer, i + 2, i + 3, (byte) 77);
        }
    }

    public byte[] writeHeadInfor() throws IOException {
        byte[] headInfor = new byte[512];
        int rec = 0;
        //装置形式
        byte[] arrays = new String("TEM").getBytes();
        for (int i = 0; i < 8; i++) {
            if (i < arrays.length) {
                headInfor[rec] = arrays[i];
            } else {
                headInfor[rec] = 0x00;
            }
            rec++;
        }
        //工作地点
        byte[] workPos = frame.tem_posTextField.getText().getBytes();
        for (int i = 0; i < 128; i++) {
            if (i < workPos.length) {
                headInfor[rec] = workPos[i];
            } else {
                headInfor[rec] = 0x00;
            }
            rec++;
        }
        //GPS
        for (int i = 0; i < 32; i++) {
            headInfor[rec] = 0x00;
            
            rec++;
        }
        //定位ok no
        byte[] gpsState = new String("OK").getBytes();
        for (int i = 0; i < gpsState.length; i++) {
            headInfor[rec] = gpsState[i];
            rec++;
        }
        //文件名 
        byte[] fileName = new String("0362F20150925160").getBytes();
        for (int i = 0; i < fileName.length; i++) {
            headInfor[rec] = fileName[i];
            rec++;
        }
        //记录时间
//        byte[] gpsTime = new byte[19];
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String date = df.format(new Date()).replaceAll("[-]", ".");
        byte[] gpsTime = date.getBytes();
        for (int i = 0; i < gpsTime.length; i++) {
            headInfor[rec] = gpsTime[i];
            rec++;
        }
        //增益

        byte[] gain = shortToByteArray((short) Integer.parseInt(frame.tem_gainComboBox.getSelectedItem().toString()));//增益
//        byte[] gain = new byte[]{0x00, 0x01};//增益
        for (int i = 0; i < gain.length; i++) {
            headInfor[rec] = gain[i];
            rec++;
        }
        //道数
//        byte[] channels = new byte[1];//道数
        byte[] channels = new byte[1];//道数
        for (int i = 0; i < channels.length; i++) {
            headInfor[rec] = (byte) Integer.parseInt(frame.tem_channelSpinner.getValue().toString());
            rec++;
        }
        //通道定义 new byte[32];
        byte[] channelEH = new String("1=EX1/2=EX1/3=EX1").getBytes();//通道定义
        for (int i = 0; i < 32; i++) {
            if (i < channelEH.length) {
                headInfor[rec] = channelEH[i];
            } else {
                headInfor[rec] = 0x00;
            }
            rec++;
        }
        //基频号
        byte[] fund = new byte[1];//基频号
        for (int i = 0; i < fund.length; i++) {
            headInfor[rec] = (byte) (frame.tem_FreComboBox.getSelectedIndex() + 1);
            rec++;
        }
        //叠加次数
//        byte[] superp = new byte[]{0x00, 0x01};//叠加次数
        byte[] superp = shortToByteArray((short) Integer.parseInt(frame.tem_periodSpinner.getValue().toString()));//增益;//叠加次数
        for (int i = 0; i < superp.length; i++) {
            headInfor[rec] = superp[i];
            rec++;
        }
        //获得站点编号 工作量
        byte[] stationNumber = new byte[16];//获得站点编号
        for (int i = 0; i < stationNumber.length; i++) {
            headInfor[rec] = stationNumber[i];
            rec++;
        }
        //按键标示1 电阻6 
        byte[] otherInfor = new byte[7];//按键标示1 电阻6 
        for (int i = 0; i < otherInfor.length; i++) {
            headInfor[rec] = otherInfor[i];
            rec++;
        }
        //抑制频率
        byte[] restrainFrequency = new byte[1];//抑制频率
        for (int i = 0; i < restrainFrequency.length; i++) {
            headInfor[rec] = restrainFrequency[i];
            rec++;
        }
        //补充其他空位
        for (int i = rec; i < headInfor.length; i++) {
            headInfor[rec] = 0x00;
            rec++;
        }
        return headInfor;
    }

    /**
     * 将16位的short转换成byte数组
     *
     * @param s short
     * @return byte[] 长度为2
     *
     */
    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    public int extractHeadInfor(RandomAccessFile raf, int offset) throws IOException {
        raf.skipBytes(8);//跳出装置形式
        byte[] pos = new byte[128];//工作地点
        raf.read(pos);
        int recPos = 0;
        for (int i = 0; i < pos.length; i++) {
            byte b = pos[i];
            if (b != 0) {
                recPos = i;
            }
        }
        byte[] posSub = new byte[recPos + 1];//删去空格 只存地点
        for (int i = 0; i <= recPos; i++) {
            posSub[i] = pos[i];
        }
        String workPos = new String(posSub, "GBK");
        TEMAcquisitionProMain.daPosPath.setText(workPos);
        byte[] gpsInf = new byte[32];//经纬度
        raf.read(gpsInf);//读取gps 义
        try {
            String gpsInfStr = new String(gpsInf);
            String[] splitStrs = gpsInfStr.split(",");
            //截取纬度
            String[] latitudeSplit = splitStrs[1].split("[.]");
            String pointLat = latitudeSplit[0].substring(latitudeSplit[0].length() - 2, latitudeSplit[0].length());
            String degreeLat = latitudeSplit[0].substring(1, latitudeSplit[0].length() - 2);
            String concat = pointLat + "." + latitudeSplit[1];
            double pointLatV = Double.parseDouble(concat);
            TEMAcquisitionProMain.latitudeFormat.setText(degreeLat + "°" + pointLatV + "' " + splitStrs[1].substring(0, 1));
//        double pointLatV = Double.parseDouble(concat);
            double pointLatVV = Double.parseDouble(concat) / 60.0;
            double degreeLatVV = Double.parseDouble(degreeLat) + pointLatVV;
            //经度
            String[] longitudeSplit = splitStrs[0].split("[.]");
            String pointLong = longitudeSplit[0].substring(longitudeSplit[0].length() - 2, longitudeSplit[0].length());
            String degreeLong = longitudeSplit[0].substring(1, longitudeSplit[0].length() - 2);
            String concatLong = pointLong + "." + longitudeSplit[1];
            double pointLongV = Double.parseDouble(concatLong);
            TEMAcquisitionProMain.longitudeFormat.setText(degreeLong + "°" + pointLongV + "' " + splitStrs[0].substring(0, 1));
            double pointLonVV = Double.parseDouble(concatLong) / 60.0;
            double degreeLonVV = Double.parseDouble(degreeLong) + pointLonVV;
//        ArrayList<Double> xyPos = new ArrayList<Double>();
//        xyPos.add(degreeLonVV);
//        xyPos.add(degreeLatVV);
        } catch (Exception e) {
//            JOptionPane.showMessageDialog(frame, "GPS信息有误！");
            TEMAcquisitionProMain.latitudeFormat.setText("无定位");
            TEMAcquisitionProMain.longitudeFormat.setText("无定位");
//            TEMAcquisitionProMain.locationState.setText("NO");
        }
        byte[] gpsState = new byte[2];//定位ok no
        raf.read(gpsState);//定位状态
        String gpsStateStr = new String(gpsState);
        TEMAcquisitionProMain.locationState.setText(gpsStateStr);
        byte[] fileName = new byte[16];//文件名
        raf.read(fileName);
        byte[] gpsTime = new byte[19];//记录时间
        raf.read(gpsTime);
        String gpsTimeStr = new String(gpsTime);
        String[] gpsTimeStrs = gpsTimeStr.split("[ ]");
        String[] gpsTimeDateStrs = gpsTimeStrs[0].split(".");
//        GPSDialog.dateFormat.setText(gpsTimeDateStrs[0]+"年"+gpsTimeDateStrs[1]+"月"+gpsTimeDateStrs[2]+"日");
//        TEMAcquisitionProMain.dateFormat.setText(gpsTimeStrs[0]);
//        TEMAcquisitionProMain.timeFormat.setText(gpsTimeStrs[1]);
        System.out.println(gpsTimeStrs[0] + "时间");
        System.out.println(gpsTimeStrs[1] + "时间");
        Parameters.gain = raf.readShort();//读取增益
        TEMAcquisitionProMain.gSpinner.setValue(Parameters.gain);
        Parameters.channels = raf.read();//读取道数
        TEMAcquisitionProMain.channelSpinner.setValue(Parameters.channels);
        System.out.println(Parameters.channels + "道数");
        byte[] channelEH = new byte[32];//通道定义
        raf.read(channelEH);
        String channelEHStr = new String(channelEH);
        String[] unitChannelEH = channelEHStr.trim().split("[/]");
        String[] channel1_EH = unitChannelEH[0].split("=");
        String[] channel2_EH = unitChannelEH[1].split("=");
        String[] channel3_EH = unitChannelEH[2].split("=");
        Parameters.channelsEH = new String[3];
        Parameters.channelsEH[0] = channel1_EH[1].substring(0, 2);
        Parameters.channelsEH[1] = channel2_EH[1].substring(0, 2);
        Parameters.channelsEH[2] = channel3_EH[1].substring(0, 2);
        frame.channel1_ComboBox.setSelectedItem(Parameters.channelsEH[0]);
        frame.channel2_ComboBox.setSelectedItem(Parameters.channelsEH[1]);
        frame.channel3_ComboBox.setSelectedItem(Parameters.channelsEH[2]);
        Parameters.fund = raf.read();//基频号 求采样长度
        System.out.println(Parameters.fund + "基频");
        Parameters.superp = raf.readShort();//获得叠加次数
        TEMAcquisitionProMain.superpositonSpinner.setValue(Parameters.superp);
        raf.readShort();//获得站点编号
        raf.skipBytes(7);//按键标示1 电阻6 
        Parameters.restrainFrequency = raf.read();//抑制频率
        //根据抑制频率设定基频
        switch (Parameters.fund) {
            case 1:
                Parameters.fund = 1;
                if (Parameters.restrainFrequency == 0) {
                    dividend = 25;
                } else {
                    dividend = 30;
                }
                break;
            case 2:
                Parameters.fund = 2;
                if (Parameters.restrainFrequency == 0) {
                    dividend = 12.5;
                } else {
                    dividend = 15;
                }
                break;
            case 3:
                Parameters.fund = 4;
                if (Parameters.restrainFrequency == 0) {
                    dividend = 6.25;
                } else {
                    dividend = 7.5;
                }
                break;
            case 4:
                Parameters.fund = 4;
                if (Parameters.restrainFrequency == 0) {
                    dividend = 3.125;
                } else {
                    dividend = 3.75;
                }
                break;
            case 5:
                Parameters.fund = 5;
                if (Parameters.restrainFrequency == 0) {
                    dividend = 250;
                } else {
                }
                break;
            case 6:
                Parameters.fund = 6;
                if (Parameters.restrainFrequency == 0) {
                    dividend = 50;
                } else {
                }
                break;
            case 7:
                Parameters.fund = 7;
                if (Parameters.restrainFrequency == 0) {
                    dividend = 100;
                } else {
                }
                break;
        }
        raf.skipBytes(offset + 512 - (int) raf.getFilePointer());//跳出文件头
        return Parameters.channels;
    }
}
