package com.mib.backend.service;

import com.mib.backend.dto.request.UpdateInterestsRequest;
import com.mib.backend.dto.response.InterestResponse;

import java.util.List;
import java.util.UUID;

public interface InterestService {

    List<InterestResponse> getCatalog(UUID currentUserId);

    List<InterestResponse> updateMyInterests(UUID currentUserId, UpdateInterestsRequest request);
}
