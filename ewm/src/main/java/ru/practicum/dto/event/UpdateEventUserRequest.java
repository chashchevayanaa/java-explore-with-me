package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.dto.location.LocationDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(value = {
        "id", "state", "createdOn", "publishedOn",
        "views", "confirmedRequests", "initiator"
})
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000)
    private String annotation;

    @Setter(AccessLevel.NONE)
    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private LocationDto location;
    private Boolean paid;

    @Min(0)
    private Integer participantLimit;

    private Boolean requestModeration;
    private String stateAction;

    @Size(min = 3, max = 120)
    private String title;

    @JsonProperty("category")
    public void setCategory(JsonNode node) {
        if (node == null || node.isNull()) {
            this.category = null;
        } else if (node.isNumber()) {
            this.category = node.asLong();
        } else if (node.isObject() && node.has("id")) {
            this.category = node.get("id").asLong();
        }
    }

    public Long getCategory() {
        return category;
    }
}