package org.molgenis.oneclickimporter.service;

import static org.molgenis.oneclickimporter.service.utils.OneClickImporterTestUtils.loadFile;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.exceptions.NoDataException;
import org.molgenis.oneclickimporter.service.Impl.ExcelServiceImpl;
import org.testng.annotations.Test;

public class ExcelServiceTest {
  private ExcelService excelService = new ExcelServiceImpl();

  @Test(
    expectedExceptions = EmptySheetException.class,
    expectedExceptionsMessageRegExp = "Sheet \\[empty_sheet\\] is empty"
  )
  public void buildExcelSheetsWithEmptyFile()
      throws InvalidFormatException, IOException, URISyntaxException, NoDataException,
          EmptySheetException {
    excelService.buildExcelSheetsFromFile(loadFile(ExcelServiceTest.class, "/empty-sheet.xlsx"));
  }

  @Test(
    expectedExceptions = NoDataException.class,
    expectedExceptionsMessageRegExp =
        "Header was found, but no data is present in sheet \\[Sheet1\\]"
  )
  public void buildExcelSheetsWithHeaderOnly()
      throws InvalidFormatException, IOException, URISyntaxException, NoDataException,
          EmptySheetException {
    excelService.buildExcelSheetsFromFile(
        loadFile(ExcelServiceTest.class, "/header-without-data.xlsx"));
  }
}
