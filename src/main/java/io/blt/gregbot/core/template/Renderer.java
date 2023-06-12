package io.blt.gregbot.core.template;

import io.blt.util.Obj;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

/**
 * Renders a template replacing any matching variable key with its value.
 * <p>In a template a variable is identified using the syntax {@code "{variable}"}.</p>
 */
public class Renderer {

    private final Pattern pattern = Pattern.compile("\\{(.+?)}");

    private final Map<String, String> variables = new HashMap<>();

    /**
     * Sets the variables to replace when rendering.
     *
     * @param variables {@link Map} of variables to replace when rendering
     */
    public void setVariables(Map<String, String> variables) {
        this.variables.clear();
        this.variables.putAll(variables);
    }

    /**
     * Returns {@code template} fully rendered with values.
     * <p>
     * Rendering will use variables set both via {@link Renderer#setVariables(Map)} and the {@code variables} parameter.
     * Precedence is given to the {@code variables} parameter.
     * </p>
     *
     * @param template  Template to render
     * @param variables Extra variables to use
     * @return Rendered template
     * @throws UnknownVariableException Thrown when {@code template} uses an unknown variable key
     */
    public String render(final String template, final Map<String, String> variables) throws UnknownVariableException {
        var builder = new StringBuilder();

        var matcher = pattern.matcher(template);

        while (matcher.find()) {
            var key = matcher.group(1);
            var value = Obj.orElseGet(variables.get(key), () -> getValue(key));

            matcher.appendReplacement(builder, Matcher.quoteReplacement(value));
        }

        matcher.appendTail(builder);

        return builder.toString();
    }

    /**
     * Returns {@code template} fully rendered with values.
     *
     * @param template Template to render
     * @return Rendered template
     * @throws UnknownVariableException Thrown when {@code template} uses an unknown variable key
     */
    public String render(final String template) throws UnknownVariableException {
        return render(template, Map.of());
    }

    /**
     * Finds any unknown variables in the template.
     *
     * @param template Template to test
     * @return Set containing any unknown variable keys
     */
    public Set<String> findUnknownVariables(final String template) {
        var result = new LinkedHashSet<String>();

        var matcher = pattern.matcher(template);

        while (matcher.find()) {
            var key = matcher.group(1);
            if (!variables.containsKey(key)) {
                result.add(key);
            }
        }

        return result;
    }

    private String getValue(String key) throws UnknownVariableException {
        var variable = variables.get(key);
        if (isNull(variable)) {
            throw new UnknownVariableException(key);
        }
        return variable;
    }
}
