package com.example.busticketpro.service;

import com.example.busticketpro.model.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendBookingConfirmation(Ticket ticket) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(ticket.getEmail());
            helper.setSubject("Xác nhận đặt vé - " + ticket.getTicketCode());
            helper.setText(buildBookingEmailContent(ticket), true);
            mailSender.send(message);
            System.out.println("Đã gửi email xác nhận vé: " + ticket.getTicketCode());
        } catch (Exception e) {
            System.err.println("Lỗi gửi email xác nhận: " + e.getMessage());
        }
    }

    // Gửi khi hành khách tự hủy vé
    @Async
    public void sendCancelNotification(Ticket ticket) {
        try {
            if (ticket.getEmail() == null || ticket.getEmail().isEmpty()) return;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(ticket.getEmail());
            helper.setSubject("Xác nhận hủy vé - " + ticket.getTicketCode());
            helper.setText(buildManualCancelEmailContent(ticket), true);
            mailSender.send(message);
            System.out.println("Đã gửi email hủy vé (thủ công): " + ticket.getTicketCode());
        } catch (Exception e) {
            System.err.println("Lỗi gửi email hủy: " + e.getMessage());
        }
    }

    // Gửi khi hệ thống tự động hủy do quá 15 phút
    @Async
    public void sendAutoCancelNotification(Ticket ticket) {
        try {
            if (ticket.getEmail() == null || ticket.getEmail().isEmpty()) return;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(ticket.getEmail());
            helper.setSubject("Vé đã bị hủy tự động - " + ticket.getTicketCode());
            helper.setText(buildAutoCancelEmailContent(ticket), true);
            mailSender.send(message);
            System.out.println("Đã gửi email hủy tự động: " + ticket.getTicketCode());
        } catch (Exception e) {
            System.err.println("Lỗi gửi email hủy tự động: " + e.getMessage());
        }
    }

    private String buildBookingEmailContent(Ticket ticket) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                <h2 style="color: #0d6efd;">🚌 Bus Ticket Pro - Xác nhận đặt vé</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Bạn đã đặt vé thành công! Dưới đây là thông tin vé của bạn:</p>
                <table style="width: 100%%; border-collapse: collapse;">
                    <tr style="background: #f8f9fa;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Mã vé</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Tuyến đường</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s → %s</td>
                    </tr>
                    <tr style="background: #f8f9fa;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Giờ khởi hành</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Số ghế</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                    </tr>
                    <tr style="background: #f8f9fa;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Giá vé</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s đ</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Trạng thái</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd; color: orange;"><strong>Chờ thanh toán</strong></td>
                    </tr>
                </table>
                <p style="margin-top: 20px; color: #dc3545;">⚠️ Vui lòng thanh toán trong vòng <strong>15 phút</strong>, nếu không vé sẽ bị hủy tự động.</p>
                <p>Cảm ơn bạn đã sử dụng dịch vụ!</p>
                <p style="color: #6c757d; font-size: 12px;">Bus Ticket Pro - Hệ thống đặt vé xe khách liên tỉnh</p>
            </div>
            """.formatted(
                ticket.getPassengerName(),
                ticket.getTicketCode(),
                ticket.getTrip().getRoute().getFromLocation().getName(),
                ticket.getTrip().getRoute().getToLocation().getName(),
                ticket.getTrip().getDepartureTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                ticket.getSeat().getSeatNumber(),
                String.format("%,.0f", ticket.getTotalPrice().doubleValue())
        );
    }

    // Nội dung email khi hành khách tự hủy
    private String buildManualCancelEmailContent(Ticket ticket) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                <h2 style="color: #dc3545;">🚌 Bus Ticket Pro - Xác nhận hủy vé</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Vé <strong>%s</strong> của bạn đã được hủy thành công theo yêu cầu.</p>
                <p>Nếu bạn vẫn muốn đi, vui lòng đặt vé lại trên hệ thống.</p>
                <p>Cảm ơn bạn đã sử dụng dịch vụ!</p>
                <p style="color: #6c757d; font-size: 12px;">Bus Ticket Pro - Hệ thống đặt vé xe khách liên tỉnh</p>
            </div>
            """.formatted(ticket.getPassengerName(), ticket.getTicketCode());
    }

    // Nội dung email khi hệ thống tự động hủy
    private String buildAutoCancelEmailContent(Ticket ticket) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                <h2 style="color: #dc3545;">🚌 Bus Ticket Pro - Vé đã bị hủy tự động</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Vé <strong>%s</strong> của bạn đã bị hủy tự động do quá <strong>15 phút</strong> chưa thanh toán.</p>
                <p>Nếu bạn vẫn muốn đi, vui lòng đặt vé lại trên hệ thống.</p>
                <p style="color: #6c757d; font-size: 12px;">Bus Ticket Pro - Hệ thống đặt vé xe khách liên tỉnh</p>
            </div>
            """.formatted(ticket.getPassengerName(), ticket.getTicketCode());
    }
}