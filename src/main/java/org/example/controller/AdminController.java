package org.example.controller;

import jakarta.validation.Valid;
import org.example.config.SantaProperties;
import org.example.dto.ParticipantForm;
import org.example.repository.ParticipantRepository;
import org.example.service.GameException;
import org.example.service.GameService;
import org.example.service.RegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ParticipantRepository participants;
    private final RegistrationService registration;
    private final GameService game;
    private final SantaProperties properties;

    public AdminController(ParticipantRepository participants, RegistrationService registration,
                           GameService game, SantaProperties properties) {
        this.participants = participants;
        this.registration = registration;
        this.game = game;
        this.properties = properties;
    }

    /** @param reveal показать пары «кто кому дарит» — только по явному запросу организатора */
    @GetMapping
    public String page(@RequestParam(defaultValue = "false") boolean reveal, Model model) {
        model.addAttribute("form", new ParticipantForm());
        model.addAttribute("reveal", reveal);
        return render(model);
    }

    @PostMapping("/participants")
    public String add(@Valid @ModelAttribute("form") ParticipantForm form, BindingResult errors,
                      Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return render(model);
        }
        try {
            String name = registration.addByAdmin(form).getFullName();
            redirect.addFlashAttribute("message", "Участник " + name + " добавлен");
            return "redirect:/admin";
        } catch (GameException e) {
            model.addAttribute("error", e.getMessage());
            return render(model);
        }
    }

    @PostMapping("/participants/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        return run(redirect, () -> {
            game.deleteParticipant(id);
            return "Участник удалён";
        });
    }

    @PostMapping("/draw")
    public String draw(RedirectAttributes redirect) {
        return run(redirect, () -> {
            var pairs = game.runDraw();
            long sent = pairs.stream().filter(r -> r.isMailSent()).count();
            return "Жеребьёвка проведена. Писем отправлено: " + sent + " из " + pairs.size();
        });
    }

    @PostMapping("/resend")
    public String resend(RedirectAttributes redirect) {
        return run(redirect, () -> "Повторно отправлено писем: " + game.resendFailed());
    }

    @PostMapping("/reset")
    public String reset(RedirectAttributes redirect) {
        return run(redirect, () -> {
            game.reset();
            return "Жеребьёвка сброшена";
        });
    }

    private String render(Model model) {
        model.addAttribute("participants", participants.findAllByOrderByRegisteredAtAsc());
        var pairs = game.pairs();
        model.addAttribute("pairs", pairs);
        model.addAttribute("sentCount", pairs.stream().filter(r -> r.isMailSent()).count());
        model.addAttribute("failed", pairs.stream().filter(r -> !r.isMailSent()).toList());
        model.addAttribute("reveal", Boolean.TRUE.equals(model.getAttribute("reveal")));
        model.addAttribute("drawDone", registration.isDrawDone());
        model.addAttribute("deadlinePassed", registration.isDeadlinePassed());
        model.addAttribute("deadline", HomeController.DEADLINE_FORMAT.format(properties.registrationDeadline()));
        model.addAttribute("allInterests", properties.interests());
        return "admin";
    }

    private String run(RedirectAttributes redirect, java.util.function.Supplier<String> action) {
        try {
            redirect.addFlashAttribute("message", action.get());
        } catch (GameException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
}
