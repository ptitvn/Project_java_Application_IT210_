package com.example.busticketpro.service;

import com.example.busticketpro.model.SeatStatus;
import com.example.busticketpro.model.Ticket;
import com.example.busticketpro.model.TicketStatus;
import com.example.busticketpro.repository.SeatRepository;
import com.example.busticketpro.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AutoCancelService {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private EmailService emailService;

    //  Chạy mỗi 5 phút, hủy vé PENDING quá 15 phút
    @Scheduled(fixedRate = 60000) // 1 phút
    @Transactional
    public void autoCancelExpiredTickets() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(15);
        List<Ticket> expiredTickets = ticketRepository.findExpiredPendingTickets(expiredTime);

        if (expiredTickets.isEmpty()) return;

        System.out.println("Đang hủy " + expiredTickets.size() + " vé hết hạn...");

        for (Ticket ticket : expiredTickets) {
            ticket.setStatus(TicketStatus.CANCELLED);
            ticketRepository.save(ticket);

            ticket.getSeat().setStatus(SeatStatus.AVAILABLE);
            ticket.getSeat().setLockedAt(null);
            seatRepository.save(ticket.getSeat());

            emailService.sendAutoCancelNotification(ticket);
            System.out.println("Đã hủy vé: " + ticket.getTicketCode());
        }
    }
}