package com.restaurant.application.port.in;

import com.restaurant.application.dto.request.RegisterOwnerRequest;
import com.restaurant.application.dto.response.RegistrationResponse;

public interface RegisterOwnerUseCase {
    RegistrationResponse registerOwner(RegisterOwnerRequest request);
}
