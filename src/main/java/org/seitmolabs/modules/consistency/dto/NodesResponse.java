package org.seitmolabs.modules.consistency.dto;

import java.util.List;

public record NodesResponse(
        String key,
        List<NodeInfoResponse> nodes
) {
}
