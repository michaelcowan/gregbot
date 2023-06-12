package io.blt.gregbot.core.template;

import io.blt.util.Obj;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RendererTest {

    Renderer renderer = Obj.tap(Renderer::new, r -> r.setVariables(
            Map.of(
                    "name", "Greg",
                    "age", "21",
                    "subject", "pizza")));

    @Test
    void renderShouldReplaceKnownVariables() throws UnknownVariableException {
        var result = renderer.render("We all knew {name} was an expert on {subject}.");

        assertThat(result).isEqualTo("We all knew Greg was an expert on pizza.");
    }

    @Test
    void renderShouldThrowWhenRenderingAnUnknownVariable() {
        assertThatExceptionOfType(UnknownVariableException.class)
                .isThrownBy(() -> renderer.render("A missing variable name is {foo}"))
                .withMessage("Unknown variable key 'foo'")
                .extracting(UnknownVariableException::variable)
                .isEqualTo("foo");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "only has opening moustache {",
            "only has opening moustache with name {age",
            "only has closing moustache }",
            "only has closing moustache with name age}"
    })
    void renderShouldIgnoreBadlyFormedVariables(String template) throws UnknownVariableException {
        var result = renderer.render(template);

        assertThat(result).isEqualTo(template);
    }

    @Test
    void renderShouldReplaceKnownVariablesGivingPrecedenceToExtraVariables() throws UnknownVariableException {
        var result = renderer.render("We all knew {name} was an expert on {subject}.", Map.of("subject", "puns"));

        assertThat(result).isEqualTo("We all knew Greg was an expert on puns.");
    }

    @Test
    void renderShouldUseExtraVariablesForAnUnknownVariable() throws UnknownVariableException {
        var result = renderer.render("Hello {unknown}.", Map.of("unknown", "World"));

        assertThat(result).isEqualTo("Hello World.");
    }

    @Test
    void renderShouldThrowWhenRenderingAnUnknownVariableUnsatisfiedByExtraVariables() {
        assertThatExceptionOfType(UnknownVariableException.class)
                .isThrownBy(() -> renderer.render("A missing variable name is {foo}", Map.of("bar", "baz")))
                .withMessage("Unknown variable key 'foo'")
                .extracting(UnknownVariableException::variable)
                .isEqualTo("foo");
    }

    @Test
    void findUnknownVariablesShouldReturnUnknownVariableNames() {
        var result = renderer.findUnknownVariables("the best {subject} was from {shop} and always came with a {gift}");

        assertThat(result).containsExactly("shop", "gift");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Its impossible to eat enough {subject}!",
            "His name is {name} and he is {age} years old."
    })
    void findUnknownVariablesShouldReturnEmptyForKnownVariableNames(String template) {
        var result = renderer.findUnknownVariables(template);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @EmptySource
    void findUnknownVariablesShouldReturnEmptyForEmptyTemplate(String template) {
        var result = renderer.findUnknownVariables(template);

        assertThat(result).isEmpty();
    }


}
