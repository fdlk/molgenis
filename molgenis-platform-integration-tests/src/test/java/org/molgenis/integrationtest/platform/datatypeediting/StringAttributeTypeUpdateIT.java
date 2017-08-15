package org.molgenis.integrationtest.platform.datatypeediting;

import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.HTML;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.text.ParseException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {PlatformITConfig.class})
public class StringAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT {
  @BeforeClass
  public void setUp() {
    super.setup(STRING, STRING);
  }

  @AfterMethod
  public void afterMethod() {
    super.afterMethod(STRING);
  }

  @AfterClass
  public void afterClass() {
    super.afterClass();
  }

  @DataProvider(name = "validConversionData")
  public Object[][] validConversionData() {
    return new Object[][] {
      {"true", BOOL, true},
      {"1", INT, 1},
      {"4243298", LONG, 4243298L},
      {"1.234", DECIMAL, 1.234},
      {"1", XREF, "label1"},
      {"1", CATEGORICAL, "label1"},
      {
        "A VERY LONG TEXT!!!!@#$#{@}{@}{#%$#*($&@#",
        TEXT,
        "A VERY LONG TEXT!!!!@#$#{@}{@}{#%$#*($&@#"
      },
      {"1", ENUM, "1"},
      {"<h1>Hello World</h1>", HTML, "<h1>Hello World</h1>"},
      {"Compounds go!", COMPOUND, null},
      {"1990-11-13", DATE, "1990-11-13"},
      {"2016-11-13T20:20:20+0100", DATE_TIME, "2016-11-13T20:20:20+0100"},
      {"2016-11-13T20:20:20+0500", DATE_TIME, "2016-11-13T20:20:20+0500"}
    };
  }

  /**
   * Valid conversion cases for STRING to: INT, TEXT, BOOL, DECIMAL, LONG, XREF, CATEGORICAL,
   * COMPOUND, ENUM, HTML, DATE, DATE_TIME
   *
   * @param valueToConvert The value that will be converted
   * @param typeToConvertTo The type to convert to
   * @param convertedValue The expected value after converting the type
   */
  @Test(dataProvider = "validConversionData")
  public void testValidConversion(
      String valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
      throws ParseException {
    testTypeConversion(valueToConvert, typeToConvertTo);

    if (typeToConvertTo.equals(DATE)) convertedValue = parseLocalDate(convertedValue.toString());
    if (typeToConvertTo.equals(DATE_TIME)) convertedValue = parseInstant(convertedValue.toString());

    // Assert if conversion was successful
    assertEquals(getActualDataType(), typeToConvertTo);
    assertEquals(getActualValue(), convertedValue);
  }

  @DataProvider(name = "invalidConversionTestCases")
  public Object[][] invalidConversionTestCases() {
    return new Object[][] {
      {
        "not true",
        BOOL,
        MolgenisValidationException.class,
        "Value [not true] of this entity attribute is not of type [BOOL]."
      },
      {
        "1b",
        INT,
        MolgenisValidationException.class,
        "Value [1b] of this entity attribute is not of type [INT or LONG]."
      },
      {
        "1234567890b",
        LONG,
        MolgenisValidationException.class,
        "Value [1234567890b] of this entity attribute is not of type [INT or LONG]."
      },
      {
        "1.123b",
        DECIMAL,
        MolgenisValidationException.class,
        "Value [1.123b] of this entity attribute is not of type [DECIMAL]."
      },
      {
        "ref123",
        XREF,
        MolgenisValidationException.class,
        "Unknown xref value 'ref123' for attribute 'mainAttribute' of entity 'MAINENTITY'."
      },
      {
        "ref123",
        CATEGORICAL,
        MolgenisValidationException.class,
        "Unknown xref value 'ref123' for attribute 'mainAttribute' of entity 'MAINENTITY'."
      },
      {
        "Test@Test.Test",
        EMAIL,
        MolgenisValidationException.class,
        "Attribute data type update from [STRING] to [EMAIL] not allowed, allowed types are [BOOL, CATEGORICAL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, TEXT, XREF]"
      },
      {
        "https://www.google.com",
        HYPERLINK,
        MolgenisValidationException.class,
        "Attribute data type update from [STRING] to [HYPERLINK] not allowed, allowed types are [BOOL, CATEGORICAL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, TEXT, XREF]"
      },
      {
        "enumOption100",
        ENUM,
        MolgenisValidationException.class,
        "Unknown enum value for attribute 'mainAttribute' of entity 'MAINENTITY'."
      },
      {
        "Not a date",
        DATE,
        MolgenisValidationException.class,
        "Value [Not a date] of this entity attribute is not of type [DATE]."
      },
      {
        "Not a date time",
        DATE_TIME,
        MolgenisValidationException.class,
        "Value [Not a date time] of this entity attribute is not of type [DATE_TIME]."
      },
      {
        "ref123",
        MREF,
        MolgenisValidationException.class,
        "Attribute data type update from [STRING] to [MREF] not allowed, allowed types are [BOOL, CATEGORICAL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, TEXT, XREF]"
      },
      {
        "ref123",
        CATEGORICAL_MREF,
        MolgenisValidationException.class,
        "Attribute data type update from [STRING] to [CATEGORICAL_MREF] not allowed, allowed types are [BOOL, CATEGORICAL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, TEXT, XREF]"
      },
      {
        "ref123",
        FILE,
        MolgenisValidationException.class,
        "Attribute data type update from [STRING] to [FILE] not allowed, allowed types are [BOOL, CATEGORICAL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, TEXT, XREF]"
      },
      {
        "ref123",
        ONE_TO_MANY,
        MolgenisValidationException.class,
        "Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()"
      }
    };
  }

  /**
   * Invalid conversion cases for STRING to: BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, EMAIL,
   * HYPERLINK, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE
   *
   * @param valueToConvert The value that will be converted
   * @param typeToConvertTo The type to convert to
   * @param exceptionClass The expected class of the exception that will be thrown
   * @param exceptionMessage The expected exception message
   */
  @Test(dataProvider = "invalidConversionTestCases")
  public void testInvalidConversion(
      String valueToConvert,
      AttributeType typeToConvertTo,
      Class exceptionClass,
      String exceptionMessage) {
    try {
      testTypeConversion(valueToConvert, typeToConvertTo);
      fail("Conversion should have failed");
    } catch (Exception exception) {
      assertTrue(exception.getClass().isAssignableFrom(exceptionClass));
      assertEquals(exception.getMessage(), exceptionMessage);
    }
  }
}
