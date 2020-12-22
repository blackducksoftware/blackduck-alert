package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorTypeEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class DefaultDescriptorAccessorTest {
    @Test
    public void getRegisteredDescriptorsTest() {
        final String name = "name-test";
        final Long typeId = 1L;
        final DescriptorType descriptorType = DescriptorType.CHANNEL;

        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(name, typeId);
        registeredDescriptorEntity.setId(1L);
        DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity(descriptorType.name());

        RegisteredDescriptorRepository registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        DescriptorTypeRepository descriptorTypeRepository = Mockito.mock(DescriptorTypeRepository.class);

        Mockito.when(registeredDescriptorRepository.findAll()).thenReturn(List.of(registeredDescriptorEntity));
        Mockito.when(descriptorTypeRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorTypeEntity));

        DefaultDescriptorAccessor descriptorAccessor = new DefaultDescriptorAccessor(registeredDescriptorRepository, null, null, descriptorTypeRepository);
        List<RegisteredDescriptorModel> registeredDescriptorModelList = descriptorAccessor.getRegisteredDescriptors();

        assertEquals(1, registeredDescriptorModelList.size());
        RegisteredDescriptorModel registeredDescriptorModel = registeredDescriptorModelList.get(0);
        assertEquals(name, registeredDescriptorModel.getName());
        assertEquals(descriptorType, registeredDescriptorModel.getType());
    }

    @Test
    public void getRegisteredDescriptorByKeyTest() {
        final String name = "name-test";
        final Long typeId = 1L;
        final DescriptorType descriptorType = DescriptorType.CHANNEL;

        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(name, typeId);
        registeredDescriptorEntity.setId(1L);
        DescriptorKey descriptorKey = createDescriptorKey("descriptorKey-test");
        DescriptorKey emptyDescriptorKey = createDescriptorKey("bad-key");
        DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity(descriptorType.name());

        RegisteredDescriptorRepository registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        DescriptorTypeRepository descriptorTypeRepository = Mockito.mock(DescriptorTypeRepository.class);

        Mockito.when(registeredDescriptorRepository.findFirstByName(descriptorKey.getUniversalKey())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(registeredDescriptorRepository.findFirstByName(emptyDescriptorKey.getUniversalKey())).thenReturn(Optional.empty());
        Mockito.when(descriptorTypeRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorTypeEntity));

        DefaultDescriptorAccessor descriptorAccessor = new DefaultDescriptorAccessor(registeredDescriptorRepository, null, null, descriptorTypeRepository);
        Optional<RegisteredDescriptorModel> registeredDescriptorModelOptional = descriptorAccessor.getRegisteredDescriptorByKey(descriptorKey);
        Optional<RegisteredDescriptorModel> registeredDescriptorModelOptionalEmpty = descriptorAccessor.getRegisteredDescriptorByKey(emptyDescriptorKey);

        assertTrue(registeredDescriptorModelOptional.isPresent());
        RegisteredDescriptorModel registeredDescriptorModel = registeredDescriptorModelOptional.get();
        assertFalse(registeredDescriptorModelOptionalEmpty.isPresent());
        assertEquals(name, registeredDescriptorModel.getName());
        assertEquals(descriptorType, registeredDescriptorModel.getType());
    }

    @Test
    public void getRegisteredDescriptorsByTypeTest() {
        final String name = "name-test";
        final Long typeId = 1L;
        final DescriptorType descriptorType = DescriptorType.CHANNEL;

        RegisteredDescriptorRepository registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        DescriptorTypeRepository descriptorTypeRepository = Mockito.mock(DescriptorTypeRepository.class);

        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(name, typeId);
        registeredDescriptorEntity.setId(1L);
        DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity(descriptorType.name());
        descriptorTypeEntity.setId(1L);

        Mockito.when(descriptorTypeRepository.findFirstByType(descriptorType.name())).thenReturn(Optional.of(descriptorTypeEntity));
        Mockito.when(registeredDescriptorRepository.findByTypeId(Mockito.any())).thenReturn(List.of(registeredDescriptorEntity));
        Mockito.when(descriptorTypeRepository.findById(registeredDescriptorEntity.getTypeId())).thenReturn(Optional.of(descriptorTypeEntity));

        DefaultDescriptorAccessor descriptorAccessor = new DefaultDescriptorAccessor(registeredDescriptorRepository, null, null, descriptorTypeRepository);
        List<RegisteredDescriptorModel> registeredDescriptorModelList = descriptorAccessor.getRegisteredDescriptorsByType(descriptorType);

        assertEquals(1, registeredDescriptorModelList.size());
        RegisteredDescriptorModel registeredDescriptorModel = registeredDescriptorModelList.get(0);
        assertEquals(name, registeredDescriptorModel.getName());
        assertEquals(descriptorType, registeredDescriptorModel.getType());
    }

    @Test
    public void getRegisteredDescriptorsByTypeMissingDescriptorTypeTest() {
        final String name = "name-test";
        final Long typeId = 1L;
        final DescriptorType descriptorType = DescriptorType.CHANNEL;

        RegisteredDescriptorRepository registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        DescriptorTypeRepository descriptorTypeRepository = Mockito.mock(DescriptorTypeRepository.class);

        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(name, typeId);
        registeredDescriptorEntity.setId(1L);
        DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity(descriptorType.name());
        descriptorTypeEntity.setId(1L);

        Mockito.when(descriptorTypeRepository.findFirstByType(descriptorType.name())).thenReturn(Optional.empty());
        Mockito.when(descriptorTypeRepository.save(Mockito.any())).thenReturn(descriptorTypeEntity);
        Mockito.when(registeredDescriptorRepository.findByTypeId(Mockito.any())).thenReturn(List.of(registeredDescriptorEntity));
        Mockito.when(descriptorTypeRepository.findById(registeredDescriptorEntity.getTypeId())).thenReturn(Optional.of(descriptorTypeEntity));

        DefaultDescriptorAccessor descriptorAccessor = new DefaultDescriptorAccessor(registeredDescriptorRepository, null, null, descriptorTypeRepository);
        List<RegisteredDescriptorModel> registeredDescriptorModelList = descriptorAccessor.getRegisteredDescriptorsByType(descriptorType);

        assertEquals(1, registeredDescriptorModelList.size());
        RegisteredDescriptorModel registeredDescriptorModel = registeredDescriptorModelList.get(0);
        assertEquals(name, registeredDescriptorModel.getName());
        assertEquals(descriptorType, registeredDescriptorModel.getType());
    }

    @Test
    public void getRegisteredDescriptorByIdTest() {
        final String name = "name-test";
        final Long typeId = 1L;
        final DescriptorType descriptorType = DescriptorType.CHANNEL;
        final Long descriptorId = 2L;

        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(name, typeId);
        registeredDescriptorEntity.setId(1L);
        DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity(descriptorType.name());
        descriptorTypeEntity.setId(2L);

        RegisteredDescriptorRepository registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        DescriptorTypeRepository descriptorTypeRepository = Mockito.mock(DescriptorTypeRepository.class);

        Mockito.when(registeredDescriptorRepository.findById(descriptorId)).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(descriptorTypeRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorTypeEntity));

        DefaultDescriptorAccessor descriptorAccessor = new DefaultDescriptorAccessor(registeredDescriptorRepository, null, null, descriptorTypeRepository);
        Optional<RegisteredDescriptorModel> registeredDescriptorModelOptional = descriptorAccessor.getRegisteredDescriptorById(descriptorId);

        assertTrue(registeredDescriptorModelOptional.isPresent());
        RegisteredDescriptorModel registeredDescriptorModel = registeredDescriptorModelOptional.get();
        assertEquals(typeId, registeredDescriptorModel.getId());
        assertEquals(name, registeredDescriptorModel.getName());
        assertEquals(descriptorType, registeredDescriptorModel.getType());
    }

    @Test
    public void getFieldsForDescriptorTest() {
        final String name = "name-test";
        final Long typeId = 1L;
        final ConfigContextEnum configContextEnum = ConfigContextEnum.GLOBAL;
        final ConfigContextEnum invalidConfigContextEnum = ConfigContextEnum.DISTRIBUTION;
        final String definedFieldsKey = "defined-field-key-test";
        Boolean isSensitive = Boolean.TRUE;

        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(name, typeId);
        registeredDescriptorEntity.setId(1L);
        DescriptorKey descriptorKey = createDescriptorKey("descriptorKey-test");
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        configContextEntity.setId(3L);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(definedFieldsKey, isSensitive);

        RegisteredDescriptorRepository registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        DefinedFieldRepository definedFieldRepository = Mockito.mock(DefinedFieldRepository.class);
        ConfigContextRepository configContextRepository = Mockito.mock(ConfigContextRepository.class);
        DescriptorTypeRepository descriptorTypeRepository = Mockito.mock(DescriptorTypeRepository.class);

        Mockito.when(registeredDescriptorRepository.findFirstByName(descriptorKey.getUniversalKey())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(configContextRepository.findFirstByContext(configContextEnum.name())).thenReturn(Optional.of(configContextEntity));
        //Used to test the optional expression
        Mockito.when(configContextRepository.findFirstByContext(invalidConfigContextEnum.name())).thenReturn(Optional.empty());
        Mockito.when(configContextRepository.save(Mockito.any())).thenReturn(configContextEntity);
        Mockito.when(definedFieldRepository.findByDescriptorIdAndContext(Mockito.any(), Mockito.any())).thenReturn(List.of(definedFieldEntity));

        DefaultDescriptorAccessor descriptorAccessor = new DefaultDescriptorAccessor(registeredDescriptorRepository, definedFieldRepository, configContextRepository, descriptorTypeRepository);
        List<DefinedFieldModel> definedFieldModelList = descriptorAccessor.getFieldsForDescriptor(descriptorKey, configContextEnum);
        List<DefinedFieldModel> emptyConfigContextDefinedFieldModelList = descriptorAccessor.getFieldsForDescriptor(descriptorKey, invalidConfigContextEnum);

        assertEquals(1, definedFieldModelList.size());
        DefinedFieldModel definedFieldModel = definedFieldModelList.get(0);
        assertEquals(definedFieldsKey, definedFieldModel.getKey());
        assertEquals(isSensitive, definedFieldModel.getSensitive());
        List<ConfigContextEnum> configContextList = new ArrayList<>(definedFieldModel.getContexts());
        assertEquals(configContextEnum, configContextList.get(0));

        assertEquals(1, emptyConfigContextDefinedFieldModelList.size());
        DefinedFieldModel emptyConfigContextDefinedFieldModel = emptyConfigContextDefinedFieldModelList.get(0);
        assertEquals(definedFieldsKey, emptyConfigContextDefinedFieldModel.getKey());
        assertEquals(isSensitive, emptyConfigContextDefinedFieldModel.getSensitive());
        List<ConfigContextEnum> configContextList2 = new ArrayList<>(emptyConfigContextDefinedFieldModel.getContexts());
        assertEquals(invalidConfigContextEnum, configContextList2.get(0));
    }

    @Test
    public void getFieldsForDescriptorByIdTest() {
        final String name = "name-test";
        final Long typeId = 1L;
        final ConfigContextEnum configContextEnum = ConfigContextEnum.GLOBAL;
        final String definedFieldsKey = "defined-field-key-test";
        Boolean isSensitive = Boolean.TRUE;
        final Long descriptorId = 1L;

        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(name, typeId);
        registeredDescriptorEntity.setId(1L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        configContextEntity.setId(3L);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(definedFieldsKey, isSensitive);

        RegisteredDescriptorRepository registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        DefinedFieldRepository definedFieldRepository = Mockito.mock(DefinedFieldRepository.class);
        ConfigContextRepository configContextRepository = Mockito.mock(ConfigContextRepository.class);
        DescriptorTypeRepository descriptorTypeRepository = Mockito.mock(DescriptorTypeRepository.class);

        Mockito.when(registeredDescriptorRepository.findById(descriptorId)).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(configContextRepository.findFirstByContext(configContextEnum.name())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(definedFieldRepository.findByDescriptorIdAndContext(Mockito.any(), Mockito.any())).thenReturn(List.of(definedFieldEntity));

        DefaultDescriptorAccessor descriptorAccessor = new DefaultDescriptorAccessor(registeredDescriptorRepository, definedFieldRepository, configContextRepository, descriptorTypeRepository);
        List<DefinedFieldModel> definedFieldModelList = descriptorAccessor.getFieldsForDescriptorById(descriptorId, configContextEnum);

        assertEquals(1, definedFieldModelList.size());
        DefinedFieldModel definedFieldModel = definedFieldModelList.get(0);
        assertEquals(definedFieldsKey, definedFieldModel.getKey());
        assertEquals(isSensitive, definedFieldModel.getSensitive());
        List<ConfigContextEnum> configContextList = new ArrayList<>(definedFieldModel.getContexts());
        assertEquals(configContextEnum, configContextList.get(0));
    }

    private DescriptorKey createDescriptorKey(String key) {
        return new DescriptorKey(key, key) {};
    }

}
