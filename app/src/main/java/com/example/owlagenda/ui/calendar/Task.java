package com.example.owlagenda.ui.calendar;

import androidx.annotation.ColorRes;

import com.example.owlagenda.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private LocalDateTime time;
    private Airport departure;
    private Airport destination;
    @ColorRes private int color;

    public Task(LocalDateTime time, Airport departure, Airport destination, @ColorRes int color) {
        this.time = time;
        this.departure = departure;
        this.destination = destination;
        this.color = color;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Airport getDeparture() {
        return departure;
    }

    public void setDeparture(Airport departure) {
        this.departure = departure;
    }

    public Airport getDestination() {
        return destination;
    }

    public void setDestination(Airport destination) {
        this.destination = destination;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public static List<Task> generateFlights() {
        ArrayList<Task> tasks = new ArrayList<>();
        // Suponha que `currentMonth` seja uma instância de `YearMonth`.
        YearMonth currentMonth = YearMonth.now(); // Inicialize conforme necessário

        LocalDate date = currentMonth.minusMonths(1).atDay(9);

        tasks.add(
                new Task(
                        LocalDateTime.of(date, LocalTime.of(20, 15)),
                        new Airport("Asaba", "ABB"),
                        new Airport("Port Harcourt", "PHC"),
                        R.color.example_3_blue // Certifique-se de que você tem um equivalente em Java para cores.
                )
        );

        date = currentMonth.minusMonths(1).atDay(15);

        tasks.add(
                new Task(
                        LocalDateTime.of(date, LocalTime.of(20, 15)),
                        new Airport("Asaba", "ABB"),
                        new Airport("Port Harcourt", "PHC"),
                        R.color.cor_primaria // Certifique-se de que você tem um equivalente em Java para cores.
                )
        );

        date = currentMonth.minusMonths(1).atDay(19);

        tasks.add(
                new Task(
                        LocalDateTime.of(date, LocalTime.of(20, 15)),
                        new Airport("Asaba", "ABB"),
                        new Airport("Port Harcourt", "PHC"),
                        R.color.blue_grey_700 // Certifique-se de que você tem um equivalente em Java para cores.
                )
        );

        date = currentMonth.minusMonths(1).atDay(20);

        tasks.add(
                new Task(
                        LocalDateTime.of(date, LocalTime.of(20, 15)),
                        new Airport("Asaba", "ABB"),
                        new Airport("Port Harcourt", "PHC"),
                        R.color.brown_700 // Certifique-se de que você tem um equivalente em Java para cores.
                )
        );

        date = currentMonth.minusMonths(1).atDay(20);

        tasks.add(
                new Task(
                        LocalDateTime.of(date, LocalTime.of(20, 15)),
                        new Airport("Asaba", "ABB"),
                        new Airport("Port Harcourt", "PHC"),
                        R.color.example_3_blue // Certifique-se de que você tem um equivalente em Java para cores.
                )
        );

        return tasks;
    }

    public static String flightDateTimeFormatter(LocalDateTime time) {
        return DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm").format(time);
    }


    static class Airport {
        private String city;
        private String code;

        public Airport(String city, String code) {
            this.city = city;
            this.code = code;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
