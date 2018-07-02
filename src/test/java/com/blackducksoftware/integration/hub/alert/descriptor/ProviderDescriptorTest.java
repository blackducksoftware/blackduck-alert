package com.blackducksoftware.integration.hub.alert.descriptor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public class ProviderDescriptorTest {

    @Test
    public void testType() {
        final TestProviderDescriptor testProviderDescriptor = new TestProviderDescriptor();
        final DescriptorType type = testProviderDescriptor.getType();

        assertEquals(DescriptorType.PROVIDER, type);
    }

    class TestProviderDescriptor implements ProviderDescriptor {

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

    }
}
