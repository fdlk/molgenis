package org.molgenis.graphql.schema;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.molgenis.util.MolgenisDateFormat;

import java.text.ParseException;

/**
 * Scalar types for Date and DateTime.
 * TODO: How come we only need to parse, who does the formatting?
 */
public class MolgenisGraphQLScalars
{
	public static GraphQLScalarType GraphQLDate = new GraphQLScalarType("Date", "EMX Date type", new Coercing()
	{
		/**
		 * Called to convert a result of a DataFetcher to a valid runtime value.
		 *
		 * @param input is never null
		 * @return null if not possible/invalid
		 */
		@Override
		public Object serialize(Object input)
		{
			return input;
		}

		/**
		 * Called to resolve a input from a variable.
		 * Null if not possible.
		 *
		 * @param input is never null
		 * @return null if not possible/invalid
		 */
		@Override
		public Object parseValue(Object input)
		{
			try
			{
				return MolgenisDateFormat.getDateFormat().parse(input.toString());
			}
			catch (ParseException ex)
			{
				return null;
			}
		}

		/**
		 * Called to convert an AST node
		 *
		 * @param input is never null
		 * @return null if not possible/invalid
		 */
		@Override
		public Object parseLiteral(Object input)
		{
			if (input instanceof StringValue)
			{
				StringValue sv = (StringValue) input;
				try
				{
					return MolgenisDateFormat.getDateFormat().parse(sv.getValue());
				}
				catch (ParseException ex)
				{
					return null;
				}
			}
			return null;
		}
	});

	public static GraphQLScalarType GraphQLDateTime = new GraphQLScalarType("DateTime", "EMX DateTime type",
			new Coercing()
			{
				/**
				 * Called to convert a result of a DataFetcher to a valid runtime value.
				 *
				 * @param input is never null
				 * @return null if not possible/invalid
				 */
				@Override
				public Object serialize(Object input)
				{
					return input;
				}

				/**
				 * Called to resolve a input from a variable.
				 * Null if not possible.
				 *
				 * @param input is never null
				 * @return null if not possible/invalid
				 */
				@Override
				public Object parseValue(Object input)
				{
					try
					{
						return MolgenisDateFormat.getDateTimeFormat().parse(input.toString());
					}
					catch (ParseException ex)
					{
						return null;
					}
				}

				/**
				 * Called to convert an AST node
				 *
				 * @param input is never null
				 * @return null if not possible/invalid
				 */
				@Override
				public Object parseLiteral(Object input)
				{
					if (input instanceof StringValue)
					{
						StringValue sv = (StringValue) input;
						try
						{
							return MolgenisDateFormat.getDateTimeFormat().parse(sv.getValue());
						}
						catch (ParseException ex)
						{
							return null;
						}
					}
					return null;
				}
			});
}
