package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.shim.runkeeper.mapper.RunKeeperDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Emerson Farrugia
 */
public class RunKeeperPhysicalActivityDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final RunKeeperPhysicalActivityDataPointMapper mapper = new RunKeeperPhysicalActivityDataPointMapper();

    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/runkeeper/mapper/runkeeper-fitness-activities.json");

        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));

        TimeInterval effectiveTimeInterval = TimeInterval.ofStartDateTimeAndDuration(
                OffsetDateTime.of(2014, 10, 19, 13, 17, 27, 0, ZoneOffset.ofHours(2)),
                new DurationUnitValue(SECOND, 4364.74158141667));

        PhysicalActivity physicalActivity = new PhysicalActivity.Builder("Cycling")
                .setDistance(new LengthUnitValue(METER, 10128.7131871337))
                .setEffectiveTimeFrame(effectiveTimeInterval)
                .build();

        DataPoint<PhysicalActivity> dataPoint = dataPoints.get(0);

        assertThat(dataPoint.getBody(), equalTo(physicalActivity));

        DataPointAcquisitionProvenance acquisitionProvenance = dataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(acquisitionProvenance.getModality(), equalTo(SENSED));
        assertThat(acquisitionProvenance.getAdditionalProperty("external_id").isPresent(), equalTo(true));
        assertThat(acquisitionProvenance.getAdditionalProperty("external_id").get(),
                equalTo("/fitnessActivities/465161536"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(1));

        DataPoint<PhysicalActivity> dataPoint = dataPoints.get(1);

        DataPointAcquisitionProvenance acquisitionProvenance = dataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getModality(), equalTo(SELF_REPORTED));
    }
}