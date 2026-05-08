package com.example.busticketpro.controller;

import com.example.busticketpro.model.Ticket;
import com.example.busticketpro.model.TicketStatus;
import com.example.busticketpro.repository.TicketRepository;
import com.example.busticketpro.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private StaffService staffService;

    @GetMapping("/tickets")
    public String staffHome(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        return "staff_tickets";
    }

    /**
     * CORE-08: Danh sách vé chờ thanh toán (PENDING)
     */
    @GetMapping("/pending-tickets")
    public String pendingTickets(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        List<Ticket> pendingTickets = ticketRepository.findByStatusWithDetails(TicketStatus.PENDING);
        model.addAttribute("tickets", pendingTickets);
        return "staff_pending_tickets";
    }

    /**
     * Xác nhận thanh toán vé (PENDING -> PAID)
     */
    @PostMapping("/tickets/confirm/{id}")
    public String confirmTicket(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        String msg = staffService.confirmPayment(id);
        redirectAttrs.addFlashAttribute("successMsg", msg);
        return "redirect:/staff/pending-tickets";
    }

    /**
     * Hủy vé và giải phóng ghế
     */
    @PostMapping("/tickets/cancel/{id}")
    public String cancelTicket(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        String msg = staffService.cancelTicket(id);
        redirectAttrs.addFlashAttribute("successMsg", msg);
        return "redirect:/staff/pending-tickets";
    }
}