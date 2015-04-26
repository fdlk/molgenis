package org.molgenis.ui.jobs;

/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.util.Assert;
import org.springframework.web.socket.sockjs.frame.AbstractSockJsMessageCodec;

import com.google.gson.Gson;

/**
 * A Gson codec for encoding and decoding SockJS messages.
 *
 * @author Fleur Kelpin
 */
public class GsonSockJsMessageCodec extends AbstractSockJsMessageCodec
{

	private final Gson objectMapper;

	public GsonSockJsMessageCodec()
	{
		this.objectMapper = new Gson();
	}

	public GsonSockJsMessageCodec(Gson objectMapper)
	{
		Assert.notNull(objectMapper, "objectMapper must not be null");
		this.objectMapper = objectMapper;
	}

	@Override
	public String[] decode(String content) throws IOException
	{
		return objectMapper.fromJson(content, String[].class);
	}

	@Override
	public String[] decodeInputStream(InputStream content) throws IOException
	{
		return this.objectMapper.fromJson(new InputStreamReader(content, "UTF-8"), String[].class);
	}

	@Override
	protected char[] applyJsonQuoting(String content)
	{
		String json = objectMapper.toJson(content);
		json = json.substring(1, json.length() - 1);
		return json.toCharArray();
	}
}
