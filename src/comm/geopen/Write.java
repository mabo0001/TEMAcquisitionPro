/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comm.geopen;

import handler.geopen.Parameters;
import handler.geopen.SetTableRowColor;
import java.awt.Component;
import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import tem.geopen.TEMAcquisitionProMain;

/**
 *
 * @author Administrator
 */
public class Write implements Runnable, SerialPortEventListener {

    private TEMAcquisitionProMain frame;
    public static CommPortIdentifier portId;
    public static SerialPort serialPort;
    private static OutputStream outputStream;
    private static InputStream inputStream;//读取数据
    private DataInputStream dis;
    public Thread writeThread;//写端口线程
    private int recWriteCounts = 0;//只写端口一次
    public int recWriteModel = 0;//记录发送参数的模式 55dd-0 55bb-1
    //标记不同返回数据
    private boolean recStart = false;//记录是否开始读取数据返回 满足55dd
    private boolean recTestStation = false;//记录叫站信息返回 满足55BB
    private boolean recGPSMainStation = false;//记录主站返回 满足55CC
    private boolean recSiteData = false;//记录子站处理数据返回 满足5511
    private boolean recMainResis = false;//记录子站处理数据返回 满足55EE
    private int dataLength = 0;//返回数据大小
    public static String selectedWiresName = "";//定义无线控制 选项
    public static int selectedWires = 0;//定义无线控制 选项
    public static int selectedSiteDataIndex = 0;//定义得到单个子站数据
    private int recAddWireless = 0;//只加入一次 主站采集 全部子站采集
    public static int TABLE_WIDTH = 0;//计算的表格宽度
    private String fileName = null;//保存文件用
    private byte[] headArray = new byte[512];//存储数据头
    private int tempData = 0;
    private ArrayList<Integer> temp = new ArrayList<Integer>();//储存纯数据
    private DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
    private XYSeries[] xyseries = null;
    private String suffix = ".TM";
    private int unitChannelPoints = 120;//30*4 

