/*
 * #%L
 * LDAP User Management
 * %%
 * Copyright (C) 2016 Kiril Arabadzhiyski
 * %%
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
 * #L%
 */
package com.a9ski.um.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonArrayProvider implements MessageBodyWriter<JSONArray>, MessageBodyReader<JSONArray> {

	private static final Logger logger = LoggerFactory.getLogger(JsonArrayProvider.class);

	@Context
	private UriInfo uriInfo;

	public long getSize(JSONArray t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return isJson(type);
	}

	private boolean isJson(Class<?> type) {
		return JSONArray.class.isAssignableFrom(type);
	}

	public void writeTo(JSONArray t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
		final String jsonString;
		try {
			jsonString = t.toString(2);
		} catch (JSONException ex) {
			logger.error("Error converting JSON to string", ex); 
			throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
		}

		String callbackParam = uriInfo.getQueryParameters().getFirst("callback");
		OutputStreamWriter writer = new OutputStreamWriter(entityStream, getCharset(mediaType));
		if (callbackParam != null) {
			writer.write(callbackParam);
			writer.write("("); //$NON-NLS-1$
		}
		writer.write(jsonString);
		if (callbackParam != null) {
			writer.write(")"); //$NON-NLS-1$
		}
		writer.flush();
	}

	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return isJson(type);
	}
	
	private Charset getCharset(MediaType m) {
		String name = null;
		if (m != null) {
			name = m.getParameters().get("charset");
		}
		if (name == null) {
			name = "UTF-8";
		}
		return Charset.forName(name);
	}

	public JSONArray readFrom(Class<JSONArray> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
		try {
			return new JSONArray(new JSONTokener(new InputStreamReader(entityStream, getCharset(mediaType))));
		} catch (JSONException ex) {
			logger.error("Error reading JSON", ex); 
			throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
		}
	}

}
