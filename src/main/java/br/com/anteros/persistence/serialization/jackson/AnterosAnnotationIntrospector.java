package br.com.anteros.persistence.serialization.jackson;


import br.com.anteros.persistence.metadata.annotation.Transient;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;


public class AnterosAnnotationIntrospector extends AnnotationIntrospector
{
    private static final long serialVersionUID = 1L;


    protected boolean _cfgCheckTransient;

    
    public AnterosAnnotationIntrospector() { }

    public AnterosAnnotationIntrospector setUseTransient(boolean state) {
        _cfgCheckTransient = state;
        return this;
    }

    @Override
    public Version version() {
        return ModuleVersion.instance.version();
    }


    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        return _cfgCheckTransient && m.hasAnnotation(Transient.class);
    }
}
