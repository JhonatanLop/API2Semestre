package org.openjfx.api2semestre.view.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.openjfx.api2semestre.appointment.Appointment;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.StringConverter;

public class ChartGenerator {

    @SuppressWarnings("unused") private static class ChartData {
        LineChart<Number, Number> chart;
        XYChart.Series<Number, Number> series;
        NumberAxis xAxis;
        NumberAxis yAxis;
        public ChartData(
            LineChart<Number, Number> chart,
            XYChart.Series<Number, Number> series,
            NumberAxis xAxis,
            NumberAxis yAxis
        ) {
            this.chart = chart;
            this.series = series;
            this.xAxis = xAxis;
            this.yAxis = yAxis;
        }
    }
    
    private static  ChartData emptyChart (
        String title,
        Optional<NumberAxis> xAxisOptional,
        Optional<NumberAxis> yAxisOptional
    
    ) {
        // Create the x-axis representing time
        NumberAxis xAxis = xAxisOptional.isPresent() ? xAxisOptional.get() : new NumberAxis();

        // Create the y-axis representing integers
        NumberAxis yAxis = yAxisOptional.isPresent() ? yAxisOptional.get() : new NumberAxis();

        // Create the line chart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        // Set the title of the chart
        lineChart.setTitle(title);

        // Create a series to hold the data points
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        // Add the series to the line chart
        lineChart.getData().add(series);

        // Remove os circulos dos pontos da linha
        lineChart.setCreateSymbols(false);

        // Remove a legenda
        lineChart.setLegendVisible(false);

        return new ChartData(lineChart, series, xAxis, yAxis);

    }

    public static LineChart<Number, Number> hourIntersectionCountGraph (Appointment[] appointments) {

        NumberAxis xAxis = new NumberAxis(0, 1440, 60);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override public Number fromString(String string) { return null; }
            @Override public String toString (Number object) {
                int minutes = object.intValue();
                return String.format("%02d:%02d", (minutes / 60) % 24, minutes % 60);
            }
        });

        ChartData chartData = emptyChart(
            "Frequência de Apontamento por Hora do Dia",
            Optional.of(xAxis),
            Optional.empty()
        );

        LineChart<Number, Number> lineChart = chartData.chart;
        XYChart.Series<Number, Number> series = chartData.series;
        NumberAxis yAxis = chartData.yAxis;

        // Add the data points to the series
        long startChart = Timestamp.valueOf("2023-01-01 00:00:00").getTime();
        long endChart = Timestamp.valueOf("2023-01-02 00:00:00").getTime();

        final LocalDate defaultDate = LocalDate.of(2023, 1, 1);

        int maxIntersectionCount = 0;

        // Convert the timestamps to time values in minutes
        for (long timeAtPosition = startChart; timeAtPosition <= endChart; timeAtPosition += 60000) {
            
            double position = (double) (timeAtPosition - startChart) / (endChart - startChart) * 24 * 60;

            // Count the number of intersections for the current time
            int intersectionCount = 0;
            for (Appointment apt : appointments) {

                LocalDateTime aptStartDateTime = apt.getStart().toLocalDateTime();
                LocalDateTime aptEndDateTime = apt.getEnd().toLocalDateTime();   

                int dayCount = aptEndDateTime.toLocalDate().compareTo(aptStartDateTime.toLocalDate());
             
                long aptStart = Timestamp.valueOf(aptStartDateTime.toLocalTime().atDate(
                    defaultDate
                )).getTime();

                long aptEnd = Timestamp.valueOf(aptEndDateTime.toLocalTime().atDate(
                    defaultDate.plusDays(dayCount)
                )).getTime();

                for (int i = 0; i < 1 + dayCount; i++) {
                    if (timeAtPosition + (i * 86400000) <= aptStart || timeAtPosition + (i * 86400000) > aptEnd) continue;
                    intersectionCount++;
                }
            }

            // Add the data point to the series
            series.getData().add(new XYChart.Data<>(position, intersectionCount));
            if (intersectionCount > maxIntersectionCount) maxIntersectionCount = intersectionCount;

        }

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);        
        yAxis.setUpperBound(maxIntersectionCount + 1);
        yAxis.setTickUnit(1.0);
        yAxis.setMinorTickVisible(false);

        return lineChart;
    }
}