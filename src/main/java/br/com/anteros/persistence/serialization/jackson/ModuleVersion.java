package br.com.anteros.persistence.serialization.jackson;

import com.fasterxml.jackson.core.util.VersionUtil;

class ModuleVersion extends VersionUtil
{
    public final static ModuleVersion instance = new ModuleVersion();
}
