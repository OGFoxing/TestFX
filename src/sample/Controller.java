package sample;

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
    CategoryAxis xAxis;
    @FXML
    NumberAxis yAxis;
    @FXML
    LineChart<Number,Number> bc;
    @FXML
    BarChart<String,Number> barChart;
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

    XYChart.Series core;
    XYChart.Series memoryfree;
    XYChart.Series memorytotal;
    XYChart.Series memoryusage;
    XYChart.Series freespace;

    OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*for 1st time get data */
        iReader iR = new iReader();
        iR.getDataFromsOS();

        charts();

        /*labels*/
        HashMap<String ,String> value = iOS();
        String stringOS = value.toString();
        labelOS.setText(stringOS);

        String  stringCore = Integer.toString(getCores());
        labelCore.setText(stringCore);

        root.setText(getRoot());

        String stringFreeHdd = String.valueOf(getPieChart().get(0).getPieValue());
        freeHdd.setText(String.format("%.7s",stringFreeHdd) + " GB");
        String stringUsageHdd = String.valueOf(getPieChart().get(1).getPieValue());
        usageHdd.setText(String.format("%.7s",stringUsageHdd) + " GB");

        systemInfoReader();

        Thread th = new Thread(new iReader());
        th.start();
    }

    protected void systemInfoReader() {
        String line = "";
        final String CHARSET = "UTF8";
        try {
            //PrintStream ps = new PrintStream(System.out,true,"IBM866");
            Process proc = Runtime.getRuntime().exec("systeminfo /FO table");
            BufferedReader bf = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            while((line = bf.readLine()) != null) {
                line = new String(line.getBytes(), "IBM866");
                System.out.println(line);
                //ps.println(line);
                textArea.appendText(line + "\n");
            }

        }
        catch (Exception ex) {

        }
        /*
        final String UTF8_STR = "Строка в UTF8";

        final String CHARSET = "UTF8";

        String cp866 = null;
        try {
            cp866 = new String(UTF8_STR.getBytes(), CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(cp866);
        */
    }

    protected void charts() {
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
        pieChart.setTitle("HDD");
        pieChart.getData().addAll(getPieChart());

        /*BarChart*/
        core = new XYChart.Series();
        core.setName("Threads");
        core.getData().add(new XYChart.Data(core.getName(),getCores()));
        barChart.getData().add(core);
    }

    protected HashMap<String, String> iOS() {
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

    public ObservableList<PieChart.Data> getPieChart() {
        double free = 0;
        double usage = 0;

        File[] roots = File.listRoots();
        for(File root: roots) {
            free = root.getFreeSpace() / 1024.0 /1024.0 / 1024.0;
            usage = (root.getTotalSpace() - root.getFreeSpace()) / 1024.0 /1024.0 / 1024.0;
        }
        System.out.println(free);
        System.out.println(usage);
        ObservableList<PieChart.Data> list = FXCollections.observableArrayList(
                new PieChart.Data("Free",free),
                new PieChart.Data("Usage", usage)
        );

        return list;
    }
    class iReader implements Runnable{
        double freeRam;
        double totalRam;
        double usedRam;

        @Override
        public  synchronized void run() {
            try {
                int time = 1;
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
    }


}
