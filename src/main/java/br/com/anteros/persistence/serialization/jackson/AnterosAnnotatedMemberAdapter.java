package br.com.anteros.persistence.serialization.jackson;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCache;
import br.com.anteros.persistence.metadata.descriptor.DescriptionField;
import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;

public class AnterosAnnotatedMemberAdapter extends AnnotatedMember {

	private static final long serialVersionUID = 1L;
	private AnnotatedMember annotatedMember;
	private SQLSessionFactory sessionFactory;

	public AnterosAnnotatedMemberAdapter(SQLSessionFactory sessionFactory, AnnotatedMember annotated) {
		super(null);
		this.annotatedMember = annotated;
		this.sessionFactory = sessionFactory;
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> acls) {
		if (acls.equals(JsonManagedReference.class)) {
			if (annotatedMember instanceof AnnotatedMember) {
				Member member = ((AnnotatedMember) annotatedMember).getMember();
				Field field = null;
				if (member instanceof Method) {
					field = ReflectionUtils.getFieldByMethodSetter((Method) member);
				} else if (member instanceof Field) {
					field = (Field) member;
				}
				if (field != null) {
					EntityCache entityCache = sessionFactory.getEntityCacheManager().getEntityCache(
							member.getDeclaringClass());
					if (entityCache != null) {
						DescriptionField descriptionField = entityCache.getDescriptionField(field.getName());
						if (descriptionField.isMappedBy()) {
							return (A) new JsonManagedReferenceImpl(descriptionField.getMappedBy());
						}
					}
				}
			}
			return annotatedMember.getAnnotation(acls);

		} else if (acls.equals(JsonBackReference.class)) {
			if (annotatedMember instanceof AnnotatedMember) {
				Member member = ((AnnotatedMember) annotatedMember).getMember();
				Field field = null;
				if (member instanceof Method) {
					field = ReflectionUtils.getFieldByMethodSetter((Method) member);
				} else if (member instanceof Field) {
					field = (Field) member;
				}
				if (field != null) {
					EntityCache entityCache = sessionFactory.getEntityCacheManager().getEntityCache(
							member.getDeclaringClass());
					if (entityCache != null) {
						DescriptionField descriptionField = entityCache.getDescriptionField(field.getName());
						if (descriptionField.isRelationShip()) {
							EntityCache entityCacheFK = sessionFactory.getEntityCacheManager().getEntityCache(
									descriptionField.getFieldClass());
							if (entityCacheFK.hasDescriptionFieldWithMappedBy(entityCache.getEntityClass(), descriptionField.getField().getName())) {
								return (A) new JsonBackReferenceImpl(descriptionField.getField().getName());
							}
						}
					}
				}
			}
			return annotatedMember.getAnnotation(acls);
		}
		return annotatedMember.getAnnotation(acls);
	}

	@Override
	public Annotated withAnnotations(AnnotationMap fallback) {
		return annotatedMember.withAnnotations(fallback);
	}

	@Override
	public AnnotatedElement getAnnotated() {
		return annotatedMember.getAnnotated();
	}

	@Override
	protected int getModifiers() {
		return 0;
	}

	@Override
	public String getName() {
		return annotatedMember.getName();
	}

	@Override
	public Type getGenericType() {
		return annotatedMember.getGenericType();
	}

	@Override
	public Class<?> getRawType() {
		return annotatedMember.getRawType();
	}

	@Override
	public Iterable<Annotation> annotations() {
		return annotatedMember.annotations();
	}

	@Override
	protected AnnotationMap getAllAnnotations() {
		throw new RuntimeException("Not implemented method.");
	}

	@Override
	public Class<?> getDeclaringClass() {
		return annotatedMember.getDeclaringClass();
	}

	@Override
	public Member getMember() {
		return annotatedMember.getMember();
	}

	@Override
	public void setValue(Object pojo, Object value) throws UnsupportedOperationException, IllegalArgumentException {
		annotatedMember.setValue(pojo, value);

	}

	@Override
	public Object getValue(Object pojo) throws UnsupportedOperationException, IllegalArgumentException {
		return annotatedMember.getValue(pojo);
	}

	class JsonManagedReferenceImpl implements JsonManagedReference {

		private String _value;

		public JsonManagedReferenceImpl(String value) {
			this._value = value;
		}

		public Class<? extends Annotation> annotationType() {
			return JsonManagedReference.class;
		}

		public String value() {
			return _value;
		}

	}

	class JsonBackReferenceImpl implements JsonBackReference {

		private String _value;

		public JsonBackReferenceImpl(String value) {
			this._value = value;
		}

		public Class<? extends Annotation> annotationType() {
			return JsonBackReference.class;
		}

		public String value() {
			return _value;
		}

	}
}