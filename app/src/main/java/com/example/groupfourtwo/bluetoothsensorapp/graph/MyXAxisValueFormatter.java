package com.example.groupfourtwo.bluetoothsensorapp.graph;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.TimeZone;

/**
 * Created by kim on 23.05.17.
 */

public class MyXAxisValueFormatter implements IAxisValueFormatter {

        private int timezone = TimeZone.getDefault().getRawOffset(); // 2 is Berlin sommertime-- 1 is Berlin wintertime
        private int dstSavings = TimeZone.getDefault().getDSTSavings();

        protected String[] mMonths = new String[]{
                "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        private BarLineChartBase<?> chart;

        private int pointsPerHour; //How many Datapoints do we Draw per  Hour

        private long startInMilSec; //How many milliseconds behind the first Jan 2016 do we start.

        public void setPointsPerHour(int p){pointsPerHour = p;}

        public void setStartInSec(long s){startInMilSec = s;}

        public MyXAxisValueFormatter(BarLineChartBase<?> chart) {
            this.chart = chart;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {


            int sec = (int) value*3600/pointsPerHour + (int) (startInMilSec/1000l) +  (timezone + dstSavings)/1000;

            int minutes = determineMinutes(sec);

            int hours = determineHours(sec);

            int days = determineDays(hours);

            int year = determineYear(days);

            int month = determineMonth(days);

            String minuteName = String.format("%02d", minutes % 60);
            String hourName = String.valueOf(hours % 24);
            String monthName = mMonths[month % mMonths.length];
            String yearName = String.valueOf(year);

            if (chart.getVisibleXRange() > 30*5*pointsPerHour ) {

                return monthName + " " + yearName;
            } else if (chart.getVisibleXRange() > 30*pointsPerHour ) {

                int dayOfMonth = determineDayOfMonth(days, month + 12 * (year - 2016));

                String appendix = "th";

                switch (dayOfMonth) {
                    case 1:
                        appendix = "st";
                        break;
                    case 2:
                        appendix = "nd";
                        break;
                    case 3:
                        appendix = "rd";
                        break;
                    case 21:
                        appendix = "st";
                        break;
                    case 22:
                        appendix = "nd";
                        break;
                    case 23:
                        appendix = "rd";
                        break;
                    case 31:
                        appendix = "st";
                        break;
                }

                return dayOfMonth == 0 ? "" : dayOfMonth + appendix + " " + monthName;
            } else {
                return hourName + ":" + minuteName;
            }
        }

        private int determineMinutes(int sec) {
            int minutes = (int) (sec/60) ;
            return minutes;
        }

        private int getDaysForMonth(int month, int year) {

            // month is 0-based

            if (month == 1) {
                boolean is29Feb = false;

                if (year < 1582)
                    is29Feb = (year < 1 ? year + 1 : year) % 4 == 0;
                else if (year > 1582)
                    is29Feb = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);

                return is29Feb ? 29 : 28;
            }

            if (month == 3 || month == 5 || month == 8 || month == 10)
                return 30;
            else
                return 31;
        }

        private int determineHours(int sec) {
                int hours = (int) (sec/3600);      ///3600
            return hours;
        }

        private int determineDays(int hours) {
            int days = (int) (hours/(24))+1;
            return days;
        }

        private int determineMonth(int dayOfYear) {

            int month = -1;
            int days = 0;

            while (days < dayOfYear) {
                month = month + 1;

                if (month >= 12)
                    month = 0;

                int year = determineYear(days);
                days += getDaysForMonth(month, year);
            }

            return Math.max(month, 0);
        }

        private int determineDayOfMonth(int days, int month) {

            int count = 0;
            int daysForMonths = 0;

            while (count < month) {

                int year = determineYear(daysForMonths);
                daysForMonths += getDaysForMonth(count % 12, year);
                count++;
            }

            return days - daysForMonths;
        }

        private int determineYear(int days) {

            if (days <= 366)
                return 2016;
            else if (days <= 730)
                return 2017;
            else if (days <= 1094)
                return 2018;
            else if (days <= 1458)
                return 2019;
            else
                return 2020;

        }



}