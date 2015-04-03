package com.liudonghua.apps.movie_recommendation_demo.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class Utils {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy");

	public static Date parseDate(String dateString) {
		try {
		    LocalDate localDate = LocalDate.parse(dateString, formatter);
		    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
		catch (DateTimeParseException ex) {
			// fix "1373|Good Morning (1971)|4-Feb-1971||http://us.imdb.com/M/title-exact?Good%20Morning%20(1971)|1|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0"
		    ex.printStackTrace();
		}
		return null;
	}


	public static String stringDate(Date date) {
		try {
			 return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(formatter);
		}
		catch (DateTimeParseException ex) {
		    ex.printStackTrace();
		}
		return null;
	}


	public static double[] generateGenreProbabilities(long size) {
		if(size == 1) {
			return new double[] {1.0};
		}
		else if(size == 2) {
			return new double[] {0.6, 0.4};
		}
		else if(size == 3) {
			return new double[] {0.5, 0.3, 0.2};
		}
		else if(size == 4) {
			return new double[] {0.4, 0.3, 0.2, 0.1};
		}
		double[] genreProbabilities = new double[(int) size];
		for(int i = 0; i < size; i++) {
			genreProbabilities[i] = 0.1;
		}
		return genreProbabilities;
	}
	
}
