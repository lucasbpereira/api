package br.app.moments.api.event;

import br.app.moments.api.user.ParticipantResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public class EventAllInfoResponse {
    private String title;
    private String description;
    private String location;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private int minParticipants;
    private int maxParticipants;
    private Set<ParticipantResponse> participantsList;

    public EventAllInfoResponse(String title,
        String description,
        String location,
        LocalDate eventDate,
        LocalTime eventTime,
        int minParticipants,
        int maxParticipants,
        Set<ParticipantResponse> participantsList) {
            this.title = title;
            this.description = description;
            this.location = location;
            this.eventDate = eventDate;
            this.eventTime = eventTime;
            this.minParticipants = minParticipants;
            this.maxParticipants = maxParticipants;
            this.participantsList = participantsList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalTime eventTime) {
        this.eventTime = eventTime;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public void setMinParticipants(int minParticipants) {
        this.minParticipants = minParticipants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Set<ParticipantResponse> getParticipantsList() {
        return participantsList;
    }

    public void setParticipantsList(Set<ParticipantResponse> participantsList) {
        this.participantsList = participantsList;
    }
}
