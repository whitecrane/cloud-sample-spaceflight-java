package com.sap.cloudsamples.spaceflight;

import java.util.Collections;

import org.apache.http.HttpStatus;

import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.service.prov.api.DataSourceHandler;
import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.EntityDataBuilder;
import com.sap.cloud.sdk.service.prov.api.ExtensionHelper;
import com.sap.cloud.sdk.service.prov.api.annotations.BeforeCreate;
import com.sap.cloud.sdk.service.prov.api.annotations.BeforeUpdate;
import com.sap.cloud.sdk.service.prov.api.exception.DatasourceException;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeCreateResponse;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeUpdateResponse;
import com.sap.cloud.sdk.service.prov.api.exits.PreExtensionResponseBuilderWithBody;
import com.sap.cloud.sdk.service.prov.api.exits.PreExtensionResponseImpl;
import com.sap.cloud.sdk.service.prov.api.exits.PreExtensionResponseWithBody;
import com.sap.cloud.sdk.service.prov.api.request.CreateRequest;
import com.sap.cloud.sdk.service.prov.api.request.UpdateRequest;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponseBuilder;

public class BookingsHandler {

	private static final String PROPERTY_ID = "ID";
	private static final String BOOKING_SERVICE = "BookingService";
	private static final String ENTITY_ITINERARIES = BOOKING_SERVICE + ".Itineraries";
	private static final String ENTITY_BOOKINGS = "Bookings";
	private static final String PROPERTY_BOOKING_ITINERARYID = "Itinerary_ID";

	// private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@BeforeCreate(serviceName = BOOKING_SERVICE, entity = ENTITY_BOOKINGS)
	public BeforeCreateResponse beforeBookingCreate(CreateRequest req, ExtensionHelper helper)
			throws ODataException, DatasourceException {
		return beforeUpsert(req.getData(), helper.getHandler());
	}

	@BeforeUpdate(serviceName = BOOKING_SERVICE, entity = ENTITY_BOOKINGS)
	public BeforeUpdateResponse beforeBookingUpdate(UpdateRequest req, ExtensionHelper helper)
			throws ODataException, DatasourceException {
		return beforeUpsert(req.getData(), helper.getHandler());
	}

	private PreExtensionResponseWithBody beforeUpsert(EntityData reqData, DataSourceHandler dataSource)
			throws ODataException, DatasourceException {
		ErrorResponseBuilder errorResponseBuilder = ErrorResponse.getBuilder();
		EntityDataBuilder entityBuilder = EntityData.getBuilder(reqData);

		errorResponseBuilder = validateItinerary(reqData, ENTITY_ITINERARIES, PROPERTY_BOOKING_ITINERARYID,
				dataSource, errorResponseBuilder);

		if (errorResponseBuilder != null) {
			return new PreExtensionResponseImpl(errorResponseBuilder.response());
		}

		return new PreExtensionResponseBuilderWithBody(entityBuilder.buildEntityData(ENTITY_BOOKINGS)).response();
	}

	private static ErrorResponseBuilder validateItinerary(EntityData reqData, String entity, String property,
			DataSourceHandler dataSource, ErrorResponseBuilder responseBuilder) throws DatasourceException {
		if (reqData.contains(property)) {
			Object itineraryId = reqData.getElementValue(property);
			EntityData itinerary = dataSource.executeRead(entity, Collections.singletonMap(PROPERTY_ID, itineraryId),
					Collections.singletonList(PROPERTY_ID));
			if (itinerary == null) {
				return constraintError("NoSuchItinerary", itineraryId, property, responseBuilder);
			}
		}
		return responseBuilder;
	}

	private static ErrorResponseBuilder constraintError(String messageKey, Object id, String target,
			ErrorResponseBuilder responseBuilder) {
		if (responseBuilder == null) {
			responseBuilder = ErrorResponse.getBuilder();
		}
		return responseBuilder.setStatusCode(HttpStatus.SC_BAD_REQUEST).setMessage(messageKey, id)
				.addErrorDetail(messageKey, target, id);
	}
}
