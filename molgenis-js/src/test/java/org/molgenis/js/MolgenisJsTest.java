package org.molgenis.js;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

public class MolgenisJsTest
{
	private MagmaScriptEvaluator magmaScriptEvaluator;

	@BeforeMethod
	public void beforeMethod() throws UnsupportedEncodingException, IOException
	{
		new RhinoConfig().init();
		magmaScriptEvaluator = new MagmaScriptEvaluator();
	}

	@Test
	public void test$()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);

		Object weight = magmaScriptEvaluator.eval("$('weight').value()", person, emd);
		assertEquals(weight, 82);
	}

	@Test
	public void testUnitConversion()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);

		Object weight = magmaScriptEvaluator.eval("$('weight').unit('kg').toUnit('pound').value()", person, emd);
		assertEquals(weight, 180.7790549915996);
	}

	@Test
	public void mapSimple()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("gender").setDataType(MolgenisFieldTypes.CATEGORICAL);

		Object result = magmaScriptEvaluator.eval("$('gender').map({'20':'2','B':'B2'}).value()", new MapEntity(
				"gender", 'B'), emd);
		assertEquals(result.toString(), "B2");
	}

	@Test
	public void mapDefault()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("gender").setDataType(MolgenisFieldTypes.CATEGORICAL);

		Object result = magmaScriptEvaluator.eval("$('gender').map({'20':'2'}, 'B2').value()", new MapEntity("gender",
				'B'), emd);
		assertEquals(result.toString(), "B2");
	}

	@Test
	public void mapNull()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("gender").setDataType(MolgenisFieldTypes.CATEGORICAL);

		Object result = magmaScriptEvaluator.eval("$('gender').map({'20':'2'}, 'B2', 'B3').value()", new MapEntity(),
				emd);
		assertEquals(result.toString(), "B3");
	}

	@Test
	public void div()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Object result = magmaScriptEvaluator.eval("$('height').div(100).value()", new MapEntity("height", 200), emd);
		assertEquals(result, 2d);
	}

	@Test
	public void pow()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Object result = magmaScriptEvaluator.eval("$('height').pow(2).value()", new MapEntity("height", 20), emd);
		assertEquals(result, 400d);
	}

	@Test
	public void testBmi()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);
		person.set("height", 189);

		Object bmi = magmaScriptEvaluator.eval("$('weight').div($('height').div(100).pow(2)).value()", person, emd);
		DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.ENGLISH));
		assertEquals(df.format(bmi), df.format(82.0 / (1.89 * 1.89)));
	}

	@Test
	public void testGlucose()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("glucose");
		emd.addAttribute("GLUC_1").setDataType(MolgenisFieldTypes.INT);

		Entity glucose = new MapEntity();
		glucose.set("GLUC_1", 4.1);

		Object bmi = magmaScriptEvaluator.eval("$('GLUC_1').div(100).value()", glucose, emd);
		DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.ENGLISH));
		assertEquals(df.format(bmi), df.format(4.1 / 100));
	}

	@Test
	public void age() throws ParseException
	{
		Date dob = new SimpleDateFormat("dd-MM-yyyy").parse("03-08-1834");

		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("birthdate").setDataType(MolgenisFieldTypes.DATE);

		Object result = magmaScriptEvaluator.eval("$('birthdate').age().value()", new MapEntity("birthdate", dob), emd);
		assertEquals(result, Math.floor((new Date().getTime() - dob.getTime()) / (365.25 * 24 * 3600 * 1000)));
	}

	@Test
	public void testNull()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("birthdate").setDataType(MolgenisFieldTypes.DATE);

		String script = "$('birthdate').age().value() < 18  || $('birthdate').value() != null";

		Object result = magmaScriptEvaluator.eval(script, new MapEntity("birthdate", new Date()), emd);
		assertEquals(result, true);

		result = magmaScriptEvaluator.eval(script, new MapEntity("birthdate", null), emd);
		assertEquals(result, false);
	}

	@Test
	public void testEq()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').eq(100).value()";

		Object result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, true);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, false);
	}

	@Test
	public void testIsNull()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').isNull().value()";

		Object result = magmaScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, true);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, false);
	}

	@Test
	public void testNot()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').isNull().not().value()";

		Object result = magmaScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, true);
	}

	@Test
	public void testOr()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').eq(99).or($('weight').eq(100)).value()";

		Object result = magmaScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, true);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, true);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, true);
	}

	@Test
	public void testGt()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').gt(100).value()";

		Object result = magmaScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, false);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, false);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 101), emd);
		assertEquals(result, true);
	}

	@Test
	public void testLt()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').lt(100).value()";

		Object result = magmaScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, true);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, false);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 101), emd);
		assertEquals(result, false);
	}

	@Test
	public void testGe()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').ge(100).value()";

		Object result = magmaScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, false);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, true);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 101), emd);
		assertEquals(result, true);
	}

	@Test
	public void testLe()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		String script = "$('weight').le(100).value()";

		Object result = magmaScriptEvaluator.eval(script, new MapEntity("weight", null), emd);
		assertEquals(result, false);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 99), emd);
		assertEquals(result, true);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 100), emd);
		assertEquals(result, true);

		result = magmaScriptEvaluator.eval(script, new MapEntity("weight", 101), emd);
		assertEquals(result, false);
	}

	@Test(enabled = false)
	public void testBatchPerformance()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);
		person.set("height", 189);

		Stopwatch sw = Stopwatch.createStarted();

		Iterable<EvaluationResult> bmi = magmaScriptEvaluator.eval(
				"$('weight').div($('height').div(100).pow(2)).value()", FluentIterable.from(Iterables.cycle(person))
						.limit(1000), emd);
		sw.stop();
		EvaluationResult expected = EvaluationResult.createSuccess((82 / Math.pow(189 / 100.0, 2)), person);
		System.out.println(sw.toString());
		assertEquals(bmi, Collections.nCopies(1000, expected));
	}

	@Test
	public void testBatchErrors()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);
		person.set("height", 189);

		List<EvaluationResult> bmis = magmaScriptEvaluator.eval("$('weight').div($('height').div(100).pow(2)).value()",
				Arrays.asList(person, null, person), emd);
		assertEquals(bmis.get(0).getValue(), 82.0 / (1.89 * 1.89));
		assertFalse(bmis.get(1).isSuccess());
		assertEquals(bmis.get(1).getException().getClass(), NullPointerException.class);
		assertEquals(bmis.get(2).getValue(), 82.0 / (1.89 * 1.89));
	}

	@Test
	public void testBatchSyntaxError()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("person");
		emd.addAttribute("weight").setDataType(MolgenisFieldTypes.INT);
		emd.addAttribute("height").setDataType(MolgenisFieldTypes.INT);

		Entity person = new MapEntity();
		person.set("weight", 82);
		person.set("height", 189);

		try
		{
			magmaScriptEvaluator.eval("$('weight'))", Arrays.asList(person, person), emd);
			Assert.fail("Syntax errors should throw exception");
		}
		catch (Exception expected)
		{
			assertEquals(expected.getMessage(), "missing ; before statement (mappingScript#1)");
		}
	}

}
