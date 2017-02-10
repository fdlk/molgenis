package org.molgenis.graphql.model;

import com.google.gson.annotations.SerializedName;

/**
 * Gets or Sets fieldType
 */
public enum FieldType
{
  @SerializedName("BOOL")
  BOOL("BOOL"),

  @SerializedName("CATEGORICAL")
  CATEGORICAL("CATEGORICAL"),

  @SerializedName("CATEGORICAL_MREF")
  CATEGORICAL_MREF("CATEGORICAL_MREF"),

  @SerializedName("COMPOUND")
  COMPOUND("COMPOUND"),

  @SerializedName("DATE")
  DATE("DATE"),

  @SerializedName("DATE_TIME")
  DATE_TIME("DATE_TIME"),

  @SerializedName("DECIMAL")
  DECIMAL("DECIMAL"),

  @SerializedName("EMAIL")
  EMAIL("EMAIL"),

  @SerializedName("ENUM")
  ENUM("ENUM"),

  @SerializedName("FILE")
  FILE("FILE"),

  @SerializedName("HTML")
  HTML("HTML"),

  @SerializedName("HYPERLINK")
  HYPERLINK("HYPERLINK"),

  @SerializedName("INT")
  INT("INT"),

  @SerializedName("LONG")
  LONG("LONG"),

  @SerializedName("MREF")
  MREF("MREF"),

  @SerializedName("ONE_TO_MANY")
  ONE_TO_MANY("ONE_TO_MANY"),

  @SerializedName("SCRIPT")
  SCRIPT("SCRIPT"),

  @SerializedName("STRING")
  STRING("STRING"),

  @SerializedName("TEXT")
  TEXT("TEXT"),

  @SerializedName("XREF")
  XREF("XREF");

  private String value;

  FieldType(String value) {
	this.value = value;
  }

  @Override
  public String toString() {
	return String.valueOf(value);
  }
}
