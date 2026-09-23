package watersec.internship.watersec_hydrolens.digitaltwin.dto;

public record ComponentRelationship(
        String sourceKey,
        String targetKey,
        RelationshipType type,
        double allocationPriority) {
    public enum RelationshipType { SUPPLIES, DEPENDS_ON, SHARES_LINE_WITH }
}
