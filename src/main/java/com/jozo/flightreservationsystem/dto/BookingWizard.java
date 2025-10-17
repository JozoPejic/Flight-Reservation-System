package com.jozo.flightreservationsystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class BookingWizard implements Serializable {
    private List<Integer> flightIds;
    private List<String> selectedClasses;
    private int currentIndex;
    private List<Integer> selectedSeatIds;

    public BookingWizard(List<Integer> flightIds, List<String> selectedClasses) {
        this.flightIds = flightIds;
        this.selectedClasses = selectedClasses;
        this.currentIndex = 0;
        this.selectedSeatIds = new java.util.ArrayList<>();
    }

    public boolean hasNext() {
        return currentIndex + 1 < flightIds.size();
    }

    public Integer getCurrentFlightId() {
        return flightIds.get(currentIndex);
    }

    public String getCurrentSeatClass() {
        return selectedClasses.get(currentIndex);
    }

    public void advance() {
        if (currentIndex < flightIds.size() - 1) {
            currentIndex++;
        }
    }

    public void addSelectedSeatId(int seatId) {
        if (this.selectedSeatIds == null) {
            this.selectedSeatIds = new java.util.ArrayList<>();
        }
        this.selectedSeatIds.add(seatId);
    }
}


