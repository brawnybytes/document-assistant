package com.brawnybytes.docassistant.generation;

import java.util.List;

public record AnswerResponse(String answer, List<SourcePage> sources) {
}