package fsa.training.travelee.controller.page;

import fsa.training.travelee.dto.ChangePasswordDto;
import fsa.training.travelee.dto.UpdateUserDto;
import fsa.training.travelee.entity.SupportRequest;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.repository.UserRepository;
import fsa.training.travelee.service.BookingService;
import fsa.training.travelee.service.SupportRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final SupportRequestService supportRequestService;
    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String showProfileForm(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UpdateUserDto dto = new UpdateUserDto();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());

        // Lấy danh sách booking của user với đầy đủ relationships
        List<Booking> userBookings = bookingService.getBookingsByUser(user);
        // Load đầy đủ thông tin cho từng booking để đảm bảo hiển thị đúng trạng thái
        for (int i = 0; i < userBookings.size(); i++) {
            try {
                Booking fullBooking = bookingService.getBookingById(userBookings.get(i).getId());
                userBookings.set(i, fullBooking);
            } catch (Exception e) {
                // Nếu có lỗi khi load booking chi tiết, giữ nguyên booking cũ
                log.warn("Không thể load chi tiết booking {}: {}", userBookings.get(i).getId(), e.getMessage());
            }
        }

        model.addAttribute("updateUserDto", dto);
        model.addAttribute("supportRequest", new SupportRequest());
        model.addAttribute("supportList", supportRequestService.findAllByUserId(user.getId()));
        model.addAttribute("loggedInUser", user);
        model.addAttribute("userBookings", userBookings);

        return "/page/profile";
    }

    @PostMapping("/profile/update")
    public String updateUser(@ModelAttribute("updateUserDto") UpdateUserDto dto, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            user.setAddress(dto.getAddress());
        }

        userRepository.save(user);
        return "redirect:/page/home";
    }
    @PostMapping("/profile/change-password")
    public String changePassword(@ModelAttribute ChangePasswordDto dto, Principal principal, RedirectAttributes ra) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            ra.addFlashAttribute("error", "Mật khẩu hiện tại không đúng.");
            return "redirect:/profile";
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            ra.addFlashAttribute("error", "Mật khẩu mới không trùng khớp.");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        ra.addFlashAttribute("success", "Đổi mật khẩu thành công.");
        return "redirect:/profile?tab=password";
    }

    @PostMapping("/profile/support")
    public String submitSupportRequestFromProfile(@ModelAttribute("supportRequest") SupportRequest request,
                                                  Authentication authentication,
                                                  RedirectAttributes ra) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        request.setUser(currentUser); // Gán user cho phản hồi
        supportRequestService.saveSupportRequest(request);

        ra.addFlashAttribute("success", "Phản hồi của bạn đã được ghi nhận!");
        return "redirect:/profile";
    }

    @PostMapping("/profile/cancel-booking/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId,
                                Authentication authentication,
                                RedirectAttributes ra) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        try {
            Booking booking = bookingService.getBookingById(bookingId);
            // Không cho hủy nếu đã thanh toán hoặc hoàn thành
            if (booking.getStatus() == fsa.training.travelee.entity.booking.BookingStatus.PAID
                    || booking.getStatus() == fsa.training.travelee.entity.booking.BookingStatus.COMPLETED) {
                ra.addFlashAttribute("error", "Không thể hủy vì booking đã thanh toán hoặc đã hoàn thành.");
                return "redirect:/profile?tab=tours";
            }

            // Hủy booking
            bookingService.cancelBooking(bookingId, "Người dùng hủy");
            ra.addFlashAttribute("success", "Đã hủy tour thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi hủy tour: " + e.getMessage());
        }

        return "redirect:/profile?tab=tours";
    }
}
