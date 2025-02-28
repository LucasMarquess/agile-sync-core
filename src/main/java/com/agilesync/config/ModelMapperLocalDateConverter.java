package com.agilesync.config;

import org.modelmapper.AbstractConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ModelMapperLocalDateConverter extends AbstractConverter<String, LocalDate> {

	@Override
	protected LocalDate convert( String source ) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );
		LocalDate localDate = LocalDate.parse( source, format );
		return localDate;
	}

}
