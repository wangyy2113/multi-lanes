package com.wangyy.multilanes.core.annotation;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE + 40)
class OnConfigCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        List<AnnotationAttributes> allAnnotationAttributes = annotationAttributesFromMultiValueMap(
                metadata.getAllAnnotationAttributes(
                        ConditionalOnConfig.class.getName()));
        List<ConditionMessage> noMatch = new ArrayList<>();
        List<ConditionMessage> match = new ArrayList<>();
        for (AnnotationAttributes annotationAttributes : allAnnotationAttributes) {
            ConditionOutcome outcome = determineOutcome(annotationAttributes);
            (outcome.isMatch() ? match : noMatch).add(outcome.getConditionMessage());
        }
        if (!noMatch.isEmpty()) {
            return ConditionOutcome.noMatch(ConditionMessage.of(noMatch));
        }
        return ConditionOutcome.match(ConditionMessage.of(match));
    }

    private List<AnnotationAttributes> annotationAttributesFromMultiValueMap(
            MultiValueMap<String, Object> multiValueMap) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Map.Entry<String, List<Object>> entry : multiValueMap.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                Map<String, Object> map;
                if (i < maps.size()) {
                    map = maps.get(i);
                } else {
                    map = new HashMap<>();
                    maps.add(map);
                }
                map.put(entry.getKey(), entry.getValue().get(i));
            }
        }
        List<AnnotationAttributes> annotationAttributes = new ArrayList<>(
                maps.size());
        for (Map<String, Object> map : maps) {
            annotationAttributes.add(AnnotationAttributes.fromMap(map));
        }
        return annotationAttributes;
    }

    private ConditionOutcome determineOutcome(AnnotationAttributes annotationAttributes) {
        OnConfigCondition.Spec spec = new OnConfigCondition.Spec(annotationAttributes);
        List<String> missingProperties = new ArrayList<>();
        List<String> nonMatchingProperties = new ArrayList<>();
        spec.collectProperties(missingProperties, nonMatchingProperties);
        if (!missingProperties.isEmpty()) {
            return ConditionOutcome.noMatch(
                    ConditionMessage.forCondition(ConditionalOnConfig.class, spec)
                            .didNotFind("property", "properties")
                            .items(ConditionMessage.Style.QUOTE, missingProperties));
        }
        if (!nonMatchingProperties.isEmpty()) {
            return ConditionOutcome.noMatch(
                    ConditionMessage.forCondition(ConditionalOnConfig.class, spec)
                            .found("different value in config",
                                    "different value in config")
                            .items(ConditionMessage.Style.QUOTE, nonMatchingProperties));
        }
        return ConditionOutcome.match(ConditionMessage
                .forCondition(ConditionalOnConfig.class, spec).because("matched"));
    }

    private static class Spec {

        private final String[] names;
        private final String havingValue;
        private final boolean matchIfMissing;
        private final boolean relaxedNames;

        Spec(AnnotationAttributes annotationAttributes) {
            this.names = getNames(annotationAttributes);
            this.havingValue = annotationAttributes.getString("havingValue");
            this.matchIfMissing = annotationAttributes.getBoolean("matchIfMissing");
            this.relaxedNames = annotationAttributes.getBoolean("relaxedNames");
        }

        private String[] getNames(Map<String, Object> annotationAttributes) {
            String[] value = (String[]) annotationAttributes.get("value");
            String[] name = (String[]) annotationAttributes.get("name");
            return (value.length > 0 ? value : name);
        }

        private String findExistConfig(String name, Config config) {
            if (config.hasPathOrNull(name)) {
                return name;
            } else if (relaxedNames) {
                RelaxedNames relaxedNames = new RelaxedNames(name);
                for (String currentName : relaxedNames) {
                    if (config.hasPathOrNull(currentName)) {
                        return currentName;
                    }
                }
            }
            return null;
        }

        private void collectProperties(List<String> missing,
                                       List<String> nonMatching) {
            Config config = ConfigFactory.load();
            for (String name : this.names) {
                String key = findExistConfig(name, config);
                if (key != null) {
                    String value = null;
                    try {
                        value = config.getString(key);
                    } catch (Exception ignored) {
                    }

                    if (!isMatch(value)) {
                        nonMatching.add(name);
                    }
                } else {
                    if (!matchIfMissing) {
                        missing.add(name);
                    }
                }
            }
        }

        private boolean isMatch(String value) {
            if (!StringUtils.isEmpty(havingValue)) {
                return havingValue.equals(value);
            } else {
                return !"false".equals(value);
            }
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("(");
            if (this.names.length == 1) {
                result.append(this.names[0]);
            } else {
                result.append("[");
                result.append(String.join(",", this.names));
                result.append("]");
            }
            result.append(")");
            return result.toString();
        }

    }

}
