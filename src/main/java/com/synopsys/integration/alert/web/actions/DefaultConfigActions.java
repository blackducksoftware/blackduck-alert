package com.synopsys.integration.alert.web.actions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.annotation.SensitiveFieldFinder;
import com.synopsys.integration.alert.common.descriptor.config.TypeConverter;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;

@Component
public class DefaultConfigActions {
    private final ContentConverter contentConverter;

    @Autowired
    public DefaultConfigActions(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    public boolean doesConfigExist(final Long id, final RepositoryAccessor repositoryAccessor) {
        return id != null && repositoryAccessor.readEntity(id).isPresent();
    }

    public Config getConfig(final Long id, final RepositoryAccessor repositoryAccessor, final TypeConverter typeConverter) throws AlertException {
        if (id != null) {
            final Optional<? extends DatabaseEntity> foundEntity = repositoryAccessor.readEntity(id);
            if (foundEntity.isPresent()) {
                final Config restModel = typeConverter.populateConfigFromEntity(foundEntity.get());
                if (restModel != null) {
                    final Config maskedRestModel = maskRestModel(restModel);
                    return maskedRestModel;
                }
            }
        }
        return null;
    }

    public List<? extends Config> getConfigs(final RepositoryAccessor repositoryAccessor, final TypeConverter typeConverter) throws AlertException {
        final List<? extends DatabaseEntity> databaseEntities = repositoryAccessor.readEntities();
        final List<Config> restModels = getConvertedRestModels(databaseEntities, typeConverter);
        return maskRestModels(restModels);
    }

    public void deleteConfig(final Long id, final RepositoryAccessor repositoryAccessor) {
        if (id != null) {
            repositoryAccessor.deleteEntity(id);
        }
    }

    public DatabaseEntity saveConfig(final Config config, final RepositoryAccessor repositoryAccessor, final TypeConverter typeConverter) {
        if (config != null) {
            final DatabaseEntity createdEntity = typeConverter.populateEntityFromConfig(config);
            if (createdEntity != null) {
                final DatabaseEntity savedEntity = repositoryAccessor.saveEntity(createdEntity);
                return savedEntity;
            }
        }
        return null;
    }

    public DatabaseEntity updateConfig(final Config config, final RepositoryAccessor repositoryAccessor, final TypeConverter typeConverter) throws AlertException {
        if (config != null && StringUtils.isNotBlank(config.getId())) {
            try {
                DatabaseEntity createdEntity = typeConverter.populateEntityFromConfig(config);
                final DatabaseEntity savedEntity = getSavedEntity(config.getId(), repositoryAccessor);
                createdEntity = updateEntityWithSavedEntity(createdEntity, savedEntity);
                if (createdEntity != null) {
                    final DatabaseEntity updatedEntity = repositoryAccessor.saveEntity(createdEntity);
                    return updatedEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    public DatabaseEntity getSavedEntity(final String id, final RepositoryAccessor repositoryAccessor) throws AlertException {
        if (StringUtils.isNotBlank(id)) {
            final Long longId = contentConverter.getLongValue(id);
            final Optional<? extends DatabaseEntity> savedConfig = repositoryAccessor.readEntity(longId);
            if (savedConfig.isPresent()) {
                return savedConfig.get();
            }
        }
        return null;
    }

    public <T> T updateEntityWithSavedEntity(final T entity, final DatabaseEntity savedEntity) throws AlertException {
        try {
            final Class<?> newConfigClass = entity.getClass();

            final Set<Field> sensitiveFields = SensitiveFieldFinder.findSensitiveFields(newConfigClass);
            for (final Field field : sensitiveFields) {
                field.setAccessible(true);
                final Object value = field.get(entity);
                if (value == null || StringUtils.isBlank(value.toString())) {
                    if (savedEntity != null) {
                        final Class<?> savedConfigClass = savedEntity.getClass();
                        Field savedField = null;
                        try {
                            savedField = savedConfigClass.getDeclaredField(field.getName());
                        } catch (final NoSuchFieldException e) {
                            continue;
                        }
                        savedField.setAccessible(true);
                        final String savedValue = (String) savedField.get(savedEntity);
                        field.set(entity, savedValue);
                    }
                }
            }
        } catch (final SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AlertException(e.getMessage(), e);
        }

        return entity;
    }

    private List<Config> getConvertedRestModels(final List<? extends DatabaseEntity> entities, final TypeConverter typeConverter) throws AlertException {
        final List<Config> restModels = new ArrayList<>(entities.size());
        for (final DatabaseEntity entity : entities) {
            restModels.add(typeConverter.populateConfigFromEntity(entity));
        }
        return restModels;
    }

    private Config maskRestModel(final Config config) throws AlertException {
        final Class<? extends Config> restModelClass = config.getClass();
        try {
            final Set<Field> sensitiveFields = SensitiveFieldFinder.findSensitiveFields(restModelClass);

            for (final Field sensitiveField : sensitiveFields) {
                boolean isFieldSet = false;
                sensitiveField.setAccessible(true);
                final Object sensitiveFieldValue = sensitiveField.get(config);
                if (sensitiveFieldValue != null) {
                    final String sensitiveFieldString = (String) sensitiveFieldValue;
                    if (StringUtils.isNotBlank(sensitiveFieldString)) {
                        isFieldSet = true;
                    }
                }
                sensitiveField.set(config, null);

                final Field fieldIsSet = restModelClass.getDeclaredField(sensitiveField.getName() + "IsSet");
                fieldIsSet.setAccessible(true);
                final boolean sensitiveIsSetFieldValue = (boolean) fieldIsSet.get(config);
                if (!sensitiveIsSetFieldValue) {
                    fieldIsSet.setBoolean(config, isFieldSet);
                }

            }
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AlertException(e.getMessage(), e);
        }
        return config;
    }

    private List<Config> maskRestModels(final List<Config> restModels) throws AlertException {
        final List<Config> maskedRestModels = new ArrayList<>();
        for (final Config restModel : restModels) {
            maskedRestModels.add(maskRestModel(restModel));
        }
        return maskedRestModels;
    }
}
