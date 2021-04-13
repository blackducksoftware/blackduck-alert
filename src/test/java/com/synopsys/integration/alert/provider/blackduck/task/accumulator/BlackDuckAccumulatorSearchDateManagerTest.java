package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.util.DateUtils;

public class BlackDuckAccumulatorSearchDateManagerTest {
    @Test
    public void retrieveDateRangeTest() {
        BlackDuckAccumulatorSearchDateManager dateManager = createDateManager(null);
        DateRange dateRange = dateManager.retrieveNextSearchDateRange();
        assertNotNull(dateRange);
        ZonedDateTime startTime = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneOffset.UTC);
        ZonedDateTime endTime = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneOffset.UTC);
        assertNotEquals(dateRange.getStart(), dateRange.getEnd());
        ZonedDateTime expectedStartTime = endTime.minusMinutes(1);
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void retrieveDateRangeParseExceptionTest() {
        BlackDuckAccumulatorSearchDateManager dateRangeCreator = createDateManager("Not a date");

        DateRange dateRange = dateRangeCreator.retrieveNextSearchDateRange();
        assertNotNull(dateRange);
        assertEquals(dateRange.getStart(), dateRange.getEnd());
    }

    @Test
    public void retrieveDateRangeWithExistingFileTest() {
        OffsetDateTime expectedStartDate = ZonedDateTime.now(ZoneOffset.UTC)
                                               .withSecond(0)
                                               .withNano(0)
                                               .minusMinutes(5)
                                               .toOffsetDateTime();
        String expectedStartDateString = DateUtils.formatDateAsJsonString(expectedStartDate);

        BlackDuckAccumulatorSearchDateManager dateRangeCreator = createDateManager(expectedStartDateString);
        DateRange dateRange = dateRangeCreator.retrieveNextSearchDateRange();
        assertNotNull(dateRange);
        OffsetDateTime actualStartDate = dateRange.getStart();
        OffsetDateTime actualEndDate = dateRange.getEnd();
        assertEquals(expectedStartDate, actualStartDate);
        assertNotEquals(actualStartDate, actualEndDate);
    }

    @Test
    public void formatDateTest() throws ParseException {
        ProviderTaskPropertiesAccessor mockTaskPropsAccessor = createMockTaskPropertiesAccessor();
        BlackDuckAccumulatorSearchDateManager dateManager = new BlackDuckAccumulatorSearchDateManager(mockTaskPropsAccessor, 0L, "Task");

        OffsetDateTime currentDate = DateUtils.createCurrentDateTimestamp();
        dateManager.saveNextSearchStart(currentDate);

        String currentDateString = DateUtils.formatDateAsJsonString(currentDate);
        OffsetDateTime currentDateDeserialized = DateUtils.parseDateFromJsonString(currentDateString);

        DateRange dateRange = dateManager.retrieveNextSearchDateRange();

        OffsetDateTime nextSearchStart = dateRange.getStart();
        assertEquals(currentDateDeserialized, nextSearchStart);
    }

    private BlackDuckAccumulatorSearchDateManager createDateManager(String expectedDate) {
        ProviderTaskPropertiesAccessor mockTaskPropsAccessor = Mockito.mock(ProviderTaskPropertiesAccessor.class);
        Mockito.when(mockTaskPropsAccessor.getTaskProperty(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.ofNullable(expectedDate));
        return new BlackDuckAccumulatorSearchDateManager(mockTaskPropsAccessor, 0L, "Task");
    }

    private ProviderTaskPropertiesAccessor createMockTaskPropertiesAccessor() {
        return new ProviderTaskPropertiesAccessor() {
            private String taskProperty = null;

            @Override
            public Optional<String> getTaskProperty(String taskName, String propertyKey) {
                return Optional.ofNullable(taskProperty);
            }

            @Override
            public void setTaskProperty(Long configId, String taskName, String propertyKey, String propertyValue) {
                this.taskProperty = propertyValue;
            }
        };
    }

}
