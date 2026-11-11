package com.yisy.modules.doramcu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for DuckDuckGo Instant Answer API response.
 * <p>
 * Maps the JSON response from {@code https://api.duckduckgo.com/?q=QUERY&format=json}
 * to extract abstract text, related topics, and headings for theme research.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DuckDuckGoSearchResult {

    @JsonProperty("AbstractText")
    private String abstractText;

    @JsonProperty("AbstractSource")
    private String abstractSource;

    @JsonProperty("AbstractURL")
    private String abstractUrl;

    @JsonProperty("Heading")
    private String heading;

    @JsonProperty("RelatedTopics")
    private List<RelatedTopic> relatedTopics;

    @JsonProperty("Results")
    private List<Result> results;

    @JsonProperty("NoResult")
    private String noResult;

    @JsonProperty("NoDefinitions")
    private String noDefinitions;

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getAbstractSource() {
        return abstractSource;
    }

    public void setAbstractSource(String abstractSource) {
        this.abstractSource = abstractSource;
    }

    public String getAbstractUrl() {
        return abstractUrl;
    }

    public void setAbstractUrl(String abstractUrl) {
        this.abstractUrl = abstractUrl;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public List<RelatedTopic> getRelatedTopics() {
        return relatedTopics;
    }

    public void setRelatedTopics(List<RelatedTopic> relatedTopics) {
        this.relatedTopics = relatedTopics;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public String getNoResult() {
        return noResult;
    }

    public void setNoResult(String noResult) {
        this.noResult = noResult;
    }

    public String getNoDefinitions() {
        return noDefinitions;
    }

    public void setNoDefinitions(String noDefinitions) {
        this.noDefinitions = noDefinitions;
    }

    /**
     * Checks if this result contains any meaningful data.
     *
     * @return true if abstract text, related topics, or results are present
     */
    public boolean hasContent() {
        return (abstractText != null && !abstractText.isBlank())
                || (relatedTopics != null && !relatedTopics.isEmpty())
                || (results != null && !results.isEmpty());
    }

    /**
     * Combines all available text from the search result into a single string
     * for AI analysis.
     *
     * @return concatenated text from abstract, related topics, and results
     */
    public String getAllText() {
        StringBuilder sb = new StringBuilder();
        if (heading != null && !heading.isBlank()) {
            sb.append("Heading: ").append(heading).append("\n");
        }
        if (abstractText != null && !abstractText.isBlank()) {
            sb.append("Abstract: ").append(abstractText).append("\n");
        }
        if (relatedTopics != null) {
            for (RelatedTopic topic : relatedTopics) {
                if (topic.getText() != null && !topic.getText().isBlank()) {
                    sb.append("Topic: ").append(topic.getText()).append("\n");
                }
            }
        }
        if (results != null) {
            for (Result result : results) {
                if (result.getText() != null && !result.getText().isBlank()) {
                    sb.append("Result: ").append(result.getText()).append("\n");
                }
            }
        }
        return sb.toString().trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelatedTopic {
        @JsonProperty("Text")
        private String text;

        @JsonProperty("FirstURL")
        private String firstUrl;

        @JsonProperty("Topics")
        private List<RelatedTopic> topics;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getFirstUrl() {
            return firstUrl;
        }

        public void setFirstUrl(String firstUrl) {
            this.firstUrl = firstUrl;
        }

        public List<RelatedTopic> getTopics() {
            return topics;
        }

        public void setTopics(List<RelatedTopic> topics) {
            this.topics = topics;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("Text")
        private String text;

        @JsonProperty("FirstURL")
        private String firstUrl;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getFirstUrl() {
            return firstUrl;
        }

        public void setFirstUrl(String firstUrl) {
            this.firstUrl = firstUrl;
        }
    }
}
