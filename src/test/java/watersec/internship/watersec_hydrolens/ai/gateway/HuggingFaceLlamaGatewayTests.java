package watersec.internship.watersec_hydrolens.ai.gateway;

import org.junit.jupiter.api.Test;
import watersec.internship.watersec_hydrolens.ai.exception.AiAnalysisException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuggingFaceLlamaGatewayTests {
    @Test
    void extractsJsonFromMarkdownWrappedStructuredOutput() {
        assertThat(HuggingFaceLlamaGateway.extractJson("Here is the result:\n```json\n{\"hotelName\":\"Demo\"}\n```"))
                .isEqualTo("{\"hotelName\":\"Demo\"}");
    }

    @Test
    void rejectsResponsesWithoutJson() {
        assertThatThrownBy(() -> HuggingFaceLlamaGateway.extractJson("Unable to generate"))
                .isInstanceOf(AiAnalysisException.class)
                .hasMessageContaining("did not contain a JSON object");
    }
}
