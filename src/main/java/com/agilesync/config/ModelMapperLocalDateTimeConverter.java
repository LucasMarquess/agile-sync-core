package com.agilesync.config;

import org.modelmapper.AbstractConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ModelMapperLocalDateTimeConverter extends AbstractConverter<String, LocalDateTime> {

	@Override
	protected LocalDateTime convert( String source ) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss" );
		LocalDateTime localDateTime = LocalDateTime.parse( source, format );
		return localDateTime;
	}

}
