package org.molgenis.integrationtest.platform;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.data.EntityTestHarness;
import org.openjdk.jmh.annotations.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.molgenis.test.data.EntityTestHarness.ATTR_DECIMAL;

/**
 * Created by fkelpin on 30/08/16.
 */
public class BenchmarkQueries
{
	@State(value = Scope.Benchmark)
	public static class BenchmarkState
	{
		public static DataService dataService;
		public static Authentication sessionToken;
	}

	@State(value = Scope.Thread)
	public static class UserSession
	{
		@Setup(value = Level.Trial)
		public void setup()
		{
			System.out.println("setup!");
			SecurityContextHolder.getContext().setAuthentication(BenchmarkState.sessionToken);
		}
	}

	@Benchmark
	public void executeQueries(UserSession userSession)
	{
		Query q1 = new QueryImpl<>().eq(EntityTestHarness.ATTR_STRING, "string1");
		q1.pageSize(1000);

		Query q2 = new QueryImpl<>().eq(EntityTestHarness.ATTR_BOOL, true);
		q2.pageSize(500);

		Query q3 = new QueryImpl<>().eq(ATTR_DECIMAL, 1.123);

		//		runAsSystem(() ->
		//		{
		BenchmarkState.dataService.findAll("sys_test_TypeTestDynamic", q1);
		BenchmarkState.dataService.findAll("sys_test_TypeTestDynamic", q2);
		BenchmarkState.dataService.findOne("sys_test_TypeTestDynamic", q3);
		//		});
	}
}
