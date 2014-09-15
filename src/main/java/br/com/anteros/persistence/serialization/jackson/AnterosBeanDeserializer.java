package br.com.anteros.persistence.serialization.jackson;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.metadata.annotation.Fetch;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class AnterosBeanDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer {

	protected BeanDeserializer delegate;

	public AnterosBeanDeserializer(BeanDeserializer deserializer, Class<?> clazz) {
		super(clazz);
		this.delegate = deserializer;
	}

	protected AnterosBeanDeserializer(Class<?> vc) {
		super(vc);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		Object result = delegate.deserialize(jp, ctxt);
		if (result != null) {
			Class<?> rawClass = result.getClass();
			if (rawClass.isAnnotationPresent(Entity.class)) {
				Field[] allDeclaredFields = ReflectionUtils.getAllDeclaredFields(rawClass);
				for (Field field : allDeclaredFields) {
					if (field.isAnnotationPresent(Fetch.class)) {
						Fetch annotation = field.getAnnotation(Fetch.class);
						if ((annotation.mappedBy() != null) && (annotation.mappedBy() != "")) {
							if ((ReflectionUtils.isImplementsInterface(field.getType(), Collection.class) || ReflectionUtils
									.isImplementsInterface(field.getType(), Set.class))) {
								Class<?> fieldType = ReflectionUtils.getGenericType(field);
								Object objectValue;
								try {
									objectValue = field.get(result);

									for (Object value : ((Collection<?>) objectValue)) {
										Field mappedByField = ReflectionUtils.getFieldByName(fieldType,
												annotation.mappedBy());
										if (mappedByField != null) {
											mappedByField.set(value, result);
										}
									}
								} catch (Exception e) {
									throw new JacksonSerializationException(e);
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt, Object intoValue) throws IOException,
			JsonProcessingException {
		return super.deserialize(jp, ctxt, intoValue);
	}

	public void resolve(DeserializationContext ctxt) throws JsonMappingException {
		delegate.resolve(ctxt);
	}

}
