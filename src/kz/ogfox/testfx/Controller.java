package kz.ogfox.testfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import com.sun.management.OperatingSystemMXBean;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;


public class Controller implements Initializable{
    @FXML
    NumberAxis yAxis;
    @FXML
    LineChart<Number,Number> bc;
    @FXML
    BarChart<Number,Number> barChart;
    @FXML
    PieChart pieChart;
    @FXML
    Label labelOS;
    @FXML
    Label labelCore;
    @FXML
    Label root;
    @FXML
    Label freeHdd;
    @FXML
    Label usageHdd;
    @FXML
    TextArea textArea;

    protected XYChart.Series coreBar;
    protected XYChart.Series memoryfree;
    protected XYChart.Series memorytotal;
    protected XYChart.Series memoryusage;
    protected XYChart.Series freespace;

    OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    iReader iR = new iReader();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        yAxis.autoRangingProperty();
        addDataToCharts();
        setDataToLabels();
        iR.getSystemInfo();
        Thread th = new Thread(new iReader());
        th.start();
    }

    protected void setDataToLabels() {
        /*labels*/
        HashMap<String ,String> value = iR.getOS();
        String stringOS = value.toString();
        labelOS.setText(stringOS);

        String  stringCore = Integer.toString(iR.getCores());
        labelCore.setText(stringCore);

        root.setText(iR.getRoot());

        String stringFreeHdd = String.valueOf(iR.getRamInfo().get(0).getPieValue());
        freeHdd.setText(String.format("%.7s",stringFreeHdd) + " GB");
        String stringUsageHdd = String.valueOf(iR.getRamInfo().get(1).getPieValue());
        usageHdd.setText(String.format("%.7s",stringUsageHdd) + " GB");
    }


    protected void addDataToCharts() {
        /*about RAM free*/
        memoryfree = new XYChart.Series();
        memoryfree.setName("RAM free");
        /*about RAM total*/
        memorytotal = new XYChart.Series();
        memorytotal.setName("RAM total");
         /*about RAM usage*/
        memoryusage = new XYChart.Series();
        memoryusage.setName("RAM usage");
        /*LineChart*/
        bc.setTitle("RAM Live Monitor");
        bc.getData().addAll(memoryfree,memorytotal,memoryusage);

        /*PieChart*/
        freespace = new XYChart.Series();
        pieChart.setTitle("Root disk");
        pieChart.getData().addAll(iR.getRamInfo());

        /*BarChart*/
        coreBar = new XYChart.Series();
        coreBar.setName("Threads");
        coreBar.getData().add(new XYChart.Data(coreBar.getName() ,iR.getCores()));
        barChart.getData().addAll(coreBar);
    }


    class iReader implements Runnable {
        double freeRam;
        double totalRam;
        double usedRam;

        @Override
        public  synchronized void run() {
           setDataToCharts();
        }

        protected void setDataToCharts() {
            try {
                int time = 0;
                while(true) {
                    time+=2;
                    getDataFromsOS();
                    memoryfree.getData().add(new XYChart.Data(time + " ",freeRam));
                    memorytotal.getData().add(new XYChart.Data(time + " ",totalRam));
                    memoryusage.getData().add(new XYChart.Data(time +" ",usedRam));

                    Thread.sleep(2000);
                }
            }
            catch (Exception ex) {

            }
        }
        protected void getDataFromsOS() {
            freeRam = os.getFreePhysicalMemorySize()/ 1024.0 / 1024.0 / 1024.0;
            totalRam = os.getTotalPhysicalMemorySize() / 1024.0 / 1024.0 / 1024.0;
            usedRam = (os.getTotalPhysicalMemorySize() - os.getFreePhysicalMemorySize()) / 1024.0 / 1024.0 / 1024.0;
        }


        protected HashMap<String, String> getOS() {
            String Name = System.getProperty("os.name");
            String arch = null;
            double totalRam = os.getTotalPhysicalMemorySize() / 1024.0 / 1024.0 / 1024.0;
            if( totalRam <= 3200) {
                arch = "x64";
            }
            else {
                arch = "x32";
            }
            HashMap<String,String > list = new HashMap<String,String>();
            list.put(Name,arch);
            return list;
        }

        protected int getCores() {
            int cores = 0;
            cores = os.getAvailableProcessors();
            return cores;
        }

        protected String getRoot() {
            String path = System.getenv("SystemRoot");
            return path;
        }

        protected ObservableList<PieChart.Data> getRamInfo() {
            double free = 0;
            double usage = 0;

            File root = new File("c:");
            free = root.getFreeSpace() / 1024.0 /1024.0 / 1024.0;
            usage = (root.getTotalSpace() - root.getFreeSpace()) / 1024.0 /1024.0 / 1024.0;
            ObservableList<PieChart.Data> list = FXCollections.observableArrayList(
                    new PieChart.Data("Free",free),
                    new PieChart.Data("Usage", usage)
            );
            return list;
        }

        protected void getSystemInfo() {
            String line = "";
            final String CHARSET = "IBM866";
            try {
                Process proc = Runtime.getRuntime().exec("systeminfo");
                BufferedReader bf = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while((line = bf.readLine()) != null) {
                    line = new String(line.getBytes(),CHARSET);
                    textArea.appendText(line + "\n");
                }

            }
            catch (Exception ex) {

            }
        }
    }


}
