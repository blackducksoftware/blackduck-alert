package com.blackduck.integration.alert.common.descriptor;

import java.util.Optional;
import java.util.Set;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;
import com.blackduck.integration.util.Stringable;

/**
 * Note when creating a new descriptor you will need to register descriptors in the database.
 * <br/>
 * <br/>
 * Use the REGISTER_DESCRIPTOR stored procedure.
 * <br/>
 * The REGISTER_DESCRIPTOR stored procedure will assign default permissions to the descriptor for the well known roles.
 * <br/>
 * The default permissions should be sufficient for channels and providers.
 * <br/>
 * <br/>
 * For components you may need to remove permissions.  In order to do that; use the stored procedures:
 * <br/>
 * <br/>
 * REMOVE_PERMISSION - removes a single permission from the user role for the descriptor and context.
 * <br/>
 * <br/>
 * REMOVE_ALL_PERMISSIONS - removes all permissions from the user role for the descriptor and context.
 */
public abstract class Descriptor extends Stringable {
    private final DescriptorKey descriptorKey;
    private final DescriptorType type;
    private final Set<ConfigContextEnum> configContexts;

    protected Descriptor(DescriptorKey descriptorKey, DescriptorType type, Set<ConfigContextEnum> configContexts) {
        this.descriptorKey = descriptorKey;
        this.type = type;
        this.configContexts = configContexts;
    }

    public DescriptorKey getDescriptorKey() {
        return descriptorKey;
    }

    public DescriptorType getType() {
        return type;
    }

    public Set<ConfigContextEnum> getConfigContexts() {
        return configContexts;
    }

    // TODO these should be concrete methods based on optional constructor params
    public abstract Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator();

    public abstract Optional<DistributionConfigurationValidator> getDistributionValidator();

    public boolean hasConfigForType(ConfigContextEnum actionApiType) {
        return configContexts.contains(actionApiType);
    }

}
