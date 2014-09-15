package br.com.anteros.persistence.serialization.jackson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.annotation.ForeignKey;
import br.com.anteros.persistence.metadata.annotation.Transient;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

public class AnterosSerializerModifier extends BeanSerializerModifier {
	protected final int _features;
	private SQLSessionFactory sessionFactory;

	public AnterosSerializerModifier(int features, SQLSessionFactory sessionFactory) {
		_features = features;
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
			List<BeanPropertyWriter> beanProperties) {
		 List<BeanPropertyWriter> props = super.changeProperties(config, beanDesc, beanProperties);		

		/*
		 * Filtrando propriedades com @Transient
		 */
		List<String> transientOnes = new ArrayList<String>();

		for (BeanPropertyWriter p : props) {
			Field field = ReflectionUtils.getFieldByName(beanDesc.getBeanClass(), p.getName());
			if ((field != null) && (field.isAnnotationPresent(Transient.class))) {
				transientOnes.add(p.getName());
			}
		}
		
		for (Iterator<BeanPropertyWriter> iter = props.iterator(); iter.hasNext();) {
			String name = iter.next().getName();
			if (transientOnes.contains(name)) {
				iter.remove();
			}
		}
		
		/*
		 * Filtrando propriedades backReference
		 */
		List<String> mappedBy = new ArrayList<String>();
		
		for (BeanPropertyWriter p : props) {
			Field field = ReflectionUtils.getFieldByName(beanDesc.getBeanClass(), p.getName());
			if ((field != null) && (field.isAnnotationPresent(ForeignKey.class))) {
				EntityCache entityCache = sessionFactory.getEntityCacheManager().getEntityCache(p.getType().getRawClass());
				DescriptionField descriptionField = entityCache.getDescriptionFieldWithMappedBy(p.getName());
				if ((descriptionField!=null) && (descriptionField.getFieldClass() == beanDesc.getBeanClass())) {
					mappedBy.add(p.getName());
				}
			}
		}
		for (Iterator<BeanPropertyWriter> iter = props.iterator(); iter.hasNext();) {
			String name = iter.next().getName();
			if (mappedBy.contains(name)){
				iter.remove();
			}
		}

		
		return props;
	}

	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
			JsonSerializer<?> serializer) {
		return serializer;
	}

	@Override
	public JsonSerializer<?> modifyCollectionSerializer(SerializationConfig config, CollectionType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return serializer;
	}

	@Override
	public JsonSerializer<?> modifyMapSerializer(SerializationConfig config, MapType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return serializer;
	}
}
