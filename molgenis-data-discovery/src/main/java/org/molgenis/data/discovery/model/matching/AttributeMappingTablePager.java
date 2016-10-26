package org.molgenis.data.discovery.model.matching;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AttributeMappingTablePager.class)
public abstract class AttributeMappingTablePager
{
	public static final int DEFAULT_PAGE_SIZE = 50;

	public abstract int getTotal();

	public abstract int getPageSize();

	public abstract int getCurrentPage();

	public abstract int getTotalPage();

	public static AttributeMappingTablePager create(int total, int pageSize, int currentPage)
	{
		return new AutoValue_AttributeMappingTablePager(total, pageSize, currentPage, total / pageSize + 1);
	}

	public static AttributeMappingTablePager create(int total, int currentPage)
	{
		return new AutoValue_AttributeMappingTablePager(total, DEFAULT_PAGE_SIZE, currentPage,
				total / DEFAULT_PAGE_SIZE + 1);
	}
}
