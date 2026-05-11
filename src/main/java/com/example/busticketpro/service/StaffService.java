package com.example.busticketpro.service;

import com.example.busticketpro.model.*;
import com.example.busticketpro.repository.SeatRepository;
import com.example.busticketpro.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffService {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private SeatRepository seatRepository;
// Xác nhận hành khách đã trả tiền cho vé
    @Transactional(rollbackFor = Exception.class)
    public String confirmPayment(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé ID: " + ticketId));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            return "Vé này đã được xử lý trước đó!";
        }

        ticket.setStatus(TicketStatus.PAID);
        ticketRepository.save(ticket);

        Seat seat = ticket.getSeat();
        seat.setStatus(SeatStatus.BOOKED);
        seat.setLockedAt(null);
        seatRepository.save(seat);

        return "Đã xác nhận thanh toán vé: " + ticket.getTicketCode();
    }
//  Hủy vé đang chờ thanh toán theo yêu cầu hoặc do vi phạm quy định.
    @Transactional(rollbackFor = Exception.class)
    public String cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé ID: " + ticketId));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            return "Vé này đã được xử lý trước đó!";
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);

        Seat seat = ticket.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setLockedAt(null);
        seatRepository.save(seat);

        return "Đã hủy vé: " + ticket.getTicketCode() + " và giải phóng ghế " + seat.getSeatNumber();
    }
}