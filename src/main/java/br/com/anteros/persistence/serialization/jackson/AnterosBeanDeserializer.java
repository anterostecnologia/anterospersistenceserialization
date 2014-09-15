package br.com.anteros.persistence.serialization.jackson;

import java.io.IOException;
import java.util.Collection;

import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class AnterosBeanDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer {

	protected BeanDeserializer delegate;
	private SQLSessionFactory sessionFactory;

	public AnterosBeanDeserializer(SQLSessionFactory sessionFactory, BeanDeserializer deserializer, Class<?> clazz) {
		super(clazz);
		this.delegate = deserializer;
		this.sessionFactory = sessionFactory;
	}

	protected AnterosBeanDeserializer(Class<?> vc) {
		super(vc);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		Object result = delegate.deserialize(jp, ctxt);
		if (result != null) {
			EntityCache entityCache = sessionFactory.getEntityCacheManager().getEntityCache(result.getClass());
			if (entityCache != null) {
				for (DescriptionField descriptionField : entityCache.getDescriptionFields()) {
					if (descriptionField.isCollectionEntity()){
						if (descriptionField.isMappedBy()){
							try {
								Object objectValue = descriptionField.getObjectValue(result);
								if (objectValue instanceof Collection){
									for (Object value : ((Collection<?>)objectValue)){
										EntityCache entityCacheValue = sessionFactory.getEntityCacheManager().getEntityCache(value.getClass());
										if (entityCacheValue !=null) {
											DescriptionField descriptionFieldMappedBy = entityCacheValue.getDescriptionField(descriptionField.getMappedBy());
											descriptionFieldMappedBy.setObjectValue(value, result);
										}
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
