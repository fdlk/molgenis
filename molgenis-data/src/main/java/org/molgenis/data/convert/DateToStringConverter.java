package org.molgenis.data.convert;

import org.molgenis.util.MolgenisDateFormat;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateToStringConverter implements Converter<Date, String>
{

	@Override
	public String convert(Date source)
	{
		Instant instant = source.toInstant();
		LocalDate localDate = instant.atZone(ZoneId.of("UTC")).toLocalDate();
		if (localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().equals(instant))
		{
			// this is a LocalDate use the new DateFormatter and format to iso localdate format
			return localDate.format(MolgenisDateFormat.getDateFormatter());
		}

		return MolgenisDateFormat.getDateTimeFormat().format(source);
	}

}
