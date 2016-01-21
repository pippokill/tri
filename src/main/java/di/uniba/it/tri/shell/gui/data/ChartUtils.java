/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.shell.gui.data;

import di.uniba.it.tri.api.TriResultObject;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author pierpaolo
 */
public class ChartUtils {

    public static ChartPanel plotWords(List<TriResultObject> results, String title, String description) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (TriResultObject r : results) {
            String[] split = r.getValue().split("\t");
            dataset.addValue(r.getScore(), split[1], split[0]);
        }
        JFreeChart lineChart = ChartFactory.createLineChart(
                title,
                "Temporal period", description,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));
        return chartPanel;
    }

}
