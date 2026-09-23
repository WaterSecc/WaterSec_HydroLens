package watersec.internship.watersec_hydrolens.digitaltwin.dto;

import java.util.List;

public record WaterNetworkDefinition(
        String primarySource,
        List<WaterNetworkNode> nodes,
        List<WaterNetworkLink> links) {

    public record WaterNetworkNode(String key, String type, String label) {}
    public record WaterNetworkLink(String from, String to, double capacityLitersPerHour) {}
}
