package com.blackducksoftware.integration.hub.alert.descriptor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.descriptor.DescriptorType;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleConfigActions;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public class ChannelDescriptorTest {

    @Test
    public void testType() {
        final TestChannelDescriptor testChannelDescriptor = new TestChannelDescriptor();
        final DescriptorType type = testChannelDescriptor.getType();

        assertEquals(DescriptorType.CHANNEL, type);
    }

    class TestChannelDescriptor implements ChannelDescriptor {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public <E extends DatabaseEntity> Class<E> getGlobalEntityClass() {
            return null;
        }

        @Override
        public <R extends ConfigRestModel> Class<R> getGlobalRestModelClass() {
            return null;
        }

        @Override
        public <R extends JpaRepository<DatabaseEntity, Long>> R getGlobalRepository() {
            return null;
        }

        @Override
        public <A extends SimpleConfigActions> A getGlobalConfigActions() {
            return null;
        }

        @Override
        public String getDestinationName() {
            return null;
        }

        @Override
        public DistributionChannel getChannelComponent() {
            return null;
        }

        @Override
        public <E extends DatabaseEntity> Class<E> getDistributionEntityClass() {
            return null;
        }

        @Override
        public <R extends CommonDistributionConfigRestModel> Class<R> getDistributionRestModelClass() {
            return null;
        }

        @Override
        public boolean hasGlobalConfiguration() {
            return false;
        }

        @Override
        public <R extends JpaRepository<DatabaseEntity, Long>> R getDistributionRepository() {
            return null;
        }

        @Override
        public <A extends SimpleDistributionConfigActions> A getDistributionConfigActions() {
            return null;
        }

    }

}
