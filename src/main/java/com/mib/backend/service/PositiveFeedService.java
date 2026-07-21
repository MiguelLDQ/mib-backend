package com.mib.backend.service;

import com.mib.backend.dto.response.PositiveFeedItemResponse;

import java.util.List;

public interface PositiveFeedService {

    /** Retorna um subconjunto de itens ativos, escolhido de forma deterministica pela
     * data atual — o mesmo conjunto para todos os usuarios no dia, trocando sozinho a
     * cada novo dia, sem precisar de um scheduler dedicado. */
    List<PositiveFeedItemResponse> getTodayFeed(int count);
}
