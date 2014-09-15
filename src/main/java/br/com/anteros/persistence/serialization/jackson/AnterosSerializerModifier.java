package br.com.anteros.persistence.serialization.jackson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.annotation.Fetch;
import br.com.anteros.persistence.metadata.annotation.ForeignKey;
import br.com.anteros.persistence.metadata.annotation.Transient;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

public class AnterosSerializerModifier extends BeanSerializerModifier {
	protected final int _features;

	public AnterosSerializerModifier(int features) {
		_features = features;
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
				Class<?> rawClass = p.getType().getRawClass();
				Field[] allDeclaredFields = ReflectionUtils.getAllDeclaredFields(rawClass);
				for (Field f : allDeclaredFields) {
					if (f.isAnnotationPresent(Fetch.class)) {
						Fetch annotation = f.getAnnotation(Fetch.class);
						if (p.getName().equals(annotation.mappedBy())) {
							if ((ReflectionUtils.isImplementsInterface(f.getType(), Collection.class) || ReflectionUtils
									.isImplementsInterface(f.getType(), Set.class))) {
								Class<?> fieldType = ReflectionUtils.getGenericType(f);
								if (fieldType == beanDesc.getBeanClass()) {
									mappedBy.add(p.getName());
								}
							}
						}
					}
				}
			}
		}
		for (Iterator<BeanPropertyWriter> iter = props.iterator(); iter.hasNext();) {
			String name = iter.next().getName();
			if (mappedBy.contains(name)) {
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
