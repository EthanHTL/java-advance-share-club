package club.smileboy.app.services.impl;

import club.smileboy.app.services.HumanResourceService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StubHumanResourceService implements HumanResourceService {

    // stub method implementation
    public void bookHoliday(Date startDate, Date endDate, String name) {
        System.out.println("Booking holiday for [" + startDate + "-" + endDate + "] for [" + name + "] ");
    }
}