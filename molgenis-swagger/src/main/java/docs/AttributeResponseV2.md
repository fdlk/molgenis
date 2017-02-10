
# AttributeResponseV2

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**href** | **String** |  | 
**fieldType** | [**FieldTypeEnum**](#FieldTypeEnum) |  | 
**name** | **String** |  | 
**label** | **String** |  | 
**description** | **String** |  |  [optional]
**attributes** | [**List&lt;AttributeResponseV2&gt;**](AttributeResponseV2.md) |  |  [optional]
**enumOptions** | **List&lt;String&gt;** |  |  [optional]
**maxLength** | **Long** |  |  [optional]
**refEntity** | [**EntityTypeResponseV2**](EntityTypeResponseV2.md) |  |  [optional]
**mappedBy** | **String** |  |  [optional]
**auto** | **Boolean** |  | 
**nillable** | **Boolean** |  | 
**readOnly** | **Boolean** |  | 
**defaultValue** | **String** |  |  [optional]
**labelAttribute** | **Boolean** |  | 
**unique** | **Boolean** |  | 
**visible** | **Boolean** |  | 
**lookupAttribute** | **Boolean** |  | 
**isAggregatable** | **Boolean** |  | 
**range** | [**Range**](Range.md) |  |  [optional]
**expression** | **String** |  |  [optional]
**visibleExpression** | **String** |  |  [optional]
**validationExpression** | **String** |  |  [optional]


<a name="FieldTypeEnum"></a>
## Enum: FieldTypeEnum
Name | Value
---- | -----
BOOL | &quot;BOOL&quot;
CATEGORICAL | &quot;CATEGORICAL&quot;
CATEGORICAL_MREF | &quot;CATEGORICAL_MREF&quot;
COMPOUND | &quot;COMPOUND&quot;
DATE | &quot;DATE&quot;
DATE_TIME | &quot;DATE_TIME&quot;
DECIMAL | &quot;DECIMAL&quot;
EMAIL | &quot;EMAIL&quot;
ENUM | &quot;ENUM&quot;
FILE | &quot;FILE&quot;
HTML | &quot;HTML&quot;
HYPERLINK | &quot;HYPERLINK&quot;
INT | &quot;INT&quot;
LONG | &quot;LONG&quot;
MREF | &quot;MREF&quot;
ONE_TO_MANY | &quot;ONE_TO_MANY&quot;
SCRIPT | &quot;SCRIPT&quot;
STRING | &quot;STRING&quot;
TEXT | &quot;TEXT&quot;
XREF | &quot;XREF&quot;