    public Write(TEMAcquisitionProMain frame) {
        this.frame = frame;
        try {
            serialPort = (SerialPort) portId.open("DebugeUSB100", 2000);
        } catch (PortInUseException e) {
            System.out.println(11111);
        }
        try {
            outputStream = serialPort.getOutputStream();
            inputStream = serialPort.getInputStream();
            dis = new DataInputStream(inputStream);//读数据流
        } catch (IOException e) {
            System.out.println(222222);
        }
        try {
            serialPort.addEventListener(this);//给当前串口添加一个监听器
        } catch (TooManyListenersException e) {
            System.out.println(33333);
        }
        serialPort.notifyOnDataAvailable(true);//当有数据时通会通知
        try {
            serialPort.setSerialPortParams(9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) {
        }
        writeThread = new Thread(this);
        writeThread.start();
    }

    /**
     * 写文件
     *
     * @param dos
     */
    public void writeFile(DataOutputStream dos, int recWriteModel) throws UnsupportedEncodingException, IOException {
        if (recWriteModel == 0) {
            //写参数头
            short headMark = 0x55aa;//参数头55aa
            dos.writeShort(headMark);
//            dos.writeShort(0x0001);
//            dos.writeShort(0x0000);
            short paraNumbers = 30;//参数个数 工作地点是32个字节
            dos.writeShort(paraNumbers);
            //采样率
            short rate = 16;
            int frePoint = frame.tem_FreComboBox.getSelectedIndex() + 1;
            if (frePoint == 1) {//25
                rate = 16;
            } else if (frePoint == 2) {//12.5
                rate = 16;
            } else if (frePoint == 3) {//6.25
                rate = 16;
            } else if (frePoint == 4) {//3.125
                rate = 15;
            } else if (frePoint == 5) {//25
                rate = 20;
            } else if (frePoint == 6) {//50
                rate = 20;
            } else if (frePoint == 7) {//100
                rate = 20;
            }
            System.out.println(rate + "采样率");
            dos.writeShort(rate);
            //采样长度
            int length = 1024;
            dos.writeShort(length);
            //通道
            int channel = Integer.parseInt(frame.tem_channelSpinner.getValue().toString());
            System.out.println(channel + "通道");
            dos.writeShort(channel);
            //增益
            int gain = frame.tem_gainComboBox.getSelectedIndex() + 1;
            dos.writeShort(gain);
            //触发方式
            int traggerMode = 0;
            dos.writeShort(traggerMode);
            //工作模式
            int workModel = 7;
            dos.writeShort(workModel);
            //触发时间
            int hourInt = 0;//
            int minuteInt = 0;//
            dos.writeShort(hourInt);
            dos.writeShort(minuteInt);
            //标定频率段
            int calibrationFreRang = 0;
            dos.writeShort(calibrationFreRang);
            //标定频率
            int calibrationFre = 0;
            dos.writeShort(calibrationFre);
            //***MT方式时间间隔
            int mtModeTime = 0;
            dos.writeShort(mtModeTime);
            //***周期
            int period = Integer.parseInt(frame.tem_periodSpinner.getValue().toString());
            dos.writeShort(period);
            //***基波频点
//            int frePoint = frame.tem_FreComboBox.getSelectedIndex() + 1;
            System.out.println(frePoint + "基频");
            dos.writeShort(frePoint);
            //***工作地点
            String pos = frame.tem_posTextField.getText();
            byte[] bPos = pos.getBytes("GBK");
            System.out.println(new String(bPos, "GBK"));
            dos.write(bPos);
            System.out.println(bPos.length + "--工作地点");
            for (int j = 0; j < 32 - bPos.length; j++) {
                dos.writeByte(0);
            }
            //****SD卡模式 1-发送到usb 0-发送到sd卡
            int modelSD = 1;
            dos.writeShort(modelSD);
            dos.close();
//            System.out.println(modelSD);
            //无线采集控制子站编号
//            int subStationNum = frame.tem_WirelessComboBox.getSelectedIndex();
//            for (int j = 0; j < TEMAcquisitionProMain.tem_WirelessComboBox.getItemCount(); j++) {
//                if (subStationNum == 0) {//主站 无子站
//                    int para = 0;
//                    dos.writeShort(para);
//                    System.out.println(subStationNum + "主站");
//                    break;
//                } else if (subStationNum == 1) {//主站+全部子站
//                    int para = 65535;
//                    dos.writeShort(para);
//                    System.out.println(subStationNum + "全部子站");
//                    break;
//                } else if (subStationNum == 2) {//全部子站
//                    int para = 65535;
//                    dos.writeShort(para);
//                    break;
//                } else {//特定子站
//                    String para = frame.tem_WirelessComboBox.getSelectedItem().toString().trim();
//                    dos.writeShort(Short.parseShort(para));
//                    break;
//                }
//            }
        } else if (recWriteModel == 1) {//记录叫站
            int transmitPara = 21947;//55bb
            dos.writeShort(transmitPara);
            System.out.println("55BB");
        } else if (recWriteModel == 2) {//主站GPS
            int transmitPara = 0x55cc;//55cc
            dos.writeShort(transmitPara);
            System.out.println("55cc");
        } else if (recWriteModel == 3) {//获得子站电阻值
            int transmitPara = 0x55FF;//55ff
            dos.writeShort(transmitPara);
            System.out.println("55ff");
        } else if (recWriteModel == 4) {//获得子站处理数据
            int transmitPara = 0x5511;//55ff
            dos.writeShort(transmitPara);
            String para = frame.stationsComboBox.getSelectedItem().toString().trim();//特定子站编号
            dos.writeShort(Short.parseShort(para));
            System.out.println("5511");
        } else if (recWriteModel == 5) {//获得主站外部线路电阻
            int transmitPara = 0x55EE;//55EE
            dos.writeShort(transmitPara);
            System.out.println("55EE");
        }
    }

    @Override
    public void run() {
        if (recWriteCounts == 0) {//只写一次
            try {
                writeFile(new DataOutputStream(outputStream), recWriteModel);
                recWriteCounts = 1;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            case SerialPortEvent.DATA_AVAILABLE:
//                try {
//                    System.out.print(Integer.toHexString(dis.read()) + " ");
//                    System.out.println(dis.available() + "Length");
//                    while (dis.available() > 0) {
//                        System.out.print(Integer.toHexString(dis.read()) + " ");
//                    }
//                } catch (IOException ex) {
//                    Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
//                }
                while (recStart == false) {
                    if (recStart == false && recTestStation == false && recGPSMainStation == false && recSiteData == false && recMainResis == false) {
                        try {
                            String str = Integer.toHexString(dis.read());
                            System.out.println(str + "--1");
                            if (str.equalsIgnoreCase("55")) {
                                str = Integer.toHexString(dis.read());
                                System.out.println(str + "--2");
                                if (str.equalsIgnoreCase("DD")) {//数据体
                                    recStart = true;
                                    dataLength = dis.readInt();//读取数据量大小                            }
                                    System.out.println(dataLength + "-长度");
                                } else if (str.equalsIgnoreCase("BB")) {//子站gps
                                    recTestStation = true;
                                } else if (str.equalsIgnoreCase("CC")) {//主站gps
                                    recGPSMainStation = true;
                                    dataLength = dis.readInt();//读取数据量大小    
                                } else if (str.equalsIgnoreCase("77")) {
                                    JOptionPane.showMessageDialog(frame, "没有发现SD卡，请插入后再采集！");
                                    close();
                                    return;
                                } else if (str.equalsIgnoreCase("88")) {
                                    if (frame.togetherA == true) {//主站和全部子站模式
                                        frame.releaseFrame();//关闭所有
                                    } else {
                                        if (!frame.transmittedResisButt.isSelected()) {//接地电阻
                                            frame.releaseFrame();
//                                            JOptionPane.showMessageDialog(frame, "采集完成！");
                                        }
                                        close();
                                    }
                                    return;
                                } else if (str.equalsIgnoreCase("11")) {//子站的处理数据
                                    recSiteData = true;
                                    dataLength = dis.readInt();//读取数据量大小   
                                    if (dataLength == 0) {
                                        close();
                                        frame.releaseFrame();
                                        JOptionPane.showMessageDialog(frame, "无法获得数据，请再次进行叫站，确保子站联网！");
                                        return;
                                    }
                                    System.out.println(dataLength + "===");
                                } else if (str.equalsIgnoreCase("EE")) {//外部电阻
                                    recMainResis = true;
                                    dataLength = dis.readInt();//读取数据量大小   
                                    System.out.println(dataLength + "===");
                                }
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //接收相应的返回数据
                    if (recTestStation == true && recGPSMainStation == false && recSiteData == false && recMainResis == false) {//55bb数据 叫站信息
                        try {
                            int totalByte = dis.readInt();
                            if (totalByte == 0) {//不存在gps数据的时候
                                frame.releaseFrame();//释放界面
                                JOptionPane.showMessageDialog(frame, "没有找到子站，请检查连接！");
                                close();
                                return;
                            }
                            if (totalByte >= 49) {//返回正确的gps信息的时候
//                                selectedWires = ModelWin.csamt_WirelessComboBox.getSelectedIndex();//获得所选的无线控制参数
//                                System.out.println(totalByte);
                                testStation(dis, totalByte);//读取全部叫站信息
                                //都选定上次的选项
                                if (!selectedWiresName.equalsIgnoreCase("")) {
                                    for (int i = 0; i < frame.tem_WirelessComboBox.getItemCount(); i++) {
                                        String stationName = frame.tem_WirelessComboBox.getItemAt(i).toString().trim();
                                        if (selectedWiresName.equalsIgnoreCase(stationName)) {
                                            selectedWires = i;
                                            break;
                                        }
                                        if (i == frame.tem_WirelessComboBox.getItemCount() - 1) {
                                            frame.tem_WirelessComboBox.setSelectedIndex(0);
                                            frame.releaseFrame();//释放界面
                                            JOptionPane.showMessageDialog(frame, "之前选定的"
                                                    + frame.tem_WirelessComboBox.getItemAt(selectedWires) + "没有被叫到，请重新叫站！");
                                        }
                                    }
                                    frame.tem_WirelessComboBox.setSelectedIndex(selectedWires);
                                } else {
                                    while (dis.available() > 0) {
                                        dis.read();
                                    }
                                    frame.releaseFrame();//释放界面
                                    JOptionPane.showMessageDialog(frame, "叫站信息不全，请检查子站是否定位好！");
                                    return;
                                }
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        //显示gps信息
                        if (!frame.informationPanel.isVisible()) {
                            frame.informationPanel.setVisible(true);
                        }
                        if (!frame.stationPanel.isVisible()) {
                            frame.stationPanel.setVisible(true);
                        }
                        //添加到主界面面板中
                        frame.stationPanel.add(frame.stationInforScrollPane);
                        frame.stationPanel.updateUI();
                        fitTableColumns(frame.stationInforTable, 0);
                        double difference = frame.stationPanel.getWidth() - TABLE_WIDTH - 1;
                        if (difference < 0) {
                        } else {
                            fitTableColumns(frame.stationInforTable, difference);
                        }
                        TABLE_WIDTH = 0;
                        //只有子站完成后才释放主界面
                        frame.releaseFrame();
                        //采集完成端口取消 回复默认值
                        close();
                    }
                    if (recGPSMainStation == true && recTestStation == false && recSiteData == false && recMainResis == false) { //55cc 主站gps信息
                        try {
                            byte[] gps = new byte[dataLength + 2];
                            byte[] endFlag = new byte[2];
                            byte[] gps1 = new byte[dataLength];
                            dis.read(gps);
                            if (gps[gps.length - 2] == -18) {
                                if (gps[gps.length - 2] == -18) {
//                                    JOptionPane.showMessageDialog(frame, "主站GPS信息");
//                                    close();
//                                    return;
                                } else {
                                    JOptionPane.showMessageDialog(frame, "主站GPS信息可能有误，请重新定位！");
                                    close();
                                    return;
                                }
                            }
                            for (int i = 0; i < dataLength; i++) {
                                gps1[i] = gps[i];
                            }
                            boolean ok = setParaGPS(new String(gps1, "ASCII"));
//                            System.out.println(new String(gps1, "ASCII"));
//                            //添加到主界面面板中
                            if (ok == false) {
                                JOptionPane.showMessageDialog(frame, "主站GPS信息有误，无法显示！");
                                close();
                                return;
                            } else {
                                //显示gps信息
                                if (!frame.informationPanel.isVisible()) {
                                    frame.informationPanel.setVisible(true);
                                }
                                if (!frame.stationPanel.isVisible()) {
                                    frame.mainStationPanel.setVisible(true);
                                }
                                //主站信息
                                frame.mainStationPanel.add(frame.gpsInforScrollPane);
                                frame.mainStationPanel.updateUI();
                                //添加到主界面面板中
                                fitTableColumns(frame.gpsMainStationTable, 0);
                                double difference = frame.mainStationPanel.getWidth() - TABLE_WIDTH - 1;
                                if (difference < 0) {
                                } else {
                                    fitTableColumns(frame.gpsMainStationTable, difference);
                                }
                                TABLE_WIDTH = 0;
                            }
                            //采集完成端口取消 回复默认值
                            close();
                        } catch (IOException ex) {
                            Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (recGPSMainStation == false && recTestStation == false && recSiteData == true && recMainResis == false) {//子站处理数据获得
                        try {
                            //读取开始
                            int count = 0;
                            int count1 = 1;
                            double value = (dis.available() - 1) * 1.0 / unitChannelPoints;
                            int totalChanel = (int) value;
                            if (totalChanel - value == 0) {//判断是不是整形
                                System.out.println(inputStream.available());
                                xyseries = new XYSeries[totalChanel];//初始化
                                while (dis.available() > 0) {
                                    if ((dis.available() - 1) % unitChannelPoints == 0 && dis.available() - 1 >= unitChannelPoints) {
                                        count = totalChanel - (dis.available() - 1) / unitChannelPoints;
                                        xyseries[count] = new XYSeries("时间/电压");
                                        count1 = 1;
                                    }
                                    xyseries[count].add(count1++, dis.readInt());
                                    if (count1 - 1 == 30 && count == totalChanel - 1) {//更新界面 绘图 每道30个点
                                        close();
                                        frame.releaseFrame();
                                        upDataUISiteData(xyseries, totalChanel);
                                    }
//                                System.out.print(Integer.toHexString(dis.read()) + " ");
                                }
                            } else {
                                while (dis.available() > 0) {
                                    dis.read();
                                    if (dis.available() == 1) {
                                        dis.read();
                                        close();
                                        frame.releaseFrame();
                                        JOptionPane.showMessageDialog(frame, "获取数据错误，原因可能为上次采集的文件和本软件不匹配\n或重新采集文件");
                                    }
                                }
                                return;
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(frame, "获取外部线圈电阻信息错误！请重新获取！");
                            Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (recGPSMainStation == false && recTestStation == false && recSiteData == false && recMainResis == true) {//外部电阻
                        try {
                            //读取开始
                            int[] resistances = new int[10];//通道电阻
                            int count = 0;
                            while (dis.available() > 0) {
                                int resistance = dis.readShort() * 10;
                                resistances[count] = resistance;
                                if (count == 9) {//更新图标
                                    frame.addResistanceChart();
//                                    //获得图表
                                    ChartPanel chartPanel = (ChartPanel) frame.resistancePanel.getComponents()[0];
                                    defaultcategorydataset = (DefaultCategoryDataset) ((CategoryPlot) chartPanel.getChart().getPlot()).getDataset();
                                    for (int i = 0; i < 10; i++) {
                                        defaultcategorydataset.setValue(resistances[i], "电阻", "通道 " + (i + 1));
                                    }
                                    frame.resistancePanel.setVisible(true);
                                }
                                count++;
                                frame.releaseFrame();
                                //还原坐标
                                close();
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                if (recStart == true) { //55DD数据
                    // 读取数据头
                    try {
                        if (fileName == null) {
//                            dis.read(headArray);
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                            String date = df.format(new Date()).replaceAll("[-]", "");
                            date = date.replaceAll("[:]", "");
                            date = date.replaceAll("[ ]", "");
                            fileName = date;
                            temp.clear();
                        } else if (fileName != null) {
                            while (dis.available() > 2) {
                                tempData = dis.readInt();
                                temp.add(tempData);
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Write.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //结束标记
                int remainder;
                try {
                    remainder = dis.available(); //剩余字节数
                    System.out.print(dis.available() + " 长度 ");
                    if (remainder == 2) {//结束
                        int endFlag = dis.readShort();
                        if (endFlag == 0x5588) {//5588
                            if (frame.filePath.equals("")) {
                                frame.filePath = "C:\\TEM数据临时文件夹";
                                new File(frame.filePath).mkdir();
                            }
                            String path = frame.filePath.concat("\\").concat(fileName).concat(suffix);
                            final File f = new File(path);//建立文件
                            headArray = frame.originData.writeHeadInfor();
                            save(f, headArray);//保存文件
                            frame.releaseFrame();//释放界面
//                            JOptionPane.showMessageDialog(frame, "数据保存成功！" + path);
                            //采集完成端口取消 回复默认值
                            close();
                            //显示采集文件
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    frame.clearComponent();
                                    readFileAuto(f);
                                }
                            }).start();
                            return;
                        } else if (endFlag == 0x5577) {//5577
                            JOptionPane.showMessageDialog(frame, "没有发现SD卡，请插入后再采集！");
                            frame.releaseFrame();//释放界面
                            close();
                            return;
                        }
                    } else {//有多余的字节数
//                        if (frame.filePath.equals("")) {
//                            frame.filePath = "C:\\TEM数据临时文件夹";
//                            new File(frame.filePath).mkdir();
//                        }
//                        String path = frame.filePath.concat("\\").concat(fileName).concat(suffix);
//                        final File f = new File(path);//建立文件
//                        headArray = frame.originData.writeHeadInfor();
//                        save(f, headArray);//保存文件
//                        frame.releaseFrame();//释放界面
////                            JOptionPane.showMessageDialog(frame, "数据保存成功！" + path);
//                        //采集完成端口取消 回复默认值
//                        close();
//                        //显示采集文件
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                frame.clearComponent();
//                                readFileAuto(f);
//                            }
//                        }).start();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Write.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
    }

    public void readFileAuto(File file) {
        frame.clearComponent();
        frame.originData.readTEM(file);
        //设定标题
        frame.setTitle(file.getName() + "-TEM数据采集程序");
    }

    public void upDataUISiteData(XYSeries[] xyseries, int totalChannel) {
        //添加图表
        frame.stationShowingPanel.removeAll();
        String channelName = "";
        int channel = totalChannel;
        Parameters.channelsEH = new String[channel];
        Parameters.channelsEH[0] = "EX";
        for (int i = 0; i < channel; i++) {
            channelName = "第" + (i + 1) + "道数据  通道定义：";
            frame.stationShowingPanel.add(frame.temc.createDemoPanel(channelName + Parameters.channelsEH[0], "Points", "Voltage (v) ", "TEM", new XYSeriesCollection(xyseries[i])));
        }
        final Component[] components = frame.stationShowingPanel.getComponents();
        final int counts = components.length;
        for (int i = 0; i < totalChannel; i++) {
            ChartPanel chartPanel = (ChartPanel) components[i];
            JFreeChart chart = chartPanel.getChart();
            XYPlot xYPlot = chart.getXYPlot();
            ((XYSeriesCollection) xYPlot.getDataset()).removeAllSeries();
            ((XYSeriesCollection) xYPlot.getDataset()).addSeries(xyseries[i]);
            NumberAxis xAxis = (NumberAxis) xYPlot.getDomainAxis();
            xAxis.setLabel("Points");

        }
        //更新界面
        frame.showingScrollPane.setVisible(false);
        frame.stationShowingScrollPane.setVisible(true);
        frame.showingOutPanel.updateUI();
    }

    /**
     * 保存文件
     */
    public void save(File file, byte[] b) {
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            DataOutputStream dos = new DataOutputStream(fos);//写数据流
            byte[] temp2 = new byte[temp.size() * 4];
            byte[] temp1 = new byte[4];
            int ii = 0;
            int count = 0;
            for (int i = 0; i < temp.size(); i++) {
                ii = temp.get(i);
                temp1 = int2byte(ii);
                for (int j = 0; j < 4; j++) {
                    temp2[count] = temp1[j];
                    count++;
                }
            }
            dos.write(b);
            dos.write(temp2);
            fos.close();
            dos.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "保存文件出现错误，无法保存！");
        }
    }

    /**
     * 数组转为整型
     *
     * @param res
     * @return
     */
    public static byte[] int2byte(int res) {
//        System.out.println(Integer.toBinaryString(res));;
        byte[] targets = new byte[4];
        targets[3] = (byte) (res & 0xff);// 最高位   无符号右移。
        targets[2] = (byte) ((res >> 8) & 0xff);
        targets[1] = (byte) ((res >> 16) & 0xff);
        targets[0] = (byte) (res >>> 24);
//        targets[0] = (byte) (res & 0xff);
//        targets[1] = (byte) (res >> 8 & 0xff);
//        targets[2] = (byte) (res >> 16 & 0xff);
//        targets[3] = (byte) (res >> 24 & 0xff);
        return targets;
    }

    /**
     * 处理文件头数据
     *
     * @param dis
     */
    public String readHeadData(byte[] b) {
        int fileNamePosS = 170;
        int fileNamePosE = 186;
        byte[] fileName = new byte[16];
        for (int i = fileNamePosS; i < fileNamePosE; i++) {
            fileName[i - fileNamePosS] = b[i];
        }
        String file = null;
        try {
            file = new String(fileName, "ASCII");


        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Write.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
//        try {
//            byte[] array = new byte[8];//模式
//            dis.read(array);//读取字节数
//            String str_Array = new String(array, "ASCII");
//            byte[] pos = new byte[128];//工作地点
//            dis.read(pos);
//            String pos_Array = new String(pos, "GB2312");
//            byte[] Long_Lat = new byte[32];//经纬度
//            dis.read(Long_Lat);
//            String Long_Lat_Array = new String(Long_Lat, "ASCII");
//            byte[] posStatus = new byte[2];//定位状态
//            dis.read(posStatus);
//            String posStatus_Array = new String(posStatus, "ASCII");
//            byte[] fileName = new byte[16];//文件名
//            dis.read(fileName);
//            String fileName_Array = new String(fileName, "ASCII");
//            byte[] time = new byte[19];//开始时刻
//            dis.read(time);
//            String time_Array = new String(time, "ASCII");
////            byte[] gain = new byte[2];//增益
//            int gain = dis.readShort();//
////            byte[] channels = new byte[1];//通道
//            int channels = dis.read();//
//            byte[] channelsDef = new byte[32];//通道定义
//            dis.read(channelsDef);
//            String channelsDef_Array = new String(channelsDef, "ASCII");
////            byte[] stationNum = new byte[2];//站点号
//            int stationNum = dis.readShort();
//        } catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return file;
    }

    /**
     * 设置参数
     *
     * @param strs
     * @param selectedItem
     */
    public boolean setParaGPS(String strs) {
        String[] splitStrs = strs.split("[,]");
        if (splitStrs.length == 13) {
            try {

                //截取时间
                String[] timeSplit = splitStrs[1].split("[.]");
                String second = timeSplit[0].substring(timeSplit[0].length() - 2, timeSplit[0].length());
                String minute = timeSplit[0].substring(timeSplit[0].length() - 4, timeSplit[0].length() - 2);
                String hour = String.valueOf(Integer.parseInt(timeSplit[0].substring(timeSplit[0].length() - 6, timeSplit[0].length() - 4)) + 8);
                GregorianCalendar stardBeiJing = new GregorianCalendar(0, 0,
                        0, Integer.parseInt(hour), Integer.parseInt(minute), Integer.parseInt(second));
                Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
                //截取纬度
                String[] latitudeSplit = splitStrs[2].split("[.]");
                String pointLat = latitudeSplit[0].substring(latitudeSplit[0].length() - 2, latitudeSplit[0].length());
                String degreeLat = latitudeSplit[0].substring(0, latitudeSplit[0].length() - 2);
                String concat = pointLat + "." + latitudeSplit[1];
                double pointLatV = Double.parseDouble(concat);
                double degreeLatV = Double.parseDouble(degreeLat);
                //截取经度
                String[] longitudeSplit = splitStrs[4].split("[.]");
                String pointLong = longitudeSplit[0].substring(longitudeSplit[0].length() - 2, longitudeSplit[0].length());
                String degreeLong = longitudeSplit[0].substring(0, longitudeSplit[0].length() - 2);
                String concatLong = pointLong + "." + longitudeSplit[1];
                double pointLongV = Double.parseDouble(concatLong);
                //添加到表格
                String[] strGPS = new String[8];
                int hourInt = stardBeiJing.get(stardBeiJing.HOUR_OF_DAY);
                String hourStr = null;
                if (hourInt < 10) {
                    hourStr = String.valueOf("0" + hourInt);
                } else {
                    hourStr = String.valueOf(hourInt);
                }
                int minuteInt = stardBeiJing.get(stardBeiJing.MINUTE);
                String minuteStr = null;
                if (minuteInt < 10) {
                    minuteStr = String.valueOf("0" + minuteInt);
                } else {
                    minuteStr = String.valueOf(minuteInt);
                }
                int secInt = stardBeiJing.get(stardBeiJing.SECOND);
                String secStr = null;
                if (secInt < 10) {
                    secStr = String.valueOf("0" + secInt);
                } else {
                    secStr = String.valueOf(secInt);
                }
                strGPS[0] = hourStr + ":" + minuteStr + ":" + secStr;
                strGPS[1] = degreeLat + "°" + pointLatV + "' " + splitStrs[3];
                strGPS[2] = degreeLong + "°" + pointLongV + "' " + splitStrs[5];
                strGPS[3] = splitStrs[7];
                strGPS[4] = splitStrs[8];
                strGPS[5] = splitStrs[9] + " " + splitStrs[10];
                strGPS[6] = splitStrs[11] + " " + splitStrs[12];
                strGPS[7] = splitStrs[6];
                ((DefaultTableModel) frame.gpsMainStationTable.getModel()).addRow(strGPS);
                // 设置table表头居中
                DefaultTableCellRenderer thr = (DefaultTableCellRenderer) (frame.gpsMainStationTable.getTableHeader().getDefaultRenderer());
                thr.setHorizontalAlignment(JLabel.CENTER);
                frame.gpsMainStationTable.getTableHeader().setDefaultRenderer(thr);
                DefaultTableCellRenderer cell;//设置单元格居中
                for (int m = 0; m < frame.gpsMainStationTable.getRowCount(); m++) {
                    for (int j = 0; j < frame.gpsMainStationTable.getColumnCount(); j++) {
                        if (j == 7) {//增加参数需要修改
                            String s = frame.gpsMainStationTable.getValueAt(m, j).toString();
                            if (s.equals("1") || s.equals("2")) {
                                frame.gpsMainStationTable.setValueAt("OK", m, j);
                            } else {
                                frame.gpsMainStationTable.setValueAt("NO", m, j);
                            }
                        } else {
                            cell = (DefaultTableCellRenderer) frame.gpsMainStationTable.getCellRenderer(m, j);
                            cell.setHorizontalAlignment(JLabel.CENTER);
                        }
                    }
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 读取叫站信息
     *
     * @param dis
     */
    public void testStation(DataInputStream dis, int totalBytes) {
        if (totalBytes > Integer.MAX_VALUE) {
            JOptionPane.showMessageDialog(frame, "获取失败！请重新叫站！");
            return;
        }
        try {
            byte[] b = new byte[totalBytes];
            dis.read(b);//读取完毕
            ArrayList<Integer> posEE = new ArrayList<Integer>();//存放EE在字节流中的位置
            int temp = -18;//EE结尾
            //找到EE的位置
            for (int i = 0; i < b.length; i++) {
                if (b[i] == temp) {
                    posEE.add(i + 1);
                }
            }
            //抽取每一道数据
            byte[] unitChannel = null;
            byte[] gps = null;//GPS信息
            byte[] physicalAdd = new byte[2];//物理地址
            byte[] voltage = new byte[1];//电压
            byte[] isFReady = new byte[1];//子站准备状态
            byte[] resistance = new byte[6];//各通道电阻
            byte[] fileName = new byte[22];//文件名
            int unitCount = 0;
            for (int i = 0; i < posEE.size(); i++) {
                if (i == 0) {
                    unitChannel = new byte[posEE.get(i)];
                    for (int j = 0; j < posEE.get(i); j++) {
                        unitChannel[unitCount++] = b[j];
                    }
                    //填表格
                    addreassGPS(unitChannel, gps, physicalAdd, voltage, isFReady, resistance, fileName);
                } else if (i > 0) {
                    unitChannel = new byte[posEE.get(i) - posEE.get(i - 1)];
                    for (int j = posEE.get(i - 1); j < posEE.get(i); j++) {
                        unitChannel[unitCount++] = b[j];
                    }
                    //填表格
                    addreassGPS(unitChannel, gps, physicalAdd, voltage, isFReady, resistance, fileName);
                }
                unitCount = 0;


            }
        } catch (Exception ex) {
            Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(frame, "获取失败！请重新叫站！");
        }
    }

    /**
     *   * 处理叫站信息
     *
     * @param unitChannel
     * @param gps
     * @param physicalAdd
     * @param voltage
     * @param isFReady
     * @param resistance
     * @param fileName
     */
    public void addreassGPS(byte[] unitChannel, byte[] gps, byte[] physicalAdd,
            byte[] voltage, byte[] isFReady, byte[] resistance, byte[] fileName) {

        String str_fileName = "";//保存文件名
        byte[] suffix = new byte[2];//判定是否存在文件名
        suffix[0] = unitChannel[unitChannel.length - 3];
        suffix[1] = unitChannel[unitChannel.length - 2];
        String strSuffix = new String(suffix);
        //开始赋值
        if (strSuffix.equalsIgnoreCase("CS")
                || strSuffix.equalsIgnoreCase("AM")
                || strSuffix.equalsIgnoreCase("TM")
                || strSuffix.equalsIgnoreCase("CR")
                || strSuffix.equalsIgnoreCase("IP")) {//判定最后一个字符是不是大于10 文件名里不存在单字节大于10
//            gps = new byte[unitChannel.length - 1 - 22 - 2 - 1];//减去电压 文件名 编号2 ee1
            int count = 0;//gps
            int countFileName = 0;//文件名
            int countResistance = 0;//电阻值
            gps = new byte[unitChannel.length - 1 - 22 - 2 - 1 - 1 - 6];//减去电压 文件名 编号2 ee1 子站准备状态1 电阻 6
            for (int m = 0; m < unitChannel.length; m++) {
                if (m == 0) {//获得物理地址
                    physicalAdd[1] = unitChannel[0];
                    physicalAdd[0] = unitChannel[1];
                } else if (m > 1 && m < unitChannel.length - 31) {//获得GPS信息
                    gps[count] = unitChannel[m];
                    count++;
                } else if (m == unitChannel.length - 30) {//获得子站准备状态
                    isFReady[0] = unitChannel[m];
                } else if (m > unitChannel.length - 30 && m <= unitChannel.length - 24) {//获得三个通道电阻值
                    resistance[countResistance] = unitChannel[m];
                    countResistance++;
                } else if (m > unitChannel.length - 24 && m < unitChannel.length - 1) {//文件名
                    fileName[countFileName] = unitChannel[m];
                    countFileName++;
                } else if (m == unitChannel.length - 31) {//电压值
                    voltage[0] = unitChannel[unitChannel.length - 31];
                }
            }
            str_fileName = new String(fileName);
        } else {
//            gps = new byte[unitChannel.length - 1 - 2 - 1];//减减去电压 文件名 编号2 ee1
//            int count = 0;//gps
//            int countFileName = 0;//文件名
//            int countResistance = 0;//电阻值
//            gps = new byte[unitChannel.length - 1 - 2 - 1 - 1 - 6];//减减去电压 文件名 编号2 ee1 子站准备状态1 电阻6
//            for (int m = 0; m < unitChannel.length; m++) {
//                if (m == 0) {//获得物理地址
//                    physicalAdd[0] = unitChannel[1];
//                    physicalAdd[1] = unitChannel[0];
//                } else if (m > 1 && m < unitChannel.length - 9) {//获得GPS信息
//                    gps[count] = unitChannel[m];
//                    count++;
//                } else if (m == unitChannel.length - 8) {//获得子站準備狀態
//                    isFReady[0] = unitChannel[m];
//                } else if (m > unitChannel.length - 8 && m < unitChannel.length - 1) {//获得通道电阻值
//                    resistance[countResistance] = unitChannel[m];
//                    countResistance++;
//                } else if (m == unitChannel.length - 9) {//电压值
//                    voltage[0] = unitChannel[unitChannel.length - 9];
//                }
//                str_fileName = "上次无采集";
//            }
            int count = 0;//gps
            int countFileName = 0;//文件名
            int countResistance = 0;//电阻值
            gps = new byte[unitChannel.length - 1 - 22 - 2 - 1 - 1 - 6];//减去电压 文件名 编号2 ee1 子站准备状态1 电阻 6
            for (int m = 0; m < unitChannel.length; m++) {
                if (m == 0) {//获得物理地址
                    physicalAdd[1] = unitChannel[0];
                    physicalAdd[0] = unitChannel[1];
                } else if (m > 1 && m < unitChannel.length - 31) {//获得GPS信息
                    gps[count] = unitChannel[m];
                    count++;
                } else if (m == unitChannel.length - 30) {//获得子站准备状态
                    isFReady[0] = unitChannel[m];
                } else if (m > unitChannel.length - 30 && m <= unitChannel.length - 24) {//获得三个通道电阻值
                    resistance[countResistance] = unitChannel[m];
                    countResistance++;
                } else if (m > unitChannel.length - 24 && m < unitChannel.length - 1) {//文件名
                    fileName[countFileName] = unitChannel[m];
                    countFileName++;
                } else if (m == unitChannel.length - 31) {//电压值
                    voltage[0] = unitChannel[unitChannel.length - 31];
                }
            }
            str_fileName = "上次无采集";
        }
        String str = null;
        String str1 = null;
        String str2 = null;
        String str3 = null;
        String str4_1 = null;
        String str4_2 = null;
        String str4_3 = null;
        str = new String(gps);//gps
        str1 = String.valueOf(getShort(physicalAdd, 0));//物理地址 十进制的
//        str1 = Integer.toHexString(getShort(physicalAdd, 0));//物理地址
        DecimalFormat format = new DecimalFormat("0.0");//电压定义保留位数
        str2 = String.valueOf(format.format(voltage[0] / 10.0)).concat(" V");
        if (isFReady[0] == 0) {
            str3 = "×";
        } else {
            str3 = "√";
        }
        //通道电阻值
        byte[] resisTemp = new byte[2];
        int valueResis = -1;
        resisTemp[0] = resistance[1];
        resisTemp[1] = resistance[0];
        valueResis = getShort_Int(resisTemp, 0);
        if (valueResis <= 3000) {
            str4_1 = String.valueOf(format.format(valueResis / 10.0)).concat(" K");
        } else {
            str4_1 = "XXX";
        }
        resisTemp[0] = resistance[3];
        resisTemp[1] = resistance[2];
        valueResis = getShort_Int(resisTemp, 0);
        if (valueResis <= 3000) {
            str4_2 = String.valueOf(format.format(valueResis / 10.0)).concat(" K");
        } else {
            str4_2 = "XXX";
        }
        resisTemp[0] = resistance[5];
        resisTemp[1] = resistance[4];
        valueResis = getShort_Int(resisTemp, 0);
        if (valueResis <= 3000) {
            str4_3 = String.valueOf(format.format(valueResis / 10.0)).concat(" K");
        } else {
            str4_3 = "XXX";
        }
        str = str.concat(",").concat(str1).concat(",").concat(str2).concat(",").concat(str3).concat(",")
                .concat("<html>" + "一道：" + str4_1).concat(" <br> ").concat("二道：" + str4_2).concat("<br>").concat("三道：" + str4_3 + "<html>").concat(",")
                .concat("  " + str_fileName + "  ");//gps+站点编号+电压+子站准备状态+电阻+文件名
        //添加到无线控制列表中
        if (recAddWireless == 0) {//只添加一次
            //清空列表 存在gps其他数据的话
            if (frame.stationInforTable.getRowCount() != 0) {
                frame.clearTable(frame.stationInforTable);
            }

            //清空下拉菜单
            frame.tem_WirelessComboBox.removeAllItems();
            //添加项目
            frame.tem_WirelessComboBox.addItem("主站");
            frame.tem_WirelessComboBox.addItem("主站+全部子站");
            frame.tem_WirelessComboBox.addItem("全部子站");
            recAddWireless++;
        }
        String[] strs = new String[10];
        strs = stationInformation(str);//抽取gps数据
        if (frame.tem_WirelessComboBox.getItemCount() >= 2) {//添加叫道的子站 并且定位好了
            frame.tem_WirelessComboBox.addItem(str1);
        }
        //调整行颜色
        SetTableRowColor.makeFace(frame.stationInforTable);
        ((DefaultTableModel) frame.stationInforTable.getModel()).addRow(strs);
        DefaultTableCellRenderer cell;//设置单元格居中
        for (int m = 0; m < frame.stationInforTable.getRowCount(); m++) {
            for (int j = 0; j < frame.stationInforTable.getColumnCount(); j++) {
                String s = frame.stationInforTable.getValueAt(m, j).toString();
                if (j == 9) {//增加参数需要修改
//                    if (s.trim().equals("1") || s.trim().equals("2")) {
//                        frame.stationInforTable.getColumn("卫星定位状态").setCellRenderer(new DownloadProgressBar(1));
//                    } else {
//                        frame.stationInforTable.getColumn("卫星定位状态").setCellRenderer(new DownloadProgressBar(0));
//                    }
                } else {
                    cell = (DefaultTableCellRenderer) frame.stationInforTable.getCellRenderer(m, j);
                    cell.setHorizontalAlignment(JLabel.CENTER);
                }
            }
        }
    }

    /**
     * 根据表格内容自动调整列宽度
     *
     * @param table
     */
    public static void fitTableColumns(JTable myTable, double difference) {
        myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        int columnCount = myTable.getColumnCount();
        double unitAdd = difference / columnCount;
        Enumeration columns = myTable.getColumnModel().getColumns();
        int count = 1;
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            double width = header.getDefaultRenderer().getTableCellRendererComponent(myTable, column.getIdentifier(), false, false, -1, col).getPreferredSize().getWidth();
            for (int row = 0; row < rowCount; row++) {
                double preferedWidth = myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable, myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column); // 此行很重要    
            if (unitAdd > 0) {
                column.setWidth((int) (width + myTable.getIntercellSpacing().width + unitAdd));
            } else {
                column.setWidth((int) (width + myTable.getIntercellSpacing().width));
            }
            if (difference == 0) {
                TABLE_WIDTH += width + myTable.getIntercellSpacing().width + unitAdd;
            }
            count++;
        }
    }

    /**
     * 处理GPS信息 表格显示
     *
     * @param list
     */
    public String[] stationInformation(String str) {
        String[] strs = new String[10];
        String[] splitStrs = str.split("[,]");
        strs[0] = String.valueOf(frame.stationInforTable.getRowCount() + 1);
        //物理地址
        String physicalAddress = splitStrs[splitStrs.length - 5];
        strs[1] = physicalAddress;
        //时间
        try {
            String[] timeSplit = splitStrs[1].split("[.]");
            String second = timeSplit[0].substring(timeSplit[0].length() - 2, timeSplit[0].length());
            String minute = timeSplit[0].substring(timeSplit[0].length() - 4, timeSplit[0].length() - 2);
            String hour = timeSplit[0].substring(timeSplit[0].length() - 6, timeSplit[0].length() - 4);
            Calendar c = Calendar.getInstance();
            String year = String.valueOf(c.get(Calendar.YEAR));
            String month = String.valueOf(c.get(Calendar.MONTH));
            String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
            GregorianCalendar stardBeiJing = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month),
                    Integer.parseInt(day), Integer.parseInt(hour) + 8, Integer.parseInt(minute), Integer.parseInt(second));
            String hour1 = String.valueOf(stardBeiJing.get(stardBeiJing.HOUR_OF_DAY));
            String minute1 = String.valueOf(stardBeiJing.get(stardBeiJing.MINUTE));
            String second1 = String.valueOf(stardBeiJing.get(stardBeiJing.SECOND));
            if (hour1.length() == 1) {
                hour1 = "0" + hour1;
            }
            if (minute1.length() == 1) {
                minute1 = "0" + minute1;
            }
            if (second1.length() == 1) {
                second1 = "0" + second1;
            }
            strs[2] = "  " + hour1 + ":" + minute1 + ":" + second1 + "  ";
        } catch (Exception e) {
            strs[2] = " 无定位 ";
        }
        //纬度
        try {
            String[] latitudeSplit = splitStrs[2].split("[.]");
            String pointLat = latitudeSplit[0].substring(latitudeSplit[0].length() - 2, latitudeSplit[0].length());
            String degreeLat = latitudeSplit[0].substring(0, latitudeSplit[0].length() - 2);
            String concat = pointLat + "." + latitudeSplit[1];
            double pointLatV = Double.parseDouble(concat);
            strs[3] = "  " + degreeLat + "°" + pointLatV + "' " + splitStrs[3] + "  ";
        } catch (Exception e) {
            strs[3] = " 无定位 ";
        }
        //经度
        try {
            String[] longitudeSplit = splitStrs[4].split("[.]");
            String pointLong = longitudeSplit[0].substring(longitudeSplit[0].length() - 2, longitudeSplit[0].length());
            String degreeLong = longitudeSplit[0].substring(0, longitudeSplit[0].length() - 2);
            String concatLong = pointLong + "." + longitudeSplit[1];
            double pointLongV = Double.parseDouble(concatLong);
            strs[4] = "  " + degreeLong + "°" + pointLongV + "' " + splitStrs[5] + "  ";
        } catch (Exception e) {
            strs[4] = " 无定位 ";
        }
//        //电池电压
//        String voltage = splitStrs[splitStrs.length - 3];
//        strs[5] = voltage;
//        //子站是否准备
//        String isReady = splitStrs[splitStrs.length - 2];
//        strs[6] = isReady;
//        //子站上次采集是否正确
//        String isSuccess = splitStrs[splitStrs.length - 1];
//        strs[7] = isSuccess;
//        //卫星定位状态
//        String stateSatellite = splitStrs[splitStrs.length - 5];
//        strs[8] = stateSatellite;
        //电池电压
        String voltage = splitStrs[splitStrs.length - 4];
        strs[5] = voltage;
        //子站准备状态
        String isFReady = splitStrs[splitStrs.length - 3];
        strs[6] = isFReady;
        //电阻值
        String resistance_1 = splitStrs[splitStrs.length - 2];
        strs[7] = resistance_1;
        //文件名
        String isSuccess = splitStrs[splitStrs.length - 1];
        strs[8] = isSuccess;
        //卫星定位状态
        String stateSatellite = splitStrs[splitStrs.length - 6];
        strs[9] = stateSatellite.trim();
        try {
            Integer.parseInt(strs[9]);
        } catch (Exception e) {
            strs[9] = 0 + "";
        }
        return strs;
    }

    /**
     * 通过byte数组取到short
     *
     * @param b
     * @param index 第几位开始取
     * @return
     */
    public static short getShort(byte[] b, int index) {
        return (short) (((b[index + 1] << 8) | b[index + 0] & 0xff));
    }

    /**
     * 通过byte数组取到short
     *
     * @param b
     * @param index 第几位开始取
     * @return
     */
    public static int getShort_Int(byte[] b, int index) {
        return ((b[index + 1] << 8) | b[index + 0] & 0xff);
    }

    /**
     * 还原叫站 关闭端口
     */
    public void close() {
//        frame.fresh();
        frame.testStation.setSelected(false);
        frame.testStation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/geopen/testStop.png")));
        frame.testStationMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/geopen/testStop16.png")));
        frame.startToggleButton.setSelected(false);
        frame.startToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/geopen/start32.png")));
        frame.startMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/geopen/start16.png")));
        frame.transmittedResisButt.setSelected(false);//获得子站电阻
        frame.transmittedResisButt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/geopen/resistance32.png")));
        frame.transmittedResisMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/geopen/resistance16.png")));
        frame.mainResisButton.setSelected(false);//获得子站电阻
        frame.mainResisButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/geopen/mainResis32.png")));
        frame.mainResisMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/geopen/mainResis16.png")));
        frame.siteDataToggleButton.setSelected(false);//获得子站处理数据
        frame.siteDataToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/geopen/siteData32.png")));
        frame.siteDataMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/geopen/siteData16.png")));
    }
}
