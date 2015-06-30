package org.molgenis.js;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
public class MagmaScriptEvaluator
{
	// Sealed shared root scope.
	// Only initialize the fixed things once
	// See http://lxr.mozilla.org/mozilla/source/js/rhino/examples/DynamicScopes.java
	private Scriptable sharedRootScope;
	private Function bindEntity;

	public MagmaScriptEvaluator() throws UnsupportedEncodingException, IOException
	{
		String script = FileCopyUtils.copyToString(new InputStreamReader(ScriptEvaluator.class
				.getResourceAsStream("/js/$.js"), "UTF-8"));
		try
		{
			Context cx = Context.enter();
			sharedRootScope = cx.initStandardObjects(null, true);
			Script dollarScript = cx.compileString(script, "$.js", 1, null);
			dollarScript.exec(cx, sharedRootScope);
			bindEntity = cx.compileFunction(sharedRootScope, "function bindEntity() { $ = $.bind(this); }",
					"bindEntity", 1, null);
			cx.seal(sharedRootScope);
		}
		finally
		{
			Context.exit();
		}
	}

	public Object eval(String source, Entity entity, EntityMetaData entityMetaData)
	{
		EvaluationResult result = Iterables.get(eval(source, Collections.singleton(entity), entityMetaData), 0);
		if (result.isSuccess())
		{
			return result.getValue();
		}
		throw result.getException();
	}

	public List<EvaluationResult> eval(final String source, final Iterable<Entity> entities,
			final EntityMetaData entityMetaData)
	{
		List<EvaluationResult> result = Lists.newArrayList();
		try
		{
			Context cx = Context.enter();
			Scriptable scope = cx.newObject(sharedRootScope);
			scope.setPrototype(sharedRootScope);
			scope.setParentScope(null);
			Script compiledScript = cx.compileString(source, "mappingScript", 1, null);

			for (Entity entity : entities)
			{
				try
				{
					Scriptable jsEntity = mapEntity(entity, entityMetaData, cx, scope);
					bindEntity.call(cx, scope, jsEntity, null);
					result.add(EvaluationResult.createSuccess(compiledScript.exec(cx, scope), entity));
				}
				catch (RuntimeException ex)
				{
					result.add(EvaluationResult.createFailure(ex, entity));
				}
			}
		}
		finally
		{
			Context.exit();
		}
		return result;
	}

	private Scriptable mapEntity(final Entity entity, final EntityMetaData entityMetaData, Context cx, Scriptable scope)
	{
		Scriptable scriptableEntity = cx.newObject(scope);
		scriptableEntity.setPrototype(scope);
		entityMetaData.getAtomicAttributes().forEach(attr -> {
			Object value = entity.get(attr.getName());
			if (value != null)
			{
				Object jsValue;
				if (value instanceof Date)
				{
					Long[] args = new Long[]
					{ ((Date) value).getTime() };
					jsValue = cx.newObject(scope, "Date", args);
				}
				else
				{
					jsValue = Context.javaToJS(value, scope);
				}
				scriptableEntity.put(attr.getName(), scriptableEntity, jsValue);
			}
		});
		return scriptableEntity;
	}
}
