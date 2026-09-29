package vn.iotstar.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/error")
@RequiredArgsConstructor
public class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {

    private final Environment environment;

    @Value("${server.error.include-message:never}")
    private String includeMessage;

    @RequestMapping
    public String handleError(HttpServletRequest request, Model model) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exceptionObj = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object messageObj = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object uriObj = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = 500;
        if (statusObj != null) {
            try {
                statusCode = Integer.parseInt(statusObj.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        String uri = uriObj != null ? uriObj.toString() : "unknown";
        String exceptionMessage = (exceptionObj instanceof Throwable t)
                ? t.getMessage()
                : (messageObj != null ? messageObj.toString() : "");

        log.error("HTTP {} error at URI [{}]: {}", statusCode, uri, exceptionMessage, (Throwable) exceptionObj);

        String userFriendlyMessage;
        if (statusCode == HttpStatus.FORBIDDEN.value()) {
            userFriendlyMessage = "Bạn không có quyền truy cập trang này (403 Forbidden).";
        } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
            userFriendlyMessage = "Trang yêu cầu không tồn tại (404 Not Found).";
        } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
            userFriendlyMessage = "Yêu cầu không hợp lệ (400 Bad Request).";
        } else {
            userFriendlyMessage = "Đã xảy ra lỗi hệ thống (HTTP " + statusCode + ").";
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("message", userFriendlyMessage);
        boolean showDetail = "always".equalsIgnoreCase(includeMessage)
                || environment.matchesProfiles("dev", "development");
        if (showDetail && exceptionMessage != null && !exceptionMessage.isBlank()) {
            model.addAttribute("errorDetail", exceptionMessage);
        }
        return "error";
    }
}
