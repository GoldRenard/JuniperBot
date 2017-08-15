package ru.caramel.juniperbot.web.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.caramel.juniperbot.web.common.AbstractController;
import ru.caramel.juniperbot.web.common.navigation.Navigation;
import ru.caramel.juniperbot.web.common.navigation.PageElement;

@Controller
public class DashboardController extends AbstractController {

    @RequestMapping("/dashboard/{serverId}")
    @Navigation(PageElement.DASHBOARD)
    public ModelAndView dashboard(@PathVariable long serverId) {
        return createModel("dashboard", serverId);
    }
}
